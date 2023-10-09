package com.adasoft.phase.equipment;

import com.rockwell.ssb.ftps.parameter.eqm.RuntimePropertyLimitConfig0100;

public class RtPhaseRuntimeProperty {
	private String bundleIdentifier;
    private String userBundleIdentifier;
    private String propertyIdentifier;
    private String propertyValueIdentifier;
    private Object propertyValue;
    private RuntimePropertyLimitConfig0100 limitConfig;
    private boolean isValid;
}
