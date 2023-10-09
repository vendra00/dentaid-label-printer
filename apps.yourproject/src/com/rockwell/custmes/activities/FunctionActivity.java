package com.rockwell.custmes.activities; // NOPMD

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.rockwell.mes.commons.base.ifc.services.PCContext;

/**
 * This activity performs a ProductionCentre function call by utilizing functionality of the super class.
 *
 * The function can be configured during build-time and is executed during runtime then.
 *
 * See {@link IFunctionEx} interface for the possible methods.
 *
 * @author RWeingar
 */
public class FunctionActivity extends InvokeActivity {

    @Override
    public String getActivityDescription() {
        return "Invokes a function method";
    }


    @Override
    public List<MethodDescriptor> getMethodDescriptors() throws NoSuchMethodException {
        List<MethodDescriptor> result = new ArrayList<MethodDescriptor>();
        Class<?> funcsEx = PCContext.class.getMethod("getFunctions").getReturnType();
        Method[] meths = funcsEx.getMethods();

        for (Method meth : meths) {
            MethodDescriptor desc = new MethodDescriptor();
            desc.setServiceInterface(funcsEx);
            desc.setMethodName(meth.getName());
            desc.setBeanName(funcsEx.getSimpleName());
            desc.setParameterTypes(meth.getParameterTypes());

            result.add(desc);
        }

        return result;
    }

    @Override
    public Object getImplementation() throws MethodDescriptorException {
        Object result = PCContext.getFunctions();
        if (result == null) {
            throw new MethodDescriptorException("Cannot access FunctionsEx interface");

        }
        return result;
    }

}
