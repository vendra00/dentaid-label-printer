package com.rockwell.custmes.helper;

/**
 * Exception for conversion errors
 * <p>
 * 
 * @author rweinga, (c) Copyright 2009 Rockwell Automation Technologies, Inc.
 *         All Rights Reserved.
 */
public class ConversionException extends Exception {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -2558084913457832024L;

    public ConversionException() {
        super();
    }

    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConversionException(String message) {
        super(message);
    }

    public ConversionException(Throwable cause) {
        super(cause);
    }

}
