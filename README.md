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

### 🐳 Docker (Recomendado)

```bash
export OPENAI_API_KEY=sk-...

docker-compose up --build
```

Acessos:

- Frontend: [http://localhost:3000](http://localhost:3000)
- Backend: [http://localhost:8080](http://localhost:8080)

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

[MIT](./LICENSE) © 2026 Luanderson Pimenta Mendes
