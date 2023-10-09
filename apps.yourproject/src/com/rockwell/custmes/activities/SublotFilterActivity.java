package com.rockwell.custmes.activities;

import com.datasweep.compatibility.client.Response;
import com.datasweep.compatibility.client.SublotFilter;
import com.datasweep.compatibility.ui.Edit;
import com.rockwell.activity.ItemDescriptor;
import com.rockwell.mes.clientfw.pec.ifc.view.NavigationBaseActivity;

public class SublotFilterActivity extends NavigationBaseActivity {
    private static final String OUTPUT_FILTER = "filter";
    private Edit edit;

    private static final int EDIT_WIDTH = 200;

    private static final int EDIT_HEIGHT = 20;

    public SublotFilterActivity() {
        edit = new Edit();
        edit.setName("editSublotName");
        edit.setActivityName("editSublotName");
        edit.setSize(EDIT_WIDTH, EDIT_HEIGHT);
        edit.setText("SL0000001");
        contentPane.add(edit);
    }

    @Override
    public Response activityExecute() {
        Response resp = super.activityExecute();

        SublotFilter filter = getFunctions().createSublotFilter();
        filter.forNameStartingWith(edit.getText());
        setOutputItem(OUTPUT_FILTER, filter);
        return resp;
    }

    @Override
    public String getActivityDescription() {
        return "Sublot filter";
    }

    @Override
    public ItemDescriptor[] outputDescriptors() {
        return new ItemDescriptor[] { new ItemDescriptor(OUTPUT_FILTER, this.getClass(), SublotFilter.class) };
    }
}
