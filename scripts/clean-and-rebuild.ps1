# Script PowerShell para limpar e reconstruir o projeto ConectaAI Gateway
# Este script faz uma limpeza completa e reconstrói tudo do zero

Write-Host "Limpando projeto completamente..." -ForegroundColor Cyan

# 1. Parar e remover todos os containers Docker
Write-Host "Parando e removendo containers Docker..." -ForegroundColor Yellow
docker-compose down -v --remove-orphans

# 2. Remover imagens Docker do projeto
Write-Host "Removendo imagens Docker..." -ForegroundColor Yellow
docker-compose rm -f

# 3. Limpar volumes Docker nao utilizados
Write-Host "Limpando volumes Docker..." -ForegroundColor Yellow
docker volume prune -f

# 4. Limpar cache e build do Gradle
Write-Host "Limpando build do Gradle..." -ForegroundColor Yellow
if (Test-Path .gradle) { Remove-Item -Recurse -Force .gradle }
if (Test-Path build) { Remove-Item -Recurse -Force build }
if (Test-Path bin) { Remove-Item -Recurse -Force bin }

# 5. Limpar arquivos temporarios
Write-Host "Limpando arquivos temporarios..." -ForegroundColor Yellow
Get-ChildItem -Recurse -Include "*.class" -ErrorAction SilentlyContinue | Remove-Item -Force
Get-ChildItem -Recurse -Include "*.log" -ErrorAction SilentlyContinue | Remove-Item -Force
Get-ChildItem -Recurse -Include ".DS_Store" -ErrorAction SilentlyContinue | Remove-Item -Force

# 6. Opcao para limpar cache do Docker (remove TODAS as imagens nao utilizadas)
$cleanCache = Read-Host "Deseja limpar cache do Docker? Isso removera TODAS as imagens nao utilizadas (y/N)"
if ($cleanCache -eq "y" -or $cleanCache -eq "Y") {
    Write-Host "Limpando cache do Docker..." -ForegroundColor Yellow
    docker system prune -a -f --volumes
}

Write-Host "Limpeza concluida!" -ForegroundColor Green
Write-Host ""

# 7. Reconstruir e subir containers
Write-Host "Iniciando build e subida do projeto..." -ForegroundColor Cyan

Write-Host "Construindo e subindo containers..." -ForegroundColor Yellow
docker-compose up --build -d

# 8. Aguardar servicos iniciarem (banco, rabbitmq, etc)
Write-Host "Aguardando servicos iniciarem (aguarde 15 segundos)..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

# 9. Executar build e testes dentro do container
Write-Host "Executando testes e build..." -ForegroundColor Yellow
docker-compose exec -T app ./gradlew clean build test --no-daemon

# 10. Verificar se build foi bem-sucedido
if ($LASTEXITCODE -ne 0) {
    Write-Host "Build ou testes falharam. Verificando logs..." -ForegroundColor Red
    docker-compose logs app
    exit 1
}

Write-Host "Projeto limpo, reconstruido e testado com sucesso!" -ForegroundColor Green
Write-Host ""
Write-Host "Status dos containers:" -ForegroundColor Cyan
docker-compose ps

Write-Host ""
Write-Host "Pronto! Seu projeto esta rodando em:" -ForegroundColor Green
Write-Host "   - API: http://localhost:8080" -ForegroundColor White
Write-Host "   - Swagger: http://localhost:8080/swagger-ui/index.html" -ForegroundColor White
Write-Host "   - RabbitMQ: http://localhost:15672 (guest/guest)" -ForegroundColor White
Write-Host "   - SonarQube: http://localhost:9000 (admin/Lipe@0811)" -ForegroundColor White
