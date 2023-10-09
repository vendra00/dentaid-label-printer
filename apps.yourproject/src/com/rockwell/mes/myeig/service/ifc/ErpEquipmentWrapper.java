package com.rockwell.mes.myeig.service.ifc;

import com.datasweep.compatibility.client.Location;
import com.datasweep.compatibility.client.Part;
import com.datasweep.compatibility.client.UnitOfMeasure;

/**
 * 
 * Class to manage equipment info
 * 
 * <p>
 * 
 * @author admpharmasuite2, (c) Copyright 2020 Rockwell Automation Technologies, Inc. All Rights Reserved.
 */
public class ErpEquipmentWrapper {

    protected String name;

    protected String description;

    protected String status;

    public ErpEquipmentWrapper(String name, String description, String status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return Returns the status.
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status The status to set.
     */
    public void setStatus(String status) {
        this.status = status;
    }

}
