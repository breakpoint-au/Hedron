package au.com.breakpoint.hedron.core.dao;

import java.sql.Connection;
import java.sql.SQLException;
import org.springframework.jdbc.datasource.DelegatingDataSource;

public class MockDataSource extends DelegatingDataSource
{
    @Override
    public Connection getConnection () throws SQLException
    {
        return super.getConnection ();
    }

    @Override
    public Connection getConnection (final String username, final String password) throws SQLException
    {
        // TODO 0 implement getConnection
        return super.getConnection (username, password);
    }

}
