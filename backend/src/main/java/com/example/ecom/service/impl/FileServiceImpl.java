package com.example.ecom.service.impl;

import com.example.ecom.exceptions.ResourceNotFoundException;
import com.example.ecom.model.Product;
import com.example.ecom.payload.ProductDTO;
import com.example.ecom.service.FileService;
import com.example.ecom.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final ModelMapper modelMapper;

    public String uploadImage(String path, MultipartFile imageFile) throws IOException {
        // FIle names of current / original file
        String originalFileName = imageFile.getOriginalFilename();
        // Generate a unique file name
        String randomId = UUID.randomUUID().toString();
        // mat.jpg -> 1234 -> 1234.jpg
        String fileName = randomId.concat(originalFileName.substring(originalFileName.lastIndexOf('.')));
        String filePath = path + File.separator + fileName;
        //                 path + "/" + fileName;
        // Check if path exists and create
        File folder = new File(path);
        if(!folder.exists()){
            folder.mkdir();
        }
        // Upload to server
        Files.copy(imageFile.getInputStream(), Paths.get(filePath));
        // return file name
        return fileName;
    }
}
