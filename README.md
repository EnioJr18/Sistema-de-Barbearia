# 💈 Sistema de Barbearia API

![Status](https://img.shields.io/badge/Status-Em%20desenvolvimento-yellow)
![Java](https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![CI](https://github.com/EnioJr18/Sistema-de-Barbearia/actions/workflows/ci.yml/badge.svg)

API REST backend para gerenciar usuários, serviços e agendamentos de uma barbearia. O projeto foi desenvolvido como prática de backend profissional com foco em regras de negócio, segurança, documentação e operação local reproduzível.

> Este repositório contém somente a API backend. Frontend web e aplicativo mobile não fazem parte da implementação atual.

## 📌 Sobre o projeto

O sistema centraliza os fluxos de cadastro, autenticação, gestão de serviços e agenda. A API usa DTOs como contrato HTTP, mantém a senha fora das respostas, aplica regras de disponibilidade por duração de serviço e oferece documentação interativa para facilitar a integração de futuros clientes.

## 📊 Status atual

O backend está em desenvolvimento ativo, com autenticação JWT, autorização por perfis, migrations versionadas, testes automatizados e execução local por Gradle ou Docker Compose.

## Diferenciais técnicos

- API REST com Spring Boot e arquitetura em camadas.
- DTOs e mappers para não expor entidades JPA nem senha/hash nas respostas.
- Bean Validation e tratamento global padronizado de erros HTTP.
- Autenticação stateless com Spring Security, JWT e BCrypt.
- Perfis `ADMIN`, `BARBEIRO` e `CLIENTE`, com regras de acesso e propriedade do recurso.
- Cadastro público que sempre cria usuários `CLIENTE`.
- Conflito de agenda por sobreposição de intervalos e duração do serviço.
- Cancelamento lógico dedicado, sem apagar o histórico do agendamento.
- Consulta de horários disponíveis.
- Flyway para versionamento de schema e Hibernate em modo `validate`.
- Swagger/OpenAPI com suporte a Bearer Token.
- Ambiente local completo com Docker Compose e PostgreSQL 18.
- 142 testes automatizados de services, controllers, mappers, segurança e tratamento de erros.

## 🛠️ Tecnologias

| Área | Tecnologias |
| --- | --- |
| Linguagem e framework | Java 25, Spring Boot 4.0.3, Spring Web MVC |
| Persistência | Spring Data JPA, PostgreSQL, Neon, H2 para testes |
| Segurança | Spring Security, JWT (Auth0 Java JWT), BCrypt |
| Validação e documentação | Jakarta Bean Validation, Springdoc OpenAPI/Swagger |
| Banco e migrações | Flyway |
| Build e testes | Gradle Kotlin DSL, JUnit 5, Mockito, MockMvc |
| Ambiente local | Docker, Docker Compose, PostgreSQL 18 |

## 🧱 Arquitetura

O projeto organiza responsabilidades em camadas, mantendo os controllers voltados ao protocolo HTTP e concentrando regras de negócio nos services.

```text
Cliente HTTP
    |
Spring Security + filtro JWT
    |
Controllers -> DTOs / Bean Validation
    |
Services -> regras de negócio e autorização contextual
    |
Repositories -> JPA
    |
PostgreSQL / H2 (testes)
```

### 📁 Estrutura principal:

```text
src/
├── main/
│   ├── java/com/seuapp/
│   │   ├── config/       # CORS, OpenAPI e bootstrap de admin local
│   │   ├── controller/   # Endpoints REST
│   │   ├── dto/          # Requests, responses e erros da API
│   │   ├── exception/    # Tratamento global de exceções
│   │   ├── mapper/       # Conversão entre DTOs e entidades
│   │   ├── model/        # Entidades JPA e enums
│   │   ├── repository/   # Acesso a dados
│   │   ├── security/     # JWT, autenticação e controle de acesso
│   │   └── service/      # Regras de usuários, serviços e agenda
│   └── resources/
│       ├── db/migration/ # Migrations Flyway
│       └── application-*.properties
└── test/java/com/seuapp/ # Testes unitários e de controllers
```

## ✅ Funcionalidades

### 👤 Usuários e autenticação

- Login em `POST /login` com retorno de token JWT.
- Cadastro público em `POST /usuarios`, sempre com perfil `CLIENTE`.
- Criação de `ADMIN` e `BARBEIRO` por um usuário `ADMIN`.
- Atualização de dados sem alterar senha pelo endpoint comum.
- Troca de senha em endpoint dedicado, com hash BCrypt.
- Respostas sem senha ou hash.

### Serviços

- CRUD de serviços com nome, descrição, preço e duração em minutos.
- Listagem paginada com filtro opcional por nome.
- Validação de preço e duração positivos.

### Agendamentos

- Criação com status inicial `PENDENTE`.
- Listagem paginada e filtros administrativos.
- Atualização e exclusão administrativa.
- Cancelamento dedicado por `PATCH /agendamentos/{id}/cancelar`.
- Consulta de horários em `GET /agendamentos/horarios-disponiveis`.

## 🗓️ Regras de agendamento

Ao criar ou consultar horários, a API aplica as regras abaixo:

- Não permite agendamentos no passado.
- Não atende aos domingos.
- O horário de funcionamento atual é de `08:00` a `18:00`.
- Um agendamento não pode iniciar antes das `08:00` nem terminar após as `18:00`.
- A duração do serviço compõe o intervalo ocupado.
- Há conflito quando `novoInicio < existenteFim && novoFim > existenteInicio`.
- Agendamentos `CANCELADO` não bloqueiam novos horários.
- Um agendamento `CANCELADO` ou `CONCLUIDO` não pode ser cancelado novamente.

Os horários disponíveis são gerados em blocos da duração do serviço consultado, considerando a agenda do barbeiro e removendo intervalos em conflito.

## 🔐 Segurança e perfis de acesso

As rotas públicas são:

- `POST /login`
- `POST /usuarios`
- `/swagger-ui.html`, `/swagger-ui/**` e `/v3/api-docs/**`

Todas as demais rotas exigem JWT no cabeçalho:

```http
Authorization: Bearer <token>
```

| Perfil | Principais permissões |
| --- | --- |
| `ADMIN` | Gestão completa de usuários, serviços e agendamentos; pode criar admins e barbeiros. |
| `BARBEIRO` | Pode criar e atualizar serviços; cria agendamento quando é o barbeiro responsável; consulta e cancela seus próprios agendamentos. |
| `CLIENTE` | Cadastro público; cria agendamento quando é o cliente informado; consulta e cancela seus próprios agendamentos. |

Regras relevantes de acesso:

- `GET /usuarios` e operações administrativas de usuários exigem `ADMIN`.
- Usuários autenticados podem consultar, editar dados e trocar somente a própria senha, exceto quando a operação é feita por `ADMIN`.
- `POST` e `PUT /servicos` aceitam `ADMIN` ou `BARBEIRO`; exclusão exige `ADMIN`.
- `GET /agendamentos` e `DELETE /agendamentos/{id}` exigem `ADMIN`.
- Um agendamento individual pode ser acessado ou cancelado pelo `ADMIN`, cliente dono ou barbeiro responsável.

## 🔗 Endpoints principais

| Grupo | Rotas |
| --- | --- |
| Autenticação | `POST /login` |
| Usuários | `POST /usuarios`, `POST /usuarios/admin`, `POST /usuarios/barbeiro`, `GET /usuarios`, `GET/PUT/DELETE /usuarios/{id}`, `PUT /usuarios/{id}/senha` |
| Serviços | `GET/POST /servicos`, `GET/PUT/DELETE /servicos/{id}` |
| Agendamentos | `GET/POST /agendamentos`, `GET/PUT/DELETE /agendamentos/{id}`, `PATCH /agendamentos/{id}/cancelar`, `GET /agendamentos/horarios-disponiveis` |

Consulte o Swagger para contratos completos, parâmetros, DTOs e respostas de erro.

## Swagger / OpenAPI

Com a aplicação em execução:

```text
Swagger UI: http://localhost:8080/swagger-ui.html
OpenAPI JSON: http://localhost:8080/v3/api-docs
```

Para testar endpoints protegidos, faça login, copie o token retornado e use o botão `Authorize` da interface Swagger. Informe o token puro; a interface adiciona o esquema Bearer.

## 🗄️ Banco de dados e Flyway

O projeto usa PostgreSQL local, Docker ou Neon. As credenciais nunca devem ser versionadas e devem ser fornecidas por variáveis de ambiente.

As migrations ficam em `src/main/resources/db/migration`. A migration inicial `V1__create_initial_schema.sql` cria:

- `tb_usuarios`, com e-mail único;
- `tb_servicos`;
- `tb_agendamentos`, com relacionamentos para cliente, barbeiro e serviço;
- índices para consultas recorrentes de agendamento.

Flyway é habilitado por padrão e Hibernate usa `spring.jpa.hibernate.ddl-auto=validate`. Em banco vazio, o Flyway cria `flyway_schema_history` e aplica a migration antes da validação do Hibernate.

Para um banco Neon legado criado previamente por Hibernate, faça backup e avalie um baseline do Flyway antes de apontar a aplicação para ele. Não recrie ou apague dados de produção automaticamente.

## 🧩 Variáveis de ambiente

| Variável | Obrigatória | Descrição |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | Sim em `dev`/`prod` | URL JDBC do PostgreSQL ou Neon. |
| `SPRING_DATASOURCE_USERNAME` | Sim em `dev`/`prod` | Usuário do banco. |
| `SPRING_DATASOURCE_PASSWORD` | Sim em `dev`/`prod` | Senha do banco. |
| `API_SECURITY_TOKEN_SECRET` | Sim em `dev`/`prod` | Segredo longo e aleatório para assinar JWT. |
| `SPRING_PROFILES_ACTIVE` | Não | Perfil: `dev`, `test`, `prod` ou `docker`. |
| `SPRING_FLYWAY_ENABLED` | Não | Controla Flyway; o padrão é `true`. |
| `APP_DEV_ADMIN_ENABLED` | Não | Habilita criação do primeiro admin em `dev` ou `docker`. |
| `APP_DEV_ADMIN_NOME` | Condicional | Nome do admin de bootstrap. |
| `APP_DEV_ADMIN_EMAIL` | Condicional | E-mail do admin de bootstrap. |
| `APP_DEV_ADMIN_SENHA` | Condicional | Senha do admin de bootstrap. |

Veja `src/main/resources/application-example.properties` para valores fictícios. Não use URL, senha ou segredo reais do Neon em arquivos versionados.

## 🚀 Como rodar localmente com Gradle

Pré-requisitos: Java 25, PostgreSQL acessível (local ou Neon) e variáveis de ambiente configuradas.

No Windows PowerShell:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/barbearia"
$env:SPRING_DATASOURCE_USERNAME="barbearia_user"
$env:SPRING_DATASOURCE_PASSWORD="troque-esta-senha"
$env:API_SECURITY_TOKEN_SECRET="use-um-segredo-longo-e-aleatorio"
$env:SPRING_PROFILES_ACTIVE="dev"
.\gradlew.bat bootRun
```

No Linux, macOS ou Git Bash:

```bash
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/barbearia"
export SPRING_DATASOURCE_USERNAME="barbearia_user"
export SPRING_DATASOURCE_PASSWORD="troque-esta-senha"
export API_SECURITY_TOKEN_SECRET="use-um-segredo-longo-e-aleatorio"
export SPRING_PROFILES_ACTIVE="dev"
./gradlew bootRun
```

O perfil `dev` pode criar o primeiro administrador, sem abrir endpoint público para isso. Para habilitá-lo localmente:

```powershell
$env:APP_DEV_ADMIN_ENABLED="true"
$env:APP_DEV_ADMIN_NOME="Admin Dev"
$env:APP_DEV_ADMIN_EMAIL="admin.dev@example.com"
$env:APP_DEV_ADMIN_SENHA="troque-esta-senha"
```

Esse bootstrap só é ativo nos perfis `dev` e `docker`. Não o habilite em produção.

## 🐋 Docker e Docker Compose

O Compose sobe a API e um PostgreSQL 18 local, com volume nomeado para persistência. O Flyway cria o schema automaticamente no primeiro início.

```bash
docker compose up --build
```

Endereços locais:

- API e Swagger: `http://localhost:8080/swagger-ui.html`
- PostgreSQL no host: `localhost:5433`

O ambiente Docker cria, por padrão, o admin local abaixo:

```text
e-mail: admin@docker.com
senha: senha-forte-docker
```

Esses valores são somente para desenvolvimento local e podem ser alterados no `docker-compose.yml` antes de subir o ambiente.

Após atualizar um volume criado com a configuração anterior do PostgreSQL 18, recrie o banco local uma única vez:

```bash
docker compose down -v
docker compose up --build
```

Para parar os containers sem apagar dados:

```bash
docker compose down
```

`docker compose down -v` remove o volume e todos os dados do banco local do Compose.

## 🧪 Testes automatizados

O perfil `test` usa H2 em memória e Flyway, sem depender do Neon. A suíte cobre services, mappers, controllers, segurança JWT e tratamento global de erros.

```powershell
.\gradlew.bat clean build
```

No Linux, macOS ou Git Bash:

```bash
./gradlew clean build
```

## ⚠️ Limitações e próximos passos

- Não há frontend web ou aplicativo mobile neste repositório.
- Horários e dias de funcionamento ainda são fixos; futuramente podem ser configurados por barbearia ou barbeiro.
- Não há mecanismo de concorrência de banco para reservar simultaneamente o mesmo intervalo.
- Não há notificações, lista de espera, pagamentos integrados ou dashboard financeiro.
- Antes de produção, recomenda-se revisar observabilidade, rate limiting, gestão de segredos, política de CORS e estratégia de backup do banco.


## 📄 Licença

Este projeto está sob a licença MIT. Consulte o arquivo [LICENSE](LICENSE) para mais detalhes.

## 🤝 Contribuição

Contribuições são bem-vindas! Sinta-se à vontade para abrir issues ou enviar pull requests focados em melhorias de arquitetura REST, segurança, testes, documentação ou regras de agendamento.

## 👨‍💻 Autor

Desenvolvido por **Enio Jr.** para estudo, evolução técnica e portfólio backend/Engenharia de Software.

**Contato:**
- LinkedIn: https://www.linkedin.com/in/enioeduardojr
- Portfólio: https://eniojr18.github.io
- Email: eniojr100@gmail.com
- Instagram: https://www.instagram.com/eniojuniorrr
