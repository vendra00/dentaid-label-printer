package com.rockwell.mes.myeig.data;

import java.io.Serializable;

import com.rockwell.integration.messaging.BasePayload;

/**
 * Consumption transfer bean. Used to create a consumption (SAP Goods Issue) upload interface transaction.
 * <p>
 * 
 * @author syim, (c) Copyright 2012 Rockwell Automation Technologies, Inc. All Rights Reserved.
 */
public class ConsumptionTransferObject extends BasePayload implements Serializable {

    /** The <code>serialVersionUID</code> */
    private static final long serialVersionUID = 6834504255905305135L;

    /** the IDOC document number or message id */
    private String idoc;

    /** the consumption date */
    private String consumeDate;

    /** the consumption time */
    private String consumeTime;

    java.util.List<ConsumedItem> consumedItems = new java.util.ArrayList<ConsumedItem>();

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
     * @return Returns the consumeDate.
     */
    public String getConsumeDate() {
        return consumeDate;
    }

    /**
     * @param consumeDate The consumeDate to set.
     */
    public void setConsumeDate(String consumeDate) {
        this.consumeDate = consumeDate;
    }

    /**
     * @return Returns the consumeTime.
     */
    public String getConsumeTime() {
        return consumeTime;
    }

    /**
     * @param consumeTime The consumeTime to set.
     */
    public void setConsumeTime(String consumeTime) {
        this.consumeTime = consumeTime;
    }

    /**
     * @return Returns the consumedItems.
     */
    public java.util.List<ConsumedItem> getConsumedItems() {
        return consumedItems;
    }

    /**
     * @param consumedItems The consumedItems to set.
     */
    public void setConsumedItems(java.util.List<ConsumedItem> consumedItems) {
        this.consumedItems = consumedItems;
    }

    public static class ConsumedItem implements Serializable {

        /**
         * Comment for <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = -7182351187981050583L;

        /** the material number */
        private String material;

        /** the location identifier */
        private String location;

        /** the batch number */
        private String batch;

        /** the order number */
        private String order;

        /** the reservation number */
        private String reservationNo;

        /** the reservation position number */
        private String reservationItemNo;

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

        /** the storage bin */
        private String storageBin;

        /** the storage type */
        private String storageType;

        /** the work breakdown */
        private String breakDownNumber;

        /** the special stock indicator */
        private String stockIndicator;

        /** the cost center */
        private String costCenter;

        /**
         * // * @return Returns the costCenter. //
         */
        public String getCostCenter() {
            return costCenter;
        }

        /**
         * @param costCenter The costCenter to set.
         */
        public void setCostCenter(String costCenter) {
            this.costCenter = costCenter;
        }

        /**
         * @return Returns the stockIndicator.
         */
        public String getStockIndicator() {
            return stockIndicator;
        }

        /**
         * @param stockIndicator The stockIndicator to set.
         */
        public void setStockIndicator(String stockIndicator) {
            this.stockIndicator = stockIndicator;
        }

        /**
         * @return Returns the breakDownNumber.
         */
        public String getBreakDownNumber() {
            return breakDownNumber;
        }

        /**
         * @param breakDownNumber The breakDownNumber to set.
         */
        public void setBreakDownNumber(String breakDownNumber) {
            this.breakDownNumber = breakDownNumber;
        }

        /**
         * @return Returns the location.
         */
        public String getLocation() {
            return location;
        }

        /**
         * @param location The location to set.
         */
        public void setLocation(String location) {
            this.location = location;
        }

        /**
         * @return Returns the storageBin.
         */
        public String getStorageBin() {
            return storageBin;
        }

        /**
         * @param storageBin The storageBin to set.
         */
        public void setStorageBin(String storageBin) {
            this.storageBin = storageBin;
        }

        /**
         * @return Returns the storageType.
         */
        public String getStorageType() {
            return storageType;
        }

        /**
         * @param storageType The storageType to set.
         */
        public void setStorageType(String storageType) {
            this.storageType = storageType;
        }

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
         * @return Returns the reservation number
         */
        public String getReservationNo() {
            return reservationNo;
        }

        /**
         * @param reservationNo Sets the reservation number
         */
        public void setReservationNo(final String reservationNo) {
            this.reservationNo = reservationNo;
        }

        /**
         * @return Returns the reservation position number
         */
        public String getReservationItemNo() {
            return reservationItemNo;
        }

        /**
         * @param reservationItemNo Sets the reservation position number
         */
        public void setReservationItemNo(final String reservationItemNo) {
            this.reservationItemNo = reservationItemNo;
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


    }

}
