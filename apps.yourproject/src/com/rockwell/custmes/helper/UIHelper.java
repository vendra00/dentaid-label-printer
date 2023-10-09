package com.rockwell.custmes.helper;

import java.awt.Dimension;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

import com.datasweep.compatibility.client.Error;
import com.datasweep.compatibility.client.Response;
import com.datasweep.compatibility.ui.CControl;
import com.datasweep.compatibility.ui.Form;
import com.datasweep.compatibility.ui.MessageIdHolder;
import com.datasweep.compatibility.ui.MessagesHolder;
import com.rockwell.compatibility.ui.taskpane.TaskPaneControl;
import com.rockwell.compatibility.ui.taskpane.TaskPaneGroup;
import com.rockwell.mes.clientfw.commons.ifc.view.IWaitCursorManager;
import com.rockwell.mes.commons.base.ifc.BarcodeListener;
import com.rockwell.mes.commons.base.ifc.i18n.I18nMessageUtility;
import com.rockwell.mes.commons.base.ifc.services.PCContext;
import com.rockwell.mes.commons.base.ifc.services.ServiceFactory;

/**
 * This class provides some helper functions for following purposes:<br/>
 * 1. Do frequently reused code<br/>
 * 2. Do complex perhaps Java/Swing internal processing which would be error-prone in PNuts
 * 
 * @author rweinga
 */
public class UIHelper {

    /** Default message pack */
    public static final String MSG_PACK_ID = "ct_MobilePECMessages";

    /**
     * Constructor
     */
    private UIHelper() {
    }

    /** key to identify related task pane objects */
    public static final String TASKPANEITEM_OBJECT = "taskPaneItemObject";

    /**
     * Enhances the functionality of the task pane control to hold an object for each item. See also
     * TaskPaneControl.addTaskPaneItem
     * 
     * The object later can be accessed In the Pnuts click event like this
     * taskPaneItem.getValue(UIHelper::TASKPANEITEM_OBJECT)
     * 
     * @param tc Task pane control
     * @param group Task pane group
     * @param text Item text
     * @param tooltip Tool tip
     * @param image optional image
     * @param obj This is the object to be linked to the item
     * @return The relevant action in order to put additional context for later usage, if needed
     */
    public static Action addTaskPaneItem(TaskPaneControl tc, TaskPaneGroup group, String text, String tooltip, ImageIcon image, Object obj) {
        tc.addTaskPaneItem(group, text, tooltip, image);
        int count = group.getContentPane().getComponentCount();
        Object jComp = group.getContentPane().getComponent(count - 1);
        Action action = null;
        if (jComp instanceof JButton) {
            JButton button = (JButton) jComp;
            action = button.getAction();
            action.putValue(TASKPANEITEM_OBJECT, obj);
            return action;
        }
        return null;
    }

    /**
     * Initializes basic CControl properties for optimized GUI-Activity implementation.
     * 
     * @param ctrl Control to configure
     * @param name Name of the control, this will be set as "name" and "activityName"
     * @param x left position
     * @param y top position
     * @param width width
     * @param height height
     */
    public static void initializeControl(CControl ctrl, String name, int x, int y, int width, int height) {
        ctrl.setLocation(x, y);
        ctrl.setPreferredSize(new Dimension(width, height));
        ctrl.setName(name);
        ctrl.setActivityName(name);
    }

    /**
     * Creates message holders and initializes the control with them.
     */
    public static void setMessage(CControl ctrl, String msgPack, String msgId) {
        MessagesHolder holder = new MessagesHolder();
        holder.setMessagesName(msgPack);
        ctrl.setMessages(holder);

        MessageIdHolder idHolder = new MessageIdHolder();
        idHolder.setMessagesName(msgPack);
        idHolder.setMessageId(msgId);
        ctrl.setTextId(idHolder);

        ctrl.setText("!" + msgPack + "." + msgId);
    }

    /**
     * Creates message holders and initializes the control with them.
     */
    public static void setMessage(CControl ctrl, String msgId) {
        setMessage(ctrl, MSG_PACK_ID, msgId);
    }

    /**
     * Creates an error object based on the given message entry and adds it to the response.
     * <p>
     * We do not use the PC API here to avoid runtime exceptions
     */
    public static Response createResponseObject(String msgPackId, String msgId) {
        return createResponseObject(msgPackId, msgId, (String[]) null);
    }

    /**
     * Creates an error object based on the given message entry of the default message pack and adds it to the response.
     * <p>
     * We do not use the PC API here to avoid runtime exceptions
     */
    public static Response createResponseObject(String msgId) {
        return createResponseObject(MSG_PACK_ID, msgId, (String[]) null);
    }

    /**
     * Creates an error object with message pack and id names with corresponding arguments and adds it to the response.
     * <p>
     * We do not use the PC API here to avoid runtime exceptions
     * 
     */
    public static Response createResponseObject(String msgPackId, String msgId, String[] args) {
        I18nMessageUtility.getLocalizedMessage(msgPackId, msgId);
        return new Response(new Error(msgPackId, msgId, args, PCContext.getServerImpl()));
    }

    /**
     * Creates an error response object based on the given exception.
     */
    public static Response createResponseObject(Throwable exc) {
        return new Response(new Error(exc, PCContext.getServerImpl()));
    }

    private static int barcodeAllowed = 0;

    /**
     * Disables barcode scanning. Each time called it increases a counter, so it can be called multiple times for for
     * cascading dialogs.
     */
    public static void disableBarcodeScan() {
        barcodeAllowed++;
        if (barcodeAllowed == 1) {
            BarcodeListener.getBarcodeListener().destroy();
        }
    }

    /**
     * Allows barcode scanning, after it was disabled.Each time called it decreases a counter, so it can be called
     * multiple times for cascading dialogs.
     */
    public static void enableBarcodeScan() {
        barcodeAllowed--;
        if (barcodeAllowed == 0) {
            BarcodeListener.getBarcodeListener().initialize();
        }
    }

    /**
     * Initializes the WaitCursorManager, in order to have a glass pane configured.
     * 
     * This is done by PEC automatically, but probably some helper forms need to do this explicitly.
     */
    public static void initWaitCursor(Form form, String msg) {
        initWaitCursor(form, msg, null);
    }

    /**
     * Initializes the WaitCursorManager, in order to have a glass pane configured.
     * 
     * This is done by PEC automatically, but probably some helper forms need to do this explicitly.
     * 
     * @deprecated since PS 7.1 use {@link #initWaitCursor(Form, String)} instead
     */
    public static void initWaitCursor(Form form, String msg, CControl template) {
        IWaitCursorManager waitCursorManager = ServiceFactory.getService(IWaitCursorManager.class);
        waitCursorManager.setActiveComponent(form.getJComponent(), msg);

    }

    public static String getLocalizedMessage(String messageId, String[] args) {
        return I18nMessageUtility.getLocalizedMessage(MSG_PACK_ID, messageId, args);
    }

    public static String getLocalizedMessage(String messageId) {
        return I18nMessageUtility.getLocalizedMessage(MSG_PACK_ID, messageId);
    }

    /**
     * Returns the preferred size to set a component at in order to render an html string. You can specify the size of
     * one dimension.
     */
    public static Dimension getPreferredSize(String html, boolean width, int prefSize) {
        JLabel resizer = new JLabel();

        resizer.setText(html);

        View view = (View) resizer.getClientProperty(BasicHTML.propertyKey);

        view.setSize(width ? prefSize : 0, width ? 0 : prefSize);

        float w = view.getPreferredSpan(View.X_AXIS);
        float h = view.getPreferredSpan(View.Y_AXIS);

        return new Dimension((int) Math.ceil(w), (int) Math.ceil(h));
    }

}