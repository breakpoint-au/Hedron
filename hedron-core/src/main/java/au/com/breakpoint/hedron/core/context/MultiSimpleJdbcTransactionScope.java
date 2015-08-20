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
 * Builds upon JdbcConnectionCachingExecutionScope. Maintains a collection of individual
 * simple transactions. There is no expectation of being able to commit coherently across
 * them.
 */
public class MultiSimpleJdbcTransactionScope extends JdbcConnectionCachingExecutionScope implements ITransactionScope
{
    public MultiSimpleJdbcTransactionScope ()
    {
        super ("MultiSimpleJdbc");
    }

    @Override
    public void close ()
    {
        // Control transaction outcome.
        try
        {
            // Commit/rollback on each data source.
            final String[] dsns = getDataSourceNames ();
            for (final String dsn : dsns)
            {
                final DataSource ds = getDataSource (dsn);
                Connection connection = null;
                try
                {
                    connection = ds.getConnection ();
                }
                catch (final SQLException e)
                {
                    // Don't allow exception since might be closing the connection
                    // as part of error path handling.
                }

                // Any SQLExceptions from here on will throw to the catch block below.
                if (connection != null)
                {
                    if (m_shouldCommit)
                    {
                        Logging.logDebug ("MultiSimpleJdbcTransactionScope committing for data source [%s]", dsn);
                        connection.commit ();
                    }
                    else
                    {
                        Logging.logWarn ("MultiSimpleJdbcTransactionScope rolling back for data source [%s]", dsn);
                        connection.rollback ();
                    }
                }
            }
        }
        catch (final SQLException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }
        finally
        {
            super.close ();
        }
    }

    @Override
    public void setup (final NamedDataSource[] dataSources)
    {
        Logging.logDebug ("MultiSimpleJdbcTransactionScope setup [%s]", new Object[]
        {
                dataSources
        });

        super.setup (dataSources);

        // Turn off auto-commit. Thess connections will be held for the remainder of the transaction
        // by JdbcConnectionCachingDataSource.
        for (final NamedDataSource namedDataSource : dataSources)
        {
            final DataSource jccds = getDataSource (namedDataSource.getName ());
            final Connection connection = HcUtilJdbc.getConnection (jccds);

            HcUtilJdbc.setAutoCommit (connection, false);
        }
    }

    @Override
    public void voteToCommit ()
    {
        m_shouldCommit = true;
    }

    private boolean m_shouldCommit;
}
