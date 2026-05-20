package senac.tsi.starwars.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"planet", "species", "primaryShip"})
@EqualsAndHashCode(exclude = {"planet", "species", "primaryShip"})
@Entity
@Table(name = "persons")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
    @Column(nullable = false)
    @Schema(example = "Luke Skywalker")
    private String name;

    @Schema(example = "19BBY")
    private String birthYear;
    @Schema(example = "male")
    private String gender;
    @Schema(example = "172")
    private String height;
    @Schema(example = "77")
    private String mass;
    @Schema(example = "https://swapi.dev/api/people/1/")
    private String swapiUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "planet_id")
    private Planet planet;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "species_id")
    private Species species;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "primary_ship_id", unique = true)
    private Starship primaryShip;
}
