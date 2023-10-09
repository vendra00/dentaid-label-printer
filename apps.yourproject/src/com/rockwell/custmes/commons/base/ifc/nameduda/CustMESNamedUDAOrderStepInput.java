package com.rockwell.custmes.commons.base.ifc.nameduda;

import com.datasweep.compatibility.client.OrderStepInput;

public class CustMESNamedUDAOrderStepInput {

    /** Generated attribute definition */
    public static final String UDA_ISASKFORMAT = "ct_askForMat";
    
    public static final String UDA_RESERVATIONNUMBER = "ct_ReservationNumber";
    
    public static final String UDA_RESERVATIONPOSITION = "ct_ReservationPosition";

    public static final String UDA_STORAGELOCATION = "ct_StorageLocation";

    public static final String UDA_SPECIALSTOCK_INDICATOR = "ct_SpecialStock_Indicator";

    /**
     * Generated method definition <br/>
     * Flag to indicate if the ask for material is sent for this OSI if, it yes, then marked as 'true' else, false
     *
     * @param obj the Object containing the named UDA
     * @return the requested value
     */
    public static Long getIsAskForMat(OrderStepInput obj) {
        try {
            return (Long) obj.getUDA(UDA_ISASKFORMAT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generated method definition <br/>
     * Flag to indicate if the ask for material is sent for this OSI if, it yes, then marked as 'true' else, false
     * 
     * @param obj the Object containing the named UDA
     * @param val the new value to be assigned to the named UDA
     */
    public static void setIsAskForMat(OrderStepInput obj, Long val) {
        try {
            obj.setUDA(val, UDA_ISASKFORMAT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param obj the Object containing the named UDA
     * @return Returns the udaReservationnumber.
     */
    public static String getReservationnumber(OrderStepInput obj) {
        try {
            return (String) obj.getUDA(UDA_RESERVATIONNUMBER);
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
    public static void setReservationnumber(OrderStepInput obj, String val) {
        try {
            obj.setUDA(val, UDA_RESERVATIONNUMBER);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param obj the Object containing the named UDA
     * @return Returns the udaReservationposition.
     */
    public static String getReservationposition(OrderStepInput obj) {
        try {
            return (String) obj.getUDA(UDA_RESERVATIONPOSITION);
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
    public static void setReservationposition(OrderStepInput obj, String val) {
        try {
            obj.setUDA(val, UDA_RESERVATIONPOSITION);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param obj the Object containing the named UDA
     * @return Returns the udaStorageLocation.
     */
    public static String getStorageLocation(OrderStepInput obj) {
        try {
            return (String) obj.getUDA(UDA_STORAGELOCATION);
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
    public static void setStorageLocation(OrderStepInput obj, String val) {
        try {
            obj.setUDA(val, UDA_STORAGELOCATION);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param obj the Object containing the named UDA
     * @return Returns the udaSpecialstockIndicator.
     */
    public static String getSpecialstockIndicator(OrderStepInput obj) {
        try {
            return (String) obj.getUDA(UDA_SPECIALSTOCK_INDICATOR);
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
    public static void setSpecialstockIndicator(OrderStepInput obj, String val) {
        try {
            obj.setUDA(val, UDA_SPECIALSTOCK_INDICATOR);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
