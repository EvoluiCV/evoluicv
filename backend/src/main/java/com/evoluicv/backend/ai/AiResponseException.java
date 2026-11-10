package com.evoluicv.backend.ai;

/** O provedor respondeu, mas sem uma análise utilizável (recusa, resposta truncada ou fora do schema). */
public class AiResponseException extends RuntimeException {

    public enum Reason {
        /** stop_reason "refusal": o classificador de segurança recusou o conteúdo. */
        REFUSED,
        /** stop_reason "max_tokens": a resposta foi cortada antes de fechar o JSON. */
        TRUNCATED,
        /** Resposta sem bloco de texto ou com JSON que não casa com o record. */
        INVALID
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
