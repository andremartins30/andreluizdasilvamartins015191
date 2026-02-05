package mt.gov.seplag.backend.service.storage;

import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class MinioService {

    private final MinioClient minioClient;
    private final MinioClient publicMinioClient;

    @Value("${minio.bucket}")
    private String bucket;

    public MinioService(MinioClient minioClient, 
                       @Qualifier("publicMinioClient") MinioClient publicMinioClient) {
        this.minioClient = minioClient;
        this.publicMinioClient = publicMinioClient;
    }


    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "file";
        }
        return filename
                .toLowerCase()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-z0-9._-]", "");
    }


    public String upload(MultipartFile file, Long albumId) {
        try {
            String safeName = sanitizeFilename(file.getOriginalFilename());
            String objectName = String.format("albums/%d/%s-%s", 
                    albumId, 
                    UUID.randomUUID(), 
                    safeName);

            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build()
            );

            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucket).build()
                );
            }

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            return objectName;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao enviar arquivo para o MinIO", e);
        }
    }


    public void removeObject(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Erro ao remover arquivo do MinIO", e);
        }
    }



    public String generatePresignedUrl(String objectName) {
        // Retorna URL do proxy do backend com query parameter
        return "/api/v1/media?path=" + objectName;
    }
}