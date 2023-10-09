package com.rockwell.custmes.commons.base.ifc.nameduda;

import com.datasweep.compatibility.client.ProcessOrderItem;

public class CustMESNamedUDAProcessOrderItem {
    public static final String UDA_CT_PLANT = "ct_plant";

    public static final String UDA_CT_WBS = "ct_WorkBreakdown";

    /**
     * @param obj the Object containing the named UDA
     * @return Returns the UDA_CT_PLANT.
     */
    public static String getPlant(ProcessOrderItem obj) {
        try {
            return (String) obj.getUDA(UDA_CT_PLANT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param obj the Object containing the named UDA
     * @return Returns the UDA_CT_WBS.
     */
    public static String getWBS(ProcessOrderItem obj) {
        try {
            return (String) obj.getUDA(UDA_CT_WBS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generated method definition <br/>
     * 
     * @param obj the Object containing the named UDA
     * @param val the new value to be assigned to the named UDA
     */
    public static void setPlant(ProcessOrderItem obj, String val) {
        try {
            obj.setUDA(val, UDA_CT_PLANT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generated method definition <br/>
     * 
     * @param obj the Object containing the named UDA
     * @param val the new value to be assigned to the named UDA
     */
    public static void setWBS(ProcessOrderItem obj, String val) {
        try {
            obj.setUDA(val, UDA_CT_WBS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
