package com.rockwell.mes.myeig.activities;

import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.client.DatasweepException;
import com.rockwell.integration.messaging.BasePayload;
import com.rockwell.mes.commons.base.ifc.exceptions.MESException;
import com.rockwell.mes.commons.base.ifc.services.ServiceFactory;
import com.rockwell.mes.myeig.data.ConsumptionTransferObject;
import com.rockwell.mes.myeig.data.ConsumptionTransferObject.ConsumedItem;
import com.rockwell.mes.myeig.service.ifc.IOutboundMessageService;

/**
 * This class implements functionality in order to process outgoing material
 * consumption messages to SAP
 * 
 * @author syim, (c) Copyright 2012 Rockwell Automation Solutions, Inc. All
 *         Rights Reserved.
 */
public class ConsumptionTransferOutboundAction extends AbstractOutboundActivity {

    /** logger */
    private static final Log LOGGER = LogFactory.getLog(ConsumptionTransferOutboundAction.class);

    @Override
    public BasePayload processPayloadData(final String eventId) throws DatasweepException, MESException {
        StringBuilder separador = new StringBuilder(1);
        separador.append(";");
        Integer target1 = null;
        Integer target2 = null;
        String numPosBatch = null;
        String numResBatch = null;
        String batchPosition[] = null;
        String batchNumber[] = null;

        logInfo(LOGGER, "Generating material consumption outbound message");
        final ConsumptionTransferObject payload = (ConsumptionTransferObject) ServiceFactory.getService(
                IOutboundMessageService.class).getPayload(Long.valueOf(eventId), IOutboundMessageService.PCA_EVENTS);

        for (ConsumedItem consumedItem : payload.getConsumedItems()) {
            String orderNo = consumedItem.getOrder();
            String materialNo = consumedItem.getMaterial();
            String quantity = consumedItem.getQuantity();
            String uom = consumedItem.getUom();
            String batch = consumedItem.getBatch();
            String reservationPos = consumedItem.getReservationItemNo();//RSPOS
            String reservationNum = consumedItem.getReservationNo();//RSNUM
            
            // RM: 13/03/2023 Definir separador
            if (reservationPos !=null) {
            StringTokenizer reservationPosTokens = new StringTokenizer(reservationPos, separador.toString());
            Integer numeroSeparadoresPos = reservationPosTokens.countTokens();
            batchPosition = reservationPos.split(";", -numeroSeparadoresPos);
            }
            if (reservationNum != null) {
            StringTokenizer reservationNumTokens = new StringTokenizer(reservationNum, separador.toString());
            Integer numeroSeparadoresNum = reservationNumTokens.countTokens();
            batchNumber = reservationNum.split(";", -numeroSeparadoresNum);
            }
            else {
            	reservationPos = "";
            	reservationNum = "";
            }          
                        
            // RM: 13/03/2023 Separar el String para obtener reservationPosition y reservationNumber
            
            
            
            // RM: 13/03/2023 Recorrer el Array en busca del batch que se haya usado.
            if (batchPosition != null) {
                for (Integer i = 0; i < batchPosition.length; i++) {
                    String positionArray = batchPosition[i];
                    if (positionArray.contains(batch)) {
                    target1 = positionArray.indexOf("_");
                    target2 = positionArray.length();
                    numPosBatch = positionArray.substring(target1 + 1, target2);
                    consumedItem.setReservationItemNo(numPosBatch);
                }
            }
        }
        if (batchNumber != null) {
                for (Integer i = 0; i < batchNumber.length; i++) {
                    String numberArray = batchNumber[i];
                    if (numberArray.contains(batch)) {
                    target1 = numberArray.indexOf("_");
                    target2 = numberArray.length();
                    numResBatch = numberArray.substring(target1 + 1, target2);
                    consumedItem.setReservationNo(numResBatch);
                   
                }
            }
        }


            // consumedItem.setReservationItemNo();
            logInfo(LOGGER, "Customer consumedItem: " + quantity + " " + uom + " of material " + materialNo + " consumed in order " + orderNo);
        }

        return payload;
    }
}