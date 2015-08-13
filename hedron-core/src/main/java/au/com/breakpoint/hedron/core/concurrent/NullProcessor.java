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
package au.com.breakpoint.hedron.core.concurrent;

import java.util.concurrent.CountDownLatch;
import au.com.breakpoint.hedron.core.log.Logging;

/**
 * Block until shutdown.
 */
public class NullProcessor implements IProcessor
{
    public NullProcessor (final String name)
    {
        m_name = name;
    }

    @Override
    public void awaitShutdownComplete ()
    {
        // Shuts down completely in signalShutdown. Nothing to wait for.
        Logging.logInfo ("%s shutdown complete %s", this, System.nanoTime ());
    }

    @Override
    public void processUntilShutdown ()
    {
        Logging.logInfo ("%s starting execution", this);

        try
        {
            m_shutdownLatch.await ();
        }
        catch (final InterruptedException e)
        {
            // Shutting down... don't care.
        }
    }

    @Override
    public void signalShutdown ()
    {
        Logging.logInfo ("%s initiating shutdown", this);

        m_shutdownLatch.countDown ();
    }

    @Override
    public String toString ()
    {
        return String.format ("NullProcessor [%s]", m_name);
    }

    private final String m_name;

    /** Used to coordinate shutdown */
    private final CountDownLatch m_shutdownLatch = new CountDownLatch (1);
}
