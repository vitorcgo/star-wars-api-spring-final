package senac.tsi.starwars.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import senac.tsi.starwars.exception.ResourceNotFoundException;
import senac.tsi.starwars.model.Person;
import senac.tsi.starwars.repository.PersonRepository;

@Service
public class PersonService {

    private final PersonRepository repository;

    @Autowired
    public PersonService(PersonRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<Person> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Person findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Personagem não encontrado com id: " + id));
    }

    @Transactional
    public Person save(Person person) {
        return repository.save(person);
    }

    @Transactional
    public Person update(Long id, Person details) {
        Person person = findById(id);
        person.setName(details.getName());
        person.setBirthYear(details.getBirthYear());
        person.setGender(details.getGender());
        person.setHeight(details.getHeight());
        person.setMass(details.getMass());
        return repository.save(person);
    }

    @Transactional
    public void delete(Long id) {
        findById(id);
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<Person> findByGender(String gender, Pageable pageable) {
        return repository.findByGenderIgnoreCase(gender, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Person> findByName(String name, Pageable pageable) {
        return repository.findByNameContainingIgnoreCase(name, pageable);
    }
}
