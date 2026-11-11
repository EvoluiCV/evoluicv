package com.evoluicv.backend.ai;

/**
 * Falha do provedor de IA em termos do domínio: recusa, resposta truncada ou fora do schema, ou falha de
 * transporte. O adapter converte as exceções do SDK nesta, para nada fora de {@code ai/anthropic} depender dele.
 */
public class AiResponseException extends RuntimeException {

    public enum Reason {
        /** stop_reason "refusal": o classificador de segurança recusou o conteúdo. */
        REFUSED,
        /** stop_reason "max_tokens": a resposta foi cortada antes de fechar o JSON. */
        TRUNCATED,
        /** Resposta sem bloco de texto ou com JSON que não casa com o record. */
        INVALID,
        /** Falha HTTP, de rede ou de timeout ao falar com o provedor. */
        UPSTREAM
    }

    private final Reason reason;

    public AiResponseException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public AiResponseException(Reason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }
}
