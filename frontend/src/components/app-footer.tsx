import { GithubIcon } from '@/components/icons/github-icon';
import { LinkedinIcon } from '@/components/icons/linkedin-icon';
import { LogoIcon } from '@/components/icons/logo-icon';
import Link from 'next/link';

export function AppFooter() {
	return (
		<footer className='w-full border-t border-border/40 bg-background py-6 mt-auto'>
			<div className='container mx-auto flex flex-col items-center justify-between gap-4 md:flex-row px-4 md:px-8'>
				<div className='flex items-center gap-3'>
					<LogoIcon />
					<span className='text-sm font-medium text-muted-foreground'>
						Desenvolvido por Luanderson
					</span>
				</div>

				<div className='flex items-center gap-5'>
					<Link
						href='https://github.com/luanderson-dev'
						target='_blank'
						rel='noopener noreferrer'
						className='text-muted-foreground transition-colors hover:text-foreground'
					>
						<GithubIcon />
					</Link>
					<Link
						href='https://linkedin.com/in/luanderson-dev'
						target='_blank'
						rel='noopener noreferrer'
						className='text-muted-foreground transition-colors hover:text-foreground'
					>
						<LinkedinIcon />
					</Link>
				</div>
			</div>
		</footer>
	);
}
