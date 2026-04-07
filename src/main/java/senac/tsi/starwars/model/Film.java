package senac.tsi.starwars.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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
    private String title;

    private Integer episodeId;

    @NotBlank(message = "Diretor é obrigatório")
    @Size(max = 200)
    private String director;

    @Size(max = 500)
    private String producer;

    private String releaseDate;

    @Column(columnDefinition = "TEXT")
    private String openingCrawl;

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
    private List<Person> characters = new ArrayList<>();

    /**
     * Relacionamento @ManyToMany com Starship.
     * Um filme apresenta várias naves; uma nave aparece em vários filmes.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "film_starships",
            joinColumns = @JoinColumn(name = "film_id"),
            inverseJoinColumns = @JoinColumn(name = "starship_id")
    )
    private List<Starship> starships = new ArrayList<>();
}
