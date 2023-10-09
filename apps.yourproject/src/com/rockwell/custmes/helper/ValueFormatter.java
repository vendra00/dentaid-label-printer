package com.rockwell.custmes.helper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.client.DatasweepException;
import com.datasweep.compatibility.client.Location;
import com.datasweep.compatibility.client.Sublot;
import com.datasweep.plantops.common.measuredvalue.IMeasuredValue;
import com.rockwell.mes.services.eqm.ifc.IMESEquipment;

/**
 * This class transforms an object to a human readable string representation.
 * <p>
 * 
 * @author rweinga, (c) Copyright 2009 Rockwell Automation Technologies, Inc. All Rights Reserved.
 */
public class ValueFormatter {

    private static final Log LOGGER = LogFactory.getLog(ValueFormatter.class);

    /** transform an object to a localized string for MobileEditActivity controls */
    public String toString(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Location) {
            Location loc = (Location) obj;
            return loc.getDescription();

        } else if (obj instanceof IMESEquipment) {
            IMESEquipment eq = (IMESEquipment) obj;
            return eq.getName();
        } else if (obj instanceof Sublot) {
            Sublot sl = (Sublot) obj;
            Object tdl;
            try {
                tdl = sl.getUDA("ct_tempDrumLabel");
                if (tdl != null) {
                    return sl.getName() + "/" + tdl;
                }
            } catch (DatasweepException e) {
                LOGGER.error("Cannot read temporary Drum label", e);
            }
            return sl.getName();
        } else if (obj instanceof IMeasuredValue) {
            // the assumption for this case is that the value returned is used for display only.
            // on the display we always want a scale of 1.
            return DataTypeHelper.format((IMeasuredValue) obj);
        }
        return obj.toString();
    }
}
