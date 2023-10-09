package com.rockwell.mes.myeig.activities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.client.DatasweepException;
import com.rockwell.integration.messaging.BasePayload;
import com.rockwell.mes.commons.base.ifc.exceptions.MESException;
import com.rockwell.mes.commons.base.ifc.services.ServiceFactory;
import com.rockwell.mes.myeig.data.ProductionTransferObject;
import com.rockwell.mes.myeig.data.ProductionTransferObject.ProducedItem;
import com.rockwell.mes.myeig.service.ifc.IOutboundMessageService;

/**
 * This class implements functionality in order to process outgoing material
 * production messages to SAP
 * 
 * @author syim, (c) Copyright 2012 Rockwell Automation Solutions, Inc. All
 *         Rights Reserved.
 */
public class ProductionTransferOutboundAction extends AbstractOutboundActivity {

    /** logger */
    private static final Log LOGGER = LogFactory.getLog(ProductionTransferOutboundAction.class);

    // @Override
    // public BasePayload processPayloadData(final String eventId) throws DatasweepException, MESException {
    //
    // logInfo(LOGGER, "Generating material production outbound message");
    // final ProductionTransferObject payload = (ProductionTransferObject) ServiceFactory.getService(
    // IOutboundMessageService.class).getPayload(Long.valueOf(eventId), IOutboundMessageService.PCA_EVENTS);
    // String orderNo = payload.getOrder();
    // String materialNo = payload.getMaterial();
    // String quantity = payload.getQuantity();
    // String uom = payload.getUom();
    // logInfo(LOGGER, quantity + " " + uom + " of material " + materialNo + " produced in order " + orderNo);
    //
    // return payload;
    // }

    // Customer CUSTOMIZATION
    @Override
    public BasePayload processPayloadData(final String eventId) throws DatasweepException, MESException {

        logInfo(LOGGER, "Generating material production outbound message");
        final ProductionTransferObject payload = (ProductionTransferObject) ServiceFactory.getService(
                IOutboundMessageService.class).getPayload(Long.valueOf(eventId), IOutboundMessageService.PCA_EVENTS);
        String orderNo = payload.getOrder();
        int numOfProducedItems = (payload != null && payload.getProducedItems() != null) ? payload.getProducedItems().size() : 0;
        logInfo(LOGGER, "Order '" + orderNo + "' has produced " + numOfProducedItems + " items.");
        if (numOfProducedItems > 0) {
            for (ProducedItem item : payload.getProducedItems()) {
                logInfo(LOGGER, item.getQuantity() + " " + item.getUom() + " of material " + item.getMaterial() + " produced in order " + orderNo);
            }
        }
        
        return payload;
    }
}
