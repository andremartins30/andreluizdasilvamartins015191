package mt.gov.seplag.backend.domain.album;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    Page<Album> findByArtist_NameContainingIgnoreCase(String name, Pageable pageable);

}