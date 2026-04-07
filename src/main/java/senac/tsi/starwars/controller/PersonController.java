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
import senac.tsi.starwars.model.Person;
import senac.tsi.starwars.service.PersonService;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(name = "People", description = "Gerenciamento de personagens Star Wars")
@RestController
@RequestMapping("/api/people")
public class PersonController {

    private final PersonService service;
    private final PagedResourcesAssembler<Person> pagedAssembler;

    @Autowired
    public PersonController(PersonService service,
                             PagedResourcesAssembler<Person> pagedAssembler) {
        this.service = service;
        this.pagedAssembler = pagedAssembler;
    }

    @Operation(summary = "Lista todos os personagens (paginado)")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Lista retornada")})
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Person>>> getAll(@ParameterObject Pageable pageable) {
        Page<Person> page = service.findAll(pageable);
        PagedModel<EntityModel<Person>> model = pagedAssembler.toModel(page, p ->
                EntityModel.of(p,
                        linkTo(methodOn(PersonController.class).getById(p.getId())).withSelfRel(),
                        linkTo(methodOn(PersonController.class).update(p.getId(), null)).withRel("update"),
                        linkTo(methodOn(PersonController.class).delete(p.getId())).withRel("delete")
                )
        );
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Busca personagem por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Personagem encontrado",
                    content = @Content(schema = @Schema(implementation = Person.class))),
            @ApiResponse(responseCode = "404", description = "Personagem não encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Person>> getById(@PathVariable Long id) {
        Person person = service.findById(id);
        EntityModel<Person> model = EntityModel.of(person,
                linkTo(methodOn(PersonController.class).getById(id)).withSelfRel(),
                linkTo(methodOn(PersonController.class).update(id, null)).withRel("update"),
                linkTo(methodOn(PersonController.class).delete(id)).withRel("delete"),
                linkTo(methodOn(PersonController.class).getAll(Pageable.unpaged())).withRel("all-people")
        );
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Cria um novo personagem")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Personagem criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    @PostMapping
    public ResponseEntity<EntityModel<Person>> create(@Valid @RequestBody Person person) {
        Person saved = service.save(person);
        EntityModel<Person> model = EntityModel.of(saved,
                linkTo(methodOn(PersonController.class).getById(saved.getId())).withSelfRel(),
                linkTo(methodOn(PersonController.class).update(saved.getId(), null)).withRel("update"),
                linkTo(methodOn(PersonController.class).delete(saved.getId())).withRel("delete")
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @Operation(summary = "Atualiza um personagem")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Personagem atualizado"),
            @ApiResponse(responseCode = "404", description = "Personagem não encontrado", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Person>> update(@PathVariable Long id,
                                                       @Valid @RequestBody Person person) {
        Person updated = service.update(id, person);
        EntityModel<Person> model = EntityModel.of(updated,
                linkTo(methodOn(PersonController.class).getById(id)).withSelfRel(),
                linkTo(methodOn(PersonController.class).update(id, null)).withRel("update"),
                linkTo(methodOn(PersonController.class).delete(id)).withRel("delete")
        );
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Remove um personagem")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Personagem removido"),
            @ApiResponse(responseCode = "404", description = "Personagem não encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Busca personagens por gênero",
            description = "Ex: male, female, n/a")
    @GetMapping("/search/by-gender")
    public ResponseEntity<PagedModel<EntityModel<Person>>> findByGender(
            @Parameter(description = "Gênero do personagem") @RequestParam String gender,
            @ParameterObject Pageable pageable) {
        Page<Person> page = service.findByGender(gender, pageable);
        return ResponseEntity.ok(pagedAssembler.toModel(page, p ->
                EntityModel.of(p,
                        linkTo(methodOn(PersonController.class).getById(p.getId())).withSelfRel())
        ));
    }

    @Operation(summary = "Busca personagens por nome (parcial)")
    @GetMapping("/search/by-name")
    public ResponseEntity<PagedModel<EntityModel<Person>>> findByName(
            @Parameter(description = "Trecho do nome") @RequestParam String name,
            @ParameterObject Pageable pageable) {
        Page<Person> page = service.findByName(name, pageable);
        return ResponseEntity.ok(pagedAssembler.toModel(page, p ->
                EntityModel.of(p,
                        linkTo(methodOn(PersonController.class).getById(p.getId())).withSelfRel())
        ));
    }
}
