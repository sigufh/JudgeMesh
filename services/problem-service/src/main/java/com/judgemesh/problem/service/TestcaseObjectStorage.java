package com.judgemesh.problem.service;

import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Service
public class TestcaseObjectStorage {
    private static final Logger log = LoggerFactory.getLogger(TestcaseObjectStorage.class);

    private final MinioClient minioClient;
    private final String bucket;
    private final boolean failOnUnavailable;

    public TestcaseObjectStorage(
            ObjectProvider<MinioClient> minioClient,
            @Value("${minio.buckets.testcases:testcases}") String bucket,
            @Value("${minio.fail-on-unavailable:false}") boolean failOnUnavailable) {
        this.minioClient = minioClient.getIfAvailable();
        this.bucket = bucket;
        this.failOnUnavailable = failOnUnavailable;
    }

    public String putText(Long problemId, Integer caseIndex, String suffix, String content) {
        String key = "problem-" + problemId + "/" + caseIndex + "." + suffix;
        if (minioClient == null) {
            return dataUrl(content);
        }
        try {
            ensureBucket();
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                    .contentType("text/plain; charset=utf-8")
                    .build());
            return key;
        } catch (Exception ex) {
            if (failOnUnavailable) {
                throw new IllegalStateException("MinIO testcase write failed: " + key, ex);
            }
            log.warn("MinIO unavailable, falling back to data URL for {}", key, ex);
            return dataUrl(content);
        }
    }

    public String readUrl(String objectOrInlineContent) {
        if (objectOrInlineContent == null || objectOrInlineContent.isBlank()) {
            return dataUrl("");
        }
        if (objectOrInlineContent.startsWith("data:") || objectOrInlineContent.startsWith("http://")
                || objectOrInlineContent.startsWith("https://")) {
            return objectOrInlineContent;
        }
        if (!objectOrInlineContent.startsWith("problem-")) {
            return dataUrl(objectOrInlineContent);
        }
        if (minioClient == null) {
            return dataUrl(objectOrInlineContent);
        }
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket)
                    .object(objectOrInlineContent)
                    .expiry(5, TimeUnit.MINUTES)
                    .build());
        } catch (Exception ex) {
            if (failOnUnavailable) {
                throw new IllegalStateException("MinIO testcase presign failed: " + objectOrInlineContent, ex);
            }
            log.warn("MinIO unavailable, falling back to data URL for {}", objectOrInlineContent, ex);
            return dataUrl(objectOrInlineContent);
        }
    }

    private void ensureBucket() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }

    private static String dataUrl(String value) {
        String encoded = Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
        return "data:text/plain;base64," + encoded;
    }
}
