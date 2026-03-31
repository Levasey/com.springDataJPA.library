package com.springDataJPA.library.exception;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Log log = LogFactory.getLog(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNotFound(ResourceNotFoundException ex) {
        ModelAndView mv = new ModelAndView("error/not-found");
        mv.addObject("message", ex.getMessage());
        return mv;
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleBadRequest(BadRequestException ex) {
        ModelAndView mv = new ModelAndView("error/bad-request");
        mv.addObject("message", ex.getMessage());
        return mv;
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ModelAndView handleConflict(ConflictException ex) {
        ModelAndView mv = new ModelAndView("error/conflict");
        mv.addObject("message", ex.getMessage());
        return mv;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleIllegalArgument(IllegalArgumentException ex) {
        ModelAndView mv = new ModelAndView("error/bad-request");
        mv.addObject("message", ex.getMessage());
        return mv;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleUnexpected(Exception ex) {
        log.error("Unhandled exception in MVC controller", ex);
        ModelAndView mv = new ModelAndView("error/server-error");
        mv.addObject("message", "Something went wrong. Please try again later.");
        return mv;
    }
}
