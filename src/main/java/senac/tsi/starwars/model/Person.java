package senac.tsi.starwars.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    private String name;

    private String birthYear;
    private String gender;
    private String height;
    private String mass;
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
