package com.rockwell.mes.myeig.activities;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.client.Batch;
import com.datasweep.compatibility.client.DatasweepException;
import com.datasweep.compatibility.client.MeasuredValue;
import com.datasweep.compatibility.client.Part;
import com.datasweep.compatibility.ui.Time;
import com.datasweep.plantops.common.constants.IObjectTypes;
import com.rockwell.integration.messaging.MessageEnvelope;
import com.rockwell.mes.commons.base.ifc.exceptions.MESException;
import com.rockwell.mes.commons.base.ifc.fsm.FSMHelper;
import com.rockwell.mes.commons.base.ifc.services.PCContext;
import com.rockwell.mes.commons.base.ifc.services.ServiceFactory;
import com.rockwell.mes.myeig.data.BatchTransferObject;
import com.rockwell.mes.myeig.data.BatchTransferObject.BatchAddtionalData;
import com.rockwell.mes.myeig.data.BatchTransferObject.BatchAttribute;
import com.rockwell.mes.myeig.data.BatchTransferObject.BatchStatus;
import com.rockwell.mes.myeig.data.BatchTransferObject.Material;
import com.rockwell.mes.myeig.data.MaterialTransferObject.MaterialText;
import com.rockwell.mes.myeig.service.ifc.IInboundMessageService;
import com.rockwell.mes.myeig.utility.IntegrationGatewayHelper;
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
public class BatchTransferObjectInboundAction extends AbstractInboundActivity {

    /** logger */
    private static final Log LOGGER = LogFactory.getLog(BatchTransferObjectInboundAction.class);

    private static final String SynthonPlantCode = "2100";

    @Override
    public void processActivityData(final MessageEnvelope data) throws DatasweepException, MESException {

        // RM:03/ENE/2023-- Declaro el fichero BATMAS para que pueda extraer los segmentos y asignarles clase.
        BatchTransferObject batchTO = (BatchTransferObject) data.getPayload();
        setDocNum(batchTO.getIdoc()); // set the document no. for all logging
        logInfo(LOGGER, "Batch inbound integration activity start.");

        Part material = null;
        String batchName = null;
        Time expiryDate = null;
        Time retestDate = null;
        Time productionDate = null;
        String VendorBatch = "";
        MeasuredValue potency = null;
        String BatchPlant = "";
        String transition = null;

        for (Material materialObject : batchTO.getMaterials()) {

            // 1. material number
            // RM: 03/ENE/2023-- Asigno a la variable material el valor que esta en el segmento E1BATMAS.MATNR
            material = validate(true, materialObject.getMaterialNumber(), Part.class, "Material Number");

            // 2. batch number
            // RM: 03/ENE/2023-- Asigno a la variable batchName el valor que esta en el segmento E1BATMAS.CHARGE
            batchName = validate(true, materialObject.getBatchNumber(), String.class, "Batch Name");
            setObjectsProcessed("Batch Name: " + batchName); // for inbound event log details
           
            for (BatchAttribute BatchAttribute : materialObject.getBatchAttributes()) {
                // 3. expiry date
                // RM: 03/ENE/2023-- Asigno a la variable expiryDate el valor que esta en el segmento
                // E1BATCHATT.EXPIRYDATE
                expiryDate = IntegrationGatewayHelper.fromSapString(BatchAttribute.getExpirationDate());

                // 4. retest date
                // RM: 03/ENE/2023-- Asigno a la variable retestDate el valor que esta en el segmento
                // E1BATCHATT.NEXTINSPEC
                retestDate = IntegrationGatewayHelper.fromSapString(BatchAttribute.getNextInspectionDate());

                // 5. Production date
                // RM: 03/ENE/2023-- Asigno a la variable productionDate el valor que esta en el segmento
                // E1BATCHATT.PROD_DATE
                productionDate = IntegrationGatewayHelper.fromSapString(BatchAttribute.getProductionDate());

                // 6. VendorBatch
                // RM: 03/ENE/2023-- Asigno a la variable VendorBatch el valor que esta en el segmento
                // E1BATCHATT.VENDORBATCH
                VendorBatch = BatchAttribute.getVendorBatch();

            }

            for (BatchAddtionalData BatchAddtionalData : materialObject.getBatchAdditionalData()) {
                // 7. Calculate potency
                // RM: 03/ENE/2023-- Se pone por defecto una potencia de 100, luego si existe un valor en el segmento
                // ZE1BATADDDATA.BQMPOTENCY, convierto ese valor en
                // decimal y se lo asigno a la variable strPotency.
                String strPotency = "100";
                if (!StringUtils.isEmpty(BatchAddtionalData.getPotency())) {
                    try {
                        BigDecimal potencyRaw = new BigDecimal(BatchAddtionalData.getPotency());
                        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
                        formatter.setMaximumFractionDigits(2);
                        formatter.setMinimumFractionDigits(1);
                        strPotency = formatter.format(potencyRaw);

                    } catch (Exception e) {
                        addError(this.getClass().getName(), "Exception converting the potency in a decimal number. Error: " + e.toString() + "\n");
                        LOGGER.error("Exception converting the potency in a decimal number. Error: ", e);
                    }
        
                }
                potency = IntegrationGatewayHelper.getMeasuredValue(strPotency + " %");

                // 8. Check Plant
                // RM: 03/ENE/2023-- Compruebo que la planta tiene el codigo de planta correcto, dependiendo de si es ES
                // (2100) o CL(2600)
                String Plant = validate(true, BatchAddtionalData.getPlant(), String.class, "Plant");
                if (Plant != null && !StringUtils.equals(Plant, SynthonPlantCode)) {
                    addError(this.getClass().getName(), "Plant code is incorrect.\n");
                }
                // RM: 03/ENE/2023-- Asigno a la variable expiryDate el valor que esta en el segmento
                // ZE1BATADDDATA.PLANT
                BatchPlant = BatchAddtionalData.getPlant();

            }

            for (BatchStatus BatchStatus : materialObject.getBatchStatus()) {
                // 9. Calculate batch status
                // RM: 03/ENE/2023-- Asigno a la variable BatchStatusVar el valor que esta en el segmento
                String BatchStatusVar = BatchStatus.getRestricted();
                // RM: 03/ENE/2023-- Dependieno de si el status es RELEASED o BLOCKED le asigno a la variable transition
                // dicho valor.
                if (StringUtils.equals("RELEASED", StringUtils.upperCase(BatchStatusVar))) {
                    transition = AbstractBatchQualityTransitionEventListener.FSM_TRANS_RELEASE;
                    logInfo(LOGGER, "Batch: " + batchName + " determined transition = '" + transition + "'");

                } else if (StringUtils.equals("BLOCKED", StringUtils.upperCase(BatchStatusVar))) {
                    transition = AbstractBatchQualityTransitionEventListener.FSM_TRANS_BLOCK;
                    logInfo(LOGGER, "Batch: " + batchName + " determined transition = '" + transition + "'");

                } else {
                    addError(this.getClass().getName(), "Batch status could not be determined\n");
                    LOGGER.error("Batch status could not be determined");
                }

            }

        }
        if (getErrors() == null || getErrors().size() == 0) {

            HashMap<String, Object> udas = new HashMap<String, Object>();
            udas.put("X_productionDate", productionDate);
            udas.put("X_retestDate", retestDate);
            udas.put("ct_Plant", BatchPlant);
            udas.put("ct_VendorBatch", VendorBatch);

            final IInboundMessageService ims = ServiceFactory.getService(IInboundMessageService.class);
            final IBatchService batchSrv = ServiceFactory.getService(IBatchService.class);

            // Compruebo si el Batch ya existe previamente a crearlo yo
            Batch batch = batchSrv.loadBatchByCompoundIdentifier(batchName, material.getPartNumber());

            // Tengo que llamar a la funcion CreateERPBatch aunque ya exista el Batch porque esta funcion actualiza la
            // Potencia del Batch, el ExpiricyDate y RetestDate
            BatchBuilder bb =
                    new BatchBuilder(material).batchName(batchName).expiryDate(expiryDate).retestDate(retestDate).potency(potency).udaMap(udas);
            ims.createERPBatch(bb, transition);

            // Si ya existia el Batch antes de crearlo, tengo que hacerle update del Status. Porque la funcion de
            // CreateERPBatch no le habra cambiado el Status
            // AFH: Se ha comentado este if para solucionar el fallo del batch update
            // if (batch != null) {
            // ims.updateERPBatchStatus(new BatchBuilder(material).batchName(batchName), transition);
            // }
        } else
            logError(LOGGER, MessageFormat.format(resources.getString("ct_eihub_error.generic_error"), new Object[] { batchTO.getIdoc() }));

    }

    @Override
    public Long getObjectType() {
        return new Long(IObjectTypes.TYPE_BATCH);
    }
}
