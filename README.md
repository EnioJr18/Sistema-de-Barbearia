# 💈 Sistema de Gerenciamento de Barbearia 

![Status](https://img.shields.io/badge/Status-Em_Desenvolvimento-yellow)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![React](https://img.shields.io/badge/React-19-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)

## 📖 Sobre o Projeto

Este é um sistema Fullstack completo para o gerenciamento de serviços, usuários e agendamentos de uma barbearia/clínica. O projeto nasceu de uma arquitetura colaborativa e está dividido em dois microssistemas independentes que se comunicam via API REST.

- **Back-end (API):** Construído para ser um "cofre" seguro e escalável. Responsável por blindar as regras de negócio, gerenciar o banco de dados e garantir que as informações só cheguem a quem tem o nível de acesso correto.
- **Front-end (SPA):** Construído para ser a interface interativa do cliente e da administração. Responsável por consumir a API de forma inteligente e apresentar os dados com uma experiência de usuário fluida. *(Em desenvolvimento por David) Gabriel*.
---

## ✨ Funcionalidades

### 📱 1. O App Mobile (Visão do Cliente)
O foco do aplicativo que o David Gabriel vai fazer no React Native tem que ser a velocidade. O cliente quer marcar o corte em 3 cliques.

* **Catálogo de Serviços:** Uma tela visual mostrando os cortes, barbas, combos, com descrições e preços.
* **Seleção de Profissional:** O cliente pode escolher o seu barbeiro favorito ou selecionar "Qualquer um disponível" (isso rende uma lógica bem legal no backend!).
* **Motor de Agendamento (O Core):** Um calendário onde o cliente clica no dia e o app mostra apenas os horários que estão realmente vagos para aquele barbeiro.
* **Histórico e Status:** Uma área onde o cliente vê os cortes futuros (podendo cancelar com antecedência) e os cortes passados.
* **Notificações (Futuro):** Receber um aviso de "Seu corte é daqui a 1 hora!".

### 💻 2. O Painel Web (Visão do Barbeiro / Admin)
Aqui é o sistema de gestão que vai rodar no computador ou tablet da barbearia.

* **Dashboard de Agenda (Kanban ou Calendário):** A tela principal onde o barbeiro vê todos os clientes do dia. Ele clica no cliente que acabou de sentar na cadeira e muda o status de ```PENDENTE``` para ```CONCLUIDO```.
* **CRUD de Gestão:** Telas para cadastrar novos serviços, mudar o preço de um corte, ou cadastrar um novo barbeiro que entrou na equipe.
* **Fechamento de Caixa:** Como você inteligentemente colocou os atributos ```preco``` e ```formaDePagamento``` no model de Agendamento, o sistema web pode ter uma tela que soma automaticamente quanto a barbearia faturou no dia e separa o que foi no PIX, Dinheiro ou Cartão.

### ⚙️ 3. O "Motor" no Backend (Onde você vai brilhar)
A beleza de separar o Front do Back é que o Deivinho não vai precisar calcular nada disso. Ele só vai pedir os dados para a sua API, e o seu código Java fará o trabalho pesado. Suas principais funcionalidades invisíveis serão:

* **Algoritmo de Horários Disponíveis:** Essa é a funcionalidade mais desafiadora e legal de programar. Quando o app pedir os horários do dia 15, o seu código vai ter que:
      1. Ver a hora que a barbearia abre e fecha (ex: 09h às 18h).
      2. Pegar a ```duracaoEmMinutos``` do serviço escolhido.
      3. Olhar no banco de dados os ```Agendamentos``` que já existem para aquele dia.
      4. Subtrair os horários ocupados e devolver para o Front apenas os blocos de tempo que sobraram.
* **Proteção contra Concorrência:** Se dois clientes abrirem o app ao mesmo tempo e tentarem marcar sexta-feira às 19h com o mesmo barbeiro, a sua API tem que deixar o primeiro passar e travar o segundo, avisando que o horário acabou de ser preenchido.
* **Segurança de Rotas:** Garantir, usando o Token JWT, que um cliente comum não consiga mandar uma requisição para a rota de deletar um serviço ou ver o faturamento do dia.

---

## 🚀 Tecnologias Utilizadas (Stack)

A API foi construída com foco em escalabilidade e segurança, servindo como a única fonte de verdade para as plataformas Web e Mobile.
* **Java 25 + Spring Boot** 
* **PostgreSQL** (Hospedado via Neon - *Serverless*).
* **Spring Security + JWT (JSON Web Tokens):** Para autenticação e autorização de rotas protegidas.
* **Swagger/OpenAPI (springdoc-openapi):** Para documentação interativa dos endpoints, facilitando a integração com o frontend.
* **Docker:** Para conteinerização e padronização do ambiente.
* **Hospedagem:** Deploy planejado via Render ou Railway.

### Frontend (Web)
Painel administrativo focado na experiência do barbeiro e do dono do estabelecimento.
* **React + Vite**
* **TailwindCSS & shadcn/ui:** Para componentização e estilização rápida e profissional.
* **Hospedagem:** Deploy planejado via Vercel ou Netlify.

### Frontend (Mobile)
Aplicativo focado na experiência do cliente final para agendamento rápido.
* **React Native**
* **Expo:** Para abstração de configurações nativas e testes simplificados.

---

## ⚙️ Como rodar o Backend localmente

### Pré-requisitos
* Java 25 instalado.
* Conta no Neon.tech com um banco de dados PostgreSQL criado.

### Passos para Execução
1. Clone o repositório:
   ```bash
   git clone https://github.com/EnioJr18/Sistema-de-Barbearia.git
   ```

2. Configure as credenciais no arquivo ```src/main/resources/application.properties```:
```bash
spring.datasource.url=jdbc:postgresql://[SEU-HOST-NEON]/[NOME-DO-BANCO]?sslmode=require
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
```

3. Execute o servidor via Gradle:
```bash
./gradlew bootRun
```

---

## 📄 Licença
Este projeto está sob a licença MIT. Veja o arquivo LICENSE para mais detalhes.


## 🤝 Contribuição
Contribuições são bem-vindas! Sinta-se à vontade para abrir issues ou enviar pull requests.


## 👨‍💻 Autores

O desenvolvimento está dividido em duas frentes principais:

* **Backend (API e Banco de Dados):** Desenvolvido por Enio Jr.
* **Frontend (Web e Mobile):** Desenvolvido por Daivid Gabriel.

Desenvolvido para fins de estudo e portfólio 💻

Entre em contato com Enio Jr. para dúvidas, sugestões ou colaborações futuras: <br>
📧 E-mail: eniojr100@gmail.com <br>
🔗 LinkedIn: https://www.linkedin.com/in/enioeduardojr <br>
📷 Instagram: https://www.instagram.com/enio_juniorrr <br>

Entre em contato com David Gabriel para dúvidas, sugestões ou colaborações futuras: <br>
📧 E-mail: davidglm.trabalho@gmail.com <br>
🔗 LinkedIn: https://www.linkedin.com/in/davidgabriellm <br>
📷 Instagram: https://www.instagram.com/davinho_glm <br>