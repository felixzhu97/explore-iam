package com.iam.identity.infrastructure.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.util.StringUtils;

/**
 * Prefers an explicit safe relative {@code continue} form field (SPA deep link), otherwise the
 * saved request (e.g. OAuth authorize or {@code /clients}).
 */
final class ContinueUrlAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    ContinueUrlAuthenticationSuccessHandler() {
        setDefaultTargetUrl("/");
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws ServletException, IOException {
        String continueUrl = request.getParameter("continue");
        if (isSafeRelativePath(continueUrl)) {
            clearAuthenticationAttributes(request);
            getRedirectStrategy().sendRedirect(request, response, continueUrl);
            return;
        }
        super.onAuthenticationSuccess(request, response, authentication);
    }

    private static boolean isSafeRelativePath(String path) {
        if (!StringUtils.hasText(path)) {
            return false;
        }
        String value = path.trim();
        return value.startsWith("/")
                && !value.startsWith("//")
                && !value.contains("://")
                && !value.contains("\\");
    }
}
