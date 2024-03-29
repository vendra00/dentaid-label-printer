package com.adasoft.gv.phase.product.labelgenerator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.ws.rs.core.MediaType;

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
public class RtPhaseExecutorLabelGen0100 extends //
        AbstractPhaseExecutor0200<RtPhaseModelLabelGen0100, RtPhaseViewLabelGen0100, //
        RtPhaseExceptionViewLabelGen0100, RtPhaseActionViewLabelGen0100> implements ActionListener {

    /** Message pack for this phase. */
    public static final String MSGPACK = "PhaseLabelGen0100";
    
    private static final String API_URL = "http://192.168.15.15:8080/adasoft/api/labels";

    /**
     * Creates the executor for an ACTIVE phase or a COMPLETED phase in case of resume.
     * 
     * @param inPhaseCompleter the object, which shall be used to complete the phase
     * @param inRtPhase the runtime phase to be executed
     */
    public RtPhaseExecutorLabelGen0100(final IPhaseCompleter inPhaseCompleter, final IMESRtPhase inRtPhase) {
        super(inPhaseCompleter, inRtPhase);
    }

    /**
     * Creates the executor for a PREVIEW phase.
     * 
     * @param inPhase the related phase
     * @param inStep the related activity set step
     */
    public RtPhaseExecutorLabelGen0100(final IMESPhase inPhase, final ActivitySetStep inStep) {
        super(inPhase, inStep);
    }

    @Override
    protected RtPhaseModelLabelGen0100 createModel() {
        return new RtPhaseModelLabelGen0100(this);
    }

    @Override
    protected RtPhaseViewLabelGen0100 createView(RtPhaseModelLabelGen0100 theModel) {
        return new RtPhaseViewLabelGen0100(theModel);
    }

    @Override
    protected RtPhaseExceptionViewLabelGen0100 createExceptionView(RtPhaseModelLabelGen0100 theModel) {
        return new RtPhaseExceptionViewLabelGen0100(theModel);
    }

    @Override
    protected RtPhaseActionViewLabelGen0100 createActionView(RtPhaseModelLabelGen0100 theModel) {
        return new RtPhaseActionViewLabelGen0100(theModel);
    }

    @Override
    protected void performPhaseCompletion() {
        // TODO Auto-generated method stub
    }

    @Override
    protected boolean performPhaseCompletionCheck() {
    	System.out.println("==============================================");
        System.out.println("=========== CONFIRMED CLICK ==================");
        System.out.println("======= PHASE COMPLETION CHECK ===============");
        System.out.println("==============================================");
        return true;
    }

    @Override
    protected void start() {
        System.out.println("START SERVER");

        String pingEndpointUrl = API_URL + "/ping";
        System.out.println("PING URL: " + pingEndpointUrl);

        try {
            URL url = new URL(pingEndpointUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set request method to GET
            connection.setRequestMethod("GET");

            // Set the appropriate media type for the response
            connection.setRequestProperty("Accept", MediaType.TEXT_PLAIN);

            // Get the response code
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the response
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                System.out.println("Response from ping endpoint: " + response.toString());
            } else {
                // Handle other response codes if needed
                System.out.println("Failed to get response. Response code: " + responseCode);
            }

            connection.disconnect();
        } catch (IOException e) {
            // Handle the exception appropriately
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
    }

	@Override
	protected void exceptionTransactionCallback(String arg0, IMESExceptionRecord arg1, IESignatureExecutor arg2) {
		// TODO Auto-generated method stub
		
	}

}
