package com.rockwell.mes.myeig.service.ifc;

import java.util.List;
import java.util.Map;

import com.datasweep.compatibility.client.DatasweepException;
import com.datasweep.compatibility.client.Location;
import com.datasweep.compatibility.client.MeasuredValue;
import com.datasweep.compatibility.client.Part;
import com.datasweep.compatibility.client.UnitOfMeasure;
import com.rockwell.mes.commons.base.ifc.exceptions.MESException;
import com.rockwell.mes.commons.base.ifc.services.IMESService;
import com.rockwell.mes.services.inventory.ifc.BatchBuilder;

/**
 * This service provides functionality for EIG in-bound message processing.
 * <p>
 * 
 * @author syim, (c) Copyright 2012 Rockwell Automation Solutions, Inc. All
 *         Rights Reserved.
 */
public interface IInboundMessageService extends IErpMessageService, IMESService {

    /** Transaction history comments */
    public static final String TRANS_HIST_COMMENT = "Gateway";

    /** The inbound event logging table */
    public static final String AT_PC_INT_INBOUND_EVENT_LOG = "AT_PC_INT_INBOUND_EVENT_LOG";

    /**
     * Creates the PharmaSuite material from ERP received data
     * 
     * @param number Material number
     * @param desc Material description
     * @param udas Map of part UDAs to be saved (key should start with X_ or C_)
     * @throws DatasweepException thrown when error occurs
     */
    public void createERPMaterial(String number, String desc, Map<String, Object> udas, String HazardDataString, String PrecautionaryDataString)
            throws DatasweepException;

    /**
     * Creates the ERP BOM/BOM items
     * 
     * @param material The BOM material
     * @param method The BOM method
     * @param baseQty The base quantity
     * @param items The List of bom item properties
     * @throws DatasweepException thrown when error occurs
     */
    public void createERPBom(Part material, String method, MeasuredValue baseQty, List<Map<String, Object>> items)
            throws DatasweepException;

    /**
     * Creates the PharmaSuite batch from ERP received data
     * 
     * @param builder The batch builder
     * @param transition The transition to apply
     * @throws DatasweepException thrown when error occurs
     * @throws MESException thrown when error occurs
     */
    public void createERPBatch(BatchBuilder builder, String transition) throws DatasweepException, MESException;

    /**
     * Update the PharmaSuite batch status from ERP received data
     * 
     * @param builder The batch builder
     * @param transition The transition to apply
     * @throws DatasweepException thrown when error occurs
     * @throws MESException thrown when error occurs
     */
    public void updateERPBatchStatus(BatchBuilder builder, String transition) throws DatasweepException, MESException;

    /**
     * Creates and explodes ERP process order
     * 
     * @param builder The order builder
     * @return
     * @throws DatasweepException thrown when error occurs
     * @throws MESException thrown when error occurs
     */
    public void createERPOrder(ErpOrderBuilder builder) throws DatasweepException, MESException;

    /**
     * RDG /** Explode ERP process order
     * 
     * @param builder The order builder
     * @return
     * @throws DatasweepException thrown when error occurs
     * @throws MESException thrown when error occurs
     */
    public void explodeERPOrder(ErpOrderBuilder builder) throws DatasweepException, MESException;

    /**
     * SGO /** Delete ERP process order
     * 
     * @param builder The order builder
     * @return
     * @throws DatasweepException thrown when error occurs
     * @throws MESException thrown when error occurs
     */

    public void deleteERPOrder(ErpOrderBuilder builder) throws DatasweepException, MESException;
    // RM 17/03/2023 -----------------------------------  release ERP ORDER---------------------------------
    /**
     * RMM /** Release ERP process order
     * 
     * @param builder The order builder
     * @return
     * @throws DatasweepException thrown when error occurs
     * @throws MESException thrown when error occurs
     */

    public void releaseERPOrder(ErpOrderBuilder builder) throws DatasweepException, MESException;
    // RM 17/03/2023 ----------------------------------------------------------------------------------------------
    /**
     * Creates the PharmaSuite sublot from ERP received data
     * 
     * @param batchName
     * @param sublot
     * @param quantity
     * @param uom
     * @param storageLocation
     * @param material
     * @throws DatasweepException thrown when error occurs
     * @throws MESException thrown when error occurs
     */
    public void createERPSublot(String batchName, String sublot, String quantity, UnitOfMeasure uom, Location storageLocation, Part material)
            throws DatasweepException, MESException;

    /**
     * Creates the PharmaSuite sublot from ERP received data
     * 
     * @param sublots
     * @throws DatasweepException thrown when error occurs
     * @throws MESException thrown when error occurs
     */
    public void createERPSublots(List<ErpSublotWrapper> sublots)
            throws DatasweepException, MESException;

    /**
     * Creates the PharmaSuite equipment from ERP received data
     * 
     * @param name
     * @param description
     * @param status
     * @throws DatasweepException thrown when error occurs
     * @throws MESException thrown when error occurs
     */
    public void createERPEquipment(String name, String description, String status) throws DatasweepException, MESException;

    /**
     * Creates the PharmaSuite equipment from ERP received data
     * 
     * @param equipments
     * @throws DatasweepException thrown when error occurs
     * @throws MESException thrown when error occurs
     */
    public void createERPEquipments(List<ErpEquipmentWrapper> equipments) throws DatasweepException, MESException;

}