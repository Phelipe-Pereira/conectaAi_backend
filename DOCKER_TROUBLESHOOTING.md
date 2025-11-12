# Troubleshooting Docker

## Problema: Erro 500 Internal Server Error do Docker Desktop

### Diagnóstico
O erro `request returned 500 Internal Server Error for API route and version` indica que o Docker Desktop está com problemas na API interna.

### Soluções (em ordem de prioridade)

#### 1. Reiniciar Docker Desktop
```powershell
# Fechar completamente o Docker Desktop
# - Clique com botão direito no ícone na bandeja do sistema
# - Selecione "Quit Docker Desktop"
# - Aguarde alguns segundos
# - Abra o Docker Desktop novamente
# - Aguarde até aparecer "Docker Desktop is running"
```

#### 2. Verificar Status do Docker
```powershell
# Verificar se Docker está respondendo
docker version

# Se der erro, tente:
docker ps
```

#### 3. Resetar Docker Desktop
```powershell
# Limpar tudo do Docker
docker system prune -a --volumes

# Depois reiniciar Docker Desktop
```

#### 4. Verificar Espaço em Disco
```powershell
# Verificar espaço disponível
Get-PSDrive C
```

#### 5. Reinstalar Docker Desktop
Se nada funcionar:
1. Desinstalar Docker Desktop completamente
2. Baixar a versão mais recente de https://www.docker.com/products/docker-desktop
3. Reinstalar e reiniciar o computador

### Alternativa: Rodar sem Docker

Se o Docker não funcionar, você pode rodar a aplicação localmente:

#### 1. Instalar PostgreSQL e RabbitMQ localmente
```powershell
# PostgreSQL pode ser instalado via:
# https://www.postgresql.org/download/windows/

# RabbitMQ pode ser instalado via:
# https://www.rabbitmq.com/download.html
```

#### 2. Configurar application.properties
```properties
# Usar configurações locais ao invés de Docker
spring.datasource.url=jdbc:postgresql://localhost:5432/conectaai
spring.rabbitmq.host=localhost
```

#### 3. Rodar a aplicação
```powershell
./gradlew bootRun
```

### Sobre os Warnings de Variáveis

Os warnings sobre `ASAAS_TOKEN`, `STRIPE_SECRET`, `MP_ACCESS_TOKEN` são **normais** e não impedem o funcionamento. Eles aparecem porque essas variáveis não estão definidas no ambiente.

Para eliminar os warnings (opcional), crie um arquivo `.env` na raiz do projeto:
```env
ASAAS_TOKEN=
STRIPE_SECRET=
MP_ACCESS_TOKEN=
```

### Verificar Logs do Docker Desktop

Se o Docker Desktop estiver rodando mas dando erro, verifique os logs:
- Windows: `%LOCALAPPDATA%\Docker\log.txt`
- Ou use: `Get-Content "$env:LOCALAPPDATA\Docker\log.txt" -Tail 50`

### Comandos Úteis

```powershell
# Ver status dos containers
docker ps -a

# Ver logs de um container
docker logs <container_name>

# Parar todos os containers
docker stop $(docker ps -aq)

# Remover todos os containers
docker rm $(docker ps -aq)

# Limpar tudo (cuidado!)
docker system prune -a --volumes
```
