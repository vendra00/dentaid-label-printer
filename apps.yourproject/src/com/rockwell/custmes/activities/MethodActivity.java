package com.rockwell.custmes.activities;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.rockwell.activity.ItemDescriptor;

/**
 * This activity is capable to invoke an arbitrary method for a object utilizing the super class functionality of method
 * invocation. All methods of variable types of the underlying activity set are available during configuration.
 *
 * @author RWeingar
 */
public class MethodActivity extends InvokeActivity {

    /** The object for which to invoke a method */
    public static final String INPUT_OBJECT = "object";

    @Override
    public Object getImplementation() throws MethodDescriptorException, ClassNotFoundException {
        return getInputItem(INPUT_OBJECT);
    }

    @Override
    public List<MethodDescriptor> getMethodDescriptors() throws NoSuchMethodException {
        List<MethodDescriptor> descs = new ArrayList<MethodDescriptor>();

        for (Class<?> cls : ClassUtils.getClassesForMethodInvocation()) {
            Method[] meths = cls.getMethods();
            for (Method meth : meths) {
                if (Modifier.isPublic(meth.getModifiers())) {
                    MethodDescriptor desc = new MethodDescriptor();
                    desc.setServiceInterface(cls);
                    desc.setMethodName(meth.getName());
                    desc.setBeanName(cls.getSimpleName());
                    desc.setParameterTypes(meth.getParameterTypes());
                    descs.add(desc);
                }
            }
        }

        return descs;
    }

    @Override
    public String getActivityDescription() {
        return "Invokes a method of a given object";
    }

    @Override
    public ItemDescriptor[] inputDescriptors() {
        ItemDescriptor[] superDescs = super.inputDescriptors();
        Class<?> cls = Object.class;
        try {
            cls = this.getInterface();
            Method meth = getMethod();
            if (Modifier.isStatic(meth.getModifiers())) {
                return superDescs;
            }
        } catch (MethodDescriptorException e) {
            return superDescs;
        } catch (NoSuchMethodException e) {
            return superDescs;
        }

        ItemDescriptor[] descs;
        if (superDescs != null) {
            descs = new ItemDescriptor[superDescs.length + 1];
            System.arraycopy(superDescs, 0, descs, 1, superDescs.length);
        } else {
            descs = new ItemDescriptor[1];
        }

        descs[0] = ItemDescriptor.createItemDescriptor(this.getClass(), INPUT_OBJECT, cls, new Object[] {
                ItemDescriptor.SHORTDESCRIPTION, "The method is invoked on this object", ItemDescriptor.DISPLAYNAME,
                "Object" });
        return descs;
    }
}
