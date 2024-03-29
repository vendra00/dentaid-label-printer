package com.adasoft.gv.phase.product.labelgenerator;

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


/**
 * Generated class definition
 */
public abstract class MESGeneratedRtPhaseDataLabelGen0100Filter extends MESATObjectFilter  {

    /** Generated attribute definition */
    private static final long serialVersionUID = 1L;

    /** Generated attribute definition */
    private static final String ATDEFINITION_NAME = "GVPhDatLabelGen0100";

    /**
     * Generated method definition
     *
     * @param server The Server object
     */
    public MESGeneratedRtPhaseDataLabelGen0100Filter(Server server) {
        super(server, ATDEFINITION_NAME);
    }

    /**
     * Generated method definition
     *
     */
    public MESGeneratedRtPhaseDataLabelGen0100Filter() {
        super(PCContext.getServerImpl(), ATDEFINITION_NAME);
    }

    /**
     * Generated method definition
     *
     * @return the list of the objects
     */
    @Override     
    public List<MESRtPhaseDataLabelGen0100> getFilteredObjects () {
        return MESATObject.getFilteredMESATObjectList(this, MESRtPhaseDataLabelGen0100.class);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataLabelGen0100Filter forParentEqualTo(IMESRtPhase value) //
            throws DatasweepException {
        String columnName = MESRtPhaseDataLabelGen0100.COL_NAME_PARENT;
        return (MESRtPhaseDataLabelGen0100Filter) forColumnNameEqualTo(columnName, Long.valueOf(value.getKey()));
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataLabelGen0100Filter forParentNotEqualTo(IMESRtPhase value) //
            throws DatasweepException {
        String columnName = MESRtPhaseDataLabelGen0100.COL_NAME_PARENT;
        return (MESRtPhaseDataLabelGen0100Filter) forColumnNameNotEqualTo(columnName, Long.valueOf(value.getKey()));
    }



    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataLabelGen0100Filter forNameEqualTo(String value) //
            throws DatasweepException {
        return (MESRtPhaseDataLabelGen0100Filter) forColumnNameEqualTo("GVName", value);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataLabelGen0100Filter forNameNotEqualTo(String value) //
            throws DatasweepException {
        return (MESRtPhaseDataLabelGen0100Filter) forColumnNameNotEqualTo("GVName", value);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataLabelGen0100Filter forNameContaining(String value) //
            throws DatasweepException {
        return (MESRtPhaseDataLabelGen0100Filter) forColumnNameContaining("GVName", value);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataLabelGen0100Filter forNameStartingWith(String value) //
            throws DatasweepException {
        return (MESRtPhaseDataLabelGen0100Filter) forColumnNameStartingWith("GVName", value);
    }


    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataLabelGen0100Filter forDescriptionEqualTo(String value) //
            throws DatasweepException {
        return (MESRtPhaseDataLabelGen0100Filter) forColumnNameEqualTo("GVDescription", value);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataLabelGen0100Filter forDescriptionNotEqualTo(String value) //
            throws DatasweepException {
        return (MESRtPhaseDataLabelGen0100Filter) forColumnNameNotEqualTo("GVDescription", value);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataLabelGen0100Filter forDescriptionContaining(String value) //
            throws DatasweepException {
        return (MESRtPhaseDataLabelGen0100Filter) forColumnNameContaining("GVDescription", value);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataLabelGen0100Filter forDescriptionStartingWith(String value) //
            throws DatasweepException {
        return (MESRtPhaseDataLabelGen0100Filter) forColumnNameStartingWith("GVDescription", value);
    }


    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataLabelGen0100Filter forValueEqualTo(BigDecimal value) //
            throws DatasweepException {
        return (MESRtPhaseDataLabelGen0100Filter) forColumnNameEqualTo("GVValue", value);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataLabelGen0100Filter forValueNotEqualTo(BigDecimal value) //
            throws DatasweepException {
        return (MESRtPhaseDataLabelGen0100Filter) forColumnNameNotEqualTo("GVValue", value);
    }

}
