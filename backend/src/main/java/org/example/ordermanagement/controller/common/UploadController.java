package org.example.ordermanagement.controller.common;

import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.result.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 全局通用文件上传中心 - 统一处理系统内产生的图片资源存储请求。
 * 现阶段已分离隔离处理头像及店铺/菜品配图。
 */
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    @Value("${upload.path:}")
    private String uploadPath;

    /**
     * 专注处理用户头像上传逻辑
     */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return uploadImage(file, "avatars", "/static/avatars/");
    }

    /**
     * 专注处理商家环境图、菜单内菜品配图的上传逻辑
     */
    @PostMapping("/image")
    public Result<String> uploadShopImage(@RequestParam("file") MultipartFile file) {
        return uploadImage(file, "images", "/static/images/");
    }

    private Result<String> uploadImage(MultipartFile file, String subDir, String urlPrefix) {
        if (file.isEmpty()) {
            return Result.fail("文件不能为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.fail("只能上传图片文件");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.fail("文件大小不能超过5MB");
        }
        try {
            String projectPath = System.getProperty("user.dir");
            if (uploadPath == null || uploadPath.isEmpty()) {
                uploadPath = projectPath + File.separator + "uploads";
            }
            Path uploadDir = Paths.get(uploadPath, subDir);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;
            Path filePath = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return Result.success(urlPrefix + fileName);
        } catch (IOException e) {
            return Result.fail("文件上传失败: " + e.getMessage());
        }
    }
}
