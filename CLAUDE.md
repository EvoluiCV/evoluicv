# CLAUDE.md

Evolui CV: análise de currículo com IA simulando um recrutador sênior. Monolito em dois apps (`frontend/` Next.js 16, `backend/` Spring Boot 3.5 / Java 25), sem banco e sem login.

## Fluxo de entrega

- Branch `feature/<issue>-<slug>` a partir de `develop` → PR para `develop`. `main` só recebe releases.
- O pipeline fica em `.woodpecker.yml`: PRs rodam os testes, `develop` publica o staging e a produção sai por deploy manual.
- Segredos ficam fora do repositório. Aqui ficam só os nomes, em `.env.example`.
- `docker-compose.yml` é endurecido (read-only, `cap_drop: ALL`, `no-new-privileges`, limites de recurso, nenhuma porta publicada). Mantenha esse padrão em toda alteração.
- `scripts/ci/smoke.sh` faz no máximo 2 POSTs por execução, ambos reprovados na validação, para não gastar cota nem chamar a IA.

## Caminho de uma requisição

```
navegador → Next /api/cv/analyze (route.ts, proxy) → backend BACKEND_INTERNAL_URL/api/cv/analyze
```

Só o Next alcança o backend. O proxy repassa só o `X-Real-IP` gravado pelo proxy de borda e descarta o `X-Forwarded-For` do navegador, que é forjável. O backend identifica o cliente por `ClientIp`.

Duas camadas de limite: o `RateLimitingFilter` barra rajadas (5/min, 30/h por IP), e a `DailyQuota` conta análises válidas por IP e no total do dia (`QUOTA_PER_IP_DAILY`, `QUOTA_GLOBAL_DAILY`; zera 00:00 America/Sao_Paulo; fica em memória). A cota é reservada no `CvAnalysisService` depois da validação, então pedidos 422/400 não a consomem. Falha do provedor (502) devolve a cota; recusa (422 `ai_refused`) gasta, porque é sobre o conteúdo enviado.

## Contratos

- Os records em `backend/.../ai/model/` são o contrato com o modelo e com o frontend. Ao mudar um record, atualize no **mesmo commit** `frontend/src/types/analysis.ts` e o schema/descrições enviados ao modelo.
- `GlobalExceptionHandler` define o envelope de erro `{timestamp, status, error, message}`, que `frontend/src/lib/api.ts` lê. Falha do provedor de IA vira 502.
- Texto de CV com menos de 200 caracteres (`MIN_TEXT_LENGTH`) retorna 422.

## Regras de IA

- Os prompts ficam em `backend/src/main/resources/prompts/`. Toda análise cita evidência do CV e nunca inventa experiência, número ou qualificação.
- A resposta segue o idioma do CV. `CvAnalysis.language` é a fonte da verdade, e o segundo agente a recebe.
- O serviço só conhece o `CvAnalysisPort`. O `AnthropicCvAnalysisAdapter` (SDK oficial, `claude-haiku-5-5`) é o único que sabe de modelo, effort (`AI_*_MODEL`/`AI_*_EFFORT`), cache do system prompt e schema de saída derivado dos records. Não envie `temperature`/`top_p`: o modelo rejeita.
- `stop_reason` `refusal` vira 422 `ai_refused`; `max_tokens` ou JSON fora do schema viram 502 `ai_invalid_response`; falha HTTP ou de rede vira 502 `ai_upstream_error`. O adapter converte toda exceção do SDK em `AiResponseException`, e nada fora de `ai/anthropic` importa `com.anthropic`. O log do adapter traz só contagem de tokens, nunca conteúdo. Mensagem de exceção pode carregar trecho do CV: o `GlobalExceptionHandler` loga só tipo e local (`semConteudo`), e o `LogSemConteudoDeCvTest` cobre isso.
- Cada chamada à IA alimenta as métricas `evoluicv_ai_*` (tokens, custo estimado, latência por resultado), e a cota alimenta `evoluicv_quota_rejected_total`. Labels só com valores de conjunto fechado (agente, modelo, tipo, resultado), nunca dado do usuário. Ao trocar de modelo, atualize `evoluicv.ai.pricing`. Referência e painel: `observability/`.
- Os testes mockam o `CvAnalysisPort` com `@MockitoBean`; o adapter é testado contra uma API da Anthropic falsa local. Chamadas reais custam dinheiro: acontecem só no eval (`eval/`) e com aprovação de quem mantém o projeto.

## Frontend

- Use **Yarn**, que é o dono do `yarn.lock`. Testes: `yarn test` (Vitest).
- `.tsx` são indentados com tabs.
- O React Compiler está ativo e cuida da memoização, então o código fica sem `useMemo`/`useCallback` manuais.
- Componentes de `src/components/ui/` vêm do shadcn CLI (style `radix-mira`, ícones HugeIcons). Adicione novos com `npx shadcn@latest add <componente>`.
- `src/app/layout.tsx` monta a cadeia de providers e o shell (sidebar + header). Páginas novas renderizam dentro dele.

## Backend

- Use o Maven wrapper: `./mvnw test`, `./mvnw spring-boot:run`.
- Modele dados com `record`s Java, sem Lombok.
- O CI usa a imagem Maven fixada e confere se `.mvn/wrapper/maven-wrapper.properties` aponta para a mesma versão. Atualize os dois juntos.
