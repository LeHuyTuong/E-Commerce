package com.example.ecom.service;

import com.example.ecom.model.User;

public interface UserService {
    User existsByUsername(String username);
}
