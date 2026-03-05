# 📝 NotaFácil - Backend

Sistema de emissão de **NFS-e** (Nota Fiscal de Serviço Eletrônica) para a prefeitura de **Fortaleza/CE**.

## 🚀 Tecnologias

- **Java 21** + **Spring Boot 3.4.5**
- **Spring Security** + **JWT** (autenticação stateless)
- **JPA/Hibernate** + **PostgreSQL**
- **Jetty** (servidor HTTP)
- **SOAP Client** (comunicação com SEFIN Fortaleza)
- **Swagger/OpenAPI** (documentação da API)
- **Maven** (build)

## 📋 Pré-requisitos

- Java 21+
- Maven 3.9+
- PostgreSQL 16+

## ⚙️ Configuração

### Banco de Dados

```sql
CREATE DATABASE rpsdb;
```

### application.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/rpsdb
    username: postgres
    password: 123
  jpa:
    hibernate:
      ddl-auto: update
```

### Cadastro inicial de empresa

```sql
INSERT INTO empresa (
  cnpj, inscricao_municipal, regime_especial_tributacao,
  optante_simples_nacional, incentivador_cultural,
  item_lista_servico, codigo_tributacao_municipio,
  codigo_municipio, aliquota
) VALUES (
  '27288254000103', '469159', 1, 1, 2,
  '8.02', '859960401', '2304400', 0.05
);
```

## 🏃 Executando

```bash
# Build
mvn clean package -DskipTests

# Executar
java -jar target/notafacil-1.0.0.jar

# Ou via Maven
mvn spring-boot:run
```

O servidor sobe na porta **8081** por padrão.

## 🔐 Autenticação

O sistema usa **JWT (JSON Web Token)** para autenticação.

### Usuário padrão

| Campo | Valor |
|-------|-------|
| Usuário | `admin` |
| Senha | `admin123` |
| Perfil | `ADMIN` |
| CNPJ | `27288254000103` |

> ⚠️ **Troque a senha padrão em produção!**

### Login

```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Resposta:
```json
{
  "token": "eyJhbG...",
  "username": "admin",
  "nome": "Administrador",
  "role": "ADMIN",
  "cnpj": "27288254000103"
}
```

### Usando o token

```bash
curl -H "Authorization: Bearer <token>" http://localhost:8081/api/v1/nfse/...
```

## 📡 Endpoints

### Autenticação (públicos)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/auth/login` | Login — retorna token JWT |
| `GET` | `/auth/me` | Dados do usuário logado |

### NFS-e (autenticados)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/api/v1/nfse/recepcionar-lote-rps` | Envia lote de RPS para a prefeitura |
| `GET` | `/api/v1/nfse/consulta-situacao-lote-rps/{protocolo}` | Consulta situação do lote |
| `GET` | `/api/v1/nfse/consulta-lote-rps/{protocolo}` | Consulta detalhes do lote |
| `POST` | `/api/v1/nfse/emitir-rps` | Emite RPS em lotes (assíncrono) |
| `POST` | `/api/v1/nfse/emitir-rps-teste` | Emite RPS síncrono (usa CNPJ do JWT) |
| `POST` | `/api/v1/nfse/rps-existentes` | Verifica quais cobranças já possuem RPS |

### Usuários (somente ADMIN)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/api/v1/usuarios` | Listar usuários |
| `GET` | `/api/v1/usuarios/{id}` | Buscar usuário por ID |
| `POST` | `/api/v1/usuarios` | Criar usuário |
| `PUT` | `/api/v1/usuarios/{id}` | Atualizar usuário |
| `DELETE` | `/api/v1/usuarios/{id}` | Remover usuário |

### Certificado Digital

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/certificates/import` | Importar certificado (.p12/.pfx) |

### Monitoramento (públicos)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/actuator/health` | Status da aplicação |
| `GET` | `/swagger-ui.html` | Documentação interativa (Swagger) |

## 🗄️ Modelo de Dados

### Tabela `empresa`

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id` | BIGINT | Chave primária |
| `cnpj` | VARCHAR(14) | CNPJ da empresa (único) |
| `inscricao_municipal` | VARCHAR(20) | Inscrição municipal |
| `regime_especial_tributacao` | INTEGER | Regime de tributação |
| `optante_simples_nacional` | INTEGER | Optante pelo Simples |
| `incentivador_cultural` | INTEGER | Incentivador cultural |
| `item_lista_servico` | VARCHAR(10) | Item da lista de serviços |
| `codigo_tributacao_municipio` | VARCHAR(20) | Código de tributação |
| `codigo_municipio` | VARCHAR(7) | Código IBGE do município |
| `aliquota` | DECIMAL(10,6) | Alíquota ISS |

### Tabela `rps`

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id` | BIGINT | Chave primária |
| `empresa_id` | BIGINT | FK → empresa |
| `id_cobranca` | BIGINT | ID da cobrança na planilha (deduplicação) |
| `request_id` | BIGINT | ID de referência do cliente |
| `numero` | VARCHAR(30) | Número do RPS |
| `serie` | VARCHAR(10) | Série do RPS |
| `valor_servicos` | DECIMAL(13,2) | Valor dos serviços |
| `tomador_cpf` | VARCHAR(14) | CPF do tomador |
| `tomador_razao_social` | VARCHAR(115) | Nome do tomador |
| `discriminacao` | VARCHAR(2000) | Descrição do serviço |
| `status` | INTEGER | 0=Pendente, 1=Enviando, 2=Enviado, 3=Falha |
| `protocolo` | VARCHAR(60) | Protocolo da prefeitura |
| `mensagem_erro` | VARCHAR(2000) | Mensagem de erro |
| `created_at` | TIMESTAMP | Data de criação |

### Tabela `usuarios`

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id` | BIGINT | Chave primária |
| `username` | VARCHAR (único) | Login do usuário |
| `password` | VARCHAR | Senha (BCrypt) |
| `nome` | VARCHAR | Nome completo |
| `cnpj` | VARCHAR(14) | CNPJ da empresa vinculada |
| `role` | VARCHAR | Perfil: `ADMIN` ou `USER` |

## 📁 Estrutura do Projeto

```
src/main/java/br/com/notafacil/
├── config/          # DataLoader, configurações
├── controller/      # AuthController, NfseController, UsuarioController
├── dto/             # DTOs (request/response)
├── entity/          # Entidades JPA (Empresa, RPS, Usuario)
├── repository/      # Repositórios Spring Data
├── schemas/         # Schemas SOAP (NFS-e)
├── security/        # JWT, Filter, SecurityConfig
├── service/         # Lógica de negócio (NfseService, NfseService1)
└── strategy/        # Estratégias de alias de certificado
```

## 🌐 Deploy

### Systemd

```bash
# Criar serviço
sudo nano /etc/systemd/system/notafacil-backend.service

# Conteúdo:
[Unit]
Description=NotaFacil Backend
After=network.target postgresql.service

[Service]
Type=simple
WorkingDirectory=/root/notafacil-backend
ExecStart=/usr/bin/java -jar target/notafacil-1.0.0.jar
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target

# Ativar e iniciar
sudo systemctl daemon-reload
sudo systemctl enable notafacil-backend
sudo systemctl start notafacil-backend
```

### Caddy (reverse proxy)

```
notafacil-api.adapterbot.cloud {
    reverse_proxy localhost:8081
}
```

## 📌 Versão

**v1.0.0** — MVP com autenticação JWT, emissão de RPS e deduplicação por idCobranca.

## 📄 Licença

Projeto privado — uso interno.
