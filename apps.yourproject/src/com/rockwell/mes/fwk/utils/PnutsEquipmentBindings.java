package com.rockwell.mes.fwk.utils;

import com.datasweep.compatibility.client.Keyed;
import com.rockwell.mes.services.eqm.ifc.GxPContextItemClass;
import com.rockwell.mes.services.eqm.ifc.GxPContextMap;

/**
 * Class to be able to use equipment bindings in Pnuts !!
 * inner enum in pnuts are hard to get !!
 * <p>
 * @author sschotte, (c) Copyright 2010 Rockwell Automation Technologies, Inc. All Rights Reserved.
 */
public class PnutsEquipmentBindings extends GxPContextMap {
    
    /** process order */
    private static final String PROCESSORDER = "ProcessOrder";
    /** */
    private static final String ORDERSTEP = "OrderStep";
    /** */
    private static final String PRODUCTPART = "ProductPart";
    /** */
    private static final String SUBLOT = "Sublot";
    /** */
    private static final String BATCH = "Batch";
    /** */
    private static final String OSI = "OrderStepInput";
    /** */
    private static final String LOCATION = "Location";
    /** */
    private static final String ROOM = "Room";
    /** */
    private static final String MATERIALPART = "MaterialPart";
    /** */
    private static final String WEIGHINGTYPE = "WeighingType";
  
    
    /**
     * Add DB-recored-element to bindings.
     * 
     * @see java.util.HashMap#put()
     * 
     * @param key - The key string value
     * @param theObject - The keyed object
     */
    public void putObject(String key, Keyed theObject) {
        
        
        putObject(getBindKeyFromString(key), theObject);
    }

    /**
     * Add DB-key-element to bindings.
     * 
     * @see java.util.HashMap#put()
     * 
     * @param key - The key
     * @param value - The database key of a keyed object
     */
    public void putKey(String key, Long value) {
        putKey(getBindKeyFromString(key), value);
    }

    /**
     * Add Long-element to bindings.
     * 
     * @see java.util.HashMap#put()
     * 
     * @param key - The key
     * @param longVal - The Long object
     */
    public void putLong(String key, Long longVal) {
        putLong(getBindKeyFromString(key), longVal);
    }   
    

    
    /**
     * 
     * @param key String
     * @return GxPContextItemClass enum value or null
     */
    private GxPContextItemClass getBindKeyFromString(String key) {
        GxPContextItemClass val = null;
        if (key.equals(PROCESSORDER)) {
            val = GxPContextItemClass.ProcessOrder;
        }
        else if (key.equals(ORDERSTEP)) {
            val = GxPContextItemClass.OrderStep;
        }
        else if (key.equals(PRODUCTPART)) {
            val = GxPContextItemClass.ProductPart;
        }
        else if (key.equals(BATCH)) {
            val = GxPContextItemClass.Batch;
        }
        else if (key.equals(SUBLOT)) {
            val = GxPContextItemClass.Sublot;
        }
        else if (key.equals(OSI)) {
            val = GxPContextItemClass.OrderStepInput;
        }
        else if (key.equals(LOCATION)) {
            val = GxPContextItemClass.Location;
        }
        else if (key.equals(ROOM)) {
            val = GxPContextItemClass.Room;
        }
        else if (key.equals(MATERIALPART)) {
            val = GxPContextItemClass.MaterialPart;
        }
        else if (key.equals(WEIGHINGTYPE)) {
            val = GxPContextItemClass.WeighingType;
        }
        else {
            val = null;
        }
        return val;
    }
    
    
    
}
