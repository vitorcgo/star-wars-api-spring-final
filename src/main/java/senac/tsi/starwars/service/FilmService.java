package senac.tsi.starwars.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import senac.tsi.starwars.exception.ResourceNotFoundException;
import senac.tsi.starwars.model.Film;
import senac.tsi.starwars.repository.FilmRepository;

@Service
public class FilmService {

    private final FilmRepository repository;

    @Autowired
    public FilmService(FilmRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<Film> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Film findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Filme não encontrado com id: " + id));
    }

    @Transactional
    public Film save(Film film) {
        return repository.save(film);
    }

    @Transactional
    public Film update(Long id, Film details) {
        Film film = findById(id);
        film.setTitle(details.getTitle());
        film.setEpisodeId(details.getEpisodeId());
        film.setDirector(details.getDirector());
        film.setProducer(details.getProducer());
        film.setReleaseDate(details.getReleaseDate());
        film.setOpeningCrawl(details.getOpeningCrawl());
        return repository.save(film);
    }

    @Transactional
    public void delete(Long id) {
        findById(id);
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<Film> findByDirector(String director, Pageable pageable) {
        return repository.findByDirectorContainingIgnoreCase(director, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Film> findByTitle(String title, Pageable pageable) {
        return repository.findByTitleContainingIgnoreCase(title, pageable);
    }
}
