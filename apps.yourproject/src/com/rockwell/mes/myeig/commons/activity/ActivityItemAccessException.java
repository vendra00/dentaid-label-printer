package com.rockwell.mes.myeig.commons.activity;

/**
 * Exception indicating problems when using annotated activities.
 * <p>
 * 
 * @author syim, (c) Copyright 2012 Rockwell Automation Technologies, Inc. All
 *         Rights Reserved.
 */
public class ActivityItemAccessException extends Exception {

    /** The <code>serialVersionUID</code> */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public ActivityItemAccessException() {
        super();
    }

    /**
     * @param message The message
     * @param cause The cause
     */
    public ActivityItemAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message The message
     */
    public ActivityItemAccessException(String message) {
        super(message);
    }

    /**
     * @param cause The cause
     */
    public ActivityItemAccessException(Throwable cause) {
        super(cause);
    }
}
