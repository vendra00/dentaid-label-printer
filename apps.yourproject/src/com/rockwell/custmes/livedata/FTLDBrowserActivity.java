package com.rockwell.custmes.livedata;

import java.awt.Dimension;

import com.datasweep.compatibility.client.Response;
import com.datasweep.compatibility.ui.ActivityControl;
import com.datasweep.compatibility.ui.BorderStyle;
import com.datasweep.compatibility.ui.FlatButton;
import com.datasweep.compatibility.ui.LayoutStyle;
import com.datasweep.compatibility.ui.MessageIdHolder;
import com.datasweep.compatibility.ui.MessagesHolder;
import com.datasweep.compatibility.ui.Panel;
import com.datasweep.compatibility.ui.TreeView;
import com.datasweep.plantops.swing.ControlDock;
import com.datasweep.plantops.swing.MultiFlowLayout;
import com.rockwell.activity.CComponentEvent;
import com.rockwell.activity.CComponentEventListener;
import com.rockwell.activity.ItemDescriptor;
import com.rockwell.livedata.LiveData;
import com.rockwell.mes.commons.base.ifc.IExceptionHandler;
import com.rockwell.mes.commons.base.ifc.services.ServiceFactory;

/**
 * The class provides the base GUI for OPC Tag and Server browsing.
 *
 * @author spunzman
 */
class FTLDBrowserActivity extends ActivityControl implements
        CComponentEventListener {

    private static final int BUTTONS_PREF_WIDTH = 40;

    private static final int TREE_PREF_WIDTH = 360;

    private static final int BROWSER_PREF_HEIGHT = 400;

    private static final int BROWSER_PREF_WIDTH = 600;

    private static final int BUTTON_PREF_HEIGHT = 25;

    private static final int BUTTON_PREF_WIDTH = 100;

    /**
     * Input / Output Parameter
     */
    protected static final String LIVE_DATA_OBJECT = "LIVE_DATA_OBJECT";
    protected static final String OUTPUT_ITEM_NAME = "OUTPUT_SELECTED_TAG";

    /**
     * UI Elements
     */
    protected TreeView tree;
    private FlatButton okButton;
    private FlatButton cancelButton;

    protected LiveData liveData;
    protected String outputItem;
    protected String messageWhenNoNodeSelected;
    protected static final String MSG_PACK = "ct_ui_Equipment";
    protected MessagesHolder msgPack = new MessagesHolder(this);

    public FTLDBrowserActivity() {
        super();

        msgPack.setMessagesName("ct_ui_Equipment");

        setPreferredSize(new Dimension(BROWSER_PREF_WIDTH, BROWSER_PREF_HEIGHT));
        tree = new TreeView();
        tree.setPreferredSize(new Dimension(BROWSER_PREF_WIDTH, TREE_PREF_WIDTH));
        tree.setDock(ControlDock.TOP);
        tree.setName("GenericTree");

        Panel buttonPanel = new Panel();
        buttonPanel.setName("ButtonPanel");
        buttonPanel.setPreferredSize(new Dimension(BROWSER_PREF_WIDTH, BUTTONS_PREF_WIDTH));
        buttonPanel.setDock(ControlDock.BOTTOM);
        buttonPanel.setLayoutStyle(LayoutStyle.FLOW);
        buttonPanel.setFlowAlignment(MultiFlowLayout.RIGHT);
        buttonPanel.setBorderStyle(BorderStyle.NONE);

        buttonPanel.add(generateOkButton());
        buttonPanel.add(generateCancelButton());
        add(tree);
        add(buttonPanel);
    }

    protected final FlatButton generateOkButton() {
        okButton = new FlatButton();
        okButton.setName("okButton");
        okButton.setPreferredSize(new Dimension(BUTTON_PREF_WIDTH, BUTTON_PREF_HEIGHT));
        okButton.setText("Ok");

        MessageIdHolder mh = new MessageIdHolder();
        mh.setMessageId("ok_Button");
        okButton.setTextId(mh);
        okButton.setMessages(msgPack);

        okButton.addCComponentEventListener(this);
        return okButton;
    }

    protected final FlatButton generateCancelButton() {
        cancelButton = new FlatButton();
        cancelButton.setName("cancelButton");
        cancelButton.setPreferredSize(new Dimension(BUTTON_PREF_WIDTH, BUTTON_PREF_HEIGHT));
        cancelButton.setText("Cancel");

        MessageIdHolder mh = new MessageIdHolder();
        mh.setMessageId("cancel_Button");
        cancelButton.setTextId(mh);
        cancelButton.setMessages(msgPack);

        cancelButton.addCComponentEventListener(this);
        return cancelButton;
    }

    protected static IExceptionHandler getExceptionHandler() {
        ServiceFactory serviceFactory = ServiceFactory.getInstance();
        return (IExceptionHandler) serviceFactory.getService(
                IExceptionHandler.class, "ExceptionHandler");
    }

    protected final String getMessage(String msgId) {
        return getFunctions().createMessageFromID(MSG_PACK, msgId, null);
    }

    protected void handleEvent(Object src, String evt) {
        if (src.equals(getCancelButton()) && evt.equals(FlatButton.CLICK)) {
            getFunctions().closeDialog(1);
        }
    }

    @Override
    public Object ccomponentEventFired(CComponentEvent ev) {
        Object src = ev.getSource();
        String evt = ev.getEvent();
        handleEvent(src, evt);
        return null;
    }

    @Override
    protected String[] getActivityEvents() {
        return null;
    }

    protected FlatButton getOkButton() {
        return okButton;
    }

    protected FlatButton getCancelButton() {
        return cancelButton;
    }

    @Override
    public ItemDescriptor[] inputDescriptors() {
        return null;
    }

    @Override
    protected void inputItemSet(String key, Object value) {
    }

    @Override
    public Response activityExecute() {
        return new Response();
    }

    @Override
    protected void configurationItemSet(String arg0, Object arg1) {
    }

    @Override
    protected void configurationLoaded() {
    }

    @Override
    public String getActivityDescription() {
        return null;
    }

    @Override
    public ItemDescriptor[] outputDescriptors() {
        return null;
    }

    @Override
    protected void shutdown() {
    }

    @Override
    protected void startup() {
    }

    @Override
    protected void updateAfterExecute() {
    }

    public ItemDescriptor[] configurationDescriptors() {
        return null;
    }
}
