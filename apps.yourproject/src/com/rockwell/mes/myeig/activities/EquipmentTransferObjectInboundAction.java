package com.rockwell.mes.myeig.activities;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.client.DatasweepException;
import com.datasweep.compatibility.client.Location;
import com.datasweep.compatibility.client.Part;
import com.datasweep.compatibility.client.UnitOfMeasure;
import com.datasweep.plantops.common.constants.IObjectTypes;
import com.rockwell.integration.messaging.MessageEnvelope;
import com.rockwell.mes.commons.base.ifc.exceptions.MESException;
import com.rockwell.mes.commons.base.ifc.services.PCContext;
import com.rockwell.mes.commons.base.ifc.services.ServiceFactory;
import com.rockwell.mes.myeig.data.EquipmentTransferObject;
import com.rockwell.mes.myeig.data.EquipmentTransferObject.IDOC;
import com.rockwell.mes.myeig.service.ifc.ErpEquipmentWrapper;
import com.rockwell.mes.myeig.service.ifc.IInboundMessageService;

/**
 * This class implements functionality to process incoming equipments messages
 * 
 * @author tmedina (Customer customization)
 */
public class EquipmentTransferObjectInboundAction extends AbstractInboundActivity {

    /** logger */
    private static final Log LOGGER = LogFactory.getLog(EquipmentTransferObjectInboundAction.class);

    @Override
    public void processActivityData(final MessageEnvelope data) throws DatasweepException, MESException {

        EquipmentTransferObject equipmentTO = (EquipmentTransferObject) data.getPayload();
        
        if (equipmentTO == null || equipmentTO.getIdocs() == null || equipmentTO.getIdocs().isEmpty()) {
            String errMsg = "Equipment inbound integration activity has received an empty list of equipments!!!";
            logError(LOGGER, errMsg);
            addError(this.getClass().getName(), errMsg);
        } else {
            
            List<ErpEquipmentWrapper> equipments = new ArrayList<ErpEquipmentWrapper>();

            for (IDOC idoc : equipmentTO.getIdocs()) {
                setDocNum(idoc.getDocNum()); // set the document no. for all logging
                logInfo(LOGGER, "Equipment inbound integration activity start (processing IDOC: '" + idoc.getDocNum() + "'.)");

                // 1. equipment name
                String name = validate(true, idoc.getCode(), String.class, "Equipment code");
                setObjectsProcessed("Equipment Name " + name); // for inbound event log details

                // 2. equipment description
                String description = validate(true, idoc.getDescription(), String.class, "Equipment description");

                // 3. status
                String status = validate(true, idoc.getStatus(), String.class, "Status");

                if (getErrors() == null || getErrors().size() == 0) {
                    equipments.add(new ErpEquipmentWrapper(name, description, status));
                } else {
                    logError(LOGGER, MessageFormat.format(resources.getString("ct_eihub_error.generic_error"), new Object[] { idoc.getDocNum() }));
                    break; // tmedina --> don't go on adding more items to the list
                }
            }
            
            if (equipments.size() != equipmentTO.getIdocs().size()) {
                logError(LOGGER,
                        String.format("A list with %d equipments(s) has been received. But it has not been processed due data validation errors!",
                                equipmentTO.getIdocs().size()));
            } else {
                final IInboundMessageService ims = ServiceFactory.getService(IInboundMessageService.class);
                ims.createERPEquipments(equipments);
            }

        }

    }

    @Override
    public Long getObjectType() {
        return new Long(IObjectTypes.TYPE_SUBLOT);
    }

}
