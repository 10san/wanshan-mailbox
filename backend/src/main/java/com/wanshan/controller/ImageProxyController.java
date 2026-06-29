package com.wanshan.controller;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.io.OutputStream;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageProxyController {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @GetMapping("/**")
    public void proxy(HttpServletRequest request, HttpServletResponse response) {
        String fullPath = request.getRequestURI();
        // 去掉 /api/v1/images/ 前缀，得到 MinIO object name
        String objectName = fullPath.replace("/api/v1/images/", "");

        try (InputStream is = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucket).object(objectName).build())) {

            // 根据扩展名设置 Content-Type
            String contentType = "image/jpeg";
            if (objectName.endsWith(".png")) contentType = "image/png";
            else if (objectName.endsWith(".webp")) contentType = "image/webp";
            else if (objectName.endsWith(".gif")) contentType = "image/gif";

            response.setContentType(contentType);
            response.setHeader("Cache-Control", "public, max-age=604800");

            OutputStream os = response.getOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) != -1) {
                os.write(buf, 0, n);
            }
            os.flush();
        } catch (Exception e) {
            response.setStatus(404);
        }
    }
}
