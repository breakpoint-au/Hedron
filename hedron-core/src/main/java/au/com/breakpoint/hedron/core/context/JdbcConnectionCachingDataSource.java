//                       __________________________________
//                ______|         Copyright 2008           |______
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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.SmartDataSource;

/**
 * An implementation of Spring's SmartDataSource that holds onto a JDBC connection until
 * it is told to close it via the closeConnection () method. This is useful for performing
 * multiple Spring JDBC actions on a connection. Spring's behaviour is to get a new
 * connection each time. JdbcConnectionCachingDataSource ensures that the same connection
 * is used for subsequent Spring JDBC actions, which is required for transactional
 * handling at the middle tier.
 *
 * JdbcConnectionCachingDataSource decorates a real DataSource (decorator pattern).
 */
public class JdbcConnectionCachingDataSource implements SmartDataSource
{
    public JdbcConnectionCachingDataSource (final DataSource realDataSource)
    {
        m_realDataSource = realDataSource;
    }

    public void closeConnection ()
    {
        // Allow the Spring framework to close the connection. releaseConnection () calls
        // shouldClose ().
        m_shouldClose = true;
        DataSourceUtils.releaseConnection (m_connection, this);
        m_connection = null;
    }

    @Override
    public Connection getConnection () throws SQLException
    {
        if (m_connection == null)
        {
            m_connection = m_realDataSource.getConnection ();
        }

        return m_connection;
    }

    @Override
    public Connection getConnection (final String username, final String password) throws SQLException
    {
        if (m_connection == null)
        {
            m_connection = m_realDataSource.getConnection (username, password);
        }

        return m_connection;
    }

    @Override
    public int getLoginTimeout () throws SQLException
    {
        // Return 0, indicating the default system timeout is to be used.
        return 0;
    }

    @Override
    public PrintWriter getLogWriter () throws SQLException
    {
        ThreadContext.assertFault (false, "Unimplemented method getLogWriter ()");
        return null;
    }

    @Override
    public Logger getParentLogger () throws SQLFeatureNotSupportedException
    {
        return m_realDataSource.getParentLogger ();
    }

    @Override
    public boolean isWrapperFor (final Class<?> arg0) throws SQLException
    {
        return false;
    }

    @Override
    public void setLoginTimeout (final int arg0) throws SQLException
    {
        ThreadContext.assertFault (false, "Unimplemented method setLoginTimeout (int arg0)");
    }

    @Override
    public void setLogWriter (final PrintWriter arg0) throws SQLException
    {
        ThreadContext.assertFault (false, "Unimplemented method setLogWriter (PrintWriter arg0)");
    }

    /**
     * @see org.springframework.jdbc.datasource.SmartDataSource#shouldClose(java.sql.Connection)
     */
    @Override
    public boolean shouldClose (final Connection con)
    {
        return m_shouldClose;
    }

    @Override
    public String toString ()
    {
        String s;
        if (m_realDataSource instanceof BasicDataSource)
        {
            final BasicDataSource bds = (BasicDataSource) m_realDataSource;

            s = String.format ("[%s, %s, %s]", bds.getUrl (), bds.getUsername (), bds.getPassword ());
        }
        else
        {
            s = String.format ("[%s]", m_realDataSource.toString ());
        }
        return s;
    }

    @Override
    public <T> T unwrap (final Class<T> arg0) throws SQLException
    {
        return null;
    }

    private Connection m_connection;

    private final DataSource m_realDataSource;

    private boolean m_shouldClose;// connection is held until this is set
}
