package com.climbe.api_climbe.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.notificacao")
public class PropriedadesNotificacao {

    private List<Duration> delaysRetry = List.of(
        Duration.ofSeconds(30),
        Duration.ofMinutes(2),
        Duration.ofMinutes(10),
        Duration.ofHours(1),
        Duration.ofHours(6),
        Duration.ofHours(24)
    );

    private Integer maxTentativas = 6;
    private Integer batchSize = 100;
    private Duration sseTimeout = Duration.ofMinutes(30);
    private Duration sseHeartbeatInterval = Duration.ofSeconds(25);
    private Duration slaInAppSegundos = Duration.ofSeconds(10);
    private Duration slaEmailMinutos = Duration.ofMinutes(5);
    private Integer cleanupDias = 30;

    public List<Duration> getDelaysRetry() {
        return new ArrayList<>(delaysRetry);
    }

    public void setDelaysRetry(List<Duration> delaysRetry) {
        this.delaysRetry = delaysRetry;
    }

    public Integer getMaxTentativas() {
        return maxTentativas;
    }

    public void setMaxTentativas(Integer maxTentativas) {
        this.maxTentativas = maxTentativas;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public Duration getSseTimeout() {
        return sseTimeout;
    }

    public void setSseTimeout(Duration sseTimeout) {
        this.sseTimeout = sseTimeout;
    }

    public Duration getSseHeartbeatInterval() {
        return sseHeartbeatInterval;
    }

    public void setSseHeartbeatInterval(Duration sseHeartbeatInterval) {
        this.sseHeartbeatInterval = sseHeartbeatInterval;
    }

    public Duration getSlaInAppSegundos() {
        return slaInAppSegundos;
    }

    public void setSlaInAppSegundos(Duration slaInAppSegundos) {
        this.slaInAppSegundos = slaInAppSegundos;
    }

    public Duration getSlaEmailMinutos() {
        return slaEmailMinutos;
    }

    public void setSlaEmailMinutos(Duration slaEmailMinutos) {
        this.slaEmailMinutos = slaEmailMinutos;
    }

    public Integer getCleanupDias() {
        return cleanupDias;
    }

    public void setCleanupDias(Integer cleanupDias) {
        this.cleanupDias = cleanupDias;
    }
}
