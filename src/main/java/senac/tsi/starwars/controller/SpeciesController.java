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
import senac.tsi.starwars.model.Species;
import senac.tsi.starwars.model.enums.SpeciesClassification;
import senac.tsi.starwars.service.SpeciesService;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(name = "Species", description = "Gerenciamento de espécies Star Wars")
@RestController
@RequestMapping("/api/species")
public class SpeciesController {

    private final SpeciesService service;
    private final PagedResourcesAssembler<Species> pagedAssembler;

    @Autowired
    public SpeciesController(SpeciesService service,
                              PagedResourcesAssembler<Species> pagedAssembler) {
        this.service = service;
        this.pagedAssembler = pagedAssembler;
    }

    @Operation(summary = "Lista todas as espécies (paginado)")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Lista retornada")})
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Species>>> getAll(@ParameterObject Pageable pageable) {
        Page<Species> page = service.findAll(pageable);
        PagedModel<EntityModel<Species>> model = pagedAssembler.toModel(page, s ->
                EntityModel.of(s,
                        linkTo(methodOn(SpeciesController.class).getById(s.getId())).withSelfRel(),
                        linkTo(methodOn(SpeciesController.class).update(s.getId(), null)).withRel("update"),
                        linkTo(methodOn(SpeciesController.class).delete(s.getId())).withRel("delete")
                )
        );
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Busca espécie por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Espécie encontrada",
                    content = @Content(schema = @Schema(implementation = Species.class))),
            @ApiResponse(responseCode = "404", description = "Espécie não encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Species>> getById(@PathVariable Long id) {
        Species species = service.findById(id);
        EntityModel<Species> model = EntityModel.of(species,
                linkTo(methodOn(SpeciesController.class).getById(id)).withSelfRel(),
                linkTo(methodOn(SpeciesController.class).update(id, null)).withRel("update"),
                linkTo(methodOn(SpeciesController.class).delete(id)).withRel("delete"),
                linkTo(methodOn(SpeciesController.class).getAll(Pageable.unpaged())).withRel("all-species")
        );
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Cria uma nova espécie")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Espécie criada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    @PostMapping
    public ResponseEntity<EntityModel<Species>> create(@Valid @RequestBody Species species) {
        Species saved = service.save(species);
        EntityModel<Species> model = EntityModel.of(saved,
                linkTo(methodOn(SpeciesController.class).getById(saved.getId())).withSelfRel(),
                linkTo(methodOn(SpeciesController.class).update(saved.getId(), null)).withRel("update"),
                linkTo(methodOn(SpeciesController.class).delete(saved.getId())).withRel("delete")
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @Operation(summary = "Atualiza uma espécie")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Espécie atualizada"),
            @ApiResponse(responseCode = "404", description = "Espécie não encontrada", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Species>> update(@PathVariable Long id,
                                                        @Valid @RequestBody Species species) {
        Species updated = service.update(id, species);
        EntityModel<Species> model = EntityModel.of(updated,
                linkTo(methodOn(SpeciesController.class).getById(id)).withSelfRel(),
                linkTo(methodOn(SpeciesController.class).update(id, null)).withRel("update"),
                linkTo(methodOn(SpeciesController.class).delete(id)).withRel("delete")
        );
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Remove uma espécie")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Espécie removida"),
            @ApiResponse(responseCode = "404", description = "Espécie não encontrada", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Busca espécies por classificação biológica",
            description = "Valores possíveis: MAMMAL, REPTILE, AMPHIBIAN, ARTIFICIAL, INSECTOID, GASTROPOD, SENTIENT, UNKNOWN")
    @GetMapping("/search/by-classification")
    public ResponseEntity<PagedModel<EntityModel<Species>>> findByClassification(
            @Parameter(description = "Classificação biológica") @RequestParam SpeciesClassification classification,
            @ParameterObject Pageable pageable) {
        Page<Species> page = service.findByClassification(classification, pageable);
        return ResponseEntity.ok(pagedAssembler.toModel(page, s ->
                EntityModel.of(s,
                        linkTo(methodOn(SpeciesController.class).getById(s.getId())).withSelfRel())
        ));
    }

    @Operation(summary = "Busca espécies por nome (parcial)")
    @GetMapping("/search/by-name")
    public ResponseEntity<PagedModel<EntityModel<Species>>> findByName(
            @Parameter(description = "Trecho do nome") @RequestParam String name,
            @ParameterObject Pageable pageable) {
        Page<Species> page = service.findByName(name, pageable);
        return ResponseEntity.ok(pagedAssembler.toModel(page, s ->
                EntityModel.of(s,
                        linkTo(methodOn(SpeciesController.class).getById(s.getId())).withSelfRel())
        ));
    }
}
