#!/bin/bash

set -e

echo "🧹 Limpando projeto completamente..."

echo "📦 Parando e removendo containers Docker..."
docker-compose down -v --remove-orphans

echo "🗑️  Removendo imagens Docker..."
docker-compose rm -f

echo "🧼 Limpando volumes Docker..."
docker volume prune -f

echo "📂 Limpando build do Gradle..."
rm -rf .gradle
rm -rf build
rm -rf bin

echo "🗂️  Limpando arquivos temporários..."
find . -name "*.class" -type f -delete
find . -name "*.log" -type f -delete
find . -name ".DS_Store" -type f -delete

echo "🔨 Limpando cache do Docker (opcional)..."
read -p "Deseja limpar cache do Docker? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    docker system prune -a -f --volumes
fi

echo "✅ Limpeza concluída!"
echo ""
echo "🚀 Iniciando build e subida do projeto..."

echo "📦 Construindo e subindo containers..."
docker-compose up --build -d

echo "⏳ Aguardando serviços iniciarem..."
sleep 10

echo "🧪 Executando testes e build..."
docker-compose exec -T app ./gradlew clean build test --no-daemon || {
    echo "❌ Build ou testes falharam. Verificando logs..."
    docker-compose logs app
    exit 1
}

echo "✅ Projeto limpo, reconstruído e testado com sucesso!"
echo ""
echo "📊 Status dos containers:"
docker-compose ps

