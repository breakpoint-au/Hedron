package au.com.breakpoint.hedron.core.dao;

import java.sql.Connection;
import org.apache.commons.dbcp2.DelegatingConnection;

public class MockConnection extends DelegatingConnection<Connection>
{
    public MockConnection ()
    {
        super (null);
    }
}
