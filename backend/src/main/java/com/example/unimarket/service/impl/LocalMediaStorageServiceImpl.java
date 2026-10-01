package com.example.unimarket.service.impl;

import com.example.unimarket.exception.ValidationException;
import com.example.unimarket.service.IMediaStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class LocalMediaStorageServiceImpl implements IMediaStorageService {
    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/gif", "gif",
            "image/webp", "webp"
    );

    private final Path uploadRoot;
    private final long maximumBytes;

    public LocalMediaStorageServiceImpl(
            @Value("${unimarket.media.upload-dir:./uploads}") String uploadDirectory,
            @Value("${unimarket.media.max-image-bytes:5242880}") long maximumBytes) {
        this.uploadRoot = Path.of(uploadDirectory).toAbsolutePath().normalize();
        this.maximumBytes = maximumBytes;
    }

    @Override
    public String storeImage(MultipartFile file, String collection) {
        if (file == null || file.isEmpty()) throw new ValidationException("Choose an image to upload.");
        if (file.getSize() > maximumBytes) throw new ValidationException("Images must be 5 MB or smaller.");

        String contentType = file.getContentType() == null
                ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        String extension = EXTENSIONS.get(contentType);
        if (extension == null) throw new ValidationException("Only PNG, JPEG, GIF, and WebP images are supported.");
        if (!hasExpectedSignature(file, contentType)) throw new ValidationException("The uploaded file is not a valid image.");
        if (collection == null || !collection.matches("[a-z0-9-]+")) {
            throw new IllegalArgumentException("Invalid media collection.");
        }

        String filename = UUID.randomUUID() + "." + extension;
        Path collectionDirectory = uploadRoot.resolve(collection).normalize();
        Path destination = collectionDirectory.resolve(filename).normalize();
        if (!destination.startsWith(uploadRoot)) throw new ValidationException("The image destination is invalid.");

        try (InputStream input = file.getInputStream()) {
            Files.createDirectories(collectionDirectory);
            Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new ValidationException("The image could not be stored. Please try again.");
        }
        return "/uploads/" + collection + "/" + filename;
    }

    private boolean hasExpectedSignature(MultipartFile file, String contentType) {
        byte[] header = new byte[12];
        int length;
        try (InputStream input = file.getInputStream()) {
            length = input.read(header);
        } catch (IOException exception) {
            return false;
        }
        return switch (contentType) {
            case "image/jpeg" -> length >= 3 && unsigned(header[0]) == 0xff
                    && unsigned(header[1]) == 0xd8 && unsigned(header[2]) == 0xff;
            case "image/png" -> length >= 8 && unsigned(header[0]) == 0x89
                    && header[1] == 'P' && header[2] == 'N' && header[3] == 'G'
                    && unsigned(header[4]) == 0x0d && unsigned(header[5]) == 0x0a
                    && unsigned(header[6]) == 0x1a && unsigned(header[7]) == 0x0a;
            case "image/gif" -> length >= 6 && header[0] == 'G' && header[1] == 'I'
                    && header[2] == 'F' && header[3] == '8'
                    && (header[4] == '7' || header[4] == '9') && header[5] == 'a';
            case "image/webp" -> length >= 12 && header[0] == 'R' && header[1] == 'I'
                    && header[2] == 'F' && header[3] == 'F' && header[8] == 'W'
                    && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';
            default -> false;
        };
    }

    private int unsigned(byte value) {
        return value & 0xff;
    }
}
