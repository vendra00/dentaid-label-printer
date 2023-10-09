package com.rockwell.mes.myeig.activities;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
import com.rockwell.mes.myeig.data.SublotTransferObject;
import com.rockwell.mes.myeig.data.SublotTransferObject.Detail;
//import com.rockwell.mes.myeig.data.SublotTransferObject.IDOC;
import com.rockwell.mes.myeig.data.SublotTransferObject.Stock;
import com.rockwell.mes.myeig.service.ifc.ErpSublotWrapper;
import com.rockwell.mes.myeig.service.ifc.IInboundMessageService;

/**
 * This class implements functionality to process incoming sublots messages
 * 
 * @author tmedina (Customer customization)
 */
public class SublotTransferObjectInboundAction extends AbstractInboundActivity {

    /** The default value for Storage Area value */
    private static final String DEFAULT_STORAGE_AREA = "WeightArea";

    private static final String SynthonPlantCode = "2100";

    /** logger */
    private static final Log LOGGER = LogFactory.getLog(SublotTransferObjectInboundAction.class);

    @Override
    public void processActivityData(final MessageEnvelope data) throws DatasweepException, MESException {

        String DocNumber = "";

        SublotTransferObject sublotTO = (SublotTransferObject) data.getPayload();
        
        if (sublotTO == null || sublotTO.getIdoc() == null || sublotTO.getIdoc().isEmpty()) {
            String errMsg = "Sublot inbound integration activity has received an empty list of subLots!!!";
            logError(LOGGER, errMsg);
            addError(this.getClass().getName(), errMsg);
        } else {
            
            List<ErpSublotWrapper> sublots_ToProcess = new ArrayList<ErpSublotWrapper>();
            Integer sublots_Received = 0;

            setDocNum(sublotTO.getIdoc()); // set the document no. for all logging
            DocNumber = sublotTO.getIdoc();
            logInfo(LOGGER, "Sublot inbound integration activity start (processing IDOC: '" + sublotTO.getIdoc() + "'.)");

            for (Stock Stock : sublotTO.getStock()) {

                // 0. Plant
                String Plant = validate(true, Stock.getPlant(), String.class, "Plant");
                if (Plant != null && !StringUtils.equals(Plant, SynthonPlantCode)) {
                    addError(this.getClass().getName(), "Plant code is incorrect.\n");
                }

                for (Detail Detail : Stock.getDetails()) {
                    sublots_Received = sublots_Received + 1;

                    // 1. material number
                    Part material = validate(true, Detail.getMaterial(), Part.class, "Material Number");

                    // 2. batch number
                    String batchName = validate(true, Detail.getBatch(), String.class, "Batch Name");
                    setObjectsProcessed("Batch Name" + batchName); // for inbound event log details

                    // 3. sublot Qty
                    String quantity = validate(true, Detail.getQuantity(), String.class, "Sublot Quantity");

                    // 4. sublot UoM
                    UnitOfMeasure uom = validate(true, Detail.getUnitOfMeasure(), UnitOfMeasure.class, "Unit of Measure");

                    // 5. Sublot Id
                    String sublot = validate(true, Detail.getSublot(), String.class, "Sublot Id");

                    // 6. Storage location
                    Location storageLocation = PCContext.getFunctions().getLocation(DEFAULT_STORAGE_AREA);

                    if (getErrors() == null || getErrors().size() == 0) {
                        sublots_ToProcess.add(new ErpSublotWrapper(batchName, material, quantity, storageLocation, sublot, uom));
                    } else {
                        logError(LOGGER, MessageFormat.format(resources.getString("ct_eihub_error.generic_error"), new Object[] { DocNumber }));
                        break; // tmedina --> don't go on adding more items to the list
                    }
                }
            }
            
            // Si alguno de los sublotes que han llegado en el fichero no se procesan, no se inserta ninguno de ellos.
            if (sublots_ToProcess.size() != sublots_Received) {
                logError(LOGGER,
                        String.format("A list with %d sublot(s) has been received. But not all has not been processed due data validation errors!",
                                sublotTO.getStock().size()));
            } else {
                final IInboundMessageService ims = ServiceFactory.getService(IInboundMessageService.class);
                ims.createERPSublots(sublots_ToProcess);
            }

        }

    }

    @Override
    public Long getObjectType() {
        return new Long(IObjectTypes.TYPE_SUBLOT);
    }

}
