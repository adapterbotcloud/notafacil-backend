# 📝 NotaFácil - Backend

Sistema de emissão de **NFS-e** (Nota Fiscal de Serviço Eletrônica) para a prefeitura de **Fortaleza/CE** via GINFES.

## 🚀 Tecnologias

- **Java 21** + **Spring Boot 3.4.5**
- **Spring Security** + **JWT** (autenticação stateless)
- **JPA/Hibernate** + **PostgreSQL**
- **Jetty** (servidor HTTP)
- **SOAP Client** (comunicação com SEFIN Fortaleza / GINFES)
- **Azure Key Vault** (armazenamento de certificados digitais A1)
- **XML Digital Signature** (assinatura de NFS-e)
- **Swagger/OpenAPI** (documentação da API)

## ⚙️ Configuração

### Variáveis de Ambiente (`.env`)

```bash
AZURE_KEYVAULT_URL=https://notafacil.vault.azure.net/
AZURE_CLIENT_ID=<seu-client-id>
AZURE_CLIENT_SECRET=<seu-client-secret>
AZURE_TENANT_ID=<seu-tenant-id>
```

## 🔐 Perfis de Acesso

| Perfil | Descrição |
|--------|----------|
| `ADMIN` | Acesso total — gerencia todos os usuários, empresas e certificados |
| `GESTOR` | Gerencia usuários do mesmo CNPJ + importa certificado digital |
| `USER` | Upload, emissão de RPS, consulta de status e certificado (somente leitura) |

## 📡 Endpoints Principais

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/auth/login` | Login (retorna JWT) |
| `POST` | `/api/v1/nfse/emitir-rps-teste` | Emite RPS (CNPJ do JWT) |
| `POST` | `/api/v1/nfse/reenviar-pendentes` | Reenvia RPS Pendente/Falha |
| `GET` | `/api/v1/rps?ano=&mes=` | Listar RPS por competência |
| `GET` | `/api/v1/rps/resumo` | Resumo por ano/mês |
| `GET` | `/api/v1/rps/job-status` | Status do job de protocolos |
| `GET` | `/certificates/check/{name}` | Status do certificado (todos) |
| `POST` | `/certificates/import` | Importar certificado (ADMIN/GESTOR) |
| `CRUD` | `/api/v1/usuarios` | Gestão de usuários (ADMIN/GESTOR) |

## 🗄️ Modelo de Dados

### Tabela `empresa`

Campos: `cnpj`, `razao_social`, `inscricao_municipal`, `endereco`, `numero`, `complemento`, `bairro`, `cep`, `telefone`, `substituto_tributario`, `aliquota`, `item_lista_servico`, `codigo_tributacao_municipio`, `codigo_municipio`, `regime_especial_tributacao`, `optante_simples_nacional`, `incentivador_cultural`

### Tabela `rps`

Status: `0`=Pendente, `1`=Enviando, `2`=Enviado, `3`=Falha, `4`=Processado

Deduplicação por `id_cobranca`. Competência via `mes_cobranca`/`ano_cobranca`.

### Tabela `usuarios`

Campos: `username`, `password` (BCrypt), `nome`, `cnpj`, `role`

## 🔄 Job de Consulta de Protocolos

`ConsultaProtocoloJob` executa a cada **3 minutos**:
- Busca protocolos com status "Enviado"
- Consulta situação na SEFIN via SOAP assinado
- Atualiza para Processado (4) ou Falha (3)

## 🔏 Assinatura Digital

- Certificados A1 no **Azure Key Vault** (alias `CNPJ{cnpj}`)
- XML assinado com **SHA-256 + RSA** (enveloped)
- Namespaces limpos via `XmlNamespaceCleaner`
- Valores: 2 decimais (monetários), 4 decimais (alíquota)
- `dataEmissao`: `now() - 1 min` (evita rejeição SEFIN)
- Cabeçalho GINFES v3 no envelope SOAP

## 🌐 Deploy

- **URL**: `https://notafacil-api.adapterbot.cloud`
- **Porta**: 8081
- **Serviço**: `systemctl restart notafacil-backend`
- **Proxy**: Caddy reverse proxy

## 📌 Versão: v1.3.0

- **v1.3.0** — Campos empresa expandidos, CabecalhoDto XML, Job com assinatura digital e namespaces, signXmlWithAlias, certificado visível para USER, layout responsivo
- **v1.2.0** — Deduplicação idCobranca, Azure .env, KeyVault fallback
- **v1.1.0** — Perfil GESTOR, filtro competência, PDF
- **v1.0.0** — MVP JWT + emissão RPS
