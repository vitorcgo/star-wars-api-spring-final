package senac.tsi.starwars.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import senac.tsi.starwars.exception.ResourceNotFoundException;
import senac.tsi.starwars.model.Planet;
import senac.tsi.starwars.model.enums.PlanetClimate;
import senac.tsi.starwars.repository.PlanetRepository;

@Service
public class PlanetService {

    private final PlanetRepository repository;

    @Autowired
    public PlanetService(PlanetRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<Planet> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Planet findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Planeta não encontrado com id: " + id));
    }

    @Transactional
    public Planet save(Planet planet) {
        return repository.save(planet);
    }

    @Transactional
    public Planet update(Long id, Planet details) {
        Planet planet = findById(id);
        planet.setName(details.getName());
        planet.setClimate(details.getClimate());
        planet.setTerrain(details.getTerrain());
        planet.setPopulation(details.getPopulation());
        planet.setDiameter(details.getDiameter());
        return repository.save(planet);
    }

    @Transactional
    public void delete(Long id) {
        findById(id); // lança 404 se não existir
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<Planet> findByClimate(PlanetClimate climate, Pageable pageable) {
        return repository.findByClimate(climate, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Planet> findByName(String name, Pageable pageable) {
        return repository.findByNameContainingIgnoreCase(name, pageable);
    }
}
