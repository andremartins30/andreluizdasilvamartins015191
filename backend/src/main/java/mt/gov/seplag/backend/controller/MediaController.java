package mt.gov.seplag.backend.controller;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;

@RestController
@RequestMapping("/api/v1/media")
public class MediaController {

    private static final Logger log = LoggerFactory.getLogger(MediaController.class);
    
    private final MinioClient minioClient;
    private final String bucket = "album-covers";

    public MediaController(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @GetMapping("/**")
    public ResponseEntity<InputStreamResource> getMedia(@RequestParam String path) {
        try {
            log.info("Fetching media from MinIO - bucket: {}, path: {}", bucket, path);
            
            InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .build()
            );

            
            MediaType contentType = MediaType.IMAGE_JPEG; // padrão
            String lowerPath = path.toLowerCase();
            if (lowerPath.endsWith(".png")) {
                contentType = MediaType.IMAGE_PNG;
            } else if (lowerPath.endsWith(".gif")) {
                contentType = MediaType.IMAGE_GIF;
            } else if (lowerPath.endsWith(".webp")) {
                contentType = MediaType.valueOf("image/webp");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(contentType);
            headers.setCacheControl("max-age=3600");

            log.info("Successfully fetched media: {}", path);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(stream));

        } catch (Exception e) {
            log.error("Error fetching media from MinIO - bucket: {}, path: {}, error: {}", 
                    bucket, path, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
