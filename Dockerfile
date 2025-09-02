# ---- Etapa 1: Build do jar (sem testes) ----
FROM gradle:8.7.0-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle clean bootJar -x test

# ---- Etapa 2: Imagem final ----
FROM eclipse-temurin:21-jre
WORKDIR /app
# Copia o jar criado no passo anterior
COPY --from=builder /app/build/libs/*.jar app.jar

# Porta HTTP da sua API
EXPOSE 8080

# Ativa o perfil docker (opcional) e liga Liquibase automaticamente
ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-jar", "app.jar"]
