package com.rockwell.mes.myeig.activities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.client.DatasweepException;
import com.rockwell.integration.messaging.BasePayload;
import com.rockwell.mes.commons.base.ifc.exceptions.MESException;
import com.rockwell.mes.commons.base.ifc.services.ServiceFactory;
import com.rockwell.mes.myeig.data.OrderStatusTransferObject;
import com.rockwell.mes.myeig.data.OrderStatusTransferObject.E1ZProstat;
import com.rockwell.mes.myeig.service.ifc.IOutboundMessageService;

/**
 * This class implements functionality in order to process outgoing order status
 * messages to SAP
 * 
 * @author syim, (c) Copyright 2012 Rockwell Automation Solutions, Inc. All
 *         Rights Reserved.
 */
public class OrderStatusTransferOutboundAction extends AbstractOutboundActivity {

    /** logger */
    private static final Log LOGGER = LogFactory.getLog(OrderStatusTransferOutboundAction.class);

    @Override
    public BasePayload processPayloadData(final String eventId) throws DatasweepException, MESException {

        logInfo(LOGGER, "Generating order status outbound message");
        // AFH 20/02/2023 Stat interface
        final OrderStatusTransferObject payload = (OrderStatusTransferObject) ServiceFactory.getService(
                IOutboundMessageService.class).getPayload(Long.valueOf(eventId), IOutboundMessageService.PCA_EVENTS);
        /*        String orderNo = payload.getOrderNo();
        String operation = payload.getErpOperationNo();
        String recipe = payload.getControlRecipeId();
        String status = payload.getErpStatus(); */
        // payload.getE1Zprostat();
        E1ZProstat ez1 = payload.getE1Zprostat();
        if (ez1 != null) {
            String insplot = ez1.getInsplot();
            String statFinished = ez1.getStatFinished();
            String statProdReviewed = ez1.getStatProdReviewed();
            String statQaReviewed = ez1.getStatQaReviewed();
        logInfo(LOGGER, "Inpection lot: " + insplot + " Stat Finished: " + statFinished + " Stat Prod Reviewed: " + statProdReviewed
                    + " Stat Qa Reviewed: " + statQaReviewed);
        } else {
            logInfo(LOGGER, "Fallo en objeto intermedio vacio E1Zprostat");
        }
        return payload;
    }
}
