package com.adasoft.phase.equipment.parameter;

import com.rockwell.mes.commons.base.ifc.objects.BulkSaveableMESATObject;
import com.rockwell.mes.services.s88.ifc.recipe.IMESProcessParameterData;

import com.datasweep.compatibility.client.ATRow;
import com.datasweep.compatibility.client.Response;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class MESParamMulPropsHide extends BulkSaveableMESATObject implements IMESProcessParameterData{

	protected static final String ATDEFINITION_NAME = "L_ParamMulPropsHide0100";
    public static final String PROP_NAME_MAKEPHASEHIDDEN = "makePhaseHidden";
    public static final String DB_COL_NAME_MAKEPHASEHIDDEN = "L_MakePhaseHidden_Y";
    public static final String COL_NAME_MAKEPHASEHIDDEN = "L_MakePhaseHidden";
    
    public String getATDefinitionName() {
        return "L_ParamMulPropsHide0100";
    }
    
    public MESParamMulPropsHide(long key) {
        super(key);
    }

    public MESParamMulPropsHide(MESParamMulPropsHide source) {
        super(source);
    }

    public MESParamMulPropsHide(ATRow baseATRow) {
        super(baseATRow);
    }

    public MESParamMulPropsHide() {
    }

    protected void synchronizeAfterATRowRefresh() {
        super.synchronizeAfterATRowRefresh();
    }

    public Boolean getMakePhaseHidden() {
        return (Boolean)this.dgtATRow.getValue("L_MakePhaseHidden");
    }

    public void setMakePhaseHidden(Boolean value) {
        Boolean oldValue = this.getMakePhaseHidden();
        this.dgtATRow.setValue("L_MakePhaseHidden", value);
        this.pcs.firePropertyChange("makePhaseHidden", oldValue, value);
    }

    public Response prepareATRowForSave(boolean isBulkSave) {
        Response res = super.prepareATRowForSave(isBulkSave);
        return res;
    }

    public String getDataAsString() {
        List<Object> result = new ArrayList<Object>();
        result.add(this.getMakePhaseHidden());
        return StringUtils.join(result, ';');
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
