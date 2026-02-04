package mt.gov.seplag.backend.domain.album;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumCoverRepository extends JpaRepository<AlbumCover, Long> {
    List<AlbumCover> findByAlbumId(Long albumId);
    void deleteByAlbumId(Long albumId);
}
