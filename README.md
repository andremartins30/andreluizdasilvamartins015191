# PROCESSO SELETIVO CONJUNTO Nº 001/2026/SEPLAG e demais Órgãos - Engenheiro da Computação- Sênior
# Sistema de Gerenciamento de Artistas e Álbuns

Aplicação full stack desenvolvida em Java Spring Boot e React TypeScript para gerenciamento de artistas, álbuns e sincronização de dados externos.

## Sobre o Projeto

Este sistema oferece uma solução completa para catalogação e gerenciamento de artistas musicais, seus álbuns e capas. Desenvolvido seguindo princípios de Clean Code, arquitetura em camadas e padrões de projeto consolidados.

### Tecnologias Principais

**Backend**
- Java 17 com Spring Boot 3.5
- Spring Security com autenticação JWT
- PostgreSQL 16 para persistência
- Flyway para versionamento de banco
- MinIO (S3) para armazenamento de imagens
- WebSocket para notificações em tempo real
- Bucket4j para rate limiting
- JUnit 5 e Mockito para testes

**Frontend**
- React 19 com TypeScript
- Tailwind CSS v4 para estilização
- RxJS para gerenciamento de estado reativo
- Axios para comunicação HTTP
- React Router para navegação
- Vitest e Testing Library para testes

**Infraestrutura**
- Docker e Docker Compose para orquestração
- Nginx como servidor web de produção

## Pré-requisitos

- Docker 20.10 ou superior
- Docker Compose 2.0 ou superior
- 4GB de RAM disponível
- Portas disponíveis: 5173, 8080, 5433, 9000, 9001

## Instalação e Execução

### Executando com Docker Compose (Recomendado)

1. Clone o repositório:
```bash
git clone <repository-url>
cd andreluizdasilvamartins015191
```

2. Configure as variáveis de ambiente (opcional, já possui defaults):
```bash
cp .env.example .env
```

3. Inicie todos os serviços:
```bash
docker compose up --build
```

4. Aguarde a inicialização completa (aproximadamente 2 minutos na primeira execução).

5. Acesse a aplicação:
- **Frontend**: http://localhost:5173
- **API**: http://localhost:8080
- **Swagger**: http://localhost:8080/swagger-ui.html
- **MinIO Console**: http://localhost:9001

### Executando Localmente (Desenvolvimento)

**Backend:**
```bash
cd backend
./mvnw clean install
./mvnw spring-boot:run
```

**Frontend:**
```bash
cd frontend
npm install
npm run dev
```

## Credenciais Padrão

### Aplicação
- **Usuário**: andre
- **Senha**: 123456

### MinIO Console
- **Usuário**: admin
- **Senha**: admin123

## Estrutura do Projeto

```
.
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/mt/gov/seplag/backend/
│   │   │   │   ├── config/          # Configurações (CORS, Security, WebSocket)
│   │   │   │   ├── controller/      # Endpoints REST
│   │   │   │   ├── domain/          # Entidades JPA
│   │   │   │   ├── security/        # Filtros JWT e Rate Limiting
│   │   │   │   ├── service/         # Lógica de negócio
│   │   │   │   └── web/             # DTOs
│   │   │   └── resources/
│   │   │       └── db/migration/    # Scripts Flyway
│   │   └── test/                    # Testes unitários
│   ├── Dockerfile
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── api/                     # Serviços HTTP
│   │   ├── auth/                    # Contexto de autenticação
│   │   ├── components/              # Componentes React
│   │   ├── facades/                 # Facade Pattern
│   │   ├── pages/                   # Páginas da aplicação
│   │   ├── routes/                  # Configuração de rotas
│   │   └── store/                   # Gerenciamento de estado (BehaviorSubject)
│   ├── Dockerfile
│   └── package.json
├── docker-compose.yml
└── README.md
```

## Sincronização de Regionais (Requisito Sênior)

Sistema de sincronização de regionais da Polícia Civil com complexidade algorítmica O(n + m).
## Funcionalidades Implementadas

### Autenticação e Segurança
- Autenticação JWT com expiração de 5 minutos
- Refresh token para renovação automática
- Rate limiting (100 req/min para GET, 50 req/min para POST/PUT/DELETE)
- CORS configurado para origens permitidas
- Proteção de rotas no frontend

### Gerenciamento de Artistas
- CRUD completo de artistas
- Busca por nome com filtro
- Ordenação alfabética (ascendente e descendente)
- Paginação de resultados
- Contagem de álbuns por artista

### Gerenciamento de Álbuns
- CRUD completo de álbuns
- Associação de álbuns a artistas
- Upload de capa de álbum
- Armazenamento no MinIO (S3)
- URLs pré-assinadas com expiração de 30 minutos
- Notificações em tempo real via WebSocket ao cadastrar novo álbum

### Sincronização de Regionais (Requisito Sênior)
- Importação de dados da API externa
- Tabela com campos: id, id_externo, nome, ativo, criado_em
- Algoritmo de sincronização com complexidade O(n + m)
- Regras implementadas:
  - Novo registro → inserção
  - Registro removido → inativação
  - Atributo alterado → inativação do anterior + criação de novo
- Fallback inteligente: tenta via backend, se falhar busca do navegador
- Manutenção de histórico completo

### Qualidade e Boas Práticas
- Health checks (liveness e readiness)
- Testes unitários no backend
- Documentação OpenAPI/Swagger
- Migrações versionadas com Flyway
- Padrão Facade no frontend
- Gerenciamento de estado reativo com BehaviorSubject
- Layout responsivo com Tailwind CSS
- Lazy loading de rotas
- Tratamento centralizado de erros

## Arquitetura

### Backend - Arquitetura em Camadas

```
Controller → Service → Repository → Database
     ↓
   DTOs ← Domain Models
```

**Padrões Utilizados:**
- Repository Pattern para acesso a dados
- DTO Pattern para transferência de dados
- Dependency Injection via Spring
- Builder Pattern para construção de objetos complexos

### Frontend - Arquitetura Componentizada

```
Pages → Facade → Services → API
  ↓       ↓
Stores (BehaviorSubject)
  ↓
Components
```

**Padrões Utilizados:**
- Facade Pattern para abstração de complexidade
- Observer Pattern via RxJS BehaviorSubject
- Container/Presentational Components
- Protected Routes com HOC

### Fluxo de Autenticação

```
1. Login → JWT + Refresh Token
2. Request com Authorization: Bearer <token>
3. Backend valida token
4. Se expirado: frontend renova com refresh token
5. Se refresh expirado: redireciona para login
```

### Algoritmo de Sincronização de Regionais

**Complexidade: O(n + m)**
- n = número de regionais na API externa
- m = número de regionais ativas no banco local

**Estratégia:**
1. Busca regionais ativas do banco (1 query)
2. Converte API e banco em Maps (O(n + m))
3. Itera regionais da API (O(n)):
   - Se novo → insere
   - Se alterado → inativa antigo + cria novo
   - Se igual → não faz nada
4. Itera regionais locais não processadas (O(m)):
   - Se não está na API → inativa
5. Salva tudo em batch (2 operações)

## API Endpoints

### Autenticação
```
POST   /api/v1/auth/register    - Registro de usuário
POST   /api/v1/auth/login       - Login
POST   /api/v1/auth/refresh     - Renovar token
```

### Artistas
```
GET    /api/v1/artists          - Listar artistas (paginado)
GET    /api/v1/artists/{id}     - Buscar artista por ID
POST   /api/v1/artists          - Criar artista
PUT    /api/v1/artists/{id}     - Atualizar artista
DELETE /api/v1/artists/{id}     - Remover artista
```

### Álbuns
```
GET    /api/v1/albums           - Listar álbuns
POST   /api/v1/albums           - Criar álbum
PUT    /api/v1/albums/{id}      - Atualizar álbum
DELETE /api/v1/albums/{id}      - Remover álbum
POST   /api/v1/albums/{id}/cover - Upload de capa
```

### Regionais
```
GET    /api/v1/regionais        - Listar todas as regionais
GET    /api/v1/regionais/ativas - Listar apenas ativas
POST   /api/v1/regionais/sincronizar - Sincronizar com API externa
POST   /api/v1/regionais/importar    - Importação manual
```

### Monitoramento
```
GET    /actuator/health         - Status da aplicação
GET    /actuator/health/liveness - Liveness probe
GET    /actuator/health/readiness - Readiness probe
```

## Testes

### Backend
```bash
cd backend
./mvnw test
```

**Cobertura atual:**
- AuthServiceTest: registro, login, refresh token
- ArtistServiceTest: CRUD completo
- Testes de integração parciais

### Frontend
```bash
cd frontend
npm test
```

## Monitoramento e Health Checks

A aplicação implementa health checks conforme especificação do edital:

**Liveness Probe**: Verifica se a aplicação está rodando
```bash
curl http://localhost:8080/actuator/health/liveness
```

**Readiness Probe**: Verifica se está pronta para receber requisições
```bash
curl http://localhost:8080/actuator/health/readiness
```

**Health Indicators Customizados:**
- DatabaseHealthIndicator: Status da conexão PostgreSQL
- MinioHealthIndicator: Status do bucket e conectividade MinIO


## Troubleshooting

### Erro ao subir containers
```bash
# Limpar volumes e reconstruir
docker compose down -v
docker compose up --build
```

### Backend não conecta no MinIO
```bash
# Verificar se bucket foi criado
docker exec -it minio mc ls local/
```

### Erro de migração Flyway
```bash
# Resetar banco
docker compose down -v postgres
docker compose up postgres
```

### Frontend não carrega
```bash
# Verificar logs
docker compose logs frontend
# Reconstruir sem cache
docker compose build --no-cache frontend
```


## Licença

Este projeto foi desenvolvido para fins avaliativos.

## Autor

Desenvolvido como projeto técnico para processo seletivo Engenheiro de Computação Senior Seplag/MT.
