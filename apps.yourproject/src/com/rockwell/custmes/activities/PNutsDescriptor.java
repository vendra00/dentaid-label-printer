package com.rockwell.custmes.activities;

import java.io.Serializable;

public class PNutsDescriptor implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String name;
    private Class<?> type = Object.class;
    private String defaultValue;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the type
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(Class<?> type) {
        this.type = type;
    }

    /**
     * @return the defaultValue
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * @param defaultValue
     *            the defaultValue to set
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String toString() {
        String typeName = getType() == null ? "void" : getType().getSimpleName();
        return getName() + "(" + typeName + ")";
    }

}
