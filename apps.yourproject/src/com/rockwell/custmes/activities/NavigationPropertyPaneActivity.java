package com.rockwell.custmes.activities;

import com.datasweep.compatibility.client.Filter;
import com.datasweep.compatibility.ui.CContainer;
import com.datasweep.plantops.swing.ControlDock;
import com.l2fprod.common.propertysheet.PropertySheet;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.rockwell.activity.ItemDescriptor;
import com.rockwell.compatibility.ui.propertypane.PropertyPaneControl;
import com.rockwell.mes.clientfw.pec.ifc.view.NavigationBaseActivity;

public class NavigationPropertyPaneActivity extends NavigationBaseActivity {

    private static final int ROW_HEIGHTIN_PIXEL = 21;

    public static final String INPUT_OBJECT = "object";

    private PropertyPaneControl propertyPane;

    private Object bean;

    public NavigationPropertyPaneActivity() {
        super();
        propertyPane = new PropertyPaneControl();
    }

    @Override
    protected void startup() {
        super.startup();
        propertyPane.setDock(ControlDock.FILL);

        PropertySheetPanel psp = (PropertySheetPanel) propertyPane.getNativeComponent();
        psp.getTable().setRowHeight(ROW_HEIGHTIN_PIXEL);
        psp.setMode(PropertySheet.VIEW_AS_CATEGORIES);

        contentPane.add(propertyPane);
    }

    @Override
    protected void inputItemSet(String key, final Object value) {
        super.inputItemSet(key, value);

        if (INPUT_OBJECT.equals(key) && value != null) {
            bean = value;
        }
    }

    @Override
    protected void setParent(CContainer parent) {
        super.setParent(parent);
        propertyPane.setObject(bean);
    }

    @Override
    public String getActivityDescription() {
        return "Shows a property pane for the given input item";
    }


    @Override
    public ItemDescriptor[] inputDescriptors() {
        return new ItemDescriptor[] { new ItemDescriptor(INPUT_OBJECT, this.getClass(), Filter.class) };
    }

}
