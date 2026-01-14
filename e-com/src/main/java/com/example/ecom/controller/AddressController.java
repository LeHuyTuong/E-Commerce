package com.example.ecom.controller;

import com.example.ecom.payload.AddressDTO;
import com.example.ecom.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressDTO> createAddress(@Valid @RequestBody AddressDTO addressDTO) {
        AddressDTO savedAddressDTO = addressService.createAddress(addressDTO);
        return new ResponseEntity<AddressDTO>(savedAddressDTO, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AddressDTO>> getAddress() {
        List<AddressDTO> savedAddressDTO = addressService.getAllAddress();
        return new ResponseEntity<List<AddressDTO>>(savedAddressDTO, HttpStatus.OK);
    }
    @GetMapping("/{addressId}")
    public ResponseEntity<AddressDTO> getAddress(@PathVariable  Long addressId) {
        AddressDTO savedAddressDTO = addressService.getAddressById(addressId);
        return new ResponseEntity<AddressDTO>(savedAddressDTO, HttpStatus.OK);
    }

    @PutMapping("{addressId}")
    public ResponseEntity<AddressDTO> updateAddress(@PathVariable  Long addressId
            ,@Valid @RequestBody AddressDTO addressDTO) {
        AddressDTO updatedAddressDTO = addressService.updateAddress(addressId,addressDTO);
        return new ResponseEntity<AddressDTO>(updatedAddressDTO, HttpStatus.OK);
    }

    @DeleteMapping("{addressId}")
    public ResponseEntity<String> deleteAddress(@Valid @PathVariable Long addressId) {
        String deletedAddressDTO = addressService.deleteAddress(addressId);
        return new ResponseEntity<String>(deletedAddressDTO, HttpStatus.OK);
    }
}
