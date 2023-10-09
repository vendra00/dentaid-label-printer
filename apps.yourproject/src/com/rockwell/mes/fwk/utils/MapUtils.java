package com.rockwell.mes.fwk.utils;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * <p>
 * @author sschotte, (c) Copyright 2010 Rockwell Automation Technologies, Inc. All Rights Reserved.
 */
public class MapUtils {
    
    
    
    /**
     * helper function: should be in a dedicated utilities class
     * @param mappings list of strings
     * @return map instance
     */
    public static Map<String, String> generateMap(List<String> mappings) {
        Map m = new HashMap<String, String>();
        for (String mapping : mappings) {
            int idx = mapping.indexOf("=");
            String key = mapping.substring(0, idx);
            String value = mapping.substring(idx + 1, mapping.length());
            m.put(key, value);
        }
        return m;
        
    }
    
    /**
    *
    * @param toString String
    * @return Hashtable
    */
   public static Hashtable getHashtableFromString(String toString) {
       Hashtable map = new Hashtable();
       if (toString.length() > 0) {
          int pos1 = toString.indexOf("{");
          int pos2 = toString.indexOf("}");
          if (pos1 < 0 || pos2 < 0) {
              return map;
          }
          String sub = toString.substring(pos1 + 1, pos2);
          StringTokenizer tupleTk = new StringTokenizer(sub, ",");
          while (tupleTk.hasMoreTokens()) {
              String tuple = tupleTk.nextToken();
              tuple = tuple.trim();
              int eqPos = tuple.indexOf("=");
              if (eqPos < 0) {
                  return map;
              }
              String key = tuple.substring(0, eqPos);
              String value = tuple.substring(eqPos + 1, tuple.length());
              map.put(key, value);
          }

       }
       return map;
   }
    

}
