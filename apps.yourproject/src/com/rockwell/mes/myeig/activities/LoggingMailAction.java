package com.rockwell.mes.myeig.activities;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.client.Response;
import com.rockwell.activityset.ActivityContainer;
import com.rockwell.integration.activities.helper.ValidationErrors;
import com.rockwell.integration.activities.helper.ValidationErrors.ValidationError;
import com.rockwell.mes.commons.base.ifc.configuration.MESConfiguration;
import com.rockwell.mes.commons.base.ifc.services.PCContext;
import com.rockwell.mes.myeig.commons.activity.ActivityInputItem;
import com.rockwell.mes.myeig.commons.activity.AnnotatedNonGUIActivity;

/**
 * E-mailing logging class for EIG interface exceptions
 * 
 * @author syim, (c) Copyright 2012 Rockwell Automation Solutions, Inc. All
 *         Rights Reserved.
 */
public class LoggingMailAction extends AnnotatedNonGUIActivity {

    /** LOGGER */
    private static final Log LOGGER = LogFactory.getLog(LoggingMailAction.class);

    /** The validation errors */
    private ValidationErrors error;

    /** new line string */
    private static final String NEW_LINE = "\n";

    /**
     * {@inheritDoc} @see
     * com.rockwell.mes.myeig.commons.activity.AnnotatedNonGUIActivity
     * #activityExecute()
     */
    public Response activityExecute() {
        ValidationErrors data;
        try {
            if (this.getParent() instanceof ActivityContainer) {

                String messageId = null;
                data = getError();

                if (data != null) {
                    final StringBuffer emailError = new StringBuffer();
                    emailError.append("The following error(s) has occurred in the ERP Integration Gateway:\n");
                    for (int i = 0; i < data.getErrors().size(); i++) {
                        final ValidationError indi = data.getErrors().get(i);
                        if (indi.getObjectBo().equals("Message ID")) {
                            messageId = indi.getErrorMessage();
                            emailError.append(NEW_LINE);
                            emailError.append(indi.getObjectBo());
                            emailError.append(": ");
                            emailError.append(messageId);
                        } else {
                            emailError.append("\nObject type: ");
                            emailError.append(indi.getObjectBo());
                            emailError.append("\nError message: ");
                            emailError.append(indi.getErrorMessage());
                        }
                    }
                    // write error to log
                    LOGGER.info(emailError.toString());

                    // email error to administrator
                    final String emailRecipient = MESConfiguration.getMESConfiguration().getString(
                            "eig_AdministratorAddress",
                            StringUtils.EMPTY, "EIG administrator e-mail address");

                    if (StringUtils.isNotBlank(emailRecipient)) { // send only when email address is set
                        final String dbName = PCContext.getFunctions().getDBInfo().getActiveDBDatabaseName();
                        Response resp = getFunctions().sendEmail(emailRecipient,
                                "Gateway error notification - Message ID " + messageId + " <" + dbName + ">",
                                emailError.toString());
                        if (resp.isError()) {
                            LOGGER.info("Email error: " + resp.getFirstErrorMessage());
                        }
                    }
                }
            }
            // success(data);
        } catch (Exception e) {
            LOGGER.error("Problem logging/e-mailing gateway issue: " + StringUtils.defaultString(e.getMessage()));
        }

        return new Response();
    }

    @Override
    public String getActivityDescription() {
        return "This activity will do the logging/error emailing for the interface activities";
    }

    /**
     * @param error Sets the error(s)
     */
    @ActivityInputItem
    public void setError(final ValidationErrors error) {
        this.error = error;
    }

    /**
     * @return Returns the error(s)
     */
    public ValidationErrors getError() {
        return error;
    }

}
