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
package au.com.breakpoint.hedron.core.log;

import java.util.List;
import au.com.breakpoint.hedron.core.Counter;
import au.com.breakpoint.hedron.core.CounterRange;
import au.com.breakpoint.hedron.core.CounterThroughput;
import au.com.breakpoint.hedron.core.MaxCounter;
import au.com.breakpoint.hedron.core.TimedScope;

public interface IInstrumentionListener
{
    void onExecutionSummary (String contextId, List<TimedScope> listTimedScope,
        List<CounterThroughput> listCounterThroughput, List<Counter> listCounter, List<CounterRange> listCounterRange,
        List<MaxCounter> listMaxCounter);

    void onOperationFailed (String contextId, String messageText, String operationName, long msecDuration,
        long msecLimit, String limitPropertyName);

    void onOperationSlow (String contextId, String operationName, long msecDuration, long msecLimit,
        String limitPropertyName);

    void onOperationTimedOut (final String contextId, String operationName, long msecDuration, long msecLimit,
        String limitPropertyName);

    default void setActive (@SuppressWarnings ("unused") final boolean isActive)
    {
    }
}
