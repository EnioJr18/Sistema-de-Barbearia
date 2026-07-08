# 💈 Sistema de Barbearia API

![Status](https://img.shields.io/badge/Status-Em%20Desenvolvimento-yellow)
![Java](https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-Auth0-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-Kotlin%20DSL-02303A?style=for-the-badge&logo=gradle&logoColor=white)

API REST para gerenciamento básico de usuários, serviços e agendamentos de uma barbearia, desenvolvida com Java, Spring Boot, Spring Security, JWT e PostgreSQL.

## 📌 Sobre o Projeto

Este repositório contém atualmente apenas a **API Backend** do Sistema de Barbearia.

O objetivo do projeto é praticar desenvolvimento backend com Spring Boot, centralizando regras de negócio, autenticação, autorização inicial e persistência de dados em uma API REST. Futuramente, esta API poderá ser consumida por um painel Web administrativo e por um aplicativo Mobile para clientes.

## 🎯 Objetivos do Projeto

Este projeto foi desenvolvido para praticar:

- Desenvolvimento de APIs REST com Spring Boot.
- Arquitetura em camadas.
- Autenticação e autorização com Spring Security e JWT.
- Persistência com Spring Data JPA e PostgreSQL.
- Organização de regras de negócio no backend.
- Boas práticas de documentação para portfólio.

## 📊 Status do Projeto

| Módulo/Recurso | Status |
| --- | --- |
| API Backend | Em desenvolvimento |
| Banco PostgreSQL | Implementado |
| Autenticação JWT | Implementada |
| CRUD de usuários | Implementado |
| CRUD de serviços | Implementado |
| CRUD de agendamentos | Implementado |
| Frontend Web | Planejado |
| App Mobile | Planejado |
| Swagger/OpenAPI | Implementado |
| Docker | Planejado |
| Testes automatizados completos | Planejado |

## ✅ Funcionalidades Implementadas

- Cadastro de usuários.
- Login com geração de token JWT.
- CRUD básico de usuários.
- CRUD básico de serviços.
- CRUD básico de agendamentos.
- Listagens paginadas.
- Filtros básicos por nome, cliente e barbeiro.
- Proteção global de rotas com Spring Security.
- Regras iniciais de validação de agendamento.

## 🗓️ Regras de Agendamento Atuais

Atualmente, a API aplica algumas validações básicas ao criar um agendamento:

- Não permite agendamento no passado.
- Não permite agendamento aos domingos.
- Permite apenas horários entre 08h e 18h.
- Não permite dois agendamentos para o mesmo barbeiro no mesmo horário exato.

Limitações desta etapa:

- Ainda não existe cálculo completo de horários disponíveis.
- Ainda não existe validação por duração do serviço.
- Ainda não existe proteção robusta contra concorrência.

## 🧭 Funcionalidades Planejadas / Roadmap

- Frontend Web administrativo.
- Aplicativo Mobile para clientes.
- Docker e Docker Compose.
- Testes automatizados mais completos.
- Algoritmo de horários disponíveis.
- Cancelamento dedicado de agendamento.
- Controle granular por roles.
- Dashboard de agenda.
- Fechamento de caixa.
- Notificações.

## 💡 Próximas Melhorias Técnicas

- Implementar DTOs completos de request e response.
- Adicionar Bean Validation com `@Valid`, `@NotBlank`, `@Email` e validações semelhantes.
- Melhorar controle de permissões por perfil.
- Adicionar testes unitários e de integração.
- Adicionar Flyway ou Liquibase.
- Adicionar Docker/Docker Compose.
- Melhorar tratamento global de exceções.
- Evitar retorno direto de entidades JPA nas respostas.

## 🛠️ Tecnologias Utilizadas

### Linguagem e Framework

- Java 25
- Spring Boot 4.0.3
- Spring Web MVC

### Persistência

- Spring Data JPA
- PostgreSQL
- Neon

### Segurança

- Spring Security
- JWT com Auth0 Java JWT

### Build e Dependências

- Gradle Kotlin DSL
- Lombok

### Ferramentas planejadas

- Docker
- React
- React Native

## 📁 Estrutura do Repositório

```text
Sistema-de-Barbearia/
├── src/
│   ├── main/
│   │   ├── java/com/seuapp/
│   │   └── resources/
│   └── test/
├── build.gradle.kts
├── gradlew
├── gradlew.bat
└── README.md
```

## 🧱 Arquitetura

O projeto segue uma arquitetura em camadas parcial, separando responsabilidades principais entre controllers, services, repositories, models, DTOs, configurações e tratamento de exceções.

```text
src/main/java/com/seuapp
├── config
├── controller
├── dto
├── exception
├── model
├── repository
├── service
└── BarbeariaApplication.java
```

Fluxo simplificado da arquitetura:

```text
Cliente HTTP / Frontend futuro
↓
Spring Security + JWT
↓
Controllers
↓
Services
↓
Repositories
↓
PostgreSQL
```

Esse fluxo representa como uma requisição percorre a API: ela entra pela camada de segurança, passa pelos controllers, segue para as regras de negócio nos services, acessa os repositories e, por fim, persiste ou consulta dados no PostgreSQL.

Função das principais camadas:

- `controller`: expõe os endpoints REST da API.
- `service`: concentra regras de negócio e serviços auxiliares, como autenticação, JWT e agendamento.
- `repository`: interfaces Spring Data JPA para acesso ao banco de dados.
- `model`: entidades JPA persistidas no banco.
- `dto`: objetos usados para transportar dados em respostas e autenticação.
- `config`: configurações de segurança, CORS e filtros.
- `exception`: tratamento centralizado parcial de erros da API.

## 🧩 Modelagem Principal

Entidades principais:

- `Usuario`
- `Servico`
- `Agendamento`

Relacionamentos principais:

- Um `Agendamento` possui um cliente, representado por `Usuario`.
- Um `Agendamento` possui um barbeiro, também representado por `Usuario`.
- Um `Agendamento` possui um serviço, representado por `Servico`.

## 🔗 Endpoints Principais

### Autenticação

| Método | Rota | Descrição | Acesso |
| --- | --- | --- | --- |
| `POST` | `/login` | Autentica o usuário e retorna um token JWT | Público |

### Usuários

| Método | Rota | Descrição | Acesso |
| --- | --- | --- | --- |
| `GET` | `/usuarios` | Lista usuários de forma paginada, com filtro opcional por nome | Protegido |
| `POST` | `/usuarios` | Cadastra um novo usuário | Público |
| `GET` | `/usuarios/{id}` | Busca um usuário por ID | Protegido |
| `PUT` | `/usuarios/{id}` | Atualiza um usuário existente | Protegido |
| `DELETE` | `/usuarios/{id}` | Remove um usuário | Protegido/Admin |

### Serviços

| Método | Rota | Descrição | Acesso |
| --- | --- | --- | --- |
| `GET` | `/servicos` | Lista serviços de forma paginada, com filtro opcional por nome | Protegido |
| `POST` | `/servicos` | Cadastra um novo serviço | Protegido |
| `GET` | `/servicos/{id}` | Busca um serviço por ID | Protegido |
| `PUT` | `/servicos/{id}` | Atualiza um serviço existente | Protegido |
| `DELETE` | `/servicos/{id}` | Remove um serviço | Protegido |

### Agendamentos

| Método | Rota | Descrição | Acesso |
| --- | --- | --- | --- |
| `GET` | `/agendamentos` | Lista agendamentos de forma paginada, com filtros opcionais por barbeiro ou cliente | Protegido |
| `POST` | `/agendamentos` | Cria um novo agendamento aplicando regras básicas de validação | Protegido |
| `GET` | `/agendamentos/{id}` | Busca um agendamento por ID | Protegido |
| `PUT` | `/agendamentos/{id}` | Atualiza um agendamento existente | Protegido |
| `DELETE` | `/agendamentos/{id}` | Remove um agendamento | Protegido |

## 🔐 Segurança

A autenticação é feita com Spring Security e JWT.

Fluxo básico:

1. O usuário realiza login em `POST /login`.
2. A API retorna um token JWT.
3. O token deve ser enviado nas próximas requisições pelo header:

```http
Authorization: Bearer <token>
```

Rotas públicas:

- `POST /login`
- `POST /usuarios`

Demais rotas exigem autenticação via JWT. A rota `DELETE /usuarios/{id}` exige perfil administrativo.

Perfis atuais:

- `BARBEIRO` -> `ROLE_ADMIN`
- `CLIENTE` -> `ROLE_USUARIO`

> Observação: no estado atual, é recomendável revisar o cadastro público de usuários para evitar que qualquer pessoa escolha o perfil `BARBEIRO` e receba permissões administrativas.

O controle por roles ainda é parcial e deve ser evoluído nas próximas etapas.

## 🗄️ Banco de Dados

- Banco usado: PostgreSQL.
- Ambiente usado: Neon.
- Persistência com JPA/Hibernate.
- Configuração atual de schema: `spring.jpa.hibernate.ddl-auto=update`.
- Ainda não há migrations com Flyway ou Liquibase.

## ⚙️ Configuração Local

### Pré-requisitos

- Java 25 instalado.
- Gradle Wrapper disponível no projeto.
- PostgreSQL local ou conta no Neon.

Use variáveis de ambiente para configurar credenciais e segredos. Evite manter dados sensíveis diretamente no `application.properties`.

O arquivo `src/main/resources/application-example.properties` contém valores fictícios para consulta. Não copie credenciais reais para arquivos versionados.

Variáveis obrigatórias:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
API_SECURITY_TOKEN_SECRET
```

Variáveis opcionais:

```text
SPRING_PROFILES_ACTIVE=dev
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=true
```

Exemplo seguro de configuração:

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}

api.security.token.secret=${API_SECURITY_TOKEN_SECRET}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

## 🚀 Como Rodar

Clone o repositório:

```bash
git clone https://github.com/EnioJr18/Sistema-de-Barbearia.git
cd Sistema-de-Barbearia
```

No Windows PowerShell:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/barbearia"
$env:SPRING_DATASOURCE_USERNAME="barbearia_user"
$env:SPRING_DATASOURCE_PASSWORD="troque-esta-senha"
$env:API_SECURITY_TOKEN_SECRET="troque-este-segredo-jwt-com-valor-longo-e-aleatorio"
$env:SPRING_PROFILES_ACTIVE="dev"
.\gradlew.bat bootRun
```

No Linux, macOS ou Git Bash:

```bash
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/barbearia"
export SPRING_DATASOURCE_USERNAME="barbearia_user"
export SPRING_DATASOURCE_PASSWORD="troque-esta-senha"
export API_SECURITY_TOKEN_SECRET="troque-este-segredo-jwt-com-valor-longo-e-aleatorio"
export SPRING_PROFILES_ACTIVE="dev"
./gradlew bootRun
```

Por padrão, a API fica disponível em:

```text
http://localhost:8080
```

## Swagger / OpenAPI

Com a aplicação em execução, a documentação interativa fica disponível em:

```text
http://localhost:8080/swagger-ui.html
```

A especificação OpenAPI em JSON fica disponível em:

```text
http://localhost:8080/v3/api-docs
```

Para testar endpoints protegidos no Swagger:

1. Faça login em `POST /login`.
2. Copie o token JWT retornado.
3. Clique em `Authorize` no Swagger UI.
4. Informe o token no campo Bearer/JWT.

Use o token puro se a UI já aplicar o esquema Bearer automaticamente. Se necessário, informe no formato:

```text
Bearer <token>
```

## 🧪 Testes

O projeto ainda possui testes mínimos. A cobertura automatizada deve ser ampliada para controllers, services, repositories, segurança e regras de agendamento.

No Windows:

```powershell
.\gradlew.bat test
```

No Linux, macOS ou Git Bash:

```bash
./gradlew test
```

## ⚠️ Limitações Atuais

- Este repositório ainda não inclui frontend Web ou Mobile.
- Docker e Docker Compose ainda não estão implementados.
- A suíte de testes ainda é básica.
- O controle de autorização por perfil ainda é parcial.
- A agenda ainda não calcula horários disponíveis com base na duração do serviço.
- A validação de conflito verifica apenas o mesmo barbeiro no mesmo horário exato.
- Credenciais e segredos devem ser removidos do `application.properties` e migrados para variáveis de ambiente.

## 👨‍💻 Autores

### Backend/API

Desenvolvido por **Enio Jr.**

- E-mail: eniojr100@gmail.com
- LinkedIn: https://www.linkedin.com/in/enioeduardojr
- Instagram: https://www.instagram.com/enio_juniorrr
- Site Portfólio: https://eniojr18.github.io

### Frontend Web/Mobile

Planejado para uma etapa futura.

---

Projeto desenvolvido para fins de estudo, prática backend e portfólio.
