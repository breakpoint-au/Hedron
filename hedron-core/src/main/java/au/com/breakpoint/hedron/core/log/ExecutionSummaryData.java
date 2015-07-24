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

import java.io.Serializable;
import com.google.gson.annotations.SerializedName;
import au.com.breakpoint.hedron.core.Counter;
import au.com.breakpoint.hedron.core.CounterRange;
import au.com.breakpoint.hedron.core.CounterThroughput;
import au.com.breakpoint.hedron.core.MaxCounter;
import au.com.breakpoint.hedron.core.TimedScope;

public class ExecutionSummaryData implements Serializable
{
    public ExecutionSummaryData (final TimedScope[] ts, final CounterThroughput[] ctps, final Counter[] cs,
        final CounterRange[] crs, final MaxCounter[] mcs)
    {
        m_timedScopes = ts;
        m_counterThroughputs = ctps;
        m_counters = cs;
        m_counterRanges = crs;
        m_maxCounters = mcs;
    }

    public CounterRange[] getCounterRanges ()
    {
        return m_counterRanges;
    }

    public Counter[] getCounters ()
    {
        return m_counters;
    }

    public CounterThroughput[] getCounterThroughputs ()
    {
        return m_counterThroughputs;
    }

    public MaxCounter[] getMaxCounters ()
    {
        return m_maxCounters;
    }

    public TimedScope[] getTimedScopes ()
    {
        return m_timedScopes;
    }

    @SerializedName ("p")
    private final CounterRange[] m_counterRanges;

    @SerializedName ("q")
    private final Counter[] m_counters;

    @SerializedName ("r")
    private final CounterThroughput[] m_counterThroughputs;

    @SerializedName ("s")
    private final MaxCounter[] m_maxCounters;

    @SerializedName ("t")
    private final TimedScope[] m_timedScopes;

    private static final long serialVersionUID = 1L;
}
