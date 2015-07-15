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