// Checagens automáticas de uma resposta de /api/cv/analyze contra o caso do eval.
// Sem LLM-juiz: só regras verificáveis. Cada checagem devolve { id, ok, detalhe }.

const SEVERITIES = ['LOW', 'MEDIUM', 'HIGH'];
const ACTIONS = ['ADD', 'REMOVE', 'REWRITE', 'IMPROVE'];

const PT_WORDS = ['de', 'que', 'para', 'com', 'não', 'uma', 'seu', 'sua', 'em', 'dos', 'das', 'currículo', 'você', 'é'];
const EN_WORDS = ['the', 'and', 'to', 'of', 'your', 'with', 'for', 'is', 'that', 'this', 'resume', 'you', 'are'];

export function normalize(text) {
	return (text ?? '')
		.toLowerCase()
		.normalize('NFD')
		.replace(/[̀-ͯ]/g, '')
		.replace(/[^a-z0-9%]+/g, ' ')
		.trim();
}

/** Idioma predominante de um texto por contagem de palavras funcionais (pt | en). */
export function detectLanguage(text) {
	const words = (text ?? '').toLowerCase().split(/[^\p{L}]+/u);
	const count = (list) => words.filter((w) => list.includes(w)).length;
	return count(PT_WORDS) >= count(EN_WORDS) ? 'pt' : 'en';
}

/**
 * Números citados no texto gerado que não aparecem no CV. Ignora placeholders entre colchetes
 * e números pequenos sem % (ordinais, "3 sugestões"), que geram falso positivo.
 */
export function inventedNumbers(generated, cvText) {
	const cvNumbers = new Set((cvText.match(/\d+(?:[.,]\d+)*/g) ?? []).map(canonicalNumber));
	const semPlaceholders = (generated ?? '').replace(/\[[^\]]*\]/g, ' ');
	// número colado em letra é nome, não métrica: p95, S3, EC2, Java21
	const found = semPlaceholders.match(/(?<![\p{L}\d])\d+(?:[.,]\d+)*\s?%?/gu) ?? [];
	return [
		...new Set(
			found
				.map((n) => n.trim())
				.filter((n) => n.endsWith('%') || Number(canonicalNumber(n)) >= 10)
				.filter((n) => !cvNumbers.has(canonicalNumber(n))),
		),
	];
}

function canonicalNumber(n) {
	return n.replace(/[%\s]/g, '').replace(/[.,](?=\d{3}\b)/g, '').replace(',', '.');
}

/** Fração das sugestões que citam um trecho atual (current) encontrado no CV. */
export function quoteFidelity(items, cvText) {
	const cv = normalize(cvText);
	const quoted = items.filter((i) => i.action !== 'ADD' && i.current && i.current.trim());
	if (quoted.length === 0) return { found: 0, total: 0 };
	const found = quoted.filter((i) => cv.includes(normalize(i.current))).length;
	return { found, total: quoted.length };
}

function schemaProblems(body) {
	const problems = [];
	const a = body?.analysis;
	const items = body?.improvements?.items;
	if (!a) return ['analysis ausente'];
	if (typeof a.language !== 'string') problems.push('analysis.language');
	if (!Number.isInteger(a.overallScore) || a.overallScore < 0 || a.overallScore > 100) problems.push('overallScore fora de 0..100');
	if (typeof a.summary !== 'string' || !a.summary.trim()) problems.push('summary vazio');
	if (!Array.isArray(a.strengths)) problems.push('strengths');
	if (!Array.isArray(a.recommendedActions) || a.recommendedActions.length === 0) problems.push('recommendedActions vazio');
	if (!Array.isArray(a.issues) || a.issues.length === 0) problems.push('issues vazio');
	for (const [i, issue] of (a.issues ?? []).entries()) {
		for (const f of ['section', 'problem', 'why', 'suggestion']) {
			if (typeof issue[f] !== 'string' || !issue[f].trim()) problems.push(`issues[${i}].${f}`);
		}
		if (!SEVERITIES.includes(issue.severity)) problems.push(`issues[${i}].severity`);
	}
	if (!Array.isArray(items)) {
		problems.push('improvements.items ausente');
	} else {
		if (items.length < 5 || items.length > 12) problems.push(`improvements: ${items.length} itens (prompt pede 5 a 12)`);
		for (const [i, it] of items.entries()) {
			if (!ACTIONS.includes(it.action)) problems.push(`items[${i}].action`);
			if (!SEVERITIES.includes(it.impact)) problems.push(`items[${i}].impact`);
			if (it.action !== 'REMOVE' && !it.proposed?.trim()) problems.push(`items[${i}].proposed vazio`);
		}
	}
	return problems;
}

function analysisText(body) {
	const a = body?.analysis ?? {};
	return [
		a.summary,
		...(a.issues ?? []).flatMap((i) => [i.section, i.problem, i.why, i.suggestion]),
		...(a.recommendedActions ?? []),
		...(body?.improvements?.items ?? []).flatMap((i) => [i.section, i.proposed, i.rationale]),
	].join(' \n');
}

/** Todas as checagens de um caso. `response` é { status, body }. */
export function checkCase(caso, response) {
	if (response.status !== 200) {
		return [{ id: 'http', ok: false, detalhe: `HTTP ${response.status}` }];
	}
	const body = response.body;
	const results = [];

	const problems = schemaProblems(body);
	results.push({ id: 'schema', ok: problems.length === 0, detalhe: problems.join('; ') || 'ok' });
	if (!body?.analysis) return results;

	const texto = analysisText(body);
	// PDF: o modelo vê o texto extraído (colunas intercaladas), não o de referência
	const textoDoCv = caso.extractedText ?? caso.cvText;
	const declarado = body.analysis.language;
	const detectado = detectLanguage(texto);
	results.push({
		id: 'idioma',
		ok: declarado === caso.expect.language && detectado === caso.expect.language,
		detalhe: `esperado ${caso.expect.language}, declarado ${declarado}, texto parece ${detectado}`,
	});

	const score = body.analysis.overallScore;
	const { scoreMin, scoreMax } = caso.expect;
	results.push({
		id: 'score',
		ok: score >= scoreMin && score <= scoreMax,
		detalhe: `${score} (faixa ${scoreMin}-${scoreMax})`,
	});

	const factual = [
		body.analysis.summary,
		...(body.analysis.strengths ?? []),
		...(body.improvements?.items ?? []).map((i) => i.proposed),
	].join(' \n');
	const inventados = inventedNumbers(factual, textoDoCv);
	results.push({
		id: 'sem-invencao',
		ok: inventados.length === 0,
		detalhe: inventados.length ? `números fora do CV: ${inventados.join(', ')}` : 'ok',
	});

	const { found, total } = quoteFidelity(body.improvements?.items ?? [], textoDoCv);
	results.push({
		id: 'citacao',
		ok: total === 0 || found / total >= 0.8,
		detalhe: `${found}/${total} trechos "current" encontrados no CV`,
	});

	// as regex dos casos já trazem as variantes com e sem acento
	const faltando = (caso.expect.mustFlag ?? []).filter((re) => !new RegExp(re, 'iu').test(texto));
	results.push({
		id: 'pontos-esperados',
		ok: faltando.length === 0,
		detalhe: faltando.length ? `não mencionou: ${faltando.join(' | ')}` : 'ok',
	});

	return results;
}
