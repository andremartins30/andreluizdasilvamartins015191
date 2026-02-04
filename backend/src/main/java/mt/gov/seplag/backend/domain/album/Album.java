package mt.gov.seplag.backend.domain.album;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import mt.gov.seplag.backend.domain.artist.Artist;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "albums")
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToOne
    private Artist artist;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AlbumCover> covers = new ArrayList<>();

    @Column(name = "cover_object_name")
    private String coverObjectName;

    public Album() {}

    public Album(String title, Artist artist) {
        this.title = title;
        this.artist = artist;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Artist getArtist() {
        return artist;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public List<AlbumCover> getCovers() {
        return covers;
    }

    public void addCover(AlbumCover cover) {
        covers.add(cover);
        cover.setAlbum(this);
    }

    public void removeCover(AlbumCover cover) {
        covers.remove(cover);
        cover.setAlbum(null);
    }

    public String getCoverObjectName() {
        return coverObjectName;
    }

    public void setCoverObjectName(String coverObjectName) {
        this.coverObjectName = coverObjectName;
    }
    
}