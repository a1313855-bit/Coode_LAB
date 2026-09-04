package com.example.demo.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/coode_lab/upload")
public class FileUploadController {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @PostMapping
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "請選擇檔案"));
        }

        try {
            String original = file.getOriginalFilename();
            String ext = "";
            if (original != null && original.lastIndexOf('.') >= 0) {
                ext = original.substring(original.lastIndexOf('.'));
                if (ext.length() > 12) ext = "";
            }

            String filename = UUID.randomUUID().toString().replace("-", "") + ext;

            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(filename).toFile());

            return ResponseEntity.ok(Map.of("url", "/images/products/upload/" + filename));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "圖片上傳失敗：" + e.getMessage()));
        }
    }
}
