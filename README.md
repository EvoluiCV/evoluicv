# 🚀 Evolui CV

![Next.js](https://img.shields.io/badge/Next.js-black?style=for-the-badge&logo=next.js&logoColor=white)
![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![TailwindCSS](https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Java 25](https://img.shields.io/badge/Java_25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![OpenAI](https://img.shields.io/badge/OpenAI-412991?style=for-the-badge&logo=openai&logoColor=white)

> Plataforma que transforma currículo em vantagem competitiva através de análise inteligente baseada em IA.

---

## 🚀 Teste Agora

👉 [https://evoluicv.luanderson.dev.br/](https://evoluicv.luanderson.dev.br/)

![demo](https://i.ibb.co/v6DXCKxk/demo-evoluicv.gif)

---

## 📌 Visão Geral

O **Evolui CV** simula a análise de um recrutador sênior para avaliar currículos de forma crítica, direta e acionável.

A aplicação identifica problemas reais, explica o impacto de cada um deles e propõe melhorias alinhadas ao objetivo profissional do candidato ou a uma vaga específica.

### 💡 Diferencial

Não é só “feedback genérico de IA”. Aqui o foco é:

- Diagnóstico realista
- Explicação clara do problema
- Sugestão prática de melhoria

---

## 🧠 Como Funciona

A análise acontece através de um pipeline com dois agentes:

### 1. Recruiter Analyst

- Assume a persona de um recrutador experiente
- Detecta automaticamente o idioma
- Retorna:
    - `score`
    - `strengths`
    - `issues` (com `why`, `severity`, `suggestion`)
    - `recommendedActions`

### 2. Improvement Suggestions

- Recebe o CV + análise
- Retorna melhorias estruturadas:
    - `ADD`
    - `REMOVE`
    - `REWRITE`
    - `IMPROVE`

---

## 🖥️ Interface

Fluxo simples e direto:

```
Envio do CV | Parecer do recrutador | Sugestões de melhoria
```

Sem distração. Só decisão.

---

## ✨ Funcionalidades

- Upload de arquivos (`PDF`, `DOCX`, `TXT`)
- Colagem direta de texto
- Suporte multilíngue
- Análise baseada em vaga específica
- Score geral do currículo
- Problemas priorizados por severidade
- Sugestões práticas por seção
- Comparação antes x depois
- Tema claro/escuro

---

## 🏗️ Arquitetura

Monorepo dividido em dois serviços:

### Frontend

- Next.js 16
- React 19
- TypeScript
- Tailwind CSS v4

### Backend

- Spring Boot 3.5
- Java 25
- LangChain4j
- Apache Tika

---

## 🔌 Comunicação

Frontend consome a API via:

```
src/lib/api.ts → analyzeCv()
```

- Formato: `multipart/form-data`
- Base URL configurável via:

```
NEXT_PUBLIC_API_URL
```

---

## 🚀 Como Rodar

### 🐳 Docker (mesmo compose do CI/CD)

O `docker-compose.yml` da raiz é o mesmo compose usado pelo pipeline de CI/CD para build e deploy de staging e produção (runbook de operação na documentação privada do projeto). Pontos que mudam o "rodar local" em relação a versões antigas deste README:

- As chaves de serviço do compose são `evoluicv-api` (backend) e `evoluicv-web` (frontend) — propositalmente diferentes do `container_name` de cada ambiente, para não colidir no DNS entre produção e staging (ver ADR-001, decisão D1b).
- Não existe mais variável de porta publicada nem `ports:` no compose — em staging/produção o acesso é sempre pelo domínio público via proxy reverso, nunca por `localhost`.
- O compose é parametrizado por `COMPOSE_PROJECT_NAME` (nome do ambiente) e `IMAGE_TAG` (tag/SHA da imagem), além de `OPENAI_API_KEY`.

```bash
export OPENAI_API_KEY=sk-...
export COMPOSE_PROJECT_NAME=evoluicv
export IMAGE_TAG=local
docker network create proxy   # rede externa exigida pelo compose; só na 1ª vez
docker compose up --build
```

Como não há porta publicada, o acesso local a esse compose é pela rede do Docker (não por `localhost:3000`/`:8080`). Para o dia a dia de desenvolvimento, prefira a seção **Rodando Localmente** abaixo.

---

### 💻 Rodando Localmente

#### Backend

```bash
cd backend
export OPENAI_API_KEY=sk-...
./mvnw spring-boot:run
```

Rodar testes:

```bash
./mvnw test
```

---

#### Frontend

```bash
cd frontend
yarn install
yarn dev
```

---

## 🔀 Fluxo de Deploy (CI/CD)

O deploy é automatizado por um pipeline Woodpecker self-hosted, seguindo git-flow:

- **`develop`** — todo push dispara testes e, se verdes, **deploy automático em staging** ([evoluicv-staging.luanderson.dev.br](https://evoluicv-staging.luanderson.dev.br)).
- **`main`** — só recebe PR vindo de `develop` (release). O deploy em **produção** ([evoluicv.luanderson.dev.br](https://evoluicv.luanderson.dev.br)) **não é automático**: exige clicar em **Deploy** no pipeline verde de `main`, no Woodpecker.
- **`feature/*` / `fix/*`** — saem de `develop`, testados via pipeline de `pull_request`, sem deploy.
- **`hotfix/*`** — saem de `main`, PR para `main`, deploy manual, depois back-merge em `develop`.

O runbook de operação (pipeline, secrets, rollback e procedimento de corte) vive na documentação privada do projeto (`EvoluiCV/evoluicv-docs`), não neste repositório.

---

## 📁 Estrutura do Projeto

```
evoluicv/
├── backend/
│   ├── ai/
│   ├── cv/
│   ├── config/
│   ├── error/
│   └── prompts/
└── frontend/
    ├── app/
    ├── components/
    ├── lib/
    └── types/
```

---

## 📌 Regras Técnicas

- Contrato frontend/backend é rígido
- Alterações exigem sincronização imediata
- CORS liberado apenas para localhost:3000
- Tamanho mínimo do CV: 200 caracteres
- Abaixo disso retorna HTTP 422

---

## ⚠️ Limitações Atuais

- Sem autenticação
- Sem persistência de dados
- Sem streaming (SSE)
- Sem OCR para PDFs baseados em imagem

---

## 🧪 Stack Técnica

| Camada   | Tecnologia           |
| -------- | -------------------- |
| Frontend | Next.js + React      |
| Backend  | Spring Boot          |
| IA       | OpenAI + LangChain4j |
| Parsing  | Apache Tika          |

---

## 🤝 Contribuindo

O Evolui CV é open source. Contribuições são bem-vindas — desde correções pontuais até novas funcionalidades.

Veja o [`CONTRIBUTING.md`](./CONTRIBUTING.md) para fluxo de issues, PRs, estilo de código e reporte de vulnerabilidades.

---

## 👨‍💻 Autor

**Luanderson Pimenta Mendes** — Backend Software Engineer

---

## 📄 Licença

Licenciado sob [GNU AGPLv3](./LICENSE) © 2026 Luanderson Pimenta Mendes.

> ⚠️ **AGPLv3 implica:** se você hospedar uma versão modificada deste software como serviço acessível pela rede, deve disponibilizar o código-fonte modificado aos usuários do serviço. Uso pessoal/interno e contribuições não são afetados.

### Modelo open-core

Este repositório (`evoluicv`) contém o **núcleo open source** — Analyzer básico para self-host. Funcionalidades premium da plataforma SaaS hospedada (análise expandida com ATS Match / Interview Prep / LinkedIn Repositioning, memória persistente cross-device, federação LinkedIn, editor de CV, exports, billing) ficam em repositório privado e não são distribuídas sob AGPLv3.

Use [evoluicv.luanderson.dev.br](https://evoluicv.luanderson.dev.br/) para experiência completa, ou self-host este repo para uso individual com sua própria chave OpenAI.
