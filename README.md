# Desafio Técnico — Consulta de CEP

Aplicação Java / Spring Boot que consulta CEPs em uma API externa (mocada com
WireMock) e grava o log de cada consulta em banco relacional — com timestamp
e o payload retornado pela API.

Organizada em **Arquitetura Hexagonal (Ports & Adapters)**, com documentação
OpenAPI/Swagger, endpoints de observabilidade (Actuator) e pipeline de CI no
GitHub Actions.

<p align="center">
  <img src="docs/arquitetura-hexagonal.svg" alt="Arquitetura Hexagonal do projeto" width="640"/>
</p>

Três camadas concêntricas com dependências apontando sempre **para dentro**:
o domínio no núcleo define as *portas* (interfaces) que os *adaptadores* da
infraestrutura implementam. Detalhes em [`docs/architecture.md`](docs/architecture.md).

## Stack

- **Java 17** · Spring Boot 3.2 · Spring Data JPA · Spring Boot Actuator
- **Springdoc OpenAPI** 2.3 (Swagger UI)
- **PostgreSQL 16** (via Docker) · **H2** (modo local, sem Docker)
- **WireMock 3** (mock da API de CEP)
- **Docker** / Docker Compose
- JUnit 5 · Mockito · AssertJ
- **GitHub Actions** (CI/CD)

## Como executar (modo Docker — recomendado)

Requer apenas Docker. Um único comando sobe tudo:

```bash
docker compose up --build
```

Três containers sobem juntos:

| Serviço    | Porta host | Descrição                          |
|------------|------------|------------------------------------|
| `app`      | 8080       | Aplicação Spring Boot              |
| `wiremock` | 8081       | Mock da API de CEP                 |
| `postgres` | 5432       | Banco relacional dos logs          |

Credenciais do Postgres: user `postgres` / pass `postgres` / db `cepdb`.

Para parar e limpar os volumes:

```bash
docker compose down -v
```

## Como executar (modo local — sem Docker para o app)

Perfil padrão usa **H2 em memória** — nenhum banco precisa ser instalado.
Só o WireMock precisa estar rodando:

```bash
# 1) Subir apenas o WireMock
docker run -d --rm --name cep-wiremock -p 8081:8080 \
  -v "$(pwd)/wiremock/mappings:/home/wiremock/mappings:ro" \
  wiremock/wiremock:3.6.0

# 2) Subir a aplicação
./mvnw spring-boot:run
```

Console do H2: http://localhost:8080/h2-console (JDBC `jdbc:h2:mem:cepdb`, user `sa`, sem senha).

## Endpoints

| Método | Rota                           | Descrição                                |
|--------|--------------------------------|------------------------------------------|
| GET    | `/api/cep/{cep}`               | Consulta um CEP                          |
| GET    | `/api/cep/logs`                | Lista todos os logs de consulta          |
| GET    | `/api/cep/logs/{cep}`          | Lista logs de um CEP específico          |
| GET    | `/swagger-ui.html`             | Documentação interativa (Swagger UI)     |
| GET    | `/v3/api-docs`                 | OpenAPI JSON                             |
| GET    | `/actuator/health`             | Healthcheck (liveness/readiness)         |
| GET    | `/actuator/info`               | Metadados da aplicação                   |
| GET    | `/actuator/metrics`            | Métricas de runtime                      |

### Exemplos com curl

```bash
# CEP válido mocado
curl http://localhost:8080/api/cep/01310-100

# CEP com formato inválido -> 400
curl -i http://localhost:8080/api/cep/abc

# CEP inexistente -> 404
curl -i http://localhost:8080/api/cep/99999999

# Listagem dos logs persistidos
curl http://localhost:8080/api/cep/logs
```

Todo o script de demonstração dos cenários está em `./demo.sh`.

## Arquitetura Hexagonal (Ports & Adapters)

O projeto segue o padrão **Ports & Adapters** para desacoplar o núcleo de
negócio da infraestrutura. As dependências apontam sempre **para dentro** —
nada em `domain` conhece Spring, JPA ou HTTP.

```
src/main/java/com/example/cep/
├── domain/              ← NÚCLEO (zero framework)
│   ├── model/           entidades de negócio (records)
│   ├── port/            interfaces (portas)
│   └── exception/       exceções de negócio
├── application/         ← CASOS DE USO
│   ├── ConsultarCepUseCase.java
│   └── ListarLogsUseCase.java
└── infrastructure/      ← ADAPTADORES
    ├── web/             adaptador de entrada HTTP (Controller, DTOs)
    ├── client/          adaptador de saída HTTP (ViaCepClient)
    ├── persistence/     adaptador de saída JPA (Entity, Repository, Adapter)
    └── config/          configurações Spring (RestTemplate, OpenAPI)
```

**Portas** (interfaces em `domain.port`):
- `CepProvider` — consulta um CEP em um provedor externo
- `ConsultaLogRepository` — persiste logs de consulta

**Adaptadores** (em `infrastructure`):
- `ViaCepClient` — implementação HTTP da `CepProvider` (aponta para WireMock em dev)
- `ConsultaLogRepositoryAdapter` — implementação JPA da `ConsultaLogRepository`
- `CepController` — adaptador de entrada que traduz HTTP em chamadas ao caso de uso

### Ganhos práticos

- Trocar o provedor de CEP (ViaCEP real, cache Redis, fallback multi-provedor) é **criar um novo adaptador** e injetá-lo no lugar do atual — nenhuma linha de código de aplicação ou domínio muda.
- Trocar de JPA para MongoDB é trocar o adaptador de persistência — o caso de uso continua idêntico.
- Testes unitários do caso de uso **não sobem Spring**: substituem as portas por mocks do Mockito (ver `ConsultarCepUseCaseTest`).

Ver também [`docs/architecture.md`](docs/architecture.md) (diagrama) e
[`docs/SOLID.md`](docs/SOLID.md) (mapeamento dos 5 princípios para classes
concretas).

## Documentação OpenAPI (Swagger)

Com a aplicação rodando:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

A documentação é gerada automaticamente pelo Springdoc a partir das anotações
`@Operation`, `@ApiResponse` e `@Schema` no controller e DTOs. É possível
testar os endpoints diretamente pela UI.

## Observabilidade (Actuator)

- `GET /actuator/health` — healthcheck, usado tipicamente como `livenessProbe`/`readinessProbe` em Kubernetes
- `GET /actuator/info` — metadados da aplicação (nome, versão, descrição)
- `GET /actuator/metrics` — métricas de runtime (JVM, HTTP, datasource)

## CEPs disponíveis no mock

| CEP        | Endereço                                   |
|------------|--------------------------------------------|
| `01310100` | Avenida Paulista, Bela Vista — São Paulo   |
| `04567000` | Av. das Nações Unidas, Vila Olímpia — SP   |
| `22070011` | Avenida Atlântica, Copacabana — RJ         |

Qualquer outro CEP válido (8 dígitos) cai no mapeamento *catch-all* e retorna
`{"erro": true}` — exatamente o comportamento do ViaCEP real.

## Rodar os testes

```bash
./mvnw test
```

O `ConsultarCepUseCaseTest` cobre os três fluxos principais (sucesso, CEP
inválido, CEP não encontrado, erro do provedor, sanitização de máscara) sem
subir contexto Spring — é o ganho prático da arquitetura hexagonal.

## CI/CD

O pipeline do GitHub Actions (`.github/workflows/ci.yml`) executa a cada push:

1. Build e testes com `mvn clean verify`
2. Publica relatórios de teste como artefato
3. Valida o build da imagem Docker

## Como inspecionar os logs persistidos

### Via endpoint da aplicação
```bash
curl http://localhost:8080/api/cep/logs
```

### Via psql no container Postgres
```bash
docker exec -it cep-postgres psql -U postgres -d cepdb
```
```sql
SELECT id, cep, data_hora_consulta, status FROM consulta_log ORDER BY id;
```

### Via DBeaver ou outra ferramenta gráfica
Host `localhost`, porta `5432`, database `cepdb`, user/pass `postgres`/`postgres`.

## Trocar do mock para o ViaCEP real

Basta apontar a URL base para `https://viacep.com.br`. O contrato JSON é
idêntico — nenhuma mudança de código é necessária.

```yaml
cep:
  provider:
    base-url: https://viacep.com.br
```

Essa flexibilidade é justamente o ganho prático de aplicar o **DIP** do SOLID:
o serviço depende da interface, não da implementação concreta.
