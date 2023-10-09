package com.rockwell.custmes.livedata;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;

import com.rockwell.custmes.helper.UIHelper;
import com.rockwell.livedata.AsyncTransaction;
import com.rockwell.livedata.FTException;
import com.rockwell.livedata.FTLDGroup;
import com.rockwell.livedata.FTLDGroupListener;
import com.rockwell.livedata.ItemData;
import com.rockwell.livedata.LiveData;
import com.rockwell.livedata.LiveDataGroup;
import com.rockwell.livedata.LiveDataGroups;
import com.rockwell.livedata.LiveDataItem;
import com.rockwell.livedata.LiveDataItems;
import com.rockwell.livedata.LiveDataServer;
import com.rockwell.livedata.LiveDataSource;
import com.rockwell.mes.commons.base.ifc.IExceptionHandler;
import com.rockwell.mes.commons.base.ifc.configuration.MESConfiguration;
import com.rockwell.mes.commons.base.ifc.exceptions.MESException;
import com.rockwell.mes.commons.base.ifc.services.ServiceFactory;

/**
 * Class implemented as singleton to minimize time intense load /
 * configuration process.
 *
 * @author SPunzman
 */
public final class LiveDataHandler extends LiveData {


    private static final int LIVEDATA_UPDATE_RATE = 1000;

    /** The names of the configuration properties of the default live data user and password */
    private static final String CONFIG_LIVE_DATA_USER = "liveDataDefaultUsername";
    private static final String CONFIG_LIVE_DATA_PW = "liveDataDefaultPassword";

    /**
     * Internal Live Data Handler Singleton Object.
     */
    private static final LiveDataHandler LIVEDATAHANDLER = new LiveDataHandler();

    /**
     * Private constructor for LiveDataHandler.
     */
    private LiveDataHandler() {
    }

    /**
     * This static method is intended to retrieve the already initialized LiveDataHandler object without passing in user
     * and password. see: {@link #getLiveDataHandler(String, String)}
     *
     * @return singleton LiveDataHandler instance
     * @throws FTException
     *             exception is thrown in case class has not been properly initialized
     *             {@link #getLiveDataHandler(String, String)}
     */
    public static LiveDataHandler getLiveDataHandler() {
        if (!StringUtils.isEmpty(LIVEDATAHANDLER.getUserName())) {
            return LIVEDATAHANDLER;
        }

        String user = MESConfiguration.getMESConfiguration().getString(CONFIG_LIVE_DATA_USER, "",
                "Live Data Default User");
        String password = MESConfiguration.getMESConfiguration().getString(CONFIG_LIVE_DATA_PW, "",
                "Live Data Default PW");

        if (StringUtils.isEmpty(user) || StringUtils.isEmpty(password)) {
            getExceptionHandler().handleError(UIHelper.getLocalizedMessage("liveData_user_not_configured"),
                    null, null, JOptionPane.DEFAULT_OPTION, null);
        }

        LIVEDATAHANDLER.setUserName(user);
        LIVEDATAHANDLER.setPassword(password);

        return LIVEDATAHANDLER;
    }

    /**
     * This static method is intended to retrieve the already initialized LiveDataHandler object without passing in user
     * and password. see: {@link #getLiveDataHandler(String, String)}. It will remove all registered servers,
     * except for the given one.
     *
     * @return singleton LiveDataHandler instance
     * @throws FTException
     *             exception is thrown in case class has not been properly initialized
     *             {@link #getLiveDataHandler(String, String)}
     */
    public static LiveDataHandler getLiveDataHandler(String serverString) {
        LiveDataHandler myHandler = LiveDataHandler.getLiveDataHandler();
        try {
            if (StringUtils.isEmpty(serverString)) {
                String[] args = {serverString};
                throw new MESException(UIHelper.getLocalizedMessage("liveData_no_valid_opc_server", args));
            }

            for (LiveDataServer server : myHandler.getServers()) {
                myHandler.removeServer(server);
            }

            myHandler.getServerCustom(serverString);
        } catch (FTException e) {
            getExceptionHandler().handleError(e.getLocalizedMessage(), null, null, JOptionPane.DEFAULT_OPTION, e);
            myHandler = null;
        } catch (MESException ex) {
            getExceptionHandler().handleError(ex.getLocalizedMessage(), null, null, JOptionPane.DEFAULT_OPTION, null);
            myHandler = null;
        }
        return myHandler;
    }

    /**
     * Configure LiveDataHandler object. Can be called upon form initialization or at identification of OPC Server and
     * Tag(s). - An instance of this class can handle x number of server - An server can handle x number of tags
     *
     * @param server
     *            Connection string of OPC Server to be registered.
     * @param tag
     *            String array of OPC Tag names to be registered.
     * @throws FTException
     *             Thrown in case OPC configuration error.
     */

    public LiveDataGroup createLiveDataGroup(final String server, final String groupId, final String[] groupTags) {
        LiveDataGroup tempGroup = null;

        try {
            final LiveDataServer tempServer = getServerCustom(server);
            tempGroup = createGroup(tempServer, groupId);
            LiveDataItem[] tempItemArray = new LiveDataItem[groupTags.length];

            for (int x = 0; x < groupTags.length; x++) {
                tempItemArray[x] = getItem(tempGroup, groupTags[x]);
            }
        } catch (FTException e) {
            getExceptionHandler().handleError(e.getLocalizedMessage(), null, null, JOptionPane.DEFAULT_OPTION, e);
        }

        return tempGroup;
    }

    @Deprecated
    public AsyncTransaction asyncWrite(final LiveDataGroup group, final String[] tag, final Object[] value) {
        AsyncTransaction transaction = null;

        final Object obj = genericWriteTag(group, tag, value, false);

        if (obj instanceof AsyncTransaction) {
            transaction = (AsyncTransaction) obj;
        }

        return transaction;
    }

    /**
     * Writes multiple OPC tag on a given OPC server (Sync write). If not already
     * configured ({@link #configureLiveData(String, String[])})
     * the OPC server and OPC tag will be automatically added.
     *
     * @param server
     *            Connection string of OPC Server to be registered.
     * @param tag
     *            string array of OPC tags to be set.
     * @param value
     *            Value array to be set.
     * @return Array containing null or {@link com.rockwell.livedata.FTException} objects in case of write error
     */
    public FTException[] syncWrite(final LiveDataGroup group, final String[] tag, final Object[] value) {
        FTException[] exceptions = null;
        Object obj = genericWriteTag(group, tag, value, true);

        if (obj instanceof FTException[]) {
            exceptions = (FTException[]) obj;
        }

        return exceptions;
    }

    @Deprecated
    public AsyncTransaction asyncRead(final LiveDataGroup group, final String[] tag) {
        AsyncTransaction result = null;
        Object obj = genericReadTag(group, tag, false);

        if (obj instanceof AsyncTransaction) {
            result = (AsyncTransaction) obj;
        }

        return result;
    }

    /**
     * Read a set of OPC tags from a given OPC server (Sync read). If not already
     * configured ({@link #configureLiveData(String, String[])})
     * the OPC server or OPC tag(s) will be automatically added to the object.
     *
     * @param server
     *            Connection string of OPC Server to be registered.
     * @param tag
     *            String array of OPC Tag names to be registered.
     * @return Transaction Object
     */
    public ItemData[] syncRead(final LiveDataGroup group, final String[] tag) {
        ItemData[] result = null;
        Object obj = genericReadTag(group, tag, true);

        if (obj instanceof ItemData[]) {
            result = (ItemData[]) obj;
        }

        return result;
    }

    /**
     * This function will return a string representation of the current LiveData configuration.
     *
     * @return All configured OPC servers and configured OPC tags
     */
    @Deprecated
    public String getLiveDataConfiguration() {
        return getLiveDataConfiguration(LIVEDATAHANDLER);
    }

    /**
     * This function will return a string representation of a given LiveData configuration.
     *
     * @param externalLiveData
     *            LiveData object to be analyzed
     * @return All configured OPC servers and configured OPC tags
     */
    @Deprecated
    public String getLiveDataConfiguration(final LiveData externalLiveData) {
        StringBuilder configuration = new StringBuilder();
        configuration.append("FTLD Object \n");

        for (LiveDataServer ld : externalLiveData.getServers()) {
            configuration.append("\tServer: " + ld.toString() + "\n");
            for (LiveDataGroup ldg : ld.getGroups().getGroups()) {
                configuration.append("\t\tGroup: " + ldg.getName() + "\n");
                for (LiveDataItem ldi : ldg.getItems().getItems()) {
                    configuration.append("\t\t\tItem: " + ldi.getName() + "\n");
                }
            }
        }
        return configuration.toString();
    }

    @Deprecated
    public void addGroupEventListener(final FTLDGroupListener listener) {
        try {
            for (LiveDataServer ld : getServers()) {
                for (LiveDataGroup ldg : ld.getGroups().getGroups()) {
                    FTLDGroup ftldGroup = ldg.getFTLDGroup();
                    ftldGroup.removeEventListener(listener);
                    ftldGroup.addEventListener(listener);
                }
            }
        } catch (FTException e) {
            getExceptionHandler().handleError(e.getLocalizedMessage(), null, null, JOptionPane.DEFAULT_OPTION, e);
        }
    }

    private LiveDataServer getServerCustom(final String server) throws FTException {
        LiveDataServer ftldServer = getServer(server);

        if (ftldServer == null) {
            ftldServer = addServer(server);
        }

        try {
            ftldServer.getFTLDServer();
        } catch (NullPointerException ex) { // NOPMD - hpl: paranoid mode
            removeServer(ftldServer);
            String[] args = {server};
            throw new FTException(UIHelper.getLocalizedMessage("liveData_no_valid_opc_server", args));
        }

        return ftldServer;
    }

    private LiveDataGroup getGroup(final LiveDataServer ftldServer, final String groupName) {
        LiveDataGroups ftldGroups = ftldServer.getGroups();
        LiveDataGroup ftldGroup = ftldGroups.getGroup(groupName);

        return ftldGroup;
    }

    private LiveDataGroup createGroup(final LiveDataServer ftldServer, final String groupName) {
        LiveDataGroup ftldGroup = getGroup(ftldServer, groupName);

        if (ftldGroup == null) {
            try {
                LiveDataGroups ftldGroups = ftldServer.getGroups();
                ftldGroup = ftldGroups.addGroup(groupName);
                ftldGroup.setUpdateRate(LIVEDATA_UPDATE_RATE);
                ftldGroup.setActive(true);
            } catch (FTException e) {
                getExceptionHandler().handleError(e.getLocalizedMessage(), null
                        , null, JOptionPane.DEFAULT_OPTION, e);
            }
        }

        return ftldGroup;
    }

    public LiveDataGroup getGroup(final String groupName, final String server) {
        LiveDataGroup group = null;
        try {
            LiveDataServer ftldServer = getServerCustom(server);
            group = getGroup(ftldServer, groupName);
        } catch (FTException e) {
            getExceptionHandler().handleError(e.getLocalizedMessage(), null
                    , null, JOptionPane.DEFAULT_OPTION, e);
        }

        return group;
    }

    private LiveDataItem getItem(final LiveDataGroup ftldGroup, final String itemName) throws FTException {
        boolean isLiveDataItemSet = false;

        LiveDataItems ftldItems = ftldGroup.getItems();
        LiveDataItem ftldItem = null;

        for (LiveDataItem ftldItemTemp : ftldItems.getItems()) {
            if (ftldItemTemp.getName().equalsIgnoreCase(itemName)) {
                ftldItem = ftldItemTemp;
                isLiveDataItemSet = true;
                break;
            }
        }

        if (!isLiveDataItemSet) {
            ftldItem = ftldItems.addItem(itemName);
        }

        return ftldItem;
    }

    private Object genericReadTag(final LiveDataGroup group, final String[] tags, final boolean sync) {
        Object transaction = null;

        try {
            LiveDataItem[] tempItemArray = new LiveDataItem[tags.length];

            for (int x = 0; x < tags.length; x++) {
                tempItemArray[x] = getItem(group, tags[x]);
            }

            if (sync) {
                transaction = group.syncRead(LiveDataSource.OPCDevice, tempItemArray);
            } else {
                transaction = group.asyncRead(tempItemArray);
            }

        } catch (FTException e) {
            getExceptionHandler().handleError(e.getLocalizedMessage(), null, null, JOptionPane.DEFAULT_OPTION, e);
        }

        return transaction;
    }

    private Object genericWriteTag(final LiveDataGroup group, final String[] tags
            , final Object[] values, final boolean sync) {
        Object result = null;

        if (tags.length != values.length) {
            getExceptionHandler().handleError(UIHelper.getLocalizedMessage("liveData_configuration_error")
                    , null, null, null, JOptionPane.DEFAULT_OPTION);
            return result;
        } else if (tags.length == 0) {
            getExceptionHandler().handleError(UIHelper.getLocalizedMessage("liveData_not_tags_to read")
                    , null, null, null, JOptionPane.DEFAULT_OPTION);
            return result;
        }

        try {
            LiveDataItem[] tempItemArray = new LiveDataItem[tags.length];
            Object[] tempValueArray = new Object[values.length];

            for (int x = 0; x < tags.length; x++) {
                tempItemArray[x] = getItem(group, tags[x]);
                tempValueArray[x] = values[x];
            }

            if (sync) {
                result = group.syncWrite(tempItemArray, tempValueArray);
            } else {
                result = group.asyncWrite(tempItemArray, tempValueArray);
            }

        } catch (FTException e) {
            getExceptionHandler().handleError(e.getLocalizedMessage(), null, null, JOptionPane.DEFAULT_OPTION, e);
        }

        return result;
    }

    private static IExceptionHandler getExceptionHandler() {
        ServiceFactory serviceFactory = ServiceFactory.getInstance();
        return (IExceptionHandler) serviceFactory.getService(IExceptionHandler.class, "ExceptionHandler");
    }
}
