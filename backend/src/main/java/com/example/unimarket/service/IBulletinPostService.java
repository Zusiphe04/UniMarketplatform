package com.example.unimarket.service;

import com.example.unimarket.domain.enums.BulletinPostType;
import com.example.unimarket.request.CreateBulletinPostRequest;
import com.example.unimarket.request.UpdateBulletinPostRequest;
import com.example.unimarket.response.BulletinPostResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface IBulletinPostService {
    Page<BulletinPostResponse> listPublished(BulletinPostType type, int page, int size);
    BulletinPostResponse getPublished(UUID postId);
    Page<BulletinPostResponse> listOwn(UUID authorId, int page, int size);
    BulletinPostResponse create(UUID authorId, CreateBulletinPostRequest request);
    BulletinPostResponse update(UUID authorId, UUID postId, UpdateBulletinPostRequest request);
    BulletinPostResponse publish(UUID authorId, UUID postId);
    BulletinPostResponse archive(UUID authorId, UUID postId);
    void moderateRemove(UUID moderatorId, UUID postId);
}
