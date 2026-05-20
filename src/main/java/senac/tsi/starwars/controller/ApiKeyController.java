package senac.tsi.starwars.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import senac.tsi.starwars.model.ApiKey;
import senac.tsi.starwars.service.ApiKeyService;

import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(name = "Autenticacao", description = "Geracao e revogacao de chaves de acesso (API Keys). "
        + "Gere uma chave com POST e use no header X-API-Key para operacoes de escrita.")
@RestController
@RequestMapping("/api/auth/keys")
public class ApiKeyController {

    private final ApiKeyService service;

    @Autowired
    public ApiKeyController(ApiKeyService service) {
        this.service = service;
    }

    @Operation(summary = "Gera uma nova API Key",
            description = "Cria uma nova chave de API. Envie {\"owner\": \"seu-nome\"} no corpo. " +
                    "Copie o campo 'key' retornado e use no header X-API-Key para autenticar requisições.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "API Key gerada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Corpo da requisição inválido", content = @Content)
    })
    @PostMapping
    public ResponseEntity<EntityModel<ApiKey>> generateKey(@RequestBody(required = false) Map<String, String> body) {
        String owner = (body != null) ? body.getOrDefault("owner", "anonymous") : "anonymous";
        if (owner.isBlank()) {
            owner = "anonymous";
        }
        ApiKey key = service.generateKey(owner);
        EntityModel<ApiKey> model = EntityModel.of(key,
                linkTo(methodOn(ApiKeyController.class).revokeKey(key.getId())).withRel("revoke")
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @Operation(summary = "Revoga uma API Key",
            description = "Desativa uma API Key existente. Requer autenticação com header X-API-Key válido.",
            security = @SecurityRequirement(name = "apiKey"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "API Key revogada"),
            @ApiResponse(responseCode = "401", description = "X-API-Key ausente", content = @Content),
            @ApiResponse(responseCode = "403", description = "X-API-Key inválida", content = @Content),
            @ApiResponse(responseCode = "404", description = "API Key não encontrada", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revokeKey(@PathVariable Long id) {
        service.revokeKey(id);
        return ResponseEntity.noContent().build();
    }
}
