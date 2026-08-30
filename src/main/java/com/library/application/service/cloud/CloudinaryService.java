package com.library.application.service.cloud;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file){
        Map<?, ?> result = null;
        try {
            result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("resource_type", "image", "folder", "book-covers")
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result.get("secure_url").toString();
    }

}
