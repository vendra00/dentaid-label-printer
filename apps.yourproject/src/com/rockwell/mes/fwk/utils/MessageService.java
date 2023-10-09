package com.rockwell.mes.fwk.utils; //NO PMD

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;



/**
 * Simplistic message service broadcaster
 * due to patch work, the listener are PropertyChangeListeners
 * <p>
 * @author sschotte, (c) Copyright 2010 Rockwell Automation Technologies, Inc. All Rights Reserved.
 */
public class MessageService {
    
    
    /**
     * singleton for this class
     */
    private static MessageService messageService = null;
    
    
    /**
     * map of channels defined in the system
     */
    private Map channels;
    
    /** Enumeration for channel types */
    public enum ChannelType {
        /** Local : just locally on the client side */
        LOCAL,
        /**  MESSAGEGROUP : the FTPC message group */
        MESSAGEGROUP
        
    }    
    
    /**
     * list of notification message listeners
     */
    private Map<String, List<PropertyChangeListener>> notificationMessageListenersMap = null;
    
    
    /**
     * ctor
     */
    public MessageService() {
        
        channels = new HashMap();
        notificationMessageListenersMap = new HashMap<String, List<PropertyChangeListener>>();
        
    }

    /**
     * can be called in a thread
     * @return messageService singleton
     */
    
    public static synchronized MessageService getMessageService() {
        if (messageService == null) {
            messageService = new MessageService();
        }
        return messageService;
    }
    

    
    
    /**
     * add a new comm channel
     * Note, we currently only support local because Ferring !
     * @param channelId String
     * @param type ChannelType
     */

    public synchronized void addChannel(String channelId, ChannelType type) {
        if (channels.containsKey(channelId)) {
            return; 
            // silently do not support the replacement of a new channel,
            //in production code --> exception should be thrown
            
        }
        List<PropertyChangeListener> list =  new LinkedList<PropertyChangeListener>();
        channels.put(channelId, list);
               
    }  
   
    /**
     * 
     * @param channelId String
     */
    
    public synchronized void removeChannel(String channelId) {
        removeNotificationMessageListeners(channelId);
        channels.remove(channelId);
    }
    
    /**
     * remove the listeners for this channel 
     * @param channelId String
     */
    
    public synchronized void removeNotificationMessageListeners(String channelId) {
        notificationMessageListenersMap.remove(channelId);        
    }
    
    
    /**
     * removing a notificationMessage listener object
     * @param channelId String
     * @param listener INotificationMessageListener
     */
    
    public synchronized void removeNotificationMessageListener(String channelId, 
            PropertyChangeListener listener) {
        if (listener == null) {
            return;
        }
        List<PropertyChangeListener> list = notificationMessageListenersMap.get(channelId);
        if (list == null) {
            return;   
        }
        removeNotificationMessageListener(list, listener);   
    }
    
    /**
     * removing a INotificationMessageListener object
     * @param list List<INotificationMessageListener>
     * @param listener INotificationMessageListener
     */
    protected synchronized void removeNotificationMessageListener(List<PropertyChangeListener> list,
            PropertyChangeListener listener) {
        if (listener == null) {
            return;
        }
        if (list == null) {
            return;
        }
        
        if (list.contains(listener)) {
            list.remove(listener);
        }       
    }   

    /**
     * 
     * @param channelId String
     * @param listener INotificationMessageListener
     */
    
    public synchronized void addNotificationMessageListener(String channelId,
            PropertyChangeListener listener) {
        if (listener == null) {
            return;
        }
        if (notificationMessageListenersMap.containsKey(channelId)) {
            List<PropertyChangeListener> list = notificationMessageListenersMap.get(channelId);
            if (list == null) {
                list = new LinkedList<PropertyChangeListener>();
                
                              
            }
            list = addNotificationMessageListener(list, listener);
            notificationMessageListenersMap.put(channelId, list);  
        }
        else {
            List<PropertyChangeListener> list = new LinkedList<PropertyChangeListener>();
            list.add(listener);
            notificationMessageListenersMap.put(channelId, list);
        }
    }
    
    /**
     * 
     * @param list List<INotificationMessageListener>
     * @param listener INotificationMessageListener
     * @return List<INotificationMessageListener>
     */
    protected synchronized List<PropertyChangeListener> 
      addNotificationMessageListener(List<PropertyChangeListener> list,
              PropertyChangeListener listener) {        
        if (list.contains(listener)) {
            return list;
        }
        list.add(listener);
        return list;
        
    }
    
   /**
    * 
    * @param channelId String
    * @param propertyName String
    * @param oldValue object
    * @param  newValue Object
    */
    public synchronized void notify(String channelId, String propertyName, Object oldValue, Object newValue) { 
        List<PropertyChangeListener> list = notificationMessageListenersMap.get(channelId);
 
        PropertyChangeEvent message = new PropertyChangeEvent(this, propertyName, oldValue, newValue);        
        if (list != null) {
            notify(list, message);
        }        
    }    
    
    /**
     * 
     * @param list List
     * @param message NotificationMessage
     */
    protected synchronized void notify(List<PropertyChangeListener> list, PropertyChangeEvent message) {    
        for (PropertyChangeListener lst : list) {
            lst.propertyChange(message);
        }    
    }
}    