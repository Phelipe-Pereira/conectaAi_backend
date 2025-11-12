# Ferramentas de Qualidade de Código

## Ferramentas Configuradas

### 1. Testes e Cobertura

#### JUnit
- Framework de testes padrão do Spring Boot
- Execução: `./gradlew test`
- Relatórios: `build/reports/tests/test/index.html`

#### JaCoCo
- Análise de cobertura de código
- Cobertura mínima: 70%
- Execução: `./gradlew jacocoTestReport`
- Relatórios:
  - HTML: `build/jacocoHtml/index.html`
  - XML: `build/reports/jacoco/test/jacocoTestReport.xml`

### 2. Qualidade de Código

#### Checkstyle
- Análise de estilo de código
- Configuração: `config/checkstyle/checkstyle.xml`
- Execução: `./gradlew checkstyleMain checkstyleTest`
- Relatórios: `build/reports/checkstyle/main.html`

#### SonarQube
- Análise estática completa de código
- Integração com JaCoCo e Checkstyle
- Execução: `./gradlew sonar`
- Servidor: http://localhost:9000 (via Docker)

### 3. Segurança

#### SpotBugs (com FindSecBugs)
- Detecção de bugs e vulnerabilidades de segurança
- Plugin FindSecBugs para análise de segurança
- Configuração: `config/spotbugs/exclude.xml`
- Execução: `./gradlew spotbugsMain spotbugsTest`
- Relatórios:
  - HTML: `build/reports/spotbugs/main.html`
  - XML: `build/reports/spotbugs/main.xml`

#### OWASP Dependency Check
- Análise de vulnerabilidades em dependências
- CVSS mínimo para falhar build: 7.0
- Configuração: `config/owasp/suppressions.xml`
- Execução: `./gradlew dependencyCheckAnalyze`
- Relatórios:
  - HTML: `build/reports/dependency-check-report.html`
  - JSON: `build/reports/dependency-check-report.json`
  - JUnit: `build/reports/dependency-check-junit.xml`

## Comandos Úteis

```bash
# Executar todas as verificações de qualidade
./gradlew check

# Executar testes e cobertura
./gradlew test jacocoTestReport

# Executar análise de segurança
./gradlew spotbugsMain dependencyCheckAnalyze

# Executar análise completa (incluindo SonarQube)
./gradlew clean test jacocoTestReport checkstyleMain spotbugsMain dependencyCheckAnalyze sonar

# Ver relatórios
# - Testes: build/reports/tests/test/index.html
# - Cobertura: build/jacocoHtml/index.html
# - Checkstyle: build/reports/checkstyle/main.html
# - SpotBugs: build/reports/spotbugs/main.html
# - OWASP: build/reports/dependency-check-report.html
```

## Integração CI/CD

Todas as ferramentas estão integradas na task `check` do Gradle, que é executada automaticamente em pipelines CI/CD.

