package senac.tsi.starwars.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import senac.tsi.starwars.exception.ResourceNotFoundException;
import senac.tsi.starwars.model.ApiKey;
import senac.tsi.starwars.repository.ApiKeyRepository;

import java.util.UUID;

@Service
public class ApiKeyService {

    private final ApiKeyRepository repository;

    @Autowired
    public ApiKeyService(ApiKeyRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ApiKey generateKey(String owner) {
        ApiKey apiKey = new ApiKey();
        apiKey.setOwner(owner);
        apiKey.setKey(UUID.randomUUID().toString().replace("-", ""));
        return repository.save(apiKey);
    }

    @Transactional(readOnly = true)
    public boolean isValidKey(String key) {
        return repository.existsByKeyAndActiveTrue(key);
    }

    @Transactional(readOnly = true)
    public Page<ApiKey> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional
    public void revokeKey(Long id) {
        ApiKey apiKey = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("API Key não encontrada com id: " + id));
        apiKey.setActive(false);
        repository.save(apiKey);
    }
}
