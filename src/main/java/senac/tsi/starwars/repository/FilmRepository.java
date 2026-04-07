package senac.tsi.starwars.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import senac.tsi.starwars.model.Film;

@Repository
public interface FilmRepository extends JpaRepository<Film, Long> {

    // Busca customizada 1: por diretor (parcial, case-insensitive)
    Page<Film> findByDirectorContainingIgnoreCase(String director, Pageable pageable);

    // Busca customizada 2: por título parcial
    Page<Film> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
