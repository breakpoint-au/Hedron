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

import java.util.function.Consumer;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.IFactory;
import au.com.breakpoint.hedron.core.context.ExecutionScope;
import au.com.breakpoint.hedron.core.context.IScope;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.core.log.Logging;

/**
 * Strategy pattern style implementation where this layer adds the retry/restart logic to
 * a nested IProcessor implementation. It implements the “let it fail” programming
 * approach; if something “bad” happens, let the processor fail and handle the processor
 * failure by creating another copy and reinitialising everything.
 */
public class RetryProcessor implements IProcessor
{
    public RetryProcessor (final IFactory<IProcessor> factory, final int retryPauseMsec)
    {
        this (factory, retryPauseMsec, null);
    }

    public RetryProcessor (final IFactory<IProcessor> factory, final int retryPauseMsec,
        final Consumer<? super RetryProcessor> restartCallback)
    {
        m_processorFactory = factory;
        m_retryPauseMsec = retryPauseMsec;
        m_restartCallback = restartCallback;

        m_processor = m_processorFactory.newInstance ();
    }

    @Override
    public void awaitShutdownComplete ()
    {
        m_processor.awaitShutdownComplete ();
    }

    @Override
    public void processUntilShutdown ()
    {
        // Loops here since fault exceptions can happen if database goes down temporarily etc.
        do
        {
            // Fault barrier here for the case of database going down etc.
            try
            {
                // Sit in a loop performing the required adaptor function.
                // Wrap in a nested scope for context id tracking.
                try (final IScope scope = new ExecutionScope ("RetryProcessor"))
                {
                    m_processor.processUntilShutdown ();
                }
            }
            catch (final Throwable e)
            {
                ThreadContext.logException (e);
            }

            if (!m_shutdownRequested)
            {
                // Signal the old processor instance to shut down, but don't wait for it to happen.
                m_processor.signalShutdown ();

                // Reinitialise with a new processor instance.
                m_processor = m_processorFactory.newInstance ();

                // A FailureException must have happened in this main thread. This is
                // assumed to be some temporary condition such as infrastructure being offline.
                // Pause and carry on.
                Logging.logInfo ("(pausing on exception)");
                HcUtil.pause (m_retryPauseMsec);

                adviseRestart ();
            }
        }
        while (!m_shutdownRequested);
    }

    @Override
    public void signalShutdown ()
    {
        m_shutdownRequested = true;
        m_processor.signalShutdown ();
    }

    @Override
    public String toString ()
    {
        return m_processor.toString ();
    }

    private void adviseRestart ()
    {
        if (m_restartCallback != null)
        {
            m_restartCallback.accept (this);
        }
    }

    private volatile IProcessor m_processor;

    private final IFactory<IProcessor> m_processorFactory;

    private final Consumer<? super RetryProcessor> m_restartCallback;

    private final int m_retryPauseMsec;

    private volatile boolean m_shutdownRequested;
}
