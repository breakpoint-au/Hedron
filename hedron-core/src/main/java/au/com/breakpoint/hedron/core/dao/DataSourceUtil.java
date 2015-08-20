//                       __________________________________
//                ______|      Copyright 2008-2015         |______
//                \     |     Breakpoint Pty Limited       |     /
//                 \    |   http://www.breakpoint.com.au   |    /
//                 /    |__________________________________|    \
//                /_________/                          \_________\
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
// except in compliance with the License. You may obtain a copy of the License at
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
// either express or implied. See the License for the specific language governing
// permissions and limitations under the License.
//
package au.com.breakpoint.hedron.core.dao;

import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import au.com.breakpoint.hedron.core.IFactoryArg;

public class DataSourceUtil
{
    /**
     * Create the data source according to the configurable policy (defaults to DBCP
     * BasicDataSource.
     *
     * @param d
     *            details of the data source to be created
     * @return created data source
     */
    public static DataSource createDataSource (final DatabaseDefinition d)
    {
        return m_dataSourceFactory.newInstance (d);
    }

    public static IFactoryArg<DatabaseDefinition, DataSource> getDataSourceFactory ()
    {
        return m_dataSourceFactory;
    }

    public static void setDataSourceFactory (final IFactoryArg<DatabaseDefinition, DataSource> dataSourceFactory)
    {
        m_dataSourceFactory = dataSourceFactory;
    }

    /**
     * Configurable policy for creating the data source (defaults to DBCP2
     * BasicDataSource).
     */
    private static volatile IFactoryArg<DatabaseDefinition, DataSource> m_dataSourceFactory = d ->
    {
        final BasicDataSource bds = new BasicDataSource ();

        bds.setDriverClassName (d.driverClassName);
        bds.setUrl (d.url);
        bds.setUsername (d.username);
        bds.setPassword (d.password);
        bds.setDefaultCatalog (d.schema);

        return bds;
    };
}
