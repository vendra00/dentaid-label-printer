package com.rockwell.custmes.model;


import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.datasweep.compatibility.client.DatasweepException;
import com.datasweep.compatibility.client.Filter;
import com.datasweep.compatibility.ui.Time;
import com.datasweep.plantops.common.constants.IDataTypes;
import com.datasweep.plantops.common.constants.filtering.IFilterComparisonOperators;
import com.datasweep.plantops.common.constants.filtering.IFilterSortOrders;
import com.datasweep.plantops.common.measuredvalue.IMeasuredValue;
import com.rockwell.custmes.annotations.Column;
import com.rockwell.custmes.annotations.Id;
import com.rockwell.custmes.annotations.JoinCondition;
import com.rockwell.custmes.annotations.JoinTable;
import com.rockwell.custmes.annotations.ObjectType;
import com.rockwell.custmes.annotations.State;
import com.rockwell.custmes.annotations.Table;
import com.rockwell.custmes.helper.DataTypeHelper;
import com.rockwell.mes.commons.base.ifc.fsm.FSMHelper;
import com.rockwell.mes.commons.base.ifc.functional.MeasuredValueUtilities;
import com.rockwell.mes.commons.base.ifc.sql.ColumnDescriptor;
import com.rockwell.mes.commons.base.ifc.sql.DataBaseUtility;
import com.rockwell.mes.commons.base.ifc.sql.FastLaneReader;

/**
 * This class provides helper methods to handle view classes with DB annotations.
 * 
 * E.g. there are methods to fetch View objects from the database. <code>List<SublotView> sls =
 * AnnotationUtility.getInstance().fetchData(SublotView.class, "batchName", IFilterComparisionOperators.EQUAL_TO, "BX1", "name", IFilterComparisionOperators.EQUAL_TO, "SL00000001")</code>
 * 
 * @author RWeingar
 */
public class AnnotationUtility {
    /**
     * Comment for <code>LOGGER</code>
     */
    private static final org.apache.commons.logging.Log LOGGER = org.apache.commons.logging.LogFactory
            .getLog(AnnotationUtility.class);

    /** internal singleton */
    private static AnnotationUtility instance = new AnnotationUtility();

    /** cache for internal use after annotation parsing */
    private static Hashtable<Class<?>, String> sqlCache = new Hashtable<Class<?>, String>();

    /** cache for internal use after annotation parsing */
    private static Hashtable<Class<?>, ColumnDescriptor> keyColumnCache = new Hashtable<Class<?>, ColumnDescriptor>();

    /** cache for internal use after annotation parsing */
    private static Hashtable<Class<?>, ColumnDescriptor[]> columnCache = new Hashtable<Class<?>, ColumnDescriptor[]>();

    /** cache for internal use after annotation parsing */
    private static Hashtable<Class<?>, Map<String, Method>> methodCache = new Hashtable<Class<?>, Map<String, Method>>();

    /**
     * @return Singleton instance of this utility class
     */
    public static AnnotationUtility getInstance() {
        return instance;
    }

    /**
     * @param clazz
     *            Annotated view class
     * @return Column descriptors for this class as needed by FastLaneReader
     */
    public ColumnDescriptor[] getColumnDescriptors(Class<?> clazz) {
        initialize(clazz);
        return columnCache.get(clazz);
    }

    /**
     * @param clazz
     *            Annotated view class
     * @return The from part of an SQL statement, including all joins etc.
     */
    public String getFromClause(Class<?> clazz) {
        initialize(clazz);
        return sqlCache.get(clazz);
    }

    /**
     * @param clazz
     *            Annotated view class
     * @return The key column as needed in SQL statements
     */
    public ColumnDescriptor getKeyColumn(Class<?> clazz) {
        initialize(clazz);
        return keyColumnCache.get(clazz);
    }

    /**
     * @param clazz
     *            Annotated view class
     * @return The related Keyed object type of the annotated view class
     */
    public Class<?> getType(Class<?> clazz) {
        ObjectType type = clazz.getAnnotation(ObjectType.class);
        if (type == null) {
            return null;
        }
        return type.type();
    }

    /**
     * @param clazz
     *            Annotated view class
     * @param columnName
     *            SQL representation of the column
     * @return Read method of the property related to the given DB column
     */
    public Method getMethod(Class<?> clazz, String columnName) {
        initialize(clazz);
        return methodCache.get(clazz).get(columnName);
    }

    /**
     * @param clazz
     *            Annotated view class
     * @param meth
     *            Getter method for which the SQL column name should be determined
     * @return The SQL column name for a method on the annotated class.
     */
    public String getColumnName(Class<?> clazz, Method meth) {
        initialize(clazz);

        for (Entry<String, Method> entry : methodCache.get(clazz).entrySet()) {
            if (meth.equals(entry.getValue())) {
                return entry.getKey();
            }
        }

        return null;
    }

    /**
     * @param clazz
     *            Annotated view class
     * @param columnName
     *            SQL column name
     * @return Name of the FSM model indicated by the given column
     */
    public String getFSM(Class<?> clazz, String columnName) {
        Method meth = getMethod(clazz, columnName);
        return getFSM(meth);
    }

    /**
     * @param meth
     *            method for which to determine the FSM model
     * @return Name of the FSM model indicated by the given column
     */
    public String getFSM(Method meth) {
        boolean result = meth != null;
        result &= meth.getAnnotation(State.class) != null;
        result &= String.class.equals(meth.getReturnType());
        if (result) {
            State state = meth.getAnnotation(State.class);
            return state.fsmName();
        }
        return null;
    }

    /**
     * @param returnType
     *            The declaring class of a property
     * @return short type from the IDataTypes enumeration class
     */
    public short determineDataType(Class<?> returnType) {
        return DataTypeHelper.getDataType(returnType);
    }

    /**
     * @param clazz
     *            Annotated view class
     * @param dbColumnName
     *            SQL representation of the column
     * @return short type from the IDataTypes enumeration class
     */
    public short determineDataType(Class<?> clazz, String dbColumnName) {
        Method meth = getMethod(clazz, dbColumnName);

        Class<?> returnType = meth.getReturnType();
        return determineDataType(returnType);
    }

    /**
     * Initializes the caches for the given annotated view class. This is needed for later SQL generation and
     * introspection.
     * 
     * @param clazz
     *            Annotated view class
     */
    protected void initialize(Class<?> clazz) {
        if (sqlCache.get(clazz) != null) {
            return;
        }
        StringBuilder sql = new StringBuilder();

        Table tab = clazz.getAnnotation(Table.class);

        Map<String, JoinTable> joinTabs = new HashMap<String, JoinTable>();
        Map<String, JoinCondition> joinConditions = new HashMap<String, JoinCondition>();
        List<ColumnDescriptor> columnList = new ArrayList<ColumnDescriptor>();
        String table = tab.name();
        ColumnDescriptor keyColumn = null;
        Map<String, Method> methMap = new HashMap<String, Method>();

        List<String> fsmList = new LinkedList<String>();
        int fsmIdx = 0;

        BeanInfo bi;
        try {
            bi = Introspector.getBeanInfo(clazz);
        } catch (IntrospectionException exc) {
            throw new IllegalArgumentException(exc);
        }

        for (PropertyDescriptor pd : bi.getPropertyDescriptors()) {
            Method meth = pd.getReadMethod();
            if (meth == null) {
                continue;
            }

            // for (Method meth : clazz.getMethods()) {
            // if (!meth.getName().startsWith("get")) {
            // continue;
            // }

            String fsmName = getFSM(meth);
            Column col = meth.getAnnotation(Column.class);
            if (col == null && fsmName == null) {
                continue;
            }

            JoinTable joinTab = meth.getAnnotation(JoinTable.class);
            JoinCondition joinCond = meth.getAnnotation(JoinCondition.class);
            String alias;
            if (joinTab == null) {
                alias = tab.name();
            } else {
                if (joinTab.alias().length() > 0) {
                    alias = joinTab.alias();
                } else {
                    alias = joinTab.name();
                }
                joinTabs.put(alias, joinTab);
                if (joinCond != null) {
                    joinConditions.put(alias, joinCond);
                }
            }
            String column;
            if (fsmName == null) {
                column = alias + "." + col.name();
            } else {
                fsmIdx++;
                String stateTab = "state" + fsmIdx;
                column = stateTab + ".state_name";

                State fsm = meth.getAnnotation(State.class);

                Map<String, String> sqlParams = new HashMap<String, String>();
                sqlParams.put("stateTab", stateTab);
                sqlParams.put("objectTab", alias);
                sqlParams.put("fsmName", fsmName);
                sqlParams.put("objectKey", fsm.keyName());
                sqlParams.put("objectType", String.valueOf(fsm.type()));

                fsmList.add(DataBaseUtility.resolvePlaceHolders(FSMHelper.FSM_SQL_STATEMENT, sqlParams));
            }
            if (columnList.contains(column)) {
                throw new IllegalArgumentException("Multiple usage of column " + column);
            }
            ColumnDescriptor desc = new ColumnDescriptor(column, IDataTypes.TYPE_STRING, "", "");
            columnList.add(desc);
            methMap.put(column, meth);

            if (meth.getAnnotation(Id.class) != null) {
                keyColumn = desc;
            }
        }

        if (keyColumn == null) {
            throw new IllegalArgumentException("No key column defined");
        }

        // The key columns shall be always the first column
        columnList.remove(keyColumn);
        columnList.add(0, keyColumn);

        sql.append("\n  from ");
        sql.append(table);
        sql.append("\n    ");

        // initialize join sequence
        List<String> joinAliases = new LinkedList<String>();
        List<String> deferredJoinAliases = new LinkedList<String>();

        for (String alias : joinTabs.keySet()) {
            JoinCondition joinCond = joinConditions.get(alias);
            if (joinCond.table().equals(table) || joinCond.table().length() == 0) {
                // no dependencies, insert in the beginning
                joinAliases.add(0, alias);
            } else {
                // at to the tail
                deferredJoinAliases.add(alias);
            }
        }

        while (!deferredJoinAliases.isEmpty()) {
            int lastSize = deferredJoinAliases.size();

            for (Iterator<String> iter = deferredJoinAliases.iterator(); iter.hasNext();) {
                String alias = iter.next();
                JoinCondition joinCond = joinConditions.get(alias);
                if (joinAliases.contains(joinCond.table())) {
                    joinAliases.add(alias);
                    iter.remove();
                }
            }

            if (lastSize == deferredJoinAliases.size()) {
                throw new IllegalArgumentException("Circular or invalid joins for table aliases"
                        + deferredJoinAliases.toString());
            }

        }

        for (String alias : joinAliases) {
            JoinTable joinTab = joinTabs.get(alias);
            sql.append(" inner join ");
            sql.append(joinTab.name());
            sql.append(" ");
            if (!alias.equals(joinTab.name())) {
                sql.append(joinTab.alias());
            } else {
                sql.append(joinTab.name());
            }
            sql.append(" on (");
            JoinCondition joinCond = joinConditions.get(alias);
            if (joinCond == null) {
                throw new IllegalArgumentException("No join condition defined for " + alias);

            }
            if (joinCond.table().length() > 0) {
                sql.append(joinCond.table());

            } else {
                sql.append(table);
            }

            sql.append('.');

            if (joinCond.column().length() > 0) {
                sql.append(joinCond.column());
            } else {
                sql.append(joinCond.joinColumn());
            }
            sql.append(" = ");

            sql.append(alias);
            sql.append('.');
            sql.append(joinCond.joinColumn());

            sql.append(")\n    ");
        }

        for (String fsm : fsmList) {
            sql.append("\n    ");
            sql.append(fsm);
        }

        ColumnDescriptor[] descs = new ColumnDescriptor[columnList.size()];

        AnnotationUtility.methodCache.put(clazz, methMap);
        AnnotationUtility.columnCache.put(clazz, columnList.toArray(descs));
        AnnotationUtility.keyColumnCache.put(clazz, keyColumn);
        AnnotationUtility.sqlCache.put(clazz, sql.toString());
    }

    /**
     * @param <T>
     *            The given view class is used for type safety. This makes an explicit cast obsolete.
     * @param clazz
     *            Annotated view class
     * @param fltr
     *            A ProductionCentre filter, this shall be related to the ObjectType of the view class.
     * @return A list of view objects
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> fetchData(Class<T> clazz, Filter fltr) {
        String keyColumn = getKeyColumn(clazz).getName();
        StringBuilder sql = new StringBuilder();

        sql.append(getFromClause(clazz));

        if (fltr != null) {
            List<String> keys;
            try {
                keys = fltr.getArrayData(new String[] { keyColumn });
            } catch (DatasweepException e) {
                return new ArrayList<T>();
            }

            Map<String, String> paramMap = new HashMap<String, String>();
            paramMap.put("filter", DataBaseUtility.constructSQLWithInOperator(keyColumn, keys));
            // constructSQLWithInOperator(
            // keyColumn, keys));//
            sql.append(DataBaseUtility.resolvePlaceHolders("\n where {filter}", paramMap));

        }

        return fetchData(clazz, sql.toString());
    }

    /**
     * This method is used usually used internally by automatically determined SQL statements.
     * 
     * @param <T>
     *            The given view class is used for type safety. This makes an explicit cast obsolete.
     * 
     * @param clazz
     *            Annotated view class
     * @param sql
     *            SQL statement beginning with the where clause. It must contain all joins to populate the properties of
     *            the view class.
     * @return A list of view objects
     */
    public <T> List<T> fetchData(Class<T> clazz, String sql) {

        ColumnDescriptor[] descs = getColumnDescriptors(clazz);

        FastLaneReader rdr = new FastLaneReader();
        List<String[]> arrayData = rdr.executeQuery(descs, true, sql);

        List<T> result = populateData(clazz, descs, arrayData);

        return result;
    }

    /**
     * @param <T>
     *            The given view class is used for type safety. This makes an explicit cast obsolete.
     * 
     * @param clazz
     *            Annotated view class
     * 
     * @param compareOperators
     *            Mapping of property names of the view class to ProductionCentre compare operators
     * @param values
     *            Mapping of property names of the view class to the values to compare. The values might be null for
     *            EQUAL_TO and NOT_EQUAL_TO
     * @return A list of view objects
     */
    public <T> List<T> fetchData(Class<T> clazz, Map<String, Short> compareOperators, Map<String, Object> values) {
        StringBuilder sql = new StringBuilder();

        sql.append(getFromClause(clazz));
        String conditions = getAndConditions(clazz, compareOperators, values);
        if (conditions.length() > 0) {
            sql.append("\n   where ");
            sql.append(conditions);
        }

        return fetchData(clazz, sql.toString());
    }

    /**
     * @param <T>
     * @param clazz
     * @param compareOperators
     * @param values
     * @param orderCondition
     * @param orderType
     * @return
     */
    public <T> List<T> fetchData(Class<T> clazz, Map<String, Short> compareOperators, Map<String, Object> values,
            String[] orderCondition, short orderType) {

        StringBuilder sql = new StringBuilder();

        sql.append(getFromClause(clazz));
        String conditions = getAndConditions(clazz, compareOperators, values);
        if (conditions.length() > 0) {
            sql.append("\n   where ");
            sql.append(conditions);
        }
        String sqlOrderBy = getOrderByClause(clazz, orderCondition, orderType);
        sql.append(sqlOrderBy);

        return fetchData(clazz, sql.toString());
    }

    /**
     * Convenience method to fetch data directly with one method call with multiple AND linked conditions.
     * 
     * @param <T>
     *            The given view class is used for type safety. This makes an explicit cast obsolete.
     * 
     * @param clazz
     *            Annotated view class
     * @param args
     *            The arguments must be triples of property name (String), compare operator (short), value (Object).
     * @return
     */
    public <T> List<T> fetchData(Class<T> clazz, Object... args) {
        Map<String, Short> ops = getCompareOperatorMap(args);
        Map<String, Object> vals = getValueMap(args);

        return fetchData(clazz, ops, vals);
    }

    public String getOrderByClause(Class<?> clazz, String[] orderCondition, short orderType) {
        initialize(clazz);
        String orderTypeString = "DESC";

        if (IFilterSortOrders.ASCENDING == orderType) {
            orderTypeString = "ASC";
        }

        StringBuilder bld = new StringBuilder();

        if (orderCondition != null) {
            boolean first = true;

            for (String entry : orderCondition) {

                Method meth = null;
                try {
                    BeanInfo bi = Introspector.getBeanInfo(clazz);
                    PropertyDescriptor[] descs = bi.getPropertyDescriptors();
                    if (descs == null) {
                        throw new RuntimeException("No property descriptors");
                    }
                    for (PropertyDescriptor desc : descs) {
                        if (desc.getName().equals(entry)) {
                            meth = desc.getReadMethod();
                            break;
                        }
                    }
                } catch (IntrospectionException exc) {
                    throw new RuntimeException(exc);
                }

                if (meth == null) {
                    throw new RuntimeException("No write method for property " + clazz.getSimpleName() + "." + entry);
                }

                String columnName = getColumnName(clazz, meth);

                if (first) {
                    first = false;
                    bld.append("\n   ORDER BY " + columnName);
                } else {
                    bld.append(", " + columnName);
                }
            }

            bld.append(" " + orderTypeString);
        }

        return bld.toString();
    }

    public Map<String, Object> getValueMap(Object... args) {
        final int stepWidth = 3;
        if (args.length % stepWidth != 0) {
            throw new IllegalArgumentException("Variable arguments must be always grouped in three values");
        }

        Map<String, Object> vals = new HashMap<String, Object>();

        for (int idx = 0; idx < args.length; idx += stepWidth) {
            String name = (String) args[idx];
            Object val = args[idx + 2];
            vals.put(name, val);
        }

        return vals;
    }

    public Map<String, Short> getCompareOperatorMap(Object... args) {
        final int stepWidth = 3;
        if (args.length % stepWidth != 0) {
            throw new IllegalArgumentException("Variable arguments must be always grouped in three values");
        }

        Map<String, Short> ops = new HashMap<String, Short>();

        for (int idx = 0; idx < args.length; idx += stepWidth) {
            String name = (String) args[idx];
            Short op = (Short) args[idx + 1];
            ops.put(name, op);
        }

        return ops;
    }

    /**
     * This method transforms a property/operator/value combination into a valid SQL expression.
     * 
     * @param clazz
     *            Annotated view class
     * @param name
     *            Property name
     * @param compareOperator
     *            ProductionCentre compare operator as defined in IFilterComparisionOperators.
     * @param value
     *            Value to compare.
     * @return SQL expression
     */
    public String getCondition(Class<?> clazz, String name, short compareOperator, Object value) {

        initialize(clazz);

        Method meth = null;
        try {
            BeanInfo bi = Introspector.getBeanInfo(clazz);
            PropertyDescriptor[] descs = bi.getPropertyDescriptors();
            if (descs == null) {
                throw new RuntimeException("No property descriptors");
            }
            for (PropertyDescriptor desc : descs) {
                if (desc.getName().equals(name)) {
                    meth = desc.getReadMethod();
                    break;
                }
            }
        } catch (IntrospectionException exc) {
            throw new RuntimeException(exc);
        }

        if (meth == null) {
            throw new RuntimeException("No write method for property " + clazz.getSimpleName() + "." + name);
        }

        Class<?> javaType = meth.getReturnType();
        short pcType = determineDataType(javaType);

        String columnName = getColumnName(clazz, meth);
        StringBuilder bld = new StringBuilder();
        bld.append(columnName);
        switch (compareOperator) {
        case IFilterComparisonOperators.EQUAL_TO:
            if (value == null) {
                bld.append(" is null");
            } else {
                bld.append(" = ");
                switch (pcType) {
                case IDataTypes.TYPE_STRING:
                    bld.append("'");
                    bld.append(value);
                    bld.append("'");
                    break;
                case IDataTypes.TYPE_BOOLEAN:
                    bld.append(((Boolean) value).booleanValue() ? "1" : "0");
                    break;
                default:
                    bld.append(value);
                }
            }

            break;
        case IFilterComparisonOperators.STARTSWITH:
            String likeValue = "";
            if (value != null) {
                likeValue = value.toString();
            }
            bld.append(" LIKE '");
            bld.append(likeValue);
            bld.append("%'");
            break;
        default:
        }
        return bld.toString();
    }

    /**
     * @param clazz
     *            Annotated view class
     * @param compareOperators
     *            Mapping of property names to ProductionCentre compare operators as specified in
     *            IFilterComparisionOperators.
     * @param values
     *            Mapping of property names to compare values.
     * @return A SQL string, which concatenated the multiple conditions
     */
    public String getAndConditions(Class<?> clazz, Map<String, Short> compareOperators, Map<String, Object> values) {
        initialize(clazz);

        StringBuilder bld = new StringBuilder();
        if (compareOperators != null) {
            boolean first = true;
            for (Entry<String, Short> entry : compareOperators.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    bld.append(" and ");
                }
                short compareOperator = entry.getValue();
                Object value = null;
                if (values != null) {
                    value = values.get(entry.getKey());
                }
                bld.append(getCondition(clazz, entry.getKey(), compareOperator, value));
            }

        }

        return bld.toString();
    }

    /**
     * This method transfers the data of a List<String[]> as retrieved by getArrayData to a predefined list of
     * uninitialized view objects.
     * 
     * @param clazz
     *            Annotated view class
     * @param descs
     *            Column descriptors as needed for FastLaneReader. This should usually be obtained by
     *            {@link #getColumnDescriptors(Class)}.
     * @param rows
     *            The list of raw String[] objects as returned by getArrayDataFromActive.
     */
    public <T> List<T> populateData(Class<T> clazz, ColumnDescriptor[] descs, List<String[]> rows) {
        initialize(clazz);

        List<T> objects = new ArrayList<T>(rows.size());
        for (int idx = 0; idx < rows.size(); idx++) {
            try {
                T obj;
                obj = clazz.newInstance();
                objects.add(obj);
            } catch (InstantiationException e) {
                return null;
            } catch (IllegalAccessException e) {
                return null;
            }
        }

        for (int idx = 0; idx < descs.length; idx++) {
            ColumnDescriptor desc = descs[idx];
            String dbColumnName = desc.getName();

            Method meth = getMethod(clazz, dbColumnName);
            Class<?> returnType = meth.getReturnType();

            Method setter;
            try {
                setter = clazz.getMethod(meth.getName().replace("get", "set"), returnType);

                short dataType = determineDataType(returnType);
                for (int jdx = 0; jdx < rows.size(); jdx++) {
                    String strVal = rows.get(jdx)[idx];
                    Object obj = objects.get(jdx);
                    switch (dataType) {
                    case IDataTypes.TYPE_STRING:
                        setter.invoke(obj, strVal);
                        break;
                    case IDataTypes.TYPE_MEASUREDVALUE:
                        IMeasuredValue val = MeasuredValueUtilities.createMV(strVal);
                        setter.invoke(obj, val);
                        break;
                    case IDataTypes.TYPE_LONG:
                        Long value = null;
                        if (strVal != null) {
                            value = Long.valueOf(strVal);
                        }
                        setter.invoke(obj, value);
                        break;
                    case IDataTypes.TYPE_BOOLEAN:
                        Boolean bValue = null;
                        if (strVal != null) {
                            bValue = !"0".equals(strVal);
                        }
                        setter.invoke(obj, bValue);
                        break;
                    case IDataTypes.TYPE_DATETIME:
                        Time tValue = new Time(strVal);
                        setter.invoke(obj, tValue);
                        break;
                    default:
                        LOGGER.error("undefined data type '" + dataType + " when populating data ");
                    }
                }
            } catch (SecurityException e) {
                LOGGER.error("Error when populating data", e);
            } catch (NoSuchMethodException e) {
                LOGGER.error("Error when populating data", e);
            } catch (IllegalArgumentException e) {
                LOGGER.error("Error when populating data", e);
            } catch (IllegalAccessException e) {
                LOGGER.error("Error when populating data", e);
            } catch (InvocationTargetException e) {
                LOGGER.error("Error when populating data", e);
            }

        }
        return objects;
    }

    /**
     * @param <T>
     * @param clazz
     * @param descs
     * @param row
     * @return
     */
    public <T> T getObjectFromRawData(Class<T> clazz, ColumnDescriptor[] descs, String[] row) {
        initialize(clazz);

        List<String[]> rows = new ArrayList<String[]>();
        rows.add(row);
        List<T> result = populateData(clazz, descs, /* result, */rows);

        return result.get(0);
    }
}
