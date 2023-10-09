package com.rockwell.mes.myeig.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.client.Batch;
import com.datasweep.compatibility.client.DatasweepException;
import com.datasweep.compatibility.client.MeasuredValue;
import com.datasweep.compatibility.client.OrderStep;
import com.datasweep.compatibility.client.OrderStepOutput;
import com.datasweep.compatibility.client.ProcessOrderItem;
import com.datasweep.compatibility.client.ProcessStepProducedSublotInfo;
import com.datasweep.compatibility.client.ProducedSublotInfo;
import com.datasweep.compatibility.client.Response;
import com.datasweep.compatibility.client.Sublot;
import com.datasweep.compatibility.ui.Time;
import com.datasweep.plantops.common.constants.IOrderStepOutputTypes;
import com.rockwell.mes.commons.base.ifc.exceptions.MESException;
import com.rockwell.mes.commons.base.ifc.fsm.IFSMContext;
import com.rockwell.mes.commons.base.ifc.services.ServiceFactory;
import com.rockwell.mes.myeig.data.ProductionTransferObject.ProducedItem;
import com.rockwell.mes.myeig.service.ifc.IOutboundMessageService;
import com.rockwell.mes.services.inventory.ifc.IBatchService;
import com.rockwell.mes.services.order.ifc.AbstractProcessOrderItemStatusTransitionEventListener;
import com.rockwell.mes.services.order.ifc.OrderUtils;

public class SendERPProductionUpdateListener extends AbstractProcessOrderItemStatusTransitionEventListener {

    private static final Log LOGGER = LogFactory.getLog(SendERPProductionUpdateListener.class);

    @Override

    public Response handlePreTransitionEvent(IFSMContext e) {

        return new Response();

    }

    @Override

    public void handlePostTransitionEvent(IFSMContext e) {

        ProcessOrderItem processOrderItem = (ProcessOrderItem) e.getStatefulObject();

        sendERPProductionUpdate(processOrderItem);

    }

    /**
     * 
     * Send process order status update message to ERP
     *
     * 
     * 
     * @param poi the ProcessOrderItem of the transition
     * 
     */

    private static void sendERPProductionUpdate(ProcessOrderItem poi) {
        LOGGER.debug(">>>> Dentro del listener 'SendERPProductionUpdateListener'.");
        final IOutboundMessageService service = ServiceFactory.getService(IOutboundMessageService.class);

        try {

            String order = poi.getOrderName();
            MeasuredValue orderQuantity = poi.getQuantity();
            MeasuredValue orderActualQuantity = (MeasuredValue) poi.getUDA("X_actualQuantity");

            LOGGER.info(String.format("Exploding info from order '%s'", order));

            List<OrderStep> orderSteps = poi.getControlRecipe().getOrderSteps();

            Map<String, ProducedItem> summary = new HashMap<String, ProducedItem>();

            /*if (OrderUtils.isBatchOrder(poi) && isManufacturingOrder(poi)) {*/
            /*RD: aquí se comprobaba si la orden es de Fabricación, porque sólo se quería enviar fichero de alta producto final si la OF era de Fabricacion*/
            /*Se anula esa comprobación 20/01/2022 por peticion de la gente de SAP*/
            if (OrderUtils.isBatchOrder(poi)) {

                for (OrderStep orderstep : orderSteps) {
                    List<OrderStepOutput> orderStepOutputItems = orderstep.getOrderStepOutputItems();
                    for (OrderStepOutput stepOutput : orderStepOutputItems) {

                        MeasuredValue stepOutputActualQuantity = stepOutput.getActualQuantity();
                        MeasuredValue stepOutputPlannedQuantity = stepOutput.getPlannedQuantity();

                        if (stepOutput.getProcessingType() == IOrderStepOutputTypes.OUTPUT_TYPE_OUTPUT) {
                            List<ProducedSublotInfo> sublots = stepOutput.getProducedSublotInfoItems();
                            for (ProducedSublotInfo sublotInfo : sublots) {

                                Batch batch = null;

                                if (sublotInfo.getBatch() != null) {
                                    /*
                                     * IMPORTANT!!!! We're Assuming each batch has only one associated Part
                                     * (relationship 1:1)
                                     */
                                    batch = sublotInfo.getBatch();
                                    String partName = batch.getPartNumber();
                                    // Si la cantidad actual de la orden es 0, cogemos la cantidad de los batches
                                    // generados
                                    MeasuredValue quantity = orderActualQuantity != null ? orderActualQuantity : sublotInfo.getBatch().getQuantity();

                                    // Importante solo procesamos el material que es igual al material de la receta
                                    // maestra.!!
                                    if (partName != null
                                            && partName.equals(OrderUtils.getControlRecipe(poi).getMasterRecipe().getPart().getPartNumber())) {
                                        if (quantity != null && quantity.getValue() != null && quantity.getValue().compareTo(BigDecimal.ZERO) > 0) {
                                            ProducedItem producedItem = new ProducedItem();
                                            producedItem.setMaterial(partName);
                                            producedItem.setQuantity(quantity.getValue().toString());
                                            producedItem.setUom(quantity.getUnitOfMeasure().toString().toUpperCase(Locale.getDefault()));
                                            producedItem.setBatch(ServiceFactory.getService(IBatchService.class).retrieveBatchIdentifier(batch));

                                            Time sublotInfoProducedTime = sublotInfo.getTrxTime();
                                            producedItem.setProductionDate(sublotInfoProducedTime.getYear() + ""
                                                    + (sublotInfoProducedTime.getMonth() < 10 ? "0" + sublotInfoProducedTime.getMonth()
                                                            : sublotInfoProducedTime.getMonth())
                                                    + "" + (sublotInfoProducedTime.getDay() < 10 ? "0" + sublotInfoProducedTime.getDay()
                                                            : sublotInfoProducedTime.getDay()));

                                            summary.put(partName, producedItem);
                                        } else {
                                            LOGGER.warn(String.format("El item '%s' no tiene cantidad asociada y no será enviado.", partName));
                                        }
                                    }
                                    else
                                        LOGGER.debug(String.format("El item '%s' no es el de la receta '%s'.", partName,
                                                OrderUtils.getControlRecipe(poi).getMasterRecipe().getPart().getPartNumber()));
                                }
                            }
                        }
                    }
                }



                if (summary != null && !summary.isEmpty()) {
                    service.createProductionMessage(order, new ArrayList<ProducedItem>(summary.values()), null, null);
                } else {
                    LOGGER.info(String.format("La orden '%s' no generará ningún fichero de salida porque no tiene materiales que añadir.", order));
                }

            }
            else
                LOGGER.debug(String.format("La orden '%s' no es de produccion.", order));
            
        } catch (MESException | DatasweepException e) {
            e.printStackTrace();
            LOGGER.error(e);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e);
        }

        LOGGER.debug(">>>> Final del listener 'SendERPProductionUpdateListener'.");

    }

    private static boolean isManufacturingOrder(ProcessOrderItem poi) throws DatasweepException {
        if (poi != null && poi.getUDA(OutboundMessageService.UDA_LABEL_WORK_ORDER_TPYE) != null) {
            String orderType = poi.getUDA(OutboundMessageService.UDA_LABEL_WORK_ORDER_TPYE) != null
                    ? poi.getUDA(OutboundMessageService.UDA_LABEL_WORK_ORDER_TPYE).toString() : "";
            return OutboundMessageService.PACKAGING_ORDER_TYPE_VALUE.equals(orderType);
        } else {
            // if no info inside the UDA --> false (it never sends the product information)
            return false;
        }

    }

    /**
     * 
     * @param info
     * @return
     */
    private static MeasuredValue accumulateQuantities(ProcessStepProducedSublotInfo info) {
        if (info == null) {
            return null;
        } else {
            MeasuredValue quantity = null;
            for (Object obj : info.getSublots()) {
                Sublot sublot = (Sublot) obj;
                if (quantity == null) {
                    
                }
            }
            return quantity;
        }
    }

}