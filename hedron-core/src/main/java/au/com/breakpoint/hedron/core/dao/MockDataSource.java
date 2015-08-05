package au.com.breakpoint.hedron.core.dao;

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
        Logging.logInfo ("MockDataSource.getConnection ()");
        return new MockConnection ();
    }

    @Override
    public Connection getConnection (final String username, final String password) throws SQLException
    {
        Logging.logInfo ("MockDataSource.getConnection ()");
        return new MockConnection ();
    }

    @Override
    public int getLoginTimeout () throws SQLException
    {
        Logging.logInfo ("MockDataSource.getLoginTimeout ()");
        return 0;
    }

    @Override
    public PrintWriter getLogWriter () throws SQLException
    {
        Logging.logInfo ("MockDataSource.getLogWriter ()");
        return null;
    }

    @Override
    public Logger getParentLogger () throws SQLFeatureNotSupportedException
    {
        Logging.logInfo ("MockDataSource.getParentLogger ()");
        return null;
    }

    @Override
    public boolean isWrapperFor (final Class<?> iface) throws SQLException
    {
        Logging.logInfo ("MockDataSource.isWrapperFor ()");
        return false;
    }

    @Override
    public void setLoginTimeout (final int seconds) throws SQLException
    {
        Logging.logInfo ("MockDataSource.setLoginTimeout ()");
    }

    @Override
    public void setLogWriter (final PrintWriter out) throws SQLException
    {
        Logging.logInfo ("MockDataSource.setLogWriter ()");
    }

    @Override
    public <T> T unwrap (final Class<T> iface) throws SQLException
    {
        Logging.logInfo ("MockDataSource.unwrap ()");
        return null;
    }
}
