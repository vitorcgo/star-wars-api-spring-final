package senac.tsi.starwars.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import senac.tsi.starwars.model.Person;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    // Busca customizada 1: por gênero
    Page<Person> findByGenderIgnoreCase(String gender, Pageable pageable);

    // Busca customizada 2: por nome parcial
    Page<Person> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Extra: todos os personagens de um planeta
    Page<Person> findByPlanet_Id(Long planetId, Pageable pageable);
}
