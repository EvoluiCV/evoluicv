# CLAUDE.md

Evolui CV: análise de currículo com IA simulando um recrutador sênior. Monolito em dois apps (`frontend/` Next.js 16, `backend/` Spring Boot 3.5 / Java 25), sem banco e sem login.

## Fluxo de entrega

- Branch `feature/<issue>-<slug>` a partir de `develop` → PR para `develop`. `main` só recebe releases.
- O pipeline fica em `.woodpecker.yml`: PRs rodam os testes, `develop` publica o staging e a produção sai por deploy manual.
- Segredos ficam fora do repositório. Aqui ficam só os nomes, em `.env.example`.
- `docker-compose.yml` é endurecido (read-only, `cap_drop: ALL`, `no-new-privileges`, limites de recurso, nenhuma porta publicada). Mantenha esse padrão em toda alteração.
- `scripts/ci/smoke.sh` faz no máximo 2 POSTs por execução para não esgotar o rate limit.

## Caminho de uma requisição

```
navegador → Next /api/cv/analyze (route.ts, proxy) → backend BACKEND_INTERNAL_URL/api/cv/analyze
```

Só o Next alcança o backend. O `RateLimitingFilter` identifica o cliente pelo `X-Forwarded-For`, então o proxy precisa repassar o IP real (issue #8).

## Contratos

- Os records em `backend/.../ai/model/` são o contrato com o modelo e com o frontend. Ao mudar um record, atualize no **mesmo commit** `frontend/src/types/analysis.ts` e o schema/descrições enviados ao modelo.
- `GlobalExceptionHandler` define o envelope de erro `{timestamp, status, error, message}`, que `frontend/src/lib/api.ts` lê. Falha do provedor de IA vira 502.
- Texto de CV com menos de 200 caracteres (`MIN_TEXT_LENGTH`) retorna 422.

## Regras de IA

- Os prompts ficam em `backend/src/main/resources/prompts/`. Toda análise cita evidência do CV e nunca inventa experiência, número ou qualificação.
- A resposta segue o idioma do CV. `CvAnalysis.language` é a fonte da verdade, e o segundo agente a recebe.
- Os testes mockam os agentes (hoje via `@MockitoBean`; depois da issue #10, mockam o port de IA). Chamadas reais à API custam dinheiro: elas acontecem só no eval e com aprovação de quem mantém o projeto.
- Migração em andamento (issue #10): sai LangChain4j + OpenAI e entra o SDK oficial da Anthropic com `claude-haiku-5-5`. Enquanto o LangChain4j existir, o projeto fica no Spring Boot 3.x, porque o starter dele não roda no SB4.

## Frontend

- Use **Yarn**, que é o dono do `yarn.lock`.
- `.tsx` são indentados com tabs.
- O React Compiler está ativo e cuida da memoização, então o código fica sem `useMemo`/`useCallback` manuais.
- Componentes de `src/components/ui/` vêm do shadcn CLI (style `radix-mira`, ícones HugeIcons). Adicione novos com `npx shadcn@latest add <componente>`.
- `src/app/layout.tsx` monta a cadeia de providers e o shell (sidebar + header). Páginas novas renderizam dentro dele.

## Backend

- Use o Maven wrapper: `./mvnw test`, `./mvnw spring-boot:run`.
- Modele dados com `record`s Java, sem Lombok.
- O CI usa a imagem Maven fixada e confere se `.mvn/wrapper/maven-wrapper.properties` aponta para a mesma versão. Atualize os dois juntos.
