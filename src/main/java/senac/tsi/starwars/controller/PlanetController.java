package senac.tsi.starwars.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import senac.tsi.starwars.model.Planet;
import senac.tsi.starwars.model.enums.PlanetClimate;
import senac.tsi.starwars.service.PlanetService;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(name = "Planets", description = "Gerenciamento de planetas Star Wars")
@RestController
@RequestMapping("/api/planets")
public class PlanetController {

    private final PlanetService service;
    private final PagedResourcesAssembler<Planet> pagedAssembler;

    @Autowired
    public PlanetController(PlanetService service,
                             PagedResourcesAssembler<Planet> pagedAssembler) {
        this.service = service;
        this.pagedAssembler = pagedAssembler;
    }

    @Operation(summary = "Lista todos os planetas", description = "Retorna lista paginada de planetas com links HATEOAS")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Planet>>> getAll(@ParameterObject Pageable pageable) {
        Page<Planet> page = service.findAll(pageable);
        PagedModel<EntityModel<Planet>> model = pagedAssembler.toModel(page, planet ->
                EntityModel.of(planet,
                        linkTo(methodOn(PlanetController.class).getById(planet.getId())).withSelfRel(),
                        linkTo(methodOn(PlanetController.class).update(planet.getId(), null)).withRel("update"),
                        linkTo(methodOn(PlanetController.class).delete(planet.getId())).withRel("delete")
                )
        );
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Busca planeta por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Planeta encontrado",
                    content = @Content(schema = @Schema(implementation = Planet.class))),
            @ApiResponse(responseCode = "404", description = "Planeta não encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Planet>> getById(@PathVariable Long id) {
        Planet planet = service.findById(id);
        EntityModel<Planet> model = EntityModel.of(planet,
                linkTo(methodOn(PlanetController.class).getById(id)).withSelfRel(),
                linkTo(methodOn(PlanetController.class).update(id, null)).withRel("update"),
                linkTo(methodOn(PlanetController.class).delete(id)).withRel("delete"),
                linkTo(methodOn(PlanetController.class).getAll(Pageable.unpaged())).withRel("all-planets")
        );
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Cria um novo planeta")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Planeta criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    @PostMapping
    public ResponseEntity<EntityModel<Planet>> create(@Valid @RequestBody Planet planet) {
        Planet saved = service.save(planet);
        EntityModel<Planet> model = EntityModel.of(saved,
                linkTo(methodOn(PlanetController.class).getById(saved.getId())).withSelfRel(),
                linkTo(methodOn(PlanetController.class).update(saved.getId(), null)).withRel("update"),
                linkTo(methodOn(PlanetController.class).delete(saved.getId())).withRel("delete")
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @Operation(summary = "Atualiza um planeta existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Planeta atualizado"),
            @ApiResponse(responseCode = "404", description = "Planeta não encontrado", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Planet>> update(@PathVariable Long id,
                                                       @Valid @RequestBody Planet planet) {
        Planet updated = service.update(id, planet);
        EntityModel<Planet> model = EntityModel.of(updated,
                linkTo(methodOn(PlanetController.class).getById(id)).withSelfRel(),
                linkTo(methodOn(PlanetController.class).update(id, null)).withRel("update"),
                linkTo(methodOn(PlanetController.class).delete(id)).withRel("delete")
        );
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Remove um planeta")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Planeta removido"),
            @ApiResponse(responseCode = "404", description = "Planeta não encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Busca planetas por clima",
            description = "Filtra planetas pelo clima (enum). Valores possíveis: ARID, TEMPERATE, TROPICAL, FROZEN, MURKY, HOT, WINDY, POLLUTED, SUBARCTIC, SUPERHEATED, ARTIFICIAL, UNKNOWN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultado da busca")
    })
    @GetMapping("/search/by-climate")
    public ResponseEntity<PagedModel<EntityModel<Planet>>> findByClimate(
            @Parameter(description = "Clima do planeta") @RequestParam PlanetClimate climate,
            @ParameterObject Pageable pageable) {
        Page<Planet> page = service.findByClimate(climate, pageable);
        return ResponseEntity.ok(pagedAssembler.toModel(page, p ->
                EntityModel.of(p,
                        linkTo(methodOn(PlanetController.class).getById(p.getId())).withSelfRel())
        ));
    }

    @Operation(summary = "Busca planetas por nome (busca parcial)")
    @GetMapping("/search/by-name")
    public ResponseEntity<PagedModel<EntityModel<Planet>>> findByName(
            @Parameter(description = "Trecho do nome") @RequestParam String name,
            @ParameterObject Pageable pageable) {
        Page<Planet> page = service.findByName(name, pageable);
        return ResponseEntity.ok(pagedAssembler.toModel(page, p ->
                EntityModel.of(p,
                        linkTo(methodOn(PlanetController.class).getById(p.getId())).withSelfRel())
        ));
    }
}
