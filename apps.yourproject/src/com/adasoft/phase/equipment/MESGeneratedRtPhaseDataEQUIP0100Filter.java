package com.adasoft.phase.equipment;

/**
 * This file is generated by the PhaseLibManager
 *
 * Please do not modify this file manually !!
 */
import java.util.List;

import com.datasweep.compatibility.client.DatasweepException;
import com.datasweep.compatibility.client.Server;
import com.rockwell.mes.commons.base.ifc.objects.MESATObject;
import com.rockwell.mes.commons.base.ifc.objects.MESATObjectFilter;
import com.rockwell.mes.commons.base.ifc.services.PCContext;
import com.rockwell.mes.services.s88.ifc.execution.IMESRtPhase;

import java.math.BigDecimal;
import com.datasweep.compatibility.client.MeasuredValue;
import com.rockwell.mes.commons.base.ifc.functional.MeasuredValueUtilities;
import com.rockwell.mes.commons.base.ifc.functional.MESDuration;
import com.datasweep.compatibility.ui.Time;


/**
 * Generated class definition
 */
public abstract class MESGeneratedRtPhaseDataEQUIP0100Filter extends MESATObjectFilter  {

    /** Generated attribute definition */
    private static final long serialVersionUID = 1L;

    /** Generated attribute definition */
    private static final String ATDEFINITION_NAME = "AD_PhDatEQUIP0100";

    /**
     * Generated method definition
     *
     * @param server The Server object
     */
    public MESGeneratedRtPhaseDataEQUIP0100Filter(Server server) {
        super(server, ATDEFINITION_NAME);
    }

    /**
     * Generated method definition
     *
     */
    public MESGeneratedRtPhaseDataEQUIP0100Filter() {
        super(PCContext.getServerImpl(), ATDEFINITION_NAME);
    }

    /**
     * Generated method definition
     *
     * @return the list of the objects
     */
    @Override     
    public List<MESRtPhaseDataEQUIP0100> getFilteredObjects () {
        return MESATObject.getFilteredMESATObjectList(this, MESRtPhaseDataEQUIP0100.class);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forParentEqualTo(IMESRtPhase value) //
            throws DatasweepException {
        String columnName = MESRtPhaseDataEQUIP0100.COL_NAME_PARENT;
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameEqualTo(columnName, Long.valueOf(value.getKey()));
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forParentNotEqualTo(IMESRtPhase value) //
            throws DatasweepException {
        String columnName = MESRtPhaseDataEQUIP0100.COL_NAME_PARENT;
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameNotEqualTo(columnName, Long.valueOf(value.getKey()));
    }



    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forEquipEntityIdEqualTo(String value) //
            throws DatasweepException {
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameEqualTo("AD_EquipEntityId", value);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forEquipEntityIdNotEqualTo(String value) //
            throws DatasweepException {
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameNotEqualTo("AD_EquipEntityId", value);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forEquipEntityIdContaining(String value) //
            throws DatasweepException {
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameContaining("AD_EquipEntityId", value);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forEquipEntityIdStartingWith(String value) //
            throws DatasweepException {
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameStartingWith("AD_EquipEntityId", value);
    }


    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forBundleIdEqualTo(String value) //
            throws DatasweepException {
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameEqualTo("AD_BundleId", value);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forBundleIdNotEqualTo(String value) //
            throws DatasweepException {
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameNotEqualTo("AD_BundleId", value);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forBundleIdContaining(String value) //
            throws DatasweepException {
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameContaining("AD_BundleId", value);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forBundleIdStartingWith(String value) //
            throws DatasweepException {
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameStartingWith("AD_BundleId", value);
    }


    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forPropertyEqualTo(String value) //
            throws DatasweepException {
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameEqualTo("AD_Property", value);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forPropertyNotEqualTo(String value) //
            throws DatasweepException {
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameNotEqualTo("AD_Property", value);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forPropertyContaining(String value) //
            throws DatasweepException {
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameContaining("AD_Property", value);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forPropertyStartingWith(String value) //
            throws DatasweepException {
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameStartingWith("AD_Property", value);
    }


    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forDecimalValueEqualTo(BigDecimal value) //
            throws DatasweepException {
        MeasuredValue mv = value == null ? null : MeasuredValueUtilities.createMV(value, "");
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameEqualTo("AD_DecimalValue", mv);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forDecimalValueNotEqualTo(BigDecimal value) //
            throws DatasweepException {
        MeasuredValue mv = value == null ? null : MeasuredValueUtilities.createMV(value, "");
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameNotEqualTo("AD_DecimalValue", mv);
    }


    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forLongValueEqualTo(Long value) //
            throws DatasweepException {
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameEqualTo("AD_LongValue", value);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forLongValueNotEqualTo(Long value) //
            throws DatasweepException {
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameNotEqualTo("AD_LongValue", value);
    }


    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forMeasuredValueValueEqualTo(MeasuredValue value) //
            throws DatasweepException {
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameEqualTo("AD_MeasuredValueValue", value);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forMeasuredValueValueNotEqualTo(MeasuredValue value) //
            throws DatasweepException {
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameNotEqualTo("AD_MeasuredValueValue", value);
    }


    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forBooleanValueEqualTo(Boolean value) //
            throws DatasweepException {
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameEqualTo("AD_BooleanValue", value);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forBooleanValueNotEqualTo(Boolean value) //
            throws DatasweepException {
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameNotEqualTo("AD_BooleanValue", value);
    }


    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forStringValueEqualTo(String value) //
            throws DatasweepException {
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameEqualTo("AD_StringValue", value);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forStringValueNotEqualTo(String value) //
            throws DatasweepException {
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameNotEqualTo("AD_StringValue", value);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forStringValueContaining(String value) //
            throws DatasweepException {
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameContaining("AD_StringValue", value);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forStringValueStartingWith(String value) //
            throws DatasweepException {
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameStartingWith("AD_StringValue", value);
    }


    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forDurationValueEqualTo(MESDuration value) //
            throws DatasweepException {
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameEqualTo("AD_DurationValue", value);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forDurationValueNotEqualTo(MESDuration value) //
            throws DatasweepException {
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameNotEqualTo("AD_DurationValue", value);
    }


    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forDateTimeValueEqualTo(Time value) //
            throws DatasweepException {
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameEqualTo("AD_DateTimeValue", value);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forDateTimeValueNotEqualTo(Time value) //
            throws DatasweepException {
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameNotEqualTo("AD_DateTimeValue", value);
    }


    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forDateTimeFormatEqualTo(String value) //
            throws DatasweepException {
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameEqualTo("AD_DateTimeFormat", value);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forDateTimeFormatNotEqualTo(String value) //
            throws DatasweepException {
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameNotEqualTo("AD_DateTimeFormat", value);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forDateTimeFormatContaining(String value) //
            throws DatasweepException {
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameContaining("AD_DateTimeFormat", value);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataEQUIP0100Filter forDateTimeFormatStartingWith(String value) //
            throws DatasweepException {
        return (MESRtPhaseDataEQUIP0100Filter) forColumnNameStartingWith("AD_DateTimeFormat", value);
    }

}
