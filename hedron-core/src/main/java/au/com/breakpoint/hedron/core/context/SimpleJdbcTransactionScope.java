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
package au.com.breakpoint.hedron.core.context;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import au.com.breakpoint.hedron.core.HcUtilJdbc;
import au.com.breakpoint.hedron.core.log.Logging;

/**
 * Scopes a simple (single) JDBC transaction.
 */
public class SimpleJdbcTransactionScope extends ExecutionScope implements ITransactionScope
{
    public SimpleJdbcTransactionScope (final DataSource realDataSource)
    {
        super ("SimpleJdbc");

        m_connectionCachingDataSource = new JdbcConnectionCachingDataSource (realDataSource);

        final Connection connection = HcUtilJdbc.getConnection (m_connectionCachingDataSource);
        //System.out.printf ("Create connection [%s]%n", connection);

        // Set the isolation level before starting the transaction.
        //HcUtilJdbc.setTransactionIsolationLevel (connection, Connection.TRANSACTION_SERIALIZABLE);

        // Turn off auto-commit. This connection will be held for the remainder of the transaction
        // by JdbcConnectionCachingDataSource. This starts a transaction.
        HcUtilJdbc.setAutoCommit (connection, false);
    }

    @Override
    public void close ()
    {
        final Connection connection = m_connectionCachingDataSource.getConnectionCached ();
        //System.out.printf ("Closing connection [%s]%n", connection);

        if (connection != null)
        {
            // Control transaction outcome.
            try
            {
                if (m_shouldCommit)
                {
                    Logging.logDebug ("SimpleJdbcTransactionScope committing for data source");
                    connection.commit ();
                }
                else
                {
                    Logging.logWarn ("SimpleJdbcTransactionScope rolling back for data source");
                    connection.rollback ();
                }

                // Return connection to original state to allow pooling reuse.
                HcUtilJdbc.setAutoCommit (connection, true);
            }
            catch (final SQLException e)
            {
                // Propagate exception as unchecked fault up to the fault barrier.
                ThreadContext.throwFault (e);
            }
            finally
            {
                m_connectionCachingDataSource.closeConnection ();
            }
        }

        super.close ();
    }

    @Override
    public JdbcConnectionCachingDataSource getDataSource ()
    {
        return m_connectionCachingDataSource;
    }

    @Override
    public void voteToCommit ()
    {
        m_shouldCommit = true;
    }

    private final JdbcConnectionCachingDataSource m_connectionCachingDataSource;

    private boolean m_shouldCommit;
}
