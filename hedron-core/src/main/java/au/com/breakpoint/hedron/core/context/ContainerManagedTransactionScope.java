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

import javax.ejb.EJBContext;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.log.Logging;

/**
 * Builds upon JdbcConnectionCachingExecutionScope.
 */
public class ContainerManagedTransactionScope extends JdbcConnectionCachingExecutionScope implements ITransactionScope
{
    public ContainerManagedTransactionScope ()
    {
        super ("ContainerManaged");
    }

    @Override
    public void close ()
    {
        // Control transaction outcome.
        try
        {
            // Check whether has been set up.
            if (m_ejbContext != null)
            {
                // Control transaction outcome. Default CMT action is to commit.
                if (m_shouldCommit)
                {
                    Logging.logDebug ("ContainerManagedTransactionScope allowing commit");
                }
                else
                {
                    // Tell the container to roll back the overall transaction.
                    Logging.logWarn ("ContainerManagedTransactionScope setting rollback only");
                    m_ejbContext.setRollbackOnly ();
                }
            }
        }
        finally
        {
            super.close ();
        }
    }

    public void setup (final EJBContext ejbContext, final NamedDataSource[] dataSources)
    {
        Logging.logDebug ("ContainerManagedTransactionScope setup %s data sources", HcUtil.safeGetLength (dataSources));

        super.setup (dataSources);

        m_ejbContext = ejbContext;
    }

    @Override
    public void voteToCommit ()
    {
        m_shouldCommit = true;
    }

    private EJBContext m_ejbContext;

    private boolean m_shouldCommit;
}
