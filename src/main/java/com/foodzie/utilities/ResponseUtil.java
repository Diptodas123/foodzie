package com.foodzie.utilities;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ResponseUtil {

    private final ModelMapper modelMapper;

    public ResponseUtil(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <T> ResponseEntity<?> buildResponse(Object data, Class<T> targetClass, String message) {
        return buildResponse(data, targetClass, message, HttpStatus.OK);
    }

    public <T> ResponseEntity<?> buildResponse(Object data, Class<T> targetClass, String message, HttpStatus status) {
        T mapped = modelMapper.map(data, targetClass);
        return buildResponse(mapped, message, status);
    }

    public ResponseEntity<?> buildResponse(Object data, String message) {
        return buildResponse(data, message, HttpStatus.OK);
    }

    public ResponseEntity<?> buildResponse(Object data, String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", message);
        response.put("data", data);
        return ResponseEntity.status(status).body(response);
    }

    public ResponseEntity<?> buildResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", message);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> buildError(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }

    public ResponseEntity<?> buildError(Map<String, String> fieldErrors, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("errors", fieldErrors);
        return ResponseEntity.status(status).body(response);
    }
}
