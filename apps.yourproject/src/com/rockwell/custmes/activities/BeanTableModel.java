package com.rockwell.custmes.activities;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * <p>
 * @author hott, (c) Copyright 2010 Rockwell Automation Technologies, Inc. All Rights Reserved.
 */
public class BeanTableModel<T> implements TableModel {

    /**
     * Comment for <code>LOGGER</code>
     */
    private static final Log LOGGER = LogFactory.getLog(BeanTableModel.class);

    /**
     * Comment for <code>propertyDescriptors</code>
     */
    private List<PropertyDescriptor> propertyDescriptors;

    /**
     * Comment for <code>objects</code>
     */
    private List<T> objects;

    /**
     * Comment for <code>columnIndexes</code>
     */
    private Map<Integer, PropertyDescriptor> columnIndexes;

    /** List of listeners */
    protected EventListenerList listenerList = new EventListenerList();

    /**
     * @param someClazz
     * @throws IntrospectionException
     */
    public BeanTableModel(Class<T> someClazz) throws IntrospectionException {
        // this.clazz = someClazz;
        propertyDescriptors = new ArrayList<PropertyDescriptor>();
        objects = new ArrayList<T>();
        columnIndexes = new HashMap<Integer, PropertyDescriptor>();
        BeanInfo bi = Introspector.getBeanInfo(someClazz);
        PropertyDescriptor[] pds = bi.getPropertyDescriptors();

        int idx = 0;
        for (PropertyDescriptor pd : pds) {
            if (pd.getReadMethod() != null && pd.getWriteMethod() != null) {
                propertyDescriptors.add(pd);
                columnIndexes.put(idx, pd);
                idx++;
            }
        }

    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        listenerList.add(TableModelListener.class, l);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return propertyDescriptors.get(columnIndex).getPropertyType();
    }

    @Override
    public int getColumnCount() {
        return propertyDescriptors.size();
    }

    @Override
    public String getColumnName(int columnIndex) {
        return propertyDescriptors.get(columnIndex).getName();
    }

    @Override
    public int getRowCount() {
        return objects.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        PropertyDescriptor pd = columnIndexes.get(columnIndex);
        Method meth = pd.getReadMethod();
        try {
            return meth.invoke(objects.get(rowIndex));
        } catch (IllegalArgumentException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        } catch (InvocationTargetException e) {
            return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        listenerList.remove(TableModelListener.class, l);
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        PropertyDescriptor pd = columnIndexes.get(columnIndex);
        Method meth = pd.getWriteMethod();
        try {
            meth.invoke(objects.get(rowIndex), value);
        } catch (IllegalArgumentException e) {
            LOGGER.error("illegal argument when invoking a method, exception says "
                    + StringUtils.defaultString(e.getMessage()));
        } catch (IllegalAccessException e) {
            LOGGER.error("illegal access when invoking a method, exception says "
                    + StringUtils.defaultString(e.getMessage()));
        } catch (InvocationTargetException e) {
            LOGGER.error("problem when invoking a method, exception says " + StringUtils.defaultString(e.getMessage()));
        }
    }

    /**
     * @param lst
     */
    public void setObjects(List<T> lst) {
        objects = lst;
    }

    /**
     * @param obj
     */
    public void addItem(T obj) {
        objects.add(obj);
        fireTableChanged(new TableModelEvent(this));
    }

    /**
     * Forwards the given notification event to all
     * <code>TableModelListeners</code> that registered themselves as listeners
     * for this table model.
     * 
     * @param e the event to be forwarded
     * 
     * @see #addTableModelListener
     * @see TableModelEvent
     * @see EventListenerList
     */
    public void fireTableChanged(TableModelEvent e) {
        // Guaranteed to return a non-null array
        TableModelListener[] listeners = listenerList.getListeners(TableModelListener.class);

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 1; i >= 0; i--) {
            listeners[i].tableChanged(e);
        }
    }

    /**
     * @param row
     */
    public void removeItem(int row) {
        if (row >= 0 && row < getRowCount()) {
            objects.remove(row);
            fireTableChanged(new TableModelEvent(this));
        }
    }

}
