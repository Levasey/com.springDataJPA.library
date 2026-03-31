package com.springdatajpa.library.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;

/**
 * Sends a GET redirect so the user sees an HTML page with a clear message. Filter-level denials
 * (for example invalid or missing CSRF) do not reach {@code @ControllerAdvice}, so without this
 * handler Spring Boot serves {@code error/403.html} with an empty model.
 */
public final class LibraryAccessDeniedHandler implements AccessDeniedHandler {

    public static final String REASON_CSRF = "csrf";
    public static final String REASON_ROLE = "role";

    public static final String FORBIDDEN_PAGE_PATH = "/forbidden";

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        String reason = accessDeniedException instanceof CsrfException ? REASON_CSRF : REASON_ROLE;
        response.sendRedirect(request.getContextPath() + FORBIDDEN_PAGE_PATH + "?reason=" + reason);
    }
}
