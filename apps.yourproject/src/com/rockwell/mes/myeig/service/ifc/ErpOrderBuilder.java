package com.rockwell.mes.myeig.service.ifc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.datasweep.compatibility.client.Batch;
import com.datasweep.compatibility.client.MasterRecipe;
import com.datasweep.compatibility.client.MeasuredValue;
import com.datasweep.compatibility.client.Part;

/**
 * Implements the builder pattern for {@code ProcessOrder} creation.
 * <p>
 * 
 * @author syim, (c) Copyright 2012 Rockwell Automation Technologies, Inc. All
 *         Rights Reserved.
 */
public class ErpOrderBuilder {

    /** The order number used for {@code ProcessOrder} creation. */
    private String orderNumber;

    /** The part (product) used for process {@code ProcessOrder} creation. */
    private Part part;

    /** The quantity (product) used for {@code ProcessOrder} creation. */
    private MeasuredValue quantity;

    /** The recipe (optional) used for {@code ProcessOrder} creation. */
    private MasterRecipe recipe;

    /** The produced batch */
    private String batch;
    
    /** The order type */
    private String orderType;

    /** The (optional) map of UDAs used for {@code ProcessOrder} creation. */
    private Map<String, Object> udaMap = Collections.EMPTY_MAP;

    /**
     * The (optional) list of map of components used for {@code ProcessOrder}
     * creation.
     */
    private List<ErpOrderItemBuilder> components = new ArrayList<ErpOrderItemBuilder>();

    /**
     * <b>Note:</b> Throws {@link IllegalArgumentException} if parameter
     * constraints are violated.
     * <p>
     * 
     * @param aOrderNo the order number, must not be {@code null}
     * @param aPart the part, must not be {@code null}
     * @param aQuantity the quantity, must not be {@code null}
     */
    public ErpOrderBuilder(String aOrderNo, Part aPart, MeasuredValue aQuantity) {
        if (aOrderNo == null) {
            throw new IllegalArgumentException("Order number must not be null");
        }
        if (aPart == null) {
            throw new IllegalArgumentException("Part must not be null");
        }
        if (aQuantity == null) {
            throw new IllegalArgumentException("Quantity must not be null");
        }
        part = aPart;
        orderNumber = aOrderNo;
        quantity = aQuantity;
    }

    /**
     * @return the orderNumber for {@code ProcessOrder} creation
     */
    public String getOrderNumber() {
        return orderNumber;
    }

    /**
     * @return the {@link Part} for {@code ProcessOrder} creation
     */
    public Part getPart() {
        return part;
    }

    /**
     * @return the quantity for {@code ProcessOrder} creation
     */
    public MeasuredValue getQuantity() {
        return quantity;
    }

    /**
     * Setter for optional argument <b>recipe</b>.
     * <p>
     * 
     * @param value the value to set
     * @return the modified {@link ErpOrderBuilder}
     */
    public ErpOrderBuilder recipe(MasterRecipe value) {
        recipe = value;
        return this;
    }

    /**
     * Getter for optional argument <b>recipe</b>.
     * <p>
     * 
     * @return the recipe for {@code ProcessOrder} creation
     */
    public MasterRecipe getRecipe() {
        return recipe;
    }

    /**
     * Setter for optional argument <b>batch</b>.
     * <p>
     * 
     * @param value the value to set
     * @return the modified {@link ErpOrderBuilder}
     */
    public ErpOrderBuilder batch(String value) {
        batch = value;
        return this;
    }

    /**
     * Getter for optional argument <b>batch</b>.
     * <p>
     * 
     * @return the batch for {@code ProcessOrder} creation
     */
    public String getBatch() {
        return batch;
    }

    /**
     * Setter for optional argument <b>orderType</b>.
     * <p>
     * 
     * @param value the value to set
     * @return the modified {@link ErpOrderBuilder}
     */
    public ErpOrderBuilder orderType(String value) {
        orderType = value;
        return this;
    }

    /**
     * Getter for optional argument <b>orderType</b>.
     * <p>
     * 
     * @return the orderType for {@code ProcessOrder} creation
     */
    public String getOrderType() {
        return orderType;
    }
    
    /**
     * Setter for optional argument <b>udaMap</b>.
     * <p>
     * 
     * @param value the value to set
     * @return the modified {@link ErpOrderBuilder}
     */
    public ErpOrderBuilder udaMap(Map<String, Object> value) {
        if (value == null) {
            udaMap = Collections.EMPTY_MAP;
        } else {
            udaMap = new HashMap(value);
        }
        return this;
    }

    /**
     * Getter for optional argument <b>udaMap</b>.
     * <p>
     * 
     * @return the UDA map for {@code ProcessOrder} creation
     */
    public Map<String, Object> getUdaMap() {
        return udaMap;
    }

    /**
     * Setter for optional argument <b>components</b>.
     * <p>
     * 
     * @param value the value to set
     * @return the modified {@link ErpOrderBuilder}
     */
    public ErpOrderBuilder components(List<ErpOrderItemBuilder> value) {
        if (value == null) {
            components = new ArrayList<ErpOrderItemBuilder>();
        } else {
            components = value;
        }
        return this;
    }

    /**
     * Getter for optional argument <b>components</b>.
     * <p>
     * 
     * @return the component map for {@code ProcessOrder} creation
     */
    public List<ErpOrderItemBuilder> getComponents() {
        return components;
    }

    /**
     * Implements the builder pattern for Order components
     * <p>
     * 
     * @author syim, (c) Copyright 2012 Rockwell Automation Technologies, Inc.
     *         All Rights Reserved.
     */
    public static class ErpOrderItemBuilder {

        /** The position of the component. */
        private String position;

        /** The part of the component. */
        private Part part;

        /** The quantity of the component. */
        private MeasuredValue quantity;

        /** The batch (optional) of the component. */
        private Batch batch;

        /** The (optional) map of UDAs used of the component. */
        private Map<String, Object> udaMap = Collections.EMPTY_MAP;

        /**
         * <b>Note:</b> Throws {@link IllegalArgumentException} if parameter
         * constraints are violated.
         * <p>
         * 
         * @param aPosition the position, must not be {@code null}
         * @param aPart the part, must not be {@code null}
         * @param aQuantity the quantity, must not be {@code null}
         */
        public ErpOrderItemBuilder(String aPosition, Part aPart, MeasuredValue aQuantity) {
            if (aPosition == null) {
                throw new IllegalArgumentException("Position must not be null");
            }
            if (aPart == null) {
                throw new IllegalArgumentException("Part must not be null");
            }
            if (aQuantity == null) {
                throw new IllegalArgumentException("Quantity must not be null");
            }
            part = aPart;
            position = aPosition;
            quantity = aQuantity;
        }

        /**
         * @return the position of the component
         */
        public String getPosition() {
            return position;
        }

        /**
         * @return the {@link Part} of the component
         */
        public Part getPart() {
            return part;
        }

        /**
         * @return the quantity of the component
         */
        public MeasuredValue getQuantity() {
            return quantity;
        }

        /**
         * Setter for optional argument <b>batch</b>.
         * <p>
         * 
         * @param value the value to set
         * @return the modified {@link ErpOrderItemBuilder}
         */
        public ErpOrderItemBuilder batch(Batch value) {
            batch = value;
            return this;
        }

        /**
         * Getter for optional argument <b>batch</b>.
         * <p>
         * 
         * @return the batch of the component
         */
        public Batch getBatch() {
            return batch;
        }

        /**
         * Setter for optional argument <b>udaMap</b>.
         * <p>
         * 
         * @param value the value to set
         * @return the modified {@link ErpOrderBuilder}
         */
        public ErpOrderItemBuilder udaMap(Map<String, Object> value) {
            if (value == null) {
                udaMap = Collections.EMPTY_MAP;
            } else {
                udaMap = new HashMap(value);
            }
            return this;
        }

        /**
         * Getter for optional argument <b>udaMap</b>.
         * <p>
         * 
         * @return the UDA map for {@code ProcessOrder} creation
         */
        public Map<String, Object> getUdaMap() {
            return udaMap;
        }

    }
}
