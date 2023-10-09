package com.rockwell.mes.myeig.activities;

import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.client.Batch;
import com.datasweep.compatibility.client.DatasweepException;
import com.datasweep.compatibility.client.Part;
import com.datasweep.plantops.common.constants.IObjectTypes;
import com.rockwell.integration.messaging.MessageEnvelope;
import com.rockwell.mes.commons.base.ifc.exceptions.MESException;
import com.rockwell.mes.commons.base.ifc.services.ServiceFactory;
import com.rockwell.mes.myeig.data.BatchStatusTransferObject;
import com.rockwell.mes.myeig.data.BatchStatusTransferObject.BatchMove;
import com.rockwell.mes.myeig.service.ifc.IInboundMessageService;
import com.rockwell.mes.myeig.utility.CustomFunctionHelper;
import com.rockwell.mes.services.inventory.ifc.AbstractBatchQualityTransitionEventListener;
import com.rockwell.mes.services.inventory.ifc.BatchBuilder;
import com.rockwell.mes.services.inventory.ifc.IBatchService;

/**
 * This class implements functionality to process incoming batch messages
 * <p>
 * 
 * @author syim, (c) Copyright 2012 Rockwell Automation Technologies, Inc. All
 *         Rights Reserved.
 */
public class BatchStatusTransferObjectInboundAction extends AbstractInboundActivity {

    /** logger */
    private static final Log LOGGER = LogFactory.getLog(BatchStatusTransferObjectInboundAction.class);

    /** Constants */
    // private static final String TRASLADO_EN_CENTRO = "311"; // Traslado en centro
    private static final String LIBERAR_PRODUCTO_DE_CALIDAD = "321"; // Liberar producto de calidad
    private static final String PASAR_STOCK_EN_CONTROL_CALIDAD = "322"; // Pasar stock en control calidad
    // private static final String TRASLADO_EN_CONTROL_DE_CALIDAD = "323"; // Traslado en control de calidad
    // private static final String TRASLADO_EN_BLOQUEADO = "325"; // Traslado en bloqueado
    // private static final String SALIDA_STOCK_PARA_MUESTRAS = "333"; // Salida stock para muestras
    // private static final String SALIDA_STOCK_PARA_MUESTRAS_BLOQUEADO = "335"; // Salida stock para muestras bloqueado
    private static final String TRASLADO_DE_BLOQUEADO_A_LIBRE = "343"; // Traslado de bloqueado a libre
    private static final String TRASLADO_DE_LIBRE_A_BLOQUEADO = "344"; // Traslado de libre a bloqueado
    private static final String TRASLADO_DE_BLOQUEADO_A_CALIDAD = "349"; // Traslado de bloqueado a calidad
    private static final String TRASLADO_DE_CALIDAD_A_BLOQUEADO = "350"; // Traslado de calidad a bloqueado

    @Override
    public void processActivityData(final MessageEnvelope data) throws DatasweepException, MESException {

        BatchStatusTransferObject batchTO = (BatchStatusTransferObject) data.getPayload();
        setDocNum(batchTO.getIdoc()); // set the document no. for all logging
        logInfo(LOGGER, "Batch Status inbound integration activity start.");

        for (BatchMove batchmoveObject : batchTO.getBatchMoves()) {
    
            // 1. material number
            Part material = validate(true, batchmoveObject.getMaterial(), Part.class, "Material Number");
    
            // 2. batch number
            // CRA:BATCH
            // String batchName = validate(true, CustomFunctionHelper.buildBatchName(batchmoveObject.getBatch(),
            // batchmoveObject.getMaterial()), String.class, "Batch Name");
            String batchName = validate(true, batchmoveObject.getBatch(), String.class, "Batch Name");
            setObjectsProcessed("Batch Name: " + batchName); // for inbound event log details
    
            // 3. Calculate batch status
            // String transition = calculateStatusTransition(batchName, batchmoveObject.getMoveType());
            // SGO Check if null
            String moveType = validate(true, batchmoveObject.getMoveType(), String.class, "Move Type");
            String transition = calculateStatusTransition(batchName, moveType);
    
            final IBatchService batchSrv = ServiceFactory.getService(IBatchService.class);
            // CRA:BATCH
            // Batch batch = batchSrv.loadBatch(batchName);
            Batch batch = batchSrv.loadBatchByCompoundIdentifier(batchName, material.getPartNumber());
            if (batch == null) {
                addError(this.getClass().getName(), "Batch: " + batchName + " Material: " + material + " doesn't exists.\n");
            }

            if (getErrors() == null || getErrors().size() == 0) {
                BatchBuilder bb = new BatchBuilder(material).batchName(batchName);
                final IInboundMessageService ims = ServiceFactory.getService(IInboundMessageService.class);
                ims.updateERPBatchStatus(bb, transition);
            } else
                logError(LOGGER, MessageFormat.format(resources.getString("ct_eihub_error.generic_error"), new Object[] { batchTO.getIdoc() }));

        }

    }

    @Override
    public Long getObjectType() {
        return new Long(IObjectTypes.TYPE_BATCH);
    }

    // TODO: Make status
    /**
     * Calculate batch status transition
     * 
     * @param moveType The batch name
     * @return String The transition
     */
    private String calculateStatusTransition(String batchName, String moveType) {

        String transition = null;

        switch (moveType) {
        // case TRASLADO_EN_CENTRO:
        case LIBERAR_PRODUCTO_DE_CALIDAD:
            // case SALIDA_STOCK_PARA_MUESTRAS:
        case TRASLADO_DE_BLOQUEADO_A_LIBRE:
            transition = AbstractBatchQualityTransitionEventListener.FSM_TRANS_RELEASE;
            break;
        case PASAR_STOCK_EN_CONTROL_CALIDAD:
            // case TRASLADO_EN_CONTROL_DE_CALIDAD:
        case TRASLADO_DE_BLOQUEADO_A_CALIDAD:
            transition = AbstractBatchQualityTransitionEventListener.FSM_TRANS_QUARANTINE;
            break;
        // case TRASLADO_EN_BLOQUEADO:
        // case SALIDA_STOCK_PARA_MUESTRAS_BLOQUEADO:
        case TRASLADO_DE_LIBRE_A_BLOQUEADO:
        case TRASLADO_DE_CALIDAD_A_BLOQUEADO:
            transition = AbstractBatchQualityTransitionEventListener.FSM_TRANS_BLOCK;
            break;
        default:
            transition = AbstractBatchQualityTransitionEventListener.FSM_TRANS_QUARANTINE;
        }

        logInfo(LOGGER, "Batch: " + batchName + " determined transition = '" + transition + "'");

        return transition;
    }

}
