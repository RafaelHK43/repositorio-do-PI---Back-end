# Sistema AC - Back-end

Repositório do back-end do projeto de Atividades Complementares (Sistema AC).

## Stack do Projeto

- Java 21
- Spring Boot 3
- MySQL

---

## 🛠️ Configuração do Ambiente

O projeto utiliza **variáveis de ambiente** para proteger credenciais sensíveis como senhas do banco de dados e chaves de acesso a serviços externos. O arquivo `.env` fica fora do controle de versão (gitignore).

### Passo 1: Clonar o Repositório

```bash
git clone https://github.com/seu-usuario/repositorio-do-PI---Back-end.git
cd repositorio-do-PI---Back-end/sistema-ac
```

### Passo 2: Criar o Arquivo `.env`

Na raiz da pasta `sistema-ac`, crie um arquivo chamado `.env` copiando o template:

```bash
cp .env.example .env
```

### Passo 3: Preenchimento das Credenciais

Abra o arquivo `.env` e preencha com seus valores locais:

```env
# Credenciais do MySQL local
DB_PASSWORD=sua_senha_do_mysql

# Senha de Aplicativo do Gmail
EMAIL_PASSWORD=sua_senha_de_aplicativo_gmail
```

#### Obtendo a Senha de Aplicativo do Gmail

1. Acesse [Google App Passwords](https://myaccount.google.com/apppasswords)
2. Selecione "Mail" e "Windows Computer" (ou seu SO)
3. Clique em "Gerar"
4. Copie a senha gerada (sem espaços) e cole no `.env`

⚠️ **Importante:** Nunca compartilhe ou versione o arquivo `.env`. Ele está no `.gitignore` por razão de segurança.

---

## Para Desenvolvedores Back-end

### Configuração do application.properties

O arquivo `sistema-ac/src/main/resources/application.properties` já está configurado para ler as variáveis de ambiente:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sistema_ac?createDatabaseIfNotExist=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD}
```

**As credenciais são carregadas do arquivo `.env` através da dependência `spring-dotenv`.**

Detalhes importantes:

- A flag `createDatabaseIfNotExist=true` permite criar o banco automaticamente na primeira execução.
- A URL padrão usa a porta `3306` e o schema `sistema_ac`.
- A variável `${DB_PASSWORD}` é substituída pelo valor definido no arquivo `.env`.

### Regras de validação de horas

As regras principais de negócio já estão implementadas em `sistema-ac/src/main/java/br/edu/senac/sistema_ac/service/ValidacaoHorasService.java`:

- Limite total de 120 horas por curso.
- Limites específicos por área, conforme `RegraAtividade` cadastrada.

### Persistência local de comprovantes

Os arquivos de comprovante são persistidos localmente na pasta:

- `uploads/comprovantes`

Garanta permissões de escrita no ambiente de execução para evitar falha no upload.

## Para Desenvolvedores Front-end (PWA e Mobile)

### API centralizada REST

A integração deve ser feita por uma API centralizada seguindo padrão REST, facilitando consumo por PWA e aplicativo mobile com o mesmo contrato.

### Coleção Postman com exemplos JSON

Os exemplos de requisição e payloads JSON estão na coleção:

- `sistema-ac/postman/Sistema-AC-PI3.postman_collection.json`

### Foco da Entrega 1 (27/04)

Para a Entrega 1, priorizar endpoints de:

- Curso
- RegraAtividade

Esse escopo atende o PWA da coordenação para operação inicial.

### Usuários seed para login imediato

Use os usuários abaixo para validar autenticação sem cadastro manual:

- coordenador@senac.br / 123456
- aluno@senac.br / 123456

## Documentação Viva (Swagger)

Assim que o projeto iniciar, o Swagger UI fica disponível em:

- `/swagger-ui.html`

Com isso, novas rotas podem ser exploradas e testadas rapidamente sem depender de implementação no front-end.

## Responsáveis pelo Back-end

- [Bernardo dos Santos](https://github.com/7BZN)
- [Gustavo Moura](https://github.com/gustavomouradevbr)
- [Rafael Henrique](https://github.com/RafaelHK43)
