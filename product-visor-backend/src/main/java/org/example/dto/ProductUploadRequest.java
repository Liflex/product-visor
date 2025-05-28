package org.example.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProductUploadRequest {
    private String productData;
    private MultipartFile image;
}
