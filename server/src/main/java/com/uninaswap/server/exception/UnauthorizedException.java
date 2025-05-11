package com.uninaswap.server.exception;

/**
 * Exception thrown when an operation requires authentication but the user is not authenticated.
 */
public class UnauthorizedException extends RuntimeException {
    
    /**
     * Constructs a new unauthorized exception with null as its detail message.
     */
    public UnauthorizedException() {
        super();
    }
    
    /**
     * Constructs a new unauthorized exception with the specified detail message.
     *
     * @param message the detail message
     */
    public UnauthorizedException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new unauthorized exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new unauthorized exception with the specified cause.
     *
     * @param cause the cause
     */
    public UnauthorizedException(Throwable cause) {
        super(cause);
    }
}