package com.example.weatherapp.handler;

import com.example.weatherapp.exception.CityNotFoundException;
import com.example.weatherapp.exception.ExternalServiceUnavailableException;
import com.example.weatherapp.exception.ValueCalculationException;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {

    record ExceptionRule(Class<?> exceptionClass, HttpStatus status) {
    }

    private final List<ExceptionRule> exceptionsRules = List.of(
            new ExceptionRule(CityNotFoundException.class, HttpStatus.BAD_REQUEST),
            new ExceptionRule(ValueCalculationException.class, HttpStatus.BAD_GATEWAY),
            new ExceptionRule(ExternalServiceUnavailableException.class, HttpStatus.SERVICE_UNAVAILABLE)
    );

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Throwable exception = getError(request);
        Optional<ExceptionRule> exceptionRuleOptional = exceptionsRules.stream()
                .filter(exceptionRule -> exceptionRule.exceptionClass().isInstance(exception))
                .findFirst();

        HttpStatus status = exceptionRuleOptional.map(ExceptionRule::status)
                .orElse(HttpStatus.INTERNAL_SERVER_ERROR);

        return Map.of(
                ApiErrorKeys.CODE.getKey(), status.value(),
                ApiErrorKeys.MESSAGE.getKey(), exception.getMessage(),
                ApiErrorKeys.TIME.getKey(), Timestamp.from(Instant.now())
        );
    }
}
