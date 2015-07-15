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
package au.com.breakpoint.hedron.core.concurrent;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.GenericFactory;
import au.com.breakpoint.hedron.core.ResourceScope;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.core.log.Logging;

/**
 * A collection of IProcessor implementations that run concurrently, with the last one of
 * them running under the caller's thread.
 */
public class MultiProcessor implements IProcessor
{
    public MultiProcessor (final IProcessor... processors)
    {
        ThreadContext.assertFault (processors.length > 0, "At least 1 processor must be specified");
        m_processors = processors;
    }

    @Override
    public void awaitShutdownComplete ()
    {
        for (final IProcessor p : m_processors)
        {
            p.awaitShutdownComplete ();
        }

        m_executorService.close ();
        Logging.logInfo ("%s shutdown complete", this);
    }

    @Override
    public void processUntilShutdown ()
    {
        Logging.logInfo ("%s starting execution", this);

        final List<Callable<Void>> tasks = GenericFactory.newArrayList ();
        for (final IProcessor p : m_processors)
        {
            final Callable<Void> task = () ->
            {
                p.processUntilShutdown ();
                return null;
            };
            tasks.add (task);
        }

        // Wait on tasks in the order they complete, so that an exception will interrupt the others.
        // NB: Don't use Concurrency.createFixedThreadPool, since it handles scheduler shutdown,
        // whereas we want to handle shutdown within this processor.
        final String identifier = toString ();
        m_executorService = Concurrency.createFixedThreadPool (m_processors.length, identifier, false, false);

        HcUtil.executeConcurrently (m_executorService.get (), tasks, true);
        Logging.logDebug ("%s returned", identifier);
    }

    @Override
    public void signalShutdown ()
    {
        Logging.logInfo ("%s initiating shutdown", this);

        for (final IProcessor p : m_processors)
        {
            p.signalShutdown ();
        }
    }

    @Override
    public String toString ()
    {
        return String.format ("MultiProcessor %s", Arrays.toString (m_processors));
    }

    private volatile ResourceScope<ExecutorService> m_executorService;

    private final IProcessor[] m_processors;
}
