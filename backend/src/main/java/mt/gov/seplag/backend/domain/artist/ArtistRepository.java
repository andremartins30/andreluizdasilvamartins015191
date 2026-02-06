package mt.gov.seplag.backend.domain.artist;

import mt.gov.seplag.backend.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArtistRepository extends JpaRepository<Artist, Long> {
    Page<Artist> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    // Métodos com filtro por usuário
    Page<Artist> findByUser(User user, Pageable pageable);
    Page<Artist> findByUserAndNameContainingIgnoreCase(User user, String name, Pageable pageable);
    Optional<Artist> findByIdAndUser(Long id, User user);
}