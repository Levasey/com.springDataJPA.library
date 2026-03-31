package com.springdatajpa.library.controllers;

import com.springdatajpa.library.config.LibraryAccessDeniedHandler;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ForbiddenPageController {

    private static final String MSG_CSRF =
            "Доступ запрещён. Если вы отправляли форму — откройте страницу заново "
                    + "(часто это срабатывает при истёкшей сессии или устаревшем токене безопасности) "
                    + "и войдите снова, затем повторите действие.";
    private static final String MSG_ROLE =
            "Недостаточно прав для этого действия. Войдите под учётной записью с доступом к каталогу.";

    @GetMapping(LibraryAccessDeniedHandler.FORBIDDEN_PAGE_PATH)
    public ModelAndView forbidden(@RequestParam(name = "reason", required = false) String reason) {
        ModelAndView mv = new ModelAndView("error/403");
        mv.setStatus(HttpStatus.FORBIDDEN);
        String message = LibraryAccessDeniedHandler.REASON_CSRF.equals(reason) ? MSG_CSRF : MSG_ROLE;
        mv.addObject("message", message);
        return mv;
    }
}
