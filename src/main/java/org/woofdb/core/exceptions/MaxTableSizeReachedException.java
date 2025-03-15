package org.woofdb.core.exceptions;

public class MaxTableSizeReachedException extends Exception {
    public MaxTableSizeReachedException(final String message) {
        super(message);
    }
}
