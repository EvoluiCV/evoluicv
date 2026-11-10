import { NextResponse } from 'next/server';

export async function POST(request: Request) {
	try {
		const backendUrl =
			process.env.BACKEND_INTERNAL_URL || 'http://localhost:8080';

		const formData = await request.formData();

		// O NPM grava em X-Real-IP o IP da conexão; o X-Forwarded-For vindo do navegador é forjável.
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
			const retryAfter = response.headers.get('retry-after');
			return NextResponse.json(errorData, {
				status: response.status,
				headers: retryAfter ? { 'Retry-After': retryAfter } : undefined,
			});
		}

		const data = await response.json();
		return NextResponse.json(data);
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
