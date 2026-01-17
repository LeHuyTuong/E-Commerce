package com.example.ecom.service.impl;

import com.example.ecom.model.User;
import com.example.ecom.repositories.UserRepository;
import com.example.ecom.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User existsByUsername(String username) {
        return null;
    }
}
