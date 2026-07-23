package com.ecommerce.app.exception;


import com.ecommerce.app.payload.ErrorResponseDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Map<String,Object>> clientException(HttpClientErrorException e)
    {
        Map<String,Object> response = new HashMap<>();
        response.put("Message",e.getMessage());
        response.put("TimeStamp",LocalDateTime.now());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(APIException.class)
    public ResponseEntity<Map<String,String>> myApiException(APIException e)
    {
        Map<String,String> response = new HashMap<>();
        response.put("Type",e.type);
        response.put("Message",e.exceptionMessage);
        response.put("TimeStamp",e.timestamp.toString());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> validationException(ConstraintViolationException e)
    {

        Map<String,String> errors = e.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        error -> error.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));

        ErrorResponseDTO response = new ErrorResponseDTO("Validation.","Validation Failed.",errors,LocalDateTime.now());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> resourceNotFound(ResourceNotFoundException e)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<String> resourceAlreadyExists(ResourceAlreadyExistsException e)
    {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}
