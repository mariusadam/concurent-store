package com.ubb.ppd.lab4.server.domain.exception;

/**
 * @author Marius Adam
 */
public class StockUnavailableException extends DomainException {
    public StockUnavailableException(String message) {
        super(message);
    }
}
