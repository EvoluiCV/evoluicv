// Gera um relatório Markdown lado a lado de uma ou mais execuções do eval.
// uso: node eval/report.mjs <label-a> [label-b ...]  -> eval/reports/<label-a>[__vs__<label-b>].md
import { existsSync, mkdirSync, readFileSync, readdirSync, writeFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { checkCase } from './lib/checks.mjs';

const here = dirname(fileURLToPath(import.meta.url));
const labels = process.argv.slice(2);
if (labels.length === 0) {
	console.error('uso: node eval/report.mjs <label-a> [label-b ...]');
	process.exit(2);
}

const casos = readdirSync(join(here, 'cases'))
	.filter((f) => f.endsWith('.json'))
	.sort()
	.map((f) => JSON.parse(readFileSync(join(here, 'cases', f), 'utf8')));

function load(label, caso) {
	const file = join(here, 'runs', label, `${caso.id}.json`);
	if (!existsSync(file)) return null;
	const run = JSON.parse(readFileSync(file, 'utf8'));
	return { run, checks: checkCase(caso, run) };
}

const data = casos.map((caso) => ({ caso, porLabel: labels.map((l) => load(l, caso)) }));
const CHECK_IDS = ['http', 'schema', 'idioma', 'score', 'sem-invencao', 'citacao', 'pontos-esperados'];
const cell = (s) => String(s ?? '').replace(/\|/g, '\\|').replace(/\n+/g, ' ');
const mark = (ok) => (ok ? '✅' : '❌');

const out = [];
out.push(`# Eval: ${labels.join(' vs ')}`, '');
out.push(`Gerado em ${new Date().toISOString().slice(0, 10)} por \`node eval/report.mjs ${labels.join(' ')}\`. Checagens automáticas, sem LLM-juiz; a leitura lado a lado é para revisão humana.`, '');

out.push('## Resumo', '');
out.push(`| Execução | Casos | Checagens ok | Latência mediana |`, `|---|---|---|---|`);
for (const [i, label] of labels.entries()) {
	const rows = data.map((d) => d.porLabel[i]).filter(Boolean);
	const checks = rows.flatMap((r) => r.checks);
	const lat = rows.map((r) => r.run.latencyMs).sort((a, b) => a - b);
	const mediana = lat.length ? lat[Math.floor(lat.length / 2)] : '-';
	out.push(`| ${label} | ${rows.length}/${casos.length} | ${checks.filter((c) => c.ok).length}/${checks.length} | ${mediana} ms |`);
}
out.push('');

out.push('## Checagens por caso', '');
out.push(`| Caso | ${labels.map((l) => `${cell(l)}`).join(' | ')} |`, `|---|${labels.map(() => '---').join('|')}|`);
for (const { caso, porLabel } of data) {
	const cols = porLabel.map((r) => {
		if (!r) return 'não rodou';
		const falhas = r.checks.filter((c) => !c.ok).map((c) => c.id);
		const score = r.run.body?.analysis?.overallScore;
		return `${falhas.length ? `❌ ${falhas.join(', ')}` : '✅ tudo ok'}${score !== undefined ? ` · score ${score}` : ''}`;
	});
	out.push(`| ${caso.id} | ${cols.join(' | ')} |`);
}
out.push('');

for (const { caso, porLabel } of data) {
	out.push(`## ${caso.id}`, '', `${caso.descricao}. Objetivo: *${caso.professionalGoal}*.`, '');
	out.push(`| | ${labels.map(cell).join(' | ')} |`, `|---|${labels.map(() => '---').join('|')}|`);
	const linha = (nome, f) => out.push(`| ${nome} | ${porLabel.map((r) => (r ? cell(f(r)) : '-')).join(' | ')} |`);
	for (const id of CHECK_IDS) {
		if (!porLabel.some((r) => r?.checks.some((c) => c.id === id))) continue;
		linha(id, (r) => {
			const c = r.checks.find((x) => x.id === id);
			return c ? `${mark(c.ok)} ${c.detalhe}` : '-';
		});
	}
	linha('latência', (r) => `${r.run.latencyMs} ms`);
	linha('parecer', (r) => r.run.body?.analysis?.summary);
	linha('pontos fortes', (r) => (r.run.body?.analysis?.strengths ?? []).map((s) => `• ${s}`).join(' '));
	linha('problemas', (r) =>
		(r.run.body?.analysis?.issues ?? []).map((i) => `• [${i.severity}] ${i.section}: ${i.problem}`).join(' '),
	);
	linha('sugestões', (r) =>
		(r.run.body?.improvements?.items ?? []).map((i) => `• ${i.action} ${i.section}: ${i.proposed ?? ''}`).join(' '),
	);
	out.push('');
}

mkdirSync(join(here, 'reports'), { recursive: true });
const file = join(here, 'reports', `${labels.join('__vs__')}.md`);
writeFileSync(file, out.join('\n'));
console.log(`relatório: ${file}`);
