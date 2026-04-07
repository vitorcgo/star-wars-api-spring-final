package senac.tsi.starwars.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import senac.tsi.starwars.model.Species;
import senac.tsi.starwars.model.enums.SpeciesClassification;

@Repository
public interface SpeciesRepository extends JpaRepository<Species, Long> {

    // Busca customizada 1: por classificação biológica (Enum)
    Page<Species> findByClassification(SpeciesClassification classification, Pageable pageable);

    // Busca customizada 2: por nome parcial
    Page<Species> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
