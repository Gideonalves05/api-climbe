package com.climbe.api_climbe.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PropriedadesGoogleTest {

    @Test
    void propriedadesGoogle_deveTerValoresPadrao() {
        PropriedadesGoogle propriedades = new PropriedadesGoogle();

        assertThat(propriedades.getApplicationName()).isEqualTo("api-climbe");
        assertThat(propriedades.getOauth2()).isNotNull();
        assertThat(propriedades.getOauth2().getScope()).isEqualTo("openid,profile,email");
        assertThat(propriedades.getSheets()).isNotNull();
        assertThat(propriedades.getDrive()).isNotNull();
        assertThat(propriedades.getCalendar()).isNotNull();
        assertThat(propriedades.getGmail()).isNotNull();
        assertThat(propriedades.getRetry()).isNotNull();
    }

    @Test
    void api_deveTerValoresPadrao() {
        PropriedadesGoogle.Api api = new PropriedadesGoogle.Api();

        assertThat(api.isHabilitado()).isFalse();
        assertThat(api.getTimeoutSeconds()).isEqualTo(30);
        assertThat(api.getMaxRetries()).isEqualTo(3);
        assertThat(api.getRetryDelayMs()).isEqualTo(1000);
    }

    @Test
    void api_deveAceitarBaseUrlNoConstrutor() {
        PropriedadesGoogle.Api api = new PropriedadesGoogle.Api("https://custom.api.com");

        assertThat(api.getBaseUrl()).isEqualTo("https://custom.api.com");
    }

    @Test
    void oauth2_deveTerValoresPadrao() {
        PropriedadesGoogle.OAuth2 oauth2 = new PropriedadesGoogle.OAuth2();

        assertThat(oauth2.getScope()).isEqualTo("openid,profile,email");
        assertThat(oauth2.getRedirectUri()).isEqualTo("http://localhost:8080/oauth2/callback/google");
    }

    @Test
    void retry_deveTerValoresPadrao() {
        PropriedadesGoogle.Retry retry = new PropriedadesGoogle.Retry();

        assertThat(retry.getMaxAttempts()).isEqualTo(3);
        assertThat(retry.getBackoffMs()).isEqualTo(1000);
        assertThat(retry.getBackoffMultiplier()).isEqualTo(2.0);
    }

    @Test
    void sheetsApi_deveTerBaseUrlPadrao() {
        PropriedadesGoogle propriedades = new PropriedadesGoogle();

        assertThat(propriedades.getSheets().getBaseUrl()).isEqualTo("https://sheets.googleapis.com");
    }

    @Test
    void driveApi_deveTerBaseUrlPadrao() {
        PropriedadesGoogle propriedades = new PropriedadesGoogle();

        assertThat(propriedades.getDrive().getBaseUrl()).isEqualTo("https://www.googleapis.com/drive");
    }

    @Test
    void calendarApi_deveTerBaseUrlPadrao() {
        PropriedadesGoogle propriedades = new PropriedadesGoogle();

        assertThat(propriedades.getCalendar().getBaseUrl()).isEqualTo("https://www.googleapis.com/calendar");
    }

    @Test
    void gmailApi_deveTerBaseUrlPadrao() {
        PropriedadesGoogle propriedades = new PropriedadesGoogle();

        assertThat(propriedades.getGmail().getBaseUrl()).isEqualTo("https://gmail.googleapis.com");
    }
}
