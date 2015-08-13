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
import java.util.concurrent.atomic.AtomicLong;

/**
 * Class providing counter support, with monitoring of max level (high tide mark).
 */
public class MaxCounter implements Serializable
{
    private MaxCounter (final Class<?> usingClass)
    {
        this (usingClass.getSimpleName ());
    }

    private MaxCounter (final Class<?> usingClass, final String name)
    {
        this (HcUtil.qualifyName (usingClass, name));
    }

    private MaxCounter (final MaxCounter rhs)
    {
        m_name = rhs.m_name;
        m_counter.set (rhs.m_counter.get ());
        m_counterMax.set (rhs.m_counterMax.get ());
    }

    private MaxCounter (final String name)
    {
        m_name = name;
    }

    public long decrement ()
    {
        // No point monitoring min level since always starts at 0.
        return m_counter.decrementAndGet ();
    }

    public long get ()
    {
        return m_counter.get ();
    }

    public long getMax ()
    {
        return m_counterMax.get ();
    }

    public String getName ()
    {
        return m_name;
    }

    public long increment ()
    {
        final long value = m_counter.incrementAndGet ();
        monitorMax (value);
        return value;
    }

    @Override
    public String toString ()
    {
        return String.format ("\"%s\",%s,%s", m_name, m_counter.get (), m_counterMax.get ());
    }

    private void monitorMax (final long value)
    {
        // Lock-free implementation (optimistic 'locking' with retry).
        boolean done;
        do
        {
            // If max value already been updated concurrently to a value greater than this one,
            // ignore this one.
            final long prevMax = m_counterMax.get ();
            done = value > prevMax ? m_counterMax.compareAndSet (prevMax, value) : true;
        }
        while (!done);
    }

    private void reset ()
    {
        m_counter.set (0);
        m_counterMax.set (0);
    }

    public static void appendLines (final StringBuilder sb, final List<?> results)
    {
        for (final Object e : results)
        {
            sb.append (HcUtil.NewLine);
            sb.append (e.toString ());
        }
    }

    public static void clearResults ()
    {
        // Concurrent map: no need for synchronized block
        for (final MaxCounter ts : m_instances.values ())
        {
            ts.reset ();
        }
    }

    public static String formatResults (final List<MaxCounter> results)
    {
        return HcUtil.formatObjects ("MaxCounter,Count,Max", results);
    }

    public static MaxCounter getCounter (final String name)
    {
        return m_instances.computeIfAbsent (name, MaxCounter::new);// key == name
    }

    public static String getResults (final boolean excludeUnused)
    {
        // Take a copy of the data to avoid locking.
        final List<MaxCounter> results = getSortedResults (excludeUnused);

        return formatResults (results);
    }

    public static List<MaxCounter> getSortedResults (final boolean excludeUnused)
    {
        final List<MaxCounter> results = GenericFactory.newArrayList ();

        // Concurrent map: no need for synchronized block
        for (final MaxCounter ts : m_instances.values ())
        {
            if (!excludeUnused || ts.get () > 0)
            {
                results.add (new MaxCounter (ts));// deep copy the data
            }
        }

        results.sort (comparing (MaxCounter::getName));
        return results;
    }

    /** Factory method */
    public static MaxCounter of (final Class<?> usingClass)
    {
        return of (usingClass.getSimpleName ());
    }

    /** Factory method */
    public static MaxCounter of (final Class<?> usingClass, final String name)
    {
        return of (HcUtil.qualifyName (usingClass, name));
    }

    /** Factory method */
    public static MaxCounter of (final String name)
    {
        final MaxCounter ts = new MaxCounter (name);

        // Concurrent map: no need for synchronized block
        m_instances.put (name, ts);

        return ts;
    }

    /** Gather stats in lock-free / immutable manner for max concurrency */
    private final AtomicLong m_counter = new AtomicLong ();

    private final AtomicLong m_counterMax = new AtomicLong ();

    private final String m_name;

    /** Repository of all counters */
    private static final ConcurrentMap<String, MaxCounter> m_instances = GenericFactory.newConcurrentHashMap ();

    private static final long serialVersionUID = -7510197226816591108L;

    static
    {
        HcUtil.registerSummaryData (MaxCounter::getResults);
    }
}
