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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import au.com.breakpoint.hedron.core.concurrent.CallingThreadExecutor;

/**
 * Simple executor that executes the task in the caller's thread. Blocks until the task is
 * complete. Incomplete implementation useful mostly for mocking.
 */
public class CallingThreadMockExecutorService extends CallingThreadExecutor implements ExecutorService
{
    @Override
    public boolean awaitTermination (final long timeout, final TimeUnit unit) throws InterruptedException
    {
        return false;
    }

    @Override
    public <T> List<Future<T>> invokeAll (final Collection<? extends Callable<T>> tasks) throws InterruptedException
    {
        return null;
    }

    @Override
    public <T> List<Future<T>> invokeAll (final Collection<? extends Callable<T>> tasks, final long timeout,
        final TimeUnit unit) throws InterruptedException
    {
        return null;
    }

    @Override
    public <T> T invokeAny (final Collection<? extends Callable<T>> tasks)
        throws InterruptedException, ExecutionException
    {
        return null;
    }

    @Override
    public <T> T invokeAny (final Collection<? extends Callable<T>> tasks, final long timeout, final TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException
    {
        return null;
    }

    @Override
    public boolean isShutdown ()
    {
        return false;
    }

    @Override
    public boolean isTerminated ()
    {
        return false;
    }

    @Override
    public void shutdown ()
    {
    }

    @Override
    public List<Runnable> shutdownNow ()
    {
        return null;
    }

    @Override
    public <T> Future<T> submit (final Callable<T> task)
    {
        return new FutureTask<> (task);
    }

    @Override
    public Future<?> submit (final Runnable task)
    {
        task.run ();
        return new CompletedFuture<Void> (null);
    }

    @Override
    public <T> Future<T> submit (final Runnable task, final T result)
    {
        return null;
    }

    private static class CompletedFuture<T> implements Future<T>
    {
        public CompletedFuture (final T v)
        {
            m_value = v;
            m_exception = null;
        }

        @SuppressWarnings ("unused")
        public CompletedFuture (final T v, final Throwable re)
        {
            m_value = v;
            m_exception = re;
        }

        @Override
        public boolean cancel (final boolean mayInterruptIfRunning)
        {
            return false;
        }

        @Override
        public T get () throws ExecutionException
        {
            if (m_exception != null)
            {
                throw new ExecutionException (m_exception);
            }

            return m_value;
        }

        @Override
        public T get (final long timeout, final TimeUnit unit) throws ExecutionException
        {
            return get ();
        }

        @Override
        public boolean isCancelled ()
        {
            return false;
        }

        @Override
        public boolean isDone ()
        {
            return true;
        }

        private final Throwable m_exception;

        private final T m_value;
    }
}
