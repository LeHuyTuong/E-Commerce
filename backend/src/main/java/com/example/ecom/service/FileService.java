package com.example.ecom.service;

import com.example.ecom.payload.ProductDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {

    String uploadImage(String path, MultipartFile imageFile) throws IOException;
}
