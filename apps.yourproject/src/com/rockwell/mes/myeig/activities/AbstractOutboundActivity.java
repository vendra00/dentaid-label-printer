package com.rockwell.mes.myeig.activities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.client.DatasweepException;
import com.datasweep.compatibility.client.Response;
import com.rockwell.integration.activities.helper.ValidationErrors;
import com.rockwell.integration.messaging.BasePayload;
import com.rockwell.integration.messaging.MessageEnvelope;
import com.rockwell.mes.commons.base.ifc.exceptions.MESException;
import com.rockwell.mes.myeig.commons.activity.ActivityInputItem;
import com.rockwell.mes.myeig.commons.activity.ActivityOutputItem;
import com.rockwell.mes.myeig.commons.activity.AnnotatedNonGUIActivity;

/**
 * This class provides the base functionality to process outgoing messages to
 * Mule/EIG
 * 
 * @author syim, (c) Copyright 2012 Rockwell Automation Solutions, Inc. All
 *         Rights Reserved.
 */
public abstract class AbstractOutboundActivity extends AnnotatedNonGUIActivity {

    /** logger */
    private static final Log LOGGER = LogFactory.getLog(AbstractOutboundActivity.class);

    /** The event object */
    private MessageEnvelope eventObject;

    /** The event key */
    private Long eventKey;

    /** Validation errors */
    private ValidationErrors error;

    /** Pass/fail code */
    private Integer passAct;

    /** The document number */
    private String docNum;

    @Override
    public Response activityExecute() {

        ValidationErrors errors = new ValidationErrors();
        MessageEnvelope data = getEventObject();
        String eventId = null;
        Long objectKey = getEventKey(); // This 'key' value is the corresponding 'object_key' in the PCA_EVENTS table

        try {
            eventId = data.getPayload().getEventId();
            setDocNum(eventId);
            logInfo(LOGGER, "Processing outbound integration message for PCA_EVENT");

            // object key must exist in order to get the serialized object
            if ((objectKey == null) || (objectKey.longValue() < 0)) {
                errors.addError(this.getClass().getName(), "Outbound event object key is missing or < 0");
            } else {
                BasePayload pcData = processPayloadData(eventId);
                pcData.setEventId(eventId);
                data.setPayload(pcData);
            }

        } catch (Exception e) {
            errors.addError(this.getClass().getName(), e.toString() + "\n");
            LOGGER.error("Exception in outbound processing: ", e);
        }

        // if errors are detected set Flag and let it transfer to logging Activity
        if ((errors.getErrors() == null) || (errors.getErrors().size() == 0)) {
            setFlag(1);
            setEventObject(data);
        } else {
            // add idoc number, so reference is seen in logs and email(if applicable)
            errors.addError("Message ID", eventId);
            setFlag(2);
            setError(errors);
        }

        return new Response();
    }

    /**
     * Retrieve and process the payload data for the outbound message type
     * 
     * @param eventId The event id
     * @return The BasePayload
     * @throws DatasweepException On Error.
     * @throws MESException On Error.
     */
    public abstract BasePayload processPayloadData(String eventId) throws DatasweepException, MESException;

    /**
     * Log entry
     * 
     * @param logger The logger
     * @param entry The log entry
     */
    public void logInfo(Log logger, String entry) {
        logger.info(docNum + " - " + entry);
    }

    /**
     * @param flag Pass/fail
     */
    private void setFlag(int flag) {
        this.setPassAct(flag);
    }

    @Override
    public String getActivityDescription() {
        return "This activity creates an Object, used in ActivitySet, "
                + "and there must be an IntEventData type Variable named eventObject containing the Object.";
    }

    /**
     * @param eventObject The event object
     */
    @ActivityInputItem
    public void setEventObject(MessageEnvelope eventObject) {
        this.eventObject = eventObject;
    }

    /**
     * @return The event object
     */
    @ActivityOutputItem
    public MessageEnvelope getEventObject() {
        return eventObject;
    }

    /**
     * @param eventKey The event key
     */
    @ActivityInputItem
    public void setEventKey(Long eventKey) {
        this.eventKey = eventKey;
    }

    /**
     * @return The event key
     */
    public Long getEventKey() {
        return eventKey;
    }

    /**
     * @return The error
     */
    @ActivityOutputItem
    public ValidationErrors getError() {
        return error;
    }

    /**
     * @param error The error
     */
    public void setError(ValidationErrors error) {
        this.error = error;
    }

    /**
     * @return Pass/fail code
     */
    @ActivityOutputItem
    public Integer getPassAct() {
        return passAct;
    }

    /**
     * @param passAct Set the pass/fail code
     */
    public void setPassAct(Integer passAct) {
        this.passAct = passAct;
    }

    /**
     * @return The document number
     */
    public String getDocNum() {
        return docNum;
    }

    /**
     * @param docNum The document number to set
     */
    public void setDocNum(String docNum) {
        this.docNum = docNum;
    }

}
