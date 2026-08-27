package com.makersacademy.petly.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

import java.io.IOException;

@Service
public class ImageStorageService {

    private final RestClient restClient;
    private final String supabaseUrl;
    private final String bucket;

    public ImageStorageService(
            @Value("${supabase.url:https://localhost.supabase.co}") String supabaseUrl,
            @Value("${supabase.service-key:dummy-local-key}") String serviceKey,
            @Value("${supabase.storage-bucket:petly-uploads}") String bucket
    ) {
        this.supabaseUrl = supabaseUrl;
        this.bucket = bucket;

        this.restClient = RestClient.builder()
                .baseUrl(supabaseUrl)
                .defaultHeader("apikey", serviceKey)
                .defaultHeader("Authorization", "Bearer " + serviceKey)
                .build();
    }

    public String upload(MultipartFile image) throws IOException {

        if (image.isEmpty()) {
            throw new IllegalArgumentException("Please select an image");
        }

        String filename = UUID.randomUUID() + "-" + image.getOriginalFilename();

        restClient.post()
                .uri("/storage/v1/object/{bucket}/{filename}", bucket, filename)
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .body(new ByteArrayResource(image.getBytes()))
                .retrieve()
                .toBodilessEntity();

        return supabaseUrl
                + "/storage/v1/object/public/"
                + bucket
                + "/"
                + filename;
    }
}
