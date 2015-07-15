package au.com.breakpoint.hedron.core.concurrent;

import java.util.List;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.ShutdownPriority;
import au.com.breakpoint.hedron.core.log.Logging;

public class Processors
{
    /**
     * One top level processor that looks after the individual processors. If processors
     * has just one entry then that entry is returned, otherwise they are wrapped in a
     * MultiProcessor instance.
     *
     * @param processors
     *            the individual processors
     * @return single top level processor; if processors has just one entry then that
     *         entry is returned
     */
    public static IProcessor of (final IProcessor... processors)
    {
        return processors.length == 1 ? processors[0] : new MultiProcessor (processors);
    }

    /**
     * One top level processor that looks after the individual processors. If processors
     * has just one entry then that entry is returned, otherwise they are wrapped in a
     * MultiProcessor instance.
     *
     * @param processors
     *            the individual processors
     * @return single top level processor; if processors has just one entry then that
     *         entry is returned
     */
    public static IProcessor of (final List<IProcessor> processors)
    {
        return of (processors.toArray (new IProcessor[processors.size ()]));
    }

    /**
     * Runs a processor until the system shuts down via System.exit (). This is how the
     * Java Service Wrapper controls shutting down Java Windows NT services.
     *
     * @param processor
     */
    public static void runServiceProcessor (final IProcessor processor)
    {
        // One top level processor that looks after the individual processors.
        // Register the shutdown hook for cleanup on JVM shutdown.
        HcUtil.addShutdownTask ( () ->
        {
            // Ask processor tree to shut down.
            Logging.logInfo ("Service process initiating shutdown [%s]", processor);

            // Give the processor a chance to shut down. If the processor is a
            // MultiProcessor, then each individual processor starts shutting down.
            processor.signalShutdown ();

            // Wait for the processor to shut down. If the processor is a
            // MultiProcessor, then it waits for each individual processor shuts down.
            processor.awaitShutdownComplete ();
        } , ShutdownPriority.ApplicationExecution, "IProcessor shutdown");// higher priority than threadpool and instrumentation shutdowns

        processor.processUntilShutdown ();// blocks here
    }
}
