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
package au.com.breakpoint.hedron.core.log;

import java.util.List;
import au.com.breakpoint.hedron.core.Counter;
import au.com.breakpoint.hedron.core.CounterRange;
import au.com.breakpoint.hedron.core.CounterThroughput;
import au.com.breakpoint.hedron.core.MaxCounter;
import au.com.breakpoint.hedron.core.TimedScope;
import au.com.breakpoint.hedron.core.value.FormattedStringValue;
import au.com.breakpoint.hedron.core.value.IValue;
import au.com.breakpoint.hedron.core.value.LazyValue;

/**
 * An IInstrumentor implementation that just writes the instrumentation data to an
 * associated ILogger.
 */
public class InstrumentionStringLogger implements IInstrumentionListener
{
    public InstrumentionStringLogger (final ILogger logger)
    {
        m_logger = logger;
    }

    @Override
    public void onExecutionSummary (final String contextId, final List<TimedScope> listTimedScope,
        final List<CounterThroughput> listCounterThroughput, final List<Counter> listCounter,
        final List<CounterRange> listCounterRange, final List<MaxCounter> listMaxCounter)
    {
        final LazyValue<String> summary = LazyValue.of ( () ->
        {
            final String resultsTimedScope = TimedScope.formatResults (listTimedScope);
            final String resultsCounterThroughput = CounterThroughput.formatResults (listCounterThroughput);
            final String resultsCounter = Counter.formatResults (listCounter);
            final String resultsCounterRange = CounterRange.formatResults (listCounterRange);
            final String resultsMaxCounter = MaxCounter.formatResults (listMaxCounter);

            return String.format ("%s%n%s%n%s%n%s%n%s", resultsTimedScope, resultsCounterThroughput, resultsCounter,
                resultsCounterRange, resultsMaxCounter);
        });
        final IValue<String> v = new FormattedStringValue ("Execution summary%n%s", summary);

        // Log as a text-style message.
        m_logger.logInfo (contextId, v);
    }

    @Override
    public void onOperationFailed (final String contextId, final String messageText, final String operationName,
        final long msecDuration, final long msecLimit, final String limitPropertyName)
    {
        // TODO 0 in practice it was inconvenient having OperationFailed separated from other Fatal assertion messages
        final IValue<String> v =
            new FormattedStringValue ("Operation[%s] failed: %s duration[%s msec] limit[%s msec]%n  %s", operationName,
                contextId, msecDuration, msecLimit, messageText);

        // Log as a text-style message.
        m_logger.logFatal (contextId, v);
    }

    @Override
    public void onOperationSlow (final String contextId, final String operationName, final long msecDuration,
        final long msecLimit, final String limitPropertyName)
    {
        final IValue<String> v = new FormattedStringValue (
            limitPropertyName == null ? "Operation[%s] succeeded late: %s duration[%s msec] limit[%s msec]"
                : "Operation[%s] succeeded late: %s duration[%s msec] limit[%s msec][%s]",
            operationName, contextId, msecDuration, msecLimit, limitPropertyName);

        // Log as a text-style message.
        m_logger.logWarn (contextId, v);
    }

    @Override
    public void onOperationTimedOut (final String contextId, final String operationName, final long msecDuration,
        final long msecLimit, final String limitPropertyName)
    {
        final IValue<String> v = new FormattedStringValue (
            limitPropertyName == null ? "Operation[%s] timeout: %s limit[%s msec]"
                : "Operation[%s] timeout: %s limit[%s msec][%s]",
            operationName, contextId, msecLimit, limitPropertyName);

        // Log as a text-style message.
        m_logger.logWarn (contextId, v);
    }

    /** Logger that is used to write instrumentation events */
    private final ILogger m_logger;
}
