package com.rockwell.mes.myeig.activities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.client.Response;
import com.datasweep.core.eventlog.EventLog;
import com.rockwell.integration.framework.endpoint.implementation.EigOutboundEndpoint;
import com.rockwell.integration.framework.utility.ActivitiesUtility;
import com.rockwell.integration.framework.utility.UIConfigsCache;
import com.rockwell.integration.messaging.MessageEnvelope;
import com.rockwell.mes.myeig.commons.activity.ActivityInputItem;
import com.rockwell.mes.myeig.commons.activity.AnnotatedNonGUIActivity;

/**
 * This class implements functionality to send the outgoing messages to ERP with
 * no response expected.
 * 
 * @author syim, (c) Copyright 2012 Rockwell Automation Solutions, Inc. All
 *         Rights Reserved.
 */
public class SenderNoRspAction extends AnnotatedNonGUIActivity {

    /** logger */
    private static final Log LOGGER = LogFactory.getLog(SenderNoRspAction.class);

    /** The message envelope data */
    private MessageEnvelope eventObject;

    /**
     * {@inheritDoc} @see
     * com.rockwell.mes.myeig.commons.activity.AnnotatedNonGUIActivity
     * #activityExecute()
     */
    public Response activityExecute() {
        final MessageEnvelope data = getEventObject();

        try {
            // Responses back from ERP are not relevant here
            // TODO 
            // Change UIConfigsCache.activityToActivitySetMap to protected and use getter
            // String actEventName = UIConfigsCache.getActivityToActivitySetMap().get(ActivitiesUtility.getActivitySetName(this));
            
            String actEventName = UIConfigsCache.activityToActivitySetMap.get(ActivitiesUtility.getActivitySetName(this));
            boolean eventSent = EigOutboundEndpoint.getOutboundEndPoint(actEventName).sendEventNoWait(data);
            EigOutboundEndpoint.getOutboundEndPoint(actEventName).outboundSendEventSuccess(data.getPayload().getEventId());
            if (!eventSent) {
                throw new Exception("Failed to send event: " + data.getPayload().getEventId());
            }

            // PCIntegrationEndPoint.getEndPoint().sendEvent(data);
            // PCIntegrationEndPoint.getEndPoint().outboundSendEventSuccess(data.getPayload().getEventId());
            EventLog.Write("Successful in sending payload: " + data.getDataInfo());

        } catch (Exception e) {
            LOGGER.error("SendMessageAction::activityExecute", e);
            EventLog.writeError("Cannot send this event: " + data.getDataInfo());
            EventLog.logException(e, this, "SendMessageAction::activityExecute()");
        }

        return new Response();
    }

    @Override
    public String getActivityDescription() {
        return "This activity will wrap up the outbound event and send message to the ERP system.";
    }

    /**
     * @param eventObject Sets the event object as the message envelope data
     */
    @ActivityInputItem
    public void setEventObject(final MessageEnvelope eventObject) {
        this.eventObject = eventObject;
    }

    /**
     * @return Returns the event object
     */
    public MessageEnvelope getEventObject() {
        return eventObject;
    }
}
