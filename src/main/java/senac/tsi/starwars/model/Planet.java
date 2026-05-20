package senac.tsi.starwars.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import senac.tsi.starwars.model.enums.PlanetClimate;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "persons")
@EqualsAndHashCode(exclude = "persons")
@Entity
@Table(name = "planets")
public class Planet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
    @Column(nullable = false)
    @Schema(example = "Tatooine")
    private String name;

    /**
     * Enum de clima — requisito da entrega acadêmica.
     * A SWAPI retorna strings como "arid", "temperate", etc.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(example = "ARID")
    private PlanetClimate climate = PlanetClimate.UNKNOWN;

    @Size(max = 500)
    @Schema(example = "desert")
    private String terrain;

    @Schema(example = "200000")
    private String population;
    @Schema(example = "10465")
    private String diameter;
    @Schema(example = "https://swapi.dev/api/planets/1/")
    private String swapiUrl;

    /**
     * Relacionamento @OneToMany → @ManyToOne com Person.
     * Um planeta pode ter vários residentes.
     * @JsonIgnore evita referência circular na serialização JSON.
     */
    @OneToMany(mappedBy = "planet", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Person> persons = new ArrayList<>();
}
