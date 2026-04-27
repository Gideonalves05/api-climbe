package com.climbe.api_climbe.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "google.integracoes")
public class PropriedadesGoogle {

    private String applicationName = "api-climbe";
    private OAuth2 oauth2 = new OAuth2();
    private Api sheets = new Api("https://sheets.googleapis.com");
    private Api drive = new Api("https://www.googleapis.com/drive");
    private Api calendar = new Api("https://www.googleapis.com/calendar");
    private Api gmail = new Api("https://gmail.googleapis.com");
    private Retry retry = new Retry();

    @Getter
    @Setter
    public static class OAuth2 {
        private String clientId;
        private String clientSecret;
        private String scope = "openid,profile,email";
        private String redirectUri = "http://localhost:8080/oauth2/callback/google";
    }

    @Getter
    @Setter
    public static class Api {
        private boolean habilitado = false;
        private String baseUrl;
        private int timeoutSeconds = 30;
        private int maxRetries = 3;
        private long retryDelayMs = 1000;

        public Api() {
        }

        public Api(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }

    @Getter
    @Setter
    public static class Retry {
        private int maxAttempts = 3;
        private long backoffMs = 1000;
        private double backoffMultiplier = 2.0;
    }
}
