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

import static java.util.Comparator.comparing;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

public class CounterThroughput implements Serializable
{
    private CounterThroughput (final Class<?> usingClass, final long resolutionMsec)
    {
        this (usingClass.getSimpleName (), resolutionMsec);
    }

    private CounterThroughput (final Class<?> usingClass, final String name, final long resolutionMsec)
    {
        this (HcUtil.qualifyName (usingClass, name), resolutionMsec);
    }

    private CounterThroughput (final CounterThroughput rhs)
    {
        m_name = rhs.m_name;
        m_resolutionMsec = rhs.m_resolutionMsec;
        m_counters = new AtomicReference<> (rhs.m_counters.get ());
    }

    private CounterThroughput (final String name, final long resolutionMsec)
    {
        m_name = name;
        m_resolutionMsec = resolutionMsec;
        m_counters = new AtomicReference<> (Counters.InitialState);
    }

    public IClock getClock ()
    {
        return m_clock;
    }

    public Counters getCounters ()
    {
        return m_counters.get ();
    }

    public String getName ()
    {
        return m_name;
    }

    public long getResolutionMsec ()
    {
        return m_resolutionMsec;
    }

    public void increment ()
    {
        //// Lock-free implementation (optimistic 'locking' with retry).
        //boolean done;
        //do
        //{
        //    final Counters prevCounters = m_counters.get ();
        //    final Counters newCounters = doIncrement (prevCounters);
        //
        //    done = m_counters.compareAndSet (prevCounters, newCounters);
        //}
        //while (!done);

        // Java 8 encapsulates compareAndSet loop.
        m_counters.updateAndGet (prevCounters -> doIncrement (prevCounters));
    }

    /** For unit testing */
    public void setClock (final IClock clock)
    {
        m_clock = clock;
    }

    @Override
    public String toString ()
    {
        return String.format ("\"%s\",[%s]", m_name, m_counters.get ().getThroughputs ().toString ());
    }

    private Counters doIncrement (final Counters prevCounters)
    {
        final long nsNow = m_clock.getTimeInNanoseconds ();
        final long startEpochMsec = truncateEpoch (nsNow);

        int newCountForEpoch = prevCounters.getCountForEpoch ();
        long newStartEpochMsec = prevCounters.getStartEpochMsec ();
        LogarithmicHistogram32 newThroughputs = prevCounters.getThroughputs ();

        if (newStartEpochMsec == 0)
        {
            // First time.
            newStartEpochMsec = startEpochMsec;
            newCountForEpoch = 0;
        }
        else if (startEpochMsec > newStartEpochMsec)
        {
            // Handle epoch change. First close of the current epoch.
            newThroughputs = newThroughputs.accumulate (newCountForEpoch);

            newStartEpochMsec += m_resolutionMsec;
            while (newStartEpochMsec < startEpochMsec)
            {
                // A skipped epoch (no events).
                newThroughputs = newThroughputs.accumulate (0);
                newStartEpochMsec += m_resolutionMsec;
            }

            newStartEpochMsec = startEpochMsec;
            newCountForEpoch = 0;
        }

        ++newCountForEpoch;

        return new Counters (newStartEpochMsec, newCountForEpoch, newThroughputs);
    }

    private void reset ()
    {
        m_counters.getAndSet (Counters.InitialState);
    }

    private long truncateEpoch (final long nsNow)
    {
        return (HcUtil.nsToMsec (nsNow) / m_resolutionMsec) * m_resolutionMsec;
    }

    /** Counters data, immutable for concurrency reasons */
    public static class Counters implements Serializable
    {
        public Counters (final long startEpochMsec, final int countForEpoch, final LogarithmicHistogram32 throughputs)
        {
            m_startEpochMsec = startEpochMsec;
            m_countForEpoch = countForEpoch;
            m_throughputs = throughputs;
        }

        public int getCountForEpoch ()
        {
            return m_countForEpoch;
        }

        public long getStartEpochMsec ()
        {
            return m_startEpochMsec;
        }

        public LogarithmicHistogram32 getThroughputs ()
        {
            return m_throughputs;
        }

        private final int m_countForEpoch;

        private final long m_startEpochMsec;

        private final LogarithmicHistogram32 m_throughputs;

        static final Counters InitialState = new Counters (0, 0, new LogarithmicHistogram32 ());

        private static final long serialVersionUID = -6098519547364572662L;
    }

    /** Abstracted for unit test purposes */
    public interface IClock
    {
        public long getTimeInNanoseconds ();
    }

    public static void clearResults ()
    {
        // Concurrent map: no need for synchronized block
        for (final CounterThroughput ts : m_instances.values ())
        {
            ts.reset ();
        }
    }

    public static String formatResults (final List<CounterThroughput> results)
    {
        return HcUtil.formatObjects ("CounterThroughput,Counts", results);
    }

    public static CounterThroughput getCounterThroughput (final String name, final long resolutionMsec)
    {
        return m_instances.computeIfAbsent (name, key -> new CounterThroughput (key, resolutionMsec));// key == name
    }

    public static String getResults (final boolean excludeUnused)
    {
        // Take a copy of the data to avoid locking.
        final List<CounterThroughput> results = getSortedResults (excludeUnused);

        return formatResults (results);
    }

    public static List<CounterThroughput> getSortedResults (final boolean excludeUnused)
    {
        final List<CounterThroughput> results = GenericFactory.newArrayList ();

        // Concurrent map: no need for synchronized block
        for (final CounterThroughput ts : m_instances.values ())
        {
            if (!excludeUnused || !ts.m_counters.get ().getThroughputs ().isUnused ())
            {
                results.add (new CounterThroughput (ts));// deep copy the data
            }
        }

        // Sort the results by name.
        results.sort (comparing (CounterThroughput::getName));
        return results;
    }

    /** Factory method */
    public static CounterThroughput of (final Class<?> usingClass, final long resolutionMsec)
    {
        return of (usingClass.getSimpleName (), resolutionMsec);
    }

    /** Factory method */
    public static CounterThroughput of (final Class<?> usingClass, final String name, final long resolutionMsec)
    {
        return of (HcUtil.qualifyName (usingClass, name), resolutionMsec);
    }

    /** Factory method */
    public static CounterThroughput of (final String name, final long resolutionMsec)
    {
        final CounterThroughput ts = new CounterThroughput (name, resolutionMsec);

        // Concurrent map: no need for synchronized block
        m_instances.put (name, ts);

        return ts;
    }

    private transient IClock m_clock = System::nanoTime;

    /** Gather counters in lock-free / immutable manner for max concurrency */
    private final AtomicReference<Counters> m_counters;

    private final String m_name;

    private final long m_resolutionMsec;

    /** Repository of all timed scopes */
    private static final ConcurrentMap<String, CounterThroughput> m_instances = GenericFactory.newConcurrentHashMap ();

    private static final long serialVersionUID = -6348579948148102192L;

    static
    {
        HcUtil.registerSummaryData (CounterThroughput::getResults);
    }
}
