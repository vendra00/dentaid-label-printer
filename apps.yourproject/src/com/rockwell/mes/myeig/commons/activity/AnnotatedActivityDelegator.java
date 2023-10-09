package com.rockwell.mes.myeig.commons.activity;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rockwell.activity.ActivityUtility;
import com.rockwell.activity.ItemDescriptor;

/**
 * This class implements functionality in order to analyze annotated activities.
 * Set and get values from an annotated Activity.
 * 
 * This delegator can be used for GUI- and non-GUI activities.
 * <p>
 * 
 * @author syim, (c) Copyright 2012 Rockwell Automation Technologies, Inc. All
 *         Rights Reserved.
 */
public class AnnotatedActivityDelegator {

    /** LOGGER */
    private static final Log LOGGER = LogFactory.getLog(AnnotatedActivityDelegator.class);

    /** The delegators */
    private static Map<Class<?>, AnnotatedActivityDelegator> delegators = 
            new HashMap<Class<?>, AnnotatedActivityDelegator>();

    /**
     * Comment for <code>activityClass</code>
     */
    private Class<?> activityClass;

    /**
     * Comment for <code>initialized</code>
     */
    private volatile boolean initialized = false;

    /**
     * Comment for <code>inputDescs</code>
     */
    private ItemDescriptor[] inputDescs;

    /**
     * Comment for <code>outputDescs</code>
     */
    private ItemDescriptor[] outputDescs;

    /**
     * Comment for <code>configDescs</code>
     */
    private ItemDescriptor[] configDescs;

    /**
     * Comment for <code>properties</code>
     */
    private Map<String, PropertyDescriptor> properties = new HashMap<String, PropertyDescriptor>();

    /**
     * @param clazz The activity class
     */
    private AnnotatedActivityDelegator(Class<?> clazz) {
        activityClass = clazz;
    }

    /**
     * @param clazz The class
     * @return The delegator
     */
    public static synchronized AnnotatedActivityDelegator getDelegator(Class<?> clazz) {
        AnnotatedActivityDelegator delegator = delegators.get(clazz);
        if (delegator == null) {
            delegator = new AnnotatedActivityDelegator(clazz);
            delegator.parseAnnotations();
            delegators.put(clazz, delegator);
        }
        return delegator;
    }

    /**
     * Accesses the property write method of the activity and sets it's value.
     * In case of errors an exception is raised.
     * 
     * @param activity The activity
     * @param name The property name
     * @param value The property value
     * @throws ActivityItemAccessException The thrown exception
     */
    public void setItem(Object activity, String name, Object value) throws ActivityItemAccessException {
        PropertyDescriptor prop = properties.get(name);
        if ((prop == null) || (prop.getWriteMethod() == null)) {
            return;
        }
        try {
            if ((value == null) && prop.getWriteMethod().getParameterTypes()[0].isPrimitive()) {
                // Cannot set null to a primitive type
                return;
            }
            prop.getWriteMethod().invoke(activity, new Object[] { value });
        } catch (IllegalArgumentException e) {
            throw new ActivityItemAccessException(e);
        } catch (IllegalAccessException e) {
            throw new ActivityItemAccessException(e);
        } catch (InvocationTargetException e) {
            throw new ActivityItemAccessException(e);
        }
    }

    /**
     * Accesses the property write method of the activity and sets it's value.
     * In case of errors it simply returns.
     * 
     * @param activity The activity
     * @param name The item name
     * @param value The item value
     */
    public void setItemSafe(Object activity, String name, Object value) {
        try {
            setItem(activity, name, value);
        } catch (ActivityItemAccessException exc) {
            LOGGER.error("setItemSafe", exc);
        }
    }

    /**
     * Accesses the property read method of the activity and returns it's value.
     * In case of errors an exception is raised.
     * 
     * @param activity The activity
     * @param name The property name
     * @return
     * @throws ActivityItemAccessException The thrown exception
     * @return Object
     */
    public Object getItem(Object activity, String name) throws ActivityItemAccessException {
        PropertyDescriptor prop = properties.get(name);
        if ((prop == null) || (prop.getReadMethod() == null)) {
            throw new ActivityItemAccessException("Item not found");
        }
        try {
            Object result = prop.getReadMethod().invoke(activity, (Object[]) null);
            return result;
        } catch (IllegalArgumentException e) {
            throw new ActivityItemAccessException(e);
        } catch (IllegalAccessException e) {
            throw new ActivityItemAccessException(e);
        } catch (InvocationTargetException e) {
            throw new ActivityItemAccessException(e);
        }
    }

    /**
     * Accesses the property read method of the activity and returns it's value.
     * In case of errors null is returned
     * 
     * @param activity The activity
     * @param name The item name
     * @return Object
     */
    public Object getItemSafe(Object activity, String name) {
        try {
            return getItem(activity, name);
        } catch (ActivityItemAccessException exc) {
            LOGGER.error("getItemSafe", exc);
            return null;
        }
    }

    /**
     * Initializes the descriptors for the related class by analyzing it's
     * annotations.
     */
    protected void parseAnnotations() {
        if (initialized) {
            return;
        }
        synchronized (this) {
            if (initialized) {
                return;
            }
            List<ItemDescriptor> configs = new ArrayList<ItemDescriptor>();
            List<ItemDescriptor> inputs = new ArrayList<ItemDescriptor>();
            List<ItemDescriptor> outputs = new ArrayList<ItemDescriptor>();

            BeanInfo bi;
            try {
                bi = Introspector.getBeanInfo(activityClass);
            } catch (IntrospectionException exc) {
                throw new IllegalArgumentException(exc);
            }

            for (PropertyDescriptor prop : bi.getPropertyDescriptors()) {
                Method writeMeth = prop.getWriteMethod();
                Method readMeth = prop.getReadMethod();
                if ((writeMeth != null) && (readMeth != null)) {
                    ActivityConfigItem item = readMeth.getAnnotation(ActivityConfigItem.class);
                    if (item != null) {
                        addDescriptor(prop, configs, item.propertyEditor());
                        continue;
                    }
                }
                if (writeMeth != null) {
                    ActivityInputItem item = writeMeth.getAnnotation(ActivityInputItem.class);
                    if (item != null) {
                        addDescriptor(prop, inputs, null);
                    }
                }

                if (readMeth != null) {
                    ActivityOutputItem item = readMeth.getAnnotation(ActivityOutputItem.class);
                    if (item != null) {
                        addDescriptor(prop, outputs, null);
                    }
                }
            }

            configDescs = new ItemDescriptor[configs.size()];
            configs.toArray(configDescs);
            inputDescs = new ItemDescriptor[inputs.size()];
            inputs.toArray(inputDescs);
            outputDescs = new ItemDescriptor[outputs.size()];
            outputs.toArray(outputDescs);

            initialized = true;
        }
    }

    /**
     * Configure an descriptor during initialization
     * 
     * @param prop The property descriptor
     * @param descs The item descriptors
     * @param propertyEditor The property editor class
     */
    protected void addDescriptor(PropertyDescriptor prop, List<ItemDescriptor> descs, Class<?> propertyEditor) {
        ItemDescriptor desc = new ItemDescriptor(prop.getName(), activityClass, prop.getPropertyType());
        if ((propertyEditor != null) && (propertyEditor != Object.class)) {
            // Non default property editor
            desc.setPropertyEditorClass(propertyEditor);
        }
        descs.add(desc);
        properties.put(prop.getName(), prop);
    }

    /**
     * @return Configuration descriptors for the activity class
     */
    public ItemDescriptor[] configurationDescriptors() {
        return configDescs;
    }

    /**
     * @return Output descriptors for the activity class
     */
    public ItemDescriptor[] outputDescriptors() {
        return outputDescs;
    }

    /**
     * @return Input descriptors for the activity class
     */
    public ItemDescriptor[] inputDescriptors() {
        return inputDescs;
    }

    /**
     * Calculates the configuration utilizing the property values.
     * 
     * @param activity The activity
     * @param config The configuration
     */
    public void initConfiguration(Object activity, Hashtable<?, ?> config) {
        for (ItemDescriptor desc : configurationDescriptors()) {
            Object value = getItemSafe(activity, desc.getName());
            ActivityUtility.putItem(config, desc.getName(), value);
        }
    }
}
