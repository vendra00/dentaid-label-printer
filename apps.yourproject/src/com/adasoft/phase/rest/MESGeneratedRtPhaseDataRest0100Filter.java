package com.adasoft.phase.rest;

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



/**
 * Generated class definition
 */
public abstract class MESGeneratedRtPhaseDataRest0100Filter extends MESATObjectFilter  {

    /** Generated attribute definition */
    private static final long serialVersionUID = 1L;

    /** Generated attribute definition */
    private static final String ATDEFINITION_NAME = "AD_PhDatRest0100";

    /**
     * Generated method definition
     *
     * @param server The Server object
     */
    public MESGeneratedRtPhaseDataRest0100Filter(Server server) {
        super(server, ATDEFINITION_NAME);
    }

    /**
     * Generated method definition
     *
     */
    public MESGeneratedRtPhaseDataRest0100Filter() {
        super(PCContext.getServerImpl(), ATDEFINITION_NAME);
    }

    /**
     * Generated method definition
     *
     * @return the list of the objects
     */
    @Override     
    public List<MESRtPhaseDataRest0100> getFilteredObjects () {
        return MESATObject.getFilteredMESATObjectList(this, MESRtPhaseDataRest0100.class);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataRest0100Filter forParentEqualTo(IMESRtPhase value) //
            throws DatasweepException {
        String columnName = MESRtPhaseDataRest0100.COL_NAME_PARENT;
        return (MESRtPhaseDataRest0100Filter) forColumnNameEqualTo(columnName, Long.valueOf(value.getKey()));
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataRest0100Filter forParentNotEqualTo(IMESRtPhase value) //
            throws DatasweepException {
        String columnName = MESRtPhaseDataRest0100.COL_NAME_PARENT;
        return (MESRtPhaseDataRest0100Filter) forColumnNameNotEqualTo(columnName, Long.valueOf(value.getKey()));
    }



    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataRest0100Filter forUrlEqualTo(String value) //
            throws DatasweepException {
        return (MESRtPhaseDataRest0100Filter) forColumnNameEqualTo("AD_url", value);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataRest0100Filter forUrlNotEqualTo(String value) //
            throws DatasweepException {
        return (MESRtPhaseDataRest0100Filter) forColumnNameNotEqualTo("AD_url", value);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataRest0100Filter forUrlContaining(String value) //
            throws DatasweepException {
        return (MESRtPhaseDataRest0100Filter) forColumnNameContaining("AD_url", value);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException the PC exception
     */
    public MESRtPhaseDataRest0100Filter forUrlStartingWith(String value) //
            throws DatasweepException {
        return (MESRtPhaseDataRest0100Filter) forColumnNameStartingWith("AD_url", value);
    }

}
