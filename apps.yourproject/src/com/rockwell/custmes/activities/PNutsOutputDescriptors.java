package com.rockwell.custmes.activities;

public class PNutsOutputDescriptors extends PNutsInputDescriptors {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Class<?> returnValue;

    /**
     * @return the returnValue
     */
    public Class<?> getReturnValue() {
        return returnValue;
    }

    /**
     * @param returnValue
     *            the returnValue to set
     */
    public void setReturnValue(Class<?> returnValue) {
        this.returnValue = returnValue;
    }

    @Override
    public String toString() {
        if (getReturnValue() != null) {
            return getReturnValue().getSimpleName() + ":" + super.toString();
        } else {
            return "void:" + super.toString();
        }
    }

}
