package com.climbe.api_climbe.config;

import com.climbe.api_climbe.service.AutenticacaoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Handler executado apos login bem-sucedido via Google OAuth2.
 * Gera o JWT da aplicacao e redireciona o usuario para o frontend
 * incluindo o token na URL para que o frontend possa armazena-lo.
 */
@Component
@Conditional(OAuth2EnabledCondition.class)
public class GoogleOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AutenticacaoService autenticacaoService;
    private final String frontendRedirectUrl;

    public GoogleOAuth2SuccessHandler(
            AutenticacaoService autenticacaoService,
            @Value("${app.oauth2.frontend-redirect-url:http://localhost:5174/auth/oauth2-callback}") String frontendRedirectUrl
    ) {
        this.autenticacaoService = autenticacaoService;
        this.frontendRedirectUrl = frontendRedirectUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        String email = principal.getAttribute("email");
        String nome = principal.getAttribute("name");
        String sub = principal.getAttribute("sub");

        try {
            var loginResponse = autenticacaoService.loginComGoogle(email, nome, sub);

            String redirect = UriComponentsBuilder.fromUriString(frontendRedirectUrl)
                    .queryParam("token", loginResponse.token())
                    .queryParam("nome", URLEncoder.encode(loginResponse.nome() != null ? loginResponse.nome() : "", StandardCharsets.UTF_8))
                    .queryParam("email", URLEncoder.encode(loginResponse.email() != null ? loginResponse.email() : "", StandardCharsets.UTF_8))
                    .queryParam("cargo", URLEncoder.encode(loginResponse.cargo() != null ? loginResponse.cargo() : "", StandardCharsets.UTF_8))
                    .build()
                    .toUriString();

            getRedirectStrategy().sendRedirect(request, response, redirect);
        } catch (ResponseStatusException ex) {
            String redirect = UriComponentsBuilder.fromUriString(frontendRedirectUrl)
                    .queryParam("error", URLEncoder.encode(ex.getReason() != null ? ex.getReason() : "Falha no login", StandardCharsets.UTF_8))
                    .queryParam("status", ex.getStatusCode().value())
                    .build()
                    .toUriString();
            getRedirectStrategy().sendRedirect(request, response, redirect);
        }
    }
}
