# ConectaAI Gateway

Middleware unificado para múltiplos gateways de pagamento. Uma única API para processar cobranças, assinaturas e webhooks no Asaas, Stripe e Mercado Pago — sem reescrever código para cada provedor.

---

## O problema que resolve

Empresas que precisam aceitar pagamentos em diferentes provedores enfrentam um problema real: cada gateway tem sua própria API, seus próprios eventos de webhook, seus próprios status de transação. Trocar de provedor ou suportar mais de um ao mesmo tempo significa duplicar lógica de negócio e manter integrações incompatíveis em paralelo.

O ConectaAI resolve isso com uma camada de abstração: a aplicação que consome a API não sabe — nem precisa saber — qual gateway está por baixo.

---

## Decisões técnicas

### Por que Adapter Pattern?

Cada gateway tem comportamentos muito diferentes. O Asaas retorna datas em `dd/MM/yyyy`, o Stripe em Unix timestamp, o Mercado Pago em ISO 8601. O Asaas usa `externalReference` para referenciar a cobrança, o Stripe usa `metadata.external_id`, o Mercado Pago usa `external_reference`. Normalizar esses dados no controller ou no service criaria um switch gigante e quebraria toda vez que um gateway mudasse o contrato.

O Adapter isola completamente essa variação. O `PaymentService` chama `adapter.createPayment(...)` sem saber quem responde. A `PaymentGatewayAdapterFactory` decide qual implementação usar com base no `Provider` do request. Adicionar um quarto gateway é implementar a interface e registrar na factory — nada mais.

### Por que RabbitMQ para webhooks?

Webhooks chegam de forma imprevisível, em rajadas, e o provedor espera uma resposta rápida (200 OK) para não reenviar. Processar o webhook de forma síncrona no endpoint — atualizar banco, disparar notificações, recalcular status — cria risco real de timeout e reprocessamento duplicado.

O fluxo implementado é: receber → validar assinatura → persistir o evento raw → publicar na fila → retornar 200. O consumer processa de forma assíncrona e usa controle de idempotência para garantir que o mesmo evento não seja processado duas vezes, mesmo que o gateway reenvie.

As filas têm Dead Letter Queue (DLQ) configurada: eventos que falham repetidamente não se perdem, ficam na DLQ para reprocessamento manual ou investigação.

### Por que Liquibase?

Controle de versão do schema. Cada migration é um arquivo versionado e auditado — dá para ver exatamente quando cada tabela foi criada, qual campo foi adicionado e por quê. Em ambiente de equipe ou CI/CD isso é essencial. Também permite rollback controlado se uma migration causar problema.

---

## Arquitetura

```
                    ┌─────────────────────────────────────────┐
                    │           ConectaAI Gateway             │
                    │                                         │
  HTTP Request  ──► │  Controller  ──►  Service  ──►  Adapter │ ──► Asaas
                    │                      │                  │ ──► Stripe
  Webhook       ──► │  WebhookController   │        Factory   │ ──► MercadoPago
                    │       │              │                  │
                    │       ▼              ▼                  │
                    │   RabbitMQ  ◄──  Publisher              │
                    │       │                                 │
                    │       ▼                                 │
                    │   Consumer  ──►  PaymentService         │
                    └─────────────────────────────────────────┘
                                         │
                                    PostgreSQL
```

### Camadas

| Camada | Responsabilidade |
|---|---|
| `controller/` | Recebe requisições HTTP, valida entrada, delega para service |
| `service/` | Regra de negócio: validações, orquestração, persistência |
| `adapter/` | Tradução entre o domínio interno e cada gateway externo |
| `consumer/` | Processamento assíncrono de webhooks via RabbitMQ |
| `domain/` | Entidades JPA — Payment, Customer, Subscription, Refund |
| `dto/` | Objetos de entrada/saída da API, com Mappers dedicados |
| `exception/` | Exceções de domínio + `GlobalExceptionHandler` centralizado |
| `security/` | Filtros JWT e API Key, Spring Security stateless |
| `specification/` | Filtros dinâmicos de busca com JPA Specification |

---

## Stack

- **Java 21** + **Spring Boot 3.5.5**
- **PostgreSQL 15** com **Liquibase** (17 migrations)
- **RabbitMQ 3.13** com DLQ configurada
- **Spring Security** — autenticação dupla: JWT + API Key
- **Gateways**: Asaas SDK 1.0.3 · Stripe SDK 24.0.0 · MercadoPago SDK 2.0.0
- **Qualidade**: JaCoCo (cobertura mínima 70%) · Checkstyle · SpotBugs + FindSecBugs · SonarQube

---

## Funcionalidades implementadas

- **Clientes** — CRUD com sincronização automática no gateway selecionado
- **Pagamentos** — criação, cancelamento, atualização de status, busca com filtros dinâmicos (valor, status, provedor, data)
- **Assinaturas** — criação e gerenciamento de cobranças recorrentes
- **Reembolsos** — estorno total e parcial com validação de status
- **Webhooks recebidos** — Asaas, Stripe e MercadoPago: validação de assinatura, deduplicação por evento, processamento assíncrono via fila
- **Autenticação** — registro, login, refresh token, reset de senha, troca de senha
- **API Keys** — geração e gestão de chaves para acesso programático

---

## Rodando localmente

### Pré-requisitos

- Java 21+
- Docker e Docker Compose

### Setup

```bash
# 1. Clone o repositório
git clone https://github.com/Phelipe-Pereira/conectaAi_backend.git
cd conectaAi_backend

# 2. Crie o arquivo de variáveis de ambiente
cp .env.example .env
```

Edite o `.env` com suas credenciais:

```env
ASAAS_TOKEN=seu_token_sandbox_asaas
STRIPE_SECRET=sk_test_sua_chave_stripe
MP_ACCESS_TOKEN=seu_token_mercadopago

JWT_SECRET=gere_uma_string_aleatoria_de_64_chars
```

> Para gerar um JWT secret seguro: `openssl rand -base64 64`

```bash
# 3. Sobe PostgreSQL e RabbitMQ
docker-compose up -d app-db rabbitmq

# 4. Roda a aplicação (Liquibase executa as migrations automaticamente)
./gradlew bootRun
```

### Serviços disponíveis

| Serviço | URL |
|---|---|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| RabbitMQ Management | http://localhost:15672 (guest/guest) |
| PostgreSQL | localhost:5433 |

---

## Testes e qualidade

```bash
# Rodar testes
./gradlew test

# Relatório de cobertura (gerado em build/jacocoHtml/)
./gradlew jacocoTestReport

# Análise estática completa
./gradlew check
```

A configuração exige **70% de cobertura mínima** para o build passar (`jacocoTestCoverageVerification`).

---

## Deploy com Docker

```bash
# Build da imagem
docker build -t conectaai-gateway .

# Subir toda a stack
docker-compose up --build
```

O `docker-compose.yml` inclui app, PostgreSQL e RabbitMQ prontos para uso.

---

## Endpoints principais

```
POST   /api/v1/auth/register
POST   /api/v1/auth/login
POST   /api/v1/auth/refresh

GET    /api/v1/customers
POST   /api/v1/customers
GET    /api/v1/customers/{id}

GET    /api/v1/payments
POST   /api/v1/payments
GET    /api/v1/payments/{id}
PUT    /api/v1/payments/{id}/status
DELETE /api/v1/payments/{id}

POST   /api/v1/subscriptions
GET    /api/v1/subscriptions/{id}

POST   /api/v1/refunds
GET    /api/v1/refunds/{id}

POST   /api/v1/webhooks/{provider}    (ASAAS | STRIPE | MERCADO_PAGO)
```

Documentação completa e interativa disponível no Swagger UI após subir a aplicação.

---

## Variáveis de ambiente

| Variável | Descrição |
|---|---|
| `ASAAS_TOKEN` | Token de API do Asaas (sandbox ou produção) |
| `STRIPE_SECRET` | Chave secreta do Stripe (`sk_test_...` ou `sk_live_...`) |
| `MP_ACCESS_TOKEN` | Token de acesso do Mercado Pago |
| `JWT_SECRET` | String aleatória usada para assinar tokens JWT (mín. 64 chars) |
| `SPRING_DATASOURCE_URL` | URL do PostgreSQL (padrão: `jdbc:postgresql://localhost:5433/conectaai`) |
| `SPRING_RABBITMQ_HOST` | Host do RabbitMQ (padrão: `localhost`) |