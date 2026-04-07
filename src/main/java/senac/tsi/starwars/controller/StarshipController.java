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
import senac.tsi.starwars.model.Starship;
import senac.tsi.starwars.service.StarshipService;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(name = "Starships", description = "Gerenciamento de naves estelares Star Wars")
@RestController
@RequestMapping("/api/starships")
public class StarshipController {

    private final StarshipService service;
    private final PagedResourcesAssembler<Starship> pagedAssembler;

    @Autowired
    public StarshipController(StarshipService service,
                               PagedResourcesAssembler<Starship> pagedAssembler) {
        this.service = service;
        this.pagedAssembler = pagedAssembler;
    }

    @Operation(summary = "Lista todas as naves (paginado)")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Lista retornada")})
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Starship>>> getAll(@ParameterObject Pageable pageable) {
        Page<Starship> page = service.findAll(pageable);
        PagedModel<EntityModel<Starship>> model = pagedAssembler.toModel(page, s ->
                EntityModel.of(s,
                        linkTo(methodOn(StarshipController.class).getById(s.getId())).withSelfRel(),
                        linkTo(methodOn(StarshipController.class).update(s.getId(), null)).withRel("update"),
                        linkTo(methodOn(StarshipController.class).delete(s.getId())).withRel("delete")
                )
        );
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Busca nave por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nave encontrada",
                    content = @Content(schema = @Schema(implementation = Starship.class))),
            @ApiResponse(responseCode = "404", description = "Nave não encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Starship>> getById(@PathVariable Long id) {
        Starship starship = service.findById(id);
        EntityModel<Starship> model = EntityModel.of(starship,
                linkTo(methodOn(StarshipController.class).getById(id)).withSelfRel(),
                linkTo(methodOn(StarshipController.class).update(id, null)).withRel("update"),
                linkTo(methodOn(StarshipController.class).delete(id)).withRel("delete"),
                linkTo(methodOn(StarshipController.class).getAll(Pageable.unpaged())).withRel("all-starships")
        );
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Cria uma nova nave estelar")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Nave criada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    @PostMapping
    public ResponseEntity<EntityModel<Starship>> create(@Valid @RequestBody Starship starship) {
        Starship saved = service.save(starship);
        EntityModel<Starship> model = EntityModel.of(saved,
                linkTo(methodOn(StarshipController.class).getById(saved.getId())).withSelfRel(),
                linkTo(methodOn(StarshipController.class).update(saved.getId(), null)).withRel("update"),
                linkTo(methodOn(StarshipController.class).delete(saved.getId())).withRel("delete")
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @Operation(summary = "Atualiza uma nave estelar")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nave atualizada"),
            @ApiResponse(responseCode = "404", description = "Nave não encontrada", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Starship>> update(@PathVariable Long id,
                                                         @Valid @RequestBody Starship starship) {
        Starship updated = service.update(id, starship);
        EntityModel<Starship> model = EntityModel.of(updated,
                linkTo(methodOn(StarshipController.class).getById(id)).withSelfRel(),
                linkTo(methodOn(StarshipController.class).update(id, null)).withRel("update"),
                linkTo(methodOn(StarshipController.class).delete(id)).withRel("delete")
        );
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Remove uma nave estelar")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Nave removida"),
            @ApiResponse(responseCode = "404", description = "Nave não encontrada", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Busca naves por classe (ex: Starfighter, Freighter)")
    @GetMapping("/search/by-class")
    public ResponseEntity<PagedModel<EntityModel<Starship>>> findByClass(
            @Parameter(description = "Classe da nave") @RequestParam String starshipClass,
            @ParameterObject Pageable pageable) {
        Page<Starship> page = service.findByStarshipClass(starshipClass, pageable);
        return ResponseEntity.ok(pagedAssembler.toModel(page, s ->
                EntityModel.of(s,
                        linkTo(methodOn(StarshipController.class).getById(s.getId())).withSelfRel())
        ));
    }

    @Operation(summary = "Busca naves por nome (parcial)")
    @GetMapping("/search/by-name")
    public ResponseEntity<PagedModel<EntityModel<Starship>>> findByName(
            @Parameter(description = "Trecho do nome") @RequestParam String name,
            @ParameterObject Pageable pageable) {
        Page<Starship> page = service.findByName(name, pageable);
        return ResponseEntity.ok(pagedAssembler.toModel(page, s ->
                EntityModel.of(s,
                        linkTo(methodOn(StarshipController.class).getById(s.getId())).withSelfRel())
        ));
    }
}
