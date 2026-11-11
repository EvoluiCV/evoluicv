package com.evoluicv.backend.ai.anthropic;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

/**
 * Detecta o idioma do CV por contagem de palavras funcionais e gera a ordem de idioma
 * escrita no próprio idioma. As instruções dos agentes estão em português e, sem essa ordem
 * explícita, o modelo misturava português na análise de CVs em inglês (eval 2026-11, caso 03).
 * Cada idioma suportado é uma constante: palavras funcionais e ordem ficam juntas.
 */
enum CvLanguage {

    PT("pt", Set.of("de", "que", "para", "com", "nao", "uma", "em", "dos", "das", "na", "no", "por", "e", "os", "as"),
            "IDIOMA: O CV está em português. Escreva todos os campos de texto da resposta em português e use language \"pt\"."),
    EN("en", Set.of("the", "and", "to", "of", "with", "for", "is", "that", "in", "on", "by", "a", "an", "i", "my"),
            "LANGUAGE: The CV is written in English. Write EVERY text field of your answer in English "
                    + "(summary, strengths, problem, why, suggestion, recommendedActions, proposed, rationale), "
                    + "even though these instructions are in Portuguese. Use language \"en\"."),
    ES("es", Set.of("el", "la", "los", "las", "y", "del", "con", "por", "para", "en", "una", "que", "mi"),
            "IDIOMA: El CV está escrito en español. Escribe TODOS los campos de texto de la respuesta en español, "
                    + "aunque estas instrucciones estén en portugués. Usa language \"es\".");

    private final String code;
    private final Set<String> stopwords;
    private final String directive;

    CvLanguage(String code, Set<String> stopwords, String directive) {
        this.code = code;
        this.stopwords = stopwords;
        this.directive = directive;
    }

    /** Código ISO 639-1 do idioma predominante; empate ou texto vazio ficam em português. */
    static String detect(String text) {
        return detectLanguage(text).code;
    }

    /** Ordem de idioma para o código ISO informado; idioma desconhecido cai na detecção pelo texto. */
    static String directive(String language, String cvText) {
        String code = language == null ? "" : language.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(lang -> lang.code.equals(code))
                .findFirst()
                .orElseGet(() -> detectLanguage(cvText))
                .directive;
    }

    private static CvLanguage detectLanguage(String text) {
        String[] words = Normalizer.normalize(text == null ? "" : text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .split("[^a-z]+");
        CvLanguage best = PT;
        long bestCount = -1;
        for (CvLanguage lang : values()) {
            long count = Arrays.stream(words).filter(lang.stopwords::contains).count();
            if (count > bestCount) {
                best = lang;
                bestCount = count;
            }
        }
        return best;
    }
}
