//                       __________________________________
//                ______|         Copyright 2008           |______
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
package au.com.breakpoint.hedron.core.concurrent.mock;

import au.com.breakpoint.hedron.core.concurrent.IProcessor;
import au.com.breakpoint.hedron.core.context.AssertException;
import au.com.breakpoint.hedron.core.context.OpResult;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.core.log.Logging;

public class ProcessorMock implements IProcessor
{
    public ProcessorMock (final String suffix, final boolean createException, final long msec)
    {
        m_createException = createException;
        m_suffix = suffix;
        m_msecUntilException = msec;
    }

    @Override
    public void awaitShutdownComplete ()
    {
    }

    @Override
    public void processUntilShutdown ()
    {
        final long startMsec = System.currentTimeMillis ();

        for (; !m_shutdownRequested;)
        {
            //System.out.printf ("ProcessorMock%s [%s]%n", m_suffix, i);

            if (m_createException && (System.currentTimeMillis () - startMsec) > m_msecUntilException)
            {
                Logging.logInfo ("ProcessorMock%s simulating exception after %smsec", m_suffix, m_msecUntilException);
                throw new AssertException (OpResult.Severity.Error, null, false);
            }

            sleep (m_msecUntilException);
        }

        Logging.logInfo ("ProcessorMock%s finished", m_suffix);
    }

    @Override
    public void signalShutdown ()
    {
        m_shutdownRequested = true;
    }

    private void sleep (final long msec)
    {
        try
        {
            Thread.sleep (msec);
        }
        catch (final InterruptedException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }
    }

    private final boolean m_createException;

    private final long m_msecUntilException;

    private volatile boolean m_shutdownRequested;

    private final String m_suffix;
}
