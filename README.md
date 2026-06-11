# ⚙️ SGAC - API (Back-end)

API RESTful desenvolvida para o **Sistema de Gestão de Atividades Complementares (SGAC)**. Este projeto é o motor por trás da plataforma acadêmica, responsável por gerenciar usuários, validar regras de cursos e processar o envio de comprovantes de horas complementares.

## 🚀 Tecnologias Utilizadas
* **Linguagem:** Java 21
* **Framework:** Spring Boot
* **Persistência:** Spring Data JPA / Hibernate
* **Banco de Dados:** MySQL (Hospedado no Railway)
* **Envio de E-mails:** Spring Mail + Mailtrap SMTP
* **Documentação:** Swagger / OpenAPI 3
* **Containerização e Deploy:** Docker (Multi-stage build) & Render

## 📦 Funcionalidades Principais
* Autenticação e controle de acesso por perfis (Aluno, Coordenador, Admin).
* Upload de arquivos (comprovantes e certificados) com limite otimizado para 20MB.
* Disparo de e-mails transacionais (ex: aprovação/rejeição de horas).
* Validação automática de regras e limites de carga horária por área.

## 🛠️ Como rodar o projeto localmente

1. **Clone o repositório:**
   ```bash
   git clone [https://github.com/SeuUsuario/repositorio-do-PI---Back-end.git](https://github.com/SeuUsuario/repositorio-do-PI---Back-end.git)
