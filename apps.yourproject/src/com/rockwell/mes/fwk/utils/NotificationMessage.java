package com.rockwell.mes.fwk.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class representing the data to be exchanged.
 * <p>
 * 
 * @author sschotte, (c) Copyright 2010 Rockwell Automation Technologies, Inc. All Rights Reserved.
 */
public class NotificationMessage {

    /** properties: the properties that are being exchanged */
    private Map properties;

    /**
     * ctor
     */
    public NotificationMessage() {
        properties = new HashMap();
    }

    /**
     * Setting a property with key {@code key} and value {@code value}.
     * 
     * @param key String
     * @param value Object
     */

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    /**
     * Getting a property with key {@code key}.
     * 
     * @param key String
     * @return key String
     */
    public Object getProperty(String key) {
        return properties.get(key);
    }

    /**
     * Get the set of property keys.
     * 
     * @return Set
     */
    public Set propertyKeys() {
        return properties.keySet();
    }

    /**
     * Contains a property with {@code key}?
     * 
     * @param key String
     * @return boolean
     */
    public boolean containsKey(String key) {
        return properties.containsKey(key);
    }

}