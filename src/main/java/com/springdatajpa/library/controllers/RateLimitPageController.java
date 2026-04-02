package com.springdatajpa.library.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RateLimitPageController {

    @GetMapping("/rate-limit-exceeded")
    public String rateLimitExceeded() {
        return "error/rate-limit";
    }
}
