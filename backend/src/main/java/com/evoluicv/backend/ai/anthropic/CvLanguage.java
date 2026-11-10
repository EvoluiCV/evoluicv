package com.evoluicv.backend.ai.anthropic;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Detecta o idioma do CV por contagem de palavras funcionais e gera a ordem de idioma
 * escrita no próprio idioma. As instruções dos agentes estão em português e, sem essa ordem
 * explícita, o modelo misturava português na análise de CVs em inglês (eval 2026-11, caso 03).
 */
final class CvLanguage {

    private static final Map<String, Set<String>> STOPWORDS = Map.of(
            "pt", Set.of("de", "que", "para", "com", "nao", "uma", "em", "dos", "das", "na", "no", "por", "e", "os", "as"),
            "en", Set.of("the", "and", "to", "of", "with", "for", "is", "that", "in", "on", "by", "a", "an", "i", "my"),
            "es", Set.of("el", "la", "los", "las", "y", "del", "con", "por", "para", "en", "una", "que", "mi"));

    private static final Map<String, String> DIRECTIVES = Map.of(
            "pt", "IDIOMA: O CV está em português. Escreva todos os campos de texto da resposta em português e use language \"pt\".",
            "en", "LANGUAGE: The CV is written in English. Write EVERY text field of your answer in English "
                    + "(summary, strengths, problem, why, suggestion, recommendedActions, proposed, rationale), "
                    + "even though these instructions are in Portuguese. Use language \"en\".",
            "es", "IDIOMA: El CV está escrito en español. Escribe TODOS los campos de texto de la respuesta en español, "
                    + "aunque estas instrucciones estén en portugués. Usa language \"es\".");

    private CvLanguage() {
    }

    static String detect(String text) {
        String normalized = Normalizer.normalize(text == null ? "" : text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
        String[] words = normalized.split("[^a-z]+");
        String best = "pt";
        long bestCount = -1;
        for (String lang : new String[]{"pt", "en", "es"}) {
            Set<String> stop = STOPWORDS.get(lang);
            long count = Arrays.stream(words).filter(stop::contains).count();
            if (count > bestCount) {
                best = lang;
                bestCount = count;
            }
        }
        return best;
    }

    /** Ordem de idioma para o código ISO informado; idioma desconhecido cai na detecção pelo texto. */
    static String directive(String language, String cvText) {
        String lang = language == null ? "" : language.trim().toLowerCase(Locale.ROOT);
        return DIRECTIVES.getOrDefault(lang, DIRECTIVES.get(detect(cvText)));
    }
}
