package com.springdatajpa.library.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.validation.method.ParameterValidationResult;

import java.util.Collection;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNotFound(ResourceNotFoundException ex) {
        ModelAndView mv = new ModelAndView("error/404");
        mv.addObject("message", ex.getMessage());
        return mv;
    }

    /**
     * Spring 6: missing static resource (for example {@code /favicon.ico}) or URL that does not map to a controller.
     * Without this handler, {@link #handleUnexpected(Exception)} turns these into misleading 500 responses.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNoResourceFound(NoResourceFoundException ex) {
        log.debug("No static resource: {}", ex.getResourcePath());
        ModelAndView mv = new ModelAndView("error/404");
        mv.addObject("message", "Страница или файл не найдены.");
        return mv;
    }

    /**
     * Often means POST was sent where only PATCH/DELETE is mapped — for example if
     * {@code spring.mvc.hiddenmethod.filter.enabled} is false and forms use {@code _method}.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ModelAndView handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("HTTP method not supported: {}", ex.getMessage());
        ModelAndView mv = new ModelAndView("error/bad-request");
        mv.addObject("message",
                "Этот адрес или способ запроса не поддерживается. Обновите страницу и повторите действие из формы на сайте.");
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

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ModelAndView handleDataIntegrity(DataIntegrityViolationException ex) {
        log.debug("Data integrity violation", ex);
        ModelAndView mv = new ModelAndView("error/conflict");
        mv.addObject("message",
                "Запись конфликтует с уже имеющимися данными (например, такой email может быть уже занят).");
        return mv;
    }

    /**
     * Сообщение клиенту обобщено: произвольный {@code IllegalArgumentException} из зависимостей
     * не должен раскрывать внутренние детали.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleIllegalArgument(IllegalArgumentException ex) {
        log.debug("IllegalArgumentException (sanitized for client)", ex);
        ModelAndView mv = new ModelAndView("error/bad-request");
        mv.addObject("message", "Некорректный запрос. Проверьте данные и попробуйте снова.");
        return mv;
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleBindingOrNotValid(Exception ex) {
        String message = "Некорректные данные. Проверьте форму и попробуйте снова.";
        if (ex instanceof MethodArgumentNotValidException manve && manve.getBindingResult().getFieldError() != null) {
            message = manve.getBindingResult().getFieldError().getDefaultMessage();
        } else if (ex instanceof BindException be && be.getBindingResult().getFieldError() != null) {
            message = be.getBindingResult().getFieldError().getDefaultMessage();
        }
        ModelAndView mv = new ModelAndView("error/bad-request");
        mv.addObject("message", message);
        return mv;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(" "));
        if (message.isBlank()) {
            message = "Некорректные данные.";
        }
        ModelAndView mv = new ModelAndView("error/bad-request");
        mv.addObject("message", message);
        return mv;
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleMethodValidation(HandlerMethodValidationException ex) {
        log.debug("Method validation failed", ex);
        String message =
                ex.getAllValidationResults().stream()
                        .map(ParameterValidationResult::getResolvableErrors)
                        .flatMap(Collection::stream)
                        .map(MessageSourceResolvable::getDefaultMessage)
                        .filter(m -> m != null && !m.isBlank())
                        .collect(Collectors.joining(" "));
        if (message.isBlank()) {
            message = "Некорректные данные. Проверьте форму и попробуйте снова.";
        }
        ModelAndView mv = new ModelAndView("error/bad-request");
        mv.addObject("message", message);
        return mv;
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleAccessDenied(AccessDeniedException ex) {
        log.debug("Access denied", ex);
        ModelAndView mv = new ModelAndView("error/403");
        mv.addObject("message", "Недостаточно прав для этого действия. Войдите под учётной записью с доступом к каталогу.");
        return mv;
    }

    /**
     * Otherwise handled as 500 by {@link #handleUnexpected(Exception)} because it extends {@link Exception}.
     */
    @ExceptionHandler(ServletRequestBindingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleServletRequestBinding(ServletRequestBindingException ex) {
        ModelAndView mv = new ModelAndView("error/bad-request");
        mv.addObject("message", ex.getMessage() != null ? ex.getMessage() : "Неверный запрос.");
        return mv;
    }

    /**
     * Invalid path variable or request parameter type (for example non-numeric id).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ModelAndView mv = new ModelAndView("error/bad-request");
        mv.addObject("message", "Некорректное значение параметра запроса.");
        return mv;
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleDataAccess(DataAccessException ex) {
        log.error("Database access error", ex);
        ModelAndView mv = new ModelAndView("error/500");
        mv.addObject("message",
                "Ошибка при обращении к базе данных. Убедитесь, что СУБД запущена и применены все миграции Flyway.");
        return mv;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleUnexpected(Exception ex) {
        log.error("Unhandled exception in MVC controller", ex);
        ModelAndView mv = new ModelAndView("error/500");
        mv.addObject("message", "Произошла ошибка. Попробуйте позже.");
        return mv;
    }
}
