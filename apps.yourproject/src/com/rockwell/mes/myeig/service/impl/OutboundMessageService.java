package com.rockwell.mes.myeig.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.client.Batch;
import com.datasweep.compatibility.client.DatasweepException;
import com.datasweep.compatibility.client.MeasuredValue;
import com.datasweep.compatibility.client.OrderStep;
import com.datasweep.compatibility.client.OrderStepInput;
import com.datasweep.compatibility.client.ProcessOrderItem;
import com.datasweep.compatibility.client.Sublot;
import com.datasweep.compatibility.ui.Time;
import com.datasweep.plantops.common.constants.IOrderStepInputTypes;
import com.datasweep.plantops.common.measuredvalue.IMeasuredValue;
import com.rockwell.custmes.commons.base.ifc.nameduda.CustMESNamedUDAOrderStepInput;
import com.rockwell.custmes.commons.base.ifc.nameduda.CustMESNamedUDAProcessOrderItem;
import com.rockwell.mes.commons.base.ifc.configuration.MESConfiguration;
import com.rockwell.mes.commons.base.ifc.exceptions.MESException;
import com.rockwell.mes.commons.base.ifc.functional.BigDecimalUtilities;
import com.rockwell.mes.commons.base.ifc.functional.MeasuredValueUtilities;
import com.rockwell.mes.commons.base.ifc.nameduda.MESNamedUDAOrderStep;
import com.rockwell.mes.commons.base.ifc.nameduda.MESNamedUDAOrderStepInput;
import com.rockwell.mes.commons.base.ifc.nameduda.MESNamedUDAPart;
import com.rockwell.mes.commons.base.ifc.services.PCContext;
import com.rockwell.mes.commons.base.ifc.services.ServiceFactory;
import com.rockwell.mes.commons.base.ifc.services.Transactional;
import com.rockwell.mes.commons.base.ifc.utility.MesClassUtility;
import com.rockwell.mes.myeig.data.AskForMaterialTransferObject;
import com.rockwell.mes.myeig.data.AskForMaterialTransferObject.AskForMaterialObject;
import com.rockwell.mes.myeig.data.ConsumptionTransferObject;
import com.rockwell.mes.myeig.data.ConsumptionTransferObject.ConsumedItem;
import com.rockwell.mes.myeig.data.OrderStatusTransferObject;
import com.rockwell.mes.myeig.data.OrderStatusTransferObject.E1ZProstat;
import com.rockwell.mes.myeig.data.ProductionTransferObject;
import com.rockwell.mes.myeig.data.ProductionTransferObject.ProducedItem;
import com.rockwell.mes.myeig.data.RelocationMaterialTransferObject;
import com.rockwell.mes.myeig.data.RelocationMaterialTransferObject.RelocationMaterialObject;
import com.rockwell.mes.myeig.model.MESMyEigPersistentObjects;
import com.rockwell.mes.myeig.service.ifc.IOutboundMessageService;
import com.rockwell.mes.myeig.utility.IntegrationGatewayHelper;
import com.rockwell.mes.services.commons.ifc.functional.PartRelatedMeasuredValueUtilities;
import com.rockwell.mes.services.inventory.ifc.IBatchService;
import com.rockwell.mes.services.inventory.ifc.IMatMgmtSupportService;
import com.rockwell.mes.services.order.ifc.IMESOrderService;
import com.rockwell.mes.services.order.ifc.OrderUtils;
import com.rockwell.mes.services.recipe.ifc.EnumMaterialType;
import com.rockwell.mes.services.recipe.ifc.weighing.EnumRouteStepTypes;
import com.rockwell.mes.services.wd.ifc.IBasicOrderStepInputService;
import org.apache.commons.lang3.StringUtils;
/**
 * Service API for out-bound messaging
 * 
 * @author syim, (c) Copyright 2012 Rockwell Automation Solutions, Inc. All
 *         Rights Reserved.
 * 
 */
public class OutboundMessageService extends ErpMessageService implements IOutboundMessageService {

    /** Logger */
    private static final Log LOGGER = LogFactory.getLog(OutboundMessageService.class);

    /** The event type for consumption */
    private static final String EVENT_TYP_MATERIAL_CONSUMPTION = "MaterialConsumption";

    /** The event type for production */
    private static final String EVENT_TYP_MATERIAL_PRODUCTION = "MaterialProduction";

    /** The eventy type for order status */
    private static final String EVENT_TYP_ORDER_STATUS = "OrderStatus";

    /** The event description for consumption */
    private static final String MATERIAL_CONSUMPTION_DESC = "Material consumed in PharmaSuite";

    /** The event description for production */
    private static final String MATERIAL_PRODUCTION_DESC = "Material produced in PharmaSuite";

    /** The event description for order status */
    private static final String ORDER_STATUS_DESC = "Order status to ERP";

    /** 3 */
    public static final int SAP_DEFAULT_PRECISION_THREE = 3;

    private static final Long WATER_SAP_CODE = 6015022l;

    private static final String DEFAULT_PLANT = "2100";

    private static final String DEFAULT_LOCATION = "1110";

    // Constants for Ask for Material
    /** The event type for ask for material */
    private static final String EVENT_TYP_ASK_FOR_MATERIAL = "AskForMaterial";

    /** The event description for order status */
    private static final String ASK_FOR_MATERIAL_DESC = "Ask materials for dispensing from pharmasuite to ERP";

    /** The plant code or site for ask for material */
    private static final String CODE_FOR_ASK_FOR_MATERIAL = "04";

    /** The plant code or site for ask for material */
    private static final String PLANT_OR_SITE = "1100";

    /** The movement type for ask for material message when the UP is weighing and dispense */
    private static final String ASK_FOR_MATERIAL_MOVEMENT_TYPE_WD = "931";

    /** The movement type for ask for material message when the UP is EBR */
    private static final String ASK_FOR_MATERIAL_MOVEMENT_TYPE_EBR = "933";

    /** The movement type for ask for material message */
    private static final String ASK_FOR_MATERIAL_STORAGE_LOCATION = "1110";

    // Constants for Ask for Material
    /** The event type for ask for material */
    private static final String EVENT_TYP_RELOCATION_MATERIAL = "RelocationMaterial";

    /** The event description for order status */
    private static final String RELOCATION_MATERIAL_DESC = "Relocation materials for dispensing from pharmasuite to ERP";

    /** The plant code or site for ask for material */
    private static final String CODE_FOR_RELOCATION_MATERIAL = "04";

    /** The movement type for ask for material message when the UP is weighing and dispense */
    private static final String RELOCATION_MATERIAL_MOVEMENT_TYPE = "932";

    /** The movement type for ask for material message */
    private static final String RELOCATION_MATERIAL_STORAGE_LOCATION = "1110";
    
    /** The value for packaging order type */
    public static final String PACKAGING_ORDER_TYPE_VALUE = "ZPCK";
    
    /** The value for manufacturing order type */
    public static final String UDA_LABEL_WORK_ORDER_TPYE = "ct_workOrderType";
    
    /** The event description for order status */
    private static final String MANUAL_ORDER_INITIALS = "MO";
    
    /** The stock indicator */
    private static final String STOCK_INDICATOR = "Q";

    /** The special stock indicator */
    private static final String SPECIAL_STOCK_INDICATOR = "Q";

    /** The storage bin */
    private static final String STORAGE_BIN = "SOLIDS";

    /** The storage type */
    private static final String STORAGE_TYPE = "100";

    /** The movement type */
    private static final String MOVEMENT_TYPE = "921";

    /** The storage location for spain */
    private static final String STORAGE_LOCATION_SPAIN = "2101";

    @Override
    @Transactional
    public Long createConsumptionMessage(String order, Batch batch, MeasuredValue quantity, String rsnum, String rspos)
            throws MESException, DatasweepException {

        ConsumptionTransferObject consumptionObject = new ConsumptionTransferObject();

        ConsumedItem consumedItem = new ConsumedItem();

        String batchName = ServiceFactory.getService(IBatchService.class).retrieveBatchIdentifier(batch);
        consumedItem.setBatch(batchName);
        consumedItem.setMaterial(batch.getPartNumber());
        consumedItem.setOrder(order);
        if (quantity != null) {
            consumedItem.setQuantity(ObjectUtils.toString(quantity.getValue().setScale(SAP_DEFAULT_PRECISION_THREE, BigDecimal.ROUND_HALF_UP)));
            consumedItem.setUom(ObjectUtils.toString(quantity.getUnitOfMeasure()).toUpperCase(Locale.getDefault()));
        }
        consumedItem.setReservationItemNo(rspos);
        consumedItem.setReservationNo(rsnum);

        Time docDate = PCContext.getCurrentServerTime();
        consumptionObject.setConsumeDate(IntegrationGatewayHelper.formatSapDateStringFromTime(docDate));
        consumptionObject.setConsumeTime(IntegrationGatewayHelper.formatSapTimeStringFromTime(docDate));

        // set properties based on configuration
        String location = MESConfiguration.getMESConfiguration().getString("eig_ConsumptionStorageLocation", DEFAULT_LOCATION,
                "The storage location identifier sent in the consumption message to ERP");
        consumedItem.setLocation(location);

        String tcode = MESConfiguration.getMESConfiguration().getString("eig_ConsumptionTransactionCode", "MB1A",
                "The transaction code (TCODE) for goods issue (order) sent in the consumption message to ERP");
        consumedItem.setTransactionCode(tcode);

        String plant = MESConfiguration.getMESConfiguration().getString("eig_ConsumptionPlantCode", DEFAULT_PLANT,
                "The plant identifier (WERKS) sent in the consumption message to ERP");
        consumedItem.setPlant(plant);

        String movementType = MESConfiguration.getMESConfiguration().getString("eig_ConsumptionMovementType", "261",
                "The movement type code (BWART) for goods issue (order) sent in the consumption message to ERP");
        consumedItem.setMovementType(movementType);

        consumptionObject.getConsumedItems().add(consumedItem);

        return createConsumptionMessage(consumptionObject);

    }

    @Override
    @Transactional
    public Long createConsumptionMessage(ConsumptionTransferObject info) throws MESException, DatasweepException {
        return createOutboundObject(info, EVENT_TYP_MATERIAL_CONSUMPTION, MATERIAL_CONSUMPTION_DESC);
    }

    // CUSTOMER CUSTOMIZATION (SYNTHON)
    @Override
    @Transactional
    public Long createConsumptionMessage(OrderStepInput orderStepInput) throws MESException, DatasweepException {

        ConsumptionTransferObject consumptionObject = new ConsumptionTransferObject();
        ConsumedItem consumedItem = new ConsumedItem();
        // Pattern pattern = Pattern.compile(MANUAL_ORDER_INITIALS, Pattern.CASE_INSENSITIVE);
        ProcessOrderItem poi = OrderUtils.getProcessOrderItemOfOrderStep(orderStepInput.getOrderStep());
        String order = poi.getOrderName();
        // Matcher matcher = pattern.matcher(order);
        // boolean isManualOrder = matcher.find();
        boolean isManualOrder = false;
        if (MANUAL_ORDER_INITIALS.equals(left(order, 2))) {
            isManualOrder = true;
        }
        Batch batch = orderStepInput.getAttachSublot().getBatch();

        consumedItem.setMaterial(batch.getPartNumber());
        String batchName = ServiceFactory.getService(IBatchService.class).retrieveBatchIdentifier(batch);
        consumedItem.setBatch(batchName);
        MeasuredValue quantity = MESNamedUDAOrderStepInput.getConsumedQuantity(orderStepInput);
        consumedItem.setQuantity((quantity.getValue().setScale(SAP_DEFAULT_PRECISION_THREE, BigDecimal.ROUND_HALF_UP)).toString());
        consumedItem.setUom(quantity.getUnitOfMeasure().toString().toUpperCase(Locale.getDefault()));
        consumedItem.setOrder(order);

        String reservationNum = CustMESNamedUDAOrderStepInput.getReservationnumber(orderStepInput);
        String reservationPos = CustMESNamedUDAOrderStepInput.getReservationposition(orderStepInput);

        consumedItem.setReservationItemNo(reservationPos);
        consumedItem.setReservationNo(reservationNum);
        // String wbsElement = CustMESNamedUDAOrderStepInput.getWbselement(orderStepInput);
        String wbsElement = CustMESNamedUDAProcessOrderItem.getWBS(poi);
        consumedItem.setBreakDownNumber(wbsElement);
        String stockIndicator = CustMESNamedUDAOrderStepInput.getSpecialstockIndicator(orderStepInput);
        if (STOCK_INDICATOR.equals(stockIndicator))
            consumedItem.setStockIndicator(SPECIAL_STOCK_INDICATOR);
        else
            consumedItem.setStockIndicator(StringUtils.EMPTY);


        Time docDate = PCContext.getCurrentServerTime();
        consumptionObject.setConsumeDate(IntegrationGatewayHelper.formatSapDateStringFromTime(docDate));
        consumptionObject.setConsumeTime(IntegrationGatewayHelper.formatSapTimeStringFromTime(docDate));

        // set properties based on configuration
        // String location = MESConfiguration.getMESConfiguration().getString("eig_ConsumptionStorageLocation",
        // StringUtils.EMPTY, "The storage location identifier sent in the consumption message to ERP");
        String location = CustMESNamedUDAOrderStepInput.getStorageLocation(orderStepInput);
        consumedItem.setLocation(location);

        String tcode = MESConfiguration.getMESConfiguration().getString("eig_ConsumptionTransactionCode", "MB1A",
                "The transaction code (TCODE) for goods issue (order) sent in the consumption message to ERP");
        consumedItem.setTransactionCode(tcode);

        // String plant = CustMESNamedUDAProcessOrderItem.getPlant(poi);
        String plant = DEFAULT_PLANT;
        consumedItem.setPlant(plant);

        String movementType = MESConfiguration.getMESConfiguration().getString("eig_ConsumptionMovementType", "261",
                "The movement type code (BWART) for goods issue (order) sent in the consumption message to ERP");
        consumedItem.setMovementType(movementType);

        consumedItem.setCostCenter(StringUtils.EMPTY);
        consumedItem.setStorageBin(StringUtils.EMPTY);
        consumedItem.setStorageType(StringUtils.EMPTY);

        if (isManualOrder) {
            consumedItem.setStorageBin(STORAGE_BIN);
            consumedItem.setStorageType(STORAGE_TYPE);
            consumedItem.setStockIndicator(StringUtils.EMPTY);
            consumedItem.setBreakDownNumber(StringUtils.EMPTY);
            consumedItem.setReservationItemNo(StringUtils.EMPTY);
            consumedItem.setReservationNo(StringUtils.EMPTY);
            consumedItem.setLocation(STORAGE_LOCATION_SPAIN);
            consumedItem.setMovementType(MOVEMENT_TYPE);
            consumedItem.setCostCenter(MESNamedUDAOrderStep.getCostCenter(orderStepInput.getOrderStep()));
            // consumedItem.setOrder(StringUtils.EMPTY);
        }
        consumptionObject.getConsumedItems().add(consumedItem);
        return createConsumptionMessage(consumptionObject);
    }

    // Customer CUSTOMIZATION (DENTAID (OLD))
    @Override
    @Transactional
    public Long createConsumptionMessage(List<ConsumedItem> consumedItems) throws MESException, DatasweepException {
        ConsumptionTransferObject info = new ConsumptionTransferObject();

        Time docDate = PCContext.getCurrentServerTime();
        info.setConsumeDate(IntegrationGatewayHelper.formatSapDateStringFromTime(docDate));
        info.setConsumeTime(IntegrationGatewayHelper.formatSapTimeStringFromTime(docDate));

        for (ConsumedItem consumedItem : consumedItems) {
            // set properties based on configuration
            String location = ((location = consumedItem.getLocation()) != null) ? location : DEFAULT_LOCATION;
            // location = MESConfiguration.getMESConfiguration().getString("eig_ConsumptionStorageLocation",
            // consumedItem.getLocation(),
            // "The storage location identifier sent in the consumption message to ERP");
            consumedItem.setLocation(location);

            String tcode = MESConfiguration.getMESConfiguration().getString("eig_ConsumptionTransactionCode", "MB1A",
                    "The transaction code (TCODE) for goods issue (order) sent in the consumption message to ERP");
            consumedItem.setTransactionCode(tcode);

            String plant = ((plant = consumedItem.getPlant()) != null) ? plant : DEFAULT_PLANT;
            // plant = MESConfiguration.getMESConfiguration().getString("eig_ConsumptionPlantCode",
            // consumedItem.getPlant(),
            // "The plant identifier (WERKS) sent in the consumption message to ERP");
            consumedItem.setPlant(plant);

            String movementType = MESConfiguration.getMESConfiguration().getString("eig_ConsumptionMovementType", "261",
                    "The movement type code (BWART) for goods issue (order) sent in the consumption message to ERP");
            consumedItem.setMovementType(movementType);

            info.getConsumedItems().add(consumedItem);
        }

        return createOutboundObject(info, EVENT_TYP_MATERIAL_CONSUMPTION, MATERIAL_CONSUMPTION_DESC);
    }


    @Override
    @Transactional
    public Long createProductionMessage(String order, Batch batch, MeasuredValue quantity, String erpOperation, String erpPhase)
            throws MESException, DatasweepException {

        String batchName = ServiceFactory.getService(IBatchService.class).retrieveBatchIdentifier(batch);

        ProductionTransferObject producedObject = new ProductionTransferObject();
        producedObject.setOrder(order);

        Time docDate = PCContext.getCurrentServerTime();
        producedObject.setProduceDate(IntegrationGatewayHelper.formatSapDateStringFromTime(docDate));
        producedObject.setProduceTime(IntegrationGatewayHelper.formatSapTimeStringFromTime(docDate));

        ProducedItem producedItem = new ProducedItem();

        producedItem.setBatch(batchName);
        producedItem.setMaterial(batch.getPartNumber());
        if (quantity != null) {
            producedItem.setQuantity(ObjectUtils.toString(quantity.getValue().setScale(SAP_DEFAULT_PRECISION_THREE, BigDecimal.ROUND_HALF_UP)));
            producedItem.setUom(ObjectUtils.toString(quantity.getUnitOfMeasure()).toUpperCase(Locale.getDefault()));
        }
        producedItem.setErpOperationNo(erpOperation);
        producedItem.setErpPhaseNo(erpPhase);

        // set properties based on configuration
        String location = MESConfiguration.getMESConfiguration().getString("eig_ProductionStorageLocation", DEFAULT_LOCATION,
                "The storage location identifier sent in the production message to ERP");
        producedItem.setLocation(location);

        String tcode = MESConfiguration.getMESConfiguration().getString("eig_ProductionTransactionCode", "MIGO",
                "The transaction code (TCODE) for goods issue (order) sent in the production message to ERP");
        producedItem.setTransactionCode(tcode);

        String plant = MESConfiguration.getMESConfiguration().getString("eig_ProductionPlantCode", DEFAULT_PLANT,
                "The plant identifier (WERKS) sent in the production message to ERP");
        producedItem.setPlant(plant);

        String movementType = MESConfiguration.getMESConfiguration().getString("eig_ProductionMovementType", "101",
                "The movement type code (BWART) for goods receipt (order) sent in the production message to ERP");
        producedItem.setMovementType(movementType);

        producedObject.getProducedItems().add(producedItem);

        return createProductionMessage(producedObject);
    }

    @Override
    @Transactional
    public Long createProductionMessage(ProductionTransferObject info) throws MESException, DatasweepException {
        return createOutboundObject(info, EVENT_TYP_MATERIAL_PRODUCTION, MATERIAL_PRODUCTION_DESC);
    }

    // Customer CUSTOMIZATION (OLD DENTAID)
    @Override
    public Long createProductionMessage(String order, List<ProducedItem> producedItems, String erpOperation, String erpPhase)
            throws MESException, DatasweepException {

        ProductionTransferObject producedObject = new ProductionTransferObject();
        producedObject.setOrder(order);

        Time docDate = PCContext.getCurrentServerTime();
        producedObject.setProduceDate(IntegrationGatewayHelper.formatSapDateStringFromTime(docDate));
        producedObject.setProduceTime(IntegrationGatewayHelper.formatSapTimeStringFromTime(docDate));

        for (ProducedItem producedItem : producedItems) {

            // set properties based on configuration
            String location = MESConfiguration.getMESConfiguration().getString("eig_ProductionStorageLocation", DEFAULT_LOCATION,
                    "The storage location identifier sent in the production message to ERP");
            producedItem.setLocation(location);

            String tcode = MESConfiguration.getMESConfiguration().getString("eig_ProductionTransactionCode", "MIGO",
                    "The transaction code (TCODE) for goods issue (order) sent in the production message to ERP");
            producedItem.setTransactionCode(tcode);

            String plant = MESConfiguration.getMESConfiguration().getString("eig_ProductionPlantCode", DEFAULT_PLANT,
                    "The plant identifier (WERKS) sent in the production message to ERP");
            producedItem.setPlant(plant);

            String movementType = MESConfiguration.getMESConfiguration().getString("eig_ProductionMovementType", "101",
                    "The movement type code (BWART) for goods receipt (order) sent in the production message to ERP");
            producedItem.setMovementType(movementType);

            producedObject.getProducedItems().add(producedItem);
        }

        return createProductionMessage(producedObject);

    }

    @Override
    // AFH 20/03/2023 Stat interface
    /* public Long createOrderStatusMessage(String order, String controlRecipeId, String erpOperation, String erpStatus)
            throws MESException, DatasweepException {
        OrderStatusTransferObject orderStatusObject = new OrderStatusTransferObject();
        orderStatusObject.setOrderNo(order);
        orderStatusObject.setErpOperationNo(erpOperation);
        orderStatusObject.setControlRecipeId(controlRecipeId);
        orderStatusObject.setErpStatus(erpStatus);
    */
    public Long createOrderStatusMessage(String Inspection_lot, String Stat_Finished, String Stat_Prod_Finished, String Stat_Qa_Finished)
            throws MESException, DatasweepException {
        OrderStatusTransferObject orderStatusObject = new OrderStatusTransferObject();
        E1ZProstat ez1 = new OrderStatusTransferObject.E1ZProstat();

        ez1.setInsplot(Inspection_lot);
        ez1.setStatFinished(Stat_Finished);//Stat_Finished);
        ez1.setStatProdReviewed(Stat_Prod_Finished);//Stat_Prod_Finished);
        ez1.setStatQaReviewed(Stat_Qa_Finished);//Stat_Qa_Finished);
        // TODO: Testing
        ez1.setTestRun("");
        ez1.setZe1ProStatus("");

        orderStatusObject.setSiteId("");
        orderStatusObject.setSiteNumber(0);
        orderStatusObject.setVerb("");
        orderStatusObject.setE1Zprostat(ez1);
        /*
        Time docDate = PCContext.getCurrentServerTime();
        orderStatusObject.setStatusDate(IntegrationGatewayHelper.formatSapDateStringFromTime(docDate));
        orderStatusObject.setStatusTime(IntegrationGatewayHelper.formatSapTimeStringFromTime(docDate));
        */
        return createOrderStatusMessage(orderStatusObject);
    }

    @Override
    public Long createOrderStatusMessage(OrderStatusTransferObject info) throws MESException, DatasweepException {
        return createOutboundObject(info, EVENT_TYP_ORDER_STATUS, ORDER_STATUS_DESC);
    }

    /**
     * Method that actually generates the PCA event and persists the transfer
     * object
     * 
     * @param info Transfer object
     * @param objectName Identifier of event type
     * @param eventDesc Description text of upload transaction
     * @return Key of PCA_EVENTS
     * @throws MESException On Error.
     * @throws DatasweepException On Error.
     */
    private Long createOutboundObject(Object info, String objectName, String eventDesc) throws MESException,
            DatasweepException {

        LOGGER.info("Starting outbound integration message creation for PCA_EVENT (" + objectName + ")");

        MESMyEigPersistentObjects persistentObject = savePersistentObject(System.currentTimeMillis(), PCA_EVENTS,
                new byte[0]);

        IntegrationGatewayHelper.persistPcaEvent(objectName, persistentObject.getKey(), eventDesc);
        String pcaEventId = IntegrationGatewayHelper.getPcaEventKey(objectName, persistentObject.getKey());
        LOGGER.info("PCA_EVENT created with event_id '" + pcaEventId + "'");

        if (info instanceof ConsumptionTransferObject) {
            ((ConsumptionTransferObject) info).setIdoc(pcaEventId); // set the document number as the event id
        } else if (info instanceof ProductionTransferObject) {
            ((ProductionTransferObject) info).setIdoc(pcaEventId); // set the document number as the event id
        } else if (info instanceof OrderStatusTransferObject) {
            ((OrderStatusTransferObject) info).setIdoc(pcaEventId); // set the document number as the event id 
        } else if (info instanceof AskForMaterialTransferObject) {
            ((AskForMaterialTransferObject) info).setIdoc(pcaEventId); // set the document number as the event id
        } else if (info instanceof RelocationMaterialTransferObject) {
            ((RelocationMaterialTransferObject) info).setIdoc(pcaEventId); // set the document number as the event id
        } else { // outbound object type not recognized
            throw new MESException("Unsupported outbound object type " + info.getClass().getName());
        }

        persistentObject.setObjectID(Long.parseLong(pcaEventId));
        savePersistentObject(persistentObject, info);

        return Long.valueOf(persistentObject.getObjectID());

    }

    @Override
    public Long createAskForMaterialMessage(List<OrderStepInput> orderStpeInputs, OrderStep orderStep) throws MESException, DatasweepException {
        IMatMgmtSupportService matMgmtService = ServiceFactory.getService(IMatMgmtSupportService.class);
        HashMap<String, MeasuredValue> quantityObjectMap = new HashMap<>();

        // In case of dispense order step send ask for material message to ERP
        String movementType = EnumRouteStepTypes.WEIGHING.equals(MESNamedUDAOrderStep.getType(orderStep)) ? ASK_FOR_MATERIAL_MOVEMENT_TYPE_WD
                : ASK_FOR_MATERIAL_MOVEMENT_TYPE_EBR;
        IBasicOrderStepInputService osiService = ServiceFactory.getService(IBasicOrderStepInputService.class);
        
        boolean isManufacturingOrder = PACKAGING_ORDER_TYPE_VALUE.equals(getOrderTypeFromOrderStep(orderStep));
        
        for (OrderStepInput osi : orderStpeInputs) {
            // Check if -
            // 1.The material for this OSI is already ordered from warehouse.
            // 2. The OSI material is raw material
            // 3. The OSI is not a transfer OSI-
            if (!MesClassUtility.LONG_TRUE.equals(CustMESNamedUDAOrderStepInput.getIsAskForMat(osi))
                    && IOrderStepInputTypes.INPUT_TYPE_INPUT == osi.getProcessingType() && osiService.isMasterOsi(osi)
                    && isManufacturingOrder
                    && !WATER_SAP_CODE.toString().equals(osi.getPart().getPartNumber())
                    && EnumMaterialType.RAW_MATERIAL.getValue().equals(MESNamedUDAPart.getMaterialType(osi.getPart()).getValue())) {
                Batch[] batchAllocations = matMgmtService.getAllocatedBatchesForOrderStepInput(osi);
                String quantityObject = null;
                // Add quantity to be ordered for same batch/material together
                // Here assumption is every OSI will have a single batch allocated.
                if (batchAllocations.length > 0) {
                    Batch batch = batchAllocations[0];
                    quantityObject = batch.getName();
                } else {
                    quantityObject = osi.getPart().getPartNumber();
                }

                // IF OSI has different processing UoM, then the total quantity will be in base UoM.
                if (quantityObjectMap.get(quantityObject) == null) {
                    quantityObjectMap.put(quantityObject, osi.getPlannedQuantity());
                } else {
                    MeasuredValue updatedQuantity = MeasuredValueUtilities.add(quantityObjectMap.get(quantityObject), osi.getPlannedQuantity(),
                            PartRelatedMeasuredValueUtilities.getMeasuredValueConverterForPart(osi.getPart()));
                    quantityObjectMap.put(quantityObject, updatedQuantity);
                }

                CustMESNamedUDAOrderStepInput.setIsAskForMat(osi, MesClassUtility.LONG_TRUE);
                osi.save(PCContext.getCurrentServerTime(), "Ask for material message sent to erp", null);
            }
        }
        if (!quantityObjectMap.isEmpty()) {

            AskForMaterialTransferObject askForMatObject = new AskForMaterialTransferObject();

            Time docDate = PCContext.getCurrentServerTime();
            askForMatObject.setDocumentDate(IntegrationGatewayHelper.formatSapDateStringFromTime(docDate));
            askForMatObject.setPublishingDate(IntegrationGatewayHelper.formatSapDateStringFromTime(docDate));
            askForMatObject.setCode(CODE_FOR_ASK_FOR_MATERIAL);

            IBatchService batchService = ServiceFactory.getService(IBatchService.class);

            // Set the materials details for each batch/part

            for (Entry<String, MeasuredValue> entry : quantityObjectMap.entrySet()) {
                AskForMaterialObject material = new AskForMaterialObject();
                String materialIdentifier = null;
                // String batchIdentifier = StringUtils.EMPTY;
                String batchIdentifier = null;

                Batch batch = batchService.loadBatch(entry.getKey());
                if (batch != null) {
                    materialIdentifier = batch.getPartNumber();
                    batchIdentifier = batchService.retrieveBatchIdentifier(batch);
                } else {
                    materialIdentifier = entry.getKey();
                }

                IMeasuredValue quantity = entry.getValue();
                material.setMaterial(materialIdentifier);
                // material.setQuantity(
                // BigDecimalUtilities.toStringAsDecimal(quantity.getValue().setScale(SAP_DEFAULT_PRECISION_THREE,
                // BigDecimal.ROUND_HALF_UP)));

                material.setQuantity(
                        ObjectUtils.toString(quantity.getValue().setScale(SAP_DEFAULT_PRECISION_THREE, BigDecimal.ROUND_HALF_UP)));

                material.setStorageLocation(ASK_FOR_MATERIAL_STORAGE_LOCATION);
                material.setMovementType(movementType);
                material.setBatch(batchIdentifier);
                // material.setOrder(StringUtils.EMPTY);
                material.setOrder(null);
                material.setPlant(PLANT_OR_SITE);
                material.setUom(quantity.getUnitOfMeasure().getSymbol().toUpperCase());

                askForMatObject.getMaterials().add(material);
            }
            return createAskForMaterialMessage(askForMatObject);
        }
        return null;
    }

    private String getOrderTypeFromOrderStep(OrderStep orderStep) {
        String orderType = "";
        if (orderStep != null) {
            IMESOrderService orderService = ServiceFactory.getService(IMESOrderService.class);
            ProcessOrderItem poi = orderService.getOrderForOrderStep(orderStep);
            if (poi != null) {
                try {
                    orderType = poi.getUDA(UDA_LABEL_WORK_ORDER_TPYE) != null ? poi.getUDA(UDA_LABEL_WORK_ORDER_TPYE).toString() : "";
                } catch (DatasweepException e) {
                    LOGGER.error("Error trying to get UDA '" + UDA_LABEL_WORK_ORDER_TPYE + "' from ProcessOrderItem '" + poi.getOrderItem()
                            + "'. Exception is: " + e.getMessage());
                }
            }
        }
        return orderType;
    }

    @Override
    public Long createAskForMaterialMessage(AskForMaterialTransferObject info) throws MESException, DatasweepException {
        return createOutboundObject(info, EVENT_TYP_ASK_FOR_MATERIAL, ASK_FOR_MATERIAL_DESC);
    }

    @Override
    public Long createMaterialRelocationMessage(Sublot[] paramArrayOfSublot) throws MESException, DatasweepException {
        RelocationMaterialTransferObject relocationMatObject = new RelocationMaterialTransferObject();

        Time docDate = PCContext.getCurrentServerTime();
        relocationMatObject.setDocumentDate(IntegrationGatewayHelper.formatSapDateStringFromTime(docDate));
        relocationMatObject.setPublishingDate(IntegrationGatewayHelper.formatSapDateStringFromTime(docDate));
        relocationMatObject.setCode(CODE_FOR_RELOCATION_MATERIAL);

        IBatchService batchService = ServiceFactory.getService(IBatchService.class);

        HashMap<String, RelocationMaterialObject> groupedObjects = new HashMap<String, RelocationMaterialObject>();

        for (Sublot sublot : paramArrayOfSublot) {
            RelocationMaterialObject material = new RelocationMaterialObject();
            String materialIdentifier = null;
            String batchIdentifier = null;

            Batch batch = batchService.loadBatch(sublot.getBatchName());
            if (batch != null) {
                materialIdentifier = batch.getPartNumber();
                batchIdentifier = batchService.retrieveBatchIdentifier(batch);
            } else {
                materialIdentifier = sublot.getPartNumber();
            }

            // grouping materials by MaterialCode and Batch!!
            String key = batchIdentifier + "-" + materialIdentifier;
            if (groupedObjects.containsKey(key)) {
                material = groupedObjects.get(key);

                IMeasuredValue materialQuantity = MeasuredValueUtilities.createMV(material.getQuantity(), material.getUom().toLowerCase());

                MeasuredValue updatedQuantity = MeasuredValueUtilities.add(materialQuantity, sublot.getQuantity(),
                        PartRelatedMeasuredValueUtilities.getMeasuredValueConverterForPart(sublot.getPart()));

                material.setQuantity(
                        ObjectUtils.toString(updatedQuantity.getValue().setScale(SAP_DEFAULT_PRECISION_THREE, BigDecimal.ROUND_HALF_UP)));
                material.setUom(updatedQuantity.getUnitOfMeasure().getSymbol().toUpperCase());

            } else {
                material = new RelocationMaterialObject();

                IMeasuredValue quantity = sublot.getQuantity();
                material.setMaterial(materialIdentifier);
                // material.setQuantity(
                // BigDecimalUtilities.toStringAsDecimal(quantity.getValue().setScale(SAP_DEFAULT_PRECISION_THREE,
                // BigDecimal.ROUND_HALF_UP)));

                material.setQuantity(ObjectUtils.toString(quantity.getValue().setScale(SAP_DEFAULT_PRECISION_THREE, BigDecimal.ROUND_HALF_UP)));

                // ObjectUtils.toString(quantity.getValue().setScale(SAP_DEFAULT_PRECISION_THREE,
                // BigDecimal.ROUND_HALF_UP))

                material.setStorageLocation(RELOCATION_MATERIAL_STORAGE_LOCATION);
                material.setMovementType(RELOCATION_MATERIAL_MOVEMENT_TYPE);
                material.setBatch(batchIdentifier);
                // material.setOrder(StringUtils.EMPTY);
                material.setOrder(null);
                material.setPlant(PLANT_OR_SITE);
                material.setUom(quantity.getUnitOfMeasure().getSymbol().toUpperCase());

            }

            groupedObjects.put(key, material);

        }

        // Recorremos la Hashtable para añadirlo a la Lista definitiva
        List<RelocationMaterialObject> listadoFinal = new ArrayList<RelocationMaterialObject>();
        for (Entry<String, RelocationMaterialObject> entry : groupedObjects.entrySet()) {
            listadoFinal.add(entry.getValue());
        }

        relocationMatObject.getMaterials().addAll(listadoFinal);

        return createMaterialRelocationMessage(relocationMatObject);
    }

    @Override
    public Long createMaterialRelocationMessage(RelocationMaterialTransferObject info) throws MESException, DatasweepException {
        return createOutboundObject(info, EVENT_TYP_RELOCATION_MATERIAL, RELOCATION_MATERIAL_DESC);
    }

    public static String left(String s, int size) {
        return s.substring(0, Math.min(size, s.length()));
    }

}
