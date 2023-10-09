package com.rockwell.custmes.helper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import javax.swing.RepaintManager;

import org.apache.commons.lang3.StringUtils;

import pnuts.lang.Context;
import pnuts.lang.PnutsFunction;

import com.datasweep.compatibility.client.Batch;
import com.datasweep.compatibility.client.Carrier;
import com.datasweep.compatibility.client.Categorical;
import com.datasweep.compatibility.client.DCInstance;
import com.datasweep.compatibility.client.DataCollectionItem;
import com.datasweep.compatibility.client.DatasweepException;
import com.datasweep.compatibility.client.DsMessages;
import com.datasweep.compatibility.client.Error;
import com.datasweep.compatibility.client.FSMConfiguration;
import com.datasweep.compatibility.client.Filter;
import com.datasweep.compatibility.client.FlexibleStateModel;
import com.datasweep.compatibility.client.Keyed;
import com.datasweep.compatibility.client.Location;
import com.datasweep.compatibility.client.MeasuredValue;
import com.datasweep.compatibility.client.Response;
import com.datasweep.compatibility.client.RuntimeDCS;
import com.datasweep.compatibility.client.State;
import com.datasweep.compatibility.client.Sublot;
import com.datasweep.plantops.common.constants.filtering.IFilterComparisonOperators;
import com.datasweep.plantops.common.constants.filtering.IKeyedFilterAttributes;
import com.rockwell.mes.clientfw.commons.ifc.view.IWaitCursorManager;
import com.rockwell.mes.commons.base.ifc.exceptions.MESRuntimeException;
import com.rockwell.mes.commons.base.ifc.fsm.FSMConfigHelper;
import com.rockwell.mes.commons.base.ifc.i18n.I18nMessageUtility;
import com.rockwell.mes.commons.base.ifc.objects.MESATObject;
import com.rockwell.mes.commons.base.ifc.services.PCContext;
import com.rockwell.mes.commons.base.ifc.services.ServiceFactory;
import com.rockwell.mes.commons.base.ifc.services.TransactionInterceptor;
import com.rockwell.mes.services.eqm.ifc.GxPContextItemClass;
import com.rockwell.mes.services.eqm.ifc.IMESEquipment;
import com.rockwell.mes.services.eqm.ifc.IMESEquipmentService;
import com.rockwell.mes.services.eqm.ifc.IMESScaleEquipment;
import com.rockwell.mes.services.eqm.ifc.IMESScaleEquipmentService;
import com.rockwell.mes.services.inventory.ifc.ISublotService;
import com.rockwell.mes.services.inventory.ifc.TransactionHistoryContext;
import com.rockwell.mes.services.inventory.ifc.TransactionHistoryHandler;
import com.rockwell.mes.services.inventory.ifc.TransactionSubtype;
import com.rockwell.mes.services.inventory.ifc.TransactionType;
import com.rockwell.mes.services.inventory.impl.StorageLocationHelper;
import com.rockwell.mes.services.s88.ifc.execution.IRuntimeEntity;
import com.rockwell.mes.services.s88.ifc.execution.IExecuteInBoundContextConfiguration;

/**
 * This class is to provide easy access to the PC/PS data model. Since it is used in PNuts there should not be any
 * exceptions.
 * <p>
 *
 * @author rweinga
 */
public class DataModelHelper {

    /** Logger */
    private static final org.apache.commons.logging.Log LOGGER = org.apache.commons.logging.LogFactory.getLog(DataModelHelper.class);

    /* SQL Statement for sublot fetching assigned to mobile via history */
    public static final String SQL_SUBLOT = //
            "select distinct S.sublot_key from SUBLOT S" //
                    + " inner join BATCH B on (S.batch_key = B.batch_key)" //
                    + " inner join AT_X_TransactionHistory HIST on" //
                    + " (S.sublot_name = HIST.X_sublotIdentifierNew_S" //
                    + " and B.batch_name = HIST.X_batchIdentifierNew_S" //
                    + " and HIST.X_transactionType_I = %d" //
                    + " and HIST.X_transactionSubtype_I = %d)" //
                    + " WHERE HIST.X_clientContext_S = '%s'";

    public static final GxPContextItemClass BINDING_SUBLOT = GxPContextItemClass.Sublot;

    public static final GxPContextItemClass BINDING_BATCH = GxPContextItemClass.Batch;

    public static final GxPContextItemClass BINDING_LOCATION = GxPContextItemClass.Location;

    private DataModelHelper() {
    }

    /**
     * Returns a list of sublots registered for the given location
     */
    @SuppressWarnings("unchecked")
    public static List<Sublot> getSublots(Location loc) {
        try {
            Carrier bin = StorageLocationHelper.getBinOfStorageLocation(loc);
            return bin.getAllSublots();
        } catch (MESRuntimeException exc) {
            return new ArrayList<Sublot>();
        }
    }

    /**
     * Creates a new TransactionHistoryContext and sets the properties given in the parameters .
     *
     * @param context A context in order to identify the order again. E.g. a unique module id
     * @param order The order identifier
     * @return the transaction history context
     */
    public static TransactionHistoryContext createTHC(String context, String order) {
        return createTHC(context, order, null, null);
    }

    /**
     * Creates a new TransactionHistoryContext and sets the properties given in the parameters .
     *
     * @param context A context in order to identify the order again. E.g. a unique module id
     * @param order The order identifier
     * @param remark Comment
     * @return the transaction history context
     */
    public static TransactionHistoryContext createTHC(String context, String order, TransactionSubtype subType, String remark) {
        TransactionHistoryContext thc = new TransactionHistoryContext();
        thc.setClientContext(context);
        thc.setOrderIdentifier(order);
        thc.setRemark(remark);
        thc.setTransactionHistorySubtype(subType);
        return thc;
    }

    /**
     * Creates a transaction history entry for the given sublot. Once this is done it is possible to get the sublots
     * again with #getRelatedSublots.
     *
     * @param sublot
     * @param thc Transaction history context
     */
    public static void createTH(Sublot sublot, TransactionHistoryContext thc) {
        if (null == sublot) {
            return;
        }

        // Prepare transaction history handler
        TransactionHistoryHandler thHandler =
                new TransactionHistoryHandler(TransactionType.CHANGE_OF_SUBLOT_DATA, TransactionSubtype.IDENTIFICATION, thc);

        thHandler.historyDataBefore(sublot.getBatch(), sublot);

        // ... and write it
        thHandler.historyDataAfter(sublot.getBatch(), sublot);
    }

    public static String getSql(String clientContext, TransactionType type, TransactionSubtype subType) {
        final Formatter formatter = new Formatter();
        try {
            String sql = formatter.format(SQL_SUBLOT, type.getValue(), subType.getValue(), clientContext).toString();
            return sql;
        } finally {
            formatter.close();
        }
    }

    /**
     * This method shall be called in PNuts in order to process a PNuts function within a transaction. The behavior
     * depends on the return value of the PNuts function. <li>If the PNuts function returns a Response object: The
     * Response object and result is returned.In case the Response object is <b>not</b> OK, the transaction is rolled
     * back.</li> <li>The Pnuts function returns not a Response Object: A valid Response object is created and the PNuts
     * result is set as the Response result. A commit is performed.</li> <li>There is an exception during execution of
     * the PNuts function: A error-Response object with the Exception is returned</li>
     * <p>
     * <b>Example 1</b>
     * <p>
     *
     * <code>function func1(value) {<p>
     *    // do sth...<p>
     *    return createResponseObject(value)<p>
     * }<p>
     * res = DataModelHelper::callResponseMethodInTransaction(func1, ["A Parameter"], getContext())<p>
     * res.getValue() == "A Parameter" // This is true<p>
     * res.isOk() // This is true
     *  // After the call, everything is committed
     * </code>
     * <p>
     * <b>Example 2</b>
     * <p>
     * <code>function func2() {<p>
     *    // do sth...<p>
     *    return UIHelper::createResponseObject("msgPack", "msgId", null)<p>
     * }<p>
     * res = DataModelHelper::callResponseMethodInTransaction(func2, null, getContext())<p>
     * res.getValue() == null// This is true<p>
     * res.isError() // This is true<p>
     * // After the call, everything is rolled back
     * </code>
     * <p>
     * <b>Example 3</b>
     * <p>
     *
     * <code>function func3() {<p>
     *    // do sth...<p>
     *    return "AAA"<p>
     * }<p>
     * res = DataModelHelper::callResponseMethodInTransaction(func3, null, getContext())<p>
     * res.getValue() == "AAA" // This is true<p>
     * res.isOk() // This is true
     *  // After the call, everything is committed
     * </code>
     */
    public static Response callResponseMethodInTransaction(final PnutsFunction function, final Object[] params, final Context context) {
        final Response resultResponse = new Response();

        try {
            TransactionInterceptor.callInTransactionImpl(new Callable<Object>() {
                @Override
                public Object call() {
                    Object result = function.call(params == null ? new Object[] {} : params, context);
                    if (result instanceof Response) {
                        Response respObj = (Response) result;
                        resultResponse.addResponse(respObj);
                        resultResponse.setResult(respObj.getResult());
                        if (respObj.isError()) {
                            // Ensure that the transaction is rolled back on
                            // leaving
                            TransactionInterceptor.setRollback();
                        }
                    } else {
                        resultResponse.setResult(result);
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            LOGGER.error("Error during transaction", e);
            resultResponse.addError(new Error(e, PCContext.getServerImpl()));
        }

        return resultResponse;
    }

    /**
     * This method shall be called in PNuts in order to process a PNuts function. It is used in the method
     * callResponseMethodWithGlassPane() to get a kind of sandbox for long running task. The behavior depends on the
     * return value of the PNuts function. <li>If the PNuts function returns a Response object: The Response object and
     * result is returned. <li>The Pnuts function returns not a Response Object: A valid Response object is created and
     * the PNuts result is set as the Response result. A commit is performed.</li> <li>There is an exception during
     * execution of the PNuts function: A error-Response object with the Exception is returned</li>
     */
    public static Response callResponseMethod(final PnutsFunction function, final Object[] params, final Context context) {
        final Response resultResponse = new Response();

        try {
            Object result = function.call(params == null ? new Object[] {} : params, context);
            if (result instanceof Response) {
                Response respObj = (Response) result;
                resultResponse.addResponse(respObj);
                resultResponse.setResult(respObj.getResult());
            } else {
                resultResponse.setResult(result);
            }
        } catch (Exception e) {
            LOGGER.error("Error during execution", e);
            resultResponse.addError(new Error(e, PCContext.getServerImpl()));
        }

        return resultResponse;
    }

    /**
     * Like callResponseMethodInTransaction(PnutsFunction, Object[], Context) But shows the glass pane during the
     * transaction.
     *
     * @see #callResponseMethodInTransaction(PnutsFunction, Object[], Context)
     */
    public static Response callResponseMethodInTransactionWithGlassPane(final PnutsFunction function, final Object[] params, final Context context) {

        final IWaitCursorManager waitCursorManager =
                (IWaitCursorManager) ServiceFactory.getInstance().getService(IWaitCursorManager.class, "WaitCursorManager");
        waitCursorManager.startWaitCursor();
        try {
            RepaintManager mgr = RepaintManager.currentManager(null);
            // Just repaint here, since we do not leave the EDT during this call.
            // Invalidation is not necessary.
            // mgr.validateInvalidComponents();
            mgr.paintDirtyRegions();
            return callResponseMethodInTransaction(function, params, context);
        } finally {
            waitCursorManager.stopWaitCursor();
        }
    }

    /**
     * Provides a wrapper around callResponseMethod() to display the glasspane to indicate a long running task.
     */
    public static Response callResponseMethodWithGlassPane(final PnutsFunction function, final Object[] params, final Context context) {
        final IWaitCursorManager waitCursorManager =
                (IWaitCursorManager) ServiceFactory.getInstance().getService(IWaitCursorManager.class, "WaitCursorManager");
        waitCursorManager.startWaitCursor();
        try {
            RepaintManager mgr = RepaintManager.currentManager(null);
            // Just repaint here, since we do not leave the EDT during this call.
            // Invalidation is not necessary.
            // mgr.validateInvalidComponents();
            mgr.paintDirtyRegions();
            return callResponseMethod(function, params, context);
        } finally {
            waitCursorManager.stopWaitCursor();
        }
    }

    /**
     * Helper method for DCS tracking.
     * <p>
     * Writes one entry into a data collection set table and saves it to the database.
     *
     * @param obj The MESATObject into which content it should be written
     * @param dcsName Name of the DCS definition
     * @param value K=Name of the parameter, V=Parameter value
     */
    public static Response writeAndSaveDCS(MESATObject obj, String dcsName, Map<String, Object> value) {
        if (obj == null) {
            return UIHelper.createResponseObject(new IllegalArgumentException("Object must not be null"));
        }
        return writeAndSaveDCS(obj.getATRow(), dcsName, value);
    }

    /**
     * Helper method for DCS tracking.
     * <p>
     * Writes one entry into a data collection set table and saves it to the database.
     *
     * @param obj Any Keyed object
     * @param dcsName Name of the DCS definition
     * @param value K=Name of the parameter, V=Parameter value
     */
    public static Response writeAndSaveDCS(Keyed obj, String dcsName, Map<String, Object> value) {
        try {
            RuntimeDCS dcs = obj.getDCS(dcsName);
            DCInstance item = dcs.createDCInstance();

            for (Entry<String, Object> entry : value.entrySet()) {
                item.setValue(entry.getKey(), entry.getValue());
            }

            return dcs.save();
        } catch (Exception exc) {
            return UIHelper.createResponseObject(exc);
        }
    }

    /**
     * Returns a list of DCS items. Where the items are represented as a map. The list if DCS items is filtered by the
     * filter map. The items in the filter must equal.
     */
    public static List<Map<String, Object>> getDCS(MESATObject obj, String dcsName, Map<String, Object> filter) {
        return getDCS(obj.getATRow(), dcsName, filter);
    }

    /**
     * Returns a list of DCS items. Where the items are represented as a map.
     */
    public static List<Map<String, Object>> getDCS(MESATObject obj, String dcsName) {
        return getDCS(obj.getATRow(), dcsName);
    }

    /**
     * Returns a list of DCS items. Where the items are represented as a map. The list if DCS items is filtered by the
     * filter map. The items in the filter must equal.
     */
    public static List<Map<String, Object>> getDCS(Keyed obj, String dcsName, Map<String, Object> filter) {
        List<Map<String, Object>> result = getDCS(obj, dcsName);
        for (java.util.Iterator<Map<String, Object>> iter = result.iterator(); iter.hasNext();) {
            Map<String, Object> value = iter.next();
            for (Map.Entry<String, Object> entry : filter.entrySet()) {
                if (!entry.getValue().equals(value.get(entry.getKey()))) {
                    iter.remove();
                }
            }
        }

        return result;
    }

    /**
     * Returns a list of DCS items. Where the items are represented as a map.
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getDCS(Keyed obj, String dcsName) {
        List<Map<String, Object>> result = new LinkedList<Map<String, Object>>();

        RuntimeDCS dcs = obj.getDCS(dcsName);
        for (DCInstance instance : (List<DCInstance>) dcs.getAllInstances()) {
            Map<String, Object> map = new HashMap<String, Object>();
            for (DataCollectionItem item : (List<DataCollectionItem>) dcs.getDataCollectionItems()) {
                map.put(item.getName(), instance.getValue(item.getName()));
            }
            result.add(map);
        }

        return result;
    }

    public static List<?> forKeyList(Filter filter, Collection<?> lst) {
        // Vector fits the pnuts conventions
        if (lst.size() <= 0) {
            return Collections.EMPTY_LIST;
        }

        List<?> objList;
        if (lst instanceof List<?>) {
            objList = (List<?>) lst;
        } else {
            objList = new ArrayList<Object>(lst);
        }
        filter.addSearchBy(IKeyedFilterAttributes.KEY, IFilterComparisonOperators.IN, objList);
        try {
            return filter.exec();
        } catch (DatasweepException e) {
            LOGGER.error("Error on fetching data by key list", e);
            return Collections.EMPTY_LIST;
        }
    }

    public static Response bind(IMESScaleEquipment eq, String bindName, Keyed keyed) {
        IMESEquipmentService service =
                (IMESEquipmentService) ServiceFactory.getInstance().getService(IMESScaleEquipmentService.class, "MESScaleEquipmentService");

        try {
            GxPContextItemClass bind = GxPContextItemClass.valueOf(GxPContextItemClass.class, bindName);
			IRuntimeEntity rtEntity = null;
			IExecuteInBoundContextConfiguration bindingConfiguration = null;
            service.setGxPContext(eq, bind, keyed, rtEntity, bindingConfiguration);
        } catch (Throwable exc) { // NOPMD hpl - paranoid mode, catch everything
            return UIHelper.createResponseObject(exc);
        }
        return new Response();
    }

    public static Response unbind(IMESScaleEquipment eq, String bindName) {

        return new Response();
    }

    /**
     * @param obj The object to refresh
     * @return true=ok, otherwise false
     */
    public static boolean refreshObject(Object obj) {
        try {
            if (obj instanceof Categorical) {
                ((Categorical) obj).refresh();
                return true;
            } else if (obj instanceof MESATObject) {
                return ((MESATObject) obj).refresh();
            } else if (obj instanceof IMESEquipment) {
                ((IMESEquipment) obj).refresh();
                return true;
            }
        } catch (Throwable exc) { // NOPMD hpl - paranoid mode, catch everything
            LOGGER.error("Could not refresh object", exc);
        }
        return false;

    }

    /**
     * @param str string to evaluate for long compatibility
     * @return true=the string can be converted to a long, otherwise false.
     */
    public static boolean isLong(final String str) {
        try {
            Long.parseLong(str);
            return true;
        } catch (Throwable exc) { // NOPMD hpl - paranoid mode, catch everything
            return false;
        }
    }

    /**
     * @param str string to evaluate for long compatibility
     * @return true=the string can be converted to a long, otherwise false.
     */
    public static boolean isBigDecimal(final String str) {
        try {
            new BigDecimal(str);
            return true;
        } catch (Throwable exc) { // NOPMD hpl - paranoid mode, catch everything
            return false;
        }
    }

    /**
     * @param obj The ProductionCentre object
     * @param relationShipName State relationship of the object
     * @param states Array of state strings
     * @return Localized string of the requested state
     */
    public static String getLocalizedState(Object obj, String relationShipName, String[] states) {
        String[] localized = new String[states.length];
        for (int idx = 0; idx < states.length; idx++) {
            localized[idx] = getLocalizedState(obj, relationShipName, states[idx]);
        }
        return StringUtils.join(localized, ", ");
    }

    /**
     * @param obj The ProductionCentre object
     * @param relationShipName State relationship of the object
     * @param state State string
     * @return Localized string of the requested state
     */
    public static String getLocalizedState(Object obj, String relationShipName, String state) {
        Class<?> beanClass;
        if (obj instanceof Class<?>) {
            beanClass = (Class<?>) obj;
        } else {
            beanClass = obj.getClass();

        }
        String nonLocalized = "<" + state + ">";
        FSMConfiguration fsmConfig = FSMConfigHelper.getFsmConfigurationByClass(beanClass);

        if (null == fsmConfig) {
            return nonLocalized;
        }

        FlexibleStateModel fsm = FSMConfigHelper.getFsmModelByRelationship(fsmConfig, relationShipName);
        if (null == fsm) {
            return nonLocalized;
        }

        State st = fsm.getStateByName(state);

        if (st == null) {
            return nonLocalized;
        }

        String localized = st.getLocalizedName();
        if (StringUtils.isEmpty(localized)) {
            return nonLocalized;
        }
        return localized;
    }

    public static Response createSubLot(Batch batch, MeasuredValue subLotWeight, Location location, TransactionHistoryContext thc) {

        Response resultResponse = new Response();

        ISublotService service = (ISublotService) ServiceFactory.getInstance().getService(ISublotService.class, "SublotService");

        Sublot[] subLot;

        try {
            subLot = service.createSublots(batch, 1, subLotWeight, location, thc);
            resultResponse.setResult(subLot[0]);
        } catch (Exception e) {
            Error caughtError = new Error(e, PCContext.getServerImpl());
            resultResponse.addError(caughtError);
        }

        return resultResponse;
    }

    /**
     * Gets a localized message from a message pack key and a message id (platform only offers functions to get it via
     * the message pack name, but not the key)
     *
     * @param messagePackKey key of the message pack to retrieve a message from
     * @param messageId id of message to retrieve inside the message pack
     * @return message string, or null if localized message does not exist
     */
    public static String getLocalizedMessage(long messagePackKey, String messageId) {
        String msg = null;
        if (messagePackKey > 0) {
            boolean isMsgEmpty = StringUtils.isEmpty(messageId);
            if (!isMsgEmpty) {
                try {
                    // we do not use PCContext.getFunctions().getMessage(String messagesName, String messageId);
                    // because we already have the key of the message pack and can therefore
                    // take the shortcut here without getting the information again

                    DsMessages messagePack = (DsMessages) PCContext.getServerImpl().getMessagesManager().getObject(messagePackKey);
                    if (messagePack != null) {
                        msg = I18nMessageUtility.getLocalizedMessage(messagePack.getName(), messageId);
                    }
                } catch (DatasweepException e) {
                    msg = null;
                }
            }
        }
        return msg;
    }

}