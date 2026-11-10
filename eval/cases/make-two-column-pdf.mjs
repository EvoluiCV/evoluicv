// Gera 08-pt-pdf-duas-colunas.pdf a partir do JSON do caso, sem dependências.
// As linhas são gravadas alternando coluna esquerda e direita, como fazem muitos editores
// visuais: a extração de texto pode intercalar as colunas, e é isso que o caso testa.
// uso: node eval/cases/make-two-column-pdf.mjs
import { readFileSync, writeFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const here = dirname(fileURLToPath(import.meta.url));
const caso = JSON.parse(readFileSync(join(here, '08-pt-pdf-duas-colunas.json'), 'utf8'));
const { left, right } = caso.layout;

const escape = (s) => s.replace(/\\/g, '\\\\').replace(/\(/g, '\\(').replace(/\)/g, '\\)');

const ops = ['BT', '/F1 9 Tf'];
const rows = Math.max(left.length, right.length);
for (let i = 0; i < rows; i++) {
	const y = 800 - i * 13;
	if (left[i]) ops.push(`1 0 0 1 40 ${y} Tm (${escape(left[i])}) Tj`);
	if (right[i]) ops.push(`1 0 0 1 230 ${y} Tm (${escape(right[i])}) Tj`);
}
ops.push('ET');
const stream = Buffer.from(ops.join('\n'), 'latin1');

const objects = [
	'<< /Type /Catalog /Pages 2 0 R >>',
	'<< /Type /Pages /Kids [3 0 R] /Count 1 >>',
	'<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>',
	null, // stream
	'<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>',
];

const parts = [Buffer.from('%PDF-1.4\n%\xe2\xe3\xcf\xd3\n', 'latin1')];
const offsets = [];
let size = parts[0].length;
const push = (buf) => {
	parts.push(buf);
	size += buf.length;
};
objects.forEach((body, i) => {
	offsets.push(size);
	if (body === null) {
		push(Buffer.from(`${i + 1} 0 obj\n<< /Length ${stream.length} >>\nstream\n`, 'latin1'));
		push(stream);
		push(Buffer.from('\nendstream\nendobj\n', 'latin1'));
	} else {
		push(Buffer.from(`${i + 1} 0 obj\n${body}\nendobj\n`, 'latin1'));
	}
});
const xrefAt = size;
const xref = [
	'xref',
	`0 ${objects.length + 1}`,
	'0000000000 65535 f ',
	...offsets.map((o) => `${String(o).padStart(10, '0')} 00000 n `),
	'trailer',
	`<< /Size ${objects.length + 1} /Root 1 0 R >>`,
	'startxref',
	String(xrefAt),
	'%%EOF',
	'',
].join('\n');
push(Buffer.from(xref, 'latin1'));

const out = join(here, caso.file);
writeFileSync(out, Buffer.concat(parts));
console.log(`gerado ${out}`);
