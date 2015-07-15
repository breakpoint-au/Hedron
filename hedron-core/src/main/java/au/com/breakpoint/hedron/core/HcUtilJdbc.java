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
package au.com.breakpoint.hedron.core;

import java.sql.Connection;
import java.sql.SQLException;
import javax.ejb.EJBContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import au.com.breakpoint.hedron.core.context.ThreadContext;

public class HcUtilJdbc
{
    /**
     * Gets a JDBC connection from the specified data source, translating SQLException to
     * unchecked FaultException.
     *
     * @param dataSource
     * @return The JDBC connection
     */
    public static Connection getConnection (final DataSource dataSource)
    {
        Connection connection = null;
        try
        {
            connection = dataSource.getConnection ();
        }
        catch (final SQLException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }

        return connection;
    }

    public static DataSource getJndiDataSource (final String jndiDsn)
    {
        DataSource ds = null;
        try
        {
            final Context ctx = new InitialContext ();
            ds = (DataSource) ctx.lookup (jndiDsn);
        }
        catch (final NamingException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }

        return ds;
    }

    /**
     * Set the auto-commit behaviour for the specified connection.
     *
     * @param connection
     * @param autoCommit
     */
    public static void setAutoCommit (final Connection connection, final boolean autoCommit)
    {
        try
        {
            connection.setAutoCommit (autoCommit);
        }
        catch (final SQLException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }
    }

    /**
     * Sets the transaction isolation level.
     *
     * @param connection
     *            The Connection to set the isolation level for.
     *
     * @param isolationLevel
     *            Use the java.sql.Connection values: TRANSACTION_READ_UNCOMMITTED = 1;
     *            TRANSACTION_READ_COMMITTED = 2; TRANSACTION_REPEATABLE_READ = 4;
     *            TRANSACTION_SERIALIZABLE = 8;
     */
    public static void setTransactionIsolationLevel (final Connection connection, final int isolationLevel)
    {
        if (isolationLevel != -1)
        {
            try
            {
                connection.setTransactionIsolation (isolationLevel);
            }
            catch (final SQLException e)
            {
                // Propagate exception as unchecked fault up to the fault barrier.
                ThreadContext.throwFault (e);
            }
        }
    }

    /**
     * Sets the timeout for the specified EJB session context's transaction.
     *
     * @param ejbContext
     *            EJB session context.
     *
     * @param transactionTimeoutSeconds
     *            The value of the timeout in seconds. If the value is zero, the
     *            transaction service restores the default value. If the value is negative
     *            a SystemException is thrown.
     */
    public static void setTransactionTimeout (final EJBContext ejbContext, final int transactionTimeoutSeconds)
    {
        final UserTransaction txn = ejbContext.getUserTransaction ();
        setTransactionTimeout (txn, transactionTimeoutSeconds);
    }

    /**
     * Sets the timeout for the specified EJB session context's transaction.
     *
     * @param txn
     *            Specified transaction object.
     *
     * @param transactionTimeoutSeconds
     *            The value of the timeout in seconds. If the value is zero, the
     *            transaction service restores the default value. If the value is negative
     *            a SystemException is thrown.
     */
    public static void setTransactionTimeout (final UserTransaction txn, final int transactionTimeoutSeconds)
    {
        try
        {
            txn.setTransactionTimeout (transactionTimeoutSeconds);
        }
        catch (final SystemException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }
    }
}
