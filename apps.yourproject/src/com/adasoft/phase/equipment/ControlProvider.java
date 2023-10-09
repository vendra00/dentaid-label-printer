package com.adasoft.phase.equipment;

import java.util.Map;

import javax.swing.JComponent;

import com.google.common.collect.Maps;
import com.rockwell.library.ftpc.guice.logger.InjectLogger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

public class ControlProvider {
	public static final String PROPERTIESPREVIEW = "PROPERTIESPREVIEW";
    public static final String PROPERTIESACTIVE = "PROPERTIESACTIVE";
    @InjectLogger
    private Log log;
    private final Map<String, JComponent> controls = Maps.newHashMap();
    
    public ControlProvider() {
    }
    
    public void register(String controlName, JComponent control) {
        String name = StringUtils.defaultString(controlName);
        if (this.controls.containsKey(name)) {
            this.log.warn("registering control named '" + name + "' twice, check your code");
        } else {
            this.log.debug("registering control named '" + name + "'");
            this.controls.put(name, control);
        }

    }
}
