package au.com.breakpoint.hedron.core.dao.mock;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.DataSource;
import au.com.breakpoint.hedron.core.log.Logging;

public class MockDataSource implements DataSource
{
    @Override
    public Connection getConnection () throws SQLException
    {
        Logging.logDebug ("MockDataSource.getConnection ()");
        ++m_countAction;
        return new MockConnection ();
    }

    @Override
    public Connection getConnection (final String username, final String password) throws SQLException
    {
        Logging.logDebug ("MockDataSource.getConnection ()");
        ++m_countAction;
        return new MockConnection ();
    }

    public int getCountAction ()
    {
        return m_countAction;
    }

    @Override
    public int getLoginTimeout () throws SQLException
    {
        Logging.logDebug ("MockDataSource.getLoginTimeout ()");
        return 0;
    }

    @Override
    public PrintWriter getLogWriter () throws SQLException
    {
        Logging.logDebug ("MockDataSource.getLogWriter ()");
        return null;
    }

    @Override
    public Logger getParentLogger () throws SQLFeatureNotSupportedException
    {
        Logging.logDebug ("MockDataSource.getParentLogger ()");
        return null;
    }

    @Override
    public boolean isWrapperFor (final Class<?> iface) throws SQLException
    {
        Logging.logDebug ("MockDataSource.isWrapperFor ()");
        return false;
    }

    public void reset ()
    {
        m_countAction = 0;
    }

    @Override
    public void setLoginTimeout (final int seconds) throws SQLException
    {
        Logging.logDebug ("MockDataSource.setLoginTimeout ()");
    }

    @Override
    public void setLogWriter (final PrintWriter out) throws SQLException
    {
        Logging.logDebug ("MockDataSource.setLogWriter ()");
    }

    @Override
    public <T> T unwrap (final Class<T> iface) throws SQLException
    {
        Logging.logDebug ("MockDataSource.unwrap ()");
        return null;
    }

    private volatile int m_countAction;
}
