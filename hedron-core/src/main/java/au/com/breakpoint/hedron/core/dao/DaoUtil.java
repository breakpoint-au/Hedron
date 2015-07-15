//                       __________________________________
//                ______|         Copyright 2008           |______
//                \     |     Breakpoint Pty Limited       |     /
//                 \    |   http://www.breakpoint.com.au   |    /
//                 /    |__________________________________|    \
//                /_________/                          \_________\
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
// except in compliance with the License. You may obtain a copy of the License at
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
// either express or implied. See the License for the specific language governing
// permissions and limitations under the License.
//
package au.com.breakpoint.hedron.core.dao;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.object.StoredProcedure;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.GenericFactory;
import au.com.breakpoint.hedron.core.TimedScope;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.core.dao.IEntity.ColumnType;
import au.com.breakpoint.hedron.core.log.Logging;

public class DaoUtil
{
    public static class SqlData
    {
        public SqlData ()
        {
        }

        public SqlData (final String sql, final Object[] argValues)
        {
            m_sql = sql;
            m_parameterValues = argValues;
        }

        public int getParameterCount ()
        {
            return m_parameterValues.length;
        }

        public Object[] getParameterValues ()
        {
            return m_parameterValues;
        }

        public String getSql ()
        {
            return m_sql;
        }

        public void setParameterValues (final Object[] argValues)
        {
            m_parameterValues = argValues;
        }

        public void setSql (final String sql)
        {
            m_sql = sql;
        }

        private Object[] m_parameterValues;

        private String m_sql;
    }

    public static String appendOrderBySqlClause (final String sqlFragment, final String[] columnPhysicalNames,
        OrderByElement... orderByColumns)
    {
        // Handle null order by.
        if (orderByColumns == null)
        {
            orderByColumns = new OrderByElement[0];
        }

        final StringBuilder sb = new StringBuilder ();
        sb.append (sqlFragment);

        for (int i = 0; i < orderByColumns.length; ++i)
        {
            final OrderByElement obe = orderByColumns[i];

            sb.append (i == 0 ? " order by " : ", ");
            sb.append (columnPhysicalNames[obe.getColumnId ()]);
            if (!obe.isAscending ())
            {
                sb.append (" desc");
            }
        }

        final String sqlString = sb.toString ();

        return sqlString;
    }

    public static String dumpCreateEntityCode (final IEntity<?> e, final String variableName)
    {
        final StringBuilder sb = new StringBuilder ();

        final Class<?> theClass = e.getClass ();
        final String className = theClass.getSimpleName ();
        sb.append ("final " + className + " " + variableName + " = new " + className + " ();\n");

        final Field fields[] = theClass.getDeclaredFields ();
        for (final Field f : fields)
        {
            final String name = f.getName ();
            final Class<?> type = f.getType ();

            //            System.out.println ("name              = " + name);
            //            System.out.println ("decl class = " + f.getDeclaringClass ());
            //            System.out.println ("type              = " + type);
            //            final int mod = f.getModifiers ();
            //            System.out.println ("modifiers = " + Modifier.toString (mod));
            //            System.out.println ("-----");

            final String prefix = "m_column";
            if (name.startsWith (prefix))
            {
                f.setAccessible (true);

                final String columnName = name.substring (prefix.length ());

                try
                {
                    if (type == boolean.class)
                    {
                        sb.append (String.format ("%s.set%s (%s);%n", variableName, columnName, f.getBoolean (e)));
                    }
                    else if (type == int.class)
                    {
                        sb.append (String.format ("%s.set%s (%s);%n", variableName, columnName, f.getInt (e)));
                    }
                    else if (type == long.class)
                    {
                        sb.append (String.format ("%s.set%s (%s);%n", variableName, columnName, f.getLong (e)));
                    }
                    else if (type == double.class)
                    {
                        sb.append (String.format ("%s.set%s (%s);%n", variableName, columnName, f.getDouble (e)));
                    }
                    else
                    {
                        final Object value = f.get (e);
                        if (value != null)
                        {
                            sb.append (String.format ("%s.set%s (\"%s\");%n", variableName, columnName, value));
                        }
                    }
                }
                catch (IllegalArgumentException | IllegalAccessException ex)
                {
                    // Propagate exception as unchecked fault up to the fault barrier.
                    ThreadContext.throwFault (ex);
                }
            }
        }

        sb.append ("\n");

        return sb.toString ();
    }

    /**
     * Good for unit test creation.
     *
     * @param entities
     * @return
     */
    public static <T extends IEntity<?>> String dumpCreateEntityCode (final List<T> entities)
    {
        final StringBuilder sb = new StringBuilder ();

        if (entities.size () > 0)
        {
            if (true)
            {
                int i = 0;
                for (final T e : entities)
                {
                    sb.append (dumpCreateEntityCode (e, "e" + i));
                    ++i;
                }
            }

            final T e = entities.get (0);
            final Class<?> theClass = e.getClass ();
            final String className = theClass.getSimpleName ();

            sb.append ("final " + className + "[] entities" + className + " = new " + className + "[] { ");

            for (int i = 0; i < entities.size (); ++i)
            {
                if (i > 0)
                {
                    sb.append (", ");
                }
                sb.append ("e" + i);
            }
            sb.append (" };");

            sb.append ("\n");
        }

        return sb.toString ();
    }

    /**
     * Good for unit test creation.
     *
     * @param entities
     * @return
     */
    public static <T extends IEntity<?>> String dumpCreateEntityCode (final T[] entities)
    {
        return dumpCreateEntityCode (Arrays.asList (entities));
    }

    public static <T> T getOutParameter (final Map<?, ?> outParams, final String parameterName,
        final String storedProcedureName)
    {
        ThreadContext.assertFault (outParams.containsKey (parameterName),
            "The out parameter [%s] does not exist for stored procedure [%s]", parameterName, storedProcedureName);
        final Object op = outParams.get (parameterName);// can validly be null

        return HcUtil.uncheckedCast (op);// doesn't compile under javac 5
    }

    public static SqlData getWhereExpressionSqlData (final String sqlFragment, final String[] columnPhysicalNames,
        WhereElement[] clauses)
    {
        // Handle null clauses
        if (clauses == null)
        {
            clauses = new WhereElement[0];
        }

        final StringBuilder sb = new StringBuilder ();
        sb.append (sqlFragment);

        final Object[] argValues = new Object[clauses.length];
        for (int i = 0; i < clauses.length; ++i)
        {
            final WhereElement c = clauses[i];

            sb.append (i == 0 ? " where " : " and ");
            sb.append (columnPhysicalNames[c.getColumnId ()]);
            sb.append (' ');
            sb.append (c.getClauseSql ());
            sb.append (" ?");

            argValues[i] = c.getValue ();
        }

        final String sqlString = sb.toString ();

        return new SqlData (sqlString, argValues);
    }

    public static <TPrimaryKey> int performDelete (final DataSource dataSource, final IEntity<TPrimaryKey> e,
        final String sql)
    {
        // Profiling code is in executeSql ()
        return performExecuteSql (dataSource, sql, e.getColumnValues (ColumnType.PrimaryKey));
    }

    public static int performDelete (final DataSource dataSource, final String sqlFragment, final String[] columnNames,
        final WhereElement[] whereElements)
    {
        final SqlData sfd = getWhereExpressionSqlData (sqlFragment, columnNames, whereElements);
        final String sql = sfd.getSql ();
        Logging.logTrace ("SQL created [%s]", sql);

        final TimedScope ts = getTimedScope (sql);

        final Integer rowCount = ts.execute ( () ->
        {
            final JdbcTemplate sjt = new JdbcTemplate (dataSource);
            return sjt.update (sql, sfd.getParameterValues ());
        });
        Logging.logDebug ("[%s] %s affected", sql, rowCount);

        return rowCount;
    }

    public static <TPrimaryKey> void performDeleteBatch (final DataSource dataSource, final IEntity<TPrimaryKey>[] es,
        final String sql)
    {
        // Profiling code is in executeSqlBatch ()
        final List<Object[]> batchColumnValues = new ArrayList<Object[]> ();
        for (final IEntity<TPrimaryKey> e : es)
        {
            batchColumnValues.add (e.getColumnValues (ColumnType.PrimaryKey));
        }

        executeSqlBatch (dataSource, batchColumnValues, sql);
    }

    public static Map<?, ?> performExecute (final StoredProcedure sp, final Map<String, Object> inParams,
        final String storedProcedureName)
    {
        final TimedScope ts = getTimedScope (storedProcedureName);

        final Map<?, ?> outParams = ts.execute ( () -> sp.execute (inParams));
        Logging.logDebug ("[%s] %s outparams", storedProcedureName, outParams.size ());

        return outParams;
    }

    public static <TPrimaryKey> int performExecuteSql (final DataSource dataSource, final String sql,
        final Object... parameterValues)
    {
        // Time the execution of the sql.
        final TimedScope ts = getTimedScope (sql);

        final Integer rowCount = ts.execute ( () ->
        {
            final JdbcTemplate sjt = new JdbcTemplate (dataSource);
            return sjt.update (sql, parameterValues);
        });

        Logging.logDebug ("[%s] %s affected", sql, rowCount);
        return rowCount;
    }

    public static <T extends IEntity<?>> List<T> performFetch (final DataSource dataSource, final RowMapper<T> mapper,
        final String sql, final Object... parameterValues)
    {
        final TimedScope ts = getTimedScope (sql);

        final List<T> entities = ts.execute ( () ->
        {
            final JdbcTemplate sjt = new JdbcTemplate (dataSource);
            return sjt.query (sql, mapper, parameterValues);
        });
        //System.out.println (dumpCreateEntityCode (entities));

        Logging.logDebug ("[%s] %s rows", sql, entities.size ());
        return entities;
    }

    public static <T extends IEntity<?>> List<T> performFetch (final DataSource dataSource, final RowMapper<T> mapper,
        final String sqlFragment, final String[] columnNames, final OrderByElement[] orderByElements)
    {
        final String sql = appendOrderBySqlClause (sqlFragment, columnNames, orderByElements);
        Logging.logTrace ("SQL created [%s]", sql);

        return performFetch (dataSource, mapper, sql);
    }

    public static <T extends IEntity<?>> List<T> performFetch (final DataSource dataSource, final RowMapper<T> mapper,
        final String sqlFragment, final String[] columnNames, final WhereElement[] whereElements,
        final OrderByElement[] orderByElements)
    {
        final SqlData sfd = getWhereExpressionSqlData (sqlFragment, columnNames, whereElements);
        final String sql = appendOrderBySqlClause (sfd.getSql (), columnNames, orderByElements);
        Logging.logTrace ("SQL created [%s]", sql);

        return performFetch (dataSource, mapper, sql, sfd.getParameterValues ());
    }

    public static <TPrimaryKey> int performInsert (final DataSource dataSource, final IEntity<TPrimaryKey> e,
        final String sql)
    {
        // Profiling code is in executeSql ()
        return performExecuteSql (dataSource, sql, e.getColumnValues (ColumnType.All));
    }

    public static <TPrimaryKey> void performInsertBatch (final DataSource dataSource, final IEntity<TPrimaryKey>[] es,
        final String sql)
    {
        // Profiling code is in executeSqlBatch ()
        final List<Object[]> batchColumnValues = new ArrayList<Object[]> ();
        for (final IEntity<TPrimaryKey> e : es)
        {
            batchColumnValues.add (e.getColumnValues (ColumnType.All));
        }

        executeSqlBatch (dataSource, batchColumnValues, sql);
    }

    public static <TPrimaryKey> int performUpdate (final DataSource dataSource, final IEntity<TPrimaryKey> e,
        final String sql)
    {
        // Profiling code is in executeSql ()
        return performExecuteSql (dataSource, sql, getUpdateColumnValues (e));
    }

    public static int performUpdate (final DataSource dataSource, final String tableName,
        final String[] columnPhysicalNames, final SetElement[] newValues, final WhereElement[] clauses)
    {
        final Object[] columnValues = new Object[newValues.length + clauses.length];

        // Build the update SQL string and gather the column values.
        final StringBuilder sb = new StringBuilder ();
        sb.append ("update ");
        sb.append (tableName);
        sb.append (" set ");

        int index = 0;
        for (final SetElement c : newValues)
        {
            if (index > 0)
            {
                sb.append (", ");
            }
            sb.append (columnPhysicalNames[c.getColumnId ()]);
            sb.append (" = ?");

            columnValues[index++] = c.getValue ();
        }

        for (final WhereElement c : clauses)
        {
            columnValues[index++] = c.getValue ();
        }

        // Time the execution of the sql.
        final SqlData sfd = getWhereExpressionSqlData (sb.toString (), columnPhysicalNames, clauses);
        final String sql = sfd.getSql ();
        Logging.logTrace ("SQL created [%s]", sql);

        final TimedScope ts = getTimedScope (sql);

        // Perform the update.
        final Integer rowCount = ts.execute ( () ->
        {
            final JdbcTemplate sjt = new JdbcTemplate (dataSource);
            return sjt.update (sql, columnValues);
        });
        Logging.logDebug ("[%s] %s affected", sql, rowCount);

        return rowCount;
    }

    public static <TPrimaryKey> void performUpdateBatch (final DataSource dataSource, final IEntity<TPrimaryKey>[] es,
        final String sql)
    {
        // Profiling code is in executeSqlBatch ()
        final List<Object[]> batchColumnValues = new ArrayList<Object[]> ();

        for (final IEntity<TPrimaryKey> e : es)
        {
            batchColumnValues.add (getUpdateColumnValues (e));
        }

        executeSqlBatch (dataSource, batchColumnValues, sql);
    }

    private static int[] executeSqlBatch (final DataSource dataSource, final List<Object[]> batchColumnValues,
        final String sql)
    {
        final TimedScope ts = getTimedScope ("BATCH [" + sql + "]");

        final Supplier<int[]> timedService = () ->
        {
            final JdbcTemplate sjt = new JdbcTemplate (dataSource);
            final int[] result = sjt.batchUpdate (sql, batchColumnValues);

            //        final int[] updateCounts = sjt.batchUpdate (sql, batchColumnValues);
            //        int updateCountTotal = 0;
            //        for (final int i : updateCounts)
            //        {
            //            updateCountTotal += i;
            //        }
            //        return updateCountTotal;

            return result;
        };
        final int[] rowCounts = ts.execute (timedService);
        Logging.logDebug ("BATCH [%s] %s affected", sql, Arrays.toString (rowCounts));

        return rowCounts;
    }

    private static TimedScope getTimedScope (final String sql)
    {
        final TimedScope ts = TimedScope.getTimedScope ("[" + sql + "]");
        return ts;
    }

    private static <TPrimaryKey> Object[] getUpdateColumnValues (final IEntity<TPrimaryKey> e)
    {
        // Get Updatable column values then PK's.
        final Object[] updatableColumnValues = e.getColumnValues (ColumnType.Updatable);
        ThreadContext.assertFault (HcUtil.safeGetLength (updatableColumnValues) > 0,
            "Entity [%s] has no updatable columns", e.getClass ().getCanonicalName ());

        final Object[] pkColumnValues = e.getColumnValues (ColumnType.PrimaryKey);
        ThreadContext.assertFault (HcUtil.safeGetLength (pkColumnValues) > 0, "Entity [%s] has no primary key columns",
            e.getClass ().getCanonicalName ());

        final List<Object> listUpdatable = Arrays.asList (updatableColumnValues);
        final List<Object> listPk = Arrays.asList (pkColumnValues);

        final List<Object> l = GenericFactory.newArrayList ();
        l.addAll (listUpdatable);
        l.addAll (listPk);

        final Object[] columnValues = l.toArray (new Object[l.size ()]);
        return columnValues;
    }
}
