package mt.gov.seplag.backend.domain.regional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RegionalRepository extends JpaRepository<Regional, Long> {
    
    List<Regional> findByAtivoTrue();
    
    @Query("SELECT r FROM Regional r WHERE r.idExterno = :idExterno AND r.ativo = true")
    Optional<Regional> findByIdExternoAndAtivoTrue(Integer idExterno);
    
    @Query("SELECT r FROM Regional r WHERE r.idExterno = :idExterno")
    List<Regional> findByIdExterno(Integer idExterno);
}
