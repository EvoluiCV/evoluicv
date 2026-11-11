package com.evoluicv.backend.quota;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Configuração de cota inválida derruba a subida em vez de bloquear (ou liberar) tudo em silêncio. */
class QuotaPropertiesTest {

    private static final ZoneId SP = ZoneId.of("America/Sao_Paulo");

    @Test
    void tetoGlobalZeradoEhRecusado() {
        assertThatThrownBy(() -> new QuotaProperties(3, 0, SP)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cotaPorIpZeroEhAceitaPorqueDesligaOLimitePorIp() {
        assertThatCode(() -> new QuotaProperties(0, 30, SP)).doesNotThrowAnyException();
    }
}
