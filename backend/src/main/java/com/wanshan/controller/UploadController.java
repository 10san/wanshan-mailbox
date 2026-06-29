package com.wanshan.controller;

import com.wanshan.common.Result;
import com.wanshan.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class UploadController {

    private final FileStorageService fileStorageService;

    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        // 校验
        if (file.isEmpty()) {
            return Result.error("请选择图片");
        }
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/"))) {
            return Result.error("仅支持图片格式");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.error("图片大小不能超过 5MB");
        }

        try {
            String url = fileStorageService.uploadImage(file);
            return Result.success(Map.of("url", url));
        } catch (Exception e) {
            return Result.error("图片上传失败，请稍后重试");
        }
    }
}
