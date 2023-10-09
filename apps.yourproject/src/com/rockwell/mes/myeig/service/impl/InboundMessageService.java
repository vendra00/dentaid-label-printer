package com.rockwell.mes.myeig.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import javax.jms.JMSException;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.client.Batch;
import com.datasweep.compatibility.client.ControlRecipe;
import com.datasweep.compatibility.client.DatasweepException;
import com.datasweep.compatibility.client.INamedUDA;
import com.datasweep.compatibility.client.Location;
import com.datasweep.compatibility.client.MasterRecipe;
import com.datasweep.compatibility.client.MeasuredValue;
import com.datasweep.compatibility.client.OrderStep;
import com.datasweep.compatibility.client.OrderStepInput;
import com.datasweep.compatibility.client.Part;
import com.datasweep.compatibility.client.ProcessOrder;
import com.datasweep.compatibility.client.ProcessOrderItem;
import com.datasweep.compatibility.client.Sublot;
import com.datasweep.compatibility.client.TransitionReturnData;
import com.datasweep.compatibility.client.UnitOfMeasure;
import com.rockwell.custmes.services.order.ifc.IEnhancedOrderExplosionService;
import com.rockwell.mes.commons.base.ifc.choicelist.IMESChoiceElement;
import com.rockwell.mes.commons.base.ifc.choicelist.MESChoiceListHelper;
import com.rockwell.mes.commons.base.ifc.configuration.MESConfiguration;
import com.rockwell.mes.commons.base.ifc.exceptions.MESException;
import com.rockwell.mes.commons.base.ifc.exceptions.MESRuntimeException;
import com.rockwell.mes.commons.base.ifc.functional.MeasuredValueUtilities;
import com.rockwell.mes.commons.base.ifc.nameduda.MESNamedUDAOrderStepInput;
import com.rockwell.mes.commons.base.ifc.nameduda.MESNamedUDAPart;
import com.rockwell.mes.commons.base.ifc.nameduda.MESNamedUDAProcessOrderItem;
import com.rockwell.mes.commons.base.ifc.services.PCContext;
import com.rockwell.mes.commons.base.ifc.services.ServiceFactory;
import com.rockwell.mes.commons.base.ifc.services.Transactional;
import com.rockwell.mes.commons.base.impl.choicelist.MESChoiceElement;
import com.rockwell.mes.commons.messaging.ifc.MessagingActivity;
import com.rockwell.mes.commons.messaging.ifc.PharmaSuiteMessageListener;
import com.rockwell.mes.commons.messaging.ifc.PharmaSuiteMessageListenerForReuse;
import com.rockwell.mes.commons.versioning.ifc.VersionControlUtility;
import com.rockwell.mes.myeig.service.ifc.ErpSublotWrapper;
import com.rockwell.mes.myeig.service.ifc.ErpEquipmentWrapper;
import com.rockwell.mes.myeig.service.ifc.ErpOrderBuilder;
import com.rockwell.mes.myeig.service.ifc.ErpOrderBuilder.ErpOrderItemBuilder;
import com.rockwell.mes.myeig.service.ifc.IInboundMessageService;
import com.rockwell.mes.myeig.utility.TransactionalJob;
import com.rockwell.mes.services.commons.ifc.order.ProcessOrderItemFSMConstants;
import com.rockwell.mes.services.inventory.ifc.BatchBuilder;
import com.rockwell.mes.services.inventory.ifc.IBatchService;
import com.rockwell.mes.services.inventory.ifc.IMatMgmtSupportService;
import com.rockwell.mes.services.inventory.ifc.ISublotService;
import com.rockwell.mes.services.inventory.ifc.TransactionHistoryContext;
import com.rockwell.mes.services.inventory.impl.MESBatchAllocation;
import com.rockwell.mes.services.order.ifc.EnumOrderStepInputStatus;
import com.rockwell.mes.services.order.ifc.IMESOrderService;
import com.rockwell.mes.services.order.ifc.IOrderExplosionService;
import com.rockwell.mes.services.order.ifc.OrderUtils;
import com.rockwell.mes.services.recipe.ifc.EnumGHSStatementType;
import com.rockwell.mes.services.recipe.ifc.IMESGHSStatement;
import com.rockwell.mes.services.recipe.ifc.IMESRecipeService;
import com.rockwell.mes.services.s88.ifc.IS88RecipeService;
import com.rockwell.mes.services.s88.ifc.recipe.IMESERPBomHeader;
import com.rockwell.mes.services.s88.ifc.recipe.IMESERPBomItem;
import com.rockwell.mes.services.s88.ifc.recipe.IMESMaterialParameter;
import com.rockwell.mes.services.s88.impl.recipe.MESERPBomItemFilter;
import com.rockwell.mes.services.s88equipment.ifc.IS88EquipmentService;
import com.rockwell.mes.services.s88equipment.ifc.IMESS88Equipment;
import com.rockwell.mes.services.s88equipment.ifc.IMESS88EquipmentClass;
import com.rockwell.mes.services.s88.ifc.IS88EquipmentExecutionService;
import com.rockwell.mes.services.s88equipment.ifc.statusgraph.IMESS88StatusGraph;
import com.rockwell.mes.services.s88equipment.ifc.statusgraph.IMESS88StatusGraphAssignment;
import com.rockwell.mes.services.s88.ifc.execution.equipment.statusgraph.IS88StatusGraphFireTriggerResult;


/**
 * This service provides functionality for interface processing.
 * <p>
 *
 * @author syim, (c) Copyright 2012 Rockwell Automation Solutions, Inc. All Rights Reserved.
 */
public class InboundMessageService extends ErpMessageService implements IInboundMessageService {

	
    /** logger */
    private static final Log LOGGER = LogFactory.getLog(InboundMessageService.class);

    /** The category for MES parts */
    private static final String CATEGORY_MES = "MES";

    /** The default consumption type for parts */
    private static final String CONSUMPTION_TYPE_QTY = "Quantity";

    /** The default equipment class */
    private static final String DEFAULT_EQUIPMENT_CLASS = "Equipment_GMAO";

    /** The equipment graph identifier */
    private static final String EQUIPMENT_GRAPH_IDENTIFIER = "Graph_GMAO";

    /** The default trigger to be available */
    private static final String EQUIPMENT_GRAPH_NOT_AVAILABLE_IDENTIFIER = "Not_Available";

    /** The default trigger to be not available */
    private static final String EQUIPMENT_GRAPH_AVAILABLE_IDENTIFIER = "Available";

    /** The default trigger to be available */
    private static final String EQUIPMENT_TRIGGER_TO_BE_AVAILABLE = "TO_BE_AVAILABLE";

    /** The default trigger to be not available */
    private static final String EQUIPMENT_TRIGGER_TO_BE_UNAVAILABLE = "TO_BE_UNAVAILABLE";

    /** The Order Service */
    private static final IMESOrderService ORDER_SERVICE = ServiceFactory.getService(IMESOrderService.class);

    /** The enhanced order explosion service */
    private static final IEnhancedOrderExplosionService ORDER_EXPLOSION_SERVICE = ServiceFactory.getService(IEnhancedOrderExplosionService.class);

    /* private static final IOrderExplosionService ORDER_EXPLOSION_SERVICE = ServiceFactory.getService(IOrderExplosionService.class); */

    private static final IMESRecipeService RECIPE_SERVICE = ServiceFactory.getService(IMESRecipeService.class);

    private static final IS88RecipeService S88_RECIPE_SERVICE = ServiceFactory.getService(IS88RecipeService.class);

    private static final IBatchService BATCH_SERVICE = ServiceFactory.getService(IBatchService.class);

    private static final ISublotService SUBLOT_SERVICE = ServiceFactory.getService(ISublotService.class);

    private static final IS88EquipmentService S88_EQUIPMENT_SERVICE = ServiceFactory.getService(IS88EquipmentService.class);

    private static final IS88EquipmentExecutionService S88_EQUIPMENT_EXECUTION_SERVICE =
            ServiceFactory.getService(IS88EquipmentExecutionService.class);

    private static final String PlannedMode_None = "10";

    private static final String PlannedMode_Defined = "20";

    private static final String PlannedMode_Produced = "30";


    ProcessOrder processOrder;

    public enum ACTION {
        UNRELEASE_PO,
        UNEXPLODED_PO,
        REMOVE_PO,
        CREATE_PO,
        EXPLODE_PO,
        RELEASE_PO,
        NO_ACTION
    }

    private List<ACTION> actions = new ArrayList<>();

    @Override
    @Transactional
    public void createERPMaterial(String number, String desc, Map<String, Object> udas, String HazardDataString, String PrecautionaryDataString)
            throws DatasweepException {

        // Clear the Part Cache
        PCContext.getServerImpl().getSiteCache().getPartCache().setCacheEnabled(false);

        // check if part already exists
        Part materialPart = RECIPE_SERVICE.getPart(number);

        if (materialPart == null) { // create material if none exists in FTPS
            LOGGER.info("Creating new material: " + number);
            materialPart = RECIPE_SERVICE.createPart(number);
            materialPart.setCategory(CATEGORY_MES);
            materialPart.setBomTrackedMode(0); // TRACKEDMODE_EXPLODED
            materialPart.setConsumptionType(CONSUMPTION_TYPE_QTY);
            materialPart.setExtendedRevision(VersionControlUtility.DEFAULT_REVISION);
            materialPart.setRevision(VersionControlUtility.DEFAULT_REVISION);
            MESNamedUDAPart.setInternal(materialPart, 0L);
        } else {
            LOGGER.info("Updating material:  " + number);
            udas.remove("X_allowedWeighingMethods");
        }

        materialPart.setDescription(desc);

        // set UDAs of Part
        setUdasOnObject(materialPart, udas);

        materialPart.Save(null, null, PCContext.getDefaultAccessPrivilege());

        // ---------------------------------------------------------------------------------------------------------
        // Hazard Assignment

        // Primero de todo hay que borrar los Hazard que ya tenga el material
        List<IMESGHSStatement> ListGHSStatementH = RECIPE_SERVICE.getGHSStatementsForPart(materialPart, EnumGHSStatementType.HAZARD_STATEMENT);
        for (IMESGHSStatement iteration : ListGHSStatementH) {
            RECIPE_SERVICE.detachGHSStatementFromPart(iteration, materialPart, null, null, null);
        }

        // Como los datos del Hazard y Precautionary vienen separados por punto y coma, tengo que ir recorriendolos
        StringBuilder delimiter = new StringBuilder(1);
        delimiter.append(";");

        // Cuento los token que hay separados por punto y coma
        StringTokenizer HazardGHSTokens = new StringTokenizer(HazardDataString, delimiter.toString());

        // Si solo han pasado 1parámetro (sin ningún punto y coma), lo asigno directamente
        if (HazardGHSTokens.countTokens() <= 1) {

            IMESGHSStatement statement = RECIPE_SERVICE.getGHSStatement(HazardDataString);
            if (statement != null) {
                RECIPE_SERVICE.attachGHSStatementToPart(statement, materialPart, null, number, desc);
                materialPart.Save(null, null, PCContext.getDefaultAccessPrivilege());
            }
        }

        // Si han pasado más de 1 parámetro separado por punto y coma, los recorro
        else {
            while (HazardGHSTokens.countTokens() > 0) {
                String HazardGHS_Loop = HazardGHSTokens.nextToken();
                IMESGHSStatement statement = RECIPE_SERVICE.getGHSStatement(HazardGHS_Loop);
                if (statement != null) {
                    RECIPE_SERVICE.attachGHSStatementToPart(statement, materialPart, null, number, desc);
                    materialPart.Save(null, null, PCContext.getDefaultAccessPrivilege());
                }
            }
        }

        // ---------------------------------------------------------------------------------------------------------
        // Precautionary Assignment

        // Primero de todo hay que borrar los Precautionary que ya tenga el material
        List<IMESGHSStatement> ListGHSStatementP = RECIPE_SERVICE.getGHSStatementsForPart(materialPart, EnumGHSStatementType.PRECAUTIONARY_STATEMENT);
        for (IMESGHSStatement iteration : ListGHSStatementP) {
            RECIPE_SERVICE.detachGHSStatementFromPart(iteration, materialPart, null, null, null);
        }

        // Cuento los token que hay separados por punto y coma
        StringTokenizer PrecautionaryGHSTokens = new StringTokenizer(PrecautionaryDataString, delimiter.toString());

        // Si solo han pasado 1parámetro (sin ningún punto y coma), lo asigno directamente
        if (PrecautionaryGHSTokens.countTokens() <= 1) {

            IMESGHSStatement statement = RECIPE_SERVICE.getGHSStatement(PrecautionaryDataString);
            if (statement != null) {
                RECIPE_SERVICE.attachGHSStatementToPart(statement, materialPart, null, number, desc);
                materialPart.Save(null, null, PCContext.getDefaultAccessPrivilege());
            }
        }

        // Si han pasado más de 1 parámetro separado por punto y coma, los recorro
        else {
            while (PrecautionaryGHSTokens.countTokens() > 0) {
                String PrecautionaryGHS_Loop = PrecautionaryGHSTokens.nextToken();
                IMESGHSStatement statement = RECIPE_SERVICE.getGHSStatement(PrecautionaryGHS_Loop);
                if (statement != null) {
                    RECIPE_SERVICE.attachGHSStatementToPart(statement, materialPart, null, number, desc);
                    materialPart.Save(null, null, PCContext.getDefaultAccessPrivilege());
                }
            }
        }

    }

    @Override
    @Transactional
    public void createERPBom(Part material, String method, MeasuredValue baseQty, List<Map<String, Object>> items)
            throws DatasweepException {

        IMESERPBomHeader erpBomHeader = S88_RECIPE_SERVICE.getERPBomHeader(material, Integer.parseInt(method));
        if (erpBomHeader == null) {
            LOGGER.info("Creating new ERP Bom: " + material.getPartNumber() + "." + method);
            erpBomHeader = S88_RECIPE_SERVICE.createERPBomHeader(material, Integer.parseInt(method), baseQty, true);
        } else {
            LOGGER.info("Updating ERP Bom: " + material.getPartNumber() + "." + method);
            // if update, delete BOM positions which are not in the message
            for (IMESERPBomItem erpBomItem : S88_RECIPE_SERVICE.getERPBomItems(erpBomHeader)) {
                Boolean posFound = false;
                for (Map<String, Object> item : items) {
                    if (erpBomItem.getPosition().equals(item.get("position"))) {
                        posFound = true;
                        break;
                    }
                }
                if (!posFound) {
                    erpBomItem.Delete(null, null, PCContext.getDefaultAccessPrivilege());
                }
            }
            erpBomHeader.setBaseQty(baseQty);
        }

        erpBomHeader.Save(null, null, PCContext.getDefaultAccessPrivilege());

        // BOM items
        for (Map<String, Object> item : items) {
            Part component = (Part) item.get("material");
            String position = (String) item.get("position");
            MeasuredValue plannedQty = (MeasuredValue) item.get("plannedQty");

            List<IMESERPBomItem> list = getErpBomItems(erpBomHeader, position);
            IMESERPBomItem erpBomItem;
            if (list.size() < 1) { // position does not exist, create it
                erpBomItem = S88_RECIPE_SERVICE.createERPBomItem(erpBomHeader, component, position, plannedQty);
            } else { // update
                erpBomItem = list.get(0);
                erpBomItem.setMaterial(component);
                erpBomItem.setPlannedQty(plannedQty);
            }

            Boolean fixedQty = (Boolean) item.get("fixedQty");
            erpBomItem.setFixedQty(fixedQty);
            erpBomItem.Save(null, null, PCContext.getDefaultAccessPrivilege());
        }
    }

    @Override
    @Transactional
    public void createERPBatch(BatchBuilder builder, String transition) throws DatasweepException, MESException {

        // Clear the Batch Cache
        PCContext.getServerImpl().getSiteCache().getBatchCache().setCacheEnabled(false);

        String batchNo = builder.getBatchName();
        String materialNo = builder.getPart().getPartNumber();

        // Añadido - no_original
        // final IInboundMessageService ims = ServiceFactory.getService(IInboundMessageService.class);
        //

        final IBatchService batchSrv = ServiceFactory.getService(IBatchService.class);
        Batch batch = BATCH_SERVICE.loadBatchByCompoundIdentifier(batchNo, materialNo);

        if (batch == null) { // create the batch if none exists in FTPS
            LOGGER.info("Creating batch: " + batchNo + " material: " + materialNo);
            batch = batchSrv.createBatch(builder);
            // apply status transition, and adjust expiration and retest date
            batchSrv.applyQualityStatusTransitionWithTxContext2(batch, transition, builder.getExpiryDate(), builder.getRetestDate(), null);
        } else {
            LOGGER.info("Updating batch: " + batchNo + " material: " + materialNo);

            TransactionHistoryContext thCtx = new TransactionHistoryContext();
            thCtx.setRemark(TRANS_HIST_COMMENT);
            // Update potency & attributes
            batchSrv.changeBatchAttributes(batch, builder.getPotency(), builder.getUDAMap(), thCtx);

            // We only change getExpiryDate. The retest date is setted before with UDAMap
            batchSrv.setBatchExpiryDate(batch, builder.getExpiryDate());
            

            // Utilizamos esta función en vez de setBatchExpiryDate porque esta función genera una transacción que se
            // puede ver en el PMC. Con esta funcion se actualiza el Status y el ExpiricyDate
            batchSrv.applyQualityStatusTransitionWithTxContext2(batch, transition, builder.getRetestDate(), builder.getRetestDate(), null);
            batchSrv.setBatchExpiryDate(batch, builder.getExpiryDate());
            // Añadido - no_original
            // ims.updateERPBatchStatus(new BatchBuilder(builder.getPart()).batchName(batchNo), transition);
            //
        }
    }

    @Override
    @Transactional
    public void updateERPBatchStatus(BatchBuilder builder, String transition) throws DatasweepException, MESException {

        // Clear the Batch Cache
        PCContext.getServerImpl().getSiteCache().getBatchCache().setCacheEnabled(false);

        String batchNo = builder.getBatchName();
        String materialNo = builder.getPart().getPartNumber();

        final IBatchService batchSrv = ServiceFactory.getService(IBatchService.class);
        Batch batch = BATCH_SERVICE.loadBatchByCompoundIdentifier(batchNo, materialNo);

        // apply status transition
        if (batch != null)
        {
            if (transition != null)
                batchSrv.applyQualityStatusTransitionExc(batch, transition);
        }
        else
            LOGGER.error(String.format("Batch should not be null!"));
    }

    // SGO - IMPLEMENTACIÓN DELETE ERP ORDER
    @Override
    // @Transactional
    public synchronized void deleteERPOrder(ErpOrderBuilder builder) throws DatasweepException, MESException {

        // ADASOFT - remove actions list
        actions.clear();

        String orderNumber = builder.getOrderNumber();
        processOrder = PCContext.getFunctions().getProcessOrderByName(orderNumber);

        if (processOrder == null) { // not exist
            LOGGER.info("Process Order " + orderNumber + " does not exist in PharmaSuite. Cannot be deleted.");
        } else { // delete
            LOGGER.info("Deleting Order " + orderNumber + " in PharmaSuite");
            for (Object poiObj : processOrder.getProcessOrderItems()) {
                // If an order with this order number does exist
                ProcessOrderItem poi = (ProcessOrderItem) poiObj;
                String poiStatus = poi.getCurrentState("orderStatus").getState().getName();

                if ("Defined".equals(poiStatus) || "Exploded".equals(poiStatus) || "Released".equals(poiStatus)) {
                    if ("Released".equals(poiStatus)) {
                        actions.add(ACTION.UNRELEASE_PO);
                        actions.add(ACTION.UNEXPLODED_PO);
                    } else if ("Exploded".equals(poiStatus)) {
                        actions.add(ACTION.UNEXPLODED_PO);
                    } // order number does exist, but has not yet been released
                    actions.add(ACTION.REMOVE_PO);

                } else { // updates only allowed if less than release state
                    throw new MESRuntimeException("Order " + orderNumber + " already exists in a non-modificable state");
                }
            }
        }

        TransactionalJob<Void> jobRemovePO = new TransactionalJob<Void>() {
            @Override
            protected Void execute() {
                try {
                    removeUnreleasedOrder(builder);
                } catch (Throwable e) {
                    try {
                        rollback();
                    } catch (Throwable e1) {
                        LOGGER.error(ExceptionUtils.getStackTrace(e1));
                        throw new MESRuntimeException("Issue in rollback remove Order " + orderNumber, e1);
                    }
                    throw new MESRuntimeException("Issue in removing order " + builder.getOrderNumber(), e);
                }
                return null;
            }
        };

        TransactionalJob<Void> jobUnReleasePO = new TransactionalJob<Void>() {
            @Override
            protected Void execute() {
                try {
                    unReleaseProcessOrder(builder);
                } catch (Throwable e) {
                    try {
                        rollback();
                    } catch (Throwable e1) {
                        LOGGER.error(ExceptionUtils.getStackTrace(e1));
                        throw new MESRuntimeException("Issue in rollback unrelease Order " + orderNumber, e1);
                    }
                    throw new MESRuntimeException("Issue in unrelease order " + builder.getOrderNumber(), e);
                }
                return null;
            }
        };
        TransactionalJob<Void> jobUnExplodePO = new TransactionalJob<Void>() {
            @Override
            protected Void execute() {
                try {
                    unExplodeProcessOrder(builder);
                } catch (Throwable e) {
                    try {
                        rollback();
                    } catch (Throwable e1) {
                        LOGGER.error(ExceptionUtils.getStackTrace(e1));
                        throw new MESRuntimeException("Issue in rollback unexplode Order " + orderNumber, e1);
                    }
                    throw new MESRuntimeException("Issue in unexplode order " + builder.getOrderNumber(), e);
                }
                return null;
            }
        };

        for (ACTION action : actions) {
            switch (action) {
            case UNRELEASE_PO:
                jobUnReleasePO.run();
                break;
            case UNEXPLODED_PO:
                jobUnExplodePO.run();
                break;
            case REMOVE_PO:
                jobRemovePO.run();
                break;
            case NO_ACTION:
            default:
                break;
            }
        }

    }

    // SGO - FIN IMPLEMENTACIÓN DELETE ERP ORDER

    // RMM - IMPLEMENTACIÓN RELEASE ERP ORDER
    @Override
    // @Transactional
    public synchronized void releaseERPOrder(ErpOrderBuilder builder) throws DatasweepException, MESException {
    	// ADASOFT - remove actions list
        actions.clear();

        String orderNumber = builder.getOrderNumber();
        //processOrder = PCContext.getFunctions().getProcessOrderByName(orderNumber);
        processOrder = ORDER_SERVICE.loadOrder(orderNumber);
        
        if (processOrder == null) { // not exist
            LOGGER.info("Process Order " + orderNumber + " does not exist in PharmaSuite. Cannot be released.");
        } else { // release
            LOGGER.info("Relase Order " + orderNumber + " in PharmaSuite");
            for (Object poiObj : processOrder.getProcessOrderItems()) {
                // If an order with this order number does exist
                ProcessOrderItem poi = (ProcessOrderItem) poiObj;
                String poiStatus = poi.getCurrentState("orderStatus").getState().getName();
/*// RM 04/05/2023: DESCOMENTAR ESTA PARTE DEL CODIGO CUANDO SE QUIERA QUE LAS ORDENES DE PACKAGING PASEN A RELEASE DESPUES DE EXPLODE
                if ("Exploded".equals(poiStatus)&& builder.getOrderType().equals("ZPCK")) {
                	actions.add(ACTION.RELEASE_PO);
                } else { // updates only allowed if less than release state
                    throw new MESRuntimeException("Order " + orderNumber + " already exists in a non-modificable state");
                }
*/

                
            }
        }
        TransactionalJob<Void> jobReleasePO = new TransactionalJob<Void>() {
            @Override
            protected Void execute() {
                try {
                	// 23/03/2023 revisar
                	startMessagingActivity();
                    releaseProcessOrder(builder);
                } catch (Throwable e) {
                    try {
                        rollback();
                    } catch (Throwable e1) {
                        LOGGER.error(ExceptionUtils.getStackTrace(e1));
                        throw new MESRuntimeException("Issue in rollback release Order " + orderNumber, e1);
                    }
                    throw new MESRuntimeException("Issue in releasing order " + builder.getOrderNumber(), e);
                }
                return null;
            }
        };
        for (ACTION action : actions) {
            switch (action) {
            case RELEASE_PO:
                jobReleasePO.run();
                break;
            case NO_ACTION:
            default:
                break;
            }
        }
    }
    // RMM - FIN IMPLEMENTACIÓN RELEASE ERP ORDER
    @Override
    // @Transactional
    public synchronized void createERPOrder(ErpOrderBuilder builder) throws DatasweepException, MESException {

        // ADASOFT - remove actions list
        actions.clear();

        String orderNumber = builder.getOrderNumber();
        processOrder = PCContext.getFunctions().getProcessOrderByName(orderNumber);

        if (processOrder == null) { // create
            LOGGER.info("Creating Order " + orderNumber + " in PharmaSuite");
            actions.add(ACTION.CREATE_PO);

        } else { // update
            LOGGER.info("Updating Order " + orderNumber + " in PharmaSuite");
            for (Object poiObj : processOrder.getProcessOrderItems()) {
                // If an order with this order number does exist
                ProcessOrderItem poi = (ProcessOrderItem) poiObj;
                String poiStatus = poi.getCurrentState("orderStatus").getState().getName();

                if ("Defined".equals(poiStatus) || "Exploded".equals(poiStatus) || "Released".equals(poiStatus)) {
                    if ("Released".equals(poiStatus)) {
                        actions.add(ACTION.UNRELEASE_PO);
                        actions.add(ACTION.UNEXPLODED_PO);
                    } else if ("Exploded".equals(poiStatus)) {
                        actions.add(ACTION.UNEXPLODED_PO);
                    }
                    // order number does exist, but has not yet been released
                    actions.add(ACTION.REMOVE_PO);
                    actions.add(ACTION.CREATE_PO);

                } else { // updates only allowed if less than release state
                    throw new MESRuntimeException("Order " + orderNumber + " already exists in a non-modificable state");
                }
            }
        }

        TransactionalJob<Void> jobRemovePO = new TransactionalJob<Void>() {
            @Override
            protected Void execute() {
                try {
                    removeUnreleasedOrder(builder);
                } catch (Throwable e) {
                    try {
                        rollback();
                    } catch (Throwable e1) {
                        LOGGER.error(ExceptionUtils.getStackTrace(e1));
                        throw new MESRuntimeException("Issue in rollback remove Order " + orderNumber, e1);
                    }
                    //throw new MESRuntimeException("Issue in removing order " + builder.getOrderNumber());
                    throw new MESRuntimeException("Issue in removing order " + builder.getOrderNumber(), e);
                }
                return null;
            }
        };

        TransactionalJob<Void> jobUnReleasePO = new TransactionalJob<Void>() {
            @Override
            protected Void execute() {
                try {
                    unReleaseProcessOrder(builder);
                } catch (Throwable e) {
                    try {
                        rollback();
                    } catch (Throwable e1) {
                        LOGGER.error(ExceptionUtils.getStackTrace(e1));
                        throw new MESRuntimeException("Issue in rollback unrelease Order " + orderNumber, e1);
                    }
                    throw new MESRuntimeException("Issue in unrelease order " + builder.getOrderNumber(), e);
                }
                return null;
            }
        };
        TransactionalJob<Void> jobUnExplodePO = new TransactionalJob<Void>() {
            @Override
            protected Void execute() {
                try {
                    unExplodeProcessOrder(builder);
                } catch (Throwable e) {
                    try {
                        rollback();
                    } catch (Throwable e1) {
                        LOGGER.error(ExceptionUtils.getStackTrace(e1));
                        throw new MESRuntimeException("Issue in rollback unexplode Order " + orderNumber, e1);
                    }
                    throw new MESRuntimeException("Issue in unexplode order " + builder.getOrderNumber(), e);
                }
                return null;
            }
        };

        TransactionalJob<Void> jobCreateOrder = new TransactionalJob<Void>() {
            @Override
            protected Void execute() {
                try {
                    createProcessOrder(builder);
                } catch (Throwable e) {
                    try {
                        rollback();
                    } catch (Throwable e1) {
                        LOGGER.error(ExceptionUtils.getStackTrace(e1));
                        throw new MESRuntimeException("Issue in rollback create Order " + orderNumber, e1);
                    }
                    throw new MESRuntimeException("Issue in create Order " + orderNumber, e);
                    
                }
                return null;
            }
        };


        for (ACTION action : actions) {
            switch (action) {
            case UNRELEASE_PO:
                jobUnReleasePO.run();
                break;
            case UNEXPLODED_PO:
                jobUnExplodePO.run();
                break;
            case REMOVE_PO:
                jobRemovePO.run();
                break;
            case CREATE_PO:
                jobCreateOrder.run();
                break;
            case NO_ACTION:
            default:
                break;
            }
        }

    }
    
    @Override
    // @Transactional
    public synchronized void explodeERPOrder(ErpOrderBuilder builder) throws DatasweepException, MESException {


        // ADASOFT - remove actions list
        actions.clear();

        String orderNumber = builder.getOrderNumber();
        processOrder = PCContext.getFunctions().getProcessOrderByName(orderNumber);

        if (processOrder == null) { // create
            LOGGER.info("Creating Order " + orderNumber + " in PharmaSuite");
            actions.add(ACTION.CREATE_PO);
            LOGGER.info("Exploding Order " + orderNumber + " in PharmaSuite");
            actions.add(ACTION.EXPLODE_PO);

        } else { // update
            LOGGER.info("Updating Order " + orderNumber + " in PharmaSuite");
            for (Object poiObj : processOrder.getProcessOrderItems()) {
                // If an order with this order number does exist
                ProcessOrderItem poi = (ProcessOrderItem) poiObj;
                String poiStatus = poi.getCurrentState("orderStatus").getState().getName();

                if ("Defined".equals(poiStatus) || "Exploded".equals(poiStatus) || "Released".equals(poiStatus)) {
                    if ("Released".equals(poiStatus)) {
                        actions.add(ACTION.UNRELEASE_PO);
                        actions.add(ACTION.UNEXPLODED_PO);
                    } else if ("Exploded".equals(poiStatus)) {
                        actions.add(ACTION.UNEXPLODED_PO);
                    }
                    // order number does exist, but has not yet been released
                    actions.add(ACTION.REMOVE_PO);
                    actions.add(ACTION.CREATE_PO);
                    actions.add(ACTION.EXPLODE_PO);

                } else { // updates only allowed if less than release state
                    throw new MESRuntimeException("Order " + orderNumber + " already exists in a non-modificable state");
                }
            }
        }

        TransactionalJob<Void> jobRemovePO = new TransactionalJob<Void>() {
            @Override
            protected Void execute() {
                try {
                    removeUnreleasedOrder(builder);
                } catch (Throwable e) {
                    try {
                        rollback();
                    } catch (Throwable e1) {
                        LOGGER.error(ExceptionUtils.getStackTrace(e1));
                        throw new MESRuntimeException("Issue in rollback remove Order " + orderNumber, e1);
                    }
                    //throw new MESRuntimeException("Issue in removing order " + builder.getOrderNumber());
                    throw new MESRuntimeException("Issue in removing order " + builder.getOrderNumber(), e);
                }
                return null;
            }
        };

        TransactionalJob<Void> jobUnReleasePO = new TransactionalJob<Void>() {
            @Override
            protected Void execute() {
                try {
                    unReleaseProcessOrder(builder);
                } catch (Throwable e) {
                    try {
                        rollback();
                    } catch (Throwable e1) {
                        LOGGER.error(ExceptionUtils.getStackTrace(e1));
                        throw new MESRuntimeException("Issue in rollback unrelease Order " + orderNumber, e1);
                    }
                    throw new MESRuntimeException("Issue in unrelease order " + builder.getOrderNumber(), e);
                }
                return null;
            }
        };

        TransactionalJob<Void> jobUnExplodePO = new TransactionalJob<Void>() {
            @Override
            protected Void execute() {
                try {
                    unExplodeProcessOrder(builder);
                } catch (Throwable e) {
                    try {
                        rollback();
                    } catch (Throwable e1) {
                        LOGGER.error(ExceptionUtils.getStackTrace(e1));
                        throw new MESRuntimeException("Issue in rollback unexplode Order " + orderNumber, e1);
                    }
                    throw new MESRuntimeException("Issue in unexplode order " + builder.getOrderNumber(), e);
                }
                return null;
            }
        };

        TransactionalJob<Void> jobCreateOrder = new TransactionalJob<Void>() {
            @Override
            protected Void execute() {
                try {
                    createProcessOrder(builder);
                } catch (Throwable e) {
                    try {
                        rollback();
                    } catch (Throwable e1) {
                        LOGGER.error(ExceptionUtils.getStackTrace(e1));
                        throw new MESRuntimeException("Issue in rollback create Order " + orderNumber, e1);
                    }
                    throw new MESRuntimeException("Issue in create Order " + orderNumber, e);
                    
                }
                return null;
            }
        };

        TransactionalJob<Void> jobExplodeOrder = new TransactionalJob<Void>() {
            @Override
            protected Void execute() {
                try {
                    explodeProcessOrder(builder);
                } catch (Throwable e) {
                    try {
                        rollback();
                    } catch (Throwable e1) {
                        LOGGER.error(ExceptionUtils.getStackTrace(e1));
                        throw new MESRuntimeException("Issue in rollback exploding Order " + orderNumber, e1);
                    }
                    throw new MESRuntimeException("Issue in exploding Order " + orderNumber, e);

                }
                return null;
            }
        };


        for (ACTION action : actions) {
            switch (action) {
            case UNRELEASE_PO:
                jobUnReleasePO.run();
                break;
            case UNEXPLODED_PO:
                jobUnExplodePO.run();
                break;
            case REMOVE_PO:
                jobRemovePO.run();
                break;
            case CREATE_PO:
                jobCreateOrder.run();
                break;
            case EXPLODE_PO:
                jobExplodeOrder.run();
                break;
            case NO_ACTION:
            default:
                break;
            }
        }

    }
    /**
     * release process Order
     *
     * @param processorder
     * @param messageData  : data of incoming message
     * @throws JMSException 
     */
    public void releaseProcessOrder(ErpOrderBuilder builder) throws DatasweepException, MESException, JMSException {

        String orderNumber = builder.getOrderNumber();

        ProcessOrder processOrder = PCContext.getFunctions().getProcessOrderByName(orderNumber);
        processOrder = ORDER_SERVICE.loadOrder(orderNumber);

        ProcessOrderItem poi = (ProcessOrderItem) processOrder.getProcessOrderItems().get(0);

        LOGGER.info("Release order " + poi.getOrderName());
        ORDER_SERVICE.applyOrderStatusTransitionExc(ProcessOrderItemFSMConstants.FSM_TRANS_RELEASE, poi);
        

    }
    /**
     * Unrelease process Order
     *
     * @param processorder
     * @param messageData  : data of incoming message
     */
    private void unReleaseProcessOrder(ErpOrderBuilder builder) throws DatasweepException, MESException {

        String orderNumber = builder.getOrderNumber();

        processOrder = ORDER_SERVICE.loadOrder(orderNumber);

        ProcessOrderItem poi = (ProcessOrderItem) processOrder.getProcessOrderItems().get(0);

        ORDER_SERVICE.applyOrderStatusTransitionExc(ProcessOrderItemFSMConstants.FSM_TRANS_UNDORELEASE, poi, false);
        poi.getParent().Save(PCContext.getCurrentServerTime(), "EIG: Undo Release PO: " + poi.getOrderName(),
                PCContext.getDefaultAccessPrivilege());


        LOGGER.info("Undo Release Order: " + orderNumber);
    }

    /**
     * UnExplode Process Order
     *
     * @param processorder
     * @param messageData  : data of incoming message
     */
    private void unExplodeProcessOrder(ErpOrderBuilder builder) throws DatasweepException, MESException {

        String orderNumber = builder.getOrderNumber();
        // found on 02/06/2020
        // ORDER_SERVICE.
        //
        ProcessOrder processOrder = PCContext.getFunctions().getProcessOrderByName(orderNumber);
        processOrder = ORDER_SERVICE.loadOrder(orderNumber);

        ProcessOrderItem poi = (ProcessOrderItem) processOrder.getProcessOrderItems().get(0);
        // processOrder.refresh();

        LOGGER.info("UnExplode order " + poi.getOrderName());

        ORDER_EXPLOSION_SERVICE.undoExplosion(poi);
    }

    public void createProcessOrder(ErpOrderBuilder builder) throws DatasweepException, MESException {
        String orderNumber = builder.getOrderNumber();// Numero de Orden del LOIPRO
        Part product = builder.getPart();// MaterialOutput del LOIPRO
        
        ProcessOrder processOrder = PCContext.getFunctions().getProcessOrderByName(orderNumber);

        // create ProcessOrderItem if not already created
        // AR y RM:11/ENE/2023-- Si la orden no esta creada, creamos la orden.
        if (processOrder == null) { // create
            LOGGER.info("Creating Order " + orderNumber + " in PharmaSuite");
            processOrder = PCContext.getFunctions().createProcessOrder(orderNumber);
        }
        // AR y RM:11/ENE/2023
        // Hemos creado la orden pero no tiene ningun item con ese nunmero de orden, por eso comparamos si es null, si
        // es null
        // creamos los items de la orden con sus parametros.
        if (processOrder.getProcessOrderItem(orderNumber) == null) {
            ProcessOrderItem poi = processOrder.createProcessOrderItem(orderNumber,
            product.getPartNumber(), product.getPartRevision(), builder.getQuantity());// Porque partNumber vale
                                                                                               // lo mismo que
         // RM 17/04/2023: Poner valor por defecto a esta UDA, deberia ponerse en el ChoiceList pero no va.
            setUdasOnObject(poi, builder.getUdaMap()); // set UDAs of ProcessOrderItem

            processOrder.Save(null, null, PCContext.getDefaultAccessPrivilege());

            MasterRecipe recipe = builder.getRecipe();
            if (recipe != null) { // explode if recipe is specified
                ORDER_SERVICE.attachMasterRecipe(poi, builder.getRecipe());

                String batchName = builder.getBatch();
                // CRA:BATCH
                // Batch batch = batchService.loadBatch(batchName);
                Batch batch = BATCH_SERVICE.loadBatchByCompoundIdentifier(batchName, product.getPartNumber());
                if (batch == null) { // create Produced Batch if it doesn't yet exist
                    batch = BATCH_SERVICE.createBatch(new BatchBuilder(product).batchName(batchName));
                }
                // Fijamos la cantidad que tiene la orden según lo que había en la receta
                // (esto es para evitar que PS recalcule las cantidades de los items)
                //
                // *****************************
                // LINEAS COMENTADAS AR y RM:
                // MeasuredValue batchQuantity = (MeasuredValue) recipe.getUDA("X_plannedQuantity");
                // RM 18/04/2023 : Se añade la casuistica que cuando la orden es de packaging la quantity se pilla de la orden y cuando es W&D de la receta
                if (builder.getOrderType().equalsIgnoreCase("ZPCK")) {
                MeasuredValue batchQuantity = (MeasuredValue) builder.getQuantity();
                poi.setQuantity(batchQuantity);
                }
                else
                {
                	MeasuredValue batchQuantity = (MeasuredValue) recipe.getUDA("X_plannedQuantity");
                    poi.setQuantity(batchQuantity);                	
                }
                // MESNamedUDAProcessOrderItem.setActualQuantity(poi, builder.getQuantity());// AR y RM
                // poi.setQuantity(builder.getQuantity());// AR y RM: Comentado el dia 11/ENE/2023
                MESNamedUDAProcessOrderItem.setBatch(poi, batch);
                
                processOrder.Save(null, null, PCContext.getDefaultAccessPrivilege());

                // LOGGER.info("Exploding order " + orderNumber);
                // ORDER_EXPLOSION_SERVICE.setOrderBuilder(builder);
                // ORDER_EXPLOSION_SERVICE.executeExplosion(poi);

                // Check Materials
                // checkComponentsForProcessOrderItem(poi, builder.getComponents());

                // Create batch allocations
                // createAllocatedBatchesForProcessOrderItem(poi, builder.getComponents());

                // Release the order
                // LOGGER.info("Releasing order " + orderNumber);
                // ORDER_SERVICE.applyOrderStatusTransitionExc(ProcessOrderItemFSMConstants.FSM_TRANS_RELEASE, poi,
                // false);
            }
        }
    }

    public void explodeProcessOrder(ErpOrderBuilder builder) throws DatasweepException, MESException {

        String orderNumber = builder.getOrderNumber();

        ProcessOrder processOrder = PCContext.getFunctions().getProcessOrderByName(orderNumber);
        processOrder = ORDER_SERVICE.loadOrder(orderNumber);

        ProcessOrderItem poi = (ProcessOrderItem) processOrder.getProcessOrderItems().get(0);

        LOGGER.info("Exploding order " + orderNumber);
        ORDER_EXPLOSION_SERVICE.setOrderBuilder(builder);
        ORDER_EXPLOSION_SERVICE.executeExplosion(poi); // During the Explode of the order, the DummyBom material
                                                       // subtitution is made

        // Check Materials
        checkComponentsForProcessOrderItem(poi, builder.getComponents());

        // Create batch allocations
        createAllocatedBatchesForProcessOrderItem(poi, builder.getComponents());

        // Cancel OSI if Qty is 0
        cancelOSIComponentsForProcessOrderItem(poi, builder.getComponents());

        // AFH Crear nueva para cancelar materiales dummy de packaging
         //Release the order
         //LOGGER.info("Releasing order " + orderNumber);
         //ORDER_SERVICE.applyOrderStatusTransition(ProcessOrderItemFSMConstants.FSM_TRANS_RELEASE, poi);
         
    }

    @Override
    @Transactional
    public void createERPSublot(String batchName, String sublot, String quantity, UnitOfMeasure uom,
            Location storageLocation, Part material) throws DatasweepException, MESException {

        // CRA:BATCH
        // Batch batch = batchSrv.loadBatch(batchName);
        Batch batch = BATCH_SERVICE.loadBatchByCompoundIdentifier(batchName, material.getPartNumber());

        if (batch != null) {
            TransactionHistoryContext thCtx = new TransactionHistoryContext();
            thCtx.setRemark(TRANS_HIST_COMMENT);

            MeasuredValue quantityMV = MeasuredValueUtilities.createMV(new BigDecimal(quantity), uom);

            @SuppressWarnings("unused")
            Sublot newSublot = SUBLOT_SERVICE.createSublot(batch, sublot, quantityMV, storageLocation, thCtx);
        } else {
            String errMsg = String.format("Batch '%s' / Material '%s' hasn't been found!", batchName, material);
            LOGGER.error(errMsg);
            throw new MESException(errMsg);
        }

    }

    @Override
    @Transactional
    public void createERPSublots(List<ErpSublotWrapper> sublots) throws DatasweepException, MESException {
        // CRA:BATCH
        // Batch batch = batchSrv.loadBatch(batchName);

        if (sublots == null || sublots.isEmpty()) {
            String errMsg = "Sublots list is empty! Unable to create any sublot.";
            LOGGER.error(errMsg);
            throw new MESException(errMsg);
        } else {

            LOGGER.info("Sublots creation begins!");

            int counter = 1;
            for (ErpSublotWrapper wrapper : sublots) {
                Batch batch = BATCH_SERVICE.loadBatchByCompoundIdentifier(wrapper.getBatchName(), wrapper.getMaterial().getPartNumber());

                LOGGER.info(String.format("Sublots creation: processing %d/%d for Batch '%s' / Material '%s'", counter++, sublots.size(),
                        wrapper.getBatchName(), wrapper.getMaterial()));

                if (batch != null) {
                    TransactionHistoryContext thCtx = new TransactionHistoryContext();
                    thCtx.setRemark(TRANS_HIST_COMMENT);

                    MeasuredValue quantityMV = MeasuredValueUtilities.createMV(new BigDecimal(wrapper.getQuantity()), wrapper.getUom());

                    Sublot sublot = SUBLOT_SERVICE.loadSublot(batch.getName(), wrapper.getSublot());
                    if (sublot != null) {
                        SUBLOT_SERVICE.changeSublotQuantity(sublot, quantityMV, thCtx);
                        LOGGER.info(String.format("UPDATE Qty on Sublot: %s", sublot));
                    } else {
                        Sublot newSublot = SUBLOT_SERVICE.createSublot(batch, wrapper.getSublot(), quantityMV, wrapper.getStorageLocation(), thCtx);
                        LOGGER.info(String.format("CREATE Sublot: %s", newSublot));
                    }
                    


                } else {
                    String errMsg = String.format("Batch '%s' / Material '%s' hasn't been found!", wrapper.getBatchName(), wrapper.getMaterial());
                    LOGGER.error(errMsg);
                    throw new MESException(errMsg);
                }
            }

            LOGGER.info("Sublots creation ends!");
        }

    }

    @Override
    @Transactional
    public void createERPEquipment(String name, String description, String status) throws DatasweepException, MESException {
        IMESS88Equipment equipment = S88_EQUIPMENT_SERVICE.loadEquipmentByIdentifier(name);

        if (equipment != null) {
            LOGGER.info(String.format("UPDATE Equipment: %s", equipment));
        } else {
            equipment = S88_EQUIPMENT_SERVICE.createEquipment(name);
            LOGGER.info(String.format("CREATE Equipment: %s", name));
        }
        equipment.setDescription(description);
        equipment.setInventoryNumber(name);
    }

    @Override
    @Transactional
    public void createERPEquipments(List<ErpEquipmentWrapper> equipments) throws DatasweepException, MESException {

        if (equipments == null || equipments.isEmpty()) {
            String errMsg = "Equipments list is empty! Unable to create any equipment.";
            LOGGER.error(errMsg);
            throw new MESException(errMsg);
        } else {

            LOGGER.info("Equipments creation begins!");
            IMESS88EquipmentClass equipmentClass = S88_EQUIPMENT_SERVICE.loadEquipmentClassByIdentifier(DEFAULT_EQUIPMENT_CLASS);
            IMESS88StatusGraphAssignment equipmentStatusGraphAssignament = equipmentClass.getStatusGraphAssignment(EQUIPMENT_GRAPH_IDENTIFIER);
            IMESS88StatusGraph equipmentStatusGraph =
                    (equipmentStatusGraphAssignament != null) ? equipmentStatusGraphAssignament.getStatusGraph() : null;

            int counter = 1;
            for (ErpEquipmentWrapper wrapper : equipments) {

                LOGGER.info(String.format("Equipments creation: processing %d/%d for Equipment '%s' / Description '%s' / Status '%s'", counter++,
                        equipments.size(), wrapper.getName(), wrapper.getDescription(), wrapper.getStatus()));

                String statusIdentifier = statusStringIdentifier(wrapper.getStatus());

                IMESS88Equipment equipment = S88_EQUIPMENT_SERVICE.loadEquipmentByIdentifier(wrapper.getName());

                if (equipment != null) {
                    LOGGER.info(String.format("UPDATE Equipment: %s", equipment));
                } else {
                    equipment = S88_EQUIPMENT_SERVICE.createEquipment(wrapper.getName());
                    equipmentClass.addEquipment(equipment);
                    equipment.addStatusGraph(equipmentStatusGraph, statusIdentifier);
                    LOGGER.info(String.format("CREATE Equipment: %s (class: %s)", equipment, equipmentClass));
                }
                equipment.setShortDescription(wrapper.getDescription());
                equipment.setDescription(wrapper.getDescription());
                equipment.setInventoryNumber(wrapper.getName());

                // Save changes
                equipment.Save(null, null, PCContext.getDefaultAccessPrivilege());

                // Fire status change
                IMESS88StatusGraphAssignment currentGraphAvailability = equipment.getStatusGraphAssignment(EQUIPMENT_GRAPH_IDENTIFIER);
                if (currentGraphAvailability != null) {
                    String currentStatus = currentGraphAvailability.getCurrentState().getIdentifier();
                    String triggerName = (EQUIPMENT_GRAPH_AVAILABLE_IDENTIFIER.equals(statusIdentifier)) ? EQUIPMENT_TRIGGER_TO_BE_AVAILABLE
                            : EQUIPMENT_TRIGGER_TO_BE_UNAVAILABLE;
                    IS88StatusGraphFireTriggerResult result;
                    if (!statusIdentifier.equals(currentStatus))
                    {
                        result = S88_EQUIPMENT_EXECUTION_SERVICE.fireGraphTrigger(equipment, EQUIPMENT_GRAPH_IDENTIFIER, triggerName, null);
                        if (result.wasTransitionExecuted())
                            LOGGER.info(String.format("Changed Equipment: %s Status [from: %s, to: %s]", wrapper.getName(), currentStatus,
                                    statusIdentifier));
                        else {
                            String errMsg = String.format("Trigger '%s' for Equipment '%s' fails!. Error: %s", triggerName, wrapper.getName(),
                                    result.getLocalizedErrorMessageWithContext());
                            LOGGER.error(errMsg);
                            throw new MESException(errMsg);
                        }
                    }
                } else {
                    String errMsg = String.format("Graph '%s' for Equipment '%s' hasn't been found!", EQUIPMENT_GRAPH_IDENTIFIER, wrapper.getName());
                    LOGGER.error(errMsg);
                    throw new MESException(errMsg);
                }
            }

            LOGGER.info("Equipments creation ends!");
        }

    }

    String statusStringIdentifier(String s) {
        String statusString = "";

        switch (s) {
        case "0":
            statusString = EQUIPMENT_GRAPH_NOT_AVAILABLE_IDENTIFIER;
            break;
        case "1":
            statusString = EQUIPMENT_GRAPH_AVAILABLE_IDENTIFIER;
            break;
        }

        return statusString;
    }

    /**
     * Set the UDA values of the Map to a named UDA Object.
     *
     * @param object  the object to be changed
     * @param theUdas Map of values for UDAs Key is name of UDA
     * @throws DatasweepException thrown when an error occurs
     */
    public static void setUdasOnObject(INamedUDA object, Map<String, Object> theUdas) throws DatasweepException {

        for (Map.Entry<String, Object> entry : theUdas.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if ((key.startsWith(DEFAULT_AT_COLUMN_PREFIX) || key.startsWith(getCustomUdaPrefix()))
                    && !ObjectUtils.equals(object.getUDA(key), value)) {
                object.setUDA(value, key);
            }
        }
    }

    /**
     * Gets the custom UDA prefix defined in the application configuration
     *
     * @return The custom UDA prefix
     */
    private static String getCustomUdaPrefix() {
        return MESConfiguration.getMESConfiguration().getString("eig_CustomATDefinitionPrefix", "ct_",
                "Prefix used for custom (i.e. customer specific) AT columns");
    }

    /**
     * Get the ERP Bom Item of Bom header for the position number
     *
     * @param erpBomHeader The Bom Header
     * @param posNr        The position number
     * @return List<IMESERPBomItem> Bom items for position
     * @throws DatasweepException Thrown when error occcurs
     */
    private List<IMESERPBomItem> getErpBomItems(IMESERPBomHeader erpBomHeader, String posNr) throws DatasweepException {

        MESERPBomItemFilter filter = new MESERPBomItemFilter();

        filter.forErpBomHeaderEqualTo(erpBomHeader);
        if (!StringUtils.isBlank(posNr)) {
            filter.forPositionEqualTo(posNr);
        }
        filter.exec();

        return filter.getFilteredObjects();
    }

    /**
     * Starts a messaging activity if necessary
     */
    private void startMessagingActivity() {
        MessagingActivity ma;
        try {
            MessagingActivity.getInstance();
            LOGGER.debug("Messaging activity already started");
        } catch (JMSException e) {
            LOGGER.debug("Starting messaging activity");
            ma = new MessagingActivity();
            ma.startup();
        }
    }

    /**
     * Check Materials
     *
     * @param poi The process order item object
     * @param orderItems The order items from ERP
     * @throws DatasweepException Thrown when error occurs
     */
    private void checkComponentsForProcessOrderItem(ProcessOrderItem poi, List<ErpOrderItemBuilder> orderItems) throws DatasweepException {
        ControlRecipe controlRecipe = OrderUtils.getControlRecipe(poi);
        List<OrderStep> orderSteps = controlRecipe.getOrderSteps();
        String MfcPositionArray[] = null;
        String MfcPosition = null;
        for (OrderStep os : orderSteps) {
            List<OrderStepInput> orderStepInputs = os.getOrderStepInputItems();
            for (OrderStepInput osi : orderStepInputs) {
                for (ErpOrderItemBuilder item : orderItems) {
                    // RM 09/03/2023: Ojo con esta parte de codigo,añadido porque por algun motivo la UDA X_position
                    // me devuelve un null cuando en la receta si que existe posición.
                    if (osi.getUDA("X_position") == null) {
                        LOGGER.info(String.format("That item: " + item.getPart() + "don't have UDA_X_position"));
                        // RM 09/03/2023: Cuando la UDA X_position viene vacia le pongo el valor de la posicion del
                        osi.setUDA((Object) item.getPosition(), "X_position");
                        
                    }
                    if (osi.getUDA("X_position").toString().contains("_"))
                    {
                        MfcPositionArray = osi.getUDA("X_position").toString().split("_", 2);
                        MfcPosition = MfcPositionArray[0];
                        if (item.getPosition().equals(MfcPosition) && item.getPart() != null && item.getPart().getPartNumber() != null
                                && item.getPart().getPartNumber().equals(osi.getPart().getPartNumber())) {
                            LOGGER.info(String.format("Allocating '%s' on material '%s' position '%s' for order '%s'", item.getUdaMap(),
                                    item.getPart(), MESNamedUDAOrderStepInput.getPosition(osi), poi.getOrderName()));
                            // MESNamedUDAOrderStepInput.setPosition(osi, item.getPosition());
                            setUdasOnObject(osi, item.getUdaMap());
                        }
                    } else if (item.getPosition().equals(osi.getUDA("X_position"))) {
                        if (item.getPart() != null && item.getPart().getPartNumber() != null
                                && item.getPart().getPartNumber().equals(osi.getPart().getPartNumber())) {
                            LOGGER.info(String.format("Allocating '%s' on material '%s' position '%s' for order '%s'", item.getUdaMap(),
                                    item.getPart(), MESNamedUDAOrderStepInput.getPosition(osi), poi.getOrderName()));
                            // MESNamedUDAOrderStepInput.setPosition(osi, item.getPosition());
                            setUdasOnObject(osi, item.getUdaMap());
                        }
                    }
                }
            }
            os.Save(null, null, PCContext.getDefaultAccessPrivilege());
        }
    }

    /**
     * Generate batch allocations based on download information
     *
     * @param poi        The process order item object
     * @param orderItems The order items from ERP
     * @throws DatasweepException Thrown when error occurs
     */
    private void createAllocatedBatchesForProcessOrderItem(ProcessOrderItem poi, List<ErpOrderItemBuilder> orderItems)
            throws DatasweepException {
        ControlRecipe controlRecipe = OrderUtils.getControlRecipe(poi);
        List<OrderStep> orderSteps = controlRecipe.getOrderSteps();
        String MfcPositionArray[] = null;
        String MfcPosition = null;
        for (OrderStep os : orderSteps) {
            List<OrderStepInput> orderStepInputs = os.getOrderStepInputItems();
            for (OrderStepInput osi : orderStepInputs) {
                for (ErpOrderItemBuilder item : orderItems) {
                    if (osi.getUDA("X_position").toString().contains("_")) {
                        MfcPositionArray = osi.getUDA("X_position").toString().split("_", 2);
                        MfcPosition = MfcPositionArray[0];
                    if (StringUtils.isNotBlank(item.getPosition())
                            // && item.getPosition().equals(MESNamedUDAOrderStepInput.getPosition(osi))
                                && item.getPosition().equals(MfcPosition)
                            && item.getPart().getName().equals(osi.getPart().getName())
                            && (item.getBatch() != null)) {
                        LOGGER.info("Allocating batch " + item.getBatch().getName() + " on "
                                + MESNamedUDAOrderStepInput.getNumber(osi) + " for order " + poi.getOrderName());
                        MESBatchAllocation batchAllocation = new MESBatchAllocation();
                        batchAllocation.setBatchKey(Long.valueOf(item.getBatch().getKey()));
                        batchAllocation.setOrderStepInputKey(Long.valueOf(osi.getKey()));
                        batchAllocation.setOrderStep(os);
                        batchAllocation.Save(null, null, PCContext.getDefaultAccessPrivilege());
                    }
                }
                else if (item.getPosition().equals(osi.getUDA("X_position")) && item.getPart().getName().equals(osi.getPart().getName())
                        && (item.getBatch() != null)) {
                    LOGGER.info("Allocating batch " + item.getBatch().getName() + " on " + MESNamedUDAOrderStepInput.getNumber(osi) + " for order "
                            + poi.getOrderName());
                    MESBatchAllocation batchAllocation = new MESBatchAllocation();
                    batchAllocation.setBatchKey(Long.valueOf(item.getBatch().getKey()));
                    batchAllocation.setOrderStepInputKey(Long.valueOf(osi.getKey()));
                    batchAllocation.setOrderStep(os);
                    batchAllocation.Save(null, null, PCContext.getDefaultAccessPrivilege());
                }
            }
            }
        }
    }

    /**
     * Cancel OSI Components if the Loipro Qty is Zero
     *
     * @param poi The process order item object
     * @param orderItems The order items from ERP
     * @throws DatasweepException Thrown when error occurs
     */
    private void cancelOSIComponentsForProcessOrderItem(ProcessOrderItem poi, List<ErpOrderItemBuilder> orderItems) throws DatasweepException {
        ControlRecipe controlRecipe = OrderUtils.getControlRecipe(poi);
        List<OrderStep> orderSteps = controlRecipe.getOrderSteps();
        Boolean RepeatedNodeFounded = false;

        // Por cada uno de los Item que bajan del Loipro
        for (ErpOrderItemBuilder item : orderItems) {

            // Los materiales que sean Zero, tengo que cancelarlos
            Double quantity = item.getQuantity().getValue().doubleValue();
            String warehouse = (String) item.getUdaMap().get("ct_WarehouseNumber");
            RepeatedNodeFounded = false;

            // AFH: Se añade código para que solo anule los componentes que tengan el campo LGNUM del LOIPRO en blanco
            if (warehouse.isEmpty() && (quantity == 0.0 || quantity == 0)) {

                // Antes de recorrer la receta para cancelar ese material que ha bajado a Zero
                // recorro de nuevo los items que han bajado en el Loipro por si hay otro nodo
                // de ese mismo material que sí tenga cantidad y Lote asignado (cuando hay lote asignado, envian dos
                // nodos: uno con cantidad 0 y otro con cantidad + lote asignado. En ese caso no hay que cancelar nada)
                for (ErpOrderItemBuilder itemRepeated : orderItems) {

                    // Solo lo cancelo ese Material si NO encuentro otro nodo con ese mismo material, cuya cantidad
                    // no sea 0 y tenga lote asignado
                    if (item.getPart().getPartNumber().equals(itemRepeated.getPart().getPartNumber()) && itemRepeated.getBatch() != null) {
                        Double quantityRepeated = itemRepeated.getQuantity().getValue().doubleValue();
                        if ((quantityRepeated != 0.0) && (quantityRepeated != 0)) {
                            RepeatedNodeFounded = true;
                        }
                    }
                }

                if (RepeatedNodeFounded != true) {

                    // Recorro las UPs de la receta
                    for (OrderStep os : orderSteps) {
                        List<OrderStepInput> orderStepInputs = os.getOrderStepInputItems();
                        // Recorro los OSI de esa UP
                        for (OrderStepInput osi : orderStepInputs) {
                            // Cancelo todos los OSI que coincidan con el Item que ha bajado con cantidad Zero
                            // RM 01/03/2023: Hay que poner que la posicion coincida tambien
                            if (item.getPosition().equals(osi.getUDA("X_position"))
                                    && item.getPart().getPartNumber().equals(osi.getPart().getPartNumber())) {

                                // Marcamos el OSI como cancelado
                                MESNamedUDAOrderStepInput.setStatus(osi, EnumOrderStepInputStatus.OSI_STATUS_CANCELLED);

                                // Ponemos la cantidad del OSI a Zero
                                MeasuredValue qty = osi.getPlannedQuantity();
                                osi.setPlannedQuantity(
                                        PCContext.getFunctions().createMeasuredValue(BigDecimal.ZERO, qty.getSymbol(), qty.getScale()));

                                // Cambiamos el PlannedQuantityMode a None
                                IMESChoiceElement PlannedQuantityModeNone = MESChoiceListHelper.getChoiceElement("PlannedQuantityMode", "None");
                                MESNamedUDAOrderStepInput.setPlannedQuantityMode(osi, PlannedQuantityModeNone);

                                os.Save(null, null, PCContext.getDefaultAccessPrivilege());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Remove the unreleased process order
     *
     * @param poi The ProcessOrderItem
     * @throws DatasweepException Thrown when error occurs
     * @throws MESException       Thrown when error occurs
     */
    private void removeUnreleasedOrder(ErpOrderBuilder builder) throws DatasweepException, MESException {
        String orderNumber = builder.getOrderNumber();

        processOrder = PCContext.getFunctions().getProcessOrderByName(orderNumber);

        if (processOrder == null) {
            processOrder = PCContext.getFunctions().getProcessOrderByName(orderNumber);
        }
        ProcessOrderItem poi = (ProcessOrderItem) processOrder.getProcessOrderItems().get(0);

        String poiStatus = poi.getCurrentState("orderStatus").getState().getName();
        if ("Defined".equals(poiStatus)) {

            removeAllocatedBatchesForProcessOrderItem(poi);
            // Reset the process order.
            ORDER_SERVICE.detachMasterRecipe(poi);

            LOGGER.info("Removing order " + poi.getOrderName());
            processOrder.removeProcessOrderItem(poi, PCContext.getDefaultAccessPrivilege());
            processOrder.remove(null, null, PCContext.getDefaultAccessPrivilege());
        }
    }

    /**
     * Remove batch allocations for a given ProcessOrderItem
     *
     * @param poi The process order item object
     */
    private void removeAllocatedBatchesForProcessOrderItem(ProcessOrderItem poi) {
        LOGGER.info("Removing batch allocations for order " + poi.getOrderName());
        ControlRecipe controlRecipe = OrderUtils.getControlRecipe(poi);
        IMatMgmtSupportService imatService = ServiceFactory.getService(IMatMgmtSupportService.class);
        List<OrderStep> orderSteps = controlRecipe.getOrderSteps();
        for (OrderStep os : orderSteps) {
            List<OrderStepInput> orderStepInputs = os.getOrderStepInputItems();
            for (OrderStepInput osi : orderStepInputs) {
                imatService.deallocateAllBatchesForOrderStepInput(osi);
            }

        }
    }

}
