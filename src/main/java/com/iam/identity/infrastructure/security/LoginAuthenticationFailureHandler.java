package com.iam.identity.infrastructure.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.util.StringUtils;

/**
 * Redirects failed form logins back to the shared SPA login page, preserving {@code client_id} when
 * the user arrived via an OAuth authorize redirect.
 */
final class LoginAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception)
            throws IOException, ServletException {
        StringBuilder target = new StringBuilder("/login?error");
        String clientId = request.getParameter("client_id");
        if (StringUtils.hasText(clientId)) {
            target.append("&client_id=")
                    .append(URLEncoder.encode(clientId, StandardCharsets.UTF_8));
        }
        response.sendRedirect(target.toString());
    }
}
