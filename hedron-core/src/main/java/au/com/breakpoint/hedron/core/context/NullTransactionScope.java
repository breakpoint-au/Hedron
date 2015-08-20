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

import javax.sql.DataSource;
import au.com.breakpoint.hedron.core.log.Logging;

/**
 * This class is a dummy transaction scope, useful for debugging EJB-wrapped POJO's in
 * Tomcat, etc.
 *
 * Builds upon JdbcConnectionCachingExecutionScope.
 */
public class NullTransactionScope extends JdbcConnectionCachingExecutionScope implements ITransactionScope
{
    public NullTransactionScope ()
    {
        super ("NullTransactionScope");
    }

    /** Convenience constructor for one unnamed data source */
    public void setup (final DataSource realDataSource)
    {
        setup (new NamedDataSource (DEFAULT_DATA_SOURCE_NAME, realDataSource));
    }

    /** Constructor for one named data source */
    public void setup (final NamedDataSource dataSource)
    {
        Logging.logDebug ("NullTransactionScope setup [%s]", dataSource.getName ());

        // Only one data source.
        super.setup (new NamedDataSource[]
        {
                dataSource
        });
    }

    @Override
    public void voteToCommit ()
    {
        // Ignore.
    }

    // Only one data source is applicable
    private static final String DEFAULT_DATA_SOURCE_NAME = "/null";// this could be any unlikely string
}
