package com.climbe.api_climbe.service;

import com.climbe.api_climbe.model.enums.TipoNotificacao;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Builder para montar corpo HTML de e-mails por tipo de notificação.
 * MVP: HTML simples montado em string.
 * Quando volume/variedade de templates justificar, migrar para Thymeleaf.
 */
@Component
public class EmailBodyBuilder {

    public String construir(TipoNotificacao tipo, String titulo, String mensagem, String linkDestino, Map<String, Object> payload) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("<!DOCTYPE html>")
          .append("<html><head><meta charset='UTF-8'>")
          .append("<style>")
          .append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }")
          .append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }")
          .append(".header { background-color: #2563eb; color: white; padding: 20px; text-align: center; }")
          .append(".content { padding: 20px; background-color: #f9fafb; }")
          .append(".footer { padding: 20px; text-align: center; color: #666; font-size: 12px; }")
          .append(".button { display: inline-block; padding: 10px 20px; background-color: #2563eb; color: white; text-decoration: none; border-radius: 5px; margin-top: 15px; }")
          .append("</style>")
          .append("</head><body>")
          .append("<div class='container'>")
          .append("<div class='header'><h2>Climbe Investimentos</h2></div>")
          .append("<div class='content'>")
          .append("<h3>").append(escapeHtml(titulo)).append("</h3>")
          .append("<p>").append(escapeHtml(mensagem)).append("</p>");

        if (linkDestino != null && !linkDestino.isBlank()) {
            sb.append("<a href='").append(escapeHtml(linkDestino)).append("' class='button'>Ver no Sistema</a>");
        }

        if (payload != null && !payload.isEmpty()) {
            sb.append("<div style='margin-top: 20px; padding: 15px; background-color: #e5e7eb; border-radius: 5px;'>")
              .append("<strong>Detalhes adicionais:</strong><ul>");
            for (Map.Entry<String, Object> entry : payload.entrySet()) {
                sb.append("<li><strong>").append(escapeHtml(entry.getKey())).append(":</strong> ")
                  .append(escapeHtml(String.valueOf(entry.getValue()))).append("</li>");
            }
            sb.append("</ul></div>");
        }

        sb.append("</div>")
          .append("<div class='footer'>")
          .append("<p>Este e-mail foi enviado automaticamente pelo sistema Climbe.</p>")
          .append("<p>Para gerenciar suas preferências de notificação, acesse o sistema.</p>")
          .append("</div>")
          .append("</div>")
          .append("</body></html>");

        return sb.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
