package ru.dmitartur.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProductUploadRequest {
    private String productData;
    private MultipartFile image;
    
    @Override
    public String toString() {
        return "ProductUploadRequest{" +
                "productData='" + productData + '\'' +
                ", image=" + (image != null ? "present(" + image.getSize() + " bytes)" : "null") +
                '}';
    }
}
