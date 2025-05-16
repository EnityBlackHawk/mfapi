package org.example.mfapi.exception;

import lombok.Getter;


public class MfException extends RuntimeException {
    @Getter
    private String log;

    public MfException(Throwable cause, String log) {
        super(cause);
        this.log = log;
    }

}
