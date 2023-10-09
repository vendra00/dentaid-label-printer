package com.rockwell.mes.myeig.utility;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.client.MeasuredValue;
import com.datasweep.compatibility.client.UnitOfMeasure;
import com.datasweep.compatibility.ui.Time;
import com.rockwell.integration.messaging.IMessagingConstants;
import com.rockwell.mes.commons.base.ifc.configuration.MESConfiguration;
import com.rockwell.mes.commons.base.ifc.functional.MeasuredValueUtilities;
import com.rockwell.mes.commons.base.ifc.services.PCContext;
import com.rockwell.mes.commons.base.ifc.sql.DataBaseUtility;
import com.rockwell.mes.commons.base.ifc.sql.DataBaseUtility.DBType;

/**
 * Helper methods for integration gateway
 * <p>
 * 
 * @author syim, (c) Copyright 2012 Rockwell Automation Technologies, Inc. All
 *         Rights Reserved.
 */
public class IntegrationGatewayHelper {

    /** LOGGER */
    private static final Log LOGGER = LogFactory.getLog(IntegrationGatewayHelper.class);

    /** The material mapping list */
    private static final String MATERIAL_TYPE_MAP_LIST = "eig_MaterialTypeMapping";

    /** The material UoM mapping list */
    private static final String UOM_MAP_LIST_NAME = "ct_UoMMapping";

    /** The material type mapping list */
    private static final String CUSTOMER_MATERIAL_TYPE_MAP_LIST = "eig_MaterialTypeMapping";

    /** The UoM blacklisted for materialUnits */
    private static final String UOM_BLACKLIST_MAP_LIST_NAME = "ct_UoMBlackListedMapping";

    /** The default material map */
    private static final String[] DEFAULT_MATERIAL_MAP = new String[] { "ROH,RawMaterial", "INTR,IntraMaterial",
            "VERP,PackagingMaterial", "HALB,SemiFinishedGoods", "FERT,FinishedGoods" };

    /** SAP date/time substring index constant */
    private static final int SAP_YEAR_BEGIN = 0;

    /** SAP date/time substring index constant */
    private static final int SAP_YEAR_END = 4;

    /** SAP date/time substring index constant */
    private static final int SAP_MONTH_BEGIN = 4;

    /** SAP date/time substring index constant */
    private static final int SAP_MONTH_END = 6;

    /** SAP date/time substring index constant */
    private static final int SAP_DAY_BEGIN = 6;

    /** SAP date/time substring index constant */
    private static final int SAP_DAY_END = 8;

    /** SAP date/time substring index constant */
    private static final int SAP_HOUR_BEGIN = 0;

    /** SAP date/time substring index constant */
    private static final int SAP_HOUR_END = 2;

    /** SAP date/time substring index constant */
    private static final int SAP_MINUTE_BEGIN = 2;

    /** SAP date/time substring index constant */
    private static final int SAP_MINUTE_END = 4;

    /** SAP date/time substring index constant */
    private static final int SAP_SECOND_BEGIN = 4;

    /** SAP date/time substring index constant */
    private static final int SAP_SECOND_END = 6;

    /** Constructor */
    private IntegrationGatewayHelper() {
    }

    /**
     * Inserts PCA Event objects into the database. NOTE: This method is a
     * temporary solution until the standard EIG framework API is available.
     * 
     * @param objectName The object name
     * @param objectKey The object key
     * @param eventDesc The event description
     * @return success code
     * @deprecated EIG standard framework will provided an API for this later.
     */
    public static int persistPcaEvent(String objectName, Long objectKey, String eventDesc) {

        boolean dbOracle = DataBaseUtility.getDatabaseType().equals(DBType.ORACLE);
        StringBuilder sb = new StringBuilder("INSERT INTO PCA_EVENTS (");
        if (dbOracle) {
            sb.append("event_id,");
        }
        sb.append("connector_id, event_priority, event_status, "
                + "object_name, object_key, object_verb, event_description) ");
        sb.append("\n  VALUES (");
        if (dbOracle) {
            sb.append("PCA_EVENT_ID_SEQ.nextval,");
        }
        sb.append("'ProductionCentreAdapter', 1, 0, '");
        sb.append(objectName);
        sb.append("', ");
        sb.append(objectKey.toString());
        sb.append(", '");
        sb.append(IMessagingConstants.VERB_CREATE);
        sb.append("', '");
        sb.append(eventDesc);
        sb.append("')");
        String sql = sb.toString();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(sql);
        }
        int[] result = PCContext.getFunctions().executeStatements(new String[] { sql });
        return result[0];
    }

    /**
     * Get the PCA_EVENTS key as string
     * 
     * @param objectName The object name
     * @param objectKey The object key
     * @return The key
     */
    public static String getPcaEventKey(String objectName, Long objectKey) {

        String stmt = "SELECT event_id from PCA_EVENTS where object_name = '" + objectName + "' and object_key = "
                + String.valueOf(objectKey);

        List res = PCContext.getFunctions().getArrayDataFromActive(stmt);
        String[] pcaRec = (String[]) res.get(0);
        String eventId = pcaRec[0];
        return eventId;
    }

    /**
     * Returns the PharmaSuite material type from the ERP type
     * 
     * @param erpType The ERP type
     * @return mesType The PharmaSuite material type
     */
    public static String erpMaterialTypeToMesType(String erpType) {
        String mesType = StringUtils.EMPTY;
        final List<String> typeList = MESConfiguration.getMESConfiguration().getList(MATERIAL_TYPE_MAP_LIST,
                Arrays.asList(DEFAULT_MATERIAL_MAP), "List of ERP to MES material types");
        for (Object types : typeList) {
            String[] itemTypes = types.toString().split(",");
            if ((itemTypes.length == 2) && itemTypes[0].equals(erpType)) {
                mesType = itemTypes[1];
            }
        }
        return mesType;
    }

    /**
     * Returns the PharmaSuite material type from the ERP type
     * 
     * @param erpType The ERP type
     * @return mesType The PharmaSuite material type
     */
    public static String CustomerErpMaterialTypeToMesType(String erpType) {
        String mesType = StringUtils.EMPTY;
        final List<String> typeList = PCContext.getFunctions().getList(CUSTOMER_MATERIAL_TYPE_MAP_LIST).getItems();
        for (Object types : typeList) {
            String[] itemTypes = types.toString().split(",");
            if ((itemTypes.length == 2) && itemTypes[0].equals(erpType)) {
                mesType = itemTypes[1];
            }
        }
        return mesType;
    }

    /**
     * Returns the PharmaSuite UoM from the ERP Uom
     * 
     * @param erpUoM The ERP type
     * @return mesType The PharmaSuite UoM
     */
    public static String erpUnitOfMeasureToMesUoM(String erpUoM) {
        String mesUoM = StringUtils.EMPTY;
        // final List<String> uomMappingList = MESConfiguration.getMESConfiguration().getList(UOM_MAP_LIST_NAME, null,
        // "List of UoM mapping.");
        final List<String> uomMappingList = PCContext.getFunctions().getList(UOM_MAP_LIST_NAME).getItems();
        for (Object uomMapping : uomMappingList) {
            String[] theMapping = uomMapping.toString().split(";");
            if ((theMapping.length == 2) && theMapping[0].equalsIgnoreCase(erpUoM)) {
                mesUoM = theMapping[1];
            }
        }
        return mesUoM;
    }

    /**
     * Returns the customized UoM blacklisted
     * 
     * @param value The ERP type
     * @return boolean true if the param "value" exists in the list
     */
    public static boolean isUoMMaterialUnitInBlackList(String value) {
        final List<String> uomMappingList = PCContext.getFunctions().getList(UOM_BLACKLIST_MAP_LIST_NAME).getItems();

        if (uomMappingList != null && !uomMappingList.isEmpty()) {
            return uomMappingList.stream().anyMatch(value::equalsIgnoreCase);
        }

        return false;
    }

    /**
     * Return a measured value
     * 
     * @param value The value to convert to MeasuredValue
     * @param unit The unit of measure to convert to MeasuredValue
     * @return The measuredValue
     */
    public static MeasuredValue getMeasuredValue(String value, UnitOfMeasure unit) {
        MeasuredValue mv = null;
        if (StringUtils.isNotBlank(value)) {
            mv = MeasuredValueUtilities.createMV(new BigDecimal(value).stripTrailingZeros(), unit);
        }
        return mv;
    }

    /**
     * Return a measured value
     * 
     * @param valueAndUnit The value and unit string to convert to MeasuredValue
     * @return The measuredValue
     */
    public static MeasuredValue getMeasuredValue(String valueAndUnit) {
        return MeasuredValueUtilities.createMV(valueAndUnit, new Locale("en"));
    }

    /**
     * takes in a time argument and returns a date string of format YYYYMMDD for
     * SAP
     * 
     * @param time Time
     * @return date string in the format of YYYYMMDD
     */
    public static String formatSapDateStringFromTime(Time time) {
        if (time == null) {
            return "29991231";
        }
        Integer day = time.getDay();
        Integer month = time.getMonth();
        Integer year = time.getYear();
        return year.toString() + StringUtils.leftPad(month.toString(), 2, "0")
                + StringUtils.leftPad(day.toString(), 2, "0");
    }

    /**
     * takes in a time argument and returns a time string of format HHMMSS for
     * SAP
     * 
     * @param time Time
     * @return date string in the format of HHMMSS
     */
    public static String formatSapTimeStringFromTime(Time time) {
        if (time == null) {
            return "000000";
        }
        Integer hour = time.getHour();
        Integer minute = time.getMinute();
        Integer second = time.getSecond();
        return StringUtils.leftPad(hour.toString(), 2, "0") + StringUtils.leftPad(minute.toString(), 2, "0")
                + StringUtils.leftPad(second.toString(), 2, "0");
    }

    /**
     * returns Time from SAP date String of format "YYYYMMDD" * wrong format
     * returns null
     * 
     * @param sapDateString SAP date string
     * @return Time
     */
    public static Time fromSapString(String sapDateString) {
        String value = StringUtils.strip(StringUtils.defaultString(sapDateString));
        Time time = null;
        if (value.matches("[0-9]{4}(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])")) {
            String day = sapDateString.substring(SAP_DAY_BEGIN, SAP_DAY_END);
            String month = sapDateString.substring(SAP_MONTH_BEGIN, SAP_MONTH_END);
            String year = sapDateString.substring(SAP_YEAR_BEGIN, SAP_YEAR_END);
            time = new Time(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
        }
        return time;
    }

    /**
     * returns Time from SAP date String of format "YYYYMMDD" and time String of
     * format HHMMSS
     * wrong format returns null
     * 
     * @param sapDateString SAP date string
     * @param sapTimeString SAP time string
     * @return TIME
     */
    public static Time fromSapString(String sapDateString, String sapTimeString) {
        String dateValue = StringUtils.strip(StringUtils.defaultString(sapDateString));
        String timeValue = StringUtils.strip(StringUtils.defaultString(sapTimeString));
        Time time = null;
        if (dateValue.matches("[0-9]{4}(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])")
                && timeValue.matches("([0-1][0-9]|[2][0-3])[0-5][0-9][0-5][0-9]")) {
            String year = sapDateString.substring(SAP_YEAR_BEGIN, SAP_YEAR_END);
            String month = sapDateString.substring(SAP_MONTH_BEGIN, SAP_MONTH_END);
            String day = sapDateString.substring(SAP_DAY_BEGIN, SAP_DAY_END);
            String hour = sapTimeString.substring(SAP_HOUR_BEGIN, SAP_HOUR_END);
            String minute = sapTimeString.substring(SAP_MINUTE_BEGIN, SAP_MINUTE_END);
            String second = sapTimeString.substring(SAP_SECOND_BEGIN, SAP_SECOND_END);
            time = new Time(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day),
                    Integer.parseInt(hour), Integer.parseInt(minute), Integer.parseInt(second));
        }
        return time;
    }
}
