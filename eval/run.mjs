// Roda os casos do eval contra um endpoint /api/cv/analyze e grava as respostas.
// Cada caso é uma análise real: custa dinheiro e gasta a cota diária do ambiente.
//
// uso: node eval/run.mjs --base <url> --label <nome> [--only 01,03] [--pause 13]
//   --base   URL do app (ex.: https://evoluicv-staging.luanderson.dev.br)
//   --label  nome da execução; grava em eval/runs/<label>/ (ex.: 2026-11-openai-gpt-4o-mini)
//   --pause  segundos entre casos (padrão 13, abaixo do limite de rajada de 5/min por IP)
import { mkdirSync, readFileSync, readdirSync, writeFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseArgs } from 'node:util';

const here = dirname(fileURLToPath(import.meta.url));
const { values: args } = parseArgs({
	options: {
		base: { type: 'string' },
		label: { type: 'string' },
		only: { type: 'string' },
		pause: { type: 'string', default: '13' },
	},
});
if (!args.base || !args.label) {
	console.error('uso: node eval/run.mjs --base <url> --label <nome> [--only 01,03] [--pause 13]');
	process.exit(2);
}

const casesDir = join(here, 'cases');
const only = args.only?.split(',').map((s) => s.trim());
const casos = readdirSync(casesDir)
	.filter((f) => f.endsWith('.json'))
	.sort()
	.map((f) => JSON.parse(readFileSync(join(casesDir, f), 'utf8')))
	.filter((c) => !only || only.some((o) => c.id.startsWith(o)));

const outDir = join(here, 'runs', args.label);
mkdirSync(outDir, { recursive: true });
const sleep = (s) => new Promise((r) => setTimeout(r, s * 1000));

function formFor(caso) {
	const form = new FormData();
	form.set('professionalGoal', caso.professionalGoal);
	if (caso.targetRole) form.set('targetRole', caso.targetRole);
	if (caso.file) {
		const bytes = readFileSync(join(casesDir, caso.file));
		form.set('file', new Blob([bytes], { type: 'application/pdf' }), caso.file);
	} else {
		form.set('cvText', caso.cvText);
	}
	return form;
}

async function analisar(caso) {
	const inicio = performance.now();
	const res = await fetch(`${args.base.replace(/\/$/, '')}/api/cv/analyze`, {
		method: 'POST',
		body: formFor(caso),
		signal: AbortSignal.timeout(180_000),
	});
	const latencyMs = Math.round(performance.now() - inicio);
	const body = await res.json().catch(() => null);
	return { status: res.status, retryAfter: res.headers.get('retry-after'), latencyMs, body };
}

console.log(`${casos.length} caso(s) contra ${args.base} -> eval/runs/${args.label}/`);
for (const [i, caso] of casos.entries()) {
	if (i > 0) await sleep(Number(args.pause));
	let r = await analisar(caso);
	if (r.status === 429 && r.body?.error === 'rate_limit_exceeded' && Number(r.retryAfter) <= 120) {
		console.log(`  ${caso.id}: limite de rajada, esperando ${r.retryAfter}s`);
		await sleep(Number(r.retryAfter) + 1);
		r = await analisar(caso);
	}
	writeFileSync(
		join(outDir, `${caso.id}.json`),
		JSON.stringify({ caseId: caso.id, base: args.base, at: new Date().toISOString(), ...r }, null, '\t') + '\n',
	);
	console.log(`  ${caso.id}: HTTP ${r.status} em ${r.latencyMs} ms`);
	if (r.status === 429 && r.body?.error === 'daily_quota_exceeded') {
		console.error('cota diária do ambiente esgotada; parei. Rode de novo amanhã com --only para os que faltam.');
		process.exit(1);
	}
}
console.log(`pronto. relatório: node eval/report.mjs ${args.label} [outra-execução]`);
