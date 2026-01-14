package com.example.ecom.repositories;

import com.example.ecom.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address,Long> {
    Address findByAddressId(Long addressId);
}
