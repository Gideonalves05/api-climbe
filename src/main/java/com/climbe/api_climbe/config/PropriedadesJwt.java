package com.climbe.api_climbe.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.jwt")
public class PropriedadesJwt {

    private String secret;
    private String issuer;
    private Integer expiracaoMinutos;
}
