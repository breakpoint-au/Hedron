package au.com.breakpoint.hedron.core.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.function.Supplier;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.ResourceScope;
import au.com.breakpoint.hedron.core.ShutdownPriority;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.core.log.Logging;

public class Concurrency
{
    /**
     * A wrapper around Executors.newCachedThreadPool () that handles auto-shutdown of the
     * associated background threads. Because the executor service is held for object
     * lifetime, the ExecutorService itself is returned, not the ResourceScope
     * <ExecutorService>.
     *
     * @param name
     *            Name of the executor, used for debugging info.
     *
     * @return The executor service
     */
    public static ExecutorService createCachedThreadPool (final String name, final boolean isDaemon)
    {
        return createCachedThreadPool (name, isDaemon, true).get ();
    }

    /**
     * A wrapper around Executors.newCachedThreadPool () that optionally handles
     * auto-shutdown of the associated background threads.
     *
     * @param name
     * @param isDaemon
     * @param addShutdownTask
     * @return a ResourceScope<ExecutorService> that can be used in a try-with-resources
     *         block.
     */
    public static ResourceScope<ExecutorService> createCachedThreadPool (final String name, final boolean isDaemon,
        final boolean addShutdownTask)
    {
        final ExecutorService executorService =
            Executors.newCachedThreadPool (new CustomisingThreadFactory (name, isDaemon));

        return packageExecutorService (executorService, name, addShutdownTask);
    }

    /**
     * A wrapper around Executors.newFixedThreadPool () that handles auto-shutdown of the
     * associated background threads. Because the executor service is held for object
     * lifetime, the ExecutorService itself is returned, not the ResourceScope
     * <ExecutorService>.
     *
     * @param name
     *            Name of the executor, used for debugging info.
     *
     * @return The executor service
     */
    public static ExecutorService createFixedThreadPool (final int nrThreads, final String name, final boolean isDaemon)
    {
        return createFixedThreadPool (nrThreads, name, isDaemon, true).get ();
    }

    /**
     * A wrapper around Executors.newFixedThreadPool () that handles auto-shutdown of the
     * associated background threads.
     *
     * @param name
     * @param isDaemon
     * @param addShutdownTask
     * @return a ResourceScope<ExecutorService> that can be used in a try-with-resources
     *         block.
     */
    public static ResourceScope<ExecutorService> createFixedThreadPool (final int nrThreads, final String name,
        final boolean isDaemon, final boolean addShutdownTask)
    {
        final ExecutorService executorService = doCreateFixedThreadPool (nrThreads, name, isDaemon);

        return packageExecutorService (executorService, name, addShutdownTask);
    }

    public static ForkJoinPool createForkJoinPool (final String name)
    {
        return createForkJoinPool (name, true).get ();
    }

    public static ResourceScope<ForkJoinPool> createForkJoinPool (final String name, final boolean addShutdownTask)
    {
        // TODO 0 how to associate name with FJ pool itself.
        //final CustomisingThreadFactory threadFactory = new CustomisingThreadFactory (name, false);

        final ForkJoinPool pool = new ForkJoinPool ();
        return packageExecutorService (pool, name, addShutdownTask);
    }

    /**
     * A wrapper around Executors.newScheduledThreadPool () that handles auto-shutdown of
     * the associated background threads. Because the executor service is held for object
     * lifetime, the ExecutorService itself is returned, not the ResourceScope
     * <ExecutorService>.
     *
     * @param name
     *            Name of the executor, used for debugging info.
     *
     * @return The executor service
     */
    public static ScheduledExecutorService createScheduledThreadPool (final int nrThreads, final String name,
        final boolean isDaemon)
    {
        return createScheduledThreadPool (nrThreads, name, isDaemon, true).get ();
    }

    /**
     * A wrapper around Executors.newScheduledThreadPool () that handles auto-shutdown of
     * the associated background threads.
     *
     * @param name
     * @param isDaemon
     * @param addShutdownTask
     * @return a ResourceScope<ExecutorService> that can be used in a try-with-resources
     *         block.
     */
    public static ResourceScope<ScheduledExecutorService> createScheduledThreadPool (final int nrThreads,
        final String name, final boolean isDaemon, final boolean addShutdownTask)
    {
        final ScheduledExecutorService executorService =
            Executors.newScheduledThreadPool (nrThreads, new CustomisingThreadFactory (name, isDaemon));

        return packageExecutorService (executorService, name, addShutdownTask);
    }

    /**
     * A wrapper around Executors.newSingleThreadExecutor () that handles auto-shutdown of
     * the associated background threads. Because the executor service is held for object
     * lifetime, the ExecutorService itself is returned, not the ResourceScope
     * <ExecutorService>.
     *
     * @param name
     *            Name of the executor, used for debugging info.
     *
     * @return The executor service
     */
    public static ExecutorService createSingleThreadExecutor (final String name, final boolean isDaemon)
    {
        return createSingleThreadExecutor (name, isDaemon, true).get ();
    }

    /**
     * A wrapper around Executors.newSingleThreadExecutor () that optionally handles
     * auto-shutdown of the associated background threads.
     *
     * @param name
     * @param isDaemon
     * @param addShutdownTask
     * @return a ResourceScope<ExecutorService> that can be used in a try-with-resources
     *         block.
     */
    public static ResourceScope<ExecutorService> createSingleThreadExecutor (final String name, final boolean isDaemon,
        final boolean addShutdownTask)
    {
        final ExecutorService executorService =
            Executors.newSingleThreadExecutor (new CustomisingThreadFactory (name, isDaemon));

        return packageExecutorService (executorService, name, addShutdownTask);
    }

    /**
     * A wrapper around Executors.newSingleThreadScheduledExecutor () that handles
     * auto-shutdown of the associated background threads. Because the executor service is
     * held for object lifetime, the ExecutorService itself is returned, not the
     * ResourceScope<ExecutorService>.
     *
     * @param name
     *            Name of the executor, used for debugging info.
     *
     * @return The executor service
     */
    public static ScheduledExecutorService createSingleThreadScheduledExecutor (final String name,
        final boolean isDaemon)
    {
        return createSingleThreadScheduledExecutor (name, isDaemon, true).get ();
    }

    /**
     * A wrapper around Executors.newSingleThreadScheduledExecutor () that handles
     * auto-shutdown of the associated background threads.
     *
     * @param name
     * @param isDaemon
     * @param addShutdownTask
     * @return a ResourceScope<ExecutorService> that can be used in a try-with-resources
     *         block.
     */
    public static ResourceScope<ScheduledExecutorService> createSingleThreadScheduledExecutor (final String name,
        final boolean isDaemon, final boolean addShutdownTask)
    {
        final ScheduledExecutorService executorService =
            Executors.newSingleThreadScheduledExecutor (new CustomisingThreadFactory (name, isDaemon));

        return packageExecutorService (executorService, name, addShutdownTask);
    }

    public static ExecutorService doCreateFixedThreadPool (final int nrThreads, final String name,
        final boolean isDaemon)
    {
        return Executors.newFixedThreadPool (nrThreads, new CustomisingThreadFactory (name, isDaemon));
    }

    /**
     * Executes a service guarded by a semaphore, used to regulate simultaneous access to
     * resource-heavy operations.
     *
     * @param semaphore
     *            used to guard the service
     * @param service
     *            the service itself
     * @return the result of the service
     */
    public static <TOutput> TOutput executeRestricted (final Semaphore semaphore,
        final Supplier<? extends TOutput> service)
    {
        TOutput output = null;

        boolean shouldRelease = false;
        try
        {
            semaphore.acquire ();
            shouldRelease = true;

            output = service.get ();
        }
        catch (final InterruptedException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }
        finally
        {
            if (shouldRelease)
            {
                semaphore.release ();
            }
        }

        return output;
    }

    public static Consumer<? super ExecutorService> getExecutorServiceCloser (final String name)
    {
        return es ->
        {
            //System.out.printf ("** Shutdown %s%n", name);
            HcUtil.shutdownExecutorService (es, name, HcUtil.DEFAULT_THREAD_SHUTDOWN_SECONDS);
        };
    }

    private static <T extends ExecutorService> ResourceScope<T> packageExecutorService (final T executorService,
        final String name, final boolean addShutdownTask)
    {
        // Encapsulate auto-closing (executor shutdown).
        final ResourceScope<T> a = ResourceScope.of (executorService, getExecutorServiceCloser (name), name);

        if (addShutdownTask)
        {
            HcUtil.addShutdownTask (a, ShutdownPriority.ThreadExecution);
        }

        Logging.logDebug ("..created ExecutorService [%s]", name);

        return a;
    }
}
