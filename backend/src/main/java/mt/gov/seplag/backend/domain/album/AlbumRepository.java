package mt.gov.seplag.backend.domain.album;

import mt.gov.seplag.backend.domain.artist.Artist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    Page<Album> findByArtist_NameContainingIgnoreCase(String name, Pageable pageable);

    long countByArtist(Artist artist);

    Page<Album> findByArtist(Artist artist, Pageable pageable);

    @Transactional
    void deleteByArtist(Artist artist);

}