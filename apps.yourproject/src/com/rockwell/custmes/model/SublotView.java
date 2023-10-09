package com.rockwell.custmes.model;

import com.datasweep.compatibility.client.MeasuredValue;
import com.datasweep.compatibility.client.Sublot;
import com.datasweep.plantops.common.constants.IObjectTypes;
import com.datasweep.plantops.common.utility.IDBSchema;
import com.rockwell.custmes.annotations.Column;
import com.rockwell.custmes.annotations.Id;
import com.rockwell.custmes.annotations.JoinCondition;
import com.rockwell.custmes.annotations.JoinTable;
import com.rockwell.custmes.annotations.ObjectType;
import com.rockwell.custmes.annotations.State;
import com.rockwell.custmes.annotations.Table;
import com.rockwell.mes.services.inventory.ifc.AbstractBatchQualityTransitionEventListener;

/**
 * View class for sublots for performance optimization.
 * 
 * @author rweinga
 */
@Table(name = IDBSchema.SUBLOT_TABLE_NAME)
@ObjectType(type = Sublot.class)
public class SublotView implements KeyedObject {
    private long key;

    private String name;

    private MeasuredValue quantity;

    private String partNumber;

    private String partDescription;

    private String batchName;

    private String batchState;

    private String locationName;

    private String storageAreaName;

    private long carrierKey;

    private String processStatus1;

    /**
     * @return the key
     */
    @Id
    @Column(name = IDBSchema.SUBLOT_SUBLOT_KEY)
    @Override
    public long getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    @Override
    public void setKey(long key) {
        this.key = key;
    }

    /**
     * @return the name
     */
    @Column(name = IDBSchema.SUBLOT_SUBLOT_NAME)
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the state
     */
    public String getState() {
        return getBatchState(); // sublot has no state anymore since PS 4.0
    }

    /**
     * @param state the state to set
     */
    public void setState(String state) {
        // do nothing, sublot has no state anymore since PS 4.0
    }

    /**
     * @return Returns the processStatus1.
     */
    @State(fsmName = "ProcessStatus1", keyName = IDBSchema.SUBLOT_SUBLOT_KEY, type = IObjectTypes.TYPE_SUBLOT)
    public String getProcessStatus1() {
        return processStatus1;
    }

    /**
     * @param processStatus1 The processStatus1 to set.
     */
    public void setProcessStatus1(String processStatus1) {
        this.processStatus1 = processStatus1;
    }

    /**
     * @return the quantity
     */
    @Column(name = IDBSchema.SUBLOT_QUANTITY)
    public MeasuredValue getQuantity() {
        return quantity;
    }

    /**
     * @param quantity the quantity to set
     */
    public void setQuantity(MeasuredValue quantity) {
        this.quantity = quantity;
    }

    /**
     * @return the batchName
     */
    @Column(name = IDBSchema.BATCH_BATCH_NAME)
    @JoinTable(name = IDBSchema.BATCH_TABLE_NAME)
    @JoinCondition(joinColumn = IDBSchema.BATCH_BATCH_KEY)
    public String getBatchName() {
        return batchName;
    }

    /**
     * @param batchName the batchName to set
     */
    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    /**
     * @return the batchState
     */
    @JoinTable(name = IDBSchema.BATCH_TABLE_NAME)
    @State(fsmName = AbstractBatchQualityTransitionEventListener.FSM_REL_SHIP, keyName = IDBSchema.BATCH_BATCH_KEY, type = IObjectTypes.TYPE_BATCH)
    public String getBatchState() {
        return batchState;
    }

    /**
     * @param batchState the batchState to set
     */
    public void setBatchState(String batchState) {
        this.batchState = batchState;
    }

    /**
     * @return the partName
     */
    @Column(name = IDBSchema.PART_PART_NUMBER)
    @JoinTable(name = IDBSchema.PART_TABLE_NAME)
    @JoinCondition(joinColumn = IDBSchema.PART_PART_KEY, table = IDBSchema.BATCH_TABLE_NAME)
    public String getPartNumber() {
        return partNumber;
    }

    /**
     * @param partName the partName to set
     */
    public void setPartNumber(String partName) {
        this.partNumber = partName;
    }

    /**
     * @return the partDescription
     */
    @Column(name = IDBSchema.PART_DESCRIPTION)
    @JoinTable(name = IDBSchema.PART_TABLE_NAME)
    public String getPartDescription() {
        return partDescription;
    }

    /**
     * @param partDescription the partDescription to set
     */
    public void setPartDescription(String partDescription) {
        this.partDescription = partDescription;
    }

    @Column(name = IDBSchema.CARRIER_CARRIER_KEY)
    @JoinTable(name = IDBSchema.CARRIER_TABLE_NAME)
    @JoinCondition(joinColumn = IDBSchema.CARRIER_CARRIER_KEY)
    public long getCarrierKey() {
        return carrierKey;
    }

    public void setCarrierKey(long carrierKey) {
        this.carrierKey = carrierKey;
    }

    /**
     * @return the locationName
     */
    @Column(name = IDBSchema.LOCATION_LOCATION)
    @JoinTable(name = IDBSchema.LOCATION_TABLE_NAME)
    @JoinCondition(joinColumn = IDBSchema.LOCATION_LOCATION, table = IDBSchema.CARRIER_TABLE_NAME)
    public String getLocationName() {
        return locationName;
    }

    /**
     * @param locationName the locationName to set
     */
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    /**
     * @return the storageAreaName
     */
    @Column(name = IDBSchema.LOCATION_LOCATION)
    @JoinTable(name = IDBSchema.LOCATION_TABLE_NAME, alias = "SA")
    @JoinCondition(joinColumn = IDBSchema.LOCATION_LOCATION_KEY, table = IDBSchema.LOCATION_TABLE_NAME, column = IDBSchema.LOCATION_PARENT_LOCATION_KEY)
    public String getStorageAreaName() {
        return storageAreaName;
    }

    /**
     * @param storageAreaName the storageAreaName to set
     */
    public void setStorageAreaName(String storageAreaName) {
        this.storageAreaName = storageAreaName;
    }
}
