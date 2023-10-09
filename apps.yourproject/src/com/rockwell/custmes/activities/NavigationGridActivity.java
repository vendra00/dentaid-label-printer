package com.rockwell.custmes.activities; //NOPMD

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.Introspector;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.client.DatasweepException;
import com.datasweep.compatibility.client.Filter;
import com.datasweep.compatibility.client.Keyed;
import com.datasweep.compatibility.client.Response;
import com.datasweep.compatibility.client.Sublot;
import com.datasweep.compatibility.ui.CControl;
import com.datasweep.compatibility.ui.grid.RowEvent;
import com.datasweep.compatibility.ui.grid.RowEventHandler;
import com.datasweep.core.utility.SwingWorker;
import com.datasweep.plantops.swing.ControlDock;
import com.rockwell.activity.ItemDescriptor;
import com.rockwell.custmes.model.AnnotationUtility;
import com.rockwell.custmes.model.KeyedObject;
import com.rockwell.custmes.model.SublotView;
import com.rockwell.mes.clientfw.commons.ifc.view.activities.grid.GridBeanUtility;
import com.rockwell.mes.clientfw.pec.ifc.view.NavigationBaseActivity;
import com.rockwell.mes.clientfw.pec.ifc.view.NavigationPanel;
import com.rockwell.mes.clientfw.pmc.impl.view.activities.GridDataDictActivity;
import com.rockwell.mes.clientfw.pmc.impl.view.activities.grid.GridDataDictBaseActivity;
import com.rockwell.mes.commons.base.ifc.services.PCContext;
import com.rockwell.mes.commons.base.ifc.sql.ColumnDescriptor;

public class NavigationGridActivity extends NavigationBaseActivity {

    private static final int NINE = 9;

    private static final Log LOGGER = LogFactory.getLog(NavigationGridActivity.class);

    public static final String INPUT_FILTER = "filter";

    public static final String OUTPUT_OBJECT = "selectedObject";

    public static final String CONFIG_CLASS = "class";

    protected Class<?> boundClass;

    private GridDataDictActivity grid;

    private static String[] allowedBeanNames;

    private static final int BEAN_PROPERTY_START_INDEX = 3;

    public NavigationGridActivity() {
        grid = new GridDataDictActivity();
    }

    @Override
    protected void startup() {
        super.startup();
        grid.setDock(ControlDock.FILL);
        grid.addOnRowSelected(new RowEventHandler() {
            @Override
            public void dispatch(RowEvent evt) {
                setButtonSensitivity();
            }
        });

        boundClass = SublotView.class;
        try {
            boundClass = Class.forName((String) getConfigurationItem(CONFIG_CLASS));
        } catch (ClassNotFoundException e1) {
            LOGGER.error("cannot find bound class for this activity, exception says " + StringUtils.defaultString(e1.getMessage()));
        }

        ColumnDescriptor[] columnList = AnnotationUtility.getInstance().getColumnDescriptors(boundClass);

        grid.setConfigurationItem(GridDataDictActivity.CONFIG_COLUMN_COUNT, columnList.length - 1);

        for (int idx = 1; idx < columnList.length; idx++) {
            String appendix = intAsTwoDigitsString(idx);
            String configItemName = GridDataDictActivity.CONFIG_COLUMN_PREFIX + appendix;

            // Configure column descriptors
            String dbColumnName = columnList[idx].getName();
            Method meth = AnnotationUtility.getInstance().getMethod(boundClass, dbColumnName);
            grid.setConfigurationItem(configItemName, Introspector.decapitalize(meth.getName().substring(BEAN_PROPERTY_START_INDEX)));
        }

        grid.setConfigurationItem(GridDataDictActivity.CONFIG_BOUND_CLASS, boundClass.getName());
        grid.setConfigurationItem(GridDataDictActivity.CONFIG_WITH_SELECT_COLUMN, Boolean.FALSE);
        grid.setConfigurationItem(GridDataDictActivity.CONFIG_ALLOW_MULTI_SELECTION, Boolean.FALSE);
        grid.setConfigurationItem(GridDataDictActivity.CONFIG_ALLOW_USER_SELECTION, Boolean.TRUE);
        grid.setConfigurationItem(GridDataDictActivity.CONFIG_WITH_SELECT_BUTTONS, Boolean.FALSE);
        grid.setConfigurationItem(GridDataDictActivity.CONFIG_USE_SHORT_LABELS, Boolean.FALSE);
        grid.setConfigurationItem(GridDataDictActivity.CONFIG_SELECT_ON_DOUBLECLICK, Boolean.TRUE);

        grid.addComponentListener(new ComponentListener() {
            @Override
            public void componentHidden(ComponentEvent e) {
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentResized(ComponentEvent e) {
                grid.getGrid().resizeColumnsToFit(2);
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }
        });

        setButtonSensitivity();
        contentPane.add(grid);
    }

    private String intAsTwoDigitsString(int idx) {
        return ((idx <= NINE) ? "0" : "") + idx;
    }

    @Override
    public Response activityExecute() {
        Response resp = super.activityExecute();

        Object gridObj = grid.getSelectedRowObject();
        if (gridObj instanceof KeyedObject) {
            KeyedObject keyedObj = (KeyedObject) gridObj;
            try {
                Keyed obj = PCContext.getFunctions().getObject(getOutputClass(), keyedObj.getKey());
                setOutputItem(OUTPUT_OBJECT, obj);

            } catch (DatasweepException e) {
                LOGGER.error("problem when executing the activity, exception says " + StringUtils.defaultString(e.getMessage()));
            }
        }

        return resp;
    }

    private void setButtonSensitivity() {
        boolean enable = grid.getSelectedRowObject() != null;
        Map<String, CControl> navControls = navigationPanel.getNavigationControls();
        if (navControls.containsKey(NavigationPanel.PANEL_PREFIX + NavigationPanel.CCONTROL_NEXT)) {
            navControls.get(NavigationPanel.PANEL_PREFIX + NavigationPanel.CCONTROL_NEXT).setEnabled(enable);
        }

    }

    @Override
    public String getActivityDescription() {
        return "PEC Activity to visualize a grid";
    }

    @Override
    protected void inputItemSet(String key, final Object value) {
        super.inputItemSet(key, value);

        if (INPUT_FILTER.equals(key) && value != null) {
            SwingWorker sw = new SwingWorker() {

                @Override
                public Object construct() {
                    return AnnotationUtility.getInstance().fetchData((Class<?>) boundClass, (Filter) value);
                }

                @Override
                public void finished() {
                    super.finished();
                    List<?> lst = (List<?>) getValue();
                    grid.setObjects(lst);
                    if (!lst.isEmpty()) {
                        grid.setSelectedRow(0);
                    }
                    setButtonSensitivity();
                }
            };
            sw.start();
        }
    }

    @Override
    public ItemDescriptor[] inputDescriptors() {
        return new ItemDescriptor[] { new ItemDescriptor(INPUT_FILTER, this.getClass(), Filter.class) };
    }

    @Override
    public ItemDescriptor[] outputDescriptors() {
        Class<?> clz = getOutputClass();
        return new ItemDescriptor[] { new ItemDescriptor(OUTPUT_OBJECT, this.getClass(), clz) };
    }

    protected Class<?> getOutputClass() {
        Class<?> clz = Sublot.class;
        try {
            String className = (String) getConfigurationItem(CONFIG_CLASS);
            if (className == null) {
                clz = Object.class;
            } else {
                clz = Class.forName(className);
                clz = AnnotationUtility.getInstance().getType(clz);
            }
        } catch (ClassNotFoundException e) {
            LOGGER.error("cannot find output class, exception says " + StringUtils.defaultString(e.getMessage()));
        }
        return clz;
    }

    @Override
    public ItemDescriptor[] configurationDescriptors() {
        ItemDescriptor classDesc =
                GridDataDictBaseActivity.configItemListDescriptor(getAllowedBeanNames(), CONFIG_CLASS, CONFIG_CLASS, CONFIG_CLASS, this.getClass());
        return new ItemDescriptor[] { classDesc };
    }

    /**
     * The list of allowed values for column names. This list is cached to avoid expensive middle tier calls.
     *
     * @return String[] The list of allowed values for column names.
     */
    protected String[] getAllowedBeanNames() {
        if (allowedBeanNames == null || allowedBeanNames.length == 0) {
            allowedBeanNames = GridBeanUtility.getBeanClassList(this.getServer());
        }
        return allowedBeanNames;
    }

}
