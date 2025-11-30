package com.management_system.exception;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.management_system.dto.response.ErrorResponse;
import com.management_system.dto.response.MessageResponse;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import jakarta.validation.ConstraintViolationException;

@RestController
public class RestExceptionHandler {
    /** Validation constraint violation error (annotation @Valid, @NotBlank, …) */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ErrorResponse handleConstraintViolationException(ConstraintViolationException e) {
        return new ErrorResponse(e.getMessage());
    }

    /** Validation error in request body */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<String> errors = new ArrayList<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        return new ErrorResponse(errors);
    }

    /** Error IO */
    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected MessageResponse handleIOException(IOException e) {
        return new MessageResponse(e.getMessage());
    }
 
    /** Duplicate code error */
    @ExceptionHandler(DuplicateCodeException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    protected MessageResponse handleDuplicateCodeException(DuplicateCodeException e) {
        return new MessageResponse(e.getMessage());
    }

    /** Error data not found */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    protected MessageResponse handleEntityNotFoundException(EntityNotFoundException e) {
        return new MessageResponse(e.getMessage());
    }

    /** Business error */
    @ExceptionHandler(BusinessLogicException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected MessageResponse handleBusinessLogicException(BusinessLogicException e) {
        return new MessageResponse(e.getMessage());
    }

    /** Other runtime errors (fallback) */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected MessageResponse handleRuntimeException(RuntimeException e) {
        return new MessageResponse("Unexpected error: " + e.getMessage());
    }

    /** Optimistic Conflict Error */
    @ExceptionHandler(OptimisticLockException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public MessageResponse handleConflict(OptimisticLockException e) {
        return new MessageResponse(e.getMessage());
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public MessageResponse handleForbidden(ForbiddenOperationException e) {
        return new MessageResponse(e.getMessage());
    }
}
