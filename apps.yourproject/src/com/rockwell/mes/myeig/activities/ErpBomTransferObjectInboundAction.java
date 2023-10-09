package com.rockwell.mes.myeig.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.client.DatasweepException;
import com.datasweep.compatibility.client.MeasuredValue;
import com.datasweep.compatibility.client.Part;
import com.datasweep.compatibility.client.UnitOfMeasure;
import com.datasweep.plantops.common.constants.IObjectTypes;
import com.rockwell.integration.messaging.MessageEnvelope;
import com.rockwell.mes.commons.base.ifc.exceptions.MESException;
import com.rockwell.mes.commons.base.ifc.services.ServiceFactory;
import com.rockwell.mes.myeig.data.ErpBomTransferObject;
import com.rockwell.mes.myeig.data.ErpBomTransferObject.Item;
import com.rockwell.mes.myeig.service.ifc.IInboundMessageService;
import com.rockwell.mes.myeig.utility.IntegrationGatewayHelper;

/**
 * This class implements functionality to process incoming ERP BOM messages
 * <p>
 * 
 * @author syim, (c) Copyright 2012 Rockwell Automation Technologies, Inc. All
 *         Rights Reserved.
 */
public class ErpBomTransferObjectInboundAction extends AbstractInboundActivity {

    /** logger */
    private static final Log LOGGER = LogFactory.getLog(ErpBomTransferObjectInboundAction.class);

    @Override
    public void processActivityData(final MessageEnvelope data) throws DatasweepException, MESException {

        ErpBomTransferObject erpBomTransferObject = (ErpBomTransferObject) data.getPayload();
        setDocNum(erpBomTransferObject.getIdoc()); // set the document no. for all logging
        logInfo(LOGGER, "ERP BOM inbound integration activity start.");

        // 1. BOM header material
        Part material = validate(true, erpBomTransferObject.getMaterial(), Part.class, "Material");

        // 2. Method
        String alternative = validate(true, erpBomTransferObject.getAlternative(), String.class, "Method");
        setObjectsProcessed(erpBomTransferObject.getMaterial() + "." + alternative); // for in-bound event log details

        // 3. Base quantity
        String baseQtyVal = validate(true, erpBomTransferObject.getBaseQty(), String.class, "Base Quantity");
        UnitOfMeasure baseQtyUom = validate(true, erpBomTransferObject.getBaseQtyUom(), UnitOfMeasure.class,
                "Base Quantity UOM");
        MeasuredValue baseQty = IntegrationGatewayHelper.getMeasuredValue(baseQtyVal, baseQtyUom);

        // 4. BOM items
        List<Map<String, Object>> bomItems = new ArrayList<Map<String, Object>>();
        for (Item item : erpBomTransferObject.getItems()) {
            // a. BOM item position
            String itemPos = validate(true, item.getPosition(), String.class, "Item Position");
            // b. BOM item material
            Part itemMat = validate(true, item.getMaterial(), Part.class, "Item Material");
            // c. BOM item quantity
            String itemQtyVal = validate(true, item.getPlannedQty(), String.class, "Item Quantity");
            UnitOfMeasure itemQtyUom = validate(true, item.getPlannedQtyUom(), UnitOfMeasure.class, 
                    "Item Quantity UOM");
            MeasuredValue itemQty = IntegrationGatewayHelper.getMeasuredValue(itemQtyVal, itemQtyUom);
            // d. Bom item fixed quantity indicator
            Boolean fixedQty = StringUtils.equalsIgnoreCase(item.getFixedQty(), "X");
            
            HashMap<String, Object> bomItem = new HashMap<String, Object>();
            bomItem.put("position", itemPos);
            bomItem.put("material", itemMat);
            bomItem.put("plannedQty", itemQty);
            bomItem.put("fixedQty", fixedQty);
            bomItems.add(bomItem);
        }

        if (getErrors() == null || getErrors().size() == 0) {
            IInboundMessageService ims = ServiceFactory.getService(IInboundMessageService.class);
            ims.createERPBom(material, alternative, baseQty, bomItems);
        }
    }

    @Override
    public Long getObjectType() {
        return new Long(IObjectTypes.TYPE_BOM);
    }

}
