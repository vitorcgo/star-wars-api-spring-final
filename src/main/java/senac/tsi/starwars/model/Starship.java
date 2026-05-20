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
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "starships")
public class Starship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
    @Column(nullable = false)
    @Schema(example = "Millennium Falcon")
    private String name;

    @Size(max = 200)
    @Schema(example = "YT-1300 light freighter")
    private String model;

    @Size(max = 300)
    @Schema(example = "Corellian Engineering Corporation")
    private String manufacturer;

    @Schema(example = "100000")
    private String costInCredits;
    @Schema(example = "34.37")
    private String length;
    @Schema(example = "4")
    private String crew;
    @Schema(example = "6")
    private String passengers;

    @Size(max = 200)
    @Schema(example = "Light freighter")
    private String starshipClass;

    @Schema(example = "https://swapi.dev/api/starships/10/")
    private String swapiUrl;
}
