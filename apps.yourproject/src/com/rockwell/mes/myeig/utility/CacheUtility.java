package com.rockwell.mes.myeig.utility;

import com.datasweep.compatibility.client.Batch;
import com.datasweep.compatibility.client.MasterRecipe;
import com.datasweep.compatibility.client.Part;
import com.datasweep.compatibility.client.ProcessOrder;
import com.rockwell.mes.commons.base.ifc.services.PCContext;

/**
 * Utility to disable FTPC caches. This is e.g. necessary within background
 * processes, like gateways, to avoid not to reflect concurrent changes.
 * <p>
 * 
 * @author syim, (c) Copyright 2012 Rockwell Automation Technologies, Inc. All
 *         Rights Reserved.
 */
public class CacheUtility {
    /** constructor */
    private CacheUtility() {
    }

    /**
     * Disable caches of frequently used objects, modified during application usage.
     */
    public static void disableCaches() {
        setCacheEnabled(false);
    }

    /**
     * Enables caches of frequently used objects, modified during application
     * usage.
     */
    public static void enableCaches() {
        setCacheEnabled(true);
    }

    /**
     * Enables or disables the caches of frequently used objects, modified
     * during application usage.
     * 
     * @param on Enable or disable
     */
    public static void setCacheEnabled(boolean on) {
        PCContext.getServerImpl().getSiteCache().getEquipmentCache().setCacheEnabled(on);
        PCContext.getServerImpl().getSiteCache().getEquipmentClassCache().setCacheEnabled(on);
        PCContext.getServerImpl().getSiteCache().getPartCache().setCacheEnabled(on);
        PCContext.getServerImpl().getSiteCache().getWorkCenterCache().setCacheEnabled(on);
        PCContext.getServerImpl().getSiteCache().getBatchCache().setCacheEnabled(on);
        PCContext.getServerImpl().getSiteCache().getSublotCache().setCacheEnabled(on);
        // Maybe also: Carriers, Locations,
    }

    /**
     * Remove an object from the cache
     * 
     * @param object The object
     */
    public static void removeObjectFromCache(Object object) {
        if (object instanceof Part) {
            PCContext.getServerImpl().getSiteCache().getPartCache().removeFromCache(((Part) object).getKey());
        } else if (object instanceof Batch) {
            PCContext.getServerImpl().getSiteCache().getBatchCache().removeFromCache(((Batch) object).getKey());
        } else if (object instanceof ProcessOrder) {
            PCContext.getServerImpl().getSiteCache().getProcessOrderCache()
                    .removeFromCache(((ProcessOrder) object).getKey());
        } else if (object instanceof MasterRecipe) {
            PCContext.getServerImpl().getSiteCache().getMasterRecipeCache()
                    .removeFromCache(((MasterRecipe) object).getKey());
            PCContext.getServerImpl().getSiteCache().getProcessBomCache()
                    .removeFromCache(((MasterRecipe) object).getProcessBOM().getKey());
            PCContext.getServerImpl().getSiteCache().getRouteCache()
                    .removeFromCache(((MasterRecipe) object).getRoute().getKey());
        }
    }
}