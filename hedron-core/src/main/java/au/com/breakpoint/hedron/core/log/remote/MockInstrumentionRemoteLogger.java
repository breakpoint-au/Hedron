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
package au.com.breakpoint.hedron.core.log.remote;

import java.util.List;
import java.util.function.BiFunction;
import au.com.breakpoint.hedron.core.Counter;
import au.com.breakpoint.hedron.core.CounterRange;
import au.com.breakpoint.hedron.core.CounterThroughput;
import au.com.breakpoint.hedron.core.MaxCounter;
import au.com.breakpoint.hedron.core.TimedScope;
import au.com.breakpoint.hedron.core.log.AbstractLogger;
import au.com.breakpoint.hedron.core.log.IInstrumentionListenerLogger;
import au.com.breakpoint.hedron.core.log.Level;
import au.com.breakpoint.hedron.core.value.IValue;

/**
 * An IInstrumentor implementation that sends the instrumentation data to a remote server
 * or some such.
 */
public class MockInstrumentionRemoteLogger extends AbstractLogger implements IInstrumentionListenerLogger
{
    public MockInstrumentionRemoteLogger (final BiFunction<Level, IValue<String>, String> formatter,
        final String levelsConfig)
    {
        super (formatter, levelsConfig, null);

        // TODO 0 Implement constructor MockRemoteInstrumentationLogger
    }

    /** ILogger */
    @Override
    public void logString (final String contextId, final Level level, final String s)
    {
        //        final LogEntry logEntry = new LogEntry (level, Event.Message, s);
        //        final MonitorRequest req = new MonitorRequest (contextId, logEntry);
        //        sendToLogServerUdp (req);

        // TODO 0 implement logString
    }

    /** IInstrumentationLogger */
    @SuppressWarnings ("unused")
    @Override
    public void onExecutionSummary (final String contextId, final List<TimedScope> listTimedScope,
        final List<CounterThroughput> listCounterThroughput, final List<Counter> listCounter,
        final List<CounterRange> listCounterRange, final List<MaxCounter> listMaxCounter)
    {
        final TimedScope[] ts = listTimedScope.toArray (new TimedScope[listTimedScope.size ()]);
        final CounterThroughput[] ctps =
            listCounterThroughput.toArray (new CounterThroughput[listCounterThroughput.size ()]);
        final Counter[] cs = listCounter.toArray (new Counter[listCounter.size ()]);
        final CounterRange[] crs = listCounterRange.toArray (new CounterRange[listCounterRange.size ()]);
        final MaxCounter[] mcs = listMaxCounter.toArray (new MaxCounter[listMaxCounter.size ()]);

        //final MonitorRequest req = new MonitorRequest (contextId, ts, ctps, cs, crs, mcs);

        // TODO 0 implement onExecutionSummary
    }

    /** IInstrumentationLogger */
    @Override
    public void onOperationFailed (final String contextId, final String messageText, final String operationName,
        final long msecDuration, final long msecLimit, final String limitPropertyName)
    {
        //  final LogEntry logEntry = new LogEntry (Level.Fatal, Event.OperationFailed, messageText, o.getName (),
        //      HcUtil.nsToMsec (o.getNsDuration ()), o.getMsecLimit (), limitPropertyName);
        //  final MonitorRequest req = new MonitorRequest (contextId, logEntry);

        // TODO 0 implement logOperationFailed
    }

    /** IInstrumentationLogger */
    @Override
    public void onOperationSlow (final String contextId, final String operationName, final long msecDuration,
        final long msecLimit, final String limitPropertyName)
    {
        //        final LogEntry logEntry = new LogEntry (Level.Warn, Event.OperationSlow, "", o.getName (),
        //            HcUtil.nsToMsec (o.getNsDuration ()), o.getMsecLimit (), limitPropertyName);
        //        final MonitorRequest req = new MonitorRequest (contextId, logEntry);

        // TODO 0 implement logOperationSlow
    }

    /** IInstrumentationLogger */
    @Override
    public void onOperationTimedOut (final String contextId, final String operationName, final long msecDuration,
        final long msecLimit, final String limitPropertyName)
    {
        //        final LogEntry logEntry = new LogEntry (Level.Warn, Event.OperationTimedOut, "", o.getName (),
        //            HcUtil.nsToMsec (o.getNsDuration ()), o.getMsecLimit (), limitPropertyName);
        //        final MonitorRequest req = new MonitorRequest (contextId, logEntry);

        // TODO 0 implement logOperationTimedOut
    }

    /** IInstrumentationLogger */
    @Override
    public void setActive (final boolean isActive)
    {
        // TODO 0 implement setActive
    }
}
