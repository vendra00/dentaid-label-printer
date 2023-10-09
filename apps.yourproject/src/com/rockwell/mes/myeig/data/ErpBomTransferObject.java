package com.rockwell.mes.myeig.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.rockwell.integration.messaging.BasePayload;

/**
 * ERP Bill of Material transfer bean. Used to create a ERP BOMs in PharmaSuite
 * <p>
 * 
 * @author syim, (c) Copyright 2012 Rockwell Automation Technologies, Inc. All
 *         Rights Reserved.
 */
public class ErpBomTransferObject extends BasePayload implements Serializable {

    /** The <code>serialVersionUID</code> */
    private static final long serialVersionUID = 5534631944283516076L;

    /** the IDOC document number or message id */
    private String idoc;

    /** the material */
    private String material;

    /** the alternative */
    private String alternative;

    /** the base quantity */
    private String baseQty;

    /** the base quantity uom */
    private String baseQtyUom;

    /** List of BOM items */
    private List<Item> items = new ArrayList<Item>();

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
     * @return Returns the alternative.
     */
    public String getAlternative() {
        return alternative;
    }

    /**
     * @param alternative The alternative to set.
     */
    public void setAlternative(String alternative) {
        this.alternative = alternative;
    }

    /**
     * @return Returns the baseQty.
     */
    public String getBaseQty() {
        return baseQty;
    }

    /**
     * @param baseQty The baseQty to set.
     */
    public void setBaseQty(String baseQty) {
        this.baseQty = baseQty;
    }

    /**
     * @return Returns the baseQtyUom.
     */
    public String getBaseQtyUom() {
        return baseQtyUom;
    }

    /**
     * @param baseQtyUom The baseQtyUom to set.
     */
    public void setBaseQtyUom(String baseQtyUom) {
        this.baseQtyUom = baseQtyUom;
    }

    /**
     * @return Returns the list of BOM items
     */
    public List<Item> getItems() {
        return items;
    }

    /**
     * @param items Sets the list of BOM items
     */
    public void setItems(final List<Item> items) {
        this.items = items;
    }

    /**
     * BOM item bean
     * <p>
     * 
     * @author syim, (c) Copyright 2012 Rockwell Automation Technologies, Inc.
     *         All Rights Reserved.
     */
    public static class Item implements Serializable {

        /** The <code>serialVersionUID</code> */
        private static final long serialVersionUID = 1L;

        /** the item position */
        private String position;

        /** the item material */
        private String material;

        /** the planned quantity */
        private String plannedQty;

        /** the planned quantity unit of measure */
        private String plannedQtyUom;

        /** fixed quantity? */
        private String fixedQty;

        /**
         * @return Returns the position.
         */
        public String getPosition() {
            return position;
        }

        /**
         * @param position The position to set.
         */
        public void setPosition(String position) {
            this.position = position;
        }

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
         * @return Returns the plannedQty.
         */
        public String getPlannedQty() {
            return plannedQty;
        }

        /**
         * @param plannedQty The plannedQty to set.
         */
        public void setPlannedQty(String plannedQty) {
            this.plannedQty = plannedQty;
        }

        /**
         * @return Returns the plannedQtyUom.
         */
        public String getPlannedQtyUom() {
            return plannedQtyUom;
        }

        /**
         * @param plannedQtyUom The plannedQtyUom to set.
         */
        public void setPlannedQtyUom(String plannedQtyUom) {
            this.plannedQtyUom = plannedQtyUom;
        }

        /**
         * @return Returns the fixedQty.
         */
        public String getFixedQty() {
            return fixedQty;
        }

        /**
         * @param fixedQty The fixedQty to set.
         */
        public void setFixedQty(String fixedQty) {
            this.fixedQty = fixedQty;
        }
    }
}
