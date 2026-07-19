import { ThemeProvider } from '@/components/theme-provider';
import { cn } from '@/lib/utils';
import type { Metadata } from 'next';
import { Figtree, Geist, Geist_Mono } from 'next/font/google';
import './globals.css';
const figtree = Figtree({ subsets: ['latin'], variable: '--font-sans' });

const geistSans = Geist({
	variable: '--font-geist-sans',
	subsets: ['latin'],
});

const geistMono = Geist_Mono({
	variable: '--font-geist-mono',
	subsets: ['latin'],
});

export const metadata: Metadata = {
	title: 'Evolui CV — Análise crítica de currículo por IA',
	description:
		'Envie seu currículo e receba uma análise crítica baseada em critérios reais de mercado, com sugestões acionáveis e uma versão otimizada.',
};

export default function RootLayout({
	children,
}: Readonly<{
	children: React.ReactNode;
}>) {
	return (
		<html
			suppressHydrationWarning
			lang='en'
			className={cn(
				'h-full',
				'antialiased',
				geistSans.variable,
				geistMono.variable,
				'font-sans',
				figtree.variable,
			)}
		>
			<body className='min-h-full flex flex-col'>
				<ThemeProvider>{children}</ThemeProvider>
			</body>
		</html>
	);
}
