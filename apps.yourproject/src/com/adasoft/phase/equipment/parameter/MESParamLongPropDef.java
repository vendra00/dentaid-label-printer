package com.adasoft.phase.equipment.parameter;


import com.datasweep.compatibility.client.ATRow;
import com.datasweep.compatibility.client.Response;
import com.rockwell.mes.commons.base.ifc.objects.BulkSaveableMESATObject;
import com.rockwell.mes.services.s88.ifc.recipe.IMESProcessParameterData;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class MESParamLongPropDef extends BulkSaveableMESATObject implements IMESProcessParameterData {

	protected static final String ATDEFINITION_NAME = "L_ParamLongPropDef0100";
    public static final String PROP_NAME_DEFAULTVALUE = "defaultValue";
    public static final String DB_COL_NAME_DEFAULTVALUE = "L_DefaultValue_I";
    public static final String COL_NAME_DEFAULTVALUE = "L_DefaultValue";
    public static final String PROP_NAME_LOWERLIMIT = "lowerLimit";
    public static final String DB_COL_NAME_LOWERLIMIT = "L_LowerLimit_I";
    public static final String COL_NAME_LOWERLIMIT = "L_LowerLimit";
    public static final String PROP_NAME_PROPERTY = "property";
    public static final String DB_COL_NAME_PROPERTY = "L_property_S";
    public static final String COL_NAME_PROPERTY = "L_property";
    public static final String PROP_NAME_UPPERLIMIT = "upperLimit";
    public static final String DB_COL_NAME_UPPERLIMIT = "L_UpperLimit_I";
    public static final String COL_NAME_UPPERLIMIT = "L_UpperLimit";
    public static final String PROP_NAME_VALUEEDITABLE = "valueEditable";
    public static final String DB_COL_NAME_VALUEEDITABLE = "L_ValueEditable_Y";
    public static final String COL_NAME_VALUEEDITABLE = "L_ValueEditable";

    public String getATDefinitionName() {
        return "L_ParamLongPropDef0100";
    }

    public MESParamLongPropDef(long key) {
        super(key);
    }

    public MESParamLongPropDef(MESParamLongPropDef source) {
        super(source);
    }

    public MESParamLongPropDef(ATRow baseATRow) {
        super(baseATRow);
    }

    public MESParamLongPropDef() {
    }

    protected void synchronizeAfterATRowRefresh() {
        super.synchronizeAfterATRowRefresh();
    }

    public Long getDefaultValue() {
        return (Long)this.dgtATRow.getValue("L_DefaultValue");
    }

    public void setDefaultValue(Long value) {
        Long oldValue = this.getDefaultValue();
        this.dgtATRow.setValue("L_DefaultValue", value);
        this.pcs.firePropertyChange("defaultValue", oldValue, value);
    }

    public Long getLowerLimit() {
        return (Long)this.dgtATRow.getValue("L_LowerLimit");
    }

    public void setLowerLimit(Long value) {
        Long oldValue = this.getLowerLimit();
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

    public Long getUpperLimit() {
        return (Long)this.dgtATRow.getValue("L_UpperLimit");
    }

    public void setUpperLimit(Long value) {
        Long oldValue = this.getUpperLimit();
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
