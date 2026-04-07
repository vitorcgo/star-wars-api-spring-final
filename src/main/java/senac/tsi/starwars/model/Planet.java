package senac.tsi.starwars.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private String name;

    /**
     * Enum de clima — requisito da entrega acadêmica.
     * A SWAPI retorna strings como "arid", "temperate", etc.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanetClimate climate = PlanetClimate.UNKNOWN;

    @Size(max = 500)
    private String terrain;

    private String population;
    private String diameter;
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
