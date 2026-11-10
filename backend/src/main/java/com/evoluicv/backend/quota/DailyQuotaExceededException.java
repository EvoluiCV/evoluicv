package com.evoluicv.backend.quota;

public class DailyQuotaExceededException extends RuntimeException {

    private final long retryAfterSeconds;

    public DailyQuotaExceededException(long retryAfterSeconds) {
        super("Limite diário de análises atingido. Tente novamente amanhã.");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long retryAfterSeconds() {
        return retryAfterSeconds;
    }
}
