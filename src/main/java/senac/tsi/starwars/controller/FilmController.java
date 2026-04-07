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
import senac.tsi.starwars.model.Film;
import senac.tsi.starwars.service.FilmService;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(name = "Films", description = "Gerenciamento de filmes Star Wars")
@RestController
@RequestMapping("/api/films")
public class FilmController {

    private final FilmService service;
    private final PagedResourcesAssembler<Film> pagedAssembler;

    @Autowired
    public FilmController(FilmService service,
                           PagedResourcesAssembler<Film> pagedAssembler) {
        this.service = service;
        this.pagedAssembler = pagedAssembler;
    }

    @Operation(summary = "Lista todos os filmes (paginado)")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Lista retornada")})
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Film>>> getAll(@ParameterObject Pageable pageable) {
        Page<Film> page = service.findAll(pageable);
        PagedModel<EntityModel<Film>> model = pagedAssembler.toModel(page, f ->
                EntityModel.of(f,
                        linkTo(methodOn(FilmController.class).getById(f.getId())).withSelfRel(),
                        linkTo(methodOn(FilmController.class).update(f.getId(), null)).withRel("update"),
                        linkTo(methodOn(FilmController.class).delete(f.getId())).withRel("delete")
                )
        );
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Busca filme por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filme encontrado",
                    content = @Content(schema = @Schema(implementation = Film.class))),
            @ApiResponse(responseCode = "404", description = "Filme não encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Film>> getById(@PathVariable Long id) {
        Film film = service.findById(id);
        EntityModel<Film> model = EntityModel.of(film,
                linkTo(methodOn(FilmController.class).getById(id)).withSelfRel(),
                linkTo(methodOn(FilmController.class).update(id, null)).withRel("update"),
                linkTo(methodOn(FilmController.class).delete(id)).withRel("delete"),
                linkTo(methodOn(FilmController.class).getAll(Pageable.unpaged())).withRel("all-films")
        );
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Cria um novo filme")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Filme criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    @PostMapping
    public ResponseEntity<EntityModel<Film>> create(@Valid @RequestBody Film film) {
        Film saved = service.save(film);
        EntityModel<Film> model = EntityModel.of(saved,
                linkTo(methodOn(FilmController.class).getById(saved.getId())).withSelfRel(),
                linkTo(methodOn(FilmController.class).update(saved.getId(), null)).withRel("update"),
                linkTo(methodOn(FilmController.class).delete(saved.getId())).withRel("delete")
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @Operation(summary = "Atualiza um filme")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filme atualizado"),
            @ApiResponse(responseCode = "404", description = "Filme não encontrado", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Film>> update(@PathVariable Long id,
                                                     @Valid @RequestBody Film film) {
        Film updated = service.update(id, film);
        EntityModel<Film> model = EntityModel.of(updated,
                linkTo(methodOn(FilmController.class).getById(id)).withSelfRel(),
                linkTo(methodOn(FilmController.class).update(id, null)).withRel("update"),
                linkTo(methodOn(FilmController.class).delete(id)).withRel("delete")
        );
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Remove um filme")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Filme removido"),
            @ApiResponse(responseCode = "404", description = "Filme não encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Busca filmes por diretor",
            description = "Ex: ?director=George para encontrar filmes de George Lucas")
    @GetMapping("/search/by-director")
    public ResponseEntity<PagedModel<EntityModel<Film>>> findByDirector(
            @Parameter(description = "Nome do diretor (parcial)") @RequestParam String director,
            @ParameterObject Pageable pageable) {
        Page<Film> page = service.findByDirector(director, pageable);
        return ResponseEntity.ok(pagedAssembler.toModel(page, f ->
                EntityModel.of(f,
                        linkTo(methodOn(FilmController.class).getById(f.getId())).withSelfRel())
        ));
    }

    @Operation(summary = "Busca filmes por título (parcial)")
    @GetMapping("/search/by-title")
    public ResponseEntity<PagedModel<EntityModel<Film>>> findByTitle(
            @Parameter(description = "Trecho do título") @RequestParam String title,
            @ParameterObject Pageable pageable) {
        Page<Film> page = service.findByTitle(title, pageable);
        return ResponseEntity.ok(pagedAssembler.toModel(page, f ->
                EntityModel.of(f,
                        linkTo(methodOn(FilmController.class).getById(f.getId())).withSelfRel())
        ));
    }
}
