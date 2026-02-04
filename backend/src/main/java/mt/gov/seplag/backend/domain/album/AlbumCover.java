package mt.gov.seplag.backend.domain.album;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "album_covers")
public class AlbumCover {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @Column(name = "object_name", nullable = false)
    private String objectName;

    @Column(name = "upload_date")
    private LocalDateTime uploadDate;

    public AlbumCover() {
        this.uploadDate = LocalDateTime.now();
    }

    public AlbumCover(Album album, String objectName) {
        this.album = album;
        this.objectName = objectName;
        this.uploadDate = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }
}
