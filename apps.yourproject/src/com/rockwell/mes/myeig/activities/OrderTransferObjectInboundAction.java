package com.rockwell.mes.myeig.activities;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.converters.IntegerArrayConverter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



import com.datasweep.compatibility.client.Batch;
import com.datasweep.compatibility.client.DatasweepException;
import com.datasweep.compatibility.client.MFC;
import com.datasweep.compatibility.client.MasterRecipe;
import com.datasweep.compatibility.client.MasterRecipeFilter;
import com.datasweep.compatibility.client.MeasuredValue;
import com.datasweep.compatibility.client.Part;
import com.datasweep.compatibility.client.UnitOfMeasure;
import com.datasweep.compatibility.ui.Time;
import com.datasweep.plantops.common.constants.IObjectTypes;
import com.rockwell.integration.messaging.MessageEnvelope;
import com.rockwell.mes.commons.base.ifc.choicelist.MESChoiceListHelper;
import com.rockwell.mes.commons.base.ifc.configuration.MESConfiguration;
import com.rockwell.mes.commons.base.ifc.exceptions.MESException;
import com.rockwell.mes.commons.base.ifc.i18n.I18nMessageUtility;
import com.rockwell.mes.commons.base.ifc.nameduda.MESNamedUDAMFC;
import com.rockwell.mes.commons.base.ifc.services.PCContext;
import com.rockwell.mes.commons.base.ifc.services.ServiceFactory;
import com.rockwell.mes.myeig.data.OrderTransferObject;
import com.rockwell.mes.myeig.data.MaterialTransferObject.Material;
import com.rockwell.mes.myeig.data.MaterialTransferObject.MaterialText;
import com.rockwell.mes.myeig.data.MaterialTransferObject.MaterialUnitOfMeasure;
import com.rockwell.mes.myeig.data.OrderTransferObject.Operation;
import com.rockwell.mes.myeig.data.OrderTransferObject.Order;
import com.rockwell.mes.myeig.data.OrderTransferObject.OrderItem;
import com.rockwell.mes.myeig.data.OrderTransferObject.OrderStatus;
import com.rockwell.mes.myeig.data.OrderTransferObject.OrderAdditionalData;
import com.rockwell.mes.myeig.data.OrderTransferObject.OrderCpsComponent;
import com.rockwell.mes.myeig.data.OrderTransferObject.OrderCpsPack;
//import com.rockwell.mes.myeig.data.OrderTransferObject.OrderProcess;
import com.rockwell.mes.myeig.data.OrderTransferObject.OrderReservation;
//import com.rockwell.mes.myeig.data.OrderTransferObject.OrderSequence;
import com.rockwell.mes.myeig.data.OrderTransferObject.ProductionOrder;
//import com.rockwell.mes.myeig.data.OrderTransferObject.OrderStatusTransition;
//import com.rockwell.mes.myeig.data.OrderTransferObject.OrderWarehouse;
import com.rockwell.mes.myeig.service.ifc.ErpOrderBuilder;
import com.rockwell.mes.myeig.service.ifc.ErpOrderBuilder.ErpOrderItemBuilder;
import com.rockwell.mes.myeig.service.ifc.IInboundMessageService;
import com.rockwell.mes.myeig.service.ifc.IOutboundMessageService;
import com.rockwell.mes.myeig.service.impl.OutboundMessageService;
import com.rockwell.mes.myeig.utility.IntegrationGatewayHelper;
import com.rockwell.mes.services.inventory.ifc.AbstractBatchQualityTransitionEventListener;
import com.rockwell.mes.services.inventory.ifc.BatchBuilder;
import com.rockwell.mes.services.inventory.ifc.IBatchService;
import com.rockwell.mes.services.recipe.ifc.IMESRecipeService;
import com.rockwell.mes.services.s88.ifc.recipe.IMESMasterRecipe;
import com.rockwell.mes.services.s88.ifc.recipe.IMESMaterialParameter;
import com.rockwell.mes.services.s88.ifc.recipe.IMESMaterialParameter.TYPE;
import com.rockwell.mes.services.s88.ifc.recipe.IMESOperation;
import com.rockwell.mes.services.s88.ifc.recipe.IMESPhase;
import com.rockwell.mes.services.s88.ifc.recipe.IMESProcedure;
import com.rockwell.mes.services.s88.ifc.recipe.IMESUnitProcedure;
import com.rockwell.mes.services.s88.ifc.recipe.RecipeConfiguration;
import com.rockwell.mes.services.s88.impl.recipe.MESMasterRecipe;

/**
 * This class implements functionality to process incoming order messages
 * <p>
 * 
 * @author syim, (c) Copyright 2012 Rockwell Automation Technologies, Inc. All Rights Reserved.
 */
public class OrderTransferObjectInboundAction extends AbstractInboundActivity {

    /** logger */
    private static final Log LOGGER = LogFactory.getLog(OrderTransferObjectInboundAction.class);

    public enum ACTION {
        CREATE, EXPLODE, DELETE, NONE
    }

    private static final String SynthonPlantCode = "2100";

    private static final String ValorUdaSi = "1";
    private static final String ValorUdaNo = "0";    
    private static final String BulkMaterial = "30";
    private static final String DefaultCASampleType = "10";

    @Override
    public synchronized void processActivityData(final MessageEnvelope data) throws DatasweepException, MESException {
        // public void processActivityData(final MessageEnvelope data) throws DatasweepException, MESException {

        OrderTransferObject orderTransferObject = (OrderTransferObject) data.getPayload();
        setDocNum(orderTransferObject.getIdoc()); // set the document no. for all logging
        Order Order = orderTransferObject.getOrder();
        OrderAdditionalData OrderAdditionalData = Order.getOrderAdditionalData();
        OrderCpsPack OrderCpsPack = Order.getOrderCpsPack();
        OrderCpsComponent OrderCPSComponent = Order.getOrderCpsComponent();
        String rspos = "";
        String rsnum = "";
        boolean materialConQty0 = false;



        // Lista de componentes de los materiales del LOIPRO.
        List<ErpOrderItemBuilder> components = new ArrayList<ErpOrderItemBuilder>();
        // Lista de UDAs del LOIPRO.
        Map<String, Object> ordUdaMap = new HashMap<String, Object>();
        List<String> boxUDA = new ArrayList<String>();
        List<String> lblUDA = new ArrayList<String>();



        // 1. Order Number
        String orderNo = validate(true, Order.getOrderNumber(), String.class, "Order Number");
        setObjectsProcessed("Order Number: " + orderNo); // for inbound event log details

        // 2. Material Identifier OSO
        Part material = validate(true, Order.getMaterialIdentifier(), Part.class, "Material Number");

        // 3. order quantity and UOM
        String quantityVal = validate(true, Order.getTargetQuantity(), String.class, "Order Quantity");
        UnitOfMeasure quantityUom = validate(true, Order.getUnitOfMeasurement(), UnitOfMeasure.class, "Order Quantity UOM");
        MeasuredValue quantity = IntegrationGatewayHelper.getMeasuredValue(quantityVal, quantityUom);

        // 4. Order Type
        String orderType = validate(true, Order.getOrderType(), String.class, "orderType");

        // 5. Check if Master Recipe is available
        MasterRecipe recipe = null;
        String batchName = "";
        for (OrderItem OrderItem : Order.getOrderItems()) {
            // 6. Master recipe lookup
            // String routeMap = orderTransferObject.getRouteMap();
            String routeMap = validate(true, OrderItem.getProductionVersion(), String.class, "Route Map");
            recipe = getValidMasterRecipe(material, routeMap, Order.getOrderType());
            if (recipe == null) {
                addError(this.getClass().getName(), "There is no valid master recipe for the received material: " + material.getPartNumber());
            }

            // 7. Batch
            batchName = OrderItem.getBatchName();
            if (batchName == null) {
                String message = "Batch should not be null.";
                logError(LOGGER, message);
                addError(this.getClass().getName(), message + "\n");
            }
        }

        

        // 4. Start date
        Time erpStartDate = IntegrationGatewayHelper.fromSapString(Order.getBasicStartDate());
        /* Not needed for now 
         if (erpStartDate == null) {
            String message = "Start Date should not be null.";
            logError(LOGGER, message);
            addError(this.getClass().getName(), message + "\n");
        }
        */
        // 5. End date
        Time erpFinishDate = IntegrationGatewayHelper.fromSapString(Order.getBasicEndDate());
        /* Not needed for now 
        if (erpFinishDate == null) {
            String message = "End Date should not be null.";
            logError(LOGGER, message);
            addError(this.getClass().getName(), message + "\n");
        }
        */

        // 6. Components
        // RM: 03/ENE/2023--
        Map<String, String> materialList = new HashMap<String, String>();
        Map<Long, String> materialRepetido = new HashMap<Long, String>();
        List<String> materiales = new ArrayList<String>();

        String posicion = "";
        boolean mismoMaterial = false;
            for (ProductionOrder ProdOrder : Order.getProductionOrder()) {
                for (Operation Operation : ProdOrder.getOperations()) {
                    for (OrderReservation OrderReservation : Operation.getReservations()) {

                        // Personalizacion Synthon: solo cogemos los materiales que tengan StorageLocation (LGORT)
                        // RM: 03/ENE/2023-- Se asigna a la variable StorageLocation el valor del segmento E1RESBL.LGORT
                        String StorageLocation = OrderReservation.getStorageLocation();
                        if (!StringUtils.isEmpty(StorageLocation)) {
                        	
                            // a. component position
                            // RM: 03/ENE/2023-- Se asigna a la variable componentPos el valor del segmento
                            // E1RESBL.POSNR
                            String componentPos = validate(true, OrderReservation.getBomPosition(), String.class, "Component Position");

                            if (!posicion.equalsIgnoreCase(componentPos)) {
                                posicion = componentPos;
                                mismoMaterial = false;

                            } else {
                                mismoMaterial = true;

                            }
                            // componentPos = componentPos.substring(-4); RD - Comprobar si es necesario

                            // b. component material
                            // RM: 03/ENE/2023-- Se asigna a la variable componentPart el valor del segmento
                            // E1RESBL.MATNR
                            Part componentPart = validate(true, OrderReservation.getMaterialNumber(), Part.class, "Component Material");
                            materiales.add(componentPart.toString());
                            // c. component quantity
                            // RM: 03/ENE/2023-- Se asigna a la variable componentQtyVal el valor del segmento
                            // E1RESBL.BDMNG
                            String componentQtyVal = validate(true, OrderReservation.getQuantity(), String.class, "Component Quantity");
                           
                            materialConQty0 = componentQtyVal.equalsIgnoreCase("0.000") ? true : false;
                            // RM: 03/ENE/2023-- Se asigna a la variable componentQtyUom el valor del segmento
                            // E1RESBL.MEINS
                            UnitOfMeasure componentQtyUom =
                                    validate(true, OrderReservation.getUnitOfMeasure(), UnitOfMeasure.class, "Component Quantity Unit of Measure");

                            MeasuredValue componentQty = IntegrationGatewayHelper.getMeasuredValue(componentQtyVal, componentQtyUom);
                            
                            String componentRsnum = OrderReservation.getReservationNumber();

                            // d. batch allocation

                            final IBatchService batchSrv = ServiceFactory.getService(IBatchService.class);
                            // RM: 03/ENE/2023-- Se asigna a la variable componenBatchName el valor del segmento
                            // E1RESBL.CHARG
                            String componenBatchName = validate(false, OrderReservation.getBomBatch(), String.class, "Batch Name");
                            Batch componentBatch = batchSrv.loadBatchByCompoundIdentifier(componenBatchName, componentPart.getPartNumber());

                            // Create UDAMap
                            Map<String, Object> componentUdaMap = new HashMap<String, Object>();

                            ordUdaMap.put("ct_externalPosition", componentPos);

                            if (componentBatch != null && mismoMaterial == true) {
                                if (rspos == "") {
                                    rspos = materialList.get(componentPart.getPartKey() + componentPos);
                                    if (rspos == null) {
                                        rspos = "";
                                    }
                                }
                                rspos += OrderReservation.getReservationPosition() != null
                                        ? componentBatch + "_" + OrderReservation.getReservationPosition() + ";" : "";
                                componentUdaMap.put("ct_ReservationPosition", rspos);
                                materialList.put(componentPart.getPartKey() + componentPos, componentUdaMap.get("ct_ReservationPosition").toString());
                            } else if (componentBatch != null && mismoMaterial == false) {
                                rspos = "";
                                componentUdaMap.put("ct_ReservationPosition", componentBatch + "_" + OrderReservation.getReservationPosition() + ";");
                                materialList.put(componentPart.getPartKey() + componentPos, componentUdaMap.get("ct_ReservationPosition").toString());
                            } else {
                                rspos = "";
                                componentUdaMap.put("ct_ReservationPosition", OrderReservation.getReservationPosition());
                            }

                            if (componentBatch != null && mismoMaterial == true) {
                                if (rsnum == "") {
                                    rsnum = materialList.get(componentPart.getPartKey() + componentRsnum);
                                    if (rsnum == null) {
                                    	rsnum = "";
                                    }
                                }
                                rsnum += OrderReservation.getReservationNumber() != null
                                        ? componentBatch + "_" + OrderReservation.getReservationNumber() + ";" : "";
                                componentUdaMap.put("ct_ReservationNumber", rsnum);
                                materialList.put(componentPart.getPartKey() + componentRsnum, componentUdaMap.get("ct_ReservationNumber").toString());
                            } else if (componentBatch != null && mismoMaterial == false) {
                            	rsnum = "";
                                componentUdaMap.put("ct_ReservationNumber", componentBatch + "_" + OrderReservation.getReservationNumber() + ";");
                                materialList.put(componentPart.getPartKey() + componentRsnum, componentUdaMap.get("ct_ReservationNumber").toString());
                            } else {
                            	rsnum = "";
                                componentUdaMap.put("ct_ReservationNumber", OrderReservation.getReservationNumber());
                            }

                            componentUdaMap.put("ct_StorageLocation", OrderReservation.getStorageLocation());
                            componentUdaMap.put("ct_SpecialStock_Indicator", OrderReservation.getSpecialStockIndicator());
                            componentUdaMap.put("ct_WarehouseNumber", OrderReservation.getWarehouseNumber());
                            componentUdaMap.put("ct_ProductSupplyArea", OrderReservation.getProductSupplyArea());

                            ErpOrderItemBuilder itemBuilder = new ErpOrderItemBuilder(componentPos, componentPart, componentQty);
                            itemBuilder.batch(componentBatch);
                            itemBuilder.udaMap(componentUdaMap);
                            components.add(itemBuilder);
                            // **************************************************************
                        }
                    }
                }
            }            
                
                
            
        ACTION action = null;
        String transition = null;
        for (OrderStatus OrderStatus : Order.getOrderStatus()) {
            // 9. Get Stat
            action = ACTION.NONE;            
            transition = null;
            transition = OrderStatus.getObjectStatus();
            if (transition.equals("I0001")) {
                action = ACTION.CREATE;
                break;
            } else if (transition.equals("I0002")) {
                action = ACTION.EXPLODE;
                break;
            } else if (transition.equals("I0045")) {
                action = ACTION.DELETE;
                break;
            } else
                action = ACTION.NONE;
        }
        // 10. Validation of line items and material parameter (DUMMY BOM)
        // boolean isValidLineItemsAndBomItems = validateLineItemsAndBomItems(components, recipe,
        // orderTransferObject);
        // if ((!isValidLineItemsAndBomItems) && !StringUtils.equals("DELETE", action.toString())) {
        // Si está marcada como DELETE, no importa que falten posiciones porque la voy a borrar

        // String message = I18nMessageUtility.getLocalizedMessage("ct_eihub_error_processorder",
        // "bom_position_mismatch");
        // logError(LOGGER, "Error validating BOM positions. " + message);
        // addError(this.getClass().getName(), "Error validating BOM positions. " + message + ".\n");
        // }

        // UDAS PARA TODo TIPO DE ORDENES

        // 12. OrderPlant
        String Plant = validate(true, Order.getPlantCode(), String.class, "Plant");
        if (Plant != null && !StringUtils.equals(Plant, SynthonPlantCode)) {
            addError(this.getClass().getName(), "Plant code is incorrect.\n");
        }
        
        String inspectionLot = validate(true, OrderAdditionalData.getInspectionLot(), String.class, "Inspection Lot");
        String gs1ElementString = validate(true, OrderAdditionalData.getGs1ElementString(), String.class, "GS1 element string");


        ordUdaMap.put("X_erpStartDate", erpStartDate);
        ordUdaMap.put("X_erpFinishDate", erpFinishDate);
        ordUdaMap.put("X_processingType", 10); // 10 = batch
        ordUdaMap.put("X_exportedStatus", 10);
        ordUdaMap.put("X_batch", batchName);
        ordUdaMap.put("ct_plant", Order.getPlantCode());
        ordUdaMap.put("ct_WorkBreakdown", Order.getWbsElement());
        ordUdaMap.put("ct_Stability_Sample", OrderAdditionalData.getStabilitySample());
        ordUdaMap.put("ct_Stability_Sample_Qty", OrderAdditionalData.getStabilitySampleQuantity());
        ordUdaMap.put("ct_CustomerExpiryDate", OrderAdditionalData.getCustomerExpiryDate());
        ordUdaMap.put("ct_CustomerManufDate", OrderAdditionalData.getCustomerManufacturingDate());
        ordUdaMap.put("ct_Reference_sample", OrderAdditionalData.getReferenceSample());
        ordUdaMap.put("ct_Ref_sample_Qty", OrderAdditionalData.getReferenceSampleQuantity());
        ordUdaMap.put("ct_OtherSample", OrderAdditionalData.getOtherSample());
        ordUdaMap.put("ct_OtherSample_Qty", OrderAdditionalData.getOtherSampleQuantity());
        ordUdaMap.put("ct_NRSR_number", OrderAdditionalData.getNrsrNumber());
        ordUdaMap.put("ct_Customer_batch", OrderAdditionalData.getCustomerBatch());
        ordUdaMap.put("ct_Packaging_line", OrderAdditionalData.getPackagingLine());
        ordUdaMap.put("ct_Inspection_lot", inspectionLot);
        ordUdaMap.put("ct_GS1_element_string", gs1ElementString);
        ordUdaMap.put("ct_CA_Sample_Type", DefaultCASampleType);

        // UDAS SOLO PARA ORDENES DE PACKAGING

        if (orderType.equals("ZPCK")) {
            String packLvlCt1 = "";
            String pckLvl1 = "";
            String packLvlCt2 = "";
            // RM: Mirar el packContentLevel1 para saber la cantidad de Pieces, que es la que
            // interesa.
            
            if (material.getUDA("X_packagingLevelContent1") == null) {
                addError(this.getClass().getName(),
                        "This material:" + material + " don't have Contained Number L1 " + "(UDA_X_packagingLevelContent1). \n");
            } else {
                packLvlCt1 = material.getUDA("X_packagingLevelContent1").toString();
            ordUdaMap.put("ct_OsoPieceQty", packLvlCt1);
            }

            if (material.getUDA("X_packagingLevel1") == null) {
                addError(this.getClass().getName(), "This material: " + material + " don't have Meaning L1 " + "(UDA_X_packagingLevel1). \n");
            } else {
            // RM: Mirar si el material tiene Blister
                pckLvl1 = material.getUDA("X_packagingLevel1").toString();
            }
            // RM: Mirar el packContentLevel2 para saber la cantidad de Blister, que es la que
            // interesa.
            if (material.getUDA("X_packagingLevelContent2") == null) {
                addError(this.getClass().getName(),
                        "This material: " + material + " don't have Contianed Number L2 " + "(UDA_X_packagingLevelContent2). \n");
            } else {
                packLvlCt2 = material.getUDA("X_packagingLevelContent2").toString();
            }
            if (pckLvl1.equalsIgnoreCase(MESChoiceListHelper.getChoiceElement("PackagingLevel", "Blister").getValue().toString())) {
                ordUdaMap.put("ct_OsoBlisterQty", packLvlCt2);
            }
            

// -------------------------------------------------------- ZE1PRO_ADD_DATA
            // 24.E1AFKOL.ZE1PRO_ADD_DATA.ZCUS_VFDAT
            ordUdaMap.put("ct_CustomerExpiryDate", OrderAdditionalData.getCustomerExpiryDate());

            // 25.E1AFKOL.ZE1PRO_ADD_DATA.ZCUS_MANUFDAT
            ordUdaMap.put("ct_CustomerManufDate", OrderAdditionalData.getCustomerManufacturingDate());

// -------------------------------------------------------- ZE1PRO_CPS_PACK

            // 27.E1AFKOL.ZE1PRO_CPS_PACK.Z_CPS_VERSION
            ordUdaMap.put("ct_CPS_version", OrderCpsPack.getCpsVersion());

            // 28.E1AFKOL.ZE1PRO_CPS_PACK.ZZSERIALIZATION_REL
            if (OrderCpsPack.getSerializationRelevancy().toLowerCase().contains("x")) {
                ordUdaMap.put("ct_Serialization_relevancy", ValorUdaSi);
            } else {
                ordUdaMap.put("ct_Serialization_relevancy", ValorUdaNo);
            }
            // 29.E1AFKOL.ZE1PRO_CPS_PACK.ZZAGGR_BOX
            String aggregationBox = validate(true, OrderCpsPack.getAggregationBox(), String.class, "zZagger Box");
            ordUdaMap.put("ct_Aggregation_Box", aggregationBox);

            // 30.zZaggrPallet
            String aggregationPallet = validate(true, OrderCpsPack.getAggregationPallet(), String.class, "zZagger Pallet");
            ordUdaMap.put("ct_Aggregation_Pallet", aggregationPallet);

            // 31.boxPerOuterbox
            String boxPerOuterbox = validate(true, OrderCpsPack.getNumberOfBoxesPerOuterbox(), String.class, "Box per Outerbox");
            ordUdaMap.put("ct_Nboxes_per_outerbox", boxPerOuterbox);

            // 32.boxPerPallet
            String boxPerPallet = validate(true, OrderCpsPack.getNumberOfBoxesPerPallet(), String.class, "Box per pallets");
            ordUdaMap.put("ct_Nboxes_per_pallet", boxPerPallet);

            // 33.unitsPerPallet
            String unitsPerPallet = validate(true, OrderCpsPack.getNumberOfUnitsPerPallet(), String.class, "Units per pallet");
            ordUdaMap.put("ct_No_units_per_pallet", unitsPerPallet);

            // 34.productComments
            ordUdaMap.put("ct_Product_comments", OrderCpsPack.getProductComments());
            // AFH 22/02/2023 Probar el casteo a string
            ordUdaMap.put("ct_Units_P_Box", Long.toString(
                    Math.round(Double.parseDouble(unitsPerPallet) / (Double.parseDouble(boxPerPallet) * Double.parseDouble(boxPerOuterbox))), 0));

            // ------------------------------------------------------- ZE1PRO_CPS_COMP

            // 35.crossPerforation
            if (OrderCPSComponent.getCrossPerforation().toLowerCase().contains("y")) {
                ordUdaMap.put("ct_Cross_perforation", ValorUdaSi);
            } else {
                ordUdaMap.put("ct_Cross_perforation", ValorUdaNo);
            }
            // 36.laetusCodeLeaflet
            ordUdaMap.put("ct_Laetus_code_leaflet", OrderCPSComponent.getLaetusCodeLeaflet());

            // 37.laetusCodeBox
            ordUdaMap.put("ct_Laetus_code_box", OrderCPSComponent.getLaetusCodeBox());

            // 38.markBoxBatchNumber
            ordUdaMap.put("ct_Seq_Mark_Box1", OrderCPSComponent.getSequenceBoxBatchNumber());
            ordUdaMap.put("ct_Seq_Mark_Label1", OrderCPSComponent.getSequenceLabelBatchNumber());
            boxUDA.add(OrderCPSComponent.getSequenceBoxBatchNumber());
            lblUDA.add(OrderCPSComponent.getSequenceLabelBatchNumber());

            // 39.markBoxExpiryDate

            ordUdaMap.put("ct_Seq_Mark_Box2", OrderCPSComponent.getSequenceBoxExpiryDate());
            ordUdaMap.put("ct_Seq_Mark_Label2", OrderCPSComponent.getSequenceLabelExpiryDate());
            boxUDA.add(OrderCPSComponent.getSequenceBoxExpiryDate());
            lblUDA.add(OrderCPSComponent.getSequenceLabelExpiryDate());

            // 40.markBoxPrice

            ordUdaMap.put("ct_Seq_Mark_Box3", OrderCPSComponent.getSequenceBoxPrice());
            ordUdaMap.put("ct_Seq_Mark_Label3", OrderCPSComponent.getSequenceLabelPrice());
            boxUDA.add(OrderCPSComponent.getSequenceBoxPrice());
            lblUDA.add(OrderCPSComponent.getSequenceLabelPrice());

            // 41.markBoxManufacturingDate

            ordUdaMap.put("ct_Seq_Mark_Box4", OrderCPSComponent.getSequenceBoxManufacturingDate());
            ordUdaMap.put("ct_Seq_Mark_Label4", OrderCPSComponent.getSequenceLabelManufacturingDate());
            boxUDA.add(OrderCPSComponent.getSequenceBoxManufacturingDate());
            lblUDA.add(OrderCPSComponent.getSequenceLabelManufacturingDate());

            // 42.markBoxOthers

            ordUdaMap.put("ct_Seq_Mark_Box5", OrderCPSComponent.getSequenceBoxOthers());
            ordUdaMap.put("ct_Seq_Mark_Label5", OrderCPSComponent.getSequenceLabelOthers());
            boxUDA.add(OrderCPSComponent.getSequenceBoxOthers());
            lblUDA.add(OrderCPSComponent.getSequenceLabelOthers());

            // 43.markBoxSerialNumber

            ordUdaMap.put("ct_Seq_Mark_Box6", OrderCPSComponent.getSequenceBoxSerialNumber());
            ordUdaMap.put("ct_Seq_Mark_Label6", OrderCPSComponent.getSequenceLabelSerialNumber());
            boxUDA.add(OrderCPSComponent.getSequenceBoxSerialNumber());
            lblUDA.add(OrderCPSComponent.getSequenceLabelSerialNumber());

            // 44.markBoxProductCode

            ordUdaMap.put("ct_Seq_Mark_Box7", OrderCPSComponent.getSequenceBoxProductCode());
            ordUdaMap.put("ct_Seq_Mark_Label7", OrderCPSComponent.getSequenceLabelProductCode());
            boxUDA.add(OrderCPSComponent.getSequenceBoxProductCode());
            lblUDA.add(OrderCPSComponent.getSequenceLabelProductCode());

            // 54.formatCPS
            ordUdaMap.put("ct_Format_CPS", OrderCPSComponent.getFormatCPS());
            if (OrderCPSComponent.getFormatCPS().contains(" DM") && OrderCpsPack.getSerializationRelevancy().isEmpty()) {

                ordUdaMap.put("ct_Format_CPS_ContainsDM", ValorUdaSi);
            }

            // 55.customerItemNo
            ordUdaMap.put("ct_Customer_item_no", OrderCPSComponent.getCustomerItemNo());

//-------------------------------- RM: AR: -------- CODIGO PARA LOS PARAMETROS DE CAJA (MARK_BOX) 
// AFH Se añade seguridad para tratar que el valor del sequence no sea 0
            for (Integer i = 0; i < boxUDA.size(); i++) {
                switch (i) {
                case 0:
                    if ((boxUDA.get(i).isEmpty() != true) && (boxUDA.get(i).equalsIgnoreCase("0") != true)) {
                        for (OrderItem Oitem : Order.getOrderItems()) {
                            batchName = Oitem.getBatchName();
                        }
                        ordUdaMap.put("ct_Mark_Box" + boxUDA.get(i), OrderCPSComponent.getMarkBoxBatchNumber() + ": " + batchName);
                    } else {
                        logInfo(LOGGER, "The sequence box batch number is empty.");
                    }
                    break;
                case 1:
                    if ((boxUDA.get(i).isEmpty() != true) && (boxUDA.get(i).equalsIgnoreCase("0") != true)) {
                        ordUdaMap.put("ct_Mark_Box" + boxUDA.get(i),
                                OrderCPSComponent.getMarkBoxExpiryDate() + ": " + OrderAdditionalData.getCustomerExpiryDate());
                    } else {
                        logInfo(LOGGER, "The sequence box expiry date is empty.");
                    }
                    break;
                case 2:
                    if ((boxUDA.get(i).isEmpty() != true) && (boxUDA.get(i).equalsIgnoreCase("0") != true)) {
                        ordUdaMap.put("ct_Mark_Box" + boxUDA.get(i), OrderCPSComponent.getMarkBoxPrice());
                    } else {
                        logInfo(LOGGER, "The sequence box price is empty.");
                    }
                    break;
                case 3:
                    if ((boxUDA.get(i).isEmpty() != true) && (boxUDA.get(i).equalsIgnoreCase("0") != true)) {
                        ordUdaMap.put("ct_Mark_Box" + boxUDA.get(i),
                                OrderCPSComponent.getMarkBoxManufacturingDate() + ": " + OrderAdditionalData.getCustomerManufacturingDate());
                    } else {
                        logInfo(LOGGER, "The sequence box manufacturing date is empty.");
                    }
                    break;
                case 4:
                    if ((boxUDA.get(i).isEmpty() != true) && (boxUDA.get(i).equalsIgnoreCase("0") != true)) {
                        ordUdaMap.put("ct_Mark_Box" + boxUDA.get(i), OrderCPSComponent.getMarkBoxOthers());
                    } else {
                        logInfo(LOGGER, "The sequence box other is empty.");
                    }
                    break;
                case 5:
                    if ((boxUDA.get(i).isEmpty() != true) && (boxUDA.get(i).equalsIgnoreCase("0") != true)) {
                        ordUdaMap.put("ct_Mark_Box" + boxUDA.get(i), OrderCPSComponent.getMarkBoxSerialNumber());
                    } else {
                        logInfo(LOGGER, "The sequence box serial number is empty.");
                    }
                    break;
                case 6:
                    if ((boxUDA.get(i).isEmpty() != true) && (boxUDA.get(i).equalsIgnoreCase("0") != true)) {
                        ordUdaMap.put("ct_Mark_Box" + boxUDA.get(i),
                                OrderCPSComponent.getMarkBoxProductCode() + ": " + OrderCPSComponent.getValueBoxProductCode());
                    } else {
                        logInfo(LOGGER, "The sequence box product code is empty.");
                    }
                    break;
                }
            }

            // -------------------------------- RM: AR: -------- CODIGO PARA LOS PARAMETROS DE CAJA (LBL_BOX)

            for (Integer i = 0; i < lblUDA.size(); i++) {
                switch (i) {
                case 0:
                    if ((lblUDA.get(i).isEmpty() != true) && (lblUDA.get(i).equals("0") != true)) {
                        ordUdaMap.put("ct_Mark_Label" + lblUDA.get(i), OrderCPSComponent.getMarkLabelBatchNumber() + ": " + batchName);
                    } else {
                        logInfo(LOGGER, "The sequence box batch number is empty.");
                    }
                    break;
                case 1:
                    if ((lblUDA.get(i).isEmpty() != true) && (lblUDA.get(i).equalsIgnoreCase("0") != true)) {
                        ordUdaMap.put("ct_Mark_Label" + lblUDA.get(i),
                                OrderCPSComponent.getMarkLabelExpiryDate() + ": " + OrderAdditionalData.getCustomerExpiryDate());
                    } else {
                        logInfo(LOGGER, "The sequence box expiry date is empty.");
                    }
                    break;
                case 2:
                    if ((lblUDA.get(i).isEmpty() != true) && (lblUDA.get(i).equalsIgnoreCase("0") != true)) {
                        ordUdaMap.put("ct_Mark_Label" + lblUDA.get(i), OrderCPSComponent.getMarkLabelPrice());
                    } else {
                        logInfo(LOGGER, "The sequence box price is empty.");
                    }
                    break;
                case 3:
                    if ((lblUDA.get(i).isEmpty() != true) && (lblUDA.get(i).equalsIgnoreCase("0") != true)) {
                        ordUdaMap.put("ct_Mark_Label" + lblUDA.get(i),
                                OrderCPSComponent.getMarkLabelManufacturingDate() + ": " + OrderAdditionalData.getCustomerManufacturingDate());
                    } else {
                        logInfo(LOGGER, "The sequence box manufacturing date is empty.");
                    }
                    break;
                case 4:
                    if ((lblUDA.get(i).isEmpty() != true) && (lblUDA.get(i).equalsIgnoreCase("0") != true)) {
                        ordUdaMap.put("ct_Mark_Label" + lblUDA.get(i), OrderCPSComponent.getMarkLabelOthers());
                    } else {
                        logInfo(LOGGER, "The sequence box other is empty.");
                    }
                    break;
                case 5:
                    if ((lblUDA.get(i).isEmpty() != true) && (lblUDA.get(i).equalsIgnoreCase("0") != true)) {
                        ordUdaMap.put("ct_Mark_Label" + lblUDA.get(i), OrderCPSComponent.getMarkLabelSerialNumber());
                    } else {
                        logInfo(LOGGER, "The sequence box serial number is empty.");
                    }
                    break;
                case 6:
                    if ((lblUDA.get(i).isEmpty() != true) && (lblUDA.get(i).equalsIgnoreCase("0") != true)) {
                        ordUdaMap.put("ct_Mark_Label" + lblUDA.get(i),
                                OrderCPSComponent.getMarkLabelProductCode() + ": " + OrderCPSComponent.getValueLabelProductCode());
                    } else {
                        logInfo(LOGGER, "The sequence label product code is empty.");
                    }
                    break;
                }
            }
            // -------------------------------- RM: AR: -------- FIN CODIGO PARA LOS PARAMETROS DE CAJA

        } else
            logInfo(LOGGER, "That " + orderType + " dont have any UDA");

        if (getErrors() == null || getErrors().size() == 0) {

            /*
             * We are going to validate if batch exists, else we need to create it!
             */
            final IBatchService batchSrv = ServiceFactory.getService(IBatchService.class);
            final IInboundMessageService ims = ServiceFactory.getService(IInboundMessageService.class);

            Batch batch = batchSrv.loadBatchByCompoundIdentifier(batchName, material.getPartNumber());
            String batchStatusValue = AbstractBatchQualityTransitionEventListener.FSM_TRANS_QUARANTINE;

            if (batch == null) {
                // Si no existe el Batch de la OF, lo creo.
                BatchBuilder bb = new BatchBuilder(material).batchName(batchName);
                // RM: la linea de abajo la comento porque hace lo mismo que la siguiente a esa.
                // batchSrv.createBatch(bb);
                ims.createERPBatch(bb, batchStatusValue);
            }

            ErpOrderBuilder builder = new ErpOrderBuilder(orderNo, material, quantity).recipe(recipe).udaMap(ordUdaMap).components(components)
                    .batch(batchName).orderType(orderType);
            // IInboundMessageService ims = ServiceFactory.getService(IInboundMessageService.class);
            if (action == null) {
                addError(this.getClass().getName(), "Status action not found, check if the iDOC have <E1JSTKL> field. \n");
            } else {

            switch (action) {
            case CREATE:
                ims.createERPOrder(builder);
                break;
            case EXPLODE:    
                 ims.explodeERPOrder(builder);
                 if(orderType.equalsIgnoreCase("ZPCK")) {
                	 ims.releaseERPOrder(builder);
                 }
                 break;
            case DELETE:
                ims.deleteERPOrder(builder);
                break;
            case NONE:
                logError(LOGGER, "The list of transitions received doesn't match with any transition expected.");
                addError(this.getClass().getName(), "The list of transitions received doesn't match with any transition expected.\n");
                break;
            default:
                break;
            }
        }
        } else
            logError(LOGGER,
                    MessageFormat.format(resources.getString("ct_eihub_error.generic_error"), new Object[] { orderTransferObject.getIdoc() }));

    }

    /**
     * Retrieves the master recipe to be used for order explosion This lookup algorithm is likely to be different for
     * projects. In our scenario, the lookup is based on the product and ERP production version information
     * 
     * @param product The product
     * @param routeMap The Route Map
     * @return MasterRecipe The recipe
     * @throws DatasweepException thrown when error occurs
     */
    private MasterRecipe getValidMasterRecipe(final Part product, final String routeMap, final String OrderType) throws DatasweepException {
        logInfo(LOGGER, "Determining master recipe");
        MasterRecipe masterRecipe = null;
        if (product == null) {
            return masterRecipe;
        }
        String separator = MESConfiguration.getMESConfiguration().getString("eig_MasterRecipeDelimitString", "_", "Master recipe delimiter");
        // String recipeName = product.getPartNumber() + separator + routeMap;
        // Personalización de Synthon: no concatenaremos ProductMaterial_RouteMap, sino que solo hacemos "_routeMap;"
        String recipeName = separator + routeMap;
        MasterRecipeFilter masterRecipeFilter = PCContext.getFunctions().createMasterRecipeFilter();
        // masterRecipeFilter.forNameEqualTo(recipeName);
        masterRecipeFilter.forNameContaining(recipeName);

        // RaulDG: en pruebas para poder calcular el código de la receta cuando es receta de Pack
        // if (StringUtils.equals(OutboundMessageService.PACKAGING_ORDER_TYPE_VALUE, OrderType)) {
        // product.setPartNumber("DummyBOM_Material");
        // }
        // else {
        masterRecipeFilter.forProducedPartEqualTo(product);
        // }

        if (!RecipeConfiguration.allowNonValidMasterRecipeForOrder()) {
            masterRecipeFilter.forCurrentStateEqualTo("Valid");
        }
        final List recipeList = masterRecipeFilter.exec();

        if (recipeList.size() == 1) { // only return the master recipe if exactly 1 is found
            masterRecipe = (MasterRecipe) recipeList.get(0);
            logInfo(LOGGER, "Master recipe found: " + masterRecipe.getName());
        }

        return masterRecipe;
    }

    @Override
    public Long getObjectType() {
        return new Long(IObjectTypes.TYPE_PROCESSORDER);
    }

    /**
     * Method to validate the line items and bom positions (DUMMYBOM)
     * 
     * @param components - List of ErpOrderItemBuilder
     * @param mr - Master recipe
     * @throws DatasweepException - throws if any occurs
     */
    // Esta función usada en Dentaid, valida que las posiciones de la receta y las posiciones de lo que baja en el
    // Loipro casen 1 a 1 y no sobre ninguno
    private boolean validateLineItemsAndBomItems(List<ErpOrderItemBuilder> components, MasterRecipe mr, OrderTransferObject orderTransferObject)
            throws DatasweepException {
        // if (!StringUtils.equals("ZPCK", orderTransferObject.getOrderType())) {
        if (!StringUtils.equals(OutboundMessageService.PACKAGING_ORDER_TYPE_VALUE, "1")) {
            // Create a list to hold the positions for the up
            List<Integer> positions = new ArrayList<>();
            // Get the MES master recipe
            IMESMasterRecipe imesMr = new MESMasterRecipe(mr);

            List<MFC> mfcItems = mr.getMFCs();
            mfcItems = mfcItems.stream().filter(item -> TYPE.INPUT.ordinal() == item.getType()).collect(Collectors.toList());
            Map<Long, MFC> mfcMatParamMapping = new HashMap<>();
            for (MFC item : mfcItems) {
                mfcMatParamMapping.put(MESNamedUDAMFC.getMaterialInParameterKey(item), item);
            }
            // Get all the procedures
            List<IMESProcedure> procs = imesMr.getAllProcedures();
            for (IMESProcedure proc : procs) {
                // Get all the unit procedures
                List<IMESUnitProcedure> unitProcs = proc.getAllUnitProcedures();
                for (IMESUnitProcedure unitProc : unitProcs) {
                    // Get all operations of the UP
                    List<IMESOperation> ops = unitProc.getAllOperations();
                    for (IMESOperation op : ops) {
                        // Get all phases of the operations
                        List<IMESPhase> phases = op.getAllPhases();
                        for (IMESPhase phase : phases) {
                            // Iterate the material params and check that none of the material parameter has MFC
                            // position as empty and all the material parameters are in multiple of 10.
                            for (IMESMaterialParameter iMESMaterialParameter : phase.getMaterialParameters()) {
                                if ((StringUtils.isNotBlank(iMESMaterialParameter.getMFCPosition())
                                        && Integer.valueOf(iMESMaterialParameter.getMFCPosition()) % 10 != 0)
                                        || StringUtils.isBlank(iMESMaterialParameter.getMFCPosition())) {
                                    // If one of the material parameter does not satisfy the requirement return false;
                                    return false;
                                } else if (TYPE.INPUT.equals(iMESMaterialParameter.getType())
                                        && mfcMatParamMapping.containsKey(iMESMaterialParameter.getKey())) {
                                    positions.add(Integer.valueOf(iMESMaterialParameter.getMFCPosition()));
                                }
                            }
                        }
                    }
                }
            }
            // Filter the line items in multiple of 10
            List<ErpOrderItemBuilder> lineItems =
                    components.stream().filter(item -> Integer.valueOf(item.getPosition()) % 10 == 0).collect(Collectors.toList());

            // If the material parameters size is not same as line item size, their is a mismatch error.
            // Si el numero de posiciones que baja de SAP es = al numero de posiciones de la receta
            if (positions.size() == lineItems.size()) {
                // Also, check if all bom positions from Master Recipe are present in PO
                // Si las posiciones que tiene la receta están presentes en las posiciones que han venido de SAP
                for (ErpOrderItemBuilder lineItem : lineItems) {
                    if (!(positions.contains(Integer.valueOf(lineItem.getPosition())))) {
                        return false;
                    }
                }
            } else {
                return false;
            }
        }
        return true;
    }
}
