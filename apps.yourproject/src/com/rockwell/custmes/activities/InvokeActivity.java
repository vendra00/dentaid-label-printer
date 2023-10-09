package com.rockwell.custmes.activities;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.client.Response;
import com.datasweep.compatibility.manager.ServerImpl;
import com.datasweep.plantops.property.editor.EnumPropertyEditor;
import com.rockwell.activity.ItemDescriptor;
import com.rockwell.activityset.ActivitySetUtility;
import com.rockwell.activityset.PersistentSimpleType;
import com.rockwell.mes.clientfw.commons.ifc.view.ActivitySupport;

/**
 * This abstract base class provides a fundamental implementation in order to invoke an arbitrary method of an arbitrary
 * object. Reflection is utilized for this. The configuration consists of a
 * {@link #com.rockell.mes.activities.MethodDescriptor} to specify the relevant method. Input/Output descriptors are
 * calculated dependent on the given configuration. Input items are used as arguments, while the output item reflects
 * the result of the method call. Input/Output items can be mapped via standard functionality in activity sets.
 *
 * During run-time the specified method is executed with the mapped input parameters and returns a given output
 * parameter. If the output is a Response object this is also returned as result.
 *
 * Derived classes need to implement the abstract methods in order to provide a implementation and a list of method
 * descriptors for easy utilization.
 *
 * @author RWeingar
 */
public abstract class InvokeActivity extends ActivitySupport {

    private static final Log LOGGER = LogFactory.getLog(InvokeActivity.class);

    /** The method specification to invoke during execution */
    public static final String CONFIG_METHOD = "method";
    /** The return value as output item for the string */
    public static final String OUTPUT_RETURNVALUE = "returnValue";
    /** Prefix for input items used as arguments for the method call */
    public static final String INPUT_ARGUMENT_PREFIX = "arg";
    /** Prefix for configuration default values */
    public static final String CONFIG_DEFAULT_PREFIX = "default";

    private static final int NO_ELEMENTS_PER_DESC = 3;

    {
        // We have to register simple types, other wise we are not able to
        // transform the default parameters from strings
        ActivitySetUtility.registerVariablePersistentConverter(Boolean.TYPE, PersistentSimpleType.class);
        ActivitySetUtility.registerVariablePersistentConverter(Byte.TYPE, PersistentSimpleType.class);
        ActivitySetUtility.registerVariablePersistentConverter(Short.TYPE, PersistentSimpleType.class);
        ActivitySetUtility.registerVariablePersistentConverter(Integer.TYPE, PersistentSimpleType.class);
        ActivitySetUtility.registerVariablePersistentConverter(Long.TYPE, PersistentSimpleType.class);
        ActivitySetUtility.registerVariablePersistentConverter(Float.TYPE, PersistentSimpleType.class);
        ActivitySetUtility.registerVariablePersistentConverter(Double.TYPE, PersistentSimpleType.class);
        ActivitySetUtility.registerVariablePersistentConverter(Character.TYPE, PersistentSimpleType.class);
    }

    /**
     * @return Configured method descriptor for this activity.
     * @throws MethodDescriptorException
     *             If no descriptor is specified
     */
    protected MethodDescriptor getMethodDescriptor() throws MethodDescriptorException {
        MethodDescriptor desc = (MethodDescriptor) getConfigurationItem(CONFIG_METHOD);
        if (desc == null) {
            throw new MethodDescriptorException("ServiceMethod is not specified");
        }
        return desc;
    }

    /**
     * @return Class of the configured method
     * @throws MethodDescriptorException
     *             if no class is specified
     */
    protected Class<?> getInterface() throws MethodDescriptorException {
        Class<?> ifc = getMethodDescriptor().getServiceInterface();
        if (ifc == null) {
            throw new MethodDescriptorException("No service interface specified");
        }
        return ifc;
    }

    /**
     * @return method as determined by reflection
     * @throws NoSuchMethodException
     *             If the method does not exist
     * @throws MethodDescriptorException
     *             if the method is unspecified
     */
    protected Method getMethod() throws NoSuchMethodException, MethodDescriptorException {
        String methStr = getMethodDescriptor().getMethodName();
        if (methStr == null) {
            throw new MethodDescriptorException("No method name specified");
        }
        Class<?> ifc = getInterface();
        Method meth = ifc.getMethod(methStr, getMethodDescriptor().getParameterTypes());

        return meth;
    }


    @Override
    protected Response doWork() {

        try {
            Object obj = getImplementation();

            Method meth = getMethod();
            Class<?>[] paramTypes = meth.getParameterTypes();
            Object[] params = new Object[paramTypes.length];
            for (int idx = 0; idx < paramTypes.length; idx++) {
                Object arg = getInputItem(INPUT_ARGUMENT_PREFIX + idx);
                if (arg == null) {
                    String defaultValue = (String) getConfigurationItem(CONFIG_DEFAULT_PREFIX + idx);
                    Class<?> cls = paramTypes[idx];
                    if (cls.isPrimitive()) {
                        LOGGER.debug(cls.getSimpleName());
                        LOGGER.debug(Boolean.TYPE);
                    }

                    ServerImpl server = getParent().getEnvironmentGeneral().getServer();
                    Class<?> pc = ActivitySetUtility.getVariablePersistentClass(server, cls);
                    if (pc != null) {
                        arg = ActivitySetUtility.getValueByString(server, cls, defaultValue, pc);
                    }
                }
                params[idx] = arg;
            }

            Object result = meth.invoke(obj, params);
            setOutputItem(OUTPUT_RETURNVALUE, result);
            if (result instanceof Response) {
                return (Response) result;
            }

            return new Response();
        } catch (ClassNotFoundException exc) {
            return createErrorResponse(exc);
        } catch (SecurityException exc) {
            return createErrorResponse(exc);
        } catch (NoSuchMethodException exc) {
            return createErrorResponse(exc);
        } catch (IllegalArgumentException exc) {
            return createErrorResponse(exc);
        } catch (IllegalAccessException exc) {
            return createErrorResponse(exc);
        } catch (InvocationTargetException exc) {
            return createErrorResponse(exc);
        } catch (MethodDescriptorException exc) {
            return createErrorResponse(exc);
        } catch (Throwable exc) { // NOPMD hpl - paranoid mode, catch everything
            return createErrorResponse(exc);
        }
    }

    @Override
    public ItemDescriptor[] configurationDescriptors() {
        try {
            List<MethodDescriptor> descList = getMethodDescriptors();
            sortMethodDescriptors(descList);
            Object[] objs = new Object[NO_ELEMENTS_PER_DESC * descList.size()];

            for (int idx = 0; idx < descList.size(); idx++) {
                MethodDescriptor desc = descList.get(idx);
                objs[NO_ELEMENTS_PER_DESC * idx] = desc.toString();
                objs[NO_ELEMENTS_PER_DESC * idx + 1] = desc;
                objs[NO_ELEMENTS_PER_DESC * idx + 2] = desc.toString();
            }

            ItemDescriptor[] descs;
            try {
                Class<?>[] params = getMethodDescriptor().getParameterTypes();

                descs = new ItemDescriptor[1 + params.length];
                for (int idx = 0; idx < params.length; idx++) {
                    Class<?> type = params[idx];
                    descs[idx + 1] = ItemDescriptor
                            .createItemDescriptor(
                                    getClass(),
                                    CONFIG_DEFAULT_PREFIX + idx,
                                    String.class,
                                    new Object[] {
                                            ItemDescriptor.SHORTDESCRIPTION,
                                            "Default value for argument "
                                                    + idx
                                                    + ". It has to be specified in a way that the string can be"
                                                    + " converted to the needed type during execution.",
                                            ItemDescriptor.DISPLAYNAME,
                                            "Default " + idx + " (" + type.getSimpleName() + ")" });
                }
            } catch (MethodDescriptorException exc) {
                descs = new ItemDescriptor[1];
            }

            descs[0] = ItemDescriptor.createItemDescriptor(getClass(), CONFIG_METHOD, MethodDescriptor.class,
                    new Object[] { ItemDescriptor.SHORTDESCRIPTION, "Method specification",
                            ItemDescriptor.PROPERTYEDITORCLASS, EnumPropertyEditor.class,
                            ItemDescriptor.ENUMERATIONVALUES, objs, ItemDescriptor.DISPLAYNAME, "Method" });
            return descs;
        } catch (SecurityException e) {
            return null;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private void sortMethodDescriptors(List<MethodDescriptor> descList) {
        Collections.sort(descList, new Comparator<MethodDescriptor>() {
            @Override
            public int compare(MethodDescriptor o1, MethodDescriptor o2) {
                if (o1 == null || o2 == null) {
                    return 1;
                }
                return o1.toString().compareTo(o2.toString());
            }
        });
    }


    @Override
    public ItemDescriptor[] inputDescriptors() {
        try {
            Method meth = getMethod();
            Class<?>[] paramTypes = meth.getParameterTypes();
            ItemDescriptor[] descs = new ItemDescriptor[paramTypes.length];
            for (int idx = 0; idx < paramTypes.length; idx++) {
                ItemDescriptor desc = ItemDescriptor.createItemDescriptor(this.getClass(), INPUT_ARGUMENT_PREFIX + idx,
                        paramTypes[idx], new Object[] { ItemDescriptor.SHORTDESCRIPTION, "Method argument " + idx,
                                ItemDescriptor.DISPLAYNAME, "Argument " + idx });
                descs[idx] = desc;
            }
            return descs;
        } catch (SecurityException e) {
            return null;
        } catch (NoSuchMethodException e) {
            return null;
        } catch (MethodDescriptorException e) {
            return null;
        }
    }


    @Override
    public ItemDescriptor[] outputDescriptors() {
        Method meth;
        try {
            meth = getMethod();
            Class<?> cls = meth.getReturnType();
            if (cls == null || cls.equals(Void.TYPE)) {
                return null;
            }

            return new ItemDescriptor[] { ItemDescriptor.createItemDescriptor(this.getClass(), OUTPUT_RETURNVALUE, cls,
                    new Object[] { ItemDescriptor.SHORTDESCRIPTION, "Return value", ItemDescriptor.DISPLAYNAME,
                            "Return value" }) };

        } catch (SecurityException e) {
            return null;
        } catch (NoSuchMethodException e) {
            return null;
        } catch (MethodDescriptorException e) {
            return null;
        }

    }

    /**
     * Determines a list of MethodDescriptors in order to provide these the used
     * to select it during configuration.
     *
     * Implementations should be optimized for performance, since this is called
     * several times during build-time but also runtime.
     *
     * @return Method Descriptors available for selection during configuration.
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public abstract List<MethodDescriptor> getMethodDescriptors() throws NoSuchMethodException;

    /**
     * Determines the object for which to invoke the method. This might be null
     * for static methods.
     *
     * @return The object for which to invoke a method
     * @throws MethodDescriptorException
     * @throws ClassNotFoundException
     */
    public abstract Object getImplementation() throws MethodDescriptorException, ClassNotFoundException;
}
