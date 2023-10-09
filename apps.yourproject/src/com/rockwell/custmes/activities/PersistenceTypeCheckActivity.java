package com.rockwell.custmes.activities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.client.Response;
import com.datasweep.compatibility.client.RuntimeActivitySet;
import com.datasweep.compatibility.client.RuntimeActivitySetStep;
import com.rockwell.activity.Activity;
import com.rockwell.activity.ItemDescriptor;

public class PersistenceTypeCheckActivity extends Activity {

    private static final Log LOGGER = LogFactory.getLog(PersistenceTypeCheckActivity.class);

    @Override
    public Response activityExecute() {
        try {

            RuntimeActivitySetStep rStep = getParentRuntimeStep();

            RuntimeActivitySet rSet = rStep.getRuntimeActivitySet();

            int setType = rSet.getActivitySet().getPersistenceType();
            int rSetType = rSet.getPersistenceType();
            StringBuilder msg = new StringBuilder("Runtime step: " + rSet.getName() + "/" + rStep.getName() + "\n"
                    + "Activity set persistence type (expected): " + setType + "\n"
                    + "Runtime set persistence type (actual): " + rSetType);
            LOGGER.debug(rSetType);
            if (setType != rSetType) {
                msg.append("\nUNEXPECTED PERSISSTENCE TYPE");
            }
            getParent().getEnvironmentGeneral().println(new Object[] { msg.toString() });
        } catch (Exception e) {
            return createErrorResponse(e);
        }
        return new Response();
    }

    @Override
    public ItemDescriptor[] configurationDescriptors() {
        return null;
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
    public String[] getActivityEvents() {
        return null;
    }

    @Override
    public String getBaseName() {
        return null;
    }

    @Override
    public ItemDescriptor[] inputDescriptors() {
        return null;
    }

    @Override
    protected void inputItemSet(String arg0, Object arg1) {
    }

    @Override
    public ItemDescriptor[] outputDescriptors() {
        return null;
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void startup() {
    }

}
