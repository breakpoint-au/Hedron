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
