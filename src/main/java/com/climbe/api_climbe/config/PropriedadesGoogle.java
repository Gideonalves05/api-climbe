package com.climbe.api_climbe.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "google.integracoes")
public class PropriedadesGoogle {

    private String applicationName = "api-climbe";
    private Api sheets = new Api("https://sheets.googleapis.com");
    private Api drive = new Api("https://www.googleapis.com/drive");
    private Api calendar = new Api("https://www.googleapis.com/calendar");
    private Api gmail = new Api("https://gmail.googleapis.com");

    @Getter
    @Setter
    public static class Api {
        private boolean habilitado;
        private String baseUrl;

        public Api() {
        }

        public Api(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }
}
