package com.rockwell.mes.myeig.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.client.Batch;
import com.datasweep.compatibility.client.DatasweepException;
import com.datasweep.compatibility.client.MeasuredValue;
import com.datasweep.compatibility.client.OrderStep;
import com.datasweep.compatibility.client.OrderStepInput;
import com.datasweep.compatibility.client.ProcessOrderItem;
import com.datasweep.compatibility.client.Response;
import com.datasweep.compatibility.pnutsfunctions.utility.logErrorMessage;
import com.datasweep.plantops.common.constants.IOrderStepInputTypes;
import com.datasweep.plantops.common.constants.IOrderStepOutputTypes;
import com.rockwell.custmes.commons.base.ifc.nameduda.CustMESNamedUDAOrderStepInput;
import com.rockwell.custmes.commons.base.ifc.nameduda.CustMESNamedUDAProcessOrderItem;
import com.rockwell.mes.commons.base.ifc.exceptions.MESException;
import com.rockwell.mes.commons.base.ifc.fsm.IFSMContext;
import com.rockwell.mes.commons.base.ifc.nameduda.MESNamedUDAOrderStep;
import com.rockwell.mes.commons.base.ifc.services.ServiceFactory;
import com.rockwell.mes.myeig.data.ConsumptionTransferObject.ConsumedItem;
import com.rockwell.mes.myeig.service.ifc.IOutboundMessageService;
import com.rockwell.mes.services.inventory.ifc.IBatchService;
import com.rockwell.mes.services.order.ifc.AbstractProcessOrderItemStatusTransitionEventListener;
import com.rockwell.mes.services.order.ifc.OrderUtils;

public class SendERPConsumptionUpdateListener extends AbstractProcessOrderItemStatusTransitionEventListener {

    private static final Log LOGGER = LogFactory.getLog(SendERPConsumptionUpdateListener.class);

    /** The special stock indicator */
    private static final String STOCK_INDICATOR = "2";

    @Override

    public Response handlePreTransitionEvent(IFSMContext e) {

        return new Response();

    }

    @Override

    public void handlePostTransitionEvent(IFSMContext e) {

        ProcessOrderItem processOrderItem = (ProcessOrderItem) e.getStatefulObject();


        sendERPConsumptionUpdate(processOrderItem);

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

    private static void sendERPConsumptionUpdate(ProcessOrderItem poi) {

        LOGGER.debug(">>>> Dentro del listener 'SendERPConsumptionUpdateListener'.");

        final IOutboundMessageService service = ServiceFactory.getService(IOutboundMessageService.class);

        try {
            StringBuilder separador = new StringBuilder(1);
            separador.append(";");
            Integer target1 = null;
            Integer target2 = null;
            String posnrBatch = null;

            String batchPosition[] = null;
            String order = poi.getOrderName();
            MeasuredValue quantity = poi.getQuantity(); // final product - IMPORTANT!!!! This is not the consumption
                                                        // quantity
            Batch orderBatch = (Batch) poi.getUDA("X_batch"); // final product - IMPORTANT!!!! This is not the
                                                              // consumption batch

            LOGGER.info(String.format("Exploding info from order '%s'", order));

            List<ConsumedItem> consumedItems = new ArrayList<ConsumedItem>();

            if (OrderUtils.isBatchOrder(poi)) {

                List<OrderStep> orderSteps = poi.getControlRecipe().getOrderSteps();
                for (OrderStep orderstep : orderSteps) {
                    List<OrderStepInput> orderStepInputItems = orderstep.getOrderStepInputItems();
                    for (OrderStepInput consumedItem : orderStepInputItems) {

                        if (consumedItem.getProcessingType() == IOrderStepInputTypes.INPUT_TYPE_INPUT) {

                            String partName = consumedItem.getPart().getName();

                            Batch batch = null;
                            if (consumedItem.getAttachSublot() != null) {
                                batch = consumedItem.getAttachSublot().getBatch();
                            }

                            MeasuredValue consumedQuantity = (MeasuredValue) consumedItem.getUDA("X_consumedQuantity");

                            if (consumedQuantity != null && consumedQuantity.getValue() != null
                                    && consumedQuantity.getValue().compareTo(BigDecimal.ZERO) > 0) {
                                String reservationNum = (String) consumedItem.getUDA("X_number");
                                // String reservationPos = (String) consumedItem.getUDA("X_position");
                                String reservationPos = (String) consumedItem.getUDA("ct_ReservationPosition");
                                StringTokenizer reservationPosTokens = new StringTokenizer(reservationPos, separador.toString());
                                Integer numeroSeparadores = reservationPosTokens.countTokens();

                                String plant = (String) consumedItem.getUDA("ct_plant");
                                String storageLocation = (String) consumedItem.getUDA("ct_storageLocation");
                                String externalPosition = (String) consumedItem.getUDA("ct_externalPosition");

                                // If externalPosition is null, assign batch position.


                                if (batch == null) {
                                    LOGGER.warn(String.format("The input item '%s' has no batch related! We're going to use the order BATCH.",
                                            consumedItem.getName()));
                                    batch = orderBatch;
                                }

                                ConsumedItem consumptionObject = new ConsumedItem();

                                String batchName = ServiceFactory.getService(IBatchService.class).retrieveBatchIdentifier(batch);
                                consumptionObject.setBatch(batchName);
                                consumptionObject.setMaterial(batch.getPartNumber());
                                consumptionObject.setOrder(order);
                                if (reservationPos.contains(batch.getName())) {
                                    batchPosition = reservationPos.split(";", numeroSeparadores);
                                    for (Integer i = 0; i < batchPosition.length; i++) {
                                        String primerPosnr = batchPosition[i];
                                        target1 = primerPosnr.indexOf("_");
                                        target2 = primerPosnr.indexOf(";");
                                        posnrBatch = primerPosnr.substring(target1 + 1, target2 - 1);
                                        consumptionObject.setReservationItemNo(posnrBatch);
                                    }
                                }
                                else {
                                    String position = (externalPosition != null) ? externalPosition : reservationPos;
                                    consumptionObject.setReservationItemNo(position);
                                }
                                consumptionObject.setReservationItemNo(reservationPos);
                                consumptionObject.setReservationNo(reservationNum);

                                consumptionObject.setPlant(plant);
                                consumptionObject.setLocation(storageLocation);

                                consumptionObject.setStorageBin(StringUtils.EMPTY);
                                consumptionObject.setStorageType(StringUtils.EMPTY);
                                consumptionObject.setCostCenter(MESNamedUDAOrderStep.getCostCenter(consumedItem.getOrderStep()));
                                // String wbsElement = CustMESNamedUDAOrderStepInput.getWbselement(consumedItem);
                                // consumptionObject.setBreakDownNumber(wbsElement);
                                String wbsElement = CustMESNamedUDAProcessOrderItem.getWBS(poi);
                                consumptionObject.setBreakDownNumber(wbsElement);
                                String stockIndicator = CustMESNamedUDAOrderStepInput.getSpecialstockIndicator(consumedItem);

                                if (STOCK_INDICATOR.equals(stockIndicator))
                                    consumptionObject.setStockIndicator("Q");
                                else
                                    consumptionObject.setStockIndicator(StringUtils.EMPTY);

                                if (consumedQuantity != null) {
                                    consumptionObject.setQuantity(consumedQuantity.getValue().toString());
                                    consumptionObject.setUom(consumedQuantity.getUnitOfMeasure().toString().toUpperCase(Locale.getDefault()));
                                }

                                consumedItems.add(consumptionObject);
                            } else {
                                LOGGER.warn(String.format("El item '%s' no tiene cantidad asociada y no será enviado.", partName));
                            }
                        }

                    }
                }

                service.createConsumptionMessage(consumedItems);

            }
            else
                LOGGER.debug(String.format("La orden '%s' no es de produccion.", order));

        } catch (MESException | DatasweepException e) {

            // TODO Auto-generated catch block

            e.printStackTrace();

            LOGGER.error(e);

        }

        LOGGER.debug(">>>> Final del listener 'SendERPConsumptionUpdateListener'.");

    }

}