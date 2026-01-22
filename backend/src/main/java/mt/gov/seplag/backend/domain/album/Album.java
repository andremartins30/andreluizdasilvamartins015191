package mt.gov.seplag.backend.domain.album;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import mt.gov.seplag.backend.domain.artist.Artist;

@Entity
@Table(name = "albums")
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToOne
    private Artist artist;

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
}