package senac.tsi.starwars.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "starships")
public class Starship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
    @Column(nullable = false)
    private String name;

    @Size(max = 200)
    private String model;

    @Size(max = 300)
    private String manufacturer;

    private String costInCredits;
    private String length;
    private String crew;
    private String passengers;

    @Size(max = 200)
    private String starshipClass;

    private String swapiUrl;
}
