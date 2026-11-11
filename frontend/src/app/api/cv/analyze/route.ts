import { NextResponse } from 'next/server';

// Headers do backend que o navegador precisa ver (limites de taxa e cota).
const FORWARDED_RESPONSE_HEADERS = ['retry-after', 'x-ratelimit-remaining'];

function forwardedHeaders(response: Response): Headers {
	const headers = new Headers();
	for (const name of FORWARDED_RESPONSE_HEADERS) {
		const value = response.headers.get(name);
		if (value) headers.set(name, value);
	}
	return headers;
}

export async function POST(request: Request) {
	try {
		const backendUrl =
			process.env.BACKEND_INTERNAL_URL || 'http://localhost:8080';

		const formData = await request.formData();

		// O proxy de borda (Nginx Proxy Manager) grava em X-Real-IP o IP da conexão; o X-Forwarded-For vindo do navegador é forjável.
		const headers = new Headers();
		const clientIp = request.headers.get('x-real-ip');
		if (clientIp) headers.set('x-real-ip', clientIp);

		const response = await fetch(`${backendUrl}/api/cv/analyze`, {
			method: 'POST',
			body: formData,
			headers,
		});

		if (!response.ok) {
			const errorData = await response.json().catch(() => ({}));
			return NextResponse.json(errorData, {
				status: response.status,
				headers: forwardedHeaders(response),
			});
		}

		const data = await response.json();
		return NextResponse.json(data, { headers: forwardedHeaders(response) });
	} catch (error: unknown) {
		console.error('Erro no proxy manual do Next.js:', error);
		return NextResponse.json(
			{
				message: 'Erro de comunicação entre Frontend e Backend.',
			},
			{ status: 500 },
		);
	}
}
