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

import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import au.com.breakpoint.hedron.core.value.TimeLimitedLazyValue;

/**
 * Based on JCIP Memoizer sample, except FutureTask encapsulated with
 * TimeLimitedLazyValue. Threadsafe without locking on the map collection.
 *
 * @param <A>
 *            argument type for indexing the cache
 * @param <V>
 *            value type stored in the cache
 */
public class TimeLimitedIndexedCache<A, V>
{
    public TimeLimitedIndexedCache (final Class<?> usingClass, final String name, final Function<A, V> fetcher,
        final long lifetimeMsec)
    {
        m_usingClass = usingClass;
        m_name = name;
        m_fetcher = fetcher;
        m_lifetimeMsec = lifetimeMsec;
    }

    public V getValue (final A arg)
    {
        TimeLimitedLazyValue<V> f = m_dataMap.get (arg);
        if (f == null)
        {
            f = fetchAndStore (arg);
        }

        return f.get ();
    }

    private TimeLimitedLazyValue<V> fetchAndStore (final A arg)
    {
        final TimeLimitedLazyValue<V> vNew =
            TimeLimitedLazyValue.of (m_usingClass, m_name, () -> m_fetcher.apply (arg), m_lifetimeMsec);

        TimeLimitedLazyValue<V> v = m_dataMap.putIfAbsent (arg, vNew);

        // For first time, vNew will now be stored in the map. Otherwise vNew is discarded.
        if (v == null)
        {
            v = vNew;
        }

        return v;
    }

    public static <A, V> TimeLimitedIndexedCache<A, V> of (final Class<?> usingClass, final String name,
        final Function<A, V> f, final long lifetimeMsec)
    {
        return new TimeLimitedIndexedCache<A, V> (usingClass, name, f, lifetimeMsec);
    }

    private final ConcurrentMap<A, TimeLimitedLazyValue<V>> m_dataMap = GenericFactory.newConcurrentHashMap ();

    /** Function that retrieves values for the specified index */
    private final Function<A, V> m_fetcher;

    private final long m_lifetimeMsec;

    private final String m_name;

    /** Used for instrumentation counters etc */
    private final Class<?> m_usingClass;
}
