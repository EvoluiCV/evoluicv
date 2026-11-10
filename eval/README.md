# Eval de qualidade das análises

O eval compara a qualidade das análises entre versões do pipeline de IA, por exemplo antes e depois da troca de modelo (#10). As checagens são automáticas e verificáveis, sem LLM-juiz. O relatório lado a lado serve para a revisão humana.

## Casos

Os casos ficam em `cases/`. São 8 CVs fictícios: nomes, empresas e contatos inventados, com e-mails em `example.com`.

| Caso | O que testa |
|---|---|
| 01-pt-junior-fraco | clichês, sem métricas, sem links |
| 02-pt-senior-forte | CV bom: score alto, sem inventar números |
| 03-en-senior-forte | resposta inteira em inglês |
| 04-en-junior-fraco | inglês + CV fraco |
| 05-pt-transicao-carreira | experiência relevante mal posicionada |
| 06-pt-ordem-errada-certificados | ordem cronológica e certificados irrelevantes |
| 07-pt-bloco-unico | texto sem seções, com erros de digitação |
| 08-pt-pdf-duas-colunas | PDF com colunas intercaladas na extração (gerado por `cases/make-two-column-pdf.mjs`) |

Cada caso traz em `expect` o idioma, a faixa de score coerente e os pontos que a análise precisa mencionar (`mustFlag`, regex).

## Checagens (`lib/checks.mjs`)

| Checagem | Regra |
|---|---|
| schema | campos obrigatórios, enums, score 0..100, entre 5 e 12 sugestões |
| idioma | `language` declarado **e** idioma predominante do texto iguais ao do CV |
| score | dentro da faixa do caso |
| sem-invencao | todo número citado em parecer, pontos fortes e `proposed` existe no CV (placeholders `[X]` não contam) |
| citacao | ≥ 80% dos `current` de REWRITE, REMOVE e IMPROVE aparecem no CV |
| pontos-esperados | a análise menciona cada `mustFlag` do caso |

Para testar as checagens, rode `node --test eval/lib/*.test.mjs` (o CI também roda).

## Como rodar

Cada caso faz uma análise real, então **custa dinheiro e gasta a cota diária do ambiente**. Só rode com aprovação de quem mantém o projeto.

```sh
# contra o staging (VPN): usa a chave do ambiente, ninguém precisa de chave local
node eval/run.mjs --base https://evoluicv-staging.luanderson.dev.br --label 2026-11-openai-gpt-4o-mini

# relatório de uma execução, ou de duas lado a lado
node eval/report.mjs 2026-11-openai-gpt-4o-mini
node eval/report.mjs 2026-11-openai-gpt-4o-mini 2026-11-claude-haiku-5-5
```

O runner espera 13 s entre os casos para ficar abaixo do limite de rajada (5/min por IP). Se a cota diária acabar, ele para; rode de novo depois com `--only 05,06` para completar. As respostas ficam em `runs/<label>/` e os relatórios em `reports/`. Os dois são versionados, porque só contêm dados fictícios.
