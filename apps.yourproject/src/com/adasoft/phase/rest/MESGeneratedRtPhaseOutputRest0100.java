package com.adasoft.phase.rest;

/**
 * This file is generated by the PhaseGenerator
 *
 * Please do not modify this file manually !!
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.datasweep.compatibility.client.ATRow;
import com.datasweep.compatibility.client.Response;
import com.rockwell.mes.commons.base.ifc.utility.ObjectFactory;
import com.rockwell.mes.services.s88.ifc.library.IBuildingBlockOutputDescriptor;
import com.rockwell.mes.services.s88.ifc.processdata.MESRtPhaseOutput;

import com.rockwell.mes.commons.base.ifc.utility.StringUtilsEx;

 /**
 * Generated class definition
 * <br/>Application table: AD_PhOutRest0100
 */
public abstract class MESGeneratedRtPhaseOutputRest0100
 extends MESRtPhaseOutput {

    /** Generated attribute definition */
    protected static final String ATDEFINITION_NAME = "AD_PhOutRest0100";


    @Override
    public String getATDefinitionName() {
        return ATDEFINITION_NAME;
    }

    /**
     * Generated constructor
     *
     * @param key The key of the ATRow to load.
     */
    public MESGeneratedRtPhaseOutputRest0100(long key) {
        super(key);
    }

    /**
     * Generated copy constructor
     *
     * @param source the source to copy.
     */
    public MESGeneratedRtPhaseOutputRest0100(MESGeneratedRtPhaseOutputRest0100 source) {
        super(source);
    }

    /**
     * Generated constructor
     *
     * @param baseATRow The ATRow to wrap.
     */
    public MESGeneratedRtPhaseOutputRest0100(ATRow baseATRow) {
        super(baseATRow);
    }

    /**
     * Generated constructor
     */
    public MESGeneratedRtPhaseOutputRest0100() {
        super();
    }

    @Override
    protected void synchronizeAfterATRowRefresh() {
        super.synchronizeAfterATRowRefresh();
    }    
    
    /**
     * Generated method definition
     *
     * @return the requested value
     */
    public String getRequest() {
        return StringUtilsEx.decodeStringForUI((String) this.dgtATRow.getValue("AD_request"));
    }

    /**
     * Generated method definition
     *
     * @param value The new value to be assigned
     */
    public void setRequest(String value) {
        String oldValue = this.getRequest();
        this.dgtATRow.setValue("AD_request", StringUtilsEx.encodeStringForDB(value));
        pcs.firePropertyChange("request", oldValue, value);
    }
    
    @Override
    protected Response prepareATRowForSave() {
        // Check if transient references are valid and store the corresponding keys in the ATRow:
        Response res = super.prepareATRowForSave();

        return res;
    }

    /** output descriptors */
    private static final List<IBuildingBlockOutputDescriptor> OUTPUT_DESCRIPTORS =
            new ArrayList<IBuildingBlockOutputDescriptor>();

    /**
     * Initializes the output descriptors.
     */
    static {
        IBuildingBlockOutputDescriptor descriptor;

        descriptor = ObjectFactory.getInstance().createObject(IBuildingBlockOutputDescriptor.class,
                           new Class[] { String.class, String.class, Class.class },
                           new Object[] { "request", "Request", String.class }
                          );
        OUTPUT_DESCRIPTORS.add(descriptor);
    }

    /**
     * Retrieves the output descriptors of this class.
     *  
     * @return unmodifiable list of output descriptors
     */
    public static List<IBuildingBlockOutputDescriptor> getOutputDescriptorsOfClass() {
        return Collections.unmodifiableList(OUTPUT_DESCRIPTORS);
    }
    
    @Override
    public List<IBuildingBlockOutputDescriptor> getOutputDescriptors() {
        return getOutputDescriptorsOfClass();
    }
    
}
