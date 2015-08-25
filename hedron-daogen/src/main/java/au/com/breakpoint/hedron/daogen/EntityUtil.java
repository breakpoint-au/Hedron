//                       __________________________________
//                ______|      Copyright 2008-2015         |______
//                \     |     Breakpoint Pty Limited       |     /
//                 \    |   http://www.breakpoint.com.au   |    /
//                 /    |__________________________________|    \
//                /_________/                          \_________\
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
// except in compliance with the License. You may obtain a copy of the License at
//	   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
// either express or implied. See the License for the specific language governing
// permissions and limitations under the License.
//
package au.com.breakpoint.hedron.daogen;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import au.com.breakpoint.hedron.core.GenericFactory;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.daogen.strategy.ColumnTypeInfo;

public class EntityUtil
{

    public static boolean allowsOverloadDisambiguation (final List<Column> pkColumns)
    {
        // Look for non-matching primary key column.
        final Predicate<Column> criterion = c ->
        {
            final ColumnTypeInfo jti = getColumnTypeInfo (c);
            return !jti.m_javaType.equals (jti.m_javaObjectType);
        };
        return HcUtil.find (pkColumns, criterion) != null;
    }

    public static ColumnTypeInfo getColumnTypeInfo (final Column c)
    {
        final IRelation ir = c.getParent ();
        ColumnTypeInfo jti = null;
        String key = null;
        if (ir != null)
        {
            final String entityName = ir.getEntityName ();
            final String columnName = c.getName ();
            key = String.format ("%s.%s", entityName, columnName);
            jti = m_jtiCache.get (key);
        }

        if (jti == null)
        {
            jti = new ColumnTypeInfo ();

            final String columnType = c.getColumnAttributes ().getType ();
            if (columnType == null)
            {
                ThreadContext.assertInformation (false, "Unknown column type for column[%s]", c.getName ());
            }
            else if (columnType.equals ("char"))
            {
                final boolean isVarchar = c.getColumnAttributes ().getMode ().equals ("varying");
                setStringJavaType (isVarchar, c.getColumnAttributes ().getSize (), jti);

            }
            else if (columnType.equals ("integer"))
            {
                setInt (c, jti, Integer.MIN_VALUE);// Integer.MIN_VALUE = no value checking
            }
            else if (columnType.equalsIgnoreCase ("number") || columnType.equalsIgnoreCase ("decimal"))
            {
                // If no fractional digits, map to byte / int / long if will fit.
                if (c.getColumnAttributes ().getScale () == 0)
                {
                    final int wholeDigits = c.getColumnAttributes ().getPrecision ();// all whole digits
                    final BigDecimal upperLimitValue =
                        new BigDecimal (10).pow (wholeDigits).subtract (new BigDecimal (1));

                    // compareTo returns -1, 0 or 1 as this number is less than, equal to, or greater than the arg.
                    if (upperLimitValue.compareTo (MAX_BYTE) < 0)
                    {
                        // Can fit in byte.
                        jti.m_javaObjectType = "Byte";
                        jti.m_javaType = c.isNullable () ? jti.m_javaObjectType : "byte";
                        jti.m_nonPrimitiveTypeJavaLangType = c.isNullable ();
                        setHcUtilConversionMethod (jti, "getByteValue");
                        jti.m_javaTypeOfLimitValue = "byte";
                        jti.m_jdbcType = "Byte";
                        jti.m_jdbcResultSetAccessor = "getByte";
                        setJavaSqlType (jti, "TINYINT");
                        jti.m_javaCastExpression = "(Byte)";
                        jti.m_maxValue = upperLimitValue.longValue ();
                        jti.m_minValue = -jti.m_maxValue;
                        jti.m_javaTypeConstantSuffix = "";
                        jti.m_equalityExpression = getEqualsExpression (c);
                        jti.m_hashCodeExpression = c.isNullable () ? HASHCODE_OBJECT : HASHCODE_ALREADY_INT_COMPATIBLE;
                        jti.m_copyStyle = ColumnTypeInfo.CopyStyle.ShallowCopy;
                    }
                    else if (upperLimitValue.compareTo (MAX_SHORT) < 0)
                    {
                        // Can fit in short.
                        jti.m_javaObjectType = "Short";
                        jti.m_javaType = c.isNullable () ? jti.m_javaObjectType : "short";
                        jti.m_nonPrimitiveTypeJavaLangType = c.isNullable ();
                        setHcUtilConversionMethod (jti, "getShortValue");
                        jti.m_javaTypeOfLimitValue = "short";
                        jti.m_jdbcType = "Short";
                        jti.m_jdbcResultSetAccessor = "getShort";
                        setJavaSqlType (jti, "SMALLINT");
                        jti.m_javaCastExpression = "(Short)";
                        jti.m_maxValue = upperLimitValue.longValue ();
                        jti.m_minValue = -jti.m_maxValue;
                        jti.m_javaTypeConstantSuffix = "";
                        jti.m_equalityExpression = getEqualsExpression (c);
                        jti.m_hashCodeExpression = c.isNullable () ? HASHCODE_OBJECT : HASHCODE_ALREADY_INT_COMPATIBLE;
                        jti.m_copyStyle = ColumnTypeInfo.CopyStyle.ShallowCopy;
                    }
                    else if (upperLimitValue.compareTo (MAX_INT) < 0)
                    {
                        setInt (c, jti, upperLimitValue.intValue ());
                    }
                    else if (upperLimitValue.compareTo (MAX_LONG) < 0)
                    {
                        // Can fit in long.
                        jti.m_javaObjectType = "Long";
                        jti.m_javaType = c.isNullable () ? jti.m_javaObjectType : "long";
                        jti.m_nonPrimitiveTypeJavaLangType = c.isNullable ();
                        setHcUtilConversionMethod (jti, "getLongValue");
                        jti.m_javaTypeOfLimitValue = "long";
                        jti.m_jdbcType = "Long";
                        jti.m_jdbcResultSetAccessor = "getLong";
                        setJavaSqlType (jti, "BIGINT");
                        jti.m_javaCastExpression = "(Long)";
                        jti.m_maxValue = upperLimitValue.longValue ();
                        jti.m_minValue = -jti.m_maxValue;
                        jti.m_javaTypeConstantSuffix = "L";
                        jti.m_equalityExpression = getEqualsExpression (c);
                        jti.m_hashCodeExpression =
                            c.isNullable () ? HASHCODE_OBJECT : "(m_column%s ^ (m_column%s >>> 32))";
                        //                            c.isNullable () ? HASHCODE_OBJECT : "(int) (m_column%s ^ (m_column%s >>> 32))";
                        jti.m_copyStyle = ColumnTypeInfo.CopyStyle.ShallowCopy;
                    }
                    else
                    {
                        setBigDecimal (jti, c);
                    }
                }
                else
                {
                    setBigDecimal (jti, c);
                }
            }
            else if (columnType.equalsIgnoreCase ("floatingpoint"))
            {
                jti.m_javaObjectType = "Double";
                jti.m_javaType = c.isNullable () ? jti.m_javaObjectType : "double";
                jti.m_nonPrimitiveTypeJavaLangType = c.isNullable ();
                jti.m_jdbcType = "Double";
                jti.m_jdbcResultSetAccessor = "getDouble";
                setJavaSqlType (jti, "DOUBLE");
                jti.m_javaCastExpression = "(Double)";
                jti.m_equalityExpression = getEqualsExpression (c);
                jti.m_hashCodeExpression =
                    c.isNullable () ? HASHCODE_OBJECT : "(int) Double.doubleToLongBits (m_column%s)";
                jti.m_copyStyle = ColumnTypeInfo.CopyStyle.ShallowCopy;
            }
            else if (columnType.equalsIgnoreCase ("boolean"))
            {
                jti.m_javaObjectType = "Boolean";
                jti.m_javaType = c.isNullable () ? jti.m_javaObjectType : "boolean";
                jti.m_nonPrimitiveTypeJavaLangType = c.isNullable ();
                jti.m_jdbcType = "Boolean";
                jti.m_jdbcResultSetAccessor = "getBoolean";
                setJavaSqlType (jti, "BIT");
                jti.m_javaCastExpression = "(Boolean)";
                jti.m_equalityExpression = getEqualsExpression (c);
                jti.m_hashCodeExpression =
                    c.isNullable () ? HASHCODE_OBJECT : "((m_column%s ? 1 : 0) ^ ((m_column%s ? 1 : 0) >>> 32))";
                //                c.isNullable () ? HASHCODE_OBJECT : "(int) ((m_column%s ? 1 : 0) ^ ((m_column%s ? 1 : 0) >>> 32))";
                jti.m_copyStyle = ColumnTypeInfo.CopyStyle.ShallowCopy;
            }
            else if (columnType.equalsIgnoreCase ("datetime") || columnType.equalsIgnoreCase ("date"))
            {
                // java.sql.Date truncates time component
                jti.m_javaObjectType = jti.m_javaType = "Timestamp";
                jti.m_importsJavaType.add ("java.sql.Timestamp");
                jti.m_nonPrimitiveTypeJavaLangType = true;
                jti.m_jdbcType = "Timestamp";
                jti.m_jdbcResultSetAccessor = "getTimestamp";
                setJavaSqlType (jti, "TIMESTAMP");
                jti.m_javaCastExpression = "(java.sql.Timestamp)";
                jti.m_equalityExpression = EQUALS_OBJECT;
                jti.m_hashCodeExpression = HASHCODE_OBJECT;
                jti.m_copyStyle = ColumnTypeInfo.CopyStyle.Duplicate;
            }
            else if (columnType.equalsIgnoreCase ("blob") || columnType.equalsIgnoreCase ("guid"))
            {
                // TODO 4 prove that guid support works
                jti.m_javaObjectType = jti.m_javaType = "byte[]";
                jti.m_nonPrimitiveTypeJavaLangType = true;
                jti.m_jdbcType = "Bytes";
                jti.m_jdbcResultSetAccessor = "getBytes";
                setJavaSqlType (jti, "BLOB");
                jti.m_javaCastExpression = "(byte[])";
                jti.m_equalityExpression = EQUALS_OBJECT;
                jti.m_hashCodeExpression = HASHCODE_OBJECT;
                jti.m_copyStyle = ColumnTypeInfo.CopyStyle.Duplicate;
            }
            else if (columnType.equalsIgnoreCase ("clob"))
            {
                jti.m_javaObjectType = jti.m_javaType = "String";
                jti.m_nonPrimitiveTypeJavaLangType = true;
                jti.m_jdbcType = "String";
                jti.m_jdbcResultSetAccessorFormatter = e3 -> String
                    .format ("DaoUtil.getClobAsString (rs, COLUMN_NAMES[%s.Columns.%s])", e3.getE1 (), e3.getE2 ());
                jti.m_importsResultSetAccessorFormatter.add ("au.com.breakpoint.hedron.core.dao.DaoUtil");
                setJavaSqlType (jti, "CLOB");
                jti.m_javaCastExpression = "(String)";
                jti.m_size = -1;
                jti.m_equalityExpression = EQUALS_OBJECT;
                jti.m_hashCodeExpression = HASHCODE_OBJECT;
                jti.m_copyStyle = ColumnTypeInfo.CopyStyle.ShallowCopy;// no size information
            }
            else if (columnType.equalsIgnoreCase ("text"))
            {
                setStringJavaType (true, -1, jti);// no size information
            }
            else if (columnType.equalsIgnoreCase ("oracle-refcursor"))
            {
                final String refcursorType = c.getColumnAttributes ().getRefcursorType ();
                jti.m_javaObjectType = jti.m_javaType = "List<" + refcursorType + ">";
                jti.m_importsJavaType.add ("java.util.List");
                jti.m_importsEntityType.add (refcursorType);
                jti.m_nonPrimitiveTypeJavaLangType = true;
                jti.m_jdbcType = "TBD";
                jti.m_jdbcResultSetAccessor = "getTBD";
                jti.m_jdbcJavaSqlType = "OracleTypes.CURSOR";
                jti.m_importsJavaSqlType.add ("oracle.jdbc.OracleTypes");
                jti.m_javaCastExpression = null;// "(" + jti.m_javaType + ")"
                jti.m_equalityExpression = EQUALS_OBJECT;
                jti.m_hashCodeExpression = HASHCODE_OBJECT;
                jti.m_copyStyle = ColumnTypeInfo.CopyStyle.Duplicate;
                jti.m_rowMapperType = refcursorType;
            }
            else
            {
                //                ThreadContext.assertError (false, "Unsupported column type [%s] for column[%s].[%s]", columnType, c.getParent ().getName (), c.getName ());
                if (ir == null)
                {
                    ThreadContext.assertWarning (false, "Mapping String for unsupported column type [%s]", columnType);
                }
                else
                {
                    ThreadContext.assertWarning (false, "Mapping String for unsupported column type [%s] for %s.%s",
                        columnType, ir.getEntityName (), c.getName ());
                }
                setStringJavaType (true, 16, jti);
            }

            if (ir != null)
            {
                m_jtiCache.put (key, jti);
            }
        }

        return jti;
    }

    public static String getEqualsExpression (final Column c)
    {
        return c.isNullable () ? EQUALS_OBJECT : EQUALS_INTRINSIC;
    }

    public static String getFilepath (final String basePath, final String filename)
    {
        //return String.format ("%s/%s", basePath, filename);
        return HcUtil.formFilepath (basePath, filename);
    }

    public static List<Column> getNonIdentityColumns (final List<Column> columns)
    {
        return HcUtil.filter (columns, e -> !e.isIdentity ());
    }

    public static List<Column> getNonPrimaryKeyColumns (final IRelation ir)
    {
        final List<Column> l = new ArrayList<Column> ();

        final List<Column> columns = ir.getColumns ();
        for (final Column c : columns)
        {
            if (c.getRequirement () != Column.Requirement.PRIMARYKEY)
            {
                l.add (c);
            }
        }

        return l;
    }

    public static List<Column> getNonPrimaryKeyNonIdentityColumns (final IRelation ir)
    {
        final List<Column> l = new ArrayList<Column> ();

        final List<Column> columns = ir.getColumns ();
        for (final Column c : columns)
        {
            if (c.getRequirement () != Column.Requirement.PRIMARYKEY && !c.isIdentity ())
            {
                l.add (c);
            }
        }

        return l;
    }

    public static String getParametersClass (final List<Parameter> parameters)
    {
        String s = null;

        if (HcUtil.safeGetSize (parameters) == 0)
        {
            s = "Void";
        }
        else
        {
            final List<Column> columns = getColumns (parameters);
            s = getColumnsClass (columns);

        }

        return s;
    }

    public static String getPrimaryKeyClass (final IRelation ir)
    {
        String s = null;

        final Constraint pk = ir.getPrimaryConstraint ();// may be null
        if (pk == null)
        {
            s = "Void";
        }
        else
        {
            s = getColumnsClass (pk.getColumns ());
        }

        return s;
    }

    public static String getPrimaryKeyInstanceString (final IRelation ir)
    {
        String s = null;

        final Constraint pk = ir.getPrimaryConstraint ();// may be null
        if (pk == null)
        {
            s = "null";
        }
        else
        {
            final List<Column> pkColumns = pk.getColumns ();
            if (pkColumns.size () == 1)
            {
                // Simple primary key.
                final Column c = pkColumns.get (0);
                s = String.format ("m_column%s", c.getName ());// autoboxes
            }
            else
            {
                s = String.format ("new %s (", getTupleClassString (pkColumns));
                int i = 0;
                for (final Column c : pkColumns)
                {
                    final String columnName = c.getName ();
                    s = String.format ("%sm_column%s%s", s, columnName, i == pkColumns.size () - 1 ? ")" : ", ");
                    ++i;
                }
            }
        }

        return s;
    }

    public static String getSqlParameterTypeName (final int direction)
    {
        String s = null;

        switch (direction)
        {
            case Parameter.ParameterDirection.IN:
            {
                s = "SqlParameter";
                break;
            }

            case Parameter.ParameterDirection.OUT:
            case Parameter.ParameterDirection.RETURN:
            case Parameter.ParameterDirection.RETURN_AS_OUT:
            {
                s = "SqlOutParameter";
                break;
            }

            case Parameter.ParameterDirection.IN_OUT:
            {
                s = "SqlInOutParameter";
                break;
            }

            default:
            {
                break;
            }
        }

        return s;
    }

    public static String getStringColumnClauses (final String entityName, final List<Column> columns,
        final boolean accessAsEntity, final String typename)
    {
        final StringBuilder sb = new StringBuilder ();

        final String formatString =
            accessAsEntity ? "new %s (%s.Columns.%s, e.get%s ())" : "new %s (%s.Columns.%s, column%s)";

        int i = 0;
        for (final Column c : columns)
        {
            final String columnName = c.getName ();

            if (i > 0)
            {
                sb.append (", ");
            }
            final String s = String.format (formatString, typename, entityName, columnName, columnName);
            sb.append (s);
            ++i;
        }

        return sb.toString ();
    }

    public static String getStringColumnPhysicalNames (final List<Column> columns)
    {
        final StringBuilder sb = new StringBuilder ();

        int i = 0;
        for (final Column c : columns)
        {
            final String columnPhysicalName = c.getPhysicalName ();

            sb.append (String.format ("%s%s", columnPhysicalName, i == columns.size () - 1 ? "" : ", "));
            ++i;
        }

        return sb.toString ();
    }

    public static String getStringColumnsArgsWithType (final List<Column> columns)
    {
        final StringBuilder sb = new StringBuilder ();

        int i = 0;
        for (final Column c : columns)
        {
            final String columnName = c.getName ();
            final ColumnTypeInfo jti = getColumnTypeInfo (c);

            sb.append (String.format ("final %s column%s%s", jti.m_javaType, columnName,
                i == columns.size () - 1 ? "" : ", "));
            ++i;
        }

        return sb.toString ();
    }

    public static String getStringColumnsWithGetter (final List<Column> columns)
    {
        final StringBuilder sb = new StringBuilder ();

        int i = 0;
        for (final Column c : columns)
        {
            final String columnName = c.getName ();

            sb.append (String.format ("e.get%s ()%s", columnName, i == columns.size () - 1 ? "" : ", "));
            ++i;
        }

        return sb.toString ();
    }

    public static String getStringConvenienceParameters (final List<Parameter> parameters)
    {
        final StringBuilder sb = new StringBuilder ();

        int i = 0;
        for (final Parameter p : parameters)
        {
            final Column c = p.getColumn ();

            final ColumnTypeInfo jti = EntityUtil.getColumnTypeInfo (c);
            if (jti.m_javaConvenienceType != null)
            {
                final String s = String.format ("value%s == null ? null : new %s (value%s)%s", c.getName (),
                    jti.m_javaType, c.getName (), i == parameters.size () - 1 ? "" : ", ");
                sb.append (s);
            }
            else
            {
                final String s = String.format ("value%s%s", c.getName (), i == parameters.size () - 1 ? "" : ", ");
                sb.append (s);
            }
            ++i;
        }

        return sb.toString ();
    }

    public static String getStringConvenienceParametersArgs (final List<Parameter> inParameters)
    {
        final StringBuilder sb = new StringBuilder ();

        int i = 0;
        for (final Parameter p : inParameters)
        {
            final Column c = p.getColumn ();
            final ColumnTypeInfo jti = EntityUtil.getColumnTypeInfo (c);

            final String t = jti.m_javaConvenienceType != null ? jti.m_javaConvenienceType : jti.m_javaType;
            sb.append (
                String.format ("final %s value%s%s", t, c.getName (), i == inParameters.size () - 1 ? "" : ", "));
            ++i;
        }

        return sb.toString ();
    }

    public static String getStringParameterClassParameters (final List<Parameter> parameters, final String variableName)
    {
        String s = null;

        if (HcUtil.safeGetSize (parameters) == 0)
        {
            s = "";
        }
        else
        {
            final List<Column> columns = getColumns (parameters);
            s = getStringColumnClassParameters (columns, variableName);
        }

        return s;
    }

    public static String getStringParametersArgs (final List<Parameter> parameters)
    {
        final StringBuilder sb = new StringBuilder ();

        int i = 0;
        for (final Parameter p : parameters)
        {
            final Column c = p.getColumn ();
            final ColumnTypeInfo jti = getColumnTypeInfo (c);

            sb.append (String.format ("final %s value%s%s", jti.m_javaType, c.getName (),
                i == parameters.size () - 1 ? "" : ", "));
            ++i;
        }

        return sb.toString ();
    }

    public static String getStringParametersArgsRef (final List<Parameter> parameters)
    {
        final StringBuilder sb = new StringBuilder ();

        int i = 0;
        for (final Parameter p : parameters)
        {
            final Column c = p.getColumn ();

            sb.append (String.format ("value%s%s", c.getName (), i == parameters.size () - 1 ? "" : ", "));
            ++i;
        }

        return sb.toString ();
    }

    public static String getStringParameterValues (final List<Parameter> parameters)
    {
        return String.format ("new Object[] { %s }", getStringParametersArgsRef (parameters));
    }

    public static String getStringUpdateClauses (final String entityName, final List<Column> pkColumns,
        final List<Column> nonPkColumns, final boolean accessAsEntity, final Column optimisticLockColumn,
        final String sqlName)
    {
        final StringBuilder sb = new StringBuilder ();

        final String getValueFormatString = accessAsEntity ? "e.get%s ()" : "column%s";

        sb.append (String.format ("        final UpdateSql %s =", sqlName));

        // set elements
        int i = 0;
        for (final Column c : nonPkColumns)
        {
            final String columnName = c.getName ();

            final String s = String.format (getValueFormatString, columnName);
            sb.append (String.format ("%n            %s (%s.Columns.%s).set (%s)",
                i == 0 ? "new UpdateSql" : "    .and", entityName, columnName, s));
            ++i;
        }

        // where elements
        i = 0;
        for (final Column c : pkColumns)
        {
            final String columnName = c.getName ();

            final String s = String.format (getValueFormatString, columnName);
            sb.append (String.format ("%n            %s (%s.Columns.%s).equal (%s)", i == 0 ? ".where" : "    .and",
                entityName, columnName, s));
            ++i;
        }

        if (optimisticLockColumn != null)
        {
            final String name = optimisticLockColumn.getName ();
            final String s = String.format ("column%s", name);
            sb.append (String.format ("%n            %s (%s.Columns.%s).equal (%s)", i == 0 ? ".where" : "    .and",
                entityName, name, s));
            ++i;
        }

        sb.append (";");

        return sb.toString ();
    }

    public static String getTupleClassString (final List<Column> columns)
    {
        String s = String.format ("Tuple.E%s<", columns.size ());

        int i = 0;
        for (final Column c : columns)
        {
            final ColumnTypeInfo jti = getColumnTypeInfo (c);
            s = String.format ("%s%s%s", s, jti.m_javaObjectType, i == columns.size () - 1 ? ">" : ", ");
            ++i;
        }

        return s;
    }

    public static boolean hasConvenienceTypes (final List<Parameter> parameters)
    {
        boolean has = false;

        for (final Parameter p : parameters)
        {
            final Column c = p.getColumn ();
            final ColumnTypeInfo jti = getColumnTypeInfo (c);

            has = has || jti.m_javaConvenienceType != null;
        }

        return has;
    }

    public static boolean mapsToAnotherEntity (final IRelation ir)
    {
        final String name = ir.getName ();
        final String entityName = ir.getEntityName ();
        return !HcUtil.safeEquals (name, entityName);
    }

    public static void setBigDecimal (final ColumnTypeInfo jti, final Column c)
    {
        final int scale = c.getColumnAttributes ().getScale ();

        jti.m_javaObjectType = jti.m_javaType = "BigDecimal";
        jti.m_importsJavaType.add ("java.math.BigDecimal");
        jti.m_nonPrimitiveTypeJavaLangType = c.isNullable ();
        jti.m_javaConvenienceType = c.isNullable () ? "Long" : "long";

        jti.m_jdbcType = "BigDecimal";
        jti.m_jdbcResultSetAccessor = "getBigDecimal";
        setJavaSqlType (jti, "DECIMAL");
        jti.m_javaCastExpression = "(" + "BigDecimal" + ")";
        jti.m_precision = String.valueOf (c.getColumnAttributes ().getPrecision ());
        jti.m_scale = String.valueOf (scale);
        jti.m_equalityExpression = EQUALS_OBJECT;
        jti.m_hashCodeExpression = HASHCODE_OBJECT;
        jti.m_copyStyle = ColumnTypeInfo.CopyStyle.ShallowCopy;// BigDecimal is immutable so is safe to copy
    }

    public static void setInt (final Column c, final ColumnTypeInfo jti, final int upperLimitValue)
    {
        // Can fit in int.
        jti.m_javaObjectType = "Integer";
        jti.m_javaType = c.isNullable () ? jti.m_javaObjectType : "int";
        jti.m_nonPrimitiveTypeJavaLangType = c.isNullable ();
        setHcUtilConversionMethod (jti, "getIntValue");
        jti.m_javaTypeOfLimitValue = "int";
        jti.m_jdbcType = "Int";
        jti.m_jdbcResultSetAccessor = "getInt";
        setJavaSqlType (jti, "INTEGER");
        //        jti.m_javaCastExpression = "(int) (Integer)";
        jti.m_javaCastExpression = "(Integer)";// remove redundant cast
        if (upperLimitValue > Integer.MIN_VALUE)
        {
            jti.m_maxValue = upperLimitValue;
            jti.m_minValue = -jti.m_maxValue;
        }
        jti.m_javaTypeConstantSuffix = "";
        jti.m_equalityExpression = getEqualsExpression (c);
        jti.m_hashCodeExpression = c.isNullable () ? HASHCODE_OBJECT : "m_column%s";
        jti.m_copyStyle = ColumnTypeInfo.CopyStyle.ShallowCopy;
    }

    public static void setStringJavaType (final boolean isVarchar, final int size, final ColumnTypeInfo jti)
    {
        //            jti.m_javaType = size == 1 ? "char" : "String";
        jti.m_javaObjectType = jti.m_javaType = "String";
        jti.m_nonPrimitiveTypeJavaLangType = true;
        jti.m_jdbcType = "String";
        jti.m_jdbcResultSetAccessor = "getString";
        setJavaSqlType (jti, isVarchar ? "VARCHAR" : "CHAR");
        jti.m_javaCastExpression = "(String)";
        jti.m_size = size;
        jti.m_equalityExpression = EQUALS_OBJECT;
        jti.m_hashCodeExpression = HASHCODE_OBJECT;
        jti.m_copyStyle = ColumnTypeInfo.CopyStyle.ShallowCopy;
    }

    private static List<Column> getColumns (final List<Parameter> parameters)
    {
        final List<Column> columns = GenericFactory.newArrayList ();
        for (final Parameter parameter : parameters)
        {
            columns.add (parameter.getColumn ());
        }
        return columns;
    }

    private static String getColumnsClass (final List<Column> columns)
    {
        String s;

        if (columns.size () == 1)
        {
            // Simple primary key.
            final Column c = columns.get (0);
            final ColumnTypeInfo jti = getColumnTypeInfo (c);
            s = jti.m_javaObjectType;
        }
        else
        {
            s = getTupleClassString (columns);
        }

        return s;
    }

    private static String getStringColumnClassParameters (final List<Column> columns, final String variableName)
    {
        String s;

        if (columns.size () == 1)
        {
            s = variableName;
        }
        else
        {
            s = getTupleClassStringParameters (columns);
        }

        return s;
    }

    private static String getTupleClassStringParameters (final List<Column> columns)
    {
        final StringBuilder sb = new StringBuilder ();

        for (int i = 0; i < columns.size (); ++i)
        {
            sb.append (String.format ("p.getE%s ()%s", i, i == columns.size () - 1 ? "" : ", "));
        }

        return sb.toString ();
    }

    private static void setHcUtilConversionMethod (final ColumnTypeInfo jti, final String methodName)
    {
        jti.m_javaConversionMethod = "HcUtil." + methodName;
        jti.m_importsConversionMethod.add ("au.com.breakpoint.hedron.core.HcUtil");
    }

    private static void setJavaSqlType (final ColumnTypeInfo jti, final String typesType)
    {
        jti.m_jdbcJavaSqlType = "Types." + typesType;
        jti.m_importsJavaSqlType.add ("java.sql.Types");
    }

    // i. If the field is a boolean, compute (f ? 1 : 0).
    // ii. If the field is a byte, char, short, or int, compute (int) f.
    // iii. If the field is a long, compute (int) (f ^ (f >>> 32)).
    // iv. If the field is a float, compute Float.floatToIntBits(f).
    // v. If the field is a double, compute Double.doubleToLongBits(f), and
    // then hash the resulting long as in step 2.a.iii.
    // vi. If the field is an object reference and this class’s equals method
    // compares the field by recursively invoking equals, recursively
    // invoke hashCode on the field
    public static final String EQUALS_INTRINSIC = "m_column%s == eRhs.m_column%s";

    public static final String EQUALS_OBJECT =
        "au.com.breakpoint.hedron.core.HcUtil.safeEquals (m_column%s, eRhs.m_column%s)";

    public static final String HASHCODE_ALREADY_INT_COMPATIBLE = "m_column%s";

    public static final String HASHCODE_AS_INT = "(int) m_column%s";

    public static final String HASHCODE_OBJECT = "au.com.breakpoint.hedron.core.HcUtil.safeHashCode (m_column%s)";

    public static final Map<String, ColumnTypeInfo> m_jtiCache = GenericFactory.newHashMap ();

    public static final BigDecimal MAX_BYTE = new BigDecimal (Byte.MAX_VALUE);

    public static final BigDecimal MAX_INT = new BigDecimal (Integer.MAX_VALUE);

    public static final BigDecimal MAX_LONG = new BigDecimal (Long.MAX_VALUE);

    public static final BigDecimal MAX_SHORT = new BigDecimal (Short.MAX_VALUE);
}
