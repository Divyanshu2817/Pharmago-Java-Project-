package com.pharmago.exceptions;

public class PharmacyException extends Exception {

    public PharmacyException(String message) {
        super(message);
    }

    public PharmacyException(String message, Throwable cause) {
        super(message, cause);
    }
}
