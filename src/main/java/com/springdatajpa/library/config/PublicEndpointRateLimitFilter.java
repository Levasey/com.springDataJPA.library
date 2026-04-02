package com.springdatajpa.library.config;

import com.springdatajpa.library.support.MinuteBucketRateLimiter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Ограничение частоты запросов к публичным формам с одного IP (или {@code X-Forwarded-For} при включённой опции).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 15)
public class PublicEndpointRateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(PublicEndpointRateLimitFilter.class);

    private final RateLimitProperties properties;
    private final MinuteBucketRateLimiter limiter;

    public PublicEndpointRateLimitFilter(RateLimitProperties properties, MinuteBucketRateLimiter limiter) {
        this.properties = properties;
        this.limiter = limiter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!properties.isEnabled()) {
            return true;
        }
        return resolveLimit(request.getMethod(), requestPath(request)) == null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        RateLimitRule rule = resolveLimit(request.getMethod(), requestPath(request));
        if (rule == null) {
            filterChain.doFilter(request, response);
            return;
        }
        String clientKey = resolveClientIp(request) + "|" + rule.scope();
        if (!limiter.tryAcquire(clientKey, rule.maxPerMinute())) {
            log.warn("Rate limit exceeded: scope={}", rule.scope());
            String target = request.getContextPath() + "/rate-limit-exceeded";
            response.sendRedirect(target);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String requestPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        if (StringUtils.hasText(context) && uri.startsWith(context)) {
            uri = uri.substring(context.length());
        }
        if (uri.isEmpty()) {
            return "/";
        }
        return uri.startsWith("/") ? uri : "/" + uri;
    }

    private RateLimitRule resolveLimit(String method, String path) {
        if ("/forgot-password".equals(path) && "POST".equalsIgnoreCase(method)) {
            return new RateLimitRule("forgot-password-post", properties.getForgotPasswordPostPerMinute());
        }
        if ("/catalog/setup-password".equals(path)) {
            if ("GET".equalsIgnoreCase(method)) {
                return new RateLimitRule("catalog-setup-get", properties.getCatalogSetupGetPerMinute());
            }
            if ("POST".equalsIgnoreCase(method)) {
                return new RateLimitRule("catalog-setup-post", properties.getCatalogSetupPostPerMinute());
            }
        }
        return null;
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (properties.isTrustXForwardedFor()) {
            String xff = request.getHeader("X-Forwarded-For");
            if (StringUtils.hasText(xff)) {
                String first = xff.split(",")[0].trim();
                if (!first.isEmpty()) {
                    return first;
                }
            }
        }
        String addr = request.getRemoteAddr();
        return addr != null ? addr : "unknown";
    }

    private record RateLimitRule(String scope, int maxPerMinute) {}
}
