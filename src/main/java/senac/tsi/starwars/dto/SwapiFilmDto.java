package senac.tsi.starwars.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SwapiFilmDto {
    private String title;
    private String director;
    private String producer;

    @JsonProperty("episode_id")
    private Integer episodeId;

    @JsonProperty("release_date")
    private String releaseDate;

    @JsonProperty("opening_crawl")
    private String openingCrawl;

    // URLs de referência para personagens e naves
    private List<String> characters = new ArrayList<>();
    private List<String> starships = new ArrayList<>();

    private String url;
}
