package com.springdatajpa.library.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Лимиты запросов к публичным формам (забыл пароль, установка пароля каталога).
 */
@ConfigurationProperties(prefix = "library.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;
    /** Лимит POST /forgot-password с одного клиента за минуту. */
    private int forgotPasswordPostPerMinute = 8;
    /** Лимит GET /catalog/setup-password (переход по ссылке из письма) за минуту. */
    private int catalogSetupGetPerMinute = 60;
    /** Лимит POST /catalog/setup-password за минуту. */
    private int catalogSetupPostPerMinute = 15;
    /** Учитывать первый адрес из {@code X-Forwarded-For} (если приложение за reverse proxy). */
    private boolean trustXForwardedFor = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getForgotPasswordPostPerMinute() {
        return forgotPasswordPostPerMinute;
    }

    public void setForgotPasswordPostPerMinute(int forgotPasswordPostPerMinute) {
        this.forgotPasswordPostPerMinute = forgotPasswordPostPerMinute;
    }

    public int getCatalogSetupGetPerMinute() {
        return catalogSetupGetPerMinute;
    }

    public void setCatalogSetupGetPerMinute(int catalogSetupGetPerMinute) {
        this.catalogSetupGetPerMinute = catalogSetupGetPerMinute;
    }

    public int getCatalogSetupPostPerMinute() {
        return catalogSetupPostPerMinute;
    }

    public void setCatalogSetupPostPerMinute(int catalogSetupPostPerMinute) {
        this.catalogSetupPostPerMinute = catalogSetupPostPerMinute;
    }

    public boolean isTrustXForwardedFor() {
        return trustXForwardedFor;
    }

    public void setTrustXForwardedFor(boolean trustXForwardedFor) {
        this.trustXForwardedFor = trustXForwardedFor;
    }
}
