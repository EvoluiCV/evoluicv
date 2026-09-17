#!/bin/sh
# uso: smoke.sh <base_url> <readonly|full>
# readonly: só GET (produção; o rate limiter é global, ver P-EVO-1). full: + POSTs de validação (staging).
set -eu
BASE="$1"
MODE="${2:-readonly}"
fail() { echo "SMOKE FALHOU: $*"; exit 1; }
code() { curl -s -o /dev/null -w '%{http_code}' -m 15 "$@"; }

i=0
until curl -fsS -m 10 "$BASE/api/health" 2>/dev/null | grep -q '"status":"UP"'; do
  i=$((i + 1))
  [ "$i" -ge 36 ] && fail "/api/health não ficou UP em 180 s"
  sleep 5
done
echo "ok GET /api/health UP"

c=$(code "$BASE/"); [ "$c" = "200" ] || fail "GET / -> $c"
echo "ok GET / 200"

if [ "$MODE" = "full" ]; then
  # entradas confirmadas por BE/QA contra o controller (Fase 1.6)
  c=$(code -X POST -F 'cvText= ' -F 'professionalGoal=smoke' "$BASE/api/cv/analyze")
  [ "$c" = "422" ] || fail "POST analyze cv vazio -> $c (esperado 422)"
  c=$(code -X POST -F 'cvText=texto de smoke test' "$BASE/api/cv/analyze")
  [ "$c" = "400" ] || fail "POST analyze sem professionalGoal -> $c (esperado 400)"
  echo "ok POST validações 422/400"
fi
echo "SMOKE OK ($MODE) $BASE"
