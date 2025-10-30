package com.conectaai.exception;

import com.conectaai.dto.error.ErrorDetailDto;
import com.conectaai.dto.error.ErrorResponseDto;
import com.conectaai.dto.error.ValidationErrorDto;
import com.conectaai.logger.AppLogger;
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

    private static final AppLogger LOGGER = AppLogger.getLogger(GlobalExceptionHandler.class);

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

    @ExceptionHandler(CustomerAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleCustomerAlreadyExists(CustomerAlreadyExistsException exception) {
        ErrorDetailDto errorDetail = new ErrorDetailDto(
                "CUSTOMER_ALREADY_EXISTS",
                exception.getMessage()
        );
        ErrorResponseDto errorResponse = new ErrorResponseDto(errorDetail);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleCustomerNotFound(CustomerNotFoundException exception) {
        ErrorDetailDto errorDetail = new ErrorDetailDto(
                "CUSTOMER_NOT_FOUND",
                exception.getMessage()
        );
        ErrorResponseDto errorResponse = new ErrorResponseDto(errorDetail);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handlePaymentNotFound(PaymentNotFoundException exception) {
        ErrorDetailDto errorDetail = new ErrorDetailDto(
                "PAYMENT_NOT_FOUND",
                exception.getMessage()
        );
        ErrorResponseDto errorResponse = new ErrorResponseDto(errorDetail);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(PaymentAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handlePaymentAlreadyExists(PaymentAlreadyExistsException exception) {
        ErrorDetailDto errorDetail = new ErrorDetailDto(
                "PAYMENT_ALREADY_EXISTS",
                exception.getMessage()
        );
        ErrorResponseDto errorResponse = new ErrorResponseDto(errorDetail);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(InvalidPaymentStatusTransitionException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidPaymentStatusTransition(InvalidPaymentStatusTransitionException exception) {
        ErrorDetailDto errorDetail = new ErrorDetailDto(
                "INVALID_STATUS_TRANSITION",
                exception.getMessage()
        );
        ErrorResponseDto errorResponse = new ErrorResponseDto(errorDetail);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(UnsupportedPaymentMethodException.class)
    public ResponseEntity<ErrorResponseDto> handleUnsupportedPaymentMethod(UnsupportedPaymentMethodException exception) {
        ErrorDetailDto errorDetail = new ErrorDetailDto(
                "UNSUPPORTED_PAYMENT_METHOD",
                exception.getMessage()
        );
        ErrorResponseDto errorResponse = new ErrorResponseDto(errorDetail);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(UnsupportedCurrencyException.class)
    public ResponseEntity<ErrorResponseDto> handleUnsupportedCurrency(UnsupportedCurrencyException exception) {
        ErrorDetailDto errorDetail = new ErrorDetailDto(
                "UNSUPPORTED_CURRENCY",
                exception.getMessage()
        );
        ErrorResponseDto errorResponse = new ErrorResponseDto(errorDetail);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidAmount(InvalidAmountException exception) {
        ErrorDetailDto errorDetail = new ErrorDetailDto(
                "INVALID_AMOUNT",
                exception.getMessage()
        );
        ErrorResponseDto errorResponse = new ErrorResponseDto(errorDetail);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(InvalidDueDateException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidDueDate(InvalidDueDateException exception) {
        ErrorDetailDto errorDetail = new ErrorDetailDto(
                "INVALID_DUE_DATE",
                exception.getMessage()
        );
        ErrorResponseDto errorResponse = new ErrorResponseDto(errorDetail);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(SubscriptionNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleSubscriptionNotFound(SubscriptionNotFoundException exception) {
        ErrorDetailDto errorDetail = new ErrorDetailDto(
                "SUBSCRIPTION_NOT_FOUND",
                exception.getMessage()
        );
        ErrorResponseDto errorResponse = new ErrorResponseDto(errorDetail);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(SubscriptionAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleSubscriptionAlreadyExists(SubscriptionAlreadyExistsException exception) {
        ErrorDetailDto errorDetail = new ErrorDetailDto(
                "SUBSCRIPTION_ALREADY_EXISTS",
                exception.getMessage()
        );
        ErrorResponseDto errorResponse = new ErrorResponseDto(errorDetail);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(InvalidSubscriptionStatusTransitionException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidSubscriptionStatusTransition(InvalidSubscriptionStatusTransitionException exception) {
        ErrorDetailDto errorDetail = new ErrorDetailDto(
                "INVALID_SUBSCRIPTION_STATUS_TRANSITION",
                exception.getMessage()
        );
        ErrorResponseDto errorResponse = new ErrorResponseDto(errorDetail);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException exception) {
        ErrorDetailDto errorDetail = new ErrorDetailDto(
                "INVALID_ARGUMENT",
                exception.getMessage()
        );
        ErrorResponseDto errorResponse = new ErrorResponseDto(errorDetail);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleDataIntegrityViolation(
            org.springframework.dao.DataIntegrityViolationException exception) {
        String message = "Erro de integridade de dados";
        
        String exceptionMessage = exception.getMessage();
        if (exceptionMessage != null) {
            if (exceptionMessage.contains("customer_email_key") || 
                exceptionMessage.contains("idx_customer_email")) {
                message = "Email já cadastrado";
            } else if (exceptionMessage.contains("customer_cpf_key") ||
                       exceptionMessage.contains("cpf")) {
                message = "CPF já cadastrado";
            } else if (exceptionMessage.contains("customer_cnpj_key") ||
                       exceptionMessage.contains("cnpj")) {
                message = "CNPJ já cadastrado";
            } else if (exceptionMessage.contains("customer_external_id_key") ||
                       exceptionMessage.contains("external_id")) {
                message = "ID externo já cadastrado";
            }
        }
        
        ErrorDetailDto errorDetail = new ErrorDetailDto(
                "DUPLICATE_ENTRY",
                message
        );
        ErrorResponseDto errorResponse = new ErrorResponseDto(errorDetail);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception exception) {
        LOGGER.error("handleGenericException", "Erro inesperado: {} - Mensagem: {}", exception.getClass().getName(), exception.getMessage());
        LOGGER.error("handleGenericException", "Stack trace completo:", exception);
        
        ErrorDetailDto errorDetail = new ErrorDetailDto(
                "INTERNAL_SERVER_ERROR",
                "Ocorreu um erro inesperado. Por favor, tente novamente mais tarde"
        );
        ErrorResponseDto errorResponse = new ErrorResponseDto(errorDetail);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
