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

import java.io.Serializable;

/** Stats data, immutable for concurrency reasons */
public class TimedScopeStatistics implements Serializable
{
    public TimedScopeStatistics (final long durationTotal, final long executionsCount,
        final long successfulExecutionsCount, final long slowExecutionsCount, final long durationMin,
        final long durationMax, final LogarithmicHistogram32 histogram)
    {
        m_durationTotal = durationTotal;
        m_executionsCount = executionsCount;
        m_successfulExecutionsCount = successfulExecutionsCount;
        m_slowExecutionsCount = slowExecutionsCount;
        m_durationMin = durationMin;
        m_durationMax = durationMax;
        m_histogramMsec = histogram;
    }

    public double getAverageMsec ()
    {
        return HcUtil.calculateAverageMsec (getDurationTotal (), getExecutionsCount ());
    }

    public double getAverageMsecInit ()
    {
        return HcUtil.calculateAverageMsec (getDurationTotal () - getDurationMax (), getExecutionsCount () - 1);
    }

    public String getAverageString ()
    {
        return HcUtil.formatNanoseconds (HcUtil.calculateAverage (getDurationTotal (), getExecutionsCount ()));
    }

    public String getAverageStringInit ()
    {
        return HcUtil.formatNanoseconds (
            HcUtil.calculateAverage (getDurationTotal () - getDurationMax (), getExecutionsCount () - 1));
    }

    public long getDurationMax ()
    {
        return m_durationMax;
    }

    public long getDurationMin ()
    {
        return m_durationMin;
    }

    public long getDurationTotal ()
    {
        return m_durationTotal;
    }

    public long getExecutionsCount ()
    {
        return m_executionsCount;
    }

    public LogarithmicHistogram32 getHistogramMsec ()
    {
        return m_histogramMsec;
    }

    public long getSlowExecutionsCount ()
    {
        return m_slowExecutionsCount;
    }

    public long getSuccessfulExecutionsCount ()
    {
        return m_successfulExecutionsCount;
    }

    public static TimedScopeStatistics getInitialstate ()
    {
        return InitialState;
    }

    private final long m_durationMax;

    private final long m_durationMin;

    private final long m_durationTotal;

    private final long m_executionsCount;

    /** Histogram of msec duration... max value 2147483647 = 24.855 days */
    private final LogarithmicHistogram32 m_histogramMsec;

    private final long m_slowExecutionsCount;

    private final long m_successfulExecutionsCount;

    public static final TimedScopeStatistics InitialState =
        new TimedScopeStatistics (0, 0, 0, 0, Long.MAX_VALUE, 0, new LogarithmicHistogram32 ());

    private static final long serialVersionUID = 1524325380593144455L;
}
