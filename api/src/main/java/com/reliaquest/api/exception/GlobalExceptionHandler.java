package com.reliaquest.api.exception;


import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;


@RestControllerAdvice
public class GlobalExceptionHandler {
private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);


@ExceptionHandler(NotFoundException.class)
public ResponseEntity<?> handleNotFound(NotFoundException ex) {
return ResponseEntity.status(HttpStatus.NOT_FOUND)
.body(Map.of("timestamp", Instant.now().toString(), "error", ex.getMessage()));
}


@ExceptionHandler(WebClientResponseException.class)
public ResponseEntity<?> handleWebClient(WebClientResponseException ex) {
log.warn("Downstream error: status={}, body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
HttpStatus mapped = ex.getStatusCode().is4xxClientError()
? HttpStatus.BAD_GATEWAY
: HttpStatus.SERVICE_UNAVAILABLE;
return ResponseEntity.status(mapped)
.body(Map.of("timestamp", Instant.now().toString(), "error", ex.getMessage()));
}


@ExceptionHandler(Exception.class)
public ResponseEntity<?> handleGeneric(Exception ex) {
log.error("Unexpected error", ex);
return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
.body(Map.of("timestamp", Instant.now().toString(), "error", "Internal error"));
}
}