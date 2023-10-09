package com.adasoft.phase.equipment;

import com.rockwell.mes.services.s88equipment.ifc.IMESEquipmentProperty;

public class RuntimePropertyData0100 {
	private IMESEquipmentProperty property;
    private String propertyValueId;
    private Object propertyValue;
    
    public RuntimePropertyData0100(IMESEquipmentProperty eqmProp, String valueId, Object value) {
        this.property = eqmProp;
        this.propertyValueId = valueId;
        this.propertyValue = value;
    }
    
    public IMESEquipmentProperty getProperty() {
        return this.property;
    }
    
    public String getPropertyValueId() {
        return this.propertyValueId;
    }
    
    public Object getPropertyValue() {
        return this.propertyValue;
    }
}
