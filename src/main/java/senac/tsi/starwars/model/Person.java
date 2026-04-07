package senac.tsi.starwars.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "persons")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
    @Column(nullable = false)
    private String name;

    private String birthYear;
    private String gender;
    private String height;
    private String mass;
    private String swapiUrl;

    /**
     * Relacionamento @ManyToOne → @OneToMany com Planet.
     * Muitos personagens podem ser do mesmo planeta.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "planet_id")
    private Planet planet;

    /**
     * Relacionamento @ManyToOne com Species.
     * Muitos personagens pertencem à mesma espécie.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "species_id")
    private Species species;

    /**
     * Relacionamento @OneToOne com Starship.
     * Cada personagem tem, no máximo, uma nave principal.
     * A constraint unique=true garante que cada nave só tem um piloto principal.
     */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "primary_ship_id", unique = true)
    private Starship primaryShip;
}
