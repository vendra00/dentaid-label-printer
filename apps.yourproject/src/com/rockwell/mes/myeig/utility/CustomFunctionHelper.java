package com.rockwell.mes.myeig.utility;

import org.apache.commons.lang3.StringUtils;

public class CustomFunctionHelper {

    /**
     * Utility method to build the unique batch name integrated by the two items: batch + name
     * 
     * @param batch - The batch name
     * @param material - The material name
     * @return Null if two parameters are empty. Otherwise the concatenation of the two params --> batch + name
     */
    public static String buildBatchName(String batch, String material) {
        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isEmpty(batch)) {
            sb.append(batch);
        }

        if (!StringUtils.isEmpty(material)) {
            sb.append(material);
        }

        if (sb.length() <= 0) {
            return null;
        } else {
            return sb.toString();
        }
    }

}
