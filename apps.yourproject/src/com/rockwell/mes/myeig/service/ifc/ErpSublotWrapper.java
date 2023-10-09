package com.rockwell.mes.myeig.service.ifc;

import com.datasweep.compatibility.client.Location;
import com.datasweep.compatibility.client.Part;
import com.datasweep.compatibility.client.UnitOfMeasure;

/**
 * 
 * Class to manage sublot info
 * 
 * <p>
 * 
 * @author admpharmasuite2, (c) Copyright 2020 Rockwell Automation Technologies, Inc. All Rights Reserved.
 */
public class ErpSublotWrapper {

    protected Part material;

    protected String batchName;

    protected String quantity;

    protected UnitOfMeasure uom;

    protected String sublot;

    protected Location storageLocation;

    public ErpSublotWrapper(String batchName, Part material, String quantity, Location storageLocation, String sublot, UnitOfMeasure uom) {
        this.batchName = batchName;
        this.material = material;
        this.quantity = quantity;
        this.storageLocation = storageLocation;
        this.sublot = sublot;
        this.uom = uom;
    }

    /**
     * @return Returns the material.
     */
    public Part getMaterial() {
        return material;
    }

    /**
     * @param material The material to set.
     */
    public void setMaterial(Part material) {
        this.material = material;
    }

    /**
     * @return Returns the batchName.
     */
    public String getBatchName() {
        return batchName;
    }

    /**
     * @param batchName The batchName to set.
     */
    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    /**
     * @return Returns the quantity.
     */
    public String getQuantity() {
        return quantity;
    }

    /**
     * @param quantity The quantity to set.
     */
    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    /**
     * @return Returns the uom.
     */
    public UnitOfMeasure getUom() {
        return uom;
    }

    /**
     * @param uom The uom to set.
     */
    public void setUom(UnitOfMeasure uom) {
        this.uom = uom;
    }

    /**
     * @return Returns the sublot.
     */
    public String getSublot() {
        return sublot;
    }

    /**
     * @param sublot The sublot to set.
     */
    public void setSublot(String sublot) {
        this.sublot = sublot;
    }

    /**
     * @return Returns the storageLocation.
     */
    public Location getStorageLocation() {
        return storageLocation;
    }

    /**
     * @param storageLocation The storageLocation to set.
     */
    public void setStorageLocation(Location storageLocation) {
        this.storageLocation = storageLocation;
    }

}
