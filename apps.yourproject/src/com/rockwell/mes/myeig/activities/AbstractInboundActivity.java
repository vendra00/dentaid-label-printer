package com.rockwell.mes.myeig.activities;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.client.ActivitySet;
import com.datasweep.compatibility.client.Batch;
import com.datasweep.compatibility.client.DatasweepException;
import com.datasweep.compatibility.client.Part;
import com.datasweep.compatibility.client.Response;
import com.datasweep.compatibility.client.RuntimeActivitySet;
import com.datasweep.compatibility.client.RuntimeActivitySetStep;
import com.datasweep.compatibility.client.UnitOfMeasure;
import com.datasweep.compatibility.ui.Time;
import com.rockwell.activityset.ActivityContainer;
import com.rockwell.integration.activities.helper.ValidationErrors;
import com.rockwell.integration.activities.helper.ValidationErrors.ValidationError;
import com.rockwell.integration.framework.utility.InboundEventLogStats;
import com.rockwell.integration.framework.utility.LogInboundEventStats;
import com.rockwell.integration.messaging.BasePayload;
import com.rockwell.integration.messaging.IMessagingConstants;
import com.rockwell.integration.messaging.MessageEnvelope;
import com.rockwell.mes.commons.base.ifc.exceptions.MESException;
import com.rockwell.mes.commons.base.ifc.exceptions.MESRuntimeException;
import com.rockwell.mes.commons.base.ifc.services.PCContext;
import com.rockwell.mes.commons.base.ifc.services.ServiceFactory;
import com.rockwell.mes.myeig.commons.activity.ActivityInputItem;
import com.rockwell.mes.myeig.commons.activity.ActivityOutputItem;
import com.rockwell.mes.myeig.commons.activity.AnnotatedNonGUIActivity;
import com.rockwell.mes.myeig.utility.CacheUtility;
import com.rockwell.mes.services.inventory.ifc.IBatchService;
import com.rockwell.mes.services.recipe.ifc.IMESRecipeService;

/**
 * This class provides the base functionality to process incoming messages from
 * Mule/EIG
 * 
 * @author syim, (c) Copyright 2012 Rockwell Automation Solutions, Inc. All
 *         Rights Reserved.
 */
public abstract class AbstractInboundActivity extends AnnotatedNonGUIActivity {

    /** logger */
    private static final Log LOGGER = LogFactory.getLog(AbstractInboundActivity.class);

    /** Pass code */
    private static final int PASS_CODE = 1;

    /** fail code */
    private static final int FAIL_CODE = 2;

    /** maximum length of inbound log result description */
    private static final int RESULT_DESC_MAX_LEN = 255;

    /** property AS_NOT_SET, if a activity set is not set (e.g. unit test) */
    private static final String AS_NOT_SET = "N/A";

    /** The eventObject */
    private MessageEnvelope eventObject;

    /** Validation errors */
    private ValidationErrors error;

    /** Pass/fail code */
    private Integer passAct;

    /** The document number */
    private String docNum;

    /** The object(s) processed */
    private StringBuffer objectsProcessed = new StringBuffer();

    /** Resources */
    protected static final ResourceBundle resources = ResourceBundle.getBundle("com/rockwell/mes/myeig/resources/Messages");

    @Override
    public Response activityExecute() {

        CacheUtility.disableCaches();
        setError(new ValidationErrors());
        MessageEnvelope data = getEventObject();
        Time processTime = PCContext.getFunctions().createTime();

        try {
            processActivityData(data); // for INBOUND monitor      
        } catch (Exception e) {
            addError(this.getClass().getName(), e.toString() + "\n");
            LOGGER.error("Exception in inbound processing: ", e);
        }

        // if errors are detected set Flag and let it transfer to logging Activity
        String reason = StringUtils.EMPTY;
        if (getErrors() == null || getErrors().size() == 0) {
            setFlag(PASS_CODE);
        } else {
            logError(LOGGER, "Errors detected in processing.");
            // INBOUND logging ...
            reason = error.getErrorMessages();
            // add document number, so reference is seen in logs and email(if applicable)
            addError("Message ID", docNum);
            setFlag(FAIL_CODE);
        }

        // logging for INBOUND monitor...
        buildInboundEventLog(data, processTime, reason);

        return new Response();
    }

    /**
     * All logic to validate and process the data for the in-bound channel
     * should occur in this method
     * 
     * @param data The data to process
     * @throws DatasweepException On Error.
     * @throws MESException On Error.
     */
    public abstract void processActivityData(MessageEnvelope data) throws DatasweepException, MESException;

    /**
     * @return The object type
     */
    public abstract Long getObjectType();

    /**
     * Create the inbound event log record
     * 
     * @param envelope The message envelope
     * @param processTime The time or receipt/processing
     * @param reason The reason for errors
     */
    private void buildInboundEventLog(MessageEnvelope envelope, Time processTime, String reason) {

        try {
            BasePayload data = envelope.getPayload();

            // Create the in bound log object
            String asName = AS_NOT_SET;
            ActivityContainer aContainer = (ActivityContainer) getParent();
            if (aContainer != null) {
                RuntimeActivitySetStep rtAsStep = aContainer.getParentRuntimeStep();
                if (rtAsStep != null) {
                    RuntimeActivitySet rtAs = rtAsStep.getRuntimeActivitySet();
                    if (rtAs != null) {
                        ActivitySet asObj = rtAs.getActivitySet();
                        if (asObj != null) {
                            asName = asObj.getName();
                        }
                    }
                }
            }

            String resultcode = getPassAct() == PASS_CODE ? IMessagingConstants.RESULT_OK
                    : IMessagingConstants.RESULT_ERR;
            String resultdesc = getPassAct() == PASS_CODE ? getObjectsProcessed() : StringUtils.substring(reason, 0,
                    RESULT_DESC_MAX_LEN);

            InboundEventLogStats iels = new InboundEventLogStats.Builder(InboundEventLogStats.INBOUND_EVENT_LOG, data
                    .getClass().getName().trim()).result(resultcode).reason(resultdesc).verb(data.getVerb())
                    .asName(asName).rcvTime(processTime).procTime(processTime).msgType(data.getClass().getSimpleName())
                    .createTime(processTime).msgId(docNum).inputFileName(data.getInFileName())
                    .ftpcObjType(getObjectType()).build();

            LogInboundEventStats.logInboundStats(iels);

        } catch (Exception e) {
            // should not occur
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Log entry
     * 
     * @param logger The logger
     * @param entry The log entry
     */
    public void logInfo(Log logger, String entry) {
        logger.info(docNum + " - " + entry);
    }

    public void logWarn(Log logger, String entry) {
        logger.warn(docNum + " - " + entry);
    }

    public void logError(Log logger, String entry) {
        logger.error(docNum + " - " + entry);
    }

    /**
     * @param <T> the type of the property to retrieve
     * @param required A value is required and should not be null
     * @param value The name/value of the object
     * @param clazz The class of the return object
     * @param errorString The error string prefix
     * @return The object
     */
    protected <T> T validate(final boolean required, final String value, final Class<T> clazz, 
            final String errorString) {
        T returnObj = null;
        logInfo(LOGGER, "Validating " + errorString + " '" + value + "'");
        if (StringUtils.isBlank(value)) {
            if (required) {
                logError(LOGGER, "Missing " + errorString);
                addError(this.getClass().getName(), errorString + " should not be null. \n");
            }
            return returnObj;
        }
        if (clazz == String.class) {
            returnObj = (T) value;
        } else if (clazz == UnitOfMeasure.class) {
            returnObj = (T) getFunctions().getUnitOfMeasure(value.toLowerCase(Locale.getDefault()));
        } else if (clazz == Part.class) {
            returnObj = (T) ServiceFactory.getService(IMESRecipeService.class).getPart(value);
        } else if (clazz == Batch.class) {
            returnObj = (T) ServiceFactory.getService(IBatchService.class).loadBatch(value);
        } else {
            throw new MESRuntimeException("Unsupported object type " + clazz.getName());
        }
        if (returnObj == null) {
            logError(LOGGER, errorString + " '" + value + "' does not exist in the system.");
            addError(this.getClass().getName(), errorString + " '" + value + "' does not exist in the system. \n");
        }
        return returnObj;

    }

    /**
     * @param flag Pass/fail
     */
    private void setFlag(int flag) {
        this.setPassAct(flag);
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
    public MessageEnvelope getEventObject() {
        return eventObject;
    }

    /**
     * @return The error
     */
    @ActivityOutputItem
    public ValidationErrors getError() {
        return error;
    }

    /**
     * @param error The error to set
     */
    public void setError(ValidationErrors error) {
        this.error = error;
    }

    /**
     * @param className The class name
     * @param errorString The error string
     */
    public void addError(String className, String errorString) {
        error.addError(className, errorString);
    }

    /**
     * @return The errors
     */
    public List<ValidationError> getErrors() {
        return error.getErrors();
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

    /**
     * @return The objects processed
     */
    public String getObjectsProcessed() {
        return objectsProcessed.toString();
    }

    /**
     * @param processedObject The object processed
     */
    public void setObjectsProcessed(String processedObject) {
        this.objectsProcessed.append(processedObject);
        this.objectsProcessed.append("\n");
    }
}
