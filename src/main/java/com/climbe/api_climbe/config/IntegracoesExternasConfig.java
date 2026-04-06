package com.climbe.api_climbe.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({PropriedadesGoogle.class, PropriedadesJwt.class})
public class IntegracoesExternasConfig {

    @Bean("clienteGoogleSheets")
    public RestClient clienteGoogleSheets(PropriedadesGoogle propriedadesGoogle) {
        return RestClient.builder()
                .baseUrl(propriedadesGoogle.getSheets().getBaseUrl())
                .build();
    }

    @Bean("clienteGoogleDrive")
    public RestClient clienteGoogleDrive(PropriedadesGoogle propriedadesGoogle) {
        return RestClient.builder()
                .baseUrl(propriedadesGoogle.getDrive().getBaseUrl())
                .build();
    }

    @Bean("clienteGoogleCalendar")
    public RestClient clienteGoogleCalendar(PropriedadesGoogle propriedadesGoogle) {
        return RestClient.builder()
                .baseUrl(propriedadesGoogle.getCalendar().getBaseUrl())
                .build();
    }

    @Bean("clienteGoogleGmail")
    public RestClient clienteGoogleGmail(PropriedadesGoogle propriedadesGoogle) {
        return RestClient.builder()
                .baseUrl(propriedadesGoogle.getGmail().getBaseUrl())
                .build();
    }
}
