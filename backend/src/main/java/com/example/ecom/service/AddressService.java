package com.example.ecom.service;

import com.example.ecom.model.Address;
import com.example.ecom.payload.AddressDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface AddressService {
    AddressDTO createAddress(AddressDTO addressDTO);

    List<AddressDTO> getAllAddress();

    AddressDTO getAddressById(Long addressId);

    AddressDTO updateAddress( Long addressId, AddressDTO addressDTO);

    String deleteAddress( Long addressId);

    Address findAddressById(Long addressId);
}
