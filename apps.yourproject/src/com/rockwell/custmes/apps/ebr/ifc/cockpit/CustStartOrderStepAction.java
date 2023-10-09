package com.rockwell.custmes.apps.ebr.ifc.cockpit;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.datasweep.compatibility.client.WorkCenter;
import com.rockwell.mes.clientfw.pec.ifc.controller.IProductionExecutionClient;
import com.rockwell.mes.clientfw.pec.ifc.helper.I18NHelper;
import com.rockwell.mes.clientfw.pec.ifc.view.MessageIDConstants;
import com.rockwell.mes.commons.base.ifc.IExceptionHandler;
import com.rockwell.mes.commons.base.ifc.i18n.I18nMessageUtility;
import com.rockwell.mes.commons.base.ifc.services.PCContext;
import com.rockwell.mes.commons.base.ifc.services.ServiceFactory;

/** GTM: SimulationOrder concept
 * 
 * shows the form to start an order step in the cockpit
 * <p>
 * 
 * @author hott, (c) Copyright 2011 Rockwell Automation Technologies, Inc.
 *         All Rights Reserved.
 *
 */
public class CustStartOrderStepAction extends AbstractAction {
    /** Comment for <code>serialVersionUID</code> */
    private static final long serialVersionUID = 5355854875265629695L;

    /** the form name */
    private static final String FORM_NAME = "cust-ebr-StartOrderStep";

    /** message pack for PEC error and exception messages */
    private static final String PEC_EXCEPTION_MESSAGE_PACK_NAME = "pec_ExceptionMessage";

    /** message ID for "workcenter not set" */
    private static final String MSG_ID_WORKCENTER_NOT_SET = "workCenterNotSet_Msg";

    /** Constructor */
    public CustStartOrderStepAction() {
        super(I18NHelper.getUIMessage(MessageIDConstants.BUTTON_TASK_EBR_PROCESSING));
        putValue(DEFAULT, "Recipe Processing");
    }

    /**
     * Performs the check if the current work center is set and displays an
     * error message if the check fails.
     * 
     * @return <code>true</code> if check successful, <code>false</code>
     *         otherwise
     */
    private boolean performWorkCenterCheck() {
        WorkCenter workCenter = getWorkCenter();
        if (workCenter == null) {
            String helpUrl = null;
            String localizedMessage =
                    I18nMessageUtility.getLocalizedMessage(PEC_EXCEPTION_MESSAGE_PACK_NAME, MSG_ID_WORKCENTER_NOT_SET);
            ServiceFactory.getService(IExceptionHandler.class).handleError(localizedMessage, null, helpUrl,
                    JOptionPane.DEFAULT_OPTION, null);
            return false;
        }
        return true;
    }

    /**
     * @return The current work center.
     */
    private WorkCenter getWorkCenter() {
        WorkCenter workCenter = PCContext.getFunctions().getCurrentWorkCenter();
        // if no current work center is set try to retrieve it from the station
        if (workCenter == null) {
            workCenter = PCContext.getFunctions().getStation().getWorkCenter();
            PCContext.getFunctions().setCurrentWorkCenter(workCenter);
        }
        return workCenter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        IProductionExecutionClient pec =
                (IProductionExecutionClient) ServiceFactory.getInstance().getService(IProductionExecutionClient.class,
                        "ProductionExecutionClient");
        if (pec.getCurrentCockpit().isCockpitDisplayed() && performWorkCenterCheck()) {
            String title = I18NHelper.getUIMessage("EBRStartOrderStep_Title");
            pec.displayCockpitEmbeddedForm(FORM_NAME, title);
        }
    }

}
