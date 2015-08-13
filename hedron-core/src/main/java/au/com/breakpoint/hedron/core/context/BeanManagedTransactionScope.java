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

import javax.ejb.EJBContext;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.log.Logging;

/**
 * Builds upon JdbcConnectionCachingExecutionScope.
 */
public class BeanManagedTransactionScope extends JdbcConnectionCachingExecutionScope implements ITransactionScope
{
    public BeanManagedTransactionScope ()
    {
        super ("BeanManaged");
    }

    @Override
    public void close ()
    {
        // Control transaction outcome.
        try
        {
            // Check whether has been set up. Also, allow m_ejbContext to be null for IDE-based
            // debugging support (transactions don't work).
            if (m_ejbContext != null)
            {
                final UserTransaction txn = m_ejbContext.getUserTransaction ();
                if (m_shouldCommit)
                {
                    Logging.logDebug ("BeanManagedTransactionScope committing");
                    txn.commit ();
                }
                else
                {
                    Logging.logWarn ("BeanManagedTransactionScope rolling back");
                    txn.rollback ();
                }
            }
        }
        catch (SecurityException | IllegalStateException | RollbackException | HeuristicMixedException
            | HeuristicRollbackException | SystemException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }
        finally
        {
            super.close ();
        }
    }

    public void setup (final EJBContext ejbContext, final NamedDataSource[] dataSources)
    {
        super.setup (dataSources);

        m_ejbContext = ejbContext;

        // Allow m_ejbContext to be null for IDE-based debugging support (transactions
        // don't work).
        if (m_ejbContext != null)
        {
            Logging.logDebug ("BeanManagedTransactionScope setup %s data sources", HcUtil.safeGetLength (dataSources));

            final UserTransaction txn = m_ejbContext.getUserTransaction ();
            try
            {
                txn.begin ();
            }
            catch (NotSupportedException | SystemException e)
            {
                // Propagate exception as unchecked fault up to the fault barrier.
                ThreadContext.throwFault (e);
            }
        }
    }

    @Override
    public void voteToCommit ()
    {
        m_shouldCommit = true;
    }

    private EJBContext m_ejbContext;

    private boolean m_shouldCommit;
}
