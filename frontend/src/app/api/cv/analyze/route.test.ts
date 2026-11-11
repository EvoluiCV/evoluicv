import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { POST } from './route';

const backendFetch = vi.fn();

beforeEach(() => {
	vi.stubGlobal('fetch', backendFetch);
});

afterEach(() => {
	vi.unstubAllGlobals();
	backendFetch.mockReset();
});

function requisicaoDoNavegador(headers: Record<string, string> = {}) {
	const form = new FormData();
	form.set('cvText', 'texto do cv');
	form.set('professionalGoal', 'Tech Lead');
	return new Request('http://localhost/api/cv/analyze', {
		method: 'POST',
		body: form,
		headers,
	});
}

function headersEnviadosAoBackend(): Headers {
	const [, init] = backendFetch.mock.calls[0];
	return new Headers(init.headers);
}

describe('POST /api/cv/analyze (proxy)', () => {
	it('repassa ao backend o IP real gravado pelo proxy de borda, ignorando o X-Forwarded-For do navegador', async () => {
		backendFetch.mockResolvedValue(Response.json({ ok: true }));

		await POST(
			requisicaoDoNavegador({
				'x-real-ip': '203.0.113.10',
				'x-forwarded-for': '6.6.6.6, 203.0.113.10',
			}),
		);

		const enviados = headersEnviadosAoBackend();
		expect(enviados.get('x-real-ip')).toBe('203.0.113.10');
		expect(enviados.get('x-forwarded-for')).toBeNull();
	});

	it('devolve ao navegador o 429 do backend com corpo e Retry-After', async () => {
		const corpo = {
			status: 429,
			error: 'daily_quota_exceeded',
			message: 'Limite diário de análises atingido. Tente novamente amanhã.',
		};
		backendFetch.mockResolvedValue(
			Response.json(corpo, { status: 429, headers: { 'Retry-After': '3600' } }),
		);

		const resposta = await POST(requisicaoDoNavegador());

		expect(resposta.status).toBe(429);
		expect(resposta.headers.get('retry-after')).toBe('3600');
		expect(await resposta.json()).toEqual(corpo);
	});

	it('repassa o X-RateLimit-Remaining do backend, no sucesso e no erro', async () => {
		backendFetch.mockResolvedValueOnce(
			Response.json({ ok: true }, { headers: { 'X-RateLimit-Remaining': '4' } }),
		);
		backendFetch.mockResolvedValueOnce(
			Response.json(
				{ error: 'empty_cv' },
				{ status: 422, headers: { 'X-RateLimit-Remaining': '3' } },
			),
		);

		const sucesso = await POST(requisicaoDoNavegador());
		const erro = await POST(requisicaoDoNavegador());

		expect(sucesso.headers.get('x-ratelimit-remaining')).toBe('4');
		expect(erro.headers.get('x-ratelimit-remaining')).toBe('3');
	});

	it('quando o backend está fora do ar, responde 500 sem expor o erro interno', async () => {
		vi.spyOn(console, 'error').mockImplementation(() => {});
		backendFetch.mockRejectedValue(
			new Error('connect ECONNREFUSED evoluicv-api:8080'),
		);

		const resposta = await POST(requisicaoDoNavegador());

		expect(resposta.status).toBe(500);
		const corpo = await resposta.json();
		expect(JSON.stringify(corpo)).not.toContain('ECONNREFUSED');
		expect(corpo.message).toBe('Erro de comunicação entre Frontend e Backend.');
	});
});
