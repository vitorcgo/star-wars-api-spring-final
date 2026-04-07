package senac.tsi.starwars.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import senac.tsi.starwars.exception.ResourceNotFoundException;
import senac.tsi.starwars.model.Species;
import senac.tsi.starwars.model.enums.SpeciesClassification;
import senac.tsi.starwars.repository.SpeciesRepository;

@Service
public class SpeciesService {

    private final SpeciesRepository repository;

    @Autowired
    public SpeciesService(SpeciesRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<Species> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Species findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Espécie não encontrada com id: " + id));
    }

    @Transactional
    public Species save(Species species) {
        return repository.save(species);
    }

    @Transactional
    public Species update(Long id, Species details) {
        Species species = findById(id);
        species.setName(details.getName());
        species.setClassification(details.getClassification());
        species.setLanguage(details.getLanguage());
        species.setAverageHeight(details.getAverageHeight());
        species.setAverageLifespan(details.getAverageLifespan());
        return repository.save(species);
    }

    @Transactional
    public void delete(Long id) {
        findById(id);
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<Species> findByClassification(SpeciesClassification classification, Pageable pageable) {
        return repository.findByClassification(classification, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Species> findByName(String name, Pageable pageable) {
        return repository.findByNameContainingIgnoreCase(name, pageable);
    }
}
