# Star Wars SWAPI - API REST

API RESTful que consome dados da [SWAPI](https://swapi.info/) e os expõe com suporte a paginação, HATEOAS, autenticação por API Key, rate limiting, idempotência e documentação Swagger.

**Projeto Acadêmico** — Spring Boot 4 + Java 17

## Como Executar

### Pré-requisitos
- Java 17+
- Maven 3.9+

### Rodar localmente
```bash
./mvnw spring-boot:run
```

### Rodar com Docker
```bash
docker build -t starwars-api .
docker run -p 8080:8080 starwars-api
```

### Acessos
| Recurso | URL |
|---------|-----|
| API Base | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| H2 Console | http://localhost:8080/h2-console |
| OpenAPI JSON | http://localhost:8080/api-docs |

**H2 Console:** JDBC URL = `jdbc:h2:mem:starwarsdb`, User = `sa`, Password = (vazio)

## Entidades e Relacionamentos

| Entidade | Relacionamentos |
|----------|----------------|
| **Planet** | @OneToMany → Person (residentes) |
| **Species** | @OneToOne → Planet (homeworld), @OneToMany → Person |
| **Starship** | @OneToOne ← Person (piloto principal), @ManyToMany ← Film |
| **Person** | @ManyToOne → Planet, @ManyToOne → Species, @OneToOne → Starship |
| **Film** | @ManyToMany → Person (personagens), @ManyToMany → Starship (naves) |

### Enums
- `PlanetClimate`: ARID, TEMPERATE, TROPICAL, FROZEN, MURKY, HOT, WINDY, POLLUTED, SUBARCTIC, SUPERHEATED, ARTIFICIAL, UNKNOWN
- `SpeciesClassification`: MAMMAL, REPTILE, AMPHIBIAN, ARTIFICIAL, INSECTOID, GASTROPOD, SENTIENT, UNKNOWN

## Endpoints

### Films (`/api/films`)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/films` | Lista todos (paginado) |
| GET | `/api/films/{id}` | Busca por ID |
| POST | `/api/films` | Cria filme (requer X-API-Key) |
| PUT | `/api/films/{id}` | Atualiza filme (requer X-API-Key) |
| DELETE | `/api/films/{id}` | Remove filme (requer X-API-Key) |
| GET | `/api/films/search/by-director?director=` | Busca por diretor |
| GET | `/api/films/search/by-title?title=` | Busca por título |

### People (`/api/people`)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/people` | Lista todos (paginado) |
| GET | `/api/people/{id}` | Busca por ID |
| POST | `/api/people` | Cria personagem (requer X-API-Key) |
| PUT | `/api/people/{id}` | Atualiza personagem (requer X-API-Key) |
| DELETE | `/api/people/{id}` | Remove personagem (requer X-API-Key) |
| GET | `/api/people/search/by-gender?gender=` | Busca por gênero |
| GET | `/api/people/search/by-name?name=` | Busca por nome |

### Planets (`/api/planets`)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/planets` | Lista todos (paginado) |
| GET | `/api/planets/{id}` | Busca por ID |
| POST | `/api/planets` | Cria planeta (requer X-API-Key) |
| PUT | `/api/planets/{id}` | Atualiza planeta (requer X-API-Key) |
| DELETE | `/api/planets/{id}` | Remove planeta (requer X-API-Key) |
| GET | `/api/planets/search/by-climate?climate=` | Busca por clima (enum) |
| GET | `/api/planets/search/by-name?name=` | Busca por nome |

### Species (`/api/species`)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/species` | Lista todos (paginado) |
| GET | `/api/species/{id}` | Busca por ID |
| POST | `/api/species` | Cria espécie (requer X-API-Key) |
| PUT | `/api/species/{id}` | Atualiza espécie (requer X-API-Key) |
| DELETE | `/api/species/{id}` | Remove espécie (requer X-API-Key) |
| GET | `/api/species/search/by-classification?classification=` | Busca por classificação (enum) |
| GET | `/api/species/search/by-name?name=` | Busca por nome |

### Starships (`/api/starships`)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/starships` | Lista todos (paginado) |
| GET | `/api/starships/{id}` | Busca por ID |
| POST | `/api/starships` | Cria nave (requer X-API-Key) |
| PUT | `/api/starships/{id}` | Atualiza nave (requer X-API-Key) |
| DELETE | `/api/starships/{id}` | Remove nave (requer X-API-Key) |
| GET | `/api/starships/search/by-class?starshipClass=` | Busca por classe |
| GET | `/api/starships/search/by-name?name=` | Busca por nome |

### Autenticacao (`/api/auth/keys`)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/auth/keys` | Gera nova API Key (público) |
| DELETE | `/api/auth/keys/{id}` | Revoga uma chave (requer X-API-Key) |

## Autenticação (X-API-Key)

Endpoints de escrita (POST, PUT, DELETE) exigem uma API Key válida no header `X-API-Key`.

```bash
# 1. Gerar chave
curl -X POST http://localhost:8080/api/auth/keys \
  -H "Content-Type: application/json" \
  -d '{"owner": "seu-nome"}'

# 2. Usar chave em requisições protegidas
curl -X POST http://localhost:8080/api/planets \
  -H "Content-Type: application/json" \
  -H "X-API-Key: SUA_CHAVE_AQUI" \
  -d '{"name": "Dagobah", "climate": "MURKY"}'
```

## Rate Limiting

- **Limite:** 20 requisições por minuto por IP
- **Headers de resposta:** `X-RateLimit-Limit`, `X-RateLimit-Remaining`
- **Quando excedido:** HTTP 429 com header `Retry-After`

## Idempotência (X-Idempotency-Key)

Envie o header `X-Idempotency-Key` em requisições POST para garantir que a operação não seja processada duas vezes:

```bash
curl -X POST http://localhost:8080/api/planets \
  -H "Content-Type: application/json" \
  -H "X-API-Key: SUA_CHAVE" \
  -H "X-Idempotency-Key: minha-chave-unica-123" \
  -d '{"name": "Naboo", "climate": "TEMPERATE"}'
```

Se repetir a mesma requisição com a mesma `X-Idempotency-Key`, a API retorna a resposta armazenada sem processar novamente. O header `X-Idempotency-Replayed: true` indica que foi uma resposta reutilizada.

## Versionamento (X-API-Version)

Use o header `X-API-Version` para acessar versões diferentes dos endpoints:

```bash
# V1 (padrão) — retorno completo com HATEOAS
curl http://localhost:8080/api/films

# V2 — retorno resumido (sem HATEOAS, com contadores)
curl -H "X-API-Version: 2" http://localhost:8080/api/films
```

## CORS

A API permite requisições cross-origin de `localhost:3000` e `localhost:8080` com métodos GET, POST, PUT, DELETE e OPTIONS.

## Tratamento de Erros

| Código | Situação |
|--------|----------|
| 400 | Dados inválidos / JSON mal formatado / Parâmetro errado |
| 401 | Header X-API-Key ausente |
| 403 | API Key inválida ou revogada |
| 404 | Recurso não encontrado |
| 409 | Violação de integridade (ex: deletar registro com dependentes) |
| 429 | Limite de requisições excedido |
| 500 | Erro interno (sem exposição de detalhes) |

## Tecnologias

- Spring Boot 4.0.3
- Java 17
- H2 Database (em memória)
- Spring Data JPA
- Spring HATEOAS
- Springdoc OpenAPI 3.0 (Swagger UI)
- Bean Validation (Jakarta)
- Lombok
- Maven
- Docker
