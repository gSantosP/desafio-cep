# Aplicação do SOLID no projeto

Este documento mapeia cada um dos cinco princípios SOLID para trechos concretos
do código. Na arquitetura hexagonal, o SOLID fica especialmente evidente
porque a própria estrutura de pacotes já expressa as decisões.

## S — Single Responsibility Principle

Cada classe tem **uma única razão para mudar**:

- `CepController` — apenas traduz HTTP em chamadas ao caso de uso.
- `ConsultarCepUseCase` — apenas orquestra o fluxo de consulta.
- `ListarLogsUseCase` — apenas expõe listagem de logs.
- `ViaCepClient` — apenas fala HTTP com o provedor externo.
- `ConsultaLogRepositoryAdapter` — apenas persiste via JPA.
- `GlobalExceptionHandler` — apenas traduz exceções em respostas HTTP.
- `ConsultaLogEntity` — representação JPA; separada de `ConsultaLog` (domínio)
  para que mudanças na persistência não contaminem o núcleo.

## O — Open/Closed Principle

O sistema está **aberto para extensão, fechado para modificação**:

- Para suportar um novo provedor de CEP (Correios, cache Redis, fallback
  encadeado), basta criar uma nova classe que implemente `CepProvider` e
  marcá-la como `@Primary` ou usar `@Qualifier`. Nenhuma linha do
  `ConsultarCepUseCase` precisa mudar.
- Para registrar logs em outra tecnologia (Mongo, Kafka, arquivo), basta
  criar outra implementação de `ConsultaLogRepository` — mesma ideia.

O próprio adaptador `ViaCepClient` é uma prova do princípio: ele já permite
trocar de WireMock para ViaCEP real alterando apenas a propriedade
`cep.provider.base-url`, sem recompilar.

## L — Liskov Substitution Principle

`ViaCepClient` é totalmente substituível por qualquer outra implementação de
`CepProvider`. O caso de uso foi desenhado para depender apenas do contrato
público da interface, sem assumir nada específico da implementação.

Isso é demonstrado nos testes: em `ConsultarCepUseCaseTest` o provedor real é
substituído por um mock do Mockito e todo o fluxo roda normalmente — prova de
que a substituição respeita o contrato.

## I — Interface Segregation Principle

As interfaces no pacote `domain.port` são intencionalmente pequenas:

- `CepProvider` expõe um único método, `buscar(String)`.
- `ConsultaLogRepository` expõe apenas as três operações que o domínio
  realmente precisa (`salvar`, `listarTodos`, `buscarPorCep`), não todas as
  dezenas de métodos herdados do `JpaRepository`.

Classes que implementam essas portas não são forçadas a oferecer
comportamentos que não usam. Se no futuro precisarmos de busca reversa
(endereço → CEP), criaremos uma **nova interface** (`CepReverseProvider`) ao
invés de inflar a existente.

## D — Dependency Inversion Principle

Módulos de alto nível dependem de **abstrações**, não de implementações
concretas. É o princípio mais visível na arquitetura hexagonal:

```
domain/port/CepProvider            ← abstração (no domínio)
          ▲
          │ implementa
          │
infrastructure/client/ViaCepClient ← detalhe (fora do domínio)
```

O caso de uso `ConsultarCepUseCase` nunca faz `new ViaCepClient()`. O container
Spring entrega a implementação apropriada pelo construtor, e em testes
entregamos um mock — a classe não muda.

Uma forma simples de verificar: **nenhum arquivo em `com.example.cep.domain` ou
`com.example.cep.application` faz import de `com.example.cep.infrastructure`**.
As setas de dependência apontam todas para dentro.

## Resumo visual

```
┌─────────────────────────────────────────────────────────┐
│  infrastructure/                                        │
│   web.CepController ──────┐                             │
│   client.ViaCepClient ◄─┐ │ ┌───► persistence.Adapter   │
└─────────────────────────┼─┼─┼─────────────────────────┬─┘
                          │ │ │                         │
┌─────────────────────────┼─┼─┼─────────────────────────┼─┐
│  application/           │ │ │                         │ │
│   ConsultarCepUseCase ──┼─┼─┘                         │ │
│                         │ │                           │ │
└─────────────────────────┼─┼───────────────────────────┼─┘
                          │ │                           │
┌─────────────────────────┼─┼───────────────────────────┼─┐
│  domain/                │ │                           │ │
│   port.CepProvider ◄────┘ │                           │ │
│   port.ConsultaLogRepository ◄────────────────────────┘ │
│   model.Endereco, ConsultaLog, StatusConsulta           │
│   exception.*                                           │
└─────────────────────────────────────────────────────────┘
```

Todas as setas apontam para **abstrações** (Controller → UseCase → Interface),
nunca para detalhes — exatamente o que o DIP prega.
