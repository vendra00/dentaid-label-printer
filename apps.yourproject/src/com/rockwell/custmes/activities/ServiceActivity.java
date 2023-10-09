package com.rockwell.custmes.activities;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.rockwell.mes.commons.base.ifc.services.IMESService;
import com.rockwell.mes.commons.base.ifc.services.ServiceFactory;

/**
 * This activity determines a FTPS service in order to invoke a method there.
 *
 * @author RWeingar
 */
public class ServiceActivity extends InvokeActivity {

    @Override
    public String getActivityDescription() {
        return "Activity to configure and call a service";
    }

    /**
     * Adds elements to the given descriptor list
     *
     * @param descs
     *            descriptor list
     * @param ifc
     *            Service interface for which to invoke a method.
     * @param bean
     *            The bean name of the service.
     */
    private void addElements(List<MethodDescriptor> descs, Class<?> ifc, String bean) {
        try {
            getImplementation(ifc, bean);
        } catch (MethodDescriptorException e) {
            return;
        }
        Method[] meths = ifc.getMethods();
        for (Method meth : meths) {
            MethodDescriptor desc = new MethodDescriptor();
            desc.setServiceInterface(ifc);
            desc.setMethodName(meth.getName());
            desc.setBeanName(bean);
            desc.setParameterTypes(meth.getParameterTypes());
            descs.add(desc);
        }
    }


    @Override
    public List<MethodDescriptor> getMethodDescriptors() throws NoSuchMethodException {
        List<MethodDescriptor> descs = new ArrayList<MethodDescriptor>();
        for (Class<?> cls : ClassUtils.getClassesForService()) {
            String ifcName = cls.getSimpleName();
            String beanName;
            if (ifcName.startsWith("I")) {
                beanName = ifcName.substring(1);
            } else {
                beanName = ifcName;

            }
            addElements(descs, cls, beanName);
        }

        return descs;
    }

    /**
     * Determines a service.
     *
     * @param ifc
     *            Service interface
     * @param bean
     *            Service bean name
     * @return Requested service
     * @throws MethodDescriptorException
     *             In case of an invalid request
     */
    public Object getImplementation(Class<?> ifc, String bean) throws MethodDescriptorException {
        try {
            IMESService service = ServiceFactory.getInstance().getService(ifc, bean);
            return service;
        } catch (RuntimeException exc) {
            throw new MethodDescriptorException(exc);
        }

    }

    @Override
    public Object getImplementation() throws MethodDescriptorException, ClassNotFoundException {
        String beanStr = getMethodDescriptor().getBeanName();
        if (beanStr == null) {
            throw new MethodDescriptorException("No bean name specified");
        }

        return getImplementation(getInterface(), beanStr);
    }
}
