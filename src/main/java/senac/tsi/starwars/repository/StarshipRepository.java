package senac.tsi.starwars.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import senac.tsi.starwars.model.Starship;

@Repository
public interface StarshipRepository extends JpaRepository<Starship, Long> {

    // Busca customizada 1: por classe da nave (parcial, case-insensitive)
    Page<Starship> findByStarshipClassContainingIgnoreCase(String starshipClass, Pageable pageable);

    // Busca customizada 2: por nome parcial
    Page<Starship> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
