package au.com.breakpoint.hedron.core.dao;

import au.com.breakpoint.hedron.core.HcUtil;

/** JSON compatible data structure, hence no m_ prefixes */
public class DatabaseDefinition
{
    @Override
    public String toString ()
    {
        return HcUtil.toString (dsn, schema, url, username, password, driverClassName);
    }

    public String driverClassName;

    public String dsn;

    public String password;

    public String schema;

    public String url;

    public String username;
}
