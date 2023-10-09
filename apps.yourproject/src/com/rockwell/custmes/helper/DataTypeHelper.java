package com.rockwell.custmes.helper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.client.Keyed;
import com.datasweep.compatibility.client.Parameter;
import com.datasweep.compatibility.client.Response;
import com.datasweep.compatibility.ui.Time;
import com.datasweep.plantops.common.constants.IDataTypes;
import com.datasweep.plantops.common.measuredvalue.IMeasuredValue;
import com.rockwell.activityset.PersistentSimpleType;
import com.rockwell.mes.commons.base.ifc.configuration.MESConfiguration;
import com.rockwell.mes.commons.base.ifc.functional.MeasuredValueUtilities;
import com.rockwell.mes.commons.base.ifc.services.PCContext;

/**
 * Helper class for data type conversion
 * <p>
 *
 * @author rweinga
 */
public class DataTypeHelper {
    private static final Log LOGGER = LogFactory.getLog(DataTypeHelper.class);

    private static long decimalPlacesToDisplay = -1;

    private static final int DECIMAL_PLACES_TO_DISPLAY_DEFAULT = 1;

    private static long decimalPlacesToReportToSAP = -1;

    private static final int DECIMAL_PLACES_TO_REPORT_TO_SAP_DEFAULT = 1;

    /**
     * Constructor
     */
    private DataTypeHelper() {
        super();

    }

    private static PersistentSimpleType converter = new PersistentSimpleType();

    /**
     * Converts the specified string to the specified class
     *
     * @param dataType
     * @param strValue
     * @return
     */
    public static Object toObject(short dataType, String strValue) throws ConversionException {
        // null returns null
        if (strValue == null) {
            return null;
        }
        // Empty strings are also null, in case of NON strings. E.g. boolean or
        // number values
        if ("".equals(strValue) && dataType != IDataTypes.TYPE_STRING) {
            return null;
        }

        // Special treatment for dates
        try {
            if (IDataTypes.TYPE_DATETIME == dataType) {
                return new Time(strValue);
            } else if (IDataTypes.TYPE_MEASUREDVALUE == dataType) {
                return MeasuredValueUtilities.createMV(strValue);
            }

            Class<?> clz = getDataClass(dataType);
            return converter.fromString(clz, strValue);
        } catch (Throwable exc) { // NOPMD hpl - paranoid mode, catch everything
            LOGGER.error("Exception on data type conversion", exc);
            throw new ConversionException("Conversion to failed for input string " + strValue);
        }
    }

    /**
     * Converts the specified string to the specified class. Returns null in case of errors.
     *
     * @param dataType
     * @param strValue
     * @return
     */
    public static Object toObjectSafe(short dataType, String strValue) {
        try {
            return toObject(dataType, strValue);
        } catch (ConversionException e) {
            return null;
        }
    }

    /**
     * @param dataType
     * @return
     */
    public static Class<?> getDataClass(short dataType) {
        switch (dataType) {
        case IDataTypes.TYPE_STRING:
            return String.class;
        case IDataTypes.TYPE_MEASUREDVALUE:
            return IMeasuredValue.class;
        case IDataTypes.TYPE_DECIMAL:
            return BigDecimal.class;
        case IDataTypes.TYPE_INTEGER:
            return Integer.class;
        case IDataTypes.TYPE_LONG:
            return Long.class;
        case IDataTypes.TYPE_BOOLEAN:
            return Boolean.class;
        case IDataTypes.TYPE_DATETIME:
            return Time.class;
        default:
            return String.class;
        }
    }

    /**
     * @param clz
     * @return
     */
    public static short getDataType(Class<?> clz) {
        if (String.class.isAssignableFrom(clz)) {
            return IDataTypes.TYPE_STRING;
        } else if (IMeasuredValue.class.isAssignableFrom(clz)) {
            return IDataTypes.TYPE_MEASUREDVALUE;
        } else if (BigDecimal.class.isAssignableFrom(clz)) {
            return IDataTypes.TYPE_DECIMAL;
        } else if (Integer.class.isAssignableFrom(clz)) {
            return IDataTypes.TYPE_INTEGER;
        } else if (Integer.TYPE.isAssignableFrom(clz)) {
            return IDataTypes.TYPE_INTEGER;
        } else if (Long.class.isAssignableFrom(clz)) {
            return IDataTypes.TYPE_LONG;
        } else if (Long.TYPE.isAssignableFrom(clz)) {
            return IDataTypes.TYPE_LONG;
        } else if (Float.class.isAssignableFrom(clz)) {
            return IDataTypes.TYPE_FLOAT;
        } else if (Float.TYPE.isAssignableFrom(clz)) {
            return IDataTypes.TYPE_FLOAT;
        } else if (Boolean.class.isAssignableFrom(clz)) {
            return IDataTypes.TYPE_BOOLEAN;
        } else if (Boolean.TYPE.isAssignableFrom(clz)) {
            return IDataTypes.TYPE_BOOLEAN;
        } else if (Date.class.isAssignableFrom(clz)) {
            return IDataTypes.TYPE_DATETIME;
        } else if (Time.class.isAssignableFrom(clz)) {
            return IDataTypes.TYPE_DATETIME;
        }

        return IDataTypes.TYPE_STRING;
    }

    /**
     * Set the specified parameters on the given object. Saving the updates is supposed to be done by the caller.
     */
    public static Response setParameters(final Keyed keyed, final Map<String, Object> values) {
        if (keyed == null || values.isEmpty()) {
            return PCContext.getFunctions().createResponseObject(null);
        }

        try {
            // cache the parameter names
            List<Parameter> parameters = Arrays.asList(keyed.getParameters());
            List<String> parameterNames = new ArrayList<String>(parameters.size());
            for (Parameter para : parameters) {
                parameterNames.add(para.getName());
            }

            for (Entry<String, Object> entry : values.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                int keyIndex = parameterNames.indexOf(key);
                if (keyIndex != -1) {
                    Parameter parameter = parameters.get(keyIndex);
                    parameter.setValue(value);
                } else {
                    Parameter newParameter = keyed.addParameter(key);
                    newParameter.setValue(value);
                }
            }
            return PCContext.getFunctions().createResponseObject(null);

        } catch (Exception e) {
            LOGGER.error("unable to set parameters for Keyed object " + keyed.toString(), e);
            return UIHelper.createResponseObject(e);
        }
    }

    /**
     * Set the specified parameters on the given object. Saving the updates is supposed to be done by the caller.
     */
    public static Response setParameter(final Keyed keyed, final String key, final Object value) {
        if (keyed == null || value == null) {
            return PCContext.getFunctions().createResponseObject(null);
        }

        try {
            List<Parameter> parameters = Arrays.asList(keyed.getParameters());
            List<String> parameterNames = new ArrayList<String>(parameters.size());
            for (Parameter para : parameters) {
                parameterNames.add(para.getName());
            }

            int keyIndex = parameterNames.indexOf(key);
            if (keyIndex != -1) {
                Parameter parameter = parameters.get(keyIndex);
                parameter.setValue(value);
            } else {
                Parameter newParameter = keyed.addParameter(key);
                newParameter.setValue(value);
            }
            return PCContext.getFunctions().createResponseObject(null);
        } catch (Exception e) {
            LOGGER.error("unable to set parameters for Keyed object " + keyed.toString(), e);
            return UIHelper.createResponseObject(e);
        }
    }

    /**
     * @param keyed
     *            The keyed object. E.g. a sublot
     * @return Response, if successful contains a map K=name, V=value
     */
    public static Response getParameters(Keyed keyed) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            for (Parameter param : keyed.getParameters()) {
                map.put(param.getName(), param.getValue());
            }
            return PCContext.getFunctions().createResponseObject(map);
        } catch (Exception exc) {
            LOGGER.error("unable to set parameters for Keyed object " + keyed.toString(), exc);
            return UIHelper.createResponseObject(exc);
        }

    }

    /**
     * @return The default scale for measured values as specified in the application configuration
     *         "DecimalPlacesToDisplay".
     */
    public static int getDecimalPlacesToDisplay() {
        if (decimalPlacesToDisplay < 0) {
            decimalPlacesToDisplay = MESConfiguration.getMESConfiguration().getLong("DecimalPlacesToDisplay",
                    DECIMAL_PLACES_TO_DISPLAY_DEFAULT, "The number of decimal places to display for measured values");
        }
        return (int) decimalPlacesToDisplay;

    }

    /**
     * @return The default scale for measured values to be used when reporting to SAP as specified in the application
     *         configuration "DecimalPlacesToReportToSAP".
     */
    public static int getDecimalPlacesToReportToSAP() {
        if (decimalPlacesToReportToSAP < 0) {
            decimalPlacesToReportToSAP = MESConfiguration.getMESConfiguration().getLong("DecimalPlacesToReportToSAP",
                    DECIMAL_PLACES_TO_REPORT_TO_SAP_DEFAULT,
                    "The number of decimal places to be reported to SAP for measured values");
        }
        return (int) decimalPlacesToReportToSAP;

    }

    /**
     * Rounds a measured value to the default scale.
     *
     * @param value
     *            The measured value to round
     * @return
     */
    public static IMeasuredValue round(IMeasuredValue value) {

        return round(value, getDecimalPlacesToDisplay());
    }

    /**
     * Rounds a measured value to the given scale.
     *
     * @param value
     *            The measured value to round
     * @param scale
     *            the scale to round
     * @return
     */
    public static IMeasuredValue round(IMeasuredValue value, int scale) {
        if (value == null) {
            return value;
        }

        IMeasuredValue result = value;
        try {
            result = value.setScale(scale);
        } catch (Exception exc) {
            LOGGER.error("Error on setting scale", exc);
        }
        return result;
    }

    /**
     * @return the formatted output string for the measured value with the default scale.
     */
    public static String format(IMeasuredValue value) {
        return format(value, getDecimalPlacesToDisplay());
    }

    /**
     * @return the formatted output string for the measured value with the given scale.
     */
    public static String format(IMeasuredValue value, int scale) {
        IMeasuredValue mv = round(value, scale);
        if (mv != null) {
            return mv.toString();
        }
        return null;
    }

    /**
     * see the Javadoc about why we use a String in the constructor.
     * {@link http://java.sun.com/j2se/1.5.0/docs/api/java/math/BigDecimal.html#BigDecimal(double)}
     */
    public static Double roundToSap(Double value) {
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(getDecimalPlacesToReportToSAP(), BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }

}
