package com.adasoft.phase.equipment.parameter;

import com.rockwell.ssb.ftps.parameter.eqm.IRuntimePropertyValueConfig0100;

public class MESParamDecimPropDef0100 extends MESParamDecimPropDef implements IRuntimePropertyValueConfig0100{
	
	public MESParamDecimPropDef0100(long key) {
        super(key);
    }

    public MESParamDecimPropDef0100(MESParamDecimPropDef0100 source) {
        super(source);
    }

    public MESParamDecimPropDef0100(ATRow baseATRow) {
        super(baseATRow);
    }

    public MESParamDecimPropDef0100() {
    }

    public RuntimePropertyLimitConfig0100<BigDecimal> getLimitConfiguration() {
        return new RuntimePropertyLimitConfig0100(this.getLowerLimit(), this.getUpperLimit());
    }

    public BigDecimal getRuntimeValue() {
        return this.getDefaultValue();
    }

    public String getPropertyReference() {
        return (String)this.dgtATRow.getValue("L_property");
    }
}
