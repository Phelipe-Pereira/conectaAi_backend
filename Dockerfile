# ---- Etapa 1: Build do jar (sem testes) ----
FROM gradle:8.7.0-jdk21 AS builder

WORKDIR /app

# Copia arquivos de configuração do Gradle primeiro (para cache)
COPY build.gradle settings.gradle gradlew ./
COPY gradle/ ./gradle/

# Download dependencies (cache layer)
RUN ./gradlew dependencies --no-daemon || true

# Copia código fonte
COPY src/ ./src/

# Build da aplicação
RUN ./gradlew clean bootJar -x test --no-daemon

# ---- Etapa 2: Imagem final ----
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copia o jar criado no passo anterior
COPY --from=builder /app/build/libs/*.jar app.jar

# Porta HTTP da sua API
EXPOSE 8080

# Ativa o perfil docker e liga Liquibase automaticamente
ENV SPRING_PROFILES_ACTIVE=docker

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
