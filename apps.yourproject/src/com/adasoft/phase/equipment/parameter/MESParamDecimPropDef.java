package com.adasoft.phase.equipment.parameter;


import com.datasweep.compatibility.client.ATRow;
import com.datasweep.compatibility.client.MeasuredValue;
import com.datasweep.compatibility.client.Response;
import com.rockwell.mes.commons.base.ifc.functional.MeasuredValueUtilities;
import com.rockwell.mes.commons.base.ifc.objects.BulkSaveableMESATObject;
import com.rockwell.mes.services.s88.ifc.recipe.IMESProcessParameterData;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class MESParamDecimPropDef extends BulkSaveableMESATObject implements IMESProcessParameterData{
	protected static final String ATDEFINITION_NAME = "L_ParamDecimPropDef0100";
    public static final String PROP_NAME_DEFAULTVALUE = "defaultValue";
    public static final String DB_COL_NAME_DEFAULTVALUE = "L_DefaultValue_V";
    public static final String COL_NAME_DEFAULTVALUE = "L_DefaultValue";
    public static final String PROP_NAME_FRACTIONALPLACES = "fractionalPlaces";
    public static final String DB_COL_NAME_FRACTIONALPLACES = "L_FractionalPlaces_I";
    public static final String COL_NAME_FRACTIONALPLACES = "L_FractionalPlaces";
    public static final String PROP_NAME_INTEGRALPLACES = "integralPlaces";
    public static final String DB_COL_NAME_INTEGRALPLACES = "L_IntegralPlaces_I";
    public static final String COL_NAME_INTEGRALPLACES = "L_IntegralPlaces";
    public static final String PROP_NAME_LOWERLIMIT = "lowerLimit";
    public static final String DB_COL_NAME_LOWERLIMIT = "L_LowerLimit_V";
    public static final String COL_NAME_LOWERLIMIT = "L_LowerLimit";
    public static final String PROP_NAME_PROPERTY = "property";
    public static final String DB_COL_NAME_PROPERTY = "L_property_S";
    public static final String COL_NAME_PROPERTY = "L_property";
    public static final String PROP_NAME_UPPERLIMIT = "upperLimit";
    public static final String DB_COL_NAME_UPPERLIMIT = "L_UpperLimit_V";
    public static final String COL_NAME_UPPERLIMIT = "L_UpperLimit";
    public static final String PROP_NAME_VALUEEDITABLE = "valueEditable";
    public static final String DB_COL_NAME_VALUEEDITABLE = "L_ValueEditable_Y";
    public static final String COL_NAME_VALUEEDITABLE = "L_ValueEditable";

    public String getATDefinitionName() {
        return "L_ParamDecimPropDef0100";
    }

    public MESParamDecimPropDef(long key) {
        super(key);
    }

    public MESParamDecimPropDef(MESParamDecimPropDef source) {
        super(source);
    }

    public MESParamDecimPropDef(ATRow baseATRow) {
        super(baseATRow);
    }

    public MESParamDecimPropDef() {
    }

    protected void synchronizeAfterATRowRefresh() {
        super.synchronizeAfterATRowRefresh();
    }

    public BigDecimal getDefaultValue() {
        MeasuredValue mv = (MeasuredValue)this.dgtATRow.getValue("L_DefaultValue");
        return mv == null ? null : mv.getValue();
    }

    public void setDefaultValue(BigDecimal value) {
        BigDecimal oldValue = this.getDefaultValue();
        MeasuredValue mv = value == null ? null : MeasuredValueUtilities.createMV(value, "");
        this.dgtATRow.setValue("L_DefaultValue", mv);
        this.pcs.firePropertyChange("defaultValue", oldValue, value);
    }

    public Long getFractionalPlaces() {
        return (Long)this.dgtATRow.getValue("L_FractionalPlaces");
    }

    public void setFractionalPlaces(Long value) {
        Long oldValue = this.getFractionalPlaces();
        this.dgtATRow.setValue("L_FractionalPlaces", value);
        this.pcs.firePropertyChange("fractionalPlaces", oldValue, value);
    }

    public Long getIntegralPlaces() {
        return (Long)this.dgtATRow.getValue("L_IntegralPlaces");
    }

    public void setIntegralPlaces(Long value) {
        Long oldValue = this.getIntegralPlaces();
        this.dgtATRow.setValue("L_IntegralPlaces", value);
        this.pcs.firePropertyChange("integralPlaces", oldValue, value);
    }

    public BigDecimal getLowerLimit() {
        MeasuredValue mv = (MeasuredValue)this.dgtATRow.getValue("L_LowerLimit");
        return mv == null ? null : mv.getValue();
    }

    public void setLowerLimit(BigDecimal value) {
        BigDecimal oldValue = this.getLowerLimit();
        MeasuredValue mv = value == null ? null : MeasuredValueUtilities.createMV(value, "");
        this.dgtATRow.setValue("L_LowerLimit", mv);
        this.pcs.firePropertyChange("lowerLimit", oldValue, value);
    }

    public String getProperty() {
        return (String)this.dgtATRow.getValue("L_property");
    }

    public void setProperty(String value) {
        String oldValue = this.getProperty();
        this.dgtATRow.setValue("L_property", value);
        this.pcs.firePropertyChange("property", oldValue, value);
    }

    public BigDecimal getUpperLimit() {
        MeasuredValue mv = (MeasuredValue)this.dgtATRow.getValue("L_UpperLimit");
        return mv == null ? null : mv.getValue();
    }

    public void setUpperLimit(BigDecimal value) {
        BigDecimal oldValue = this.getUpperLimit();
        MeasuredValue mv = value == null ? null : MeasuredValueUtilities.createMV(value, "");
        this.dgtATRow.setValue("L_UpperLimit", mv);
        this.pcs.firePropertyChange("upperLimit", oldValue, value);
    }

    public Boolean getValueEditable() {
        return (Boolean)this.dgtATRow.getValue("L_ValueEditable");
    }

    public void setValueEditable(Boolean value) {
        Boolean oldValue = this.getValueEditable();
        this.dgtATRow.setValue("L_ValueEditable", value);
        this.pcs.firePropertyChange("valueEditable", oldValue, value);
    }

    public Response prepareATRowForSave(boolean isBulkSave) {
        Response res = super.prepareATRowForSave(isBulkSave);
        return res;
    }

    public String getDataAsString() {
        List<Object> result = new ArrayList();
        result.add(this.getDefaultValue());
        result.add(this.getFractionalPlaces());
        result.add(this.getIntegralPlaces());
        result.add(this.getLowerLimit());
        result.add(this.getProperty());
        result.add(this.getUpperLimit());
        result.add(this.getValueEditable());
        return StringUtils.join(result, ';');
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
