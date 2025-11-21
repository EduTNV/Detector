package com.projetoA3.detector.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Captura erros de IllegalArgumentException (lançados quando o cartão é inválido,
     * validação de Luhn falha, ou bandeira desconhecida).
     * Retorna 400 Bad Request em vez de 500 ou 401.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        // Retorna a mensagem de erro exata (ex: "Número de cartão inválido.")
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Captura outras exceções de tempo de execução (RuntimeException) genéricas.
     * Também retorna 400 para evitar erros 500 assustadores no frontend.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException ex, WebRequest request) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
    
    // Captura AccessDeniedException para garantir que retorne 403 (Forbidden) e não 401
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(Exception ex, WebRequest request) {
        return new ResponseEntity<>("Acesso negado: " + ex.getMessage(), HttpStatus.FORBIDDEN);
    }
}