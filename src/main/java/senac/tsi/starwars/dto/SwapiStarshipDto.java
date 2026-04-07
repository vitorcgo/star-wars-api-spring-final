package senac.tsi.starwars.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SwapiStarshipDto {
    private String name;
    private String model;
    private String manufacturer;
    private String length;
    private String crew;
    private String passengers;

    @JsonProperty("cost_in_credits")
    private String costInCredits;

    @JsonProperty("starship_class")
    private String starshipClass;

    private String url;
}
