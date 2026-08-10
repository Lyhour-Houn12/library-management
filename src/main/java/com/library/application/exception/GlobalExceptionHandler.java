package com.library.application.exception;

import com.library.application.payload.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(GenreException.class)
    public ResponseEntity<ApiResponse> handleGenreException(GenreException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(ex.getMessage(), false));
    }
    @ExceptionHandler(BookException.class)
    public ResponseEntity<ApiResponse> handleBookException(Exception ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(ex.getMessage(), false));
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ApiResponse> handleUserException(UserException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(ex.getMessage(), false));
    }
    @ExceptionHandler(SubscriptionPlanException.class)
    public ResponseEntity<ApiResponse> handleSubscriptionPlanException(SubscriptionPlanException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(ex.getMessage(), false));
    }
    @ExceptionHandler(SubscriptionException.class)
    public ResponseEntity<ApiResponse> handleSubscriptionException(SubscriptionException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(ex.getMessage(), false));
    }
    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ApiResponse> handlePaymentHandler(PaymentException ex){
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ApiResponse(ex.getMessage(), false));
    }
    @ExceptionHandler(BadCredentialException.class)
    public ResponseEntity<ApiResponse> handleBadCredentialEvent(BadCredentialException ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse(ex.getMessage(), false));
    }

    @ExceptionHandler(BookLoanException.class)
    public ResponseEntity<ApiResponse> handleBookLoanException(BookLoanException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(ex.getMessage(), false));
    }

    @ExceptionHandler(FineException.class)
    public ResponseEntity<ApiResponse> handleFineException(FineException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(ex.getMessage(), false));
    }
    @ExceptionHandler(ReservationException.class)
    public ResponseEntity<ApiResponse> handleReservationException(ReservationException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(ex.getMessage(), false));
    }
    @ExceptionHandler(WishlistException.class)
    public ResponseEntity<ApiResponse> handleWishlistException(WishlistException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(ex.getMessage(), false));
    }


    @ExceptionHandler(BookReviewException.class)
    public ResponseEntity<ApiResponse> handleBookReviewException(BookReviewException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(ex.getMessage(), false));
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex){
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });
        ValidationErrorResponse response = new ValidationErrorResponse("Validation Failed", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(ex.getMessage(),false));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("An error occurred: " + ex.getMessage(),false));
    }







    public static class ValidationErrorResponse{
        public String message;
        public Map<String, String> errors;

        public ValidationErrorResponse(String message, Map<String, String> errors){
            this.message = message;
            this.errors = errors;
        }
    }
}
