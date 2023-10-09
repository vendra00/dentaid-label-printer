package com.rockwell.mes.myeig.activities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.client.DatasweepException;
import com.rockwell.integration.messaging.BasePayload;
import com.rockwell.mes.commons.base.ifc.exceptions.MESException;
import com.rockwell.mes.commons.base.ifc.services.ServiceFactory;
import com.rockwell.mes.myeig.data.RelocationMaterialTransferObject;
import com.rockwell.mes.myeig.service.ifc.IOutboundMessageService;

public class RelocationMaterialTransferOutboundAction extends AbstractOutboundActivity {

    /** logger */
    private static final Log LOGGER = LogFactory.getLog(RelocationMaterialTransferOutboundAction.class);
    @Override
    public BasePayload processPayloadData(final String eventId) throws DatasweepException, MESException {

        logInfo(LOGGER, "Generating relocation material outbound message");
        final RelocationMaterialTransferObject payload = (RelocationMaterialTransferObject) ServiceFactory
                .getService(IOutboundMessageService.class)
                .getPayload(Long.valueOf(eventId), IOutboundMessageService.PCA_EVENTS);
        String documentDate = payload.getDocumentDate();
        int materials = payload.getMaterials().size();
        logInfo(LOGGER, "Number of Materials asked to sap" + materials + " on date" + documentDate);

        return payload;
    }
}
