package com.conectaai.exception;

import com.conectaai.dto.error.ErrorDetailDto;
import com.conectaai.dto.error.ErrorResponseDto;
import com.conectaai.dto.error.ValidationErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleUserAlreadyExists(UserAlreadyExistsException exception) {
        ErrorDetailDto errorDetail = new ErrorDetailDto(
                "USER_ALREADY_EXISTS",
                exception.getMessage()
        );
        ErrorResponseDto errorResponse = new ErrorResponseDto(errorDetail);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFound(UserNotFoundException exception) {
        ErrorDetailDto errorDetail = new ErrorDetailDto(
                "USER_NOT_FOUND",
                exception.getMessage()
        );
        ErrorResponseDto errorResponse = new ErrorResponseDto(errorDetail);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidCredentials(InvalidCredentialsException exception) {
        ErrorDetailDto errorDetail = new ErrorDetailDto(
                "INVALID_CREDENTIALS",
                exception.getMessage()
        );
        ErrorResponseDto errorResponse = new ErrorResponseDto(errorDetail);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(UserInactiveException.class)
    public ResponseEntity<ErrorResponseDto> handleUserInactive(UserInactiveException exception) {
        ErrorDetailDto errorDetail = new ErrorDetailDto(
                "USER_INACTIVE",
                exception.getMessage()
        );
        ErrorResponseDto errorResponse = new ErrorResponseDto(errorDetail);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidRefreshToken(InvalidRefreshTokenException exception) {
        ErrorDetailDto errorDetail = new ErrorDetailDto(
                "INVALID_REFRESH_TOKEN",
                exception.getMessage()
        );
        ErrorResponseDto errorResponse = new ErrorResponseDto(errorDetail);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(InvalidResetTokenException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidResetToken(InvalidResetTokenException exception) {
        ErrorDetailDto errorDetail = new ErrorDetailDto(
                "INVALID_RESET_TOKEN",
                exception.getMessage()
        );
        ErrorResponseDto errorResponse = new ErrorResponseDto(errorDetail);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidPassword(InvalidPasswordException exception) {
        ErrorDetailDto errorDetail = new ErrorDetailDto(
                "INVALID_PASSWORD",
                exception.getMessage()
        );
        ErrorResponseDto errorResponse = new ErrorResponseDto(errorDetail);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationErrors(MethodArgumentNotValidException exception) {
        List<ValidationErrorDto> validationErrors = new ArrayList<>();
        
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            ValidationErrorDto validationError = new ValidationErrorDto(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
            validationErrors.add(validationError);
        }

        ErrorDetailDto errorDetail = new ErrorDetailDto(
                "VALIDATION_ERROR",
                "Erro de validação nos campos enviados",
                validationErrors
        );
        
        ErrorResponseDto errorResponse = new ErrorResponseDto(errorDetail);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ErrorResponseDto> handleUnsupportedOperation(UnsupportedOperationException exception) {
        ErrorDetailDto errorDetail = new ErrorDetailDto(
                "OPERATION_NOT_SUPPORTED",
                exception.getMessage()
        );
        ErrorResponseDto errorResponse = new ErrorResponseDto(errorDetail);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception exception) {
        ErrorDetailDto errorDetail = new ErrorDetailDto(
                "INTERNAL_SERVER_ERROR",
                "Ocorreu um erro inesperado. Por favor, tente novamente mais tarde"
        );
        ErrorResponseDto errorResponse = new ErrorResponseDto(errorDetail);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
