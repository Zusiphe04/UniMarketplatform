package com.example.unimarket.service;

import org.springframework.web.multipart.MultipartFile;

public interface IMediaStorageService {
    String storeImage(MultipartFile file, String collection);
}
