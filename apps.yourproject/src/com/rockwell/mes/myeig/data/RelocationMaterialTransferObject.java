package com.rockwell.mes.myeig.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.rockwell.integration.messaging.BasePayload;

/**
 * 
 * Relocation for material transfer bean. Used to generate requirement to get the material for dispensing from
 * warehouse.
 * <p>
 * 
 * @author adasoft (c) Copyright 2020 Rockwell Automation Technologies, Inc. All Rights Reserved.
 */
public class RelocationMaterialTransferObject extends BasePayload implements Serializable {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 7961891684680313080L;

    /** the IDOC document number or message id */
    private String idoc;

    /** the publishing date */
    private String publishingDate;

    /** the documentation date */
    private String documentDate;

    /** the code */
    private String code;

    /** List of material details with quantity */
    private List<RelocationMaterialObject> materials = new ArrayList<>();

    /**
     * @return Returns the idoc.
     */
    public String getIdoc() {
        return idoc;
    }

    /**
     * @param idoc The idoc to set.
     */
    public void setIdoc(String idoc) {
        this.idoc = idoc;
    }

    /**
     * @return Returns the publishingDate.
     */
    public String getPublishingDate() {
        return publishingDate;
    }

    /**
     * @param publishingDate The publishingDate to set.
     */
    public void setPublishingDate(String publishingDate) {
        this.publishingDate = publishingDate;
    }

    /**
     * @return Returns the documentDate.
     */
    public String getDocumentDate() {
        return documentDate;
    }

    /**
     * @param documentDate The documentDate to set.
     */
    public void setDocumentDate(String documentDate) {
        this.documentDate = documentDate;
    }

    /**
     * @return Returns the materials.
     */
    public List<RelocationMaterialObject> getMaterials() {
        return materials;
    }

    /**
     * @param materials The materials to set.
     */
    public void setMaterials(List<RelocationMaterialObject> materials) {
        this.materials = materials;
    }

    /**
     * @return Returns the code.
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code The code to set.
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 
     * This class is used to implement the data segments batch wise
     * <p>
     * 
     * @author adasoft, (c) Copyright 2020 Rockwell Automation Technologies, Inc. All Rights Reserved.
     */

    public static class RelocationMaterialObject implements Serializable {

        /**
         * Comment for <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = 4699559280379701224L;

        /** the material number */
        private String material;

        /** the batch number */
        private String batch;

        /** the order number */
        private String order;

        /** the plant */
        private String plant;

        /** the movement type */
        private String movementType;

        /** the quantity */
        private String quantity;

        /** the quantity unit of measure */
        private String uom;

        /** the storage location */
        private String storageLocation;

        /**
         * @return Returns the material.
         */
        public String getMaterial() {
            return material;
        }

        /**
         * @param material The material to set.
         */
        public void setMaterial(String material) {
            this.material = material;
        }

        /**
         * @return Returns the batch.
         */
        public String getBatch() {
            return batch;
        }

        /**
         * @param batch The batch to set.
         */
        public void setBatch(String batch) {
            this.batch = batch;
        }

        /**
         * @return Returns the order.
         */
        public String getOrder() {
            return order;
        }

        /**
         * @param order The order to set.
         */
        public void setOrder(String order) {
            this.order = order;
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
         * @return Returns the quantity.
         */
        public String getQuantity() {
            return quantity;
        }

        /**
         * @param quantity The quantity to set.
         */
        public void setQuantity(String quantity) {
            this.quantity = quantity;
        }

        /**
         * @return Returns the uom.
         */
        public String getUom() {
            return uom;
        }

        /**
         * @param uom The uom to set.
         */
        public void setUom(String uom) {
            this.uom = uom;
        }

        /**
         * @return Returns the storageLocation.
         */
        public String getStorageLocation() {
            return storageLocation;
        }

        /**
         * @param storageLocation - The storageLocation to set.
         */
        public void setStorageLocation(String storageLocation) {
            this.storageLocation = storageLocation;
        }

    }
}
