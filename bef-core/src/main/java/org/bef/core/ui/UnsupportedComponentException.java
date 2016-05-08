package org.bef.core.ui;

/**
 * Created by enrico on 5/3/16.
 */
public class UnsupportedComponentException extends Exception {
    public UnsupportedComponentException() {
    }

    public UnsupportedComponentException(String message) {
        super(message);
    }

    public UnsupportedComponentException(String message, Throwable cause) {
        super(message, cause);
    }
}
