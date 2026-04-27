package com.climbe.api_climbe.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties({PropriedadesGoogle.class, PropriedadesJwt.class})
public class IntegracoesExternasConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean("clienteGoogleSheets")
    public RestClient clienteGoogleSheets(PropriedadesGoogle propriedadesGoogle) {
        return RestClient.builder()
                .baseUrl(propriedadesGoogle.getSheets().getBaseUrl())
                .requestFactory(clientHttpRequestFactory(propriedadesGoogle.getSheets().getTimeoutSeconds()))
                .build();
    }

    @Bean("clienteGoogleDrive")
    public RestClient clienteGoogleDrive(PropriedadesGoogle propriedadesGoogle) {
        return RestClient.builder()
                .baseUrl(propriedadesGoogle.getDrive().getBaseUrl())
                .requestFactory(clientHttpRequestFactory(propriedadesGoogle.getDrive().getTimeoutSeconds()))
                .build();
    }

    @Bean("clienteGoogleCalendar")
    public RestClient clienteGoogleCalendar(PropriedadesGoogle propriedadesGoogle) {
        return RestClient.builder()
                .baseUrl(propriedadesGoogle.getCalendar().getBaseUrl())
                .requestFactory(clientHttpRequestFactory(propriedadesGoogle.getCalendar().getTimeoutSeconds()))
                .build();
    }

    @Bean("clienteGoogleGmail")
    public RestClient clienteGoogleGmail(PropriedadesGoogle propriedadesGoogle) {
        return RestClient.builder()
                .baseUrl(propriedadesGoogle.getGmail().getBaseUrl())
                .requestFactory(clientHttpRequestFactory(propriedadesGoogle.getGmail().getTimeoutSeconds()))
                .build();
    }

    private ClientHttpRequestFactory clientHttpRequestFactory(int timeoutSeconds) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(timeoutSeconds));
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        return factory;
    }
}
