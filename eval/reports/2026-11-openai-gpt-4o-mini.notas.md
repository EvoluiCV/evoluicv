# Revisão humana: baseline gpt-4o-mini (2026-11-10)

Pipeline atual (LangChain4j + `gpt-4o-mini`, temperature 0.2), rodado contra o staging. O relatório automático está em [`2026-11-openai-gpt-4o-mini.md`](./2026-11-openai-gpt-4o-mini.md).

**42/48 checagens ok. Latência mediana de 23 s por análise** (as duas chamadas em sequência).

## O que a baseline faz bem

- O idioma está certo em todos os casos, inclusive nos dois em inglês.
- O schema é válido em todos os casos, com 5 a 12 sugestões.
- O score é coerente com a qualidade: 85 nos CVs fortes e entre 35 e 45 nos fracos.
- Clichês e certificados irrelevantes são apontados (casos 01 e 06).
- O PDF em duas colunas foi entendido apesar da extração intercalada (caso 08).

## Problemas reais

1. **Métricas inventadas em `proposed`.** Acontece em 4 de 8 casos e é o problema mais grave. O prompt manda usar placeholders `[X]%`, mas o modelo escreve números como fatos:
   - "aumentando a eficiência da coordenação em 30%" e "100 alunos… taxa de aprovação de 85%" (caso 05);
   - "resolvendo 95% dos chamados no primeiro contato" (caso 07);
   - "aumentando a taxa de cliques em 25%" (caso 08).

   O candidato que copiar a sugestão coloca no CV uma informação falsa.
2. **Fatos inventados que a checagem automática não pega:**
   - caso 08: o resumo proposto diz "7 anos de experiência **em fintech**", mas o CV é todo de varejo;
   - caso 06: a sugestão ADD inclui "Copilot, RAG" em Habilidades, tecnologias que o candidato nunca citou;
   - caso 01: o modelo escreve "sou proativ**a**", trocando o gênero do candidato.
3. **Regras do prompt ignoradas:**
   - o caso 01 não pede LinkedIn nem GitHub (regra 6);
   - o caso 06 não aponta a ordem cronológica invertida (regra 4).

## Critério para o modelo novo (#10)

O candidato só passa se for:
- **igual ou melhor** em idioma, schema e score;
- **estritamente melhor** em `sem-invencao`, ou seja, nenhuma métrica inventada fora de placeholder.

Os fatos inventados do item 2 continuam sendo revisados à mão neste arquivo.

## Ajuste nas checagens

O `mustFlag` do caso 05 passou a aceitar "objetivo". O modelo criticou a seção "Objetivo" do CV, que é o problema de posicionamento esperado, e a regex original era estreita demais.
