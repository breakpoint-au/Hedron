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
package au.com.breakpoint.hedron.core;

/**
 * Controls the order that shutdown tasks are run. Lower values are run before higher
 * values. For example ExecutionSummaryWriter shutdown task runs before threadpool
 * shutdown so that it has the chance to send a final set of ExecutionSummary to the log
 * server before shutdown, and so runs at higher priority than threadpool (default 100),
 * less than IProcessor shutdowns (25).
 *
 * The ordering for shutdown as configured below is application, then execution summary
 * writing, then restoring of logging to synchronous, then shut down any
 * application-lifetime connections, then shutdown thread pools.
 *
 * Use these values where applicable. If another level is required, specify the int values
 * directly to HcUtil.addShutdownTask ().
 */
public enum ShutdownPriority
{
    /**
     * Application and IProcessor-level execution - higher priority than threadpool and
     * instrumentation shutdowns so that application-level work can be completed before
     * infrastructure shuts down.
     */
    ApplicationExecution (100),

    /**
     * Teardown of async operation, restoring to synchronous for infrastructure
     * scale-down, eg restore the logging executor to synchronous so that logging can
     * still be used right through the shutdown process.
     */
    AsyncLogging (70),

    /** For application-lifetime connections etc */
    ConnectionManagement (1_000),

    /** Writing or sending of periodic execution summary data */
    ExecutionSummaryWriting (500),

    /** For worker thread pool operation */
    ThreadExecution (10_000);

    private ShutdownPriority (final int value)
    {
        m_value = value;
    }

    public int getValue ()
    {
        return m_value;
    }

    /** Smaller value means higher priority */
    private final int m_value;
}
