# Observabilidade

O backend expõe métricas Micrometer em `/actuator/prometheus` (porta interna 8080). Só a rede interna do projeto alcança esse endpoint; ele não passa pelo proxy do Next.

## Métricas da IA e da cota

| Série no Prometheus | Labels | O que conta |
|---|---|---|
| `evoluicv_ai_tokens_total` | `agent`, `model`, `type` (`input`, `output`, `cache_read`, `cache_write`) | tokens cobrados pela API, por chamada |
| `evoluicv_ai_cost_usd_total` | `agent`, `model` | custo estimado pela tabela `evoluicv.ai.pricing` do `application.yml` |
| `evoluicv_ai_calls_seconds_*` (histograma) | `agent`, `model`, `outcome` (`ok`, `refused`, `truncated`, `invalid`, `error`) | duração e resultado de cada chamada |
| `evoluicv_quota_rejected_total` | `scope` (`ip`, `global`) | análises barradas pela cota diária |

`agent` é `analyst` (recrutador) ou `suggestions` (revisor). Uma análise completa faz uma chamada de cada.

O custo é **estimado**: não inclui impostos nem descontos, e um modelo fora da tabela conta custo zero. A fatura da Anthropic é a fonte da verdade. Ao trocar de modelo, atualize a tabela de preço.

## Painel no Grafana

`grafana/evoluicv-ia.json` mostra custo no período, custo médio por análise, análises por resultado, rejeições por cota, tokens por tipo, latência p50/p95 por agente e a taxa de leitura do cache.

Para importar: Grafana → Dashboards → New → Import, envie o JSON e escolha a fonte Prometheus. A variável `instance` separa staging de produção.
