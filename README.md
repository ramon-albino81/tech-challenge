# Tech Challenge FIAP — API de gerenciamento de usuários

Projeto individual do Tech Challenge FIAP Fase 1. A API está em desenvolvimento; neste estágio, o repositório contém a base Spring Boot e o ambiente local com MySQL. As operações de gerenciamento de usuários e o login JWT ainda serão implementados.

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

Os valores de `.env.example` são apenas placeholders para desenvolvimento local, não são credenciais de avaliação e não devem ser usados em produção. Altere as senhas locais antes de iniciar os contêineres.

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

A URL prevista para a interface Swagger é `http://localhost:8080/swagger-ui.html`. As rotas e o fluxo de autenticação serão documentados quando os contratos da API forem implementados.

## Integrante

- Ramon Albino — desenvolvimento individual
