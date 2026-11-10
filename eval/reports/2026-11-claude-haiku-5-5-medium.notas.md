# Revisão humana: Claude Haiku 5.5, effort medium (2026-11-10)

Primeira rodada depois da migração (#10), contra o staging. Comparação automática: [`2026-11-openai-gpt-4o-mini__vs__2026-11-claude-haiku-5-5-medium.md`](./2026-11-openai-gpt-4o-mini__vs__2026-11-claude-haiku-5-5-medium.md).

**41/48 checagens ok (baseline: 42/48). Latência mediana de 31 s (baseline: 23 s).**

## Melhor que a baseline

- **Métricas inventadas praticamente sumiram.** A baseline escrevia "85%", "95%", "30%" como fatos; o Claude usa `[X]%`. Os dois números marcados agora são leves:
  - "10+ anos em ensino" (caso 05) é dedução correta: ela leciona desde 2011;
  - "Windows 10/11" (caso 07) detalha uma versão que o CV não cita.
- **Segue mais as regras do prompt.** Pede LinkedIn e GitHub no caso 01 e aponta a ordem cronológica invertida no caso 06. As duas regras falharam na baseline.

## Regressão: idioma (corrigida no mesmo PR, falta reconfirmar no eval)

Nos dois CVs em inglês, a resposta saiu em português:
- no caso 03, até `language` veio `pt`;
- no caso 04, `language` veio `en`, mas o parecer e as justificativas estão em português.

As instruções estão todas em português e puxam o idioma da resposta. A correção acrescenta um lembrete explícito e bilíngue no fim das duas mensagens de usuário (`LANGUAGE_RULE` no adapter).

## Para decidir na revisão

- **Score mais severo:** 62 nos CVs fortes (baseline: 85). Os pareceres justificam a nota. Exemplo do caso 03: o CV se posiciona como Senior, não como Staff. O prompt pede para não inflar a nota. A faixa 65 a 95 dos casos 02 e 03 pode estar generosa; deixei como está até a próxima rodada.
- **Citação no caso 08 é falso positivo da checagem.** O modelo citou o texto que o Tika extraiu, com as colunas intercaladas; a checagem compara com o texto de referência. A checagem pode passar a usar o texto extraído.
- **Latência:** +8 s por análise, com effort `medium`. O `low` pode compensar; precisa de uma rodada própria.

## Rodada v2 (com o lembrete de idioma): 45/48

Relatório: [`…__vs__2026-11-claude-haiku-5-5-medium-v2.md`](./2026-11-openai-gpt-4o-mini__vs__2026-11-claude-haiku-5-5-medium__vs__2026-11-claude-haiku-5-5-medium-v2.md).

- O caso 04 (EN) passou a responder em inglês. O caso 03 ainda mistura: o texto proposto sai em inglês, mas o parecer, os problemas e as justificativas saem em português. O lembrete genérico não basta. A próxima versão detecta o idioma do CV no backend (`CvLanguage`, contagem de palavras funcionais) e manda a ordem escrita no próprio idioma do CV ("The CV is written in English. Write EVERY text field…").
- O caso 08 passou depois de a checagem usar o texto que o Tika extrai (`extractedText` no caso).
- O "95" do caso 02 era "p95", nome de percentil. A checagem passou a ignorar número colado em letra (p95, EC2).
- Sobram duas reprovações:
  - o score 62 do caso 03, que fica para decisão humana;
  - o "Windows 10/11" do caso 07, uma invenção leve e real.
