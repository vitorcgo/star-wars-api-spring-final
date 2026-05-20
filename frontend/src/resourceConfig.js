export const resourceConfigs = {
  films: {
    label: 'Films',
    path: '/api/films',
    versionable: true,
    searchModes: [
      { label: 'Title', key: 'title', endpoint: '/search/by-title' },
      { label: 'Director', key: 'director', endpoint: '/search/by-director' }
    ],
    defaultSearchMode: 'title',
    summaryFields: ['title', 'episodeId', 'director', 'releaseDate'],
    fields: [
      { name: 'title', label: 'Title', type: 'text', required: true },
      { name: 'episodeId', label: 'Episode', type: 'number' },
      { name: 'director', label: 'Director', type: 'text', required: true },
      { name: 'producer', label: 'Producer', type: 'text' },
      { name: 'releaseDate', label: 'Release date', type: 'text' },
      { name: 'openingCrawl', label: 'Opening crawl', type: 'textarea' },
      { name: 'swapiUrl', label: 'SWAPI URL', type: 'text' },
      { name: 'characters', label: 'Characters', type: 'multiRelation', source: 'people' },
      { name: 'starships', label: 'Starships', type: 'multiRelation', source: 'starships' }
    ],
    defaults: {
      title: '',
      episodeId: '',
      director: '',
      producer: '',
      releaseDate: '',
      openingCrawl: '',
      swapiUrl: '',
      characters: [],
      starships: []
    }
  },
  people: {
    label: 'People',
    path: '/api/people',
    searchModes: [
      { label: 'Name', key: 'name', endpoint: '/search/by-name' },
      { label: 'Gender', key: 'gender', endpoint: '/search/by-gender' }
    ],
    defaultSearchMode: 'name',
    summaryFields: ['name', 'birthYear', 'gender', 'height'],
    fields: [
      { name: 'name', label: 'Name', type: 'text', required: true },
      { name: 'birthYear', label: 'Birth year', type: 'text' },
      { name: 'gender', label: 'Gender', type: 'text' },
      { name: 'height', label: 'Height', type: 'text' },
      { name: 'mass', label: 'Mass', type: 'text' },
      { name: 'swapiUrl', label: 'SWAPI URL', type: 'text' },
      { name: 'planet', label: 'Planet', type: 'relation', source: 'planets' },
      { name: 'species', label: 'Species', type: 'relation', source: 'species' },
      { name: 'primaryShip', label: 'Primary ship', type: 'relation', source: 'starships' }
    ],
    defaults: {
      name: '',
      birthYear: '',
      gender: '',
      height: '',
      mass: '',
      swapiUrl: '',
      planet: '',
      species: '',
      primaryShip: ''
    }
  },
  planets: {
    label: 'Planets',
    path: '/api/planets',
    searchModes: [
      { label: 'Name', key: 'name', endpoint: '/search/by-name' },
      { label: 'Climate', key: 'climate', endpoint: '/search/by-climate' }
    ],
    defaultSearchMode: 'name',
    summaryFields: ['name', 'climate', 'terrain', 'population'],
    fields: [
      { name: 'name', label: 'Name', type: 'text', required: true },
      {
        name: 'climate',
        label: 'Climate',
        type: 'enum',
        required: true,
        options: ['ARID', 'TEMPERATE', 'TROPICAL', 'FROZEN', 'MURKY', 'HOT', 'WINDY', 'POLLUTED', 'SUBARCTIC', 'SUPERHEATED', 'ARTIFICIAL', 'UNKNOWN']
      },
      { name: 'terrain', label: 'Terrain', type: 'text' },
      { name: 'population', label: 'Population', type: 'text' },
      { name: 'diameter', label: 'Diameter', type: 'text' },
      { name: 'swapiUrl', label: 'SWAPI URL', type: 'text' }
    ],
    defaults: {
      name: '',
      climate: 'UNKNOWN',
      terrain: '',
      population: '',
      diameter: '',
      swapiUrl: ''
    }
  },
  species: {
    label: 'Species',
    path: '/api/species',
    searchModes: [
      { label: 'Name', key: 'name', endpoint: '/search/by-name' },
      { label: 'Classification', key: 'classification', endpoint: '/search/by-classification' }
    ],
    defaultSearchMode: 'name',
    summaryFields: ['name', 'classification', 'language', 'averageHeight'],
    fields: [
      { name: 'name', label: 'Name', type: 'text', required: true },
      {
        name: 'classification',
        label: 'Classification',
        type: 'enum',
        required: true,
        options: ['MAMMAL', 'REPTILE', 'AMPHIBIAN', 'ARTIFICIAL', 'INSECTOID', 'GASTROPOD', 'SENTIENT', 'UNKNOWN']
      },
      { name: 'language', label: 'Language', type: 'text' },
      { name: 'averageHeight', label: 'Average height', type: 'text' },
      { name: 'averageLifespan', label: 'Average lifespan', type: 'text' },
      { name: 'swapiUrl', label: 'SWAPI URL', type: 'text' },
      { name: 'homeworld', label: 'Homeworld', type: 'relation', source: 'planets' }
    ],
    defaults: {
      name: '',
      classification: 'UNKNOWN',
      language: '',
      averageHeight: '',
      averageLifespan: '',
      swapiUrl: '',
      homeworld: ''
    }
  },
  starships: {
    label: 'Starships',
    path: '/api/starships',
    searchModes: [
      { label: 'Name', key: 'name', endpoint: '/search/by-name' },
      { label: 'Class', key: 'starshipClass', endpoint: '/search/by-class' }
    ],
    defaultSearchMode: 'name',
    summaryFields: ['name', 'model', 'starshipClass', 'crew'],
    fields: [
      { name: 'name', label: 'Name', type: 'text', required: true },
      { name: 'model', label: 'Model', type: 'text' },
      { name: 'manufacturer', label: 'Manufacturer', type: 'text' },
      { name: 'costInCredits', label: 'Cost in credits', type: 'text' },
      { name: 'length', label: 'Length', type: 'text' },
      { name: 'crew', label: 'Crew', type: 'text' },
      { name: 'passengers', label: 'Passengers', type: 'text' },
      { name: 'starshipClass', label: 'Starship class', type: 'text' },
      { name: 'swapiUrl', label: 'SWAPI URL', type: 'text' }
    ],
    defaults: {
      name: '',
      model: '',
      manufacturer: '',
      costInCredits: '',
      length: '',
      crew: '',
      passengers: '',
      starshipClass: '',
      swapiUrl: ''
    }
  }
};

export const relationSources = {
  planets: { label: 'Planets', resource: 'planets' },
  people: { label: 'People', resource: 'people' },
  species: { label: 'Species', resource: 'species' },
  starships: { label: 'Starships', resource: 'starships' }
};
