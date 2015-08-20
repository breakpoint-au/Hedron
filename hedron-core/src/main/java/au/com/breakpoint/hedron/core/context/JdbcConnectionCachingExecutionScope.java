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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;

/**
 * Lazy-creates a store of JDBC connections and holds onto them until the scope is
 * completed. This class is inherently thread-safe since it is stored thread-locally.
 *
 * Builds upon ExecutionScope.
 */
public class JdbcConnectionCachingExecutionScope extends ExecutionScope implements IDataSourceRepository
{
    public JdbcConnectionCachingExecutionScope (final String name)
    {
        super (name);
    }

    @Override
    public void close ()
    {
        try
        {
            // Close any open data source connections.
            for (final JdbcConnectionCachingDataSource ds : m_dataSourceRepository.values ())
            {
                ds.closeConnection ();
            }
        }
        finally
        {
            super.close ();
        }
    }

    @Override
    public DataSource getDataSource (final String dataSourceName)
    {
        final JdbcConnectionCachingDataSource ds = m_dataSourceRepository.get (dataSourceName);
        ThreadContext.assertFault (ds != null, "Unknown data source [%s]", dataSourceName);

        return ds;
    }

    @Override
    public String[] getDataSourceNames ()
    {
        final Set<String> keySet = m_dataSourceRepository.keySet ();
        return keySet.toArray (new String[keySet.size ()]);
    }

    public void setup (final NamedDataSource[] dataSources)
    {
        // Allow this to be used when there are no data sources, such as in a container-managed
        // transaction on a simple MDB.
        if (dataSources != null)
        {
            for (final NamedDataSource p : dataSources)
            {
                final JdbcConnectionCachingDataSource ds = new JdbcConnectionCachingDataSource (p.getDataSource ());
                m_dataSourceRepository.put (p.getName (), ds);
            }
        }
    }

    private final Map<String, JdbcConnectionCachingDataSource> m_dataSourceRepository = new HashMap<> ();
}
