package ru.dmitartur.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) throws Exception {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        logger.info("📁 File storage location initialized: {}", this.fileStorageLocation);

        try {
            Files.createDirectories(this.fileStorageLocation);
            logger.info("✅ File storage directory created/verified successfully");
        } catch (Exception ex) {
            logger.error("❌ Failed to create file storage directory: {}", ex.getMessage());
            throw new Exception("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public Resource loadFile(String filename) throws MalformedURLException {
        logger.debug("📂 Loading file: {}", filename);
        
        Path filePath = this.fileStorageLocation.resolve(filename).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists() || resource.isReadable()) {
            logger.debug("✅ File loaded successfully: {}", filename);
            return resource;
        } else {
            logger.error("❌ Could not read file: {}", filename);
            throw new RuntimeException("Could not read file: " + filename);
        }
    }

    public String storeFile(MultipartFile file) throws Exception {
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        logger.info("💾 Storing file: originalName={}, size={} bytes", originalFileName, file.getSize());

        // Генерируем уникальное имя файла
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + fileExtension;
        logger.debug("🔄 Generated new filename: {}", newFileName);

        // Копируем файл в целевое место
        Path targetLocation = this.fileStorageLocation.resolve(newFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        logger.info("✅ File stored successfully: originalName={}, storedName={}", originalFileName, newFileName);
        return newFileName;
    }
}