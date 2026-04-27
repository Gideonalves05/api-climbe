package com.climbe.api_climbe.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OAuth2EnabledCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String clientId = context.getEnvironment().getProperty("spring.security.oauth2.client.registration.google.client-id");
        return clientId != null && !clientId.trim().isEmpty();
    }
}
