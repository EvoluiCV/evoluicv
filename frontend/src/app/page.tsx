'use client';

import { AppFooter } from '@/components/app-footer';
import { AnalysisResultView } from '@/components/cv/analysis-result-view';
import { CvUploadForm } from '@/components/cv/cv-upload-form';
import { ImprovementSuggestionsView } from '@/components/cv/improvement-suggestions-view';
import { Button } from '@/components/ui/button';
import { analyzeCv, ApiError } from '@/lib/api';
import type { AnalysisInput, AnalysisResponse } from '@/types/analysis';
import { Alert02Icon, ClockIcon, RefreshIcon } from '@hugeicons/core-free-icons';
import { HugeiconsIcon } from '@hugeicons/react';
import { useEffect, useRef, useState } from 'react';

type Status = 'idle' | 'loading' | 'success' | 'error';

type ErrorState =
	| { kind: 'generic'; message: string }
	| { kind: 'rate-limit'; message: string; retryAt: number; daily: boolean };

export default function Page() {
	const [status, setStatus] = useState<Status>('idle');
	const [result, setResult] = useState<AnalysisResponse | null>(null);
	const [error, setError] = useState<ErrorState | null>(null);
	const abortRef = useRef<AbortController | null>(null);

	async function handleSubmit(input: AnalysisInput) {
		abortRef.current?.abort();
		const controller = new AbortController();
		abortRef.current = controller;

		setStatus('loading');
		setError(null);

		try {
			const response = await analyzeCv(input, controller.signal);
			if (controller.signal.aborted) return;
			setResult(response);
			setStatus('success');
		} catch (err) {
			if (controller.signal.aborted) return;
			if (err instanceof ApiError && err.isRateLimited) {
				const seconds = err.retryAfterSeconds ?? 60;
				setError({
					kind: 'rate-limit',
					message: err.message,
					retryAt: Date.now() + seconds * 1000,
					daily: err.errorCode === 'daily_quota_exceeded',
				});
			} else if (err instanceof ApiError) {
				setError({ kind: 'generic', message: err.message });
			} else if (err instanceof Error) {
				setError({
					kind: 'generic',
					message:
						err.message === 'Failed to fetch'
							? 'Não foi possível conectar ao servidor. Verifique se o backend está rodando em http://localhost:8080.'
							: err.message,
				});
			} else {
				setError({ kind: 'generic', message: 'Erro inesperado ao analisar o CV.' });
			}
			setStatus('error');
		}
	}

	function handleReset() {
		abortRef.current?.abort();
		setStatus('idle');
		setResult(null);
		setError(null);
	}

	const isLoading = status === 'loading';
	const hasResult = status === 'success' && result !== null;
	const showResults = isLoading || hasResult;

	return (
		<>
			<main className='mx-auto flex w-full flex-1 flex-col gap-6 p-4 lg:p-8'>
				<header className='flex flex-col gap-1'>
					<h1 className='font-heading text-2xl font-semibold tracking-tight'>
						Evolui CV
					</h1>
					<p className='text-sm text-muted-foreground'>
						Receba uma análise crítica do seu currículo feita por um
						agente de IA. Tenha acesso a um feedback baseado em
						conhecimentos reais de mercado e sugestões pontuais de
						melhoria.
					</p>
				</header>

				{error && <ErrorBanner error={error} onReset={handleReset} />}

				<div
					className={
						showResults
							? 'grid w-full gap-6 lg:grid-cols-3 lg:items-start'
							: 'mx-auto w-full max-w-3xl'
					}
				>
					<CvUploadForm
						onSubmit={handleSubmit}
						isLoading={isLoading}
					/>
					{showResults && (
						<>
							<AnalysisResultView
								data={result?.analysis ?? null}
								isLoading={isLoading}
							/>
							<ImprovementSuggestionsView
								data={result?.improvements ?? null}
								isLoading={isLoading}
							/>
						</>
					)}
				</div>
			</main>
			<AppFooter />
		</>
	);
}

function ErrorBanner({
	error,
	onReset,
}: {
	error: ErrorState;
	onReset: () => void;
}) {
	const retryAt = error.kind === 'rate-limit' ? error.retryAt : 0;
	const [now, setNow] = useState(() => Date.now());

	useEffect(() => {
		if (error.kind !== 'rate-limit') return;
		const interval = setInterval(() => setNow(Date.now()), 1000);
		return () => clearInterval(interval);
	}, [error.kind]);

	if (error.kind === 'rate-limit' && error.daily) {
		return (
			<div className='flex items-start gap-3 rounded-md border border-amber-500/30 bg-amber-500/5 p-3 text-sm text-amber-700 dark:text-amber-400'>
				<HugeiconsIcon
					icon={ClockIcon}
					className='mt-0.5 h-4 w-4 shrink-0'
				/>
				<div className='flex-1'>
					<p className='font-medium'>Limite diário de análises atingido</p>
					<p className='mt-0.5 text-xs opacity-80'>
						Para manter o serviço gratuito, limitamos as análises por dia.
						Novas análises liberam à meia-noite (horário de Brasília).
					</p>
				</div>
			</div>
		);
	}

	if (error.kind === 'rate-limit') {
		const secondsLeft = Math.max(0, Math.ceil((retryAt - now) / 1000));
		const canRetry = secondsLeft <= 0;
		return (
			<div className='flex items-start gap-3 rounded-md border border-amber-500/30 bg-amber-500/5 p-3 text-sm text-amber-700 dark:text-amber-400'>
				<HugeiconsIcon
					icon={ClockIcon}
					className='mt-0.5 h-4 w-4 shrink-0'
				/>
				<div className='flex-1'>
					<p className='font-medium'>Limite de análises atingido</p>
					<p className='mt-0.5 text-xs opacity-80'>
						Para proteger o serviço, limitamos a 5 análises por minuto
						e 30 por hora. {error.message}
					</p>
					{!canRetry && (
						<p className='mt-1 text-xs font-medium'>
							Você poderá tentar novamente em {secondsLeft}s.
						</p>
					)}
				</div>
				<Button
					type='button'
					variant='ghost'
					size='sm'
					onClick={onReset}
					disabled={!canRetry}
					className='text-amber-700 hover:text-amber-700 dark:text-amber-400 dark:hover:text-amber-400'
				>
					<HugeiconsIcon icon={RefreshIcon} className='h-3.5 w-3.5' />
					Tentar novamente
				</Button>
			</div>
		);
	}

	return (
		<div className='flex items-start gap-3 rounded-md border border-destructive/30 bg-destructive/5 p-3 text-sm text-destructive'>
			<HugeiconsIcon
				icon={Alert02Icon}
				className='mt-0.5 h-4 w-4 shrink-0'
			/>
			<div className='flex-1'>
				<p className='font-medium'>Não foi possível analisar o CV</p>
				<p className='mt-0.5 text-xs text-destructive/80'>{error.message}</p>
			</div>
			<Button
				type='button'
				variant='ghost'
				size='sm'
				onClick={onReset}
				className='text-destructive hover:text-destructive'
			>
				<HugeiconsIcon icon={RefreshIcon} className='h-3.5 w-3.5' />
				Tentar novamente
			</Button>
		</div>
	);
}
