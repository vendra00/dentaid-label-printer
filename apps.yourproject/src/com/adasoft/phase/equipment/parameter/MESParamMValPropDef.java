package com.adasoft.phase.equipment.parameter;

public class MESParamMValPropDef extends BulkSaveableMESATObject implements IMESProcessParameterData {
	
	protected static final String ATDEFINITION_NAME = "L_ParamMValPropDef0100";
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
        return "L_ParamMValPropDef0100";
    }

    public MESParamMValPropDef(long key) {
        super(key);
    }

    public MESParamMValPropDef(MESParamMValPropDef source) {
        super(source);
    }

    public MESParamMValPropDef(ATRow baseATRow) {
        super(baseATRow);
    }

    public MESParamMValPropDef() {
    }

    protected void synchronizeAfterATRowRefresh() {
        super.synchronizeAfterATRowRefresh();
    }

    public IMeasuredValue getDefaultValue() {
        return (IMeasuredValue)this.dgtATRow.getValue("L_DefaultValue");
    }

    public void setDefaultValue(IMeasuredValue value) {
        IMeasuredValue oldValue = this.getDefaultValue();
        this.dgtATRow.setValue("L_DefaultValue", value);
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

    public IMeasuredValue getLowerLimit() {
        return (IMeasuredValue)this.dgtATRow.getValue("L_LowerLimit");
    }

    public void setLowerLimit(IMeasuredValue value) {
        IMeasuredValue oldValue = this.getLowerLimit();
        this.dgtATRow.setValue("L_LowerLimit", value);
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

    public IMeasuredValue getUpperLimit() {
        return (IMeasuredValue)this.dgtATRow.getValue("L_UpperLimit");
    }

    public void setUpperLimit(IMeasuredValue value) {
        IMeasuredValue oldValue = this.getUpperLimit();
        this.dgtATRow.setValue("L_UpperLimit", value);
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
