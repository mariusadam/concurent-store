package com.ubb.ppd.lab4.server.domain.exception;

/**
 * @author Marius Adam
 */
public class DomainException extends RuntimeException {
    public DomainException() {
    }

    public DomainException(String message) {
        super(message);
    }
}
