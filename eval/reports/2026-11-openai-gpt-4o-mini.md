# Eval: 2026-11-openai-gpt-4o-mini

Gerado em 2026-11-10 por `node eval/report.mjs 2026-11-openai-gpt-4o-mini`. Checagens automáticas, sem LLM-juiz; a leitura lado a lado é para revisão humana.

## Resumo

| Execução | Casos | Checagens ok | Latência mediana |
|---|---|---|---|
| 2026-11-openai-gpt-4o-mini | 8/8 | 42/48 | 23484 ms |

## Checagens por caso

| Caso | 2026-11-openai-gpt-4o-mini |
|---|---|
| 01-pt-junior-fraco | ❌ pontos-esperados · score 35 |
| 02-pt-senior-forte | ✅ tudo ok · score 85 |
| 03-en-senior-forte | ✅ tudo ok · score 85 |
| 04-en-junior-fraco | ✅ tudo ok · score 45 |
| 05-pt-transicao-carreira | ❌ sem-invencao · score 45 |
| 06-pt-ordem-errada-certificados | ❌ sem-invencao, pontos-esperados · score 65 |
| 07-pt-bloco-unico | ❌ sem-invencao · score 45 |
| 08-pt-pdf-duas-colunas | ❌ sem-invencao · score 60 |

## 01-pt-junior-fraco

PT, júnior, mal escrito: clichês, sem métricas, sem links. Objetivo: *Primeira vaga como Desenvolvedor Backend Júnior*.

| | 2026-11-openai-gpt-4o-mini |
|---|---|
| schema | ✅ ok |
| idioma | ✅ esperado pt, declarado pt, texto parece pt |
| score | ✅ 35 (faixa 10-50) |
| sem-invencao | ✅ ok |
| citacao | ✅ 6/6 trechos "current" encontrados no CV |
| pontos-esperados | ❌ não mencionou: linkedin\|github\|portf[oó]lio |
| latência | 22916 ms |
| parecer | O currículo apresenta uma estrutura básica, mas carece de informações relevantes e específicas que demonstrem habilidades técnicas e experiências práticas. A falta de métricas e descrições concretas limita a competitividade para a vaga desejada. |
| pontos fortes | • Formação em Análise e Desenvolvimento de Sistemas em andamento. • Conhecimentos em Java, HTML e CSS. |
| problemas | • [HIGH] Sobre mim: Frases genéricas como 'sou proativa' e 'trabalho bem em equipe' não agregam valor. • [MEDIUM] Experiência: Descrição vaga das atividades realizadas como auxiliar administrativo. • [MEDIUM] Projetos: Os projetos não estão descritos de forma a mostrar impacto ou resultados. • [MEDIUM] Conhecimentos: A lista de conhecimentos está muito ampla e inclui habilidades não diretamente relacionadas ao cargo. • [HIGH] Cursos: Cursos de montagem e manutenção de hardware não são relevantes para a vaga de desenvolvedor backend. |
| sugestões | • REWRITE Sobre mim: Desenvolvedor backend júnior com foco em Java e experiência em criação de sistemas. Busco minha primeira oportunidade na área de tecnologia para aplicar meus conhecimentos e contribuir para o crescimento da equipe. • REWRITE Experiência: - Organizei e mantive planilhas que melhoraram a eficiência do setor administrativo em [X]%.  - Realizei manutenção em computadores, garantindo a continuidade das operações do escritório. • REWRITE Projetos: - Desenvolvi um sistema de cadastro de clientes em Java que facilitou o acesso a informações e reduziu o tempo de cadastro em [X]%.  - Criei interfaces em HTML e CSS para um site de receitas, melhorando a usabilidade e a experiência do usuário em [X]% • IMPROVE Conhecimentos: Java, SQL, Git, HTML, CSS. • REMOVE Cursos:  • REMOVE Cursos:  • ADD Cursos: Curso de Desenvolvimento Web (40h) ou Curso de Java para Iniciantes (40h) |

## 02-pt-senior-forte

PT, sênior, bem escrito: métricas, links, método XYZ. Objetivo: *Tech Lead em fintech*.

| | 2026-11-openai-gpt-4o-mini |
|---|---|
| schema | ✅ ok |
| idioma | ✅ esperado pt, declarado pt, texto parece pt |
| score | ✅ 85 (faixa 65-95) |
| sem-invencao | ✅ ok |
| citacao | ✅ 6/6 trechos "current" encontrados no CV |
| pontos-esperados | ✅ ok |
| latência | 24650 ms |
| parecer | O currículo apresenta uma boa estrutura e experiências relevantes, mas pode ser otimizado em termos de clareza e impacto. Algumas descrições carecem de verbos de ação mais fortes e a seção de resumo pode ser mais específica sobre a especialidade e foco. |
| pontos fortes | • Experiência sólida em backend com Java e Spring, alinhada ao objetivo de Tech Lead. • Resultados quantificáveis nas experiências, demonstrando impacto direto nas operações da fintech. • Certificação recente em AWS, relevante para o setor financeiro. |
| problemas | • [HIGH] Resumo: A descrição é genérica e não menciona diretamente a especialidade ou foco em fintech. • [MEDIUM] Experiência — PagaLeve Fintech: A primeira frase é boa, mas poderia ser mais impactante com um verbo de ação mais forte. • [MEDIUM] Experiência — LojaNet Varejo: A descrição da construção da API é vaga e não menciona a tecnologia utilizada. • [MEDIUM] Competências: As competências estão listadas, mas não estão integradas às experiências. |
| sugestões | • REWRITE Resumo: Engenheira de Software Sênior com 9 anos de experiência em backend Java em fintechs, especializada em arquitetura orientada a eventos e otimização de sistemas de pagamento, liderando squads de 6 pessoas com foco em confiabilidade (SLO de 99,95%). • REWRITE Experiência — PagaLeve Fintech: Liderei a transformação do processador de PIX, migrando de um monolito para 7 microsserviços em Spring Boot e Kafka, aumentando a vazão de 800 para 3.200 transações por segundo. • REWRITE Experiência — LojaNet Varejo: Desenvolvi a API de catálogo em Java 11 e Spring, que suportou 2 milhões de requisições diárias, melhorando a performance do sistema. • IMPROVE Experiência — PagaLeve Fintech: Implantei pipeline de CI/CD com GitHub Actions, reduzindo incidentes em produção em 38% em 12 meses, melhorando a eficiência do desenvolvimento. • IMPROVE Competências: Java 21, Spring Boot, Kafka, PostgreSQL, Redis, Docker, Kubernetes, AWS (EKS, SQS, RDS), GitHub Actions, observabilidade com Prometheus e Grafana, arquitetura orientada a eventos, DDD, CI/CD. • REMOVE Certificações:  |

## 03-en-senior-forte

EN, sênior, bem escrito: resposta precisa vir em inglês. Objetivo: *Staff Engineer at a product company*.

| | 2026-11-openai-gpt-4o-mini |
|---|---|
| schema | ✅ ok |
| idioma | ✅ esperado en, declarado en, texto parece en |
| score | ✅ 85 (faixa 65-95) |
| sem-invencao | ✅ ok |
| citacao | ✅ 6/6 trechos "current" encontrados no CV |
| pontos-esperados | ✅ ok |
| latência | 17250 ms |
| parecer | The CV presents a strong candidate for a Staff Engineer position, showcasing relevant experience and technical skills. However, there are areas for improvement in the summary and the organization of skills to enhance ATS compatibility and impact. |
| pontos fortes | • Strong technical skills in Go and Kotlin relevant to the target role. • Quantifiable achievements demonstrating impact on performance and cost savings. • Experience in leading migrations and mentoring, indicating leadership potential. |
| problemas | • [HIGH] Summary: The summary contains generic phrases like 'senior engineer' and lacks specificity about the candidate's focus and expertise. • [MEDIUM] Skills: Skills are listed in a single section without integration into the experience descriptions. • [MEDIUM] Experience — DataPoint Labs: The description is vague and lacks quantifiable impact or specific technologies used. |
| sugestões | • REWRITE Summary: Staff Engineer specializing in distributed systems with expertise in Go and Kotlin, leveraging 10 years of experience to drive platform-level technical decisions and enhance developer experience and system reliability. • IMPROVE Skills: Go, Kotlin, Java, Python, Kafka, gRPC, PostgreSQL, Cassandra, Kubernetes, Terraform, AWS, GCP, OpenTelemetry, CI/CD with GitLab; integrated into experience descriptions for practical application. • REWRITE Experience — DataPoint Labs: Developed ETL jobs in Python, optimizing data processing for analytics dashboards and improving processing speed by [X]% for timely insights. • REWRITE Experience — Shopfront: Engineered a high-performance checkout service in Kotlin and Spring, successfully managing peak loads of 5,000 orders per minute during Black Friday sales, ensuring seamless customer experience. • REWRITE Experience — Streamly: Reduced cloud expenditure by 27% (approximately USD 410k annually) by migrating batch workloads to spot instances utilizing Kubernetes autoscaling, enhancing cost efficiency. • ADD Experience — Streamly: Implemented CI/CD pipelines using GitLab, reducing deployment times by [X]% and increasing release frequency by [Y] releases per month. • REMOVE Education:  |

## 04-en-junior-fraco

EN, júnior, mal escrito: bootcamp, frases vagas, sem links. Objetivo: *Junior Frontend Developer role*.

| | 2026-11-openai-gpt-4o-mini |
|---|---|
| schema | ✅ ok |
| idioma | ✅ esperado en, declarado en, texto parece en |
| score | ✅ 45 (faixa 10-50) |
| sem-invencao | ✅ ok |
| citacao | ✅ 6/6 trechos "current" encontrados no CV |
| pontos-esperados | ✅ ok |
| latência | 21613 ms |
| parecer | The CV lacks specificity and measurable achievements, which are crucial for a Junior Frontend Developer role. The 'About me' section is generic and does not highlight relevant skills or technologies. |
| pontos fortes | • Completed a Full Stack Web Development Bootcamp, indicating recent and relevant education. • Experience with React and API integration in projects. |
| problemas | • [HIGH] About me: The section contains generic phrases like 'hard-working' and 'team player' without specifying relevant skills or technologies. • [MEDIUM] Experience - Sales Assistant: The description is vague and does not relate to frontend development or technical skills. • [MEDIUM] Projects: Descriptions of projects are too brief and lack details about technologies used and outcomes. • [MEDIUM] Skills: Skills are listed but not integrated into the experience or projects, making them feel disconnected. |
| sugestões | • REWRITE About me: Aspiring Junior Frontend Developer skilled in HTML, CSS, and JavaScript, with a passion for building responsive web applications and a strong foundation in React. • REWRITE Experience - Sales Assistant: Managed social media posts using design tools, enhancing my understanding of user engagement and digital communication, which developed my skills in visual presentation and audience interaction. • REWRITE Projects: Developed a to-do app using React, improving task management for users. Created a weather website that fetches real-time data from an API, enhancing user experience through dynamic content. • IMPROVE Projects: Developed a to-do app using React and integrated a weather API to display real-time data, showcasing my ability to create interactive applications. • IMPROVE Skills: HTML, CSS, JavaScript, React, Node.js; strong teamwork and communication skills demonstrated through collaborative projects. • ADD Projects: [Link to GitHub or portfolio showcasing projects] • REMOVE Hobbies:  |

## 05-pt-transicao-carreira

PT, transição de carreira (professora para dados): experiência relevante mas mal posicionada. Objetivo: *Transição para Analista de Dados Júnior*.

| | 2026-11-openai-gpt-4o-mini |
|---|---|
| schema | ✅ ok |
| idioma | ✅ esperado pt, declarado pt, texto parece pt |
| score | ✅ 45 (faixa 20-65) |
| sem-invencao | ❌ números fora do CV: 30%, 100, 85%, 20% |
| citacao | ✅ 5/5 trechos "current" encontrados no CV |
| pontos-esperados | ✅ ok |
| latência | 29349 ms |
| parecer | O currículo apresenta uma base sólida em matemática e estatística, mas carece de foco e detalhes específicos para a transição para Analista de Dados Júnior. A falta de métricas e descrições claras nas experiências prejudica a competitividade. |
| pontos fortes | • Experiência em matemática e estatística, que são fundamentais para a análise de dados. • Cursos relevantes em SQL, Python e Power BI, que são habilidades essenciais para a função desejada. |
| problemas | • [HIGH] Objetivo: O objetivo é muito vago e genérico: 'Atuar na área de dados'. • [HIGH] Experiência - Colégio Estadual Monte Claro: A descrição das responsabilidades é vaga e não utiliza verbos de ação fortes. • [HIGH] Experiência - Cursinho Pré-Vestibular Rumo Certo: A experiência é descrita de forma muito superficial e não demonstra impacto ou resultados. • [MEDIUM] Competências: As competências estão listadas de forma genérica e não estão integradas às experiências. |
| sugestões | • REWRITE Objetivo: Transição para Analista de Dados Júnior, utilizando habilidades em SQL, Python e Power BI para análise e visualização de dados. • REWRITE Experiência - Colégio Estadual Monte Claro: Desenvolvi uma planilha em Google Sheets para acompanhar as notas de 6 turmas, aumentando a eficiência da coordenação em 30% na gestão do desempenho escolar. • REWRITE Experiência - Cursinho Pré-Vestibular Rumo Certo: Ministrei aulas de matemática e estatística para 100 alunos, contribuindo para uma taxa de aprovação de 85% no vestibular. • IMPROVE Competências: Estatística descritiva, SQL básico, Python (pandas), Power BI, Excel e Google Sheets. Integrei SQL em projetos de acompanhamento de desempenho escolar. • ADD Projetos: Projeto de Análise de Dados: Realizei uma análise de dados de desempenho escolar utilizando Python e Power BI, resultando em insights que melhoraram a performance em 20%. • REMOVE Cursos:  |

## 06-pt-ordem-errada-certificados

PT, pleno: experiências fora da ordem cronológica inversa e certificados irrelevantes para o objetivo. Objetivo: *Desenvolvedor Frontend Pleno com React*.

| | 2026-11-openai-gpt-4o-mini |
|---|---|
| schema | ✅ ok |
| idioma | ✅ esperado pt, declarado pt, texto parece pt |
| score | ✅ 65 (faixa 25-65) |
| sem-invencao | ❌ números fora do CV: 15%, 25% |
| citacao | ✅ 5/5 trechos "current" encontrados no CV |
| pontos-esperados | ❌ não mencionou: cronol[oó]gic\|ordem\|recente |
| latência | 26728 ms |
| parecer | O currículo apresenta uma boa base de experiência em desenvolvimento frontend, mas carece de métricas e descrições mais impactantes. Além disso, a seção de certificados inclui cursos irrelevantes para a posição desejada, o que pode prejudicar a percepção do recrutador. |
| pontos fortes | • Experiência prática com React e TypeScript • Participação em um projeto de design system • Conhecimento em testes automatizados com Jest e Testing Library |
| problemas | • [HIGH] Resumo: A descrição é genérica e não destaca especializações ou tecnologias específicas. • [MEDIUM] Experiência — Agência Criativa Mar Azul: A descrição das atividades é vaga e não apresenta resultados ou impacto. • [MEDIUM] Experiência — Conecta Saúde: Falta de métricas e resultados na descrição das atividades. • [MEDIUM] Experiência — Viaje Bem Turismo: A descrição é um pouco técnica, mas carece de resultados mensuráveis. • [HIGH] Certificados: Certificados irrelevantes para a posição de desenvolvedor frontend. |
| sugestões | • REWRITE Resumo: Desenvolvedor Frontend Pleno com 4 anos de experiência em React e TypeScript, focado em criar interfaces responsivas e escaláveis. • REWRITE Experiência — Agência Criativa Mar Azul: Desenvolvi landing pages em HTML, CSS e jQuery, aumentando a taxa de conversão em 15% para clientes da agência. • REWRITE Experiência — Conecta Saúde: Desenvolvi telas do portal do paciente em React, melhorando a usabilidade e reduzindo o tempo de carregamento em 30%. • REWRITE Experiência — Viaje Bem Turismo: Migrei o site de reservas de JavaScript para TypeScript e Next.js, melhorando a performance em 25% e a manutenção do código. Implementei testes automatizados com Jest e Testing Library, aumentando a confiabilidade do sistema. • REMOVE Certificados:  • ADD Habilidades: CI/CD, Testes Automatizados, GitHub Actions, Copilot, RAG. |

## 07-pt-bloco-unico

PT, mal formatado: um bloco de texto sem seções, com erros de digitação. Objetivo: *Analista de Suporte de TI Pleno*.

| | 2026-11-openai-gpt-4o-mini |
|---|---|
| schema | ✅ ok |
| idioma | ✅ esperado pt, declarado pt, texto parece pt |
| score | ✅ 45 (faixa 10-50) |
| sem-invencao | ❌ números fora do CV: 30%, 100, 95%, 20% |
| citacao | ✅ 7/7 trechos "current" encontrados no CV |
| pontos-esperados | ✅ ok |
| latência | 23484 ms |
| parecer | O currículo apresenta informações relevantes, mas carece de estrutura e detalhamento. A falta de métricas e verbos de ação enfraquece a apresentação das experiências e habilidades do candidato. |
| pontos fortes | • Experiência de 6 anos em suporte de TI • Conhecimento em Windows Server e Active Directory • Experiência em treinamento de funcionários novos |
| problemas | • [HIGH] Resumo: O resumo não apresenta uma declaração clara de especialidade e foco. • [HIGH] Experiência — InfoCenter Serviços: A descrição das atividades é vaga e não utiliza verbos de ação fortes. • [HIGH] Experiência — Rede Farma Nordeste: Falta de métricas e resultados nas atividades descritas. • [MEDIUM] Habilidades: As habilidades estão listadas de forma solta e não estão integradas às experiências. • [MEDIUM] Ausente: Não há links para LinkedIn, GitHub ou portfólio. |
| sugestões | • REWRITE Resumo: Analista de Suporte de TI com 6 anos de experiência em configuração de redes e suporte a sistemas Windows. • REWRITE Experiência — InfoCenter Serviços: Implementei configurações de redes em servidores Windows, aumentando a eficiência do suporte em 30% e atendendo 100 chamados mensais. • REWRITE Experiência — Rede Farma Nordeste: Atendi 40 lojas, resolvendo 95% dos chamados no primeiro contato, melhorando a satisfação do cliente em 20% e utilizando o GLPI para gerenciar inventário. • IMPROVE Habilidades: Windows Server, Active Directory, Linux básico, Redes TCP/IP, Office 365, GLPI, PowerShell (básico). • ADD Links: LinkedIn: [seu-linkedin] \| GitHub: [seu-github] • REMOVE Resumo:  • REWRITE Experiência — InfoCenter Serviços: Iniciei minha carreira na InfoCenter Serviços em 2018 como Técnico de Campo, atendendo chamados de clientes e garantindo a resolução eficiente de problemas. • REWRITE Experiência — Rede Farma Nordeste: Desde 2022, atuo como Analista de Suporte na Rede Farma Nordeste, gerenciando o suporte técnico para 40 lojas. |

## 08-pt-pdf-duas-colunas

PT, PDF em duas colunas com linhas intercaladas no arquivo: testa a extração do Tika. Objetivo: *Product Designer Sênior em fintech*.

| | 2026-11-openai-gpt-4o-mini |
|---|---|
| schema | ✅ ok |
| idioma | ✅ esperado pt, declarado pt, texto parece pt |
| score | ✅ 60 (faixa 35-85) |
| sem-invencao | ❌ números fora do CV: 25% |
| citacao | ✅ 5/5 trechos "current" encontrados no CV |
| pontos-esperados | ✅ ok |
| latência | 23469 ms |
| parecer | O currículo apresenta algumas conquistas relevantes, mas carece de detalhes e métricas mais robustas que demonstrem o impacto das ações realizadas. A seção de resumo é genérica e não destaca a especialidade do candidato como Product Designer em fintechs. |
| pontos fortes | • Experiência sólida de 7 anos em design de produtos • Conquistas mensuráveis, como aumento de conversão e criação de design system |
| problemas | • [HIGH] Resumo: A descrição é genérica e não menciona a especialidade em fintechs. • [MEDIUM] Experiência — Compra Fácil App: A descrição das responsabilidades e conquistas é vaga e não utiliza verbos de ação fortes. • [MEDIUM] Experiência — Moda Sul E-commerce: A descrição é muito superficial e não apresenta resultados ou métricas. • [MEDIUM] Competências: As competências estão listadas de forma solta e não estão integradas às experiências. |
| sugestões | • REWRITE Resumo: Product Designer Sênior com 7 anos de experiência em fintechs, especializado em pesquisa, prototipação e design de interfaces. • REWRITE Experiência — Compra Fácil App: Redesenhei o checkout do app utilizando Figma, resultando em um aumento de 18% na taxa de conversão. • REWRITE Experiência — Moda Sul E-commerce: Desenvolvi telas para o aplicativo de compras, aumentando a taxa de cliques em 25% durante os testes A/B. • IMPROVE Competências: Figma, Design System, Pesquisa com usuários, Testes de usabilidade, Prototipação, Métricas de produto integradas nas experiências. • ADD Resumo: LinkedIn: [link do LinkedIn] \| Portfólio: [link do portfólio] • REMOVE Experiência — Estúdio Pixel:  |
