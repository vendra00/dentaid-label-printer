package com.rockwell.custmes.helper;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.client.Response;
import com.rockwell.custmes.livedata.LiveDataHandler;
import com.rockwell.livedata.FTException;
import com.rockwell.livedata.ItemData;
import com.rockwell.livedata.LiveDataGroup;

/**
 * Helper class to read and write OPC tags from PNuts. The helper methods here
 * should contain all necessary code for Java Exception handling, timeout
 * handling and optimization.
 * <p>
 * E.g. if required that after a unsuccessful read, there should be a pause of 5
 * minutes before the next real read is tried. It should be encapsulated here.
 *
 * @author rweinga
 */
public class LiveDataHelper {

    private static final Log LOGGER = LogFactory.getLog(LiveDataHelper.class);

    private LiveDataHelper() {

    }

    /**
     * Reads multiple OPC tags
     *
     * @param server The server. Will be initialized on the first call.
     * @param group The group string, if the group does not exist it will be
     *            created.
     * @param tag Tag string
     * @return The response object indicates if the request was successful or
     *         not. The result contains the read value object.
     */
    public static Response readTags(String server, String group, String[] tags) {
        Response response;
        try {
            LiveDataHandler handler = LiveDataHandler.getLiveDataHandler();
            LiveDataGroup grp = handler.getGroup(group, server);
            if (grp == null) {
                grp = handler.createLiveDataGroup(server, group, tags);
            }
            if (!handler.isStarted()) {
                handler.start();
            }
            ItemData[] result = handler.syncRead(grp, tags);

            if (result.length == 0) {
                String[] args = {server};
                throw new Exception(UIHelper.getLocalizedMessage("liveData_read_failed", args));
            }

            HashMap<String, Object> mapp = new HashMap<String, Object>();

            for (int x = 0; x < tags.length; x++) {
                ItemData item = result[x];

                String tagName = tags[x];
                Object tagValue = item.getValue();
                mapp.put(tagName, tagValue);
            }

            response = new Response();
            response.setResult(mapp);
        } catch (Throwable exc) { // NOPMD hpl - paranoid mode, catch everything
            String[] args = {server};
            response = UIHelper.createResponseObject(UIHelper.MSG_PACK_ID, "liveData_read_failed", args);
        }
        return response;
    }

    /**
     * Reads an OPC tag
     *
     * @param server The server. Will be initialized on the first call.
     * @param group The group string, if the group does not exist it will be
     *            created.
     * @param tag Tag string
     * @return The response object indicates if the request was successful or
     *         not. The result contains the read value object.
     */
    public static Response readTag(String server, String group, String tag) {
        Response response;
        try {
            LiveDataHandler handler = LiveDataHandler.getLiveDataHandler();
            String[] tags = new String[] { tag };
            LiveDataGroup grp = handler.getGroup(group, server);
            if (grp == null) {
                grp = handler.createLiveDataGroup(server, group, tags);
            }
            if (!handler.isStarted()) {
                handler.start();
            }
            ItemData[] result = handler.syncRead(grp, tags);
            if (result.length == 0) {
                String[] args = {server};
                throw new Exception(UIHelper.getLocalizedMessage("liveData_read_failed", args));
            }
            response = new Response();
            response.setResult(result[0].getValue());
        } catch (Throwable exc) { // NOPMD hpl - paranoid mode, catch everything
            String[] args = {server};
            response = UIHelper.createResponseObject(UIHelper.MSG_PACK_ID, "liveData_read_failed", args);
        }
        return response;
    }

    /**
     * Writes an OPC tag
     *
     * @param server The server. Will be initialized on the first call.
     * @param group The group string, if the group does not exist it will be
     *            created.
     * @param tag Tag string
     * @param value The value to set. Must match to the required OPC value.
     * @return The response object indicates if the request was successful or
     *         not.
     */
    public static Response writeTag(String server, String group, String tag, Object value) {
        Response response;
        try {
            LiveDataHandler handler = LiveDataHandler.getLiveDataHandler();
            String[] tags = new String[] { tag };
            LOGGER.debug("handler.isStarted() = " + handler.isStarted());
            LiveDataGroup grp = handler.getGroup(group, server);
            if (grp == null) {
                grp = handler.createLiveDataGroup(server, group, tags);
            }
            if (!handler.isStarted()) {
                handler.start();
            }
            FTException[] result = handler.syncWrite(grp, tags, new Object[] { value });
            for (FTException exc : result) {
                if (exc != null) {
                    return UIHelper.createResponseObject(result[0]);
                }
            }
            response = new Response();
        } catch (Throwable exc) { // NOPMD hpl - paranoid mode, catch everything
            response = UIHelper.createResponseObject(exc);
        }
        return response;
    }
}
