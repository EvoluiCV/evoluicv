import { NextResponse } from 'next/server';

export async function GET() {
	try {
		const backendUrl =
			process.env.BACKEND_INTERNAL_URL || 'http://localhost:8080';

		const response = await fetch(`${backendUrl}/api/health`, {
			method: 'GET',
		});

		if (!response.ok) {
			return NextResponse.json({ status: 'DOWN' }, { status: 503 });
		}

		const data = await response.json();
		return NextResponse.json(data);
	} catch {
		return NextResponse.json({ status: 'DOWN' }, { status: 503 });
	}
}
