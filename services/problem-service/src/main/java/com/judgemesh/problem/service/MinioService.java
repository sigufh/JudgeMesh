package com.judgemesh.problem.service;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    /**
     * 上传文件到 MinIO
     */
    public String uploadFile(String bucketName, String objectName, MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            // 兜底：如果获取不到 ContentType，默认用二进制流
            String contentType = file.getContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "application/octet-stream";
            }

            minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(inputStream, file.getSize(), -1)
                .contentType(contentType) // 使用兜底后的 contentType
                .build());
            return objectName;
        } catch (Exception e) {
            log.error("上传文件到 MinIO 失败，bucket: {}, object: {}", bucketName, objectName, e);
            throw new RuntimeException("上传文件失败");
        }
    }

    /**
     * 获取带 5 分钟过期时间的预签名下载链接（供 Go Worker 使用）
     */
    public String getPresignedUrl(String bucketName, String objectName, int expiryMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucketName)
                .object(objectName)
                .expiry(expiryMinutes, TimeUnit.MINUTES)
                .build());
        } catch (Exception e) {
            log.error("生成预签名链接失败，bucket: {}, object: {}", bucketName, objectName, e);
            throw new RuntimeException("生成预签名链接失败");
        }
    }
}
