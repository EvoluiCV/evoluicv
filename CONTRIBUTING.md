# Contribuindo com o Evolui CV

Obrigado pelo interesse em contribuir! Este guia descreve como propor mudanças, abrir issues e enviar pull requests.

---

## Código de Conduta

Trate todos com respeito. Discussões técnicas são bem-vindas; ataques pessoais não. Comportamento inadequado pode levar a banimento do projeto.

---

## Como contribuir

### 1. Antes de codar

- **Bugs:** abra uma issue com passos para reproduzir, comportamento esperado e observado, versão/ambiente.
- **Features:** abra uma issue de proposta antes de codar. PRs grandes sem alinhamento prévio podem ser fechados.
- **Dúvidas:** use [Discussions](https://github.com/EvoluiCV/evoluicv/discussions) (quando habilitado) ou abra uma issue com label `question`.

### 2. Setup local

Veja [`README.md`](./README.md) para instruções de setup do frontend e backend.

### 3. Branch e commit

- Crie branch a partir de `main`: `feature/<slug-curto>` ou `fix/<slug-curto>`.
- Use [Conventional Commits](https://www.conventionalcommits.org/):
  - `feat:` nova funcionalidade
  - `fix:` correção de bug
  - `docs:` documentação
  - `refactor:` refatoração sem mudança de comportamento
  - `test:` testes
  - `chore:` manutenção (deps, build, CI)
- Commits pequenos e focados. Mensagem no imperativo: `add X`, não `added X`.

### 4. Pull Request

- **Tamanho:** ≤ 400 linhas alteradas quando possível. PRs maiores devem ser divididos.
- **Descrição:** explique o **porquê**, não só o **o quê**. Linke a issue relacionada (`Closes #123`).
- **Testes:** inclua testes para novo código. Mantenha os existentes verdes.
- **Lint/Build:** rode `yarn lint` (frontend) e `./mvnw verify` (backend) antes de abrir PR.
- **Screenshots:** mudanças visuais devem incluir antes/depois.
- **Squash merge** é o padrão.

### 5. Review

- Mantenedores revisam em até 7 dias úteis.
- Mudanças solicitadas devem ser endereçadas em commits subsequentes (não `--force-push` durante review).
- Após aprovação, o mantenedor faz o merge.

---

## Estilo de código

### Frontend (Next.js / React / TypeScript)

- **Yarn** (não npm/pnpm).
- **Tabs** para indentação nos `.tsx` existentes.
- React Compiler ativo — **não** use `useMemo`/`useCallback` manuais sem motivo claro.
- Componentes shadcn/Radix preferidos a libs externas para UI.

### Backend (Java / Spring Boot)

- Java 25, Spring Boot 3.5, Maven.
- Sem Lombok. Prefira `record`s para DTOs e value objects.
- Domínio puro: nada de Spring/JPA na camada de domínio.
- Testes: JUnit 5 + Mockito + Testcontainers para integração.

### Comentários

- Comente o **porquê**, não o **o quê**. Código autoexplicativo dispensa comentário.
- Não comente código óbvio. Não deixe `// TODO` sem issue vinculada.

---

## Segurança

Encontrou vulnerabilidade? **Não abra issue pública.** Envie e-mail para **contato@luanderson.dev.br** com:

- Descrição do problema
- Passos para reproduzir
- Impacto estimado

Resposta em até 72h.

---

## Licença e DCO

Ao contribuir, você concorda que sua contribuição será licenciada sob [GNU AGPLv3](./LICENSE).

Cada commit deve ser assinado via [DCO](https://developercertificate.org/) (`git commit -s`) — atesta que você tem direito de submeter o código sob a licença do projeto.

### Sobre AGPLv3

A AGPLv3 é uma licença copyleft forte: forks que rodam como serviço de rede precisam abrir o código modificado. Isso protege a comunidade contra apropriação fechada do projeto. Uso pessoal, contribuições e self-host privado não são afetados.

---

## Dúvidas?

Abra uma issue com label `question` ou entre em contato via [@Luanderson-Dev](https://github.com/Luanderson-Dev).
