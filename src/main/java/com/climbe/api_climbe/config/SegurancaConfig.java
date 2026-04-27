package com.climbe.api_climbe.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SegurancaConfig {

    private final JwtAutenticacaoFilter jwtAutenticacaoFilter;

    public SegurancaConfig(JwtAutenticacaoFilter jwtAutenticacaoFilter) {
        this.jwtAutenticacaoFilter = jwtAutenticacaoFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ObjectProvider<GoogleOAuth2SuccessHandler> googleOAuth2SuccessHandler
    ) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                        "/api/auth/**",
                        "/oauth2/**",
                        "/login/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/error",
                        "/actuator/health"
                    ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAutenticacaoFilter, UsernamePasswordAuthenticationFilter.class);

        // Habilita OAuth2 login apenas se credenciais Google estao configuradas
        GoogleOAuth2SuccessHandler handler = googleOAuth2SuccessHandler.getIfAvailable();
        if (handler != null) {
            http.oauth2Login(oauth2 -> oauth2
                    .successHandler(handler)
                    .permitAll()
            );
        }

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
