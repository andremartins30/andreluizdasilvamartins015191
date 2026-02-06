package mt.gov.seplag.backend.service;

import mt.gov.seplag.backend.domain.album.Album;
import mt.gov.seplag.backend.domain.album.AlbumRepository;
import mt.gov.seplag.backend.domain.artist.Artist;
import mt.gov.seplag.backend.domain.artist.ArtistRepository;
import mt.gov.seplag.backend.domain.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class DefaultDataService {

    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;

    public DefaultDataService(ArtistRepository artistRepository, AlbumRepository albumRepository) {
        this.artistRepository = artistRepository;
        this.albumRepository = albumRepository;
    }

    @Transactional
    public void createDefaultDataForUser(User user) {
        // Serj Tankian
        Artist serjTankian = createArtist("Serj Tankian", user);
        createAlbums(serjTankian, user, Arrays.asList(
                "Harakiri",
                "Black Blooms",
                "The Rough Dog"));

        // Mike Shinoda
        Artist mikeShinoda = createArtist("Mike Shinoda", user);
        createAlbums(mikeShinoda, user, Arrays.asList(
                "The Rising Tied",
                "Post Traumatic",
                "Post Traumatic EP",
                "Where'd You Go"));

        // Michel Teló
        Artist michelTelo = createArtist("Michel Teló", user);
        createAlbums(michelTelo, user, Arrays.asList(
                "Bem Sertanejo",
                "Bem Sertanejo - O Show (Ao Vivo)",
                "Bem Sertanejo - (1ª Temporada) - EP"));

        // Guns N' Roses
        Artist gunsNRoses = createArtist("Guns N' Roses", user);
        createAlbums(gunsNRoses, user, Arrays.asList(
                "Use Your Illusion I",
                "Use Your Illusion II",
                "Greatest Hits"));
    }

    private Artist createArtist(String name, User user) {
        Artist artist = new Artist(name, user);
        return artistRepository.save(artist);
    }

    private void createAlbums(Artist artist, User user, List<String> albumTitles) {
        for (String title : albumTitles) {
            Album album = new Album(title, artist, user);
            albumRepository.save(album);
        }
    }
}
