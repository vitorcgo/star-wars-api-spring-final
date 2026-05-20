package senac.tsi.starwars.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import senac.tsi.starwars.model.ApiKey;
import senac.tsi.starwars.service.ApiKeyService;

import java.util.List;
import java.util.Map;

@Tag(name = "API Keys", description = "Geração e gerenciamento de chaves de API para autenticação")
@RestController
@RequestMapping("/api/auth/keys")
public class ApiKeyController {

    private final ApiKeyService service;

    @Autowired
    public ApiKeyController(ApiKeyService service) {
        this.service = service;
    }

    @Operation(summary = "Gera uma nova API Key",
            description = "Cria uma nova chave de API para o proprietário informado. Use a chave retornada no header X-API-Key para autenticar requisições POST/PUT/DELETE.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "API Key gerada com sucesso")
    })
    @PostMapping
    public ResponseEntity<ApiKey> generateKey(@RequestBody Map<String, String> body) {
        String owner = body.getOrDefault("owner", "anonymous");
        ApiKey key = service.generateKey(owner);
        return ResponseEntity.status(HttpStatus.CREATED).body(key);
    }

    @Operation(summary = "Lista todas as API Keys")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de API Keys retornada")
    })
    @GetMapping
    public ResponseEntity<List<ApiKey>> listKeys() {
        return ResponseEntity.ok(service.findAll());
    }

    @Operation(summary = "Revoga uma API Key",
            description = "Desativa uma API Key existente. Requisições com essa chave serão rejeitadas.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "API Key revogada"),
            @ApiResponse(responseCode = "404", description = "API Key não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revokeKey(@PathVariable Long id) {
        service.revokeKey(id);
        return ResponseEntity.noContent().build();
    }
}
