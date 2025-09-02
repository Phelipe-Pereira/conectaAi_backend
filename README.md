# 🚀 ConectaAI Gateway

**Middleware unificado para múltiplos gateways de pagamento**

---

## 📋 Visão Geral

O ConectaAI Gateway é um sistema middleware que unifica a integração com múltiplos provedores de pagamento, oferecendo uma API consistente para processamento de pagamentos, assinaturas e webhooks.

### 🎯 Objetivo
Simplificar a integração com diferentes gateways de pagamento através de uma única interface, abstraindo as particularidades de cada provedor.

---

## 🏗️ Arquitetura

### **Stack Tecnológica**
- **Java 20** - Linguagem principal
- **Spring Boot 3.5.5** - Framework web
- **PostgreSQL 15** - Banco de dados principal
- **RabbitMQ** - Mensageria assíncrona
- **Liquibase** - Controle de versão do banco
- **Docker** - Containerização

### **Gateways Integrados**
- **Asaas** (Brasil) - SDK 1.0.3
- **Stripe** (Internacional) - SDK 24.0.0
- **Mercado Pago** (América Latina) - SDK 2.0.0

---

## 🗄️ Modelo de Dados

### **Entidades Principais**
- **Customer** - Gerenciamento de clientes
- **Payment** - Processamento de pagamentos
- **Subscription** - Assinaturas recorrentes
- **Refund** - Estornos e reembolsos
- **WebhookEvent** - Eventos dos gateways
- **Idempotency** - Controle de duplicação

### **Relacionamentos**
```
Customer (1:N) Payment (1:N) Refund
Customer (1:N) Subscription
```

---

## 🚀 Configuração do Ambiente

### **Pré-requisitos**
- Java 20+
- Docker & Docker Compose
- PostgreSQL 15
- RabbitMQ

### **Configuração Local**

1. **Clone o repositório**
```bash
git clone <repository-url>
cd gateway
```

2. **Configure as variáveis de ambiente**
```bash
# Copie o arquivo de exemplo
cp .env.example .env

# Configure seus tokens dos gateways
ASAAS_TOKEN=seu_token_asaas
STRIPE_SECRET=sua_chave_stripe
MP_ACCESS_TOKEN=seu_token_mercadopago
```

3. **Inicie os serviços com Docker**
```bash
docker-compose up -d
```

4. **Execute a aplicação**
```bash
./gradlew bootRun
```

### **Portas dos Serviços**
- **API**: http://localhost:8080
- **PostgreSQL**: localhost:5433
- **RabbitMQ Management**: http://localhost:15672
- **Swagger UI**: http://localhost:8080/swagger-ui.html

---

## 📊 Status do Desenvolvimento

### ✅ **Concluído**
- [x] Configuração base do Spring Boot
- [x] Schema do banco de dados (Liquibase)
- [x] Entidades JPA completas
- [x] Enums do domínio
- [x] Configuração Docker

### 🔄 **Em Desenvolvimento**
- [ ] Repositórios Spring Data JPA
- [ ] Services de integração com gateways
- [ ] Controllers REST
- [ ] Processamento de webhooks

### ⏳ **Planejado**
- [ ] Autenticação JWT
- [ ] Documentação OpenAPI
- [ ] Testes automatizados
- [ ] Pipeline CI/CD

---

## 🔧 Configurações

### **Profiles de Ambiente**

#### **Local (application.properties)**
```properties
spring.profiles.active=local
spring.liquibase.enabled=false
spring.jpa.hibernate.ddl-auto=create-drop
```

#### **Docker (application-docker.properties)**
```properties
spring.profiles.active=docker
spring.liquibase.enabled=true
spring.jpa.hibernate.ddl-auto=validate
```

### **Tokens dos Gateways**
Os tokens são configurados via variáveis de ambiente para maior segurança:
- `ASAAS_TOKEN` - Token de API do Asaas
- `STRIPE_SECRET` - Chave secreta do Stripe  
- `MP_ACCESS_TOKEN` - Token de acesso do Mercado Pago

---

## 🧪 Testes

### **Executar Testes**
```bash
# Todos os testes
./gradlew test

# Testes específicos
./gradlew test --tests CustomerServiceTest
```

### **Cobertura de Código**
```bash
./gradlew jacocoTestReport
```

---

## 📝 API Documentation

### **Swagger/OpenAPI**
Acesse a documentação interativa em: http://localhost:8080/swagger-ui.html

### **Endpoints Principais**
- `POST /api/v1/customers` - Criar cliente
- `POST /api/v1/payments` - Processar pagamento
- `POST /api/v1/subscriptions` - Criar assinatura
- `POST /webhooks/{provider}` - Receber webhooks

---

## 🔐 Segurança

### **Autenticação**
- JWT tokens para autenticação de API
- Validação de assinatura para webhooks
- Rate limiting por endpoint

### **Validações**
- Bean Validation em todas as entradas
- Sanitização de dados
- Controle de idempotência

---

## 🐳 Deploy

### **Build da Imagem**
```bash
docker build -t conectaai-gateway .
```

### **Deploy Local**
```bash
docker-compose up --build
```

### **Deploy Produção**
```bash
# Configure as variáveis de ambiente de produção
docker run -d \
  -e SPRING_PROFILES_ACTIVE=production \
  -e DATABASE_URL=... \
  -e ASAAS_TOKEN=... \
  -p 8080:8080 \
  conectaai-gateway
```

---

## 📊 Monitoramento

### **Health Checks**
- `GET /actuator/health` - Status da aplicação
- `GET /actuator/metrics` - Métricas de performance

### **Logs**
```bash
# Acompanhar logs em tempo real
docker-compose logs -f app
```

---

## 🤝 Desenvolvimento

### **Estrutura do Projeto**
```
src/
├── main/
│   ├── java/com/conectaai/
│   │   ├── domain/          # Entidades JPA
│   │   ├── enums/           # Enums do domínio
│   │   ├── repository/      # Repositórios
│   │   ├── service/         # Lógica de negócio
│   │   └── controller/      # Controllers REST
│   └── resources/
│       ├── application.properties
│       └── db/changelog/    # Migrations Liquibase
└── test/                    # Testes automatizados
```

### **Padrões de Código**
- **Conventional Commits** para mensagens
- **Clean Code** principles
- **SOLID** principles
- **Domain-Driven Design** (DDD)

---

## 📞 Suporte

### **Contatos da Equipe**
- **Tech Lead**: [nome@empresa.com]
- **DevOps**: [devops@empresa.com]

### **Links Úteis**
- **Documentação dos Gateways**:
  - [Asaas API Docs](https://docs.asaas.com)
  - [Stripe API Docs](https://stripe.com/docs/api)
  - [Mercado Pago Docs](https://www.mercadopago.com.br/developers)

---

## 📄 Licença

Este projeto é **privado** e proprietário da empresa. Todos os direitos reservados.

---

*Última atualização: Dezembro 2024*
