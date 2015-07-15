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
package au.com.breakpoint.hedron.core;

import static java.util.Comparator.comparing;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Class providing counter support.
 */
public class Counter implements Serializable
{
    private Counter (final Class<?> usingClass)
    {
        this (usingClass.getSimpleName ());
    }

    private Counter (final Class<?> usingClass, final String name)
    {
        this (HcUtil.qualifyName (usingClass, name));
    }

    private Counter (final Counter rhs)
    {
        m_name = rhs.m_name;
        m_counter.set (rhs.m_counter.get ());
    }

    private Counter (final String name)
    {
        m_name = name;
    }

    public long get ()
    {
        return m_counter.get ();
    }

    public String getName ()
    {
        return m_name;
    }

    public void increment ()
    {
        m_counter.incrementAndGet ();
    }

    @Override
    public String toString ()
    {
        return String.format ("\"%s\",%s", m_name, m_counter.get ());
    }

    private void reset ()
    {
        m_counter.set (0);
    }

    public static void clearResults ()
    {
        // Concurrent map: no need for synchronized block
        for (final Counter ts : m_instances.values ())
        {
            ts.reset ();
        }
    }

    public static String formatResults (final List<Counter> results)
    {
        return HcUtil.formatObjects ("Counter,Count", results);
    }

    public static Counter getCounter (final String name)
    {
        return m_instances.computeIfAbsent (name, Counter::new);// key == name
    }

    public static String getResults (final boolean excludeUnused)
    {
        // Take a copy of the data to avoid locking.
        final List<Counter> results = getSortedResults (excludeUnused);

        return formatResults (results);
    }

    public static List<Counter> getSortedResults (final boolean excludeUnused)
    {
        final List<Counter> results = GenericFactory.newArrayList ();

        // Concurrent map: no need for synchronized block
        for (final Counter ts : m_instances.values ())
        {
            if (!excludeUnused || ts.get () > 0)
            {
                results.add (new Counter (ts));// deep copy the data
            }
        }

        // Sort the results by name.
        results.sort (comparing (Counter::getName));
        return results;
    }

    /** Factory method */
    public static Counter of (final Class<?> usingClass)
    {
        return of (usingClass.getSimpleName ());
    }

    /** Factory method */
    public static Counter of (final Class<?> usingClass, final String name)
    {
        return of (HcUtil.qualifyName (usingClass, name));
    }

    /** Factory method */
    public static Counter of (final String name)
    {
        final Counter ts = new Counter (name);

        // Concurrent map: no need for synchronized block
        m_instances.put (name, ts);

        return ts;
    }

    /** Gather stats in lock-free / immutable manner for max concurrency */
    private final AtomicLong m_counter = new AtomicLong ();

    private final String m_name;

    /** Repository of all counters */
    private static final ConcurrentMap<String, Counter> m_instances = GenericFactory.newConcurrentHashMap ();

    private static final long serialVersionUID = -2330391565153920971L;

    static
    {
        HcUtil.registerSummaryData (Counter::getResults);
    }
}
