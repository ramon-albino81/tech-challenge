# Tarefas

**Diretrizes obrigatórias para o agente:** antes de executar cada tarefa, carregar e aplicar as skills mapeadas em [design.md](./design.md), de acordo com o escopo. Para tarefas que cruzem escopos, aplicar todas as skills pertinentes. Não alegar uso de skill indisponível; comunicar a limitação. Não adicionar comentários de linha, bloco, multilinha ou Javadoc ao código de produção, testes, scripts ou migrações; comentários em arquivos de configuração só são permitidos quando estritamente necessários.

## 1. Preparação do repositório e fluxo Git

- [x] 1.1 Aplicar `/java-springboot`; preparar na `main` uma base mínima Java 21/Spring Boot 3 com Maven Wrapper que compile e tenha `.gitignore`; configurar `user.name` e `user.email` com a identidade real do único autor e verificar `./mvnw test`; então criar `feature/tech-challenge` a partir de `main` e verificar `git branch --list` e `git log --oneline --graph`.
- [ ] 1.2 Para cada etapa coerente do plano, criar uma nova branch `feature/<etapa>` diretamente a partir de `feature/tech-challenge`; implementar nela somente incrementos relacionados e compiláveis, abrir PR direcionado a `feature/tech-challenge`, revisar individualmente diff/testes e integrar após aprovação própria; depois de abrir o PR, aguardar autorização humana expressa antes de iniciar uma nova etapa; alterações solicitadas no PR atual permanecem na etapa em andamento; verificar que cada etapa tem branch e PR correspondentes sem simular outros autores/revisores.
- [ ] 1.3 Fazer commits progressivos sempre que uma unidade lógica estiver implementada, verificada e compilável; não acumular incrementos prontos para além da sessão de trabalho e não criar commits artificiais por arquivo ou por horário; dividir tarefas longas em partes independentes; verificar distribuição e conteúdo dos commits ao longo das branches/PRs.
- [ ] 1.4 Aplicar `/conventional-commits`; usar tipos Conventional Commits com assunto imperativo de até 72 caracteres, preferencialmente em português, e escopos quando úteis; verificar mensagens. Manter um único autor real em `git shortlog -sn`, sem inventar identidades.
- [ ] 1.5 Manter `main` estável; não usar mensagens genéricas nem reescrever histórico publicado com force-push. Fazer revisão própria antes de cada PR de etapa. Tags/releases são opcionais; verificar evidências em `git log --oneline --graph`, PRs e `git shortlog -sn`.

## 2. Fundação e estrutura do repositório

- [x] 2.1 Adicionar dependências de Spring MVC, validação, Spring Security resource server/JWT, Spring Data JPA, MySQL Connector/J, suporte Flyway para MySQL, springdoc, JUnit, Mockito e Testcontainers; verificar `./mvnw dependency:resolve`.
- [x] 2.2 Criar e manter a estrutura sugerida `src/`, `postman/collection.json`, `postman/environment.json`, `docs/`, `Dockerfile`, `docker-compose.yml`, `.env.example`, `.gitignore` e `README.md`; verificar a presença dos caminhos e que arquivos sensíveis/build, incluindo `.env`, `target/` e `.idea/`, estão ignorados.

## 3. Modelo, banco e casos de uso

- [x] 3.1 Implementar `User`, `Address` e `UserType`, com nome, e-mail, login, senha, tipo, data/hora de última alteração e endereço com rua, número, cidade e CEP; verificar testes JUnit para campos ausentes, formatos inválidos e tipos permitidos.
- [x] 3.2 Aplicar `/mysql` e `/java-springboot`; criar migração Flyway para MySQL 8.4/InnoDB com `id BIGINT UNSIGNED AUTO_INCREMENT`, charset/collation `utf8mb4`/`utf8mb4_0900_ai_ci`, colunas `name VARCHAR(150)`, `email VARCHAR(254)`, `login VARCHAR(100)`, `password_hash VARCHAR(255)`, `user_type VARCHAR(32)`, `last_modified_at DATETIME(6)` em UTC e, logo após, `address JSON NOT NULL` contendo strings não vazias `street`, `number`, `city` e `postal_code`; aplicar unicidade a e-mail/login normalizados e `CHECK` ao tipo e estrutura do JSON; alinhar validações da API aos comprimentos; verificar migração em MySQL e correspondência dos mapeamentos JPA.
- [x] 3.3 Implementar criação somente por `DONO_RESTAURANTE`, normalização de e-mail/login, armazenamento de hash e tratamento específico de duplicidade; verificar testes de sucesso, campos ausentes, e-mail/login duplicados, senha nunca em texto puro e resposta sem senha/hash.
- [x] 3.4 Implementar atualização de perfil sem senha/tipo, alteração separada da própria senha com confirmação da atual, e atualização de `last_modified_at` na criação e em cada alteração; verificar testes de sucesso e erro em cada fluxo e rejeição de senha atual incorreta.
- [x] 3.5 Implementar consulta, busca parcial por nome com paginação limitada, exclusão e regras de titularidade; verificar testes de dono, cliente na própria conta, cliente tentando agir em conta alheia, inexistência e ordenação/paginação.
- [x] 3.6 Aplicar `/java-springboot`, `/clean-architecture` e `/mysql`; implementar adaptador JPA e transações nos casos de uso; verificar testes de integração Testcontainers para CRUD, migrações, round-trip do objeto `address JSON`, persistência de data, unicidade concorrente e busca em MySQL.

## 4. JWT e autorização

- [x] 4.1 Configurar login público, emissão/validação JWT Nimbus com Spring Security, BCrypt, chave HMAC externa de pelo menos 256 bits, validade de 15 minutos e claims de identificador/tipo; verificar testes de login válido/inválido, mensagem genérica, token ausente, inválido, expirado e permissões.
- [x] 4.2 Implementar seed idempotente de `DONO_RESTAURANTE` somente em perfis de desenvolvimento/avaliação, com credenciais configuráveis e senha em hash; verificar conta em banco limpo, ausência do seed em produção e nenhuma duplicação/redefinição após reiniciar.
- [x] 4.3 Aplicar autorização: dono administra perfis e usuários; cliente atua somente sobre a própria conta; toda pessoa altera somente a própria senha; somente dono cria e escolhe tipo; verificar `401` para não autenticado e `403` para cada ação não autorizada.

## 5. API, erros e documentação

- [x] 5.1 Aplicar `/java-springboot` e `/swagger-doc-creator`; implementar as rotas `POST /api/v1/auth/login`, `POST /api/v1/users`, `GET /api/v1/users/{id}`, `GET /api/v1/users?name=&page=&size=`, `PUT /api/v1/users/{id}`, `PUT /api/v1/users/{id}/password` e `DELETE /api/v1/users/{id}` com DTOs distintos e validação; verificar métodos, payloads, autorização e status `200`, `201` e `204` definidos na especificação.
- [x] 5.2 Padronizar erros com `application/problem+json`/`ProblemDetail` para `400`, `401`, `403`, `404`, `409` e `500`, sem informação interna; verificar status, corpo e ausência de SQL/segredos em testes MVC.
- [x] 5.3 Aplicar `/swagger-doc-creator` e `/java-springboot`; documentar todas as rotas em OpenAPI/Swagger, com exemplos de sucesso e erro, campos, autenticação e fluxo JWT; verificar documento OpenAPI e acesso do Swagger no ambiente de avaliação, com proteção/desativação em produção.
- [x] 5.4 Criar coleção `postman/collection.json` e `postman/environment.json` com `baseUrl` e testes de status; cobrir cadastro válido, e-mail/login duplicados, campos obrigatórios ausentes, troca de senha válida/inválida, atualização de perfil válida/inválida, busca por nome, login válido/inválido, JWT e acessos negados; verificar importação e execução da coleção.

## 6. Docker e configuração

- [x] 6.1 Aplicar `/multi-stage-dockerfile`; criar Dockerfile obrigatoriamente multi-stage, versões de imagem fixadas, runtime mínimo e usuário não root, além de `.dockerignore`; verificar compilação da imagem e ausência de arquivos/segredos indevidos no contexto.
- [x] 6.2 Aplicar `/mysql`; criar exatamente `docker-compose.yml` com aplicação e MySQL 8.4 em contêineres, imagem fixada, health check e configuração por ambiente; criar `.env.example` sem credenciais reais; verificar `docker compose config`.
- [ ] 6.3 Executar `docker compose up --build` em ambiente limpo com configuração de exemplo não produtiva; verificar prontidão da aplicação/banco, criação da conta de avaliação, login JWT e chamadas protegidas funcionais.

## 7. README, relatório e repositório

- [ ] 7.1 Escrever README em português com descrição do projeto, tecnologias, pré-requisitos, passo a passo de execução por Docker Compose, URL Swagger, integrantes e, por usar JWT, login/senha da conta de avaliação em texto claro; verificar cada seção e exemplos contra a aplicação.
- [ ] 7.2 Preparar `docs/relatorio-tecnico.pdf` com link do repositório, arquitetura detalhada, modelo/relacionamentos, justificativa de enum e endereço, endpoints com exemplos, evidências Swagger e Postman, estrutura/tabelas/colunas do banco, variáveis e execução Docker, trecho `git log --oneline --graph`, contribuição `git shortlog -sn` e credenciais de avaliação; verificar todos os itens obrigatórios do item 6.
- [ ] 7.3 Publicar/entregar o repositório aberto em GitHub/GitLab com código-fonte, README, documentação Swagger e coleção Postman; verificar acesso sem autenticação e presença dos arquivos exigidos.
- [ ] 7.4 Submeter o relatório técnico PDF na plataforma FIAP antes do prazo informado; verificar confirmação da submissão e que o documento contém o link do repositório.

## 8. Testes e verificação automatizada

- [ ] 8.1 Aplicar `/java-springboot` e `/mysql`; cobrir regras de serviço/casos de uso com JUnit + Mockito e fluxos HTTP, segurança e MySQL com testes de integração; verificar `./mvnw verify`.

## 9. Conferência final dos critérios e checklist

- [ ] 9.1 Conferir item 1: cadastro, atualização de perfil, exclusão, troca de senha separada, busca por nome, validação de login, tipos obrigatórios, e-mail único, códigos HTTP, versionamento e ProblemDetail; verificar cada cenário na suíte API.
- [ ] 9.2 Conferir itens 2 a 5: boas práticas Spring/SOLID/OO e testabilidade, Swagger com exemplos de sucesso/erro, banco relacional em contêiner, Compose e cenários Postman completos; verificar documentação, testes e artefatos entregues.
- [ ] 9.3 Conferir itens 6 a 8: relatório PDF com todos os conteúdos, Dockerfile multi-stage, `docker-compose.yml`, `.env.example` sem segredos reais, estrutura sugerida, repositório público e README completo; verificar arquivos e evidências de execução/submissão.
- [ ] 9.4 Conferir item 9 para trabalho individual: frequência/progressão por incremento, commits atômicos/compiláveis, Conventional Commits até 72 caracteres, identidade real de autor único, branch de integração e branches por etapa, PRs para `feature/tech-challenge`, revisão individual, `.gitignore` e histórico não reescrito; verificar `git log --oneline --graph`, PRs e `git shortlog -sn`.
- [ ] 9.5 Conferir item 10 e checklist de entrega: seed criado automaticamente em ambiente limpo, senha em hash e credenciais no README/PDF, conta com permissão para avaliar endpoints protegidos, login/token documentados e coleção/environment incluídos; verificar fluxo completo como avaliador e marcar cada caixa do checklist original antes da submissão.

## 10. Checklist literal de entrega

- [ ] 10.1 Confirmar que cadastro, atualização, exclusão, troca de senha, busca por nome e validação de login funcionam; verificar cada operação com testes e coleção Postman.
- [ ] 10.2 Confirmar unicidade de e-mail e armazenamento de senha com hash; verificar restrições MySQL e teste de persistência.
- [ ] 10.3 Confirmar API versionada e erros ProblemDetail RFC 7807; verificar rotas `/api/v1` e cenários de erro automatizados.
- [ ] 10.4 Confirmar Swagger/OpenAPI com exemplos de sucesso e erro; verificar o documento gerado e a interface no ambiente de avaliação.
- [ ] 10.5 Confirmar Dockerfile, `docker-compose.yml` e `.env.example` funcionais; verificar subida completa com `docker compose up --build` em ambiente limpo.
- [ ] 10.6 Confirmar que coleção Postman JSON e environment estão no repositório; verificar caminhos, importação, variável `baseUrl` e testes dos cenários.
- [ ] 10.7 Confirmar README completo; verificar descrição, tecnologias, pré-requisitos, execução Compose, URL Swagger, integrantes e credenciais JWT de avaliação.
- [ ] 10.8 Confirmar histórico consistente para um único autor; verificar frequência/progressão dos commits, mensagens Conventional Commits, autoria real, `feature/tech-challenge`, branches e PRs por etapa, revisão individual, `git log --oneline --graph` e `git shortlog -sn` com um autor.
- [ ] 10.9 Confirmar requisito JWT do item 10: conta de avaliação criada automaticamente, credenciais no README/PDF e acesso de avaliação a endpoints protegidos; verificar autenticação em ambiente limpo e fluxo documentado.
- [ ] 10.10 Confirmar que o repositório GitHub/GitLab está aberto e contém código-fonte, README, Swagger/OpenAPI e coleção Postman; verificar acesso público sem autenticação.
- [ ] 10.11 Confirmar relatório técnico PDF com link do repositório submetido na plataforma FIAP dentro do prazo; verificar o arquivo final e a confirmação de submissão.

## Fechamento após o arquivamento

Depois que todas as tarefas rastreáveis estiverem concluídas, arquivar todas as mudanças OpenSpec relacionadas. Somente então abrir um único PR final de `feature/tech-challenge` para `main`, incluindo resumo, evidências da suíte completa, teste Docker e checklist; revisar individualmente e integrar somente após aprovação própria. Esta etapa é posterior ao arquivamento e, por isso, não é uma caixa de tarefa deste change.
