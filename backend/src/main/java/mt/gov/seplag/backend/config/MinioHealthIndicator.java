package mt.gov.seplag.backend.config;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class MinioHealthIndicator implements HealthIndicator {

    private final MinioClient minioClient;
    private final String bucket;

    public MinioHealthIndicator(
            @Value("${minio.url}") String url,
            @Value("${minio.access-key}") String accessKey,
            @Value("${minio.secret-key}") String secretKey,
            @Value("${minio.bucket}") String bucket
    ) {
        this.minioClient = MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
        this.bucket = bucket;
    }

    @Override
    public Health health() {
        try {
            // Verifica se o bucket existe
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucket)
                            .build()
            );

            if (bucketExists) {
                return Health.up()
                        .withDetail("bucket", bucket)
                        .withDetail("status", "Bucket existe e está acessível")
                        .build();
            } else {
                return Health.down()
                        .withDetail("bucket", bucket)
                        .withDetail("status", "Bucket não existe")
                        .build();
            }

        } catch (Exception e) {
            return Health.down()
                    .withDetail("bucket", bucket)
                    .withDetail("error", e.getClass().getName())
                    .withDetail("message", e.getMessage())
                    .build();
        }
    }
}
