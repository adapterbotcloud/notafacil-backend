# Briefing Frontend — Arquitetura Multi-Provedor NFS-e

**Data:** 2026-05-04
**Backend Branch:** `development`
**Commits:** `590e875`, `26e998b`, `313dba0`

---

## Resumo Executivo

O backend implementou uma arquitetura multi-provedor que roteia automaticamente a emissão de NFS-e para o provedor correto (GINFES, XTR/SA, Padrão Nacional) baseado no código IBGE do município da empresa. Os endpoints V1 **continuam funcionando sem alteração**, mas o frontend deve migrar para os V2 e adaptar os formulários de empresa para usar as novas tabelas de referência.

---

## 1. Novo Endpoint de Emissão (V2)

### Migrar de V1 para V2

| Ação | V1 (atual) | V2 (novo) |
|------|-----------|-----------|
| Emitir RPS | `POST /api/v1/nfse/emitir-rps-teste` | `POST /api/v2/nfse/emitir` |
| Reenviar pendentes | `POST /api/v1/nfse/reenviar-pendentes` | `POST /api/v2/nfse/reenviar-pendentes` |

### O que muda

- **URL** — apenas o path muda
- **Body** — **idêntico**, mesmo `EmitirNotaMinRequest`
- **Headers** — mesmos (`Authorization: Bearer`, `X-Empresa-CNPJ` como fallback)
- **Response** — mesmo `EmitirRpsResponse` (`{ rpsIds, protocolos }`)

### O que o V2 faz de diferente (transparente para o frontend)

- Roteia automaticamente para o provedor correto baseado no `codigoMunicipio` da empresa
- Determina o regime tributário (ISS ou IBS/CBS)
- Salva `providerId`, `regimeTributario`, `versaoSchema` no RPS
- Controla tentativas de envio (`tentativasEnvio`)

### Alteração em `src/services/api.ts`

```typescript
// ANTES
export async function emitirRpsTeste(cnpj: string, listaRps: any[]) {
  const res = await fetch(`${API_BASE}/nfse/emitir-rps-teste`, {
    method: 'POST',
    headers: {
      ...authHeaders(),
      'X-Empresa-CNPJ': cnpj.replace(/\D/g, ''),
    },
    body: JSON.stringify({ listaRps }),
  });
  return res.json();
}

// DEPOIS
const API_V2 = API_BASE.replace('/v1', '/v2');

export async function emitirRpsTeste(cnpj: string, listaRps: any[]) {
  const res = await fetch(`${API_V2}/nfse/emitir`, {
    method: 'POST',
    headers: {
      ...authHeaders(),
      'X-Empresa-CNPJ': cnpj.replace(/\D/g, ''),
    },
    body: JSON.stringify({ listaRps }),
  });
  return res.json();
}
```

**O endpoint V1 continua funcionando** — a migração pode ser gradual.

---

## 2. Novos Campos na Empresa

### 2.1 Atualizar `EmpresaDTO`

```typescript
export interface EmpresaDTO {
  // ... campos existentes (sem alteração) ...

  // v4 IBS/CBS — AGORA SÃO REFERÊNCIAS A TABELAS (antes eram strings livres)
  nbs?: string;               // código de 9 dígitos (FK → tabela nbs)
  indicadorOperacao?: string;  // código de 6 dígitos (FK → tabela indicador_operacao)
  cst?: string;                // código de 3 dígitos (FK → tabela cst_ibs_cbs)
  classificacaoTributaria?: string; // código de 6 dígitos (FK → tabela classificacao_tributaria)

  // NOVOS CAMPOS
  regimeTributarioPreferido?: string;  // "ISS" ou "IBS_CBS" (default: "ISS")
  ibsCbsHabilitado?: boolean;          // true/false (default: false)
}
```

### 2.2 Novos campos no formulário de empresa

Adicionar na seção "Tributação" ou criar nova seção "Reforma Tributária":

| Campo | Nome | Tipo | Default | Descrição |
|-------|------|------|---------|-----------|
| Regime Tributário | `regimeTributarioPreferido` | Select | `"ISS"` | Opções: `ISS`, `IBS_CBS` |
| IBS/CBS Habilitado | `ibsCbsHabilitado` | Switch/Toggle | `false` | Habilita regime IBS/CBS |

### 2.3 Campos v4 agora usam Select com dados da API

**ANTES:** campos de texto livre (input text)
**DEPOIS:** Select/Autocomplete populados via API de lookup

| Campo | Antes | Depois | Endpoint para popular |
|-------|-------|--------|----------------------|
| NBS | `<Input>` | `<Select>` ou `<AutoComplete>` | `GET /api/v1/lookup/nbs` |
| CST | `<Select>` hardcoded | `<Select>` dinâmico | `GET /api/v1/lookup/cst` |
| Classif. Tributária | `<Input>` | `<Select>` | `GET /api/v1/lookup/classificacao-tributaria` |
| Ind. Operação | `<Input>` | `<Select>` | `GET /api/v1/lookup/indicador-operacao` |

---

## 3. Endpoints de Lookup (NOVOS)

Todos sob `GET /api/v1/lookup/` — sem autenticação necessária.

### 3.1 NBS (Nomenclatura Brasileira de Serviços)

```
GET /api/v1/lookup/nbs
→ Lista todos os NBS habilitados
→ Response: [{ id, codigo, descricao, secao, habilitado }]

GET /api/v1/lookup/nbs/{codigo}
→ Busca NBS por código exato
→ Response: { id, codigo, descricao, secao, habilitado }

GET /api/v1/lookup/nbs/busca?q=software
→ Busca NBS por termo na descrição (case insensitive)
→ Response: [{ id, codigo, descricao, secao, habilitado }]
```

### 3.2 CST IBS/CBS

```
GET /api/v1/lookup/cst
→ Lista todos os CST habilitados
→ Response: [{ id, codigo, descricao, grupo, habilitado }]

GET /api/v1/lookup/cst/{codigo}
→ Response: { id, codigo, descricao, grupo, habilitado }
```

**Códigos disponíveis:** 101, 102, 103, 104, 105, 201, 202, 301, 302, 401, 501, 900

### 3.3 Classificação Tributária

```
GET /api/v1/lookup/classificacao-tributaria
→ Response: [{ id, codigo, descricao, grupo, habilitado }]

GET /api/v1/lookup/classificacao-tributaria/{codigo}
→ Response: { id, codigo, descricao, grupo, habilitado }
```

### 3.4 Indicador de Operação

```
GET /api/v1/lookup/indicador-operacao
→ Response: [{ id, codigo, descricao, habilitado }]

GET /api/v1/lookup/indicador-operacao/{codigo}
→ Response: { id, codigo, descricao, habilitado }
```

### 3.5 Correlação LC 116 (Auto-preenchimento)

```
GET /api/v1/lookup/correlacao
→ Lista todas as correlações
→ Response: [{ id, itemListaServico, descricaoLc116, nbsCodigo,
               indicadorOperacaoCodigo, classificacaoTributariaCodigo,
               cstPadrao, habilitado }]

GET /api/v1/lookup/correlacao/{itemListaServico}
→ Retorna a correlação para um item específico da LC 116
→ Response: {
    "itemListaServico": "01.01",
    "descricaoLc116": "Análise e desenvolvimento de sistemas",
    "nbsCodigo": "108011100",
    "indicadorOperacaoCodigo": "030102",
    "classificacaoTributariaCodigo": "410001",
    "cstPadrao": "101"
  }
```

**Uso sugerido:** Quando o usuário preencher o campo `itemListaServico` no formulário de empresa, chamar `GET /api/v1/lookup/correlacao/{valor}` e auto-preencher os campos NBS, CST, ClassTrib e IndOp.

---

## 4. Auto-preenchimento no Formulário de Empresa

### Fluxo sugerido

```
Usuário digita/seleciona Item Lista Serviço (ex: "01.01")
    ↓
Frontend chama GET /api/v1/lookup/correlacao/01.01
    ↓
Backend retorna: { nbsCodigo: "108011100", cstPadrao: "101",
                   classificacaoTributariaCodigo: "410001",
                   indicadorOperacaoCodigo: "030102" }
    ↓
Frontend auto-preenche os 4 campos v4
    ↓
Usuário pode alterar se necessário (os selects ficam editáveis)
```

### Exemplo de implementação (React)

```tsx
const handleItemListaServicoChange = async (value: string) => {
  form.setFieldsValue({ itemListaServico: value });

  try {
    const res = await fetch(`${API_BASE}/lookup/correlacao/${value}`, {
      headers: authHeaders(),
    });
    if (res.ok) {
      const data = await res.json();
      form.setFieldsValue({
        nbs: data.nbsCodigo,
        cst: data.cstPadrao,
        classificacaoTributaria: data.classificacaoTributariaCodigo,
        indicadorOperacao: data.indicadorOperacaoCodigo,
      });
    }
  } catch (e) {
    // Correlação não encontrada — usuário preenche manualmente
  }
};
```

---

## 5. Novos Campos na Listagem de RPS

A resposta de `GET /api/v1/rps` agora inclui campos adicionais em cada RPS:

| Campo | Tipo | Descrição | Exibição sugerida |
|-------|------|-----------|-------------------|
| `providerId` | `string \| null` | `"GINFES"`, `"XTR_SA"`, `"PADRAO_NACIONAL"` | Badge/tag na tabela |
| `versaoSchema` | `string \| null` | `"3"` ou `"4"` | Coluna opcional |
| `tentativasEnvio` | `number \| null` | Nº de tentativas de envio | Tooltip ou coluna |
| `numeroNfse` | `string \| null` | Número da NFS-e emitida | Coluna (quando disponível) |
| `codigoVerificacao` | `string \| null` | Código de verificação | Coluna (quando disponível) |
| `regimeTributario` | `string \| null` | `"ISS"` ou `"IBS_CBS"` | Badge/tag |

### Atualizar interface TypeScript

```typescript
interface RpsItem {
  // ... campos existentes ...

  // NOVOS
  providerId?: string;
  versaoSchema?: string;
  tentativasEnvio?: number;
  numeroNfse?: string;
  codigoVerificacao?: string;
  regimeTributario?: string;
}
```

### Exibição sugerida na tabela de RPS

- **Provider**: badge colorido (GINFES = azul, XTR_SA = verde, PADRAO_NACIONAL = roxo)
- **Regime**: badge (ISS = cinza, IBS_CBS = laranja)
- **NFS-e**: mostrar `numeroNfse` quando status = PROCESSADO (4)
- **Tentativas**: mostrar como tooltip ou quando > 1

---

## 6. O que NÃO Muda (Backward Compatible)

| Funcionalidade | Status |
|----------------|--------|
| Login (`POST /auth/login`) | Sem alteração |
| Body do `EmitirNotaMinRequest` | Mesmo formato |
| `POST /api/v1/nfse/rps-existentes` | Sem alteração |
| `GET /api/v1/rps` e `/rps/resumo` | Mesma estrutura (campos novos são opcionais) |
| `GET /api/v1/rps/job-status` | Sem alteração |
| Upload de planilha XLSX | Sem alteração (parsing é client-side) |
| `POST /api/v1/nfse/emitir-rps-teste` | **Continua funcionando** (deprecated) |
| `POST /api/v1/nfse/reenviar-pendentes` | **Continua funcionando** (deprecated) |
| Certificados (`/certificates/*`) | Sem alteração |
| Usuários CRUD | Sem alteração |

---

## 7. Prioridade de Implementação no Frontend

### P0 — Fazer agora (impacto funcional)
1. Atualizar `EmpresaDTO` com os 2 novos campos (`regimeTributarioPreferido`, `ibsCbsHabilitado`)
2. Adicionar os 2 campos no formulário de empresa (select + toggle)
3. Migrar emissão de `/api/v1/nfse/emitir-rps-teste` → `/api/v2/nfse/emitir`
4. Migrar reenvio de `/api/v1/nfse/reenviar-pendentes` → `/api/v2/nfse/reenviar-pendentes`

### P1 — Fazer em seguida (melhoria de UX)
5. Criar service `lookupApi.ts` com chamadas para `/api/v1/lookup/*`
6. Trocar campos v4 (NBS, CST, ClassTrib, IndOp) de input text → select dinâmico
7. Implementar auto-preenchimento via correlação LC 116
8. Mostrar `providerId` e `regimeTributario` na listagem de RPS

### P2 — Nice to have
9. Mostrar `numeroNfse` e `codigoVerificacao` na listagem (quando PROCESSADO)
10. Mostrar `tentativasEnvio` como indicador visual
11. Filtro por provider na listagem de RPS

---

## 8. Endpoints Completos (Referência)

### Existentes (sem alteração)
```
POST   /auth/login
GET    /api/v1/empresas
POST   /api/v1/empresas
PUT    /api/v1/empresas/{id}
GET    /api/v1/usuarios
POST   /api/v1/usuarios
PUT    /api/v1/usuarios/{id}
DELETE /api/v1/usuarios/{id}
GET    /api/v1/rps?ano={ano}&mes={mes}
GET    /api/v1/rps/resumo
GET    /api/v1/rps/job-status
POST   /api/v1/nfse/rps-existentes
POST   /certificates/import
GET    /certificates/check/{certName}
```

### V1 Deprecated (ainda funciona)
```
POST   /api/v1/nfse/emitir-rps-teste        → usar /api/v2/nfse/emitir
POST   /api/v1/nfse/reenviar-pendentes       → usar /api/v2/nfse/reenviar-pendentes
```

### V2 Novos
```
POST   /api/v2/nfse/emitir
POST   /api/v2/nfse/reenviar-pendentes
```

### Lookup Novos
```
GET    /api/v1/lookup/nbs
GET    /api/v1/lookup/nbs/{codigo}
GET    /api/v1/lookup/nbs/busca?q={termo}
GET    /api/v1/lookup/cst
GET    /api/v1/lookup/cst/{codigo}
GET    /api/v1/lookup/classificacao-tributaria
GET    /api/v1/lookup/classificacao-tributaria/{codigo}
GET    /api/v1/lookup/indicador-operacao
GET    /api/v1/lookup/indicador-operacao/{codigo}
GET    /api/v1/lookup/correlacao
GET    /api/v1/lookup/correlacao/{itemListaServico}
```
