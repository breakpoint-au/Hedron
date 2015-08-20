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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import au.com.breakpoint.hedron.core.Tuple.E2;

public class CounterRange implements Serializable
{
    private CounterRange (final Class<?> usingClass)
    {
        this (usingClass.getSimpleName ());
    }

    private CounterRange (final Class<?> usingClass, final String name)
    {
        this (HcUtil.qualifyName (usingClass, name));
    }

    private CounterRange (final CounterRange rhs)
    {
        m_name = rhs.m_name;
        m_counters = new AtomicReference<> (rhs.m_counters.get ());
    }

    private CounterRange (final String name)
    {
        m_name = name;
        m_counters = new AtomicReference<> (Counters.InitialState);
    }

    public void add (final long value, final long delta)
    {
        updateCounters (value, delta);
    }

    public Counters getCounters ()
    {
        return m_counters.get ();
    }

    public String getName ()
    {
        return m_name;
    }

    public void increment (final long value)
    {
        add (value, 1);
    }

    @Override
    public String toString ()
    {
        return String.format ("\"%s\",[%s]", m_name, m_counters.get ().getCountsString ());
    }

    private void reset ()
    {
        m_counters.getAndSet (Counters.InitialState);
    }

    private void updateCounters (final long value, final long delta)
    {
        // Lock-free implementation (optimistic 'locking' with retry).
        //boolean done;
        //do
        //{
        //    final Counters prevCounters = m_counters.get ();
        //    final Counters newCounters = updateCounters (value, delta, prevCounters);
        //
        //    done = m_counters.compareAndSet (prevCounters, newCounters);
        //}
        //while (!done);

        // Java 8 encapsulates compareAndSet loop.
        m_counters.updateAndGet (prevCounters -> updateCounters (value, delta, prevCounters));
    }

    /**
     * Update the counters.
     *
     * @param delta
     *
     * @return new immutable counters instance.
     */
    private Counters updateCounters (final long value, final long delta, final Counters prevCounters)
    {
        final long[] counts = prevCounters.m_counts;

        long[] newCounts = null;
        if (counts.length == 0)
        {
            // First entry.
            newCounts = new long[]
            {
                    value,
                    delta
            };
        }
        else
        {
            int index = -1;
            for (int i = 0; index == -1 && i < counts.length; i += 2)
            {
                if (value <= counts[i])
                {
                    // Insertion/update point.
                    index = i;
                }
            }

            if (index == -1)
            {
                // Have to add the new entries at the end.
                newCounts = Arrays.copyOf (counts, counts.length + 2);
                newCounts[counts.length] = value;
                newCounts[counts.length + 1] = delta;
            }
            else if (value == counts[index])
            {
                // Counter exists. Update it.
                newCounts = counts.clone ();
                newCounts[index + 1] += delta;
            }
            else
            {
                newCounts = new long[counts.length + 2];

                // Copy the entries prior to insertion point.
                if (index > 0)
                {
                    System.arraycopy (counts, 0, newCounts, 0, index);
                }

                // Insert the new counter.
                newCounts[index] = value;
                newCounts[index + 1] = delta;

                if (index + 2 < newCounts.length)
                {
                    System.arraycopy (counts, index, newCounts, index + 2, counts.length - index);
                }
            }
        }

        return new Counters (newCounts);
    }

    /** Counters data, immutable for concurrency reasons */
    public static class Counters implements Serializable
    {
        public Counters (final long[] counts)
        {
            m_counts = counts;
        }

        public long[] getCounts ()
        {
            return m_counts;
        }

        public String getCountsString ()
        {
            final StringBuilder sb = new StringBuilder ();

            for (int i = 0; i < m_counts.length; i += 2)
            {
                if (i > 0)
                {
                    sb.append ("; ");
                }

                sb.append (String.format ("%s:%s", m_counts[i], m_counts[i + 1]));
            }

            return sb.toString ();
        }

        public E2<long[], long[]> getEntries ()
        {
            return HcUtil.deinterleave (m_counts);
        }

        /** Two entries per counter; index then value */
        private final long[] m_counts;

        static final Counters InitialState = new Counters (new long[0]);

        private static final long serialVersionUID = 8496419794812656953L;
    }

    public static void clearResults ()
    {
        // Concurrent map: no need for synchronized block
        for (final CounterRange ts : m_instances.values ())
        {
            ts.reset ();
        }
    }

    public static String formatResults (final List<CounterRange> results)
    {
        return HcUtil.formatObjects ("CounterRange,Counts", results);
    }

    public static CounterRange getCounterRange (final String name)
    {
        return m_instances.computeIfAbsent (name, CounterRange::new);// key == name
    }

    public static String getResults (final boolean excludeUnused)
    {
        // Take a copy of the data to avoid locking.
        final List<CounterRange> results = getSortedResults (excludeUnused);

        return formatResults (results);
    }

    public static List<CounterRange> getSortedResults (final boolean excludeUnused)
    {
        final List<CounterRange> results = GenericFactory.newArrayList ();

        // Concurrent map: no need for synchronized block
        for (final CounterRange ts : m_instances.values ())
        {
            if (!excludeUnused || ts.m_counters.get ().getCounts ().length > 0)
            {
                results.add (new CounterRange (ts));// deep copy the data
            }
        }

        results.sort (comparing (CounterRange::getName));
        return results;
    }

    /** Factory method */
    public static CounterRange of (final Class<?> usingClass)
    {
        return of (usingClass.getSimpleName ());
    }

    /** Factory method */
    public static CounterRange of (final Class<?> usingClass, final String name)
    {
        return of (HcUtil.qualifyName (usingClass, name));
    }

    /** Factory method */
    public static CounterRange of (final String name)
    {
        final CounterRange ts = new CounterRange (name);

        // Concurrent map: no need for synchronized block
        m_instances.put (name, ts);

        return ts;
    }

    /** Gather counters in lock-free / immutable manner for max concurrency */
    private final AtomicReference<Counters> m_counters;

    private final String m_name;

    /** Repository of all timed scopes */
    private static final ConcurrentMap<String, CounterRange> m_instances = GenericFactory.newConcurrentHashMap ();

    private static final long serialVersionUID = -7672461017143831888L;

    static
    {
        HcUtil.registerSummaryData (CounterRange::getResults);
    }
}
