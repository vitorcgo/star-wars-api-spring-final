package senac.tsi.starwars.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import senac.tsi.starwars.model.ApiKey;
import senac.tsi.starwars.service.ApiKeyService;

import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(name = "API Keys", description = "Geração e gerenciamento de chaves de API para autenticação. " +
        "Gere uma chave com POST e use no header X-API-Key para acessar endpoints protegidos (POST/PUT/DELETE).")
@RestController
@RequestMapping("/api/auth/keys")
public class ApiKeyController {

    private final ApiKeyService service;
    private final PagedResourcesAssembler<ApiKey> pagedAssembler;

    @Autowired
    public ApiKeyController(ApiKeyService service,
                             PagedResourcesAssembler<ApiKey> pagedAssembler) {
        this.service = service;
        this.pagedAssembler = pagedAssembler;
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
                linkTo(methodOn(ApiKeyController.class).listKeys(Pageable.unpaged())).withRel("all-keys"),
                linkTo(methodOn(ApiKeyController.class).revokeKey(key.getId())).withRel("revoke")
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @Operation(summary = "Lista todas as API Keys (paginado)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de API Keys retornada")
    })
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<ApiKey>>> listKeys(@ParameterObject Pageable pageable) {
        Page<ApiKey> page = service.findAll(pageable);
        PagedModel<EntityModel<ApiKey>> model = pagedAssembler.toModel(page, k ->
                EntityModel.of(k,
                        linkTo(methodOn(ApiKeyController.class).revokeKey(k.getId())).withRel("revoke")
                )
        );
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Revoga uma API Key",
            description = "Desativa uma API Key existente. Requer autenticação com header X-API-Key válido.")
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
