package com.rockwell.custmes.activities;

import java.io.Serializable;
import java.util.Arrays;

public class MethodDescriptor implements Serializable {

    private static final long serialVersionUID = 1L;

    private Class<?> serviceInterface;
    private String methodName;
    private Class<?>[] arguments;
    private String beanName;
    private transient String strRepresentation;

    /**
     * @return the serviceInterface
     */
    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

    /**
     * @param serviceInterface
     *            the serviceInterface to set
     */
    public void setServiceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
        strRepresentation = null;
    }

    /**
     * @return the methodName
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * @param methodName
     *            the methodName to set
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
        strRepresentation = null;
    }

    /**
     * @return the arguments
     */
    public Class<?>[] getParameterTypes() {
        return arguments;
    }

    /**
     * @param someArguments
     *            the arguments to set
     */
    public void setParameterTypes(Class<?>[] someArguments) {
        this.arguments = someArguments;
        strRepresentation = null;
    }

    /**
     * @return the beanName
     */
    public String getBeanName() {
        return beanName;
    }

    /**
     * @param beanName
     *            the beanName to set
     */
    public void setBeanName(String beanName) {
        this.beanName = beanName;
        strRepresentation = null;
    }


    @Override
    public String toString() {
        if (strRepresentation != null) {
            return strRepresentation;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(beanName);
        builder.append(".");
        builder.append(methodName);
        builder.append("(");
        Class<?>[] params = getParameterTypes();
        if (params != null) {
            for (int idx = 0; idx < params.length; idx++) {
                if (idx > 0) {
                    builder.append(",");
                }
                builder.append(params[idx].getSimpleName());
            }

        }
        builder.append(")");
        return builder.toString();
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (hashCode() != obj.hashCode()) {
            return false;
        }
        if (!(obj instanceof MethodDescriptor)) {
            return false;
        }
        MethodDescriptor other = (MethodDescriptor) obj;

        if (!comp(getServiceInterface(), other.getServiceInterface())) {
            return false;
        }
        if (!comp(getMethodName(), other.getMethodName())) {
            return false;
        }

        if (!comp(getParameterTypes(), other.getParameterTypes())) {
            return false;
        }
        if (!comp(getBeanName(), other.getBeanName())) {
            return false;
        }

        return true;
    }

    private boolean comp(Object left, Object right) {
        if (left == null) {
            return right == null;
        }

        if (left.getClass().isArray() && right.getClass().isArray()) {
            return Arrays.equals((Object[]) left, (Object[]) right);
        }

        return left.equals(right);
    }


    @Override
    public int hashCode() {
        if (methodName == null || serviceInterface == null) {
            return 0;
        } else {
            return methodName.hashCode() ^ serviceInterface.hashCode();
        }
    }

}
