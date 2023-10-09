package com.rockwell.mes.myeig.service.ifc;

import java.util.List;

import com.datasweep.compatibility.client.Batch;
import com.datasweep.compatibility.client.DatasweepException;
import com.datasweep.compatibility.client.MeasuredValue;
import com.datasweep.compatibility.client.OrderStep;
import com.datasweep.compatibility.client.OrderStepInput;
import com.datasweep.compatibility.client.Sublot;
import com.rockwell.mes.commons.base.ifc.exceptions.MESException;
import com.rockwell.mes.commons.base.ifc.services.IMESService;
import com.rockwell.mes.myeig.data.AskForMaterialTransferObject;
import com.rockwell.mes.myeig.data.ConsumptionTransferObject;
import com.rockwell.mes.myeig.data.ConsumptionTransferObject.ConsumedItem;
import com.rockwell.mes.myeig.data.OrderStatusTransferObject;
import com.rockwell.mes.myeig.data.ProductionTransferObject;
import com.rockwell.mes.myeig.data.ProductionTransferObject.ProducedItem;
import com.rockwell.mes.myeig.data.RelocationMaterialTransferObject;

/**
 * Interface of service API for ERP interface messaging
 * 
 * @author syim, (c) Copyright 2012 Rockwell Automation Solutions, Inc. All
 *         Rights Reserved.
 * 
 */
public interface IOutboundMessageService extends IErpMessageService, IMESService {

    /** The PCA_EVENTS table name */
    public static final String PCA_EVENTS = "PCA_EVENTS";

    /**
     * Sends a consumption message to ERP (Goods Issue - Order).
     * 
     * @param order The order number
     * @param batch The batch
     * @param quantity The quantity consumed
     * @param rsnum The reservation number
     * @param rspos The reservation item number
     * @return key of persisted object MESMyEigPersistentObjects
     * @throws MESException On Error.
     * @throws DatasweepException On Error.
     */
    public Long createConsumptionMessage(String order, Batch batch, MeasuredValue quantity, String rsnum,
            String rspos) throws MESException, DatasweepException;

    /**
     * Sends a consumption message to ERP (Goods Issue - Order).
     * 
     * @param info The outbound consumption message object
     * @return key of persisted object MESMyEigPersistentObjects
     * @throws MESException On Error.
     * @throws DatasweepException On Error.
     */
    public Long createConsumptionMessage(ConsumptionTransferObject info) throws MESException,
            DatasweepException;

    /**
     * Sends a consumption message to ERP (Goods Issue - Order).
     * 
     * @param orderStepInput
     * @return key of persisted object MESMyEigPersistentObjects
     * @throws MESException on Error
     * @throws DatasweepException on Error
     */
    public Long createConsumptionMessage(OrderStepInput orderStepInput)
            throws MESException, DatasweepException;

    // Customer CUSTOMIZATION

    /**
     * Sends a consumption message to ERP (Goods Issue - Order).
     * 
     * @param consumedItems - The list of items that have been consumed
     * @return
     * @throws MESException On Error.
     * @throws DatasweepException On Error.
     */
    public Long createConsumptionMessage(List<ConsumedItem> consumedItems) throws MESException, DatasweepException;

    /**
     * Sends a production message to ERP (Goods Receipt - Order).
     * 
     * @param order The order number
     * @param batch The batch
     * @param quantity The quantity produced
     * @param erpOperation The ERP operation number
     * @param erpPhase The ERP phase number
     * @return key of persisted object MESMyEigPersistentObjects
     * @throws MESException On Error.
     * @throws DatasweepException On Error.
     */
    public Long createProductionMessage(String order, Batch batch, MeasuredValue quantity, String erpOperation,
            String erpPhase)
            throws MESException, DatasweepException;

    /**
     * Sends a production message to ERP (Goods Receipt - Order).
     * 
     * @param info The outbound production message object
     * @return key of persisted object MESMyEigPersistentObjects
     * @throws MESException On Error.
     * @throws DatasweepException On Error.
     */
    public Long createProductionMessage(ProductionTransferObject info) throws MESException, DatasweepException;

    /**
     * Sends an order status message to ERP.
     * 
     * @param order The order number
     * @param controlRecipeId The control recipe id
     * @param erpOperation The ERP operation number
     * @param erpStatus The ERP status
     * @return key of persisted object MESMyEigPersistentObjects
     * @throws MESException On Error.
     * @throws DatasweepException On Error.
     */
    public Long createOrderStatusMessage(String order, String controlRecipeId, String erpOperation, String erpStatus)
            throws MESException, DatasweepException;

    /**
     * Sends an order status message to ERP.
     * 
     * @param info The outbound order status message object
     * @return key of persisted object MESMyEigPersistentObjects
     * @throws MESException On Error.
     * @throws DatasweepException On Error.
     */
    public Long createOrderStatusMessage(OrderStatusTransferObject info) throws MESException, DatasweepException;

    /**
     * Sends a production message to ERP (Goods Receipt - Order).
     * 
     * @param order
     * @param producedItems
     * @param erpOperation
     * @param erpPhase
     * @return TODO
     */
    public Long createProductionMessage(String order, List<ProducedItem> producedItems, String erpOperation, String erpPhase)
            throws MESException, DatasweepException;

    /**
     * Send ask for material message to ERP
     * 
     * @param info
     * @return ask for material message
     * @throws MESException
     * @throws DatasweepException
     */
    public Long createAskForMaterialMessage(AskForMaterialTransferObject info) throws MESException, DatasweepException;

    /**
     * Send ask for material message to ERP
     * 
     * @param orderStpeInputs
     * @return ask for material message
     * @throws MESException
     * @throws DatasweepException
     */
    public Long createAskForMaterialMessage(List<OrderStepInput> orderStpeInputs, OrderStep orderStep) throws MESException, DatasweepException;

    /**
     * Send relocation material message to ERP
     * 
     * @param orderStpeInputs
     * @return ask for material message
     * @throws MESException
     * @throws DatasweepException
     */
    public Long createMaterialRelocationMessage(Sublot[] paramArrayOfSublot) throws MESException, DatasweepException;

    /**
     * Relocation material message to ERP
     * 
     * @param info
     * @return relocation for material message
     * @throws MESException
     * @throws DatasweepException
     */
    public Long createMaterialRelocationMessage(RelocationMaterialTransferObject info) throws MESException, DatasweepException;
}
