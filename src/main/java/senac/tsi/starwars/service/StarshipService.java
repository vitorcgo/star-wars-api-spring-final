package senac.tsi.starwars.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import senac.tsi.starwars.exception.ResourceNotFoundException;
import senac.tsi.starwars.model.Starship;
import senac.tsi.starwars.repository.StarshipRepository;

@Service
public class StarshipService {

    private final StarshipRepository repository;

    @Autowired
    public StarshipService(StarshipRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<Starship> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Starship findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nave não encontrada com id: " + id));
    }

    @Transactional
    public Starship save(Starship starship) {
        return repository.save(starship);
    }

    @Transactional
    public Starship update(Long id, Starship details) {
        Starship starship = findById(id);
        starship.setName(details.getName());
        starship.setModel(details.getModel());
        starship.setManufacturer(details.getManufacturer());
        starship.setCostInCredits(details.getCostInCredits());
        starship.setLength(details.getLength());
        starship.setCrew(details.getCrew());
        starship.setPassengers(details.getPassengers());
        starship.setStarshipClass(details.getStarshipClass());
        return repository.save(starship);
    }

    @Transactional
    public void delete(Long id) {
        findById(id);
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<Starship> findByStarshipClass(String starshipClass, Pageable pageable) {
        return repository.findByStarshipClassContainingIgnoreCase(starshipClass, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Starship> findByName(String name, Pageable pageable) {
        return repository.findByNameContainingIgnoreCase(name, pageable);
    }
}
