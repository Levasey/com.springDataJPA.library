package com.springdatajpa.library.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

@ControllerAdvice
public class LayoutModelAdvice {

    @ModelAttribute("headerSearchQ")
    public String headerSearchQ(@RequestParam(value = "q", required = false) String q) {
        return q != null ? q : "";
    }

    /**
     * Путь запроса относительно контекста приложения (для префиксов /books, /people в шаблоне).
     */
    @ModelAttribute("requestUri")
    public String requestUri(HttpServletRequest request) {
        return pathWithinApplication(request);
    }

    /** В разделе «Читатели» в шапке ищем по читателям, иначе — по книгам. */
    @ModelAttribute("headerSearchReadersMode")
    public boolean headerSearchReadersMode(HttpServletRequest request) {
        return pathWithinApplication(request).startsWith("/people");
    }

    @ModelAttribute("headerSearchUrl")
    public String headerSearchUrl(HttpServletRequest request) {
        return headerSearchReadersMode(request) ? "/people/search" : "/books/search";
    }

    private static String pathWithinApplication(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null) {
            return "";
        }
        String cp = request.getContextPath();
        if (cp != null && !cp.isEmpty() && uri.startsWith(cp)) {
            return uri.substring(cp.length());
        }
        return uri;
    }
}
