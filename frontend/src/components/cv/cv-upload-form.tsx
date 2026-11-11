'use client';

import {
	Card,
	CardContent,
	CardDescription,
	CardHeader,
	CardTitle,
} from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Switch } from '@/components/ui/switch';
import { Textarea } from '@/components/ui/textarea';
import { cn } from '@/lib/utils';
import type { AnalysisInput } from '@/types/analysis';
import {
	Briefcase01Icon,
	Cancel01Icon,
	CloudUploadIcon,
	File02Icon,
	Loading03Icon,
	MagicWand01Icon,
	SecurityCheckIcon,
	Target02Icon,
} from '@hugeicons/core-free-icons';
import { HugeiconsIcon } from '@hugeicons/react';
import type { DragEvent as ReactDragEvent, FormEvent } from 'react';
import { useRef, useState } from 'react';

const ACCEPT = '.pdf,.docx,.txt,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document,text/plain';
const MAX_FILE_BYTES = 10 * 1024 * 1024; // 10 MB
const MIN_TEXT_LENGTH = 200;

type Mode = 'file' | 'text';

interface Props {
	onSubmit: (input: AnalysisInput) => void;
	isLoading: boolean;
}

export function CvUploadForm({ onSubmit, isLoading }: Props) {
	const [mode, setMode] = useState<Mode>('file');
	const [file, setFile] = useState<File | null>(null);
	const [cvText, setCvText] = useState('');
	const [professionalGoal, setProfessionalGoal] = useState('');
	const [targetRole, setTargetRole] = useState('');
	const [jobFocus, setJobFocus] = useState(false);
	const [jobDescription, setJobDescription] = useState('');
	const [dragging, setDragging] = useState(false);
	const [localError, setLocalError] = useState<string | null>(null);
	const fileInputRef = useRef<HTMLInputElement>(null);

	function validateFile(candidate: File): string | null {
		const name = candidate.name.toLowerCase();
		const acceptedExt = ['.pdf', '.docx', '.txt'];
		if (!acceptedExt.some((ext) => name.endsWith(ext))) {
			return 'Formato não suportado. Envie PDF, DOCX ou TXT.';
		}
		if (candidate.size > MAX_FILE_BYTES) {
			return 'Arquivo muito grande (máx. 10 MB).';
		}
		return null;
	}

	function handleFileSelect(candidate: File | undefined | null) {
		if (!candidate) return;
		const error = validateFile(candidate);
		if (error) {
			setLocalError(error);
			return;
		}
		setLocalError(null);
		setFile(candidate);
	}

	function handleDrop(event: ReactDragEvent<HTMLDivElement>) {
		event.preventDefault();
		setDragging(false);
		handleFileSelect(event.dataTransfer.files?.[0]);
	}

	function buildGoalPayload(): { goal: string; role?: string } | null {
		const goal = professionalGoal.trim();
		const job = jobDescription.trim();

		if (jobFocus) {
			if (!job) {
				setLocalError('Cole a descrição da vaga em que você quer focar.');
				return null;
			}
			const composed = goal
				? `${goal}\n\nDescrição da vaga alvo:\n${job}`
				: `Foco nesta vaga específica:\n\n${job}`;
			return { goal: composed, role: undefined };
		}

		if (!goal) {
			setLocalError('Descreva seu objetivo profissional.');
			return null;
		}
		return { goal, role: targetRole.trim() || undefined };
	}

	function handleSubmit(event: FormEvent) {
		event.preventDefault();
		setLocalError(null);

		const payload = buildGoalPayload();
		if (!payload) return;

		if (mode === 'file') {
			if (!file) {
				setLocalError('Envie um arquivo do seu CV.');
				return;
			}
			onSubmit({
				file,
				professionalGoal: payload.goal,
				targetRole: payload.role,
			});
			return;
		}

		const text = cvText.trim();
		if (text.length < MIN_TEXT_LENGTH) {
			setLocalError(
				`O texto precisa ter pelo menos ${MIN_TEXT_LENGTH} caracteres (atual: ${text.length}).`,
			);
			return;
		}
		onSubmit({
			cvText: text,
			professionalGoal: payload.goal,
			targetRole: payload.role,
		});
	}

	return (
		<Card className='flex-1 min-w-0'>
			<CardHeader>
				<CardTitle className='flex items-center gap-2 text-base'>
					<HugeiconsIcon icon={MagicWand01Icon} className='h-4 w-4 text-primary' />
					Enviar currículo
				</CardTitle>
				<CardDescription>
					Envie seu CV e receba análise crítica + versão otimizada em segundos.
				</CardDescription>
			</CardHeader>
			<CardContent>
				<form onSubmit={handleSubmit} className='flex flex-col gap-5'>
					<div className='flex gap-1 rounded-md bg-muted p-1 text-xs'>
						<button
							type='button'
							onClick={() => setMode('file')}
							className={cn(
								'flex-1 rounded-sm px-3 py-1.5 font-medium transition-colors',
								mode === 'file'
									? 'bg-background text-foreground shadow-sm'
									: 'text-muted-foreground hover:text-foreground',
							)}
						>
							Arquivo
						</button>
						<button
							type='button'
							onClick={() => setMode('text')}
							className={cn(
								'flex-1 rounded-sm px-3 py-1.5 font-medium transition-colors',
								mode === 'text'
									? 'bg-background text-foreground shadow-sm'
									: 'text-muted-foreground hover:text-foreground',
							)}
						>
							Colar texto
						</button>
					</div>

					{mode === 'file' ? (
						file ? (
							<div className='flex items-center gap-3 rounded-md border bg-muted/30 p-3'>
								<div className='flex h-10 w-10 items-center justify-center rounded-md bg-primary/10 text-primary'>
									<HugeiconsIcon icon={File02Icon} className='h-5 w-5' />
								</div>
								<div className='min-w-0 flex-1'>
									<p className='truncate text-sm font-medium'>{file.name}</p>
									<p className='text-xs text-muted-foreground'>
										{(file.size / 1024).toFixed(0)} KB
									</p>
								</div>
								<Button
									type='button'
									variant='ghost'
									size='icon'
									onClick={() => {
										setFile(null);
										if (fileInputRef.current) {
											fileInputRef.current.value = '';
										}
									}}
									disabled={isLoading}
									aria-label='Remover arquivo'
								>
									<HugeiconsIcon icon={Cancel01Icon} className='h-4 w-4' />
								</Button>
							</div>
						) : (
							<div
								onDragOver={(event) => {
									event.preventDefault();
									setDragging(true);
								}}
								onDragLeave={() => setDragging(false)}
								onDrop={handleDrop}
								onClick={() => fileInputRef.current?.click()}
								className={cn(
									'flex cursor-pointer flex-col items-center justify-center gap-2 rounded-md border border-dashed px-4 py-10 text-center transition-colors',
									dragging
										? 'border-primary bg-primary/5'
										: 'border-border hover:border-primary/50 hover:bg-muted/40',
								)}
							>
								<div className='flex h-12 w-12 items-center justify-center rounded-full bg-primary/10 text-primary'>
									<HugeiconsIcon
										icon={CloudUploadIcon}
										className='h-6 w-6'
									/>
								</div>
								<p className='text-sm font-medium'>
									Arraste seu CV aqui ou clique para selecionar
								</p>
								<p className='text-xs text-muted-foreground'>
									PDF, DOCX ou TXT — até 10 MB
								</p>
								<input
									ref={fileInputRef}
									type='file'
									accept={ACCEPT}
									className='hidden'
									onChange={(event) =>
										handleFileSelect(event.target.files?.[0])
									}
								/>
							</div>
						)
					) : (
						<div className='flex flex-col gap-1.5'>
							<Textarea
								value={cvText}
								onChange={(event) => setCvText(event.target.value)}
								placeholder='Cole o conteúdo do seu CV aqui...'
								rows={10}
								className='resize-none font-mono text-xs'
							/>
							<p className='text-right text-[11px] text-muted-foreground tabular-nums'>
								{cvText.trim().length} / {MIN_TEXT_LENGTH} caracteres mínimos
							</p>
						</div>
					)}

					<div className='flex items-start justify-between gap-3 rounded-md border bg-muted/30 p-3'>
						<div className='flex flex-col gap-0.5'>
							<Label
								htmlFor='job-focus'
								className='cursor-pointer text-xs font-medium'
							>
								<HugeiconsIcon
									icon={Briefcase01Icon}
									className='h-3.5 w-3.5'
								/>
								Focar em uma vaga específica
							</Label>
							<p className='text-[11px] text-muted-foreground'>
								Cole a descrição da vaga e a análise será calibrada por ela.
							</p>
						</div>
						<Switch
							id='job-focus'
							checked={jobFocus}
							onCheckedChange={setJobFocus}
						/>
					</div>

					{jobFocus && (
						<div className='flex flex-col gap-1.5'>
							<Label
								htmlFor='job-description'
								className='text-xs font-medium'
							>
								<HugeiconsIcon
									icon={Briefcase01Icon}
									className='h-3.5 w-3.5'
								/>
								Descrição da vaga
								<span className='text-destructive'>*</span>
							</Label>
							<Textarea
								id='job-description'
								value={jobDescription}
								onChange={(event) =>
									setJobDescription(event.target.value)
								}
								placeholder='Cole aqui a descrição completa da vaga: responsabilidades, requisitos, stack, senioridade...'
								rows={6}
								className='resize-none'
							/>
						</div>
					)}

					<div className='flex flex-col gap-1.5'>
						<Label
							htmlFor='professional-goal'
							className='text-xs font-medium'
						>
							<HugeiconsIcon
								icon={Target02Icon}
								className='h-3.5 w-3.5'
							/>
							Objetivo profissional
							{jobFocus ? (
								<span className='text-muted-foreground'>
									(opcional)
								</span>
							) : (
								<span className='text-destructive'>*</span>
							)}
						</Label>
						<Textarea
							id='professional-goal'
							value={professionalGoal}
							onChange={(event) =>
								setProfessionalGoal(event.target.value)
							}
							placeholder={
								jobFocus
									? 'Algo a acrescentar sobre seu objetivo de carreira além da vaga? (opcional)'
									: 'Ex.: Quero migrar para uma vaga de engenheiro de software sênior focada em sistemas distribuídos.'
							}
							rows={3}
							className='resize-none'
						/>
					</div>

					{!jobFocus && (
						<div className='flex flex-col gap-1.5'>
							<Label
								htmlFor='target-role'
								className='text-xs font-medium'
							>
								<HugeiconsIcon
									icon={Briefcase01Icon}
									className='h-3.5 w-3.5'
								/>
								Cargo alvo (opcional)
							</Label>
							<Input
								id='target-role'
								value={targetRole}
								onChange={(event) =>
									setTargetRole(event.target.value)
								}
								placeholder='Ex.: Senior Backend Engineer'
							/>
						</div>
					)}

					{localError && (
						<div className='rounded-md border border-destructive/30 bg-destructive/5 px-3 py-2 text-xs text-destructive'>
							{localError}
						</div>
					)}

					<p
						id='privacy-notice'
						className='flex gap-2 rounded-md bg-muted/50 px-3 py-2 text-xs leading-relaxed text-muted-foreground'
					>
						<HugeiconsIcon
							icon={SecurityCheckIcon}
							className='mt-0.5 h-3.5 w-3.5 shrink-0'
						/>
						<span>
							<strong className='font-medium text-foreground'>Privacidade:</strong>{' '}
							o texto do seu CV é enviado à API da Anthropic (Claude) só para gerar esta
							análise. O Evolui CV não armazena o CV nem o resultado: não há banco de
							dados, e o conteúdo não vai para os logs. Seu endereço IP é usado apenas
							para limitar o número de análises por dia.{' '}
							<a
								href='https://www.anthropic.com/legal/privacy'
								target='_blank'
								rel='noopener noreferrer'
								className='underline underline-offset-2 hover:text-foreground'
							>
								Política de privacidade da Anthropic
							</a>
						</span>
					</p>

					<Button
						type='submit'
						disabled={isLoading}
						className='w-full'
						aria-describedby='privacy-notice'
					>
						{isLoading ? (
							<>
								<HugeiconsIcon
									icon={Loading03Icon}
									className='h-4 w-4 animate-spin'
								/>
								Analisando...
							</>
						) : (
							<>
								<HugeiconsIcon icon={MagicWand01Icon} className='h-4 w-4' />
								Analisar CV
							</>
						)}
					</Button>
				</form>
			</CardContent>
		</Card>
	);
}
