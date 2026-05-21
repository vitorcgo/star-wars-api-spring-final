export const resourceConfigs = {
  films: {
    label: 'Filmes',
    subtitle: 'Controle de episódios, personagens e naves ligadas aos filmes.',
    path: '/api/films',
    versionable: true,
    searchModes: [
      { label: 'Título', key: 'title', endpoint: '/search/by-title' },
      { label: 'Diretor', key: 'director', endpoint: '/search/by-director' }
    ],
    defaultSearchMode: 'title',
    summaryFields: ['title', 'episodeId', 'director', 'releaseDate'],
    fields: [
      { name: 'title', label: 'Título', type: 'text', required: true },
      { name: 'episodeId', label: 'Episódio', type: 'number' },
      { name: 'director', label: 'Diretor', type: 'text', required: true },
      { name: 'producer', label: 'Produtor', type: 'text' },
      { name: 'releaseDate', label: 'Data de lançamento', type: 'text' },
      { name: 'openingCrawl', label: 'Texto de abertura', type: 'textarea' },
      { name: 'swapiUrl', label: 'URL da SWAPI', type: 'text' },
      { name: 'characters', label: 'Personagens', type: 'multiRelation', source: 'people' },
      { name: 'starships', label: 'Naves', type: 'multiRelation', source: 'starships' }
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
    label: 'Personagens',
    subtitle: 'Cadastro de personagens com vínculos de planeta, espécie e nave principal.',
    path: '/api/people',
    searchModes: [
      { label: 'Nome', key: 'name', endpoint: '/search/by-name' },
      { label: 'Gênero', key: 'gender', endpoint: '/search/by-gender' }
    ],
    defaultSearchMode: 'name',
    summaryFields: ['name', 'birthYear', 'gender', 'height'],
    fields: [
      { name: 'name', label: 'Nome', type: 'text', required: true },
      { name: 'birthYear', label: 'Ano de nascimento', type: 'text' },
      { name: 'gender', label: 'Gênero', type: 'text' },
      { name: 'height', label: 'Altura', type: 'text' },
      { name: 'mass', label: 'Massa', type: 'text' },
      { name: 'swapiUrl', label: 'URL da SWAPI', type: 'text' },
      { name: 'planet', label: 'Planeta', type: 'relation', source: 'planets' },
      { name: 'species', label: 'Espécie', type: 'relation', source: 'species' },
      { name: 'primaryShip', label: 'Nave principal', type: 'relation', source: 'starships' }
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
    label: 'Planetas',
    subtitle: 'Controle de mundos, clima e território.',
    path: '/api/planets',
    searchModes: [
      { label: 'Nome', key: 'name', endpoint: '/search/by-name' },
      { label: 'Clima', key: 'climate', endpoint: '/search/by-climate' }
    ],
    defaultSearchMode: 'name',
    summaryFields: ['name', 'climate', 'terrain', 'population'],
    fields: [
      { name: 'name', label: 'Nome', type: 'text', required: true },
      {
        name: 'climate',
        label: 'Clima',
        type: 'enum',
        required: true,
        options: ['ARID', 'TEMPERATE', 'TROPICAL', 'FROZEN', 'MURKY', 'HOT', 'WINDY', 'POLLUTED', 'SUBARCTIC', 'SUPERHEATED', 'ARTIFICIAL', 'UNKNOWN']
      },
      { name: 'terrain', label: 'Terreno', type: 'text' },
      { name: 'population', label: 'População', type: 'text' },
      { name: 'diameter', label: 'Diâmetro', type: 'text' },
      { name: 'swapiUrl', label: 'URL da SWAPI', type: 'text' }
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
    label: 'Espécies',
    subtitle: 'Cadastro de espécies e planeta de origem.',
    path: '/api/species',
    searchModes: [
      { label: 'Nome', key: 'name', endpoint: '/search/by-name' },
      { label: 'Classificação', key: 'classification', endpoint: '/search/by-classification' }
    ],
    defaultSearchMode: 'name',
    summaryFields: ['name', 'classification', 'language', 'averageHeight'],
    fields: [
      { name: 'name', label: 'Nome', type: 'text', required: true },
      {
        name: 'classification',
        label: 'Classificação',
        type: 'enum',
        required: true,
        options: ['MAMMAL', 'REPTILE', 'AMPHIBIAN', 'ARTIFICIAL', 'INSECTOID', 'GASTROPOD', 'SENTIENT', 'UNKNOWN']
      },
      { name: 'language', label: 'Idioma', type: 'text' },
      { name: 'averageHeight', label: 'Altura média', type: 'text' },
      { name: 'averageLifespan', label: 'Expectativa de vida', type: 'text' },
      { name: 'swapiUrl', label: 'URL da SWAPI', type: 'text' },
      { name: 'homeworld', label: 'Planeta natal', type: 'relation', source: 'planets' }
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
    label: 'Naves',
    subtitle: 'Cadastro de naves estelares, modelos e capacidade.',
    path: '/api/starships',
    searchModes: [
      { label: 'Nome', key: 'name', endpoint: '/search/by-name' },
      { label: 'Classe', key: 'starshipClass', endpoint: '/search/by-class' }
    ],
    defaultSearchMode: 'name',
    summaryFields: ['name', 'model', 'starshipClass', 'crew'],
    fields: [
      { name: 'name', label: 'Nome', type: 'text', required: true },
      { name: 'model', label: 'Modelo', type: 'text' },
      { name: 'manufacturer', label: 'Fabricante', type: 'text' },
      { name: 'costInCredits', label: 'Custo em créditos', type: 'text' },
      { name: 'length', label: 'Comprimento', type: 'text' },
      { name: 'crew', label: 'Tripulação', type: 'text' },
      { name: 'passengers', label: 'Passageiros', type: 'text' },
      { name: 'starshipClass', label: 'Classe', type: 'text' },
      { name: 'swapiUrl', label: 'URL da SWAPI', type: 'text' }
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
  planets: { label: 'Planetas', resource: 'planets' },
  people: { label: 'Personagens', resource: 'people' },
  species: { label: 'Espécies', resource: 'species' },
  starships: { label: 'Naves', resource: 'starships' }
};
