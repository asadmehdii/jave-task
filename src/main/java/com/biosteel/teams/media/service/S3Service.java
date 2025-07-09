package com.biosteel.teams.media.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.url}")
    private String s3BaseUrl;

    public Resource getFileAsResource(String key) {
        try {
            S3Object s3Object = amazonS3.getObject(bucketName, key);
            S3ObjectInputStream inputStream = s3Object.getObjectContent();

            return new InputStreamResource(inputStream);
        } catch (AmazonServiceException e) {
            log.error("AWS S3 service error while getting file: {}", key, e);
            throw new RuntimeException("Failed to get file from S3: " + e.getMessage(), e);
        } catch (SdkClientException e) {
            log.error("AWS SDK client error while getting file: {}", key, e);
            throw new RuntimeException("Failed to get file from S3 due to SDK client error: " + e.getMessage(), e);
        }
    }

    public ObjectMetadata getFileMetadata(String key) {
        try {
            return amazonS3.getObjectMetadata(bucketName, key);
        } catch (AmazonServiceException e) {
            log.error("AWS S3 service error while getting metadata: {}", key, e);
            throw new RuntimeException("Failed to get file metadata from S3: " + e.getMessage(), e);
        } catch (SdkClientException e) {
            log.error("AWS SDK client error while getting metadata: {}", key, e);
            throw new RuntimeException("Failed to get file metadata from S3 due to SDK client error: " + e.getMessage(),
                    e);
        }
    }

    public String uploadFile(MultipartFile file, String key) throws IOException {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            // Add additional metadata if needed
            metadata.addUserMetadata("originalFilename", file.getOriginalFilename());
            metadata.addUserMetadata("uploadTimestamp", String.valueOf(System.currentTimeMillis()));

            PutObjectRequest request = new PutObjectRequest(
                    bucketName,
                    key,
                    file.getInputStream(),
                    metadata);

            amazonS3.putObject(request);
            log.info("Successfully uploaded file to S3: {}", key);

            return String.format("%s/%s", s3BaseUrl, key);

        } catch (AmazonServiceException e) {
            log.error("AWS S3 service error while uploading file: {}", key, e);
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage(), e);
        } catch (SdkClientException e) {
            log.error("AWS SDK client error while uploading file: {}", key, e);
            throw new RuntimeException("Failed to upload file to S3 due to SDK client error: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("IO error while uploading file: {}", key, e);
            throw new IOException("Failed to read file contents: " + e.getMessage(), e);
        }
    }

    public void deleteFile(String key) {
        try {
            DeleteObjectRequest deleteRequest = new DeleteObjectRequest(bucketName, key);
            amazonS3.deleteObject(deleteRequest);
            log.info("Successfully deleted file from S3: {}", key);

        } catch (AmazonServiceException e) {
            log.error("AWS S3 service error while deleting file: {}", key, e);
            throw new RuntimeException("Failed to delete file from S3: " + e.getMessage(), e);
        } catch (SdkClientException e) {
            log.error("AWS SDK client error while deleting file: {}", key, e);
            throw new RuntimeException("Failed to delete file from S3 due to SDK client error: " + e.getMessage(), e);
        }
    }

    public boolean doesFileExist(String key) {
        try {
            return amazonS3.doesObjectExist(bucketName, key);
        } catch (AmazonServiceException e) {
            log.error("AWS S3 service error while checking file existence: {}", key, e);
            throw new RuntimeException("Failed to check file existence in S3: " + e.getMessage(), e);
        } catch (SdkClientException e) {
            log.error("AWS SDK client error while checking file existence: {}", key, e);
            throw new RuntimeException(
                    "Failed to check file existence in S3 due to SDK client error: " + e.getMessage(), e);
        }
    }

    public String generatePreSignedUrl(String key, long expirationInMinutes) {
        try {
            java.util.Date expiration = new java.util.Date();
            expiration.setTime(expiration.getTime() + (expirationInMinutes * 60 * 1000));

            String url = amazonS3.generatePresignedUrl(bucketName, key, expiration).toString();
            log.info("Generated pre-signed URL for key: {}", key);
            return url;

        } catch (AmazonServiceException e) {
            log.error("AWS S3 service error while generating pre-signed URL: {}", key, e);
            throw new RuntimeException("Failed to generate pre-signed URL: " + e.getMessage(), e);
        } catch (SdkClientException e) {
            log.error("AWS SDK client error while generating pre-signed URL: {}", key, e);
            throw new RuntimeException("Failed to generate pre-signed URL due to SDK client error: " + e.getMessage(),
                    e);
        }
    }
}