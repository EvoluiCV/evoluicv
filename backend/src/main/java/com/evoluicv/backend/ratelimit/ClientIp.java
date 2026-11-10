package com.evoluicv.backend.ratelimit;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Identifica o cliente. O backend só é alcançável pelo proxy do Next, que grava em X-Real-IP
 * o IP que o Nginx Proxy Manager viu na conexão (não forjável pelo navegador).
 */
public final class ClientIp {

    private ClientIp() {
    }

    public static String from(HttpServletRequest request) {
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
