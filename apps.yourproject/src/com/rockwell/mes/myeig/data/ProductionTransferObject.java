package com.rockwell.mes.myeig.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.rockwell.integration.messaging.BasePayload;

/**
 * Material production transfer bean. Used to create a material production (SAP Goods Receipt) upload interface
 * transaction
 * <p>
 * 
 * @author syim, (c) Copyright 2012 Rockwell Automation Technologies, Inc. All Rights Reserved.
 */
public class ProductionTransferObject extends BasePayload implements Serializable {

    /** The <code>serialVersionUID</code> */
    private static final long serialVersionUID = -8358453709090562001L;

    /** the IDOC document number or message id */
    private String idoc;

    /** the order number */
    private String order;

    /** the consumption date */
    private String produceDate;

    /** the consumption time */
    private String produceTime;

    /** the list of produced items */
    private List<ProducedItem> producedItems = new ArrayList<ProducedItem>();

    /**
     * @return Returns the IDOC document number
     */
    public String getIdoc() {
        return idoc;
    }

    /**
     * @param idoc Sets the IDOC document number
     */
    public void setIdoc(final String idoc) {
        this.idoc = idoc;
    }

    /**
     * @return Returns the order number
     */
    public String getOrder() {
        return order;
    }

    /**
     * @param order Sets the order number
     */
    public void setOrder(final String order) {
        this.order = order;
    }

    /**
     * @return Returns the produceDate.
     */
    public String getProduceDate() {
        return produceDate;
    }

    /**
     * @param produceDate The produceDate to set.
     */
    public void setProduceDate(String produceDate) {
        this.produceDate = produceDate;
    }

    /**
     * @return Returns the produceTime.
     */
    public String getProduceTime() {
        return produceTime;
    }

    /**
     * @param produceTime The produceTime to set.
     */
    public void setProduceTime(String produceTime) {
        this.produceTime = produceTime;
    }

    public List<ProducedItem> getProducedItems() {
        return producedItems;
    }

    public void setProducedItems(List<ProducedItem> producedItems) {
        this.producedItems = producedItems;
    }

    public static class ProducedItem implements Serializable {
        
        /**
         * Comment for <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = 7387292554299598551L;

        /** the material number */
        private String material;

        /** the location identifier */
        private String location;

        /** the batch number */
        private String batch;

        /** the order item number */
        private String orderItemNo;

        /** the reservation number */
        private String erpOperationNo;

        /** the erp phase number */
        private String erpPhaseNo;

        /** the quantity */
        private String quantity;

        /** the quantity unit of measure */
        private String uom;

        /** the transaction code */
        private String transactionCode;

        /** the plant */
        private String plant;

        /** the movement type */
        private String movementType;

        /** the production date */
        private String productionDate;

        /**
         * @return Returns the material number
         */
        public String getMaterial() {
            return material;
        }

        /**
         * @param material Sets the material number
         */
        public void setMaterial(final String material) {
            this.material = material;
        }

        /**
         * @return Returns the location identifier
         */
        public String getLocation() {
            return location;
        }

        /**
         * @param location Sets the location identifier
         */
        public void setLocation(final String location) {
            this.location = location;
        }

        /**
         * @return Returns the batch number
         */
        public String getBatch() {
            return batch;
        }

        /**
         * @param batch Sets the batch number
         */
        public void setBatch(final String batch) {
            this.batch = batch;
        }

        /**
         * @return Returns the orderItemNo.
         */
        public String getOrderItemNo() {
            return orderItemNo;
        }

        /**
         * @param orderItemNo The orderItemNo to set.
         */
        public void setOrderItemNo(String orderItemNo) {
            this.orderItemNo = orderItemNo;
        }

        /**
         * @return Returns the erpOperationNo.
         */
        public String getErpOperationNo() {
            return erpOperationNo;
        }

        /**
         * @param erpOperationNo The erpOperationNo to set.
         */
        public void setErpOperationNo(String erpOperationNo) {
            this.erpOperationNo = erpOperationNo;
        }

        /**
         * @return Returns the erpPhaseNo.
         */
        public String getErpPhaseNo() {
            return erpPhaseNo;
        }

        /**
         * @param erpPhaseNo The erpPhaseNo to set.
         */
        public void setErpPhaseNo(String erpPhaseNo) {
            this.erpPhaseNo = erpPhaseNo;
        }

        /**
         * @return Returns the quantity
         */
        public String getQuantity() {
            return quantity;
        }

        /**
         * @param quantity Sets the quantity
         */
        public void setQuantity(final String quantity) {
            this.quantity = quantity;
        }

        /**
         * @return Returns the quantity unit of measure
         */
        public String getUom() {
            return uom;
        }

        /**
         * @param uom Sets the quantity unit of measure
         */
        public void setUom(final String uom) {
            this.uom = uom;
        }

        /**
         * @return Returns the transactionCode.
         */
        public String getTransactionCode() {
            return transactionCode;
        }

        /**
         * @param transactionCode The transactionCode to set.
         */
        public void setTransactionCode(String transactionCode) {
            this.transactionCode = transactionCode;
        }

        /**
         * @return Returns the plant.
         */
        public String getPlant() {
            return plant;
        }

        /**
         * @param plant The plant to set.
         */
        public void setPlant(String plant) {
            this.plant = plant;
        }

        /**
         * @return Returns the movementType.
         */
        public String getMovementType() {
            return movementType;
        }

        /**
         * @param movementType The movementType to set.
         */
        public void setMovementType(String movementType) {
            this.movementType = movementType;
        }

        /**
         * @return Returns the productionDate.
         */
        public String getProductionDate() {
            return productionDate;
        }

        /**
         * @param movementType The productionDate to set.
         */
        public void setProductionDate(String productionDate) {
            this.productionDate = productionDate;
        }

    }

}
