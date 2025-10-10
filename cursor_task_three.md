# cursor_task_three.md

## 🎯 Objetivo do arquivo

Guiar a revisão do que já foi implementado (Customer, Utils, Repositories, Liquibase, Docker/props) e conduzir a próxima
leva de entregas (User, Autenticação, DTOs, Services, Controllers e adapters dos gateways), sempre:

- explicando conceitos essenciais,
- apontando melhorias e riscos,
- validando se o código está correto, sem vícios ou code-smells,
- **sem gerar código pronto**.

---

## 1) Revisão obrigatória do que já existe ✅

### 1.1 Entidade `Customer`

- `cpf` e `cnpj` como strings numéricas normalizadas; regra “pelo menos um válido”.
- Uso de `@PrePersist/@PreUpdate` chamando normalizadores (email, estado, país, nomes, zip, phone).
- Unicidade em `email`, `cpf`, `cnpj` compatível com o schema.
- Validações de documento concentradas no domínio, não duplicadas no Service.
- **Conceito:** invariantes de negócio pertencem ao domínio para evitar inconsistências entre camadas.

### 1.2 Utils (Customer/DataNormalizer/Payment/Subscription/Refund/Idempotency/Webhook)

- Funções puras, sem efeitos colaterais.
- Nomes claros e determinísticos.
- Limites e formatos compatíveis com o banco (ex.: email ≤ 254, state = 2, zip = 8).
- **Conceito:** utils devem ser stateless, previsíveis e isolados da camada de infraestrutura.

### 1.3 Liquibase e Docker

- `spring.liquibase.enabled=true` nos ambientes locais e dockerizados.
- Scripts `001-init-tables.yml` e `002-migrate-customer-and-sizes.yml` aplicando sem erros.
- Dockerfile multi-stage build (gradle + temurin-21) funcional.
- docker-compose subindo corretamente `db`, `rabbitmq`, `sonarqube`.
- **Conceito:** o build deve ser imutável e reproduzível, independente do ambiente local.

### 1.4 Repositórios

- Métodos seguem padrões do Spring Data.
- Consultas customizadas (`@Query`) corretas e performáticas.
- Sem redundância com métodos derivados.
- `CustomerRepository` atualizado (sem `DocumentType`/`document`).
- **Conceito:** a camada de repositório não deve conter lógica de negócio, apenas persistência.

---

## 2) Próximos passos (a serem acompanhados pelo Cursor)

### 2.1 Autenticação e Entidade `User` (🚨 deve vir antes dos DTOs)

- Criar a entidade `User` e configurar autenticação JWT.
- O Cursor deve:
    - Explicar o papel de `User` e `UserRole` dentro do domínio.
    - Ensinar o fluxo de autenticação (registro, login, JWT, refresh).
    - Validar o relacionamento entre `User` e `Customer` (`@OneToMany` / `@ManyToOne`).
    - Garantir que `SecurityConfig`, `PasswordEncoder` e filtros JWT estão corretos.
    - Explicar boas práticas de segurança (CSRF, CORS, roles e authorities).
    - Confirmar que o `User` é usado apenas para autenticação e não mistura responsabilidades de negócio.

- **Conceito:** a segurança deve ser implementada antes dos DTOs para evitar retrabalho e garantir contexto de usuário
  em toda a aplicação.

---

### 2.2 DTOs (após autenticação)

- Criar DTOs para cada entidade (Customer, Payment, Subscription, Refund, User, Auth).
- O Cursor deve:
    - Ajudar a definir **Request/Response DTOs** coerentes com o domínio.
    - Explicar diferença entre **entidade JPA** e **DTO**.
    - Ensinar boas práticas de imutabilidade e mapeamento (ex.: MapStruct, record classes).
    - Validar se os DTOs não expõem dados sensíveis nem dependem de autenticação direta (o contexto vem do token JWT).

---

### 2.3 Services

- Criar camada de serviço para cada agregado.
- O Cursor deve:
    - Explicar o papel do service (coordenar regras de negócio, não acessar DB direto).
    - Validar se não há duplicação de validações já feitas na entidade.
    - Ensinar quando usar `@Transactional` e o impacto no commit.
    - Ensinar quando e como lançar exceções customizadas (boas práticas em `exception/`).

---

### 2.4 Controllers

- Criar endpoints REST seguindo RESTful padrão.
- O Cursor deve:
    - Ensinar versionamento de rotas (`/api/v1/...`).
    - Ajudar a mapear rotas com `@RestController` e `@RequestMapping` adequados.
    - Ensinar tratamento global de erros com `@ControllerAdvice`.
    - Verificar boas práticas de validação (`@Valid`, `@RequestBody`).
    - Validar integração com a camada de segurança (ex.: `@PreAuthorize`).

---

### 2.5 Integração com SDKs (Asaas, Stripe, Mercado Pago)

- Criar camada `adapter` para comunicação com SDKs.
- O Cursor deve:
    - Ensinar segregação de responsabilidades (domain-driven adapter pattern).
    - Garantir que as regras de fallback (ordem de gateways) fiquem centralizadas.
    - Explicar como mapear DTO interno ↔ DTO SDK sem acoplamento.
    - Validar se cada cliente SDK tem logs, timeouts e erros tratados adequadamente.

---

### 2.6 Documentação e Testes

- O Cursor deve:
    - Ensinar a gerar documentação automática com Swagger/OpenAPI.
    - Ensinar como criar testes unitários e de integração (JUnit + Testcontainers).
    - Validar se os testes respeitam isolamento e idempotência.
    - Corrigir code smells de teste (mocks excessivos, asserts soltos, sem padrão AAA).

---

## 3) Diretrizes de revisão contínua

- Toda análise deve começar lendo a entidade e o repositório correspondente.
- Sempre validar se:
    - o código segue convenções idiomáticas do Java e Spring Boot;
    - há normalização de dados antes de persistir;
    - há integridade entre o schema Liquibase e as entidades JPA;
    - não há duplicação entre validação de domínio e camada de serviço.
- Quando houver dúvida, o Cursor deve **explicar o porquê**, não apenas corrigir.

---

## 4) Critérios para sugerir melhorias

- Só sugerir refatorações quando houver ganho real em legibilidade, consistência ou performance.
- Evitar “micro-otimizações” desnecessárias.
- Dar contexto técnico: por exemplo, explicar por que `@Enumerated(EnumType.STRING)` é mais seguro do que o
  padrão `ORDINAL`.
- Indicar documentação de referência (Spring, JPA, Clean Architecture).

---

## 5) Meta

O objetivo deste arquivo é tornar o Cursor um **revisor técnico e mentor ativo** do projeto ConectaAI Gateway,  
ensinando e guiando as próximas etapas de desenvolvimento — **sem escrever código por conta própria**,  
mantendo o foco em arquitetura limpa, segurança e boas práticas de desenvolvimento.
