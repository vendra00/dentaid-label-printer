package com.rockwell.custmes.livedata;

import java.awt.Dimension;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.ui.ScriptArgument;
import com.rockwell.livedata.AsyncTransaction;
import com.rockwell.livedata.FTException;
import com.rockwell.livedata.FTLDGroup;
import com.rockwell.livedata.FTLDGroupCancelEvent;
import com.rockwell.livedata.FTLDGroupDataEvent;
import com.rockwell.livedata.FTLDGroupListener;
import com.rockwell.livedata.FTLDGroupWriteEvent;
import com.rockwell.livedata.GroupDataEvent;
import com.rockwell.livedata.GroupWriteEvent;
import com.rockwell.livedata.ItemData;
import com.rockwell.livedata.LiveDataGroup;
import com.rockwell.mes.clientfw.pec.ifc.view.DefaultActivityControl;

/**
 * Class implemented as singleton to minimize time intense load / 
 * configuration process.
 * 
 * @author SPunzman
 */
@Deprecated
public class LiveDataActivity extends DefaultActivityControl implements FTLDGroupListener {

    private static final Log LOGGER = LogFactory.getLog(LiveDataActivity.class);

    private static LiveDataHandler liveDataHandler = null;

    /** Activity Proxy Events */
    public static final String DATA_CHANGE = "dataChange";
    public static final String READ_COMPLETE = "readComplete";
    public static final String WRITE_COMPLETE = "writeComplete";

    public LiveDataActivity() {
        setPreferredSize(new Dimension(1, 1));
        liveDataHandler = LiveDataHandler.getLiveDataHandler();
    }

    /**
     * {@link LiveDataHandler#createLiveDataGroup(String, String, String[])}
     */
    public LiveDataGroup createLiveDataGroup(final String server, final String groupId, final String[] groupTags) {
        LiveDataGroup ldGroup = liveDataHandler.createLiveDataGroup(server, groupId, groupTags);

        liveDataHandler.removeEventListener(FTLDGroupListener.class, this);

        liveDataHandler.addGroupEventListener(this);
        liveDataHandler.addEventListener(FTLDGroupListener.class, this);         
        
        return ldGroup;
    }

    /**
     * {@link LiveDataHandler#createLiveDataGroup(String, String, String[])}
     */
    public String getLiveDataConfiguration() {    
        return liveDataHandler.getLiveDataConfiguration();
    }
    
    /**
     * {@link LiveDataHandler#getGroup(String, String)}
     */
    public LiveDataGroup getGroup(final String groupName, final String server) {
        return liveDataHandler.getGroup(groupName, server);
    }
    
    /**
     * {@link LiveDataHandler#asyncRead(LiveDataGroup, String[])}
     */
    public AsyncTransaction asyncRead(final LiveDataGroup group, final String[] tag) {
        return liveDataHandler.asyncRead(group, tag);
    }

    /**
     * {@link LiveDataHandler#syncRead(LiveDataGroup, String[])}
     */
    public ItemData[] syncRead(final LiveDataGroup group, final String[] tag) {
        return liveDataHandler.syncRead(group, tag);        
    }

    /**
     * {@link LiveDataHandler#asyncWrite(LiveDataGroup, String[], Object[])}
     */    
    public AsyncTransaction asyncWrite(final LiveDataGroup group, final String[] tag, final Object[] value) {
        return liveDataHandler.asyncWrite(group, tag, value);
    }    

    /**
     * {@link LiveDataHandler#syncWrite(LiveDataGroup, String[], Object[])}
     */
    public FTException[] syncWrite(final LiveDataGroup group, final String[] tag, final Object[] value) {
        return liveDataHandler.syncWrite(group, tag, value);
    }    
    
    /**
     * {@link FTLDGroupListener#cancelComplete(FTLDGroupCancelEvent)}} 
     */
    @Override
    public void cancelComplete(FTLDGroupCancelEvent arg0) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@link FTLDGroupListener#writeComplete(FTLDGroupWriteEvent)}} 
     */
    @Override
    public void writeComplete(FTLDGroupWriteEvent event) {
        LOGGER.debug("Write Complete Event");
        try {
            LiveDataGroup source = liveDataHandler.getGroup((FTLDGroup) event.getSource());

            fireActivityEvaluateScript(WRITE_COMPLETE, new ScriptArgument[] { new ScriptArgument("GroupWriteEvent",
                    new GroupWriteEvent(source, event)) });
        } catch (FTException ex) {
            LOGGER.error("exception calling fireActivityEvaluateScript, exception says: "
                    + StringUtils.defaultString(ex.getMessage()));
        }
    }

    /**
     * {@link FTLDGroupListener#dataChange(FTLDGroupDataEvent)}} 
     */
    @Override
    public void dataChange(FTLDGroupDataEvent event) {
        LOGGER.debug("Data Change Event");

        fireActivityEvaluateScript(DATA_CHANGE, new ScriptArgument[] { new ScriptArgument("GroupDataEvent",
                new GroupDataEvent(event.getSource(), event)) });
    }

    @Override
    public void readComplete(FTLDGroupDataEvent event) {
        LOGGER.debug("Read Complete Event");
        try {
            LiveDataGroup source = liveDataHandler.getGroup((FTLDGroup) event.getSource());

            fireActivityEvaluateScript(READ_COMPLETE, new ScriptArgument[] { new ScriptArgument("GroupDataEvent",
                    new GroupDataEvent(source, event)) });
        } catch (FTException ex) {
            LOGGER.error("exception calling fireActivityEvaluateScript, exception says: "
                    + StringUtils.defaultString(ex.getMessage()));
        }
    }

    @Override
    public String[] getActivityEvents() {
        return new String[] { DATA_CHANGE, READ_COMPLETE, WRITE_COMPLETE };
    }

    @Override
    public String getActivityDescription() {
        return "The LiveDataActivity encapsulates access to FactoyTalk LiveData. \n"
                + "It requires to set a LiveDataHandler object as input \n"
                + "The Input item will be set as Output Item as well to ensure accessibility"
                + "Events like DataChange, ReadComplete, WriteComplete will then be fired into PNuts";
    }

    @Override
    public String getBaseName() {
        return this.getClass().getSimpleName();
    }
}
