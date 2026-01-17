package com.example.ecom.service.impl;

import com.example.ecom.exceptions.APIException;
import com.example.ecom.exceptions.ResourceNotFoundException;
import com.example.ecom.model.Address;
import com.example.ecom.model.User;
import com.example.ecom.payload.AddressDTO;
import com.example.ecom.repositories.AddressRepository;
import com.example.ecom.repositories.UserRepository;
import com.example.ecom.service.AddressService;
import com.example.ecom.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AuthUtil authUtil;
    private final ModelMapper modelMapper;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO) {
        User user = authUtil.loggedInUser();
        Address address = modelMapper.map(addressDTO, Address.class);
        List<Address> addressList = user.getAddresses();
        addressList.add(address);
        user.setAddresses(addressList);

        address.setUser(user);
        Address savedAddress = addressRepository.save(address);

        return modelMapper.map(savedAddress, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAllAddress() {
        User user = authUtil.loggedInUser();
        List<Address> addresses = user.getAddresses();
        if (addresses == null || addresses.isEmpty()) {
            return List.of(); // Return empty list instead of throwing exception
        }

        List<AddressDTO> addressDTOS = addresses.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .collect(Collectors.toList());
        return addressDTOS;
    }

    @Override
    public AddressDTO getAddressById(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("address", "address", addressId));
        return modelMapper.map(address, AddressDTO.class);
    }

    @Override
    public AddressDTO updateAddress(Long addressId, AddressDTO addressDTO) {
        Address addressFromDB = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("address", "address", addressId));

        addressFromDB.setCity(addressDTO.getCity());
        addressFromDB.setState(addressDTO.getState());
        addressFromDB.setCountry(addressDTO.getCountry());
        addressFromDB.setPincode(addressDTO.getPincode());
        addressFromDB.setStreet(addressDTO.getStreet());
        addressFromDB.setBuildingName(addressDTO.getBuildingName());

        Address updatedAddress = addressRepository.save(addressFromDB);

        User user = addressFromDB.getUser();
        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressFromDB.getAddressId()));
        user.getAddresses().add(updatedAddress);
        userRepository.save(user);
        return modelMapper.map(updatedAddress, AddressDTO.class);
    }

    @Override
    public String deleteAddress(Long addressId) {
        Address address = addressRepository.findById(addressId).orElseThrow(
                () -> new ResourceNotFoundException("address", "address", addressId));
        User user = address.getUser();
        user.getAddresses().removeIf(addr -> addr.getAddressId().equals(addressId));
        userRepository.save(user);

        addressRepository.delete(address);
        return "Address deleted successfully";
    }

    @Override
    public Address findAddressById(Long addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("address", "address", addressId));
    }

}
