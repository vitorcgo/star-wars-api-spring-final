package senac.tsi.starwars.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import senac.tsi.starwars.model.Film;
import senac.tsi.starwars.service.FilmService;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Films V2", description = "Versão alternativa de filmes via header X-API-Version=2. "
        + "Formato simplificado sem HATEOAS, com contagem de personagens e naves.")
@RestController
@RequestMapping("/api/films")
public class FilmV2Controller {

    private final FilmService service;

    @Autowired
    public FilmV2Controller(FilmService service) {
        this.service = service;
    }

    @Operation(summary = "Lista filmes (V2 - formato resumido)",
            description = "Retorna filmes em formato simplificado quando header X-API-Version = 2. " +
                    "Inclui apenas título, episódio, diretor e ano de lançamento.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Lista retornada no formato V2")})
    @GetMapping(headers = "X-API-Version=2")
    public ResponseEntity<Map<String, Object>> getAllV2(@ParameterObject Pageable pageable) {
        Page<Film> page = service.findAll(pageable);

        var summaries = page.getContent().stream().map(film -> {
            Map<String, Object> summary = new HashMap<>();
            summary.put("id", film.getId());
            summary.put("title", film.getTitle());
            summary.put("episodeId", film.getEpisodeId());
            summary.put("director", film.getDirector());
            summary.put("releaseDate", film.getReleaseDate());
            summary.put("characterCount", film.getCharacters() != null ? film.getCharacters().size() : 0);
            summary.put("starshipCount", film.getStarships() != null ? film.getStarships().size() : 0);
            return summary;
        }).toList();

        Map<String, Object> result = new HashMap<>();
        result.put("version", 2);
        result.put("totalElements", page.getTotalElements());
        result.put("totalPages", page.getTotalPages());
        result.put("currentPage", page.getNumber());
        result.put("films", summaries);

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Busca filme por ID (V2 - formato resumido)",
            description = "Retorna um filme em formato reduzido quando o header X-API-Version=2 e enviado, incluindo contadores de personagens e naves.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filme encontrado (V2)"),
            @ApiResponse(responseCode = "404", description = "Filme não encontrado")
    })
    @GetMapping(value = "/{id}", headers = "X-API-Version=2")
    public ResponseEntity<Map<String, Object>> getByIdV2(
            @Parameter(description = "ID do filme") @PathVariable Long id) {
        Film film = service.findById(id);

        Map<String, Object> summary = new HashMap<>();
        summary.put("version", 2);
        summary.put("id", film.getId());
        summary.put("title", film.getTitle());
        summary.put("episodeId", film.getEpisodeId());
        summary.put("director", film.getDirector());
        summary.put("releaseDate", film.getReleaseDate());
        summary.put("characterCount", film.getCharacters() != null ? film.getCharacters().size() : 0);
        summary.put("starshipCount", film.getStarships() != null ? film.getStarships().size() : 0);

        return ResponseEntity.ok(summary);
    }
}
