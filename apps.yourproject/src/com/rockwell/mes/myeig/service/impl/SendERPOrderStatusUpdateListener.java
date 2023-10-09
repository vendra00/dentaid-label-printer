package com.rockwell.mes.myeig.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.datasweep.compatibility.client.DatasweepException;
import com.datasweep.compatibility.client.ProcessOrderItem;
import com.datasweep.compatibility.client.Response;
import com.datasweep.plantops.common.constants.IDataTypes;
import com.datasweep.plantops.common.constants.filtering.IFilterComparisonOperators;
import com.rockwell.mes.commons.base.ifc.exceptions.MESException;
import com.rockwell.mes.commons.base.ifc.fsm.IFSMContext;
import com.rockwell.mes.commons.base.ifc.services.ServiceFactory;
import com.rockwell.mes.myeig.service.ifc.IOutboundMessageService;
import com.rockwell.mes.services.commons.ifc.order.ProcessOrderItemFSMConstants;
import com.rockwell.mes.services.order.ifc.AbstractProcessOrderItemStatusTransitionEventListener;
import com.rockwell.mes.services.order.ifc.OrderUtils;

public class SendERPOrderStatusUpdateListener extends AbstractProcessOrderItemStatusTransitionEventListener {

    private static final Log LOGGER = LogFactory.getLog(SendERPOrderStatusUpdateListener.class);

    @Override

    public Response handlePreTransitionEvent(IFSMContext e) {

        return new Response();

    }

    @Override

    public void handlePostTransitionEvent(IFSMContext e) {

        ProcessOrderItem processOrderItem = (ProcessOrderItem) e.getStatefulObject();
        String statusName = processOrderItem.getCurrentState(ProcessOrderItemFSMConstants.FSM_REL_SHIP).getState().getName();
if(!statusName.equalsIgnoreCase("In Process")) {
        sendERPOrderStatusUpdate(processOrderItem);
}

    }

    /**
     * Send process order status update message to ERP
     * 
     * @param poi the ProcessOrderItem of the transition
     */
    private static void sendERPOrderStatusUpdate(ProcessOrderItem poi) {

        LOGGER.debug(">>>> Dentro del listener 'SendERPOrderStatusUpdateListener'.");

        final IOutboundMessageService service = ServiceFactory.getService(IOutboundMessageService.class);

        String statusName = poi.getCurrentState(ProcessOrderItemFSMConstants.FSM_REL_SHIP).getState().getName();
        String order = poi.getOrderName();

        try {
            // AFH 20/02/23 Stat interface
        	
            String Inspection_lot ="";
            if(poi.getUDA("ct_Inspection_lot")==null) {
            	Inspection_lot = "";
            }
            else
            {
            	Inspection_lot = poi.getUDA("ct_Inspection_lot").toString();
            }
            String Stat_Finished = "";
            String Stat_Prod_Finished = "";
            String Stat_Qa_Finished = "";

            switch (statusName) {
            case "Finished":
                Stat_Finished = "0010";
                break;
            case "ProductionReviewed":
                Stat_Prod_Finished = "0010";
                break;
            case "Reviewed":
                String a = "";
                Stat_Qa_Finished = "0010";
                break;
            default:
            }
            
            /*           
            if (statusName.equals("Finished")) {
                Stat_Finished = "0010";
            }
            
            if (statusName.equals("Production Reviewed")) {
                Stat_Prod_Finished = "0010";
            }
            
            if (statusName.equals("Review")) {
                Stat_QA_Finished = "0010";
            }
            */

            if (OrderUtils.isBatchOrder(poi))
                // AFH service.createOrderStatusMessage(poi.getName(),
                // Long.toString(OrderUtils.getControlRecipe(poi).getKey()), "ErpOp", statusName);
                service.createOrderStatusMessage(Inspection_lot, Stat_Finished, Stat_Prod_Finished, Stat_Qa_Finished);
            else
                LOGGER.debug(String.format("La orden '%s' no es de produccion.", order));

        } catch (MESException | DatasweepException e) {

            // TODO Auto-generated catch block

            e.printStackTrace();

            LOGGER.error(e);

        }

        LOGGER.debug(">>>> Final del listener 'SendERPOrderStatusUpdateListener'.");

    }

}