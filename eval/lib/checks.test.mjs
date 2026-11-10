import { test } from 'node:test';
import assert from 'node:assert/strict';
import { checkCase, detectLanguage, inventedNumbers, quoteFidelity } from './checks.mjs';

const CV = 'Reduzi a latência de 420 ms para 180 ms e os incidentes em 38%. Atendi 2.000.000 de requisições por dia.';

test('número citado que não está no CV conta como invenção', () => {
	assert.deepEqual(inventedNumbers('Reduziu incidentes em 38% e o custo em 25%.', CV), ['25%']);
});

test('placeholders entre colchetes e números pequenos não contam como invenção', () => {
	assert.deepEqual(inventedNumbers('Reduzi [X]% do tempo em [Processo 40%], 3 squads.', CV), []);
});

test('número colado em letra (p95, EC2) é nome, não métrica', () => {
	assert.deepEqual(inventedNumbers('latência p95 de [X] ms em instâncias EC2', CV), []);
});

test('número com separador de milhar diferente do CV não é invenção', () => {
	assert.deepEqual(inventedNumbers('atendendo 2,000,000 requisições', CV), []);
});

test('idioma predominante por palavras funcionais', () => {
	assert.equal(detectLanguage('Seu currículo não deixa claro o impacto das entregas para a vaga.'), 'pt');
	assert.equal(detectLanguage('Your resume does not make the impact of the work clear for this role.'), 'en');
});

test('fidelidade de citação ignora ADD e compara sem acento, caixa e pontuação', () => {
	const items = [
		{ action: 'REWRITE', current: 'reduzi a LATENCIA de 420 ms para 180 ms' },
		{ action: 'REMOVE', current: 'trecho que não existe' },
		{ action: 'ADD', current: '' },
	];
	assert.deepEqual(quoteFidelity(items, CV), { found: 1, total: 2 });
});

test('resposta não-200 vira uma falha http só', () => {
	const caso = { expect: { language: 'pt', scoreMin: 0, scoreMax: 100 }, cvText: CV };
	assert.deepEqual(checkCase(caso, { status: 502, body: {} }), [{ id: 'http', ok: false, detalhe: 'HTTP 502' }]);
});

test('score fora da faixa esperada reprova a checagem de score', () => {
	const caso = { expect: { language: 'pt', scoreMin: 10, scoreMax: 50, mustFlag: [] }, cvText: CV };
	const body = {
		analysis: {
			language: 'pt',
			overallScore: 80,
			summary: 'O currículo tem boas métricas para a vaga.',
			strengths: [],
			issues: [{ section: 'Resumo', problem: 'p', why: 'w', suggestion: 's', severity: 'LOW' }],
			recommendedActions: ['a'],
		},
		improvements: { items: Array.from({ length: 5 }, () => ({ action: 'ADD', current: null, proposed: 'x', rationale: 'r', impact: 'LOW' })) },
	};
	const score = checkCase(caso, { status: 200, body }).find((r) => r.id === 'score');
	assert.equal(score.ok, false);
});

test('em caso de PDF, citação e números são conferidos contra o texto extraído', () => {
	const caso = {
		expect: { language: 'pt', scoreMin: 0, scoreMax: 100, mustFlag: [] },
		cvText: 'Figma (coluna esquerda)\nRedesenhei o checkout (coluna direita)',
		extractedText: 'Figma Redesenhei o checkout',
	};
	const body = {
		analysis: { language: 'pt', overallScore: 50, summary: 'O currículo de design tem resultados.', strengths: [], issues: [{ section: 'a', problem: 'p', why: 'w', suggestion: 's', severity: 'LOW' }], recommendedActions: ['a'] },
		improvements: { items: [{ action: 'REWRITE', current: 'Figma Redesenhei o checkout', proposed: 'x', rationale: 'r', impact: 'LOW' }, ...Array.from({ length: 4 }, () => ({ action: 'ADD', current: '', proposed: 'x', rationale: 'r', impact: 'LOW' }))] },
	};
	const citacao = checkCase(caso, { status: 200, body }).find((r) => r.id === 'citacao');
	assert.equal(citacao.ok, true);
});
