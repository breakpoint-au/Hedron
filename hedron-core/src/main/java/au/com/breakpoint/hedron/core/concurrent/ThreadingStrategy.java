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

import java.util.concurrent.ExecutorService;
import java.util.function.Function;

/**
 * Enumeration excapsulating the supported threading strategy. Initialise each enum value
 * with function object that can create the appropriate executor.
 */
public enum ThreadingStrategy
{
    /** Create a flexible thread pool */
    CachedThreadPool (unused -> Concurrency.createCachedThreadPool ("StrategyCachedThreadPool", false)),

    /** Create a fixed-size thread pool */
    FixedThreadPool (
        fixedThreadPoolSize -> Concurrency.createFixedThreadPool (fixedThreadPoolSize, "StrategyFixedThreadPool",
            false)),

    /** Use a single thread */
    SingleThreaded (unused -> Concurrency.createSingleThreadExecutor ("StrategySingleThreaded", false));

    /**
     * @param factory
     *            Function object that can create the appropriate executor.
     */
    private ThreadingStrategy (final Function<Integer, ExecutorService> factory)
    {
        m_factory = factory;
    }

    /**
     * Instantiate the executor according to the configured threading strategy.
     *
     * @param arg
     *            Used only if fixed thread pool strategy is being used
     * @return executor providing the configured threading strategy.
     */
    public ExecutorService instantiate (final Integer arg)
    {
        return m_factory.apply (arg);
    }

    private final Function<Integer, ExecutorService> m_factory;
}
