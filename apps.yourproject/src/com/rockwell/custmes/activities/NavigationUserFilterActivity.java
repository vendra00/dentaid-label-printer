package com.rockwell.custmes.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.client.DatasweepException;
import com.datasweep.compatibility.client.Filter;
import com.datasweep.compatibility.client.NamedFilter;
import com.datasweep.compatibility.client.Response;
import com.datasweep.plantops.common.constants.filtering.IFilterComparisonOperators;
import com.datasweep.plantops.swing.ControlDock;
import com.rockwell.activity.ItemDescriptor;
import com.rockwell.custmes.model.AnnotationUtility;
import com.rockwell.custmes.model.UserFilterView;
import com.rockwell.mes.clientfw.pec.ifc.view.NavigationBaseActivity;
import com.rockwell.mes.clientfw.pmc.ifc.filters.IMESUFUserFilter;
import com.rockwell.mes.clientfw.pmc.ifc.filters.IMESUserFilterService;
import com.rockwell.mes.clientfw.pmc.impl.view.activities.UserFilterActivity;


public class NavigationUserFilterActivity extends NavigationBaseActivity {
    
    private static final Log LOGGER = LogFactory.getLog(NavigationUserFilterActivity.class);
    
    public static final String CONFIG_FILTER_NAME = "filterName";
    public static final String OUTPUT_FILTER = "filter";

    private UserFilterActivity filterAct;

    private static ItemDescriptor[] configItemDesc;
    private static ItemDescriptor[] outputItemDesc;

    private static Map<String, List<UserFilterActivity>> pool = new HashMap<String, List<UserFilterActivity>>();

    private static final int NO_ELEMENTS_PER_DESC = 3;

    public NavigationUserFilterActivity() {
        super();
    }

    protected UserFilterActivity getUFA() {
        if (filterAct == null) {
            String filterName = getFilterName();
            List<UserFilterActivity> lst = pool.get(filterName);
            if (lst == null || lst.isEmpty()) {
                filterAct = new UserFilterActivity();
                IMESUserFilterService service = filterAct.getFilterService();
                filterAct.setDock(ControlDock.FILL);
                NamedFilter filter = getFunctions().getFilterByName(filterName);
                IMESUFUserFilter userFilter;
                try {
                    userFilter = service.createUserFilter(filter);
                    filterAct.setUserFilterObject(userFilter, false);

                } catch (DatasweepException e) {
                    LOGGER.error("problem getting a user filter, exception says "
                            + StringUtils.defaultString(e.getMessage()));
                }

            } else {
                filterAct = lst.remove(0);
            }
        }
        return filterAct;
    }

    protected void putUFA() {
        UserFilterActivity ufa = filterAct;
        filterAct = null;
        if (ufa == null) {
            return;
        }
        List<UserFilterActivity> lst = pool.get(getFilterName());
        if (lst == null) {
            lst = new ArrayList<UserFilterActivity>();
            pool.put(getFilterName(), lst);
        }
        lst.add(ufa);
    }

    protected String getFilterName() {
        String filterName = (String) getConfigurationItem(CONFIG_FILTER_NAME);
        return filterName;
    }


    @Override
    protected void startup() {
        super.startup();
        contentPane.add(getUFA());
    }

    @Override
    protected void shutdown() {
        putUFA();
        super.shutdown();
    }

    @Override
    public Response activityExecute() {

        Response resp = super.activityExecute();
        IMESUserFilterService service = filterAct.getFilterService();

        IMESUFUserFilter userFilter = filterAct.getUserFilter();
        Filter filter;
        try {
            filter = service.createPCFilter(userFilter);
        } catch (DatasweepException exc) {
            return createErrorResponse(exc);
        }

        setOutputItem(OUTPUT_FILTER, filter);

        return resp;
    }

    @Override
    public String getActivityDescription() {
        return "PEC activity to visualize user filters";
    }

    @Override
    public ItemDescriptor[] configurationDescriptors() {

        if (configItemDesc == null) {
            List<UserFilterView> filters = AnnotationUtility.getInstance().fetchData(UserFilterView.class,
                    "objectClass", IFilterComparisonOperators.EQUAL_TO, "NamedFilter");
            Object[] filterDesc = new Object[filters.size() * NO_ELEMENTS_PER_DESC];
            for (int idx = 0; idx < filters.size(); idx++) {
                UserFilterView filterView = filters.get(idx);
                filterDesc[NO_ELEMENTS_PER_DESC * idx] = filterView.getName();
                filterDesc[NO_ELEMENTS_PER_DESC * idx + 1] = filterView.getName();
                filterDesc[NO_ELEMENTS_PER_DESC * idx + 2] = filterView.getName();
            }

            configItemDesc = new ItemDescriptor[] { ItemDescriptor.createItemDescriptor(this.getClass(),
                    CONFIG_FILTER_NAME, String.class, new Object[] { ItemDescriptor.SHORTDESCRIPTION,
                            "Name of a named filter", ItemDescriptor.DISPLAYNAME, "Filter",
                            ItemDescriptor.ENUMERATIONVALUES, filterDesc }) };
        }
        return configItemDesc;
    }


    @Override
    public ItemDescriptor[] outputDescriptors() {
        if (outputItemDesc == null) {
            outputItemDesc = new ItemDescriptor[] { new ItemDescriptor(OUTPUT_FILTER, this.getClass(), Filter.class) };
        }
        return outputItemDesc;
    }
}
