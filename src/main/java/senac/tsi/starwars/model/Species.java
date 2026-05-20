package senac.tsi.starwars.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import senac.tsi.starwars.model.enums.SpeciesClassification;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "persons")
@EqualsAndHashCode(exclude = "persons")
@Entity
@Table(name = "species")
public class Species {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
    @Column(nullable = false)
    @Schema(example = "Human")
    private String name;

    /**
     * Enum de classificação biológica — requisito da entrega acadêmica.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(example = "MAMMAL")
    private SpeciesClassification classification = SpeciesClassification.UNKNOWN;

    @Size(max = 200)
    @Schema(example = "Basic Galactic")
    private String language;

    @Schema(example = "180")
    private String averageHeight;
    @Schema(example = "120")
    private String averageLifespan;
    @Schema(example = "https://swapi.dev/api/species/1/")
    private String swapiUrl;

    /**
     * Relacionamento @OneToOne com Planet (planeta natal da espécie).
     * Cada planeta pode ser o mundo natal de somente uma espécie no nosso modelo.
     * A constraint unique=true garante isso no banco.
     */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "homeworld_id", unique = true)
    private Planet homeworld;

    /**
     * Lado inverso do @ManyToOne em Person.
     * @JsonIgnore evita referência circular.
     */
    @OneToMany(mappedBy = "species", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Person> persons = new ArrayList<>();
}
