package org.javacode.wallet.exceptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.javacode.wallet.exceptions.exceptiontype.NotFoundException;
import org.javacode.wallet.exceptions.exceptiontype.BadRequestException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
public class ExceptionHandler {
    @ResponseStatus(NOT_FOUND)
    @org.springframework.web.bind.annotation.ExceptionHandler(NotFoundException.class)
    public ErrorResponse handleAllExceptionsNotFound(NotFoundException e) {
        return ErrorResponse.builder()
                .errorCode(NOT_FOUND.value())
                .errorMessage(e.getMessage())
                .build();
    }

    @ResponseStatus(BAD_REQUEST)
    @org.springframework.web.bind.annotation.ExceptionHandler(BadRequestException.class)
    public ErrorResponse handleValidateException(BadRequestException e) {
        return ErrorResponse.builder()
                .errorCode(BAD_REQUEST.value())
                .errorMessage(e.getMessage())
                .build();
    }

    @ResponseStatus(BAD_REQUEST)
    @org.springframework.web.bind.annotation.ExceptionHandler(HttpMessageNotReadableException.class)
    public ErrorResponse handleValidationExceptions() {
        return ErrorResponse.builder()
                .errorCode(BAD_REQUEST.value())
                .errorMessage("Невозможно десериализовать запрос. Вероятно, ошибка в типе операции")
                .build();
    }

    @Builder
    @Getter
    @AllArgsConstructor
    public static class ErrorResponse {
        private int errorCode;
        private String errorMessage;
    }
}
