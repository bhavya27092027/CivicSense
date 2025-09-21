package com.example.civicsense.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class MediaService {

    private final S3Client s3Client;
    private final String bucketName;

    public MediaService(
            @Value("${AWS_ACCESS_KEY_ID}") String accessKey,
            @Value("${AWS_SECRET_ACCESS_KEY}") String secretKey,
            @Value("${AWS_REGION}") String region,
            @Value("${AWS_S3_BUCKET_NAME}") String bucketName
    ) {
        this.s3Client = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.of(region))
                .build();
        this.bucketName = bucketName;
    }

    public List<String> uploadFiles(List<File> files, String userPhone) {
        List<String> urls = new ArrayList<>();

        for (File file : files) {
            try {
                String key = userPhone + "/" + System.currentTimeMillis() + "_" + file.getName();
                PutObjectRequest request = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .acl("public-read")
                        .build();
                s3Client.putObject(request, Paths.get(file.getAbsolutePath()));
                String url = "https://" + bucketName + ".s3." + s3Client.region().id() + ".amazonaws.com/" + key;
                urls.add(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return urls;
    }
}
