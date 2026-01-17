package com.example.ecom.controller;

import com.example.ecom.config.AppConstants;
import com.example.ecom.model.Category;
import com.example.ecom.payload.CategoryDTO;
import com.example.ecom.payload.CategoryResponse;
import com.example.ecom.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/public/categories")
    public ResponseEntity<CategoryResponse> getAllCategories(
            @RequestParam(value = "pageNumber" , defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_CATEGORIES_BY ,required = false ) String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = AppConstants.SORT_DIR , required = false) String sortOrder
            ){
        CategoryResponse categories = categoryService.getAllCategories(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }


    @PostMapping("/public/categories")
    //@RequestMapping(value = "/public/categories", method = RequestMethod.POST)
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO category){
        CategoryDTO categoryDTO = categoryService.createCategory(category);
        return new ResponseEntity<>(categoryDTO, HttpStatus.CREATED);
    }

    @DeleteMapping("/admin/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> deleteCategory(@PathVariable Long categoryId){
        CategoryDTO deletedCategory = categoryService.deleteCategory(categoryId);
        return new ResponseEntity<>(deletedCategory, HttpStatus.OK);
    }


    @PutMapping("/public/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(@Valid @RequestBody CategoryDTO category,
                                                       @PathVariable Long categoryId){
        CategoryDTO updateCategory =  categoryService.updateCategory(category,categoryId);
        return new ResponseEntity<>(updateCategory, HttpStatus.OK);
    }

}
