package com.adasoft.phase.label_printer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import com.adasoft.phase.label_printer.model.LabelKeyValues;
import com.adasoft.phase.label_printer.model.Request;
import com.adasoft.phase.label_printer.model.RequestParameters;
import com.adasoft.phase.label_printer.service.RequestService;
import com.adasoft.phase.label_printer.service.RequestServiceImpl;
import com.adasoft.phase.rest.model.RequestType;
import com.datasweep.compatibility.client.ActivitySetStep;
import com.rockwell.mes.apps.ebr.ifc.phase.IPhaseCompleter;
import com.rockwell.mes.commons.deviation.ifc.IESignatureExecutor;
import com.rockwell.mes.commons.deviation.ifc.exceptionrecording.IMESExceptionRecord;
import com.rockwell.mes.commons.shared.phase.mvc.AbstractPhaseExecutor0200;
import com.rockwell.mes.services.s88.ifc.execution.IMESRtPhase;
import com.rockwell.mes.services.s88.ifc.recipe.IMESPhase;

/**
 * TODO: Please enter the description of this type. This is mandatory!
 * <p>
 * This runtime phase executor skeleton is generated by the PhaseLibManager.
 * <p>
 * 
 * TODO: @author UserName, (c) Copyright 2013 Rockwell Automation Technologies, Inc. All Rights Reserved.
 */
public class RtPhaseExecutorDenLP0100 extends //
        AbstractPhaseExecutor0200<RtPhaseModelDenLP0100, RtPhaseViewDenLP0100, //
        RtPhaseExceptionViewDenLP0100, RtPhaseActionViewDenLP0100> implements ActionListener {

    /** Message pack for this phase. */
    public static final String MSGPACK = "PhaseDenLP0100";
    private static final Logger LOGGER = Logger.getLogger(RtPhaseExceptionViewDenLP0100.class.getName());

    /**
     * Creates the executor for an ACTIVE phase or a COMPLETED phase in case of resume.
     * 
     * @param inPhaseCompleter the object, which shall be used to complete the phase
     * @param inRtPhase the runtime phase to be executed
     */
    public RtPhaseExecutorDenLP0100(final IPhaseCompleter inPhaseCompleter, final IMESRtPhase inRtPhase) {
        super(inPhaseCompleter, inRtPhase);
    }

    /**
     * Creates the executor for a PREVIEW phase.
     * 
     * @param inPhase the related phase
     * @param inStep the related activity set step
     */
    public RtPhaseExecutorDenLP0100(final IMESPhase inPhase, final ActivitySetStep inStep) {
        super(inPhase, inStep);
    }

    @Override
    protected RtPhaseModelDenLP0100 createModel() {
        return new RtPhaseModelDenLP0100(this);
    }

    @Override
    protected RtPhaseViewDenLP0100 createView(RtPhaseModelDenLP0100 theModel) {
        return new RtPhaseViewDenLP0100(theModel);
    }

    @Override
    protected RtPhaseExceptionViewDenLP0100 createExceptionView(RtPhaseModelDenLP0100 theModel) {
        return new RtPhaseExceptionViewDenLP0100(theModel);
    }

    @Override
    protected RtPhaseActionViewDenLP0100 createActionView(RtPhaseModelDenLP0100 theModel) {
        return new RtPhaseActionViewDenLP0100(theModel);
    }

    @Override
    protected void performPhaseCompletion() {
    	LOGGER.info("performPhaseCompletion");
    }

    @Override
    protected boolean performPhaseCompletionCheck() {
    	LOGGER.info("performPhaseCompletionCheck");
        return true;
    }

    @Override
    protected void start() {
    	LOGGER.info("start");
    	RequestService requestService = new RequestServiceImpl();
    	RequestParameters parameters = new RequestParameters();
    	String url = null;
		String method = null;
		
		for(int i = 0; i< getRtPhase().getParameters().size(); i++) {   		
			if(getRtPhase().getParameters().get(i).getIdentifier().equalsIgnoreCase(LabelKeyValues.URL.getLabelKeyValue())) {    			
				url = getRtPhase().getParameters().get(i).getDataAsString(); 
			} 
			if(getRtPhase().getParameters().get(i).getIdentifier().equalsIgnoreCase(LabelKeyValues.BODY.getLabelKeyValue())) {    			
				parameters.setBody(getRtPhase().getParameters().get(i).getDataAsString());
				if(getRtPhase().getParameters().get(i).getDataAsString() != null) {
					method = RequestType.POST.getMethod();
				}		
			}
			if(getRtPhase().getParameters().get(i).getIdentifier().equalsIgnoreCase(LabelKeyValues.APYKEY.getLabelKeyValue())) {    			
				parameters.setApiKey(getRtPhase().getParameters().get(i).getDataAsString());
				if(parameters.getApiKey() != null && parameters.getApiKey().startsWith("ApiKey; ")) {
					parameters.setApiKey(parameters.getApiKey().replace("ApiKey; ", ""));
			    }
			} 	
		} 
		
		Request request = requestService.createRequest(getRtPhase().getKey(), method, url, parameters);
		LOGGER.info("\n ID: " + request.getId() +
				"\n Request: " + request.getRequestType().getMethod() + 
				"\n URL: " + request.getUrl() + 
				"\n Body: \n" + request.getParameters().getBody() + 
				"\n ApiKey: " + request.getParameters().getApiKey());
		
    }

    @Override
    protected void exceptionTransactionCallback(String checkKey, IMESExceptionRecord exceptionRecord, IESignatureExecutor sigExecutor) {
    	LOGGER.info("exceptionTransactionCallback");
    	try {
			exceptionRecord.getStatusAsMeaning();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
    	LOGGER.info("actionPerformed ");
    }

}
