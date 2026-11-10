package com.evoluicv.backend.error;

import com.evoluicv.backend.quota.DailyQuotaExceededException;
import com.anthropic.errors.AnthropicException;
import com.evoluicv.backend.ai.AiResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return body(HttpStatus.BAD_REQUEST, "validation_error", message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return body(HttpStatus.BAD_REQUEST, "bad_request", ex.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(MissingServletRequestParameterException ex) {
        return body(HttpStatus.BAD_REQUEST, "bad_request",
                "Parâmetro obrigatório ausente: " + ex.getParameterName() + ".");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return body(HttpStatus.METHOD_NOT_ALLOWED, "method_not_allowed",
                "Método " + ex.getMethod() + " não é suportado neste endpoint.");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUpload(MaxUploadSizeExceededException ex) {
        return body(HttpStatus.PAYLOAD_TOO_LARGE, "payload_too_large",
                "Arquivo excede o tamanho máximo permitido.");
    }

    @ExceptionHandler(EmptyCvTextException.class)
    public ResponseEntity<Map<String, Object>> handleEmptyCv(EmptyCvTextException ex) {
        return body(HttpStatus.UNPROCESSABLE_ENTITY, "empty_cv", ex.getMessage());
    }

    @ExceptionHandler(UnsupportedCvFormatException.class)
    public ResponseEntity<Map<String, Object>> handleUnsupportedFormat(UnsupportedCvFormatException ex) {
        return body(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "unsupported_format", ex.getMessage());
    }

    @ExceptionHandler(DailyQuotaExceededException.class)
    public ResponseEntity<Map<String, Object>> handleDailyQuota(DailyQuotaExceededException ex) {
        ResponseEntity<Map<String, Object>> response = body(HttpStatus.TOO_MANY_REQUESTS, "daily_quota_exceeded", ex.getMessage());
        return ResponseEntity.status(response.getStatusCode())
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(ex.retryAfterSeconds()))
                .body(response.getBody());
    }

    @ExceptionHandler(AiResponseException.class)
    public ResponseEntity<Map<String, Object>> handleAiResponse(AiResponseException ex) {
        // sem o conteúdo da resposta no log: pode trazer trechos do CV (LGPD)
        log.warn("Resposta inutilizável do provedor de IA: {}", ex.reason());
        return switch (ex.reason()) {
            case REFUSED -> body(HttpStatus.UNPROCESSABLE_ENTITY, "ai_refused",
                    "Não foi possível analisar este conteúdo. Confira se o texto enviado é um currículo.");
            case TRUNCATED, INVALID -> body(HttpStatus.BAD_GATEWAY, "ai_invalid_response",
                    "A análise veio incompleta. Tente novamente em instantes.");
        };
    }

    @ExceptionHandler(AnthropicException.class)
    public ResponseEntity<Map<String, Object>> handleAnthropic(AnthropicException ex) {
        log.error("Falha ao chamar o provedor de IA", ex);
        return body(HttpStatus.BAD_GATEWAY, "ai_upstream_error",
                "Falha ao consultar o provedor de IA. Tente novamente em instantes.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Erro não tratado", ex);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "internal_error",
                "Erro interno inesperado.");
    }

    private ResponseEntity<Map<String, Object>> body(HttpStatus status, String error, String message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("timestamp", Instant.now().toString());
        payload.put("status", status.value());
        payload.put("error", error);
        payload.put("message", message);
        return ResponseEntity.status(status).body(payload);
    }
}
