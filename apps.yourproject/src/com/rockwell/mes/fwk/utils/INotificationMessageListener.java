package com.rockwell.mes.fwk.utils;


/**
 * Interface for message consumers
 * <p>
 * @author sschotte, (c) Copyright 2010 Rockwell Automation Technologies, Inc. All Rights Reserved.
 */
public interface INotificationMessageListener {
    
    /**
     * notified method called to perform some action
     * @param message NotificationMessage
     */
    public void notified(NotificationMessage message);

}
