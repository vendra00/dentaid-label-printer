package com.rockwell.mes.myeig.commons.activity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.client.Response;
import com.rockwell.activity.Activity;
import com.rockwell.activity.ConfigurationItem;
import com.rockwell.activity.ItemDescriptor;

/**
 * Provides base functionality for non GUI activities and no op default
 * implementations for all abstract methods. Annotated properties are used to
 * specify configuration, input and output items.
 * <p>
 * 
 * @author syim, (c) Copyright 2012 Rockwell Automation Technologies, Inc. All
 *         Rights Reserved.
 */
public abstract class AnnotatedNonGUIActivity extends Activity {

    /** logger */
    private static final Log LOGGER = LogFactory.getLog(AnnotatedNonGUIActivity.class);

    /** The delegator */
    private AnnotatedActivityDelegator delegator;

    /**
     * Constructor.
     */
    public AnnotatedNonGUIActivity() {
        delegator = AnnotatedActivityDelegator.getDelegator(this.getClass());
    }

    @Override
    public Response activityExecute() {
        return new Response();
    }

    @Override
    protected void configurationItemSet(String name, Object value) {
    }

    @Override
    protected void configurationLoaded() {
        if (configurationItems == null) {
            return;
        }
        for (ItemDescriptor desc : configurationDescriptors()) {
            String name = desc.getName();
            delegator.setItemSafe(this, name, configurationItems.get(name));
        }
    }

    @Override
    public String getActivityDescription() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String[] getActivityEvents() {
        return null;
    }

    @Override
    public ItemDescriptor[] inputDescriptors() {
        return delegator.inputDescriptors();
    }

    @Override
    protected void inputItemSet(String name, Object value) {
    }

    @Override
    public ItemDescriptor[] outputDescriptors() {
        return delegator.outputDescriptors();
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void startup() {
    }

    /**
     * @see com.datasweep.compatibility.ui.ActivityControl#updateAfterExecute()
     */
    protected void updateAfterExecute() {
    }

    @Override
    public ItemDescriptor[] configurationDescriptors() {
        return delegator.configurationDescriptors();
    }

    @Override
    public Object getConfigurationItem(String key) {
        try {
            return delegator.getItem(this, key);
        } catch (ActivityItemAccessException exc) {
            LOGGER.error("getConfigurationItem", exc);
            return super.getConfigurationItem(key);
        }
    }

    @Override
    public Object getInputItem(String key) {
        try {
            return delegator.getItem(this, key);
        } catch (ActivityItemAccessException exc) {
            LOGGER.error("getInputItem", exc);
            return super.getInputItem(key);
        }
    }

    @Override
    public Object getOutputItem(String key) {
        try {
            return delegator.getItem(this, key);
        } catch (ActivityItemAccessException exc) {
            LOGGER.error("getOutputItem", exc);
            return super.getOutputItem(key);
        }
    }

    @Override
    public void setConfigurationItem(String key, Object value) {
        delegator.setItemSafe(this, key, value);
        super.setConfigurationItem(key, value);
    }

    @Override
    public void setInputItem(String key, Object value) {
        delegator.setItemSafe(this, key, value);
        super.setInputItem(key, value);
    }

    @Override
    public ConfigurationItem[] getConfiguration() {
        ConfigurationItem[] result = super.getConfiguration();
        return result;
    }

    @Override
    public void setConfiguration(ConfigurationItem[] arg0) {
        super.setConfiguration(arg0);
    }

    @Override
    public String getBaseName() {
        return this.getClass().getSimpleName();
    }
}
