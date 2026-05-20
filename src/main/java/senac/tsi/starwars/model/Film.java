package senac.tsi.starwars.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"characters", "starships"})
@EqualsAndHashCode(exclude = {"characters", "starships"})
@Entity
@Table(name = "films")
public class Film {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 200, message = "Título deve ter no máximo 200 caracteres")
    @Column(nullable = false)
    @Schema(example = "A New Hope")
    private String title;

    @Schema(example = "4")
    private Integer episodeId;

    @NotBlank(message = "Diretor é obrigatório")
    @Size(max = 200)
    @Schema(example = "George Lucas")
    private String director;

    @Size(max = 500)
    @Schema(example = "Gary Kurtz, Rick McCallum")
    private String producer;

    @Schema(example = "1977-05-25")
    private String releaseDate;

    @Column(columnDefinition = "TEXT")
    @Schema(example = "It is a period of civil war...")
    private String openingCrawl;

    @Schema(example = "https://swapi.dev/api/films/1/")
    private String swapiUrl;

    /**
     * Relacionamento @ManyToMany com Person.
     * Um filme tem vários personagens; um personagem aparece em vários filmes.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "film_characters",
            joinColumns = @JoinColumn(name = "film_id"),
            inverseJoinColumns = @JoinColumn(name = "person_id")
    )
    private Set<Person> characters = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "film_starships",
            joinColumns = @JoinColumn(name = "film_id"),
            inverseJoinColumns = @JoinColumn(name = "starship_id")
    )
    private Set<Starship> starships = new HashSet<>();
}
