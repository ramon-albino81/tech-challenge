# Tech Challenge FIAP — API de gerenciamento de usuários

Projeto individual do Tech Challenge FIAP Fase 1. A API está em desenvolvimento e utiliza Spring Boot, MySQL e Flyway para persistência, com autenticação JWT e uma conta de avaliação local criada automaticamente.

## Tecnologias

- Java 21
- Spring Boot 3
- Maven Wrapper
- MySQL 8.4/InnoDB
- Flyway
- Spring Security com JWT/Nimbus
- OpenAPI/Swagger
- Docker e Docker Compose

## Pré-requisitos

- Docker Engine
- Docker Compose v2 (`docker compose`)
- Git

## Executar localmente

Copie o arquivo de exemplo para a configuração local:

```bash
cp .env.example .env
```

Os valores de `.env.example` são exclusivamente para desenvolvimento e avaliação local, não devem ser usados em produção. Altere as senhas e a chave JWT antes de iniciar os contêineres. O perfil `evaluation` cria de forma idempotente uma conta `DONO_RESTAURANTE` no banco vazio; o banco guarda somente o hash BCrypt da senha.

Credenciais locais de avaliação do arquivo de exemplo:

- Login: `avaliador`
- Senha: `AvaliacaoLocal!2026`

Esses valores são públicos e não podem ser reutilizados em produção.

Inicie a aplicação e o MySQL:

```bash
docker compose up --build
```

A aplicação fica disponível em `http://localhost:8080` e o MySQL em `localhost:3306`, conforme as portas definidas em `.env`. O serviço da aplicação aguarda o health check do banco antes de iniciar.

Para acompanhar o estado:

```bash
docker compose ps
docker compose logs -f app
```

Para encerrar os serviços mantendo os dados do MySQL:

```bash
docker compose down
```

Os dados ficam no volume `mysql_data`. Remover esse volume apaga o banco local.

## Testes

Execute a suíte automatizada com o Maven Wrapper:

```bash
./mvnw test
```

Para validar a compilação, os testes e o empacotamento:

```bash
./mvnw verify
```

## Documentação e API

A interface Swagger fica em `http://localhost:8080/swagger-ui.html`. Para obter um JWT, envie `POST /api/v1/auth/login` com `{"login":"avaliador","password":"AvaliacaoLocal!2026"}`. Use o campo `accessToken` da resposta como bearer token nas rotas protegidas. O token expira em 15 minutos. A documentação Swagger e seus endpoints podem ser desativados em produção por `SWAGGER_ENABLED=false`; o perfil `prod` já os desativa.

Rotas disponíveis:

- `POST /api/v1/auth/login`: autenticação e emissão de JWT.
- `POST /api/v1/users`: criação por `DONO_RESTAURANTE`, com resposta `201` e header `Location`.
- `GET /api/v1/users/{id}`: consulta permitida ao dono ou ao titular.
- `GET /api/v1/users?name={nome}&page=0&size=20`: busca paginada exclusiva do dono; `size` máximo `100`.
- `PUT /api/v1/users/{id}`: atualização de perfil sem senha ou tipo; campos desconhecidos são rejeitados.
- `PUT /api/v1/users/{id}/password`: troca da própria senha com confirmação da senha atual.
- `DELETE /api/v1/users/{id}`: exclusão pelo dono ou pelo titular da conta.

Erros da API são retornados como `application/problem+json`. Importe `postman/collection.json` e `postman/environment.json` no Postman e execute os requests na ordem apresentada. O environment aponta para `http://localhost:8080` e contém somente credenciais locais/não produtivas; a coleção captura os tokens, cria uma conta temporária e verifica os fluxos de autorização, duplicidade, validação, alteração de senha e exclusão.

## Integrante

- Ramon Albino — desenvolvimento individual
