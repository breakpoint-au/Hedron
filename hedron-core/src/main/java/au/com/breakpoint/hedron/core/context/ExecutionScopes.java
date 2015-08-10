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

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import au.com.breakpoint.hedron.core.HcUtil;

/**
 * Class containing generic methods that put ExecutionScope closures around blocks of user
 * code. See execute () methods.
 */
public class ExecutionScopes
{
    /**
     * Implements the execution scope pattern for a system entry point (such as thread or
     * timer task) that should not emit exceptions.
     *
     * @param service
     *            Instance of the service to call
     */
    public static void executeFaultBarrier (final Runnable service)
    {
        try (final IScope scope = new ExecutionScope ())
        {
            // Execute the business service method.
            service.run ();
        }
        catch (final Throwable e)
        {
            // ThreadContext.assertXxxx methods already log details at time of assertion,
            // but ThreadContext.logException does not re-log them.
            ThreadContext.logException (e);
        }
    }

    /**
     * Implements the execution scope pattern for a program main (). This implements a top
     * level fault barrier, with a nested ExecutionScope.
     *
     * @param <TOutput>
     *            Type of the returned item of data
     * @param service
     *            Instance of the service to call
     * @return Returned item of data
     */
    public static <TOutput> TOutput executeFaultBarrier (final Supplier<? extends TOutput> service)
    {
        TOutput output = null;

        try (final IScope scope = new ExecutionScope ())
        {
            // Execute the business service method.
            output = service.get ();
        }
        catch (final Throwable e)
        {
            // ThreadContext.assertXxxx methods already log details at time of assertion,
            // but ThreadContext.logException does not re-log them.
            ThreadContext.logException (e);
        }

        return output;
    }

    /**
     * Implements the execution scope pattern for a program main (). This implements a top
     * level fault barrier, and shuts down any background threads.
     *
     * @param <TOutput>
     *            Type of the returned item of data
     * @param service
     *            Instance of the service to call
     */
    public static void executeProgram (final Runnable service)
    {
        try (final IScope scope = new ExecutionScope ())
        {
            // Execute the business service method.
            service.run ();
        }
        catch (final Throwable e)
        {
            // ThreadContext.assertXxxx methods already log details at time of assertion,
            // but ThreadContext.logException does not re-log them.
            ThreadContext.logException (e);
        }
        finally
        {
            HcUtil.coordinateShutdown ();
        }
    }

    public static void executeTaskProtected (final Executor executor, final Runnable service)
    {
        // Wrap task in ExecutionScope.
        executor.execute ( () -> executeFaultBarrier (service));
    }

    /** Test program for shutdown */
    public static void main (final String... args)
    {
        ExecutionScopes.executeProgram ( () -> HcUtil.pause (1000));
    }

    /**
     * Schedule the execution of a task, wrapping the service in an ExecutionScope.
     *
     * @param scheduler
     *            Scheduler used to execute the service
     * @param task
     *            The work to be executed
     * @param periodMsec
     *            Period in milliseconds
     * @return a handle that can be used to wait on / cancel the scheduling
     */
    public static ScheduledFuture<?> scheduleOnce (final ScheduledExecutorService scheduler, final Runnable task,
        final int periodMsec)
    {
        // Wrap task in ExecutionScope. Note: this is not a fault barrier;
        // any exceptions will flow back via FutureTask.get ().
        return scheduler.schedule (task, periodMsec, TimeUnit.MILLISECONDS);
    }

    /**
     * Schedule the execution of a service, wrapping the service in an ExecutionScope.
     *
     * @param scheduler
     *            Scheduler used to execute the service
     * @param task
     *            The work to be executed
     * @param periodMsec
     *            Period in milliseconds
     * @param faultBarrier
     *            whether to log and protect against exceptions
     * @return a handle that can be used to wait on / cancel the scheduling
     */
    public static ScheduledFuture<?> schedulePeriodically (final ScheduledExecutorService scheduler,
        final Runnable task, final long periodMsec, final boolean faultBarrier)
    {
        return schedulePeriodically (scheduler, task, periodMsec, periodMsec, faultBarrier);
    }

    /**
     * Schedule the execution of a service, wrapping the service in an ExecutionScope.
     *
     * @param scheduler
     *            Scheduler used to execute the service
     * @param task
     *            The work to be executed
     * @param periodMsec
     *            Period in milliseconds
     * @param faultBarrier
     *            whether to log and protect against exceptions
     * @return a handle that can be used to wait on / cancel the scheduling
     */
    public static ScheduledFuture<?> schedulePeriodically (final ScheduledExecutorService scheduler,
        final Runnable task, final long initialDelayMsec, final long periodMsec, final boolean faultBarrier)
    {
        // Only wrap the task if fault barrier is specified.
        final Runnable r = faultBarrier ? () -> executeFaultBarrier (task) : task;

        // Schedule this periodic processor.
        return scheduler.scheduleAtFixedRate (r, initialDelayMsec, periodMsec, TimeUnit.MILLISECONDS);
    }
}
