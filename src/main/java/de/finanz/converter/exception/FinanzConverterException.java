package de.finanz.converter.exception;

public class FinanzConverterException extends RuntimeException {
    public FinanzConverterException(Throwable cause) {
        super(cause);
    }

    public FinanzConverterException(String message) {
        super(message);
    }
}
