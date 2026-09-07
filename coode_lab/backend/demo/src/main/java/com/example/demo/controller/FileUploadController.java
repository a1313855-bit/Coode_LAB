package com.example.demo.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.repository.ProductRepository;

@RestController
@RequestMapping("/coode_lab/upload")
public class FileUploadController {

    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Autowired
    private ProductRepository productRepository;

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

    // 商品規格圖片上傳：uploads/products/{productId}/{color}/{product.jpg | outfit.png}
    // 同一顏色不同尺寸共用同一組圖片（URL 相同）。
    @PostMapping("/products/{productId}/{color}/{filename}")
    public ResponseEntity<Map<String, Object>> uploadVariant(
            @PathVariable Long productId,
            @PathVariable String color,
            @PathVariable String filename,
            @RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "請選擇檔案"));
        }

        if (!"product.jpg".equals(filename) && !"outfit.png".equals(filename)) {
            return ResponseEntity.badRequest().body(Map.of("error", "檔名只能是 product.jpg（商品圖）或 outfit.png（試穿圖）"));
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            return ResponseEntity.status(413).body(Map.of("error", "圖片大小不得超過 10MB"));
        }

        String contentType = file.getContentType();
        if (contentType == null
                || !(contentType.equalsIgnoreCase("image/jpeg")
                        || contentType.equalsIgnoreCase("image/png"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "僅支援 JPG/JPEG 或 PNG 圖片"));
        }

        if (productId == null || productId <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "商品 ID 不正確"));
        }

        if (!productRepository.existsById(productId)) {
            return ResponseEntity.status(404).body(Map.of("error", "找不到商品 ID: " + productId));
        }

        String cleanColor = sanitizeColor(color);
        if (cleanColor.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "顏色不能為空"));
        }

        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize()
                    .resolve("products")
                    .resolve(String.valueOf(productId))
                    .resolve(cleanColor);
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(filename).toFile());

            String url = "/images/products/upload/products/" + productId + "/" + cleanColor + "/" + filename;
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "圖片上傳失敗：" + e.getMessage()));
        }
    }

    private String sanitizeColor(String color) {
        if (color == null) return "";
        String clean = color.trim().toLowerCase()
                .replaceAll("[^a-z0-9\\-]", "");
        if (clean.length() > 50) {
            clean = clean.substring(0, 50);
        }
        return clean;
    }

}
