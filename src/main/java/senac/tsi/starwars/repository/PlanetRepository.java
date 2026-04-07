package senac.tsi.starwars.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import senac.tsi.starwars.model.Planet;
import senac.tsi.starwars.model.enums.PlanetClimate;

@Repository
public interface PlanetRepository extends JpaRepository<Planet, Long> {

    // Busca customizada 1: por clima (usando o Enum)
    Page<Planet> findByClimate(PlanetClimate climate, Pageable pageable);

    // Busca customizada 2: por nome (busca parcial, case-insensitive)
    Page<Planet> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
