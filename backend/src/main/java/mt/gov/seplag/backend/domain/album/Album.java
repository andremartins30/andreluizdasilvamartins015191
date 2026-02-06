package mt.gov.seplag.backend.domain.album;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import mt.gov.seplag.backend.domain.artist.Artist;
import mt.gov.seplag.backend.domain.user.User;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AlbumCover> covers = new ArrayList<>();

    @Column(name = "cover_object_name")
    private String coverObjectName;

    public Album() {}

    public Album(String title, Artist artist) {
        this.title = title;
        this.artist = artist;
    }

    public Album(String title, Artist artist, User user) {
        this.title = title;
        this.artist = artist;
        this.user = user;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}