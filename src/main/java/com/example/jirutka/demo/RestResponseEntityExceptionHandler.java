package com.example.jirutka.demo;

import cz.jirutka.spring.exhandler.interpolators.MessageInterpolator;
import cz.jirutka.spring.exhandler.interpolators.SpelMessageInterpolator;
import cz.jirutka.spring.exhandler.messages.ErrorMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Profile;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@ControllerAdvice
@Profile("spring")
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    protected static final String
            DEFAULT_PREFIX = "default",
            TYPE_KEY = "type",
            TITLE_KEY = "title",
            DETAIL_KEY = "detail",
            INSTANCE_KEY = "instance";

    @Autowired
    private MessageSource messageSource;

    private MessageInterpolator interpolator = new SpelMessageInterpolator();

    @ExceptionHandler
    protected ResponseEntity<Object> handleConflict(
            RuntimeException ex, WebRequest request) {
        String bodyOfResponse = "This should be application specific";
        return handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (body == null) {
            ErrorMessage m = new ErrorMessage();
            m.setType(URI.create(resolveMessage(TYPE_KEY, ex, request)));
            m.setTitle(resolveMessage(TITLE_KEY, ex, request));
            m.setStatus(status);
            m.setDetail(resolveMessage(DETAIL_KEY, ex, request));
            m.setInstance(URI.create(resolveMessage(INSTANCE_KEY, ex, request)));
            body = m;
        }
        return new ResponseEntity<>(body, headers, status);
    }

    protected String resolveMessage(String key, Exception exception, WebRequest request) {

        String template = getMessage(exception, key, LocaleContextHolder.getLocale());

        Map<String, Object> vars = new HashMap<>(2);
        vars.put("ex", exception);
        vars.put("req", request);

        return interpolateMessage(template, vars);
    }

    protected String interpolateMessage(String messageTemplate, Map<String, Object> variables) {
        return interpolator.interpolate(messageTemplate, variables);
    }

    protected String getMessage(Exception exception, String key, Locale locale) {

        String prefix = exception.getClass().getName();

        String message = messageSource.getMessage(prefix + "." + key, null, null, locale);
        if (message == null) {
            message = messageSource.getMessage(DEFAULT_PREFIX + "." + key, null, null, locale);
        }
        if (message == null) {
            message = "";
        }
        return message;
    }
}
