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
 * Builds upon JdbcConnectionCachingExecutionScope.
 */
public class SimpleJdbcTransactionScope extends JdbcConnectionCachingExecutionScope implements ITransactionScope
{
    public SimpleJdbcTransactionScope ()
    {
        super ("SimpleJdbc");
    }

    @Override
    public void close ()
    {
        // Control transaction outcome.
        try
        {
            // Check whether has been set up.
            if (m_dataSourceName != null)
            {
                Connection connection = null;
                try
                {
                    connection = getDataSource ().getConnection ();
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
                        Logging.logDebug ("SimpleJdbcTransactionScope committing for data source [%s]",
                            m_dataSourceName);
                        connection.commit ();
                    }
                    else
                    {
                        Logging.logWarn ("SimpleJdbcTransactionScope rolling back for data source [%s]",
                            m_dataSourceName);
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

    /** Convenience constructor for one unnamed data source */
    public void setup (final DataSource realDataSource)
    {
        setup (new NamedDataSource (DEFAULT_DATA_SOURCE_NAME, realDataSource));
    }

    /** Constructor for one named data source */
    public void setup (final NamedDataSource dataSource)
    {
        //Logging.logDebug ("SimpleJdbcTransactionScope setup [%s]", dataSource.getName ());

        // Only one data source.
        super.setup (new NamedDataSource[]
        {
                dataSource
        });
        m_dataSourceName = dataSource.getName ();// remember the name of the one data source

        // Turn off auto-commit. This connection will be held for the remainder of the transaction
        // by JdbcConnectionCachingDataSource.
        final Connection connection = HcUtilJdbc.getConnection (getDataSource ());
        HcUtilJdbc.setAutoCommit (connection, false);
    }

    @Override
    public void voteToCommit ()
    {
        m_shouldCommit = true;
    }

    /**
     * This class specialisation takes only one data source. Get it from the base class
     * (which supports multiple data sources) by passing down the remembered name of the
     * data source.
     */
    private DataSource getDataSource ()
    {
        return getDataSource (m_dataSourceName);
    }

    private String m_dataSourceName;

    private boolean m_shouldCommit;

    private static final String DEFAULT_DATA_SOURCE_NAME = "/local";
}
