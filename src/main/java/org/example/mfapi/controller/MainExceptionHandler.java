package org.example.mfapi.controller;

import org.example.mfapi.dto.ErrorDTO;
import org.example.mfapi.exception.MfException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Arrays;

@ControllerAdvice
public class MainExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleException(Exception e) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setError("Internal Server Error");
        errorDTO.setMessage(e.getMessage());
        errorDTO.setStackTrace(Arrays.toString(e.getStackTrace()));
        return ResponseEntity.status(500).body(errorDTO);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorDTO> handleIllegalArgumentException(IllegalStateException e) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setError("Bad Request");
        errorDTO.setMessage(e.getMessage());
        errorDTO.setStackTrace(Arrays.toString(e.getStackTrace()));
        return ResponseEntity.badRequest().body(errorDTO);
    }

    @ExceptionHandler(MfException.class)
    public ResponseEntity<ErrorDTO> handleMfException(MfException e) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setError("Error on MfCore");
        errorDTO.setMessage(e.getMessage());
        errorDTO.setLog(e.getLog());
        errorDTO.setStackTrace(Arrays.toString(e.getStackTrace()));
        return ResponseEntity.internalServerError().body(errorDTO);
    }

}
