package org.myToySpring.exceptions;

public class ContextInitException extends RuntimeException {

    public ContextInitException(Throwable cause) {
        super(cause);
    }

    public ContextInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContextInitException(String message) {
        super(message);
    }

    public ContextInitException() {
        super();
    }

}
