package com.rockwell.custmes.activities;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.datasweep.compatibility.client.Batch;
import com.datasweep.compatibility.client.Part;
import com.datasweep.compatibility.client.Sublot;
import com.rockwell.mes.commons.base.ifc.objectlock.IMESObjectLockService;
import com.rockwell.mes.commons.base.ifc.services.IMESService;
import com.rockwell.mes.services.eqm.ifc.IMESContainerEquipmentService;
import com.rockwell.mes.services.eqm.ifc.IMESEquipmentService;
import com.rockwell.mes.services.eqm.ifc.IMESRoomEquipmentService;
import com.rockwell.mes.services.eqm.ifc.IMESScaleEquipmentService;
import com.rockwell.mes.services.inventory.ifc.IBatchService;
import com.rockwell.mes.services.inventory.ifc.ISublotService;
import com.rockwell.mes.services.order.ifc.IMESOrderService;
import com.rockwell.mes.services.order.ifc.IOrderExplosionService;
import com.rockwell.mes.services.recipe.ifc.IMESRecipeService;

/**
 *
 * @author RWeingar, Hott
 */
public class ClassUtils {

    /**
     * 
     */
    private ClassUtils() {
        // not meant to be instantiated
    }

    /**
     * 
     *
     * @author RWeingar
     */
    private static class ClassConfiguration {
        /**
         * Comment for <code>configuredClass</code>
         */
        private Class<?> configuredClass;
        /**
         * Comment for <code>availableForService</code>
         */
        private boolean availableForService;
        /**
         * Comment for <code>availableForMethodInvocation</code>
         */
        private boolean availableForMethodInvocation;
        /**
         * Comment for <code>availableForPNuts</code>
         */
        private boolean availableForPNuts;

        /**
         * @param cls
         */
        public ClassConfiguration(Class<?> cls) {
            this(cls, true, true, true);
        }

        public ClassConfiguration(Class<?> cls, boolean forService, boolean forMethod, boolean forPNuts) {
            this.configuredClass = cls;
            availableForMethodInvocation = forMethod;
            availableForPNuts = forPNuts;
            availableForService = forService;
            if (!IMESService.class.isAssignableFrom(cls)) {
                availableForService = false;
            }
        }

        /**
         * @return the configuredClass
         */
        public Class<?> getConfiguredClass() {
            return configuredClass;
        }

        /**
         * @return the availableForService
         */
        public boolean isAvailableForService() {
            return availableForService;
        }

        /**
         * @return the availableForMethodInvocation
         */
        public boolean isAvailableForMethodInvocation() {
            return availableForMethodInvocation;
        }

        /**
         * @return the availableForPNuts
         */
        public boolean isAvailableForPNuts() {
            return availableForPNuts;
        }

    }

    /** The list of classes to provide for method invocation */
    private static final Class<?>[] CLASSES = { String.class, Integer.class, Short.class, Long.class, Float.class,
            Double.class, Character.class, Boolean.class, UUID.class, List.class, Map.class, Part.class, Sublot.class,
            Batch.class, IMESObjectLockService.class, ISublotService.class, IBatchService.class,
            Batch.class, IMESObjectLockService.class, ISublotService.class, IBatchService.class,
            IMESEquipmentService.class, IMESContainerEquipmentService.class, IMESScaleEquipmentService.class,
            IMESRecipeService.class, IMESRoomEquipmentService.class, IMESOrderService.class,
            IOrderExplosionService.class };

    private static Class<?>[] pnutsClasses = null;
    private static Class<?>[] methodInvocationClasses = null;
    private static Class<?>[] serviceClasses = null;

    public static Map<Class<?>, ClassConfiguration> getConfiguration() {
        Map<Class<?>, ClassConfiguration> configuration = new HashMap<Class<?>, ClassConfiguration>();
        for (Class<?> cls : CLASSES) {
            configuration.put(cls, new ClassConfiguration(cls));
        }

        return configuration;
    }

    public static Class<?>[] getClassesForPNuts() {
        if (pnutsClasses == null) {
            List<Class<?>> classes = new LinkedList<Class<?>>();
            for (ClassConfiguration conf : getConfiguration().values()) {
                if (conf.isAvailableForPNuts()) {
                    classes.add(conf.getConfiguredClass());
                }
            }

            sort(classes);

            synchronized (ClassUtils.class) {
                Class<?>[] classArray = new Class<?>[classes.size()];
                classes.toArray(classArray);
                pnutsClasses = classArray;
            }
        }

        return pnutsClasses;
    }

    private static void sort(List<Class<?>> classes2) {
        Comparator<Class<?>> cmp = new Comparator<Class<?>>() {
            @Override
            public int compare(Class<?> o1, Class<?> o2) {
                return o1.getName().compareTo(o2.getName());
            }
        };
        Collections.sort(classes2, cmp);
    }

    public static Class<?>[] getClassesForService() {
        if (serviceClasses == null) {
            List<Class<?>> classes = new LinkedList<Class<?>>();
            for (ClassConfiguration conf : getConfiguration().values()) {
                if (conf.isAvailableForService()) {
                    classes.add(conf.getConfiguredClass());
                }
            }
            sort(classes);

            synchronized (ClassUtils.class) {
                Class<?>[] classArray = new Class<?>[classes.size()];
                classes.toArray(classArray);
                serviceClasses = classArray;
            }
        }

        return serviceClasses;
    }

    public static Class<?>[] getClassesForMethodInvocation() {
        if (methodInvocationClasses == null) {
            List<Class<?>> classes = new LinkedList<Class<?>>();
            for (ClassConfiguration conf : getConfiguration().values()) {
                if (conf.isAvailableForMethodInvocation()) {
                    classes.add(conf.getConfiguredClass());
                }
            }
            sort(classes);

            synchronized (ClassUtils.class) {
                Class<?>[] classArray = new Class<?>[classes.size()];
                classes.toArray(classArray);
                methodInvocationClasses = classArray;
            }
        }

        return methodInvocationClasses;
    }
}
