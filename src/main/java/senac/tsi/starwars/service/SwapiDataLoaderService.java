package senac.tsi.starwars.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import senac.tsi.starwars.dto.*;
import senac.tsi.starwars.model.*;
import senac.tsi.starwars.model.enums.PlanetClimate;
import senac.tsi.starwars.model.enums.SpeciesClassification;
import senac.tsi.starwars.repository.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Carrega os dados da SWAPI no banco H2 ao iniciar a aplicação.
 * Implementa CommandLineRunner para execução pós-inicialização do contexto Spring.
 *
 * Ordem de carregamento (respeita dependências entre entidades):
 *  1. Planets   → sem dependências
 *  2. Species   → depende de Planet (homeworld, @OneToOne)
 *  3. Starships → sem dependências
 *  4. People    → depende de Planet, Species, Starship
 *  5. Films     → depende de Person e Starship (@ManyToMany)
 */
@Slf4j
@Service
public class SwapiDataLoaderService implements CommandLineRunner {

    private final RestClient swapiRestClient;
    private final PlanetRepository planetRepository;
    private final SpeciesRepository speciesRepository;
    private final PersonRepository personRepository;
    private final StarshipRepository starshipRepository;
    private final FilmRepository filmRepository;

    // Mapas de URL-SWAPI → ID no nosso banco (para resolver referências)
    private final Map<String, Long> planetUrlToId = new HashMap<>();
    private final Map<String, Long> speciesUrlToId = new HashMap<>();
    private final Map<String, Long> personUrlToId = new HashMap<>();
    private final Map<String, Long> starshipUrlToId = new HashMap<>();

    // Controle de constraint @OneToOne: cada planeta/nave só pode ter um vínculo
    private final Set<Long> usedPlanetIdsForHomeworld = new HashSet<>();
    private final Set<Long> usedStarshipIdsForPilot = new HashSet<>();

    @Autowired
    public SwapiDataLoaderService(RestClient swapiRestClient,
                                   PlanetRepository planetRepository,
                                   SpeciesRepository speciesRepository,
                                   PersonRepository personRepository,
                                   StarshipRepository starshipRepository,
                                   FilmRepository filmRepository) {
        this.swapiRestClient = swapiRestClient;
        this.planetRepository = planetRepository;
        this.speciesRepository = speciesRepository;
        this.personRepository = personRepository;
        this.starshipRepository = starshipRepository;
        this.filmRepository = filmRepository;
    }

    @Override
    public void run(String... args) {
        log.info("=== Iniciando carregamento de dados da SWAPI ===");
        loadPlanets();
        loadSpecies();
        loadStarships();
        loadPeople();
        loadFilms();
        log.info("=== Carregamento concluído ===");
    }

    // -------------------------------------------------------------------------
    // Planetas
    // -------------------------------------------------------------------------

    private void loadPlanets() {
        log.info("Carregando planetas...");
        try {
            SwapiPlanetDto[] dtos = swapiRestClient.get()
                    .uri("/planets/")
                    .retrieve()
                    .body(SwapiPlanetDto[].class);

            if (dtos == null || dtos.length == 0) {
                log.warn("Nenhum planeta retornado pela SWAPI.");
                return;
            }

            for (SwapiPlanetDto dto : dtos) {
                try {
                    Planet planet = new Planet();
                    planet.setName(dto.getName());
                    planet.setClimate(parseClimate(dto.getClimate()));
                    planet.setTerrain(dto.getTerrain());
                    planet.setPopulation(dto.getPopulation());
                    planet.setDiameter(dto.getDiameter());
                    planet.setSwapiUrl(dto.getUrl());

                    planet = planetRepository.save(planet);
                    if (dto.getUrl() != null) {
                        planetUrlToId.put(dto.getUrl(), planet.getId());
                    }
                } catch (Exception e) {
                    log.error("Erro ao salvar planeta '{}': {}", dto.getName(), e.getMessage());
                }
            }
            log.info("{} planetas carregados com sucesso.", dtos.length);

        } catch (HttpClientErrorException.NotFound e) {
            // TRATAMENTO CRÍTICO: 404 — endpoint não encontrado
            log.error("404: Endpoint /planets/ não encontrado na SWAPI.");
        } catch (HttpServerErrorException e) {
            // TRATAMENTO CRÍTICO: 5xx — erro no servidor da SWAPI
            log.error("Erro 5xx ao carregar planetas: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Erro inesperado ao carregar planetas: {}", e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Espécies
    // -------------------------------------------------------------------------

    private void loadSpecies() {
        log.info("Carregando espécies...");
        try {
            SwapiSpeciesDto[] dtos = swapiRestClient.get()
                    .uri("/species/")
                    .retrieve()
                    .body(SwapiSpeciesDto[].class);

            if (dtos == null || dtos.length == 0) {
                log.warn("Nenhuma espécie retornada pela SWAPI.");
                return;
            }

            for (SwapiSpeciesDto dto : dtos) {
                try {
                    Species species = new Species();
                    species.setName(dto.getName());
                    species.setClassification(parseClassification(dto.getClassification()));
                    species.setLanguage(dto.getLanguage());
                    species.setAverageHeight(dto.getAverageHeight());
                    species.setAverageLifespan(dto.getAverageLifespan());
                    species.setSwapiUrl(dto.getUrl());

                    // @OneToOne: atribui homeworld apenas se o planeta ainda não foi vinculado
                    if (dto.getHomeworld() != null && !dto.getHomeworld().isBlank()) {
                        Long planetId = planetUrlToId.get(dto.getHomeworld());
                        if (planetId != null && !usedPlanetIdsForHomeworld.contains(planetId)) {
                            planetRepository.findById(planetId).ifPresent(p -> {
                                species.setHomeworld(p);
                                usedPlanetIdsForHomeworld.add(planetId);
                            });
                        }
                    }

                    Species saved = speciesRepository.save(species);
                    if (dto.getUrl() != null) {
                        speciesUrlToId.put(dto.getUrl(), saved.getId());
                    }
                } catch (Exception e) {
                    log.error("Erro ao salvar espécie '{}': {}", dto.getName(), e.getMessage());
                }
            }
            log.info("{} espécies carregadas com sucesso.", dtos.length);

        } catch (HttpClientErrorException.NotFound e) {
            log.error("404: Endpoint /species/ não encontrado na SWAPI.");
        } catch (HttpServerErrorException e) {
            log.error("Erro 5xx ao carregar espécies: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Erro inesperado ao carregar espécies: {}", e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Naves
    // -------------------------------------------------------------------------

    private void loadStarships() {
        log.info("Carregando naves estelares...");
        try {
            SwapiStarshipDto[] dtos = swapiRestClient.get()
                    .uri("/starships/")
                    .retrieve()
                    .body(SwapiStarshipDto[].class);

            if (dtos == null || dtos.length == 0) {
                log.warn("Nenhuma nave retornada pela SWAPI.");
                return;
            }

            for (SwapiStarshipDto dto : dtos) {
                try {
                    Starship starship = new Starship();
                    starship.setName(dto.getName());
                    starship.setModel(dto.getModel());
                    starship.setManufacturer(dto.getManufacturer());
                    starship.setCostInCredits(dto.getCostInCredits());
                    starship.setLength(dto.getLength());
                    starship.setCrew(dto.getCrew());
                    starship.setPassengers(dto.getPassengers());
                    starship.setStarshipClass(dto.getStarshipClass());
                    starship.setSwapiUrl(dto.getUrl());

                    Starship saved = starshipRepository.save(starship);
                    if (dto.getUrl() != null) {
                        starshipUrlToId.put(dto.getUrl(), saved.getId());
                    }
                } catch (Exception e) {
                    log.error("Erro ao salvar nave '{}': {}", dto.getName(), e.getMessage());
                }
            }
            log.info("{} naves carregadas com sucesso.", dtos.length);

        } catch (HttpClientErrorException.NotFound e) {
            log.error("404: Endpoint /starships/ não encontrado na SWAPI.");
        } catch (HttpServerErrorException e) {
            log.error("Erro 5xx ao carregar naves: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Erro inesperado ao carregar naves: {}", e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Personagens
    // -------------------------------------------------------------------------

    private void loadPeople() {
        log.info("Carregando personagens...");
        try {
            SwapiPersonDto[] dtos = swapiRestClient.get()
                    .uri("/people/")
                    .retrieve()
                    .body(SwapiPersonDto[].class);

            if (dtos == null || dtos.length == 0) {
                log.warn("Nenhum personagem retornado pela SWAPI.");
                return;
            }

            for (SwapiPersonDto dto : dtos) {
                try {
                    Person person = new Person();
                    person.setName(dto.getName());
                    person.setBirthYear(dto.getBirthYear());
                    person.setGender(dto.getGender());
                    person.setHeight(dto.getHeight());
                    person.setMass(dto.getMass());
                    person.setSwapiUrl(dto.getUrl());

                    // @ManyToOne: planeta natal
                    if (dto.getHomeworld() != null && !dto.getHomeworld().isBlank()) {
                        Long planetId = planetUrlToId.get(dto.getHomeworld());
                        if (planetId != null) {
                            planetRepository.findById(planetId).ifPresent(person::setPlanet);
                        }
                    }

                    // @ManyToOne: espécie (pega a primeira da lista)
                    if (dto.getSpecies() != null && !dto.getSpecies().isEmpty()) {
                        Long speciesId = speciesUrlToId.get(dto.getSpecies().get(0));
                        if (speciesId != null) {
                            speciesRepository.findById(speciesId).ifPresent(person::setSpecies);
                        }
                    }

                    // @OneToOne: nave principal (pega a primeira nave ainda não vinculada)
                    if (dto.getStarships() != null) {
                        for (String shipUrl : dto.getStarships()) {
                            Long shipId = starshipUrlToId.get(shipUrl);
                            if (shipId != null && !usedStarshipIdsForPilot.contains(shipId)) {
                                starshipRepository.findById(shipId).ifPresent(ship -> {
                                    person.setPrimaryShip(ship);
                                    usedStarshipIdsForPilot.add(shipId);
                                });
                                break; // só uma nave por personagem
                            }
                        }
                    }

                    Person saved = personRepository.save(person);
                    if (dto.getUrl() != null) {
                        personUrlToId.put(dto.getUrl(), saved.getId());
                    }
                } catch (Exception e) {
                    log.error("Erro ao salvar personagem '{}': {}", dto.getName(), e.getMessage());
                }
            }
            log.info("{} personagens carregados com sucesso.", dtos.length);

        } catch (HttpClientErrorException.NotFound e) {
            log.error("404: Endpoint /people/ não encontrado na SWAPI.");
        } catch (HttpServerErrorException e) {
            log.error("Erro 5xx ao carregar personagens: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Erro inesperado ao carregar personagens: {}", e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Filmes
    // -------------------------------------------------------------------------

    private void loadFilms() {
        log.info("Carregando filmes...");
        try {
            SwapiFilmDto[] dtos = swapiRestClient.get()
                    .uri("/films/")
                    .retrieve()
                    .body(SwapiFilmDto[].class);

            if (dtos == null || dtos.length == 0) {
                log.warn("Nenhum filme retornado pela SWAPI.");
                return;
            }

            for (SwapiFilmDto dto : dtos) {
                try {
                    Film film = new Film();
                    film.setTitle(dto.getTitle());
                    film.setEpisodeId(dto.getEpisodeId());
                    film.setDirector(dto.getDirector());
                    film.setProducer(dto.getProducer());
                    film.setReleaseDate(dto.getReleaseDate());
                    film.setOpeningCrawl(dto.getOpeningCrawl());
                    film.setSwapiUrl(dto.getUrl());

                    // @ManyToMany: personagens
                    if (dto.getCharacters() != null) {
                        for (String charUrl : dto.getCharacters()) {
                            Long personId = personUrlToId.get(charUrl);
                            if (personId != null) {
                                personRepository.findById(personId)
                                        .ifPresent(film.getCharacters()::add);
                            }
                        }
                    }

                    // @ManyToMany: naves
                    if (dto.getStarships() != null) {
                        for (String shipUrl : dto.getStarships()) {
                            Long shipId = starshipUrlToId.get(shipUrl);
                            if (shipId != null) {
                                starshipRepository.findById(shipId)
                                        .ifPresent(film.getStarships()::add);
                            }
                        }
                    }

                    filmRepository.save(film);
                } catch (Exception e) {
                    log.error("Erro ao salvar filme '{}': {}", dto.getTitle(), e.getMessage());
                }
            }
            log.info("{} filmes carregados com sucesso.", dtos.length);

        } catch (HttpClientErrorException.NotFound e) {
            log.error("404: Endpoint /films/ não encontrado na SWAPI.");
        } catch (HttpServerErrorException e) {
            log.error("Erro 5xx ao carregar filmes: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Erro inesperado ao carregar filmes: {}", e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Utilitários de parsing
    // -------------------------------------------------------------------------

    /**
     * Converte a string de clima da SWAPI para o Enum PlanetClimate.
     * A SWAPI pode retornar valores compostos como "arid, temperate" — usamos o primeiro.
     */
    private PlanetClimate parseClimate(String climate) {
        if (climate == null || climate.isBlank() || climate.equalsIgnoreCase("unknown")) {
            return PlanetClimate.UNKNOWN;
        }
        String first = climate.split(",")[0].trim().toUpperCase().replace(" ", "_");
        try {
            return PlanetClimate.valueOf(first);
        } catch (IllegalArgumentException e) {
            return PlanetClimate.UNKNOWN;
        }
    }

    /**
     * Converte a string de classificação da SWAPI para o Enum SpeciesClassification.
     */
    private SpeciesClassification parseClassification(String classification) {
        if (classification == null || classification.isBlank() || classification.equalsIgnoreCase("unknown")) {
            return SpeciesClassification.UNKNOWN;
        }
        try {
            return SpeciesClassification.valueOf(classification.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return SpeciesClassification.UNKNOWN;
        }
    }
}
