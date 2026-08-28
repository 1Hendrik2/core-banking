package com.hmetsallik.corebanking.common;

import com.hmetsallik.corebanking.account.exception.AccountNotFoundException;
import com.hmetsallik.corebanking.account.exception.DuplicateCurrencyException;
import com.hmetsallik.corebanking.common.dto.ErrorResponse;
import com.hmetsallik.corebanking.transaction.exception.CurrencyNotSupportedException;
import com.hmetsallik.corebanking.transaction.exception.InsufficientFundsException;
import com.hmetsallik.corebanking.transaction.exception.InvalidAmountException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleAccountNotFound_returns404WithMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleAccountNotFound(new AccountNotFoundException(UUID.randomUUID()));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("Account not found");
    }

    @Test
    void handleDuplicateCurrency_returns400() {
        ResponseEntity<ErrorResponse> response =
                handler.handleDuplicateCurrency(new DuplicateCurrencyException(List.of()));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleCurrencyNotSupported_returns400() {
        ResponseEntity<ErrorResponse> response = handler.handleCurrencyNotSupported(
                new CurrencyNotSupportedException(UUID.randomUUID(), Currency.EUR));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleInvalidAmount_returns400() {
        ResponseEntity<ErrorResponse> response =
                handler.handleInvalidAmount(new InvalidAmountException(BigDecimal.ZERO));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleInsufficientFunds_returns409() {
        ResponseEntity<ErrorResponse> response = handler.handleInsufficientFunds(
                new InsufficientFundsException(UUID.randomUUID(), Currency.EUR));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void handleValidation_returns400WithStructuredFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("createTransactionRequest", "money", "money is required");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFieldErrors()).hasSize(1);
        assertThat(response.getBody().getFieldErrors().getFirst().getField()).isEqualTo("money");
        assertThat(response.getBody().getFieldErrors().getFirst().getMessage()).isEqualTo("money is required");
    }

    @Test
    void handleUnreadable_returns400() {
        ResponseEntity<ErrorResponse> response =
                handler.handleUnreadable(mock(HttpMessageNotReadableException.class));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Malformed request body");
    }

    @Test
    void handleTypeMismatch_returns400WithFieldName() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("accountId");

        ResponseEntity<ErrorResponse> response = handler.handleTypeMismatch(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("accountId");
    }

    @Test
    void handleDataIntegrity_returns400() {
        ResponseEntity<ErrorResponse> response =
                handler.handleDataIntegrity(mock(DataIntegrityViolationException.class));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleUnexpected_returns500() {
        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Unexpected error");
    }
}
