package mt.gov.seplag.backend.domain.album;

import mt.gov.seplag.backend.domain.artist.Artist;
import mt.gov.seplag.backend.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    Page<Album> findByArtist_NameContainingIgnoreCase(String name, Pageable pageable);

    long countByArtist(Artist artist);

    Page<Album> findByArtist(Artist artist, Pageable pageable);

    @Transactional
    void deleteByArtist(Artist artist);

    // Métodos com filtro por usuário
    Page<Album> findByUser(User user, Pageable pageable);
    Page<Album> findByUserAndArtist_NameContainingIgnoreCase(User user, String artistName, Pageable pageable);
    Optional<Album> findByIdAndUser(Long id, User user);
    
    @Transactional
    void deleteAllByUser(User user);
    
    long countByUserAndArtist(User user, Artist artist);
    
    long countByUser(User user);

}