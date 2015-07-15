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

import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import au.com.breakpoint.hedron.core.value.SafeLazyValue;

/**
 * Based on JCIP Memoizer sample, except FutureTask encapsulated with LazyValue. No
 * locking on the map collection is required.
 *
 * @param <A>
 *            argument type for indexing the cache
 * @param <V>
 *            value type stored in the cache
 */
public class IndexedCache<A, V>
{
    public IndexedCache (final Function<A, V> fetcher)
    {
        m_fetcher = fetcher;
    }

    public V get (final A arg)
    {
        SafeLazyValue<V> f = m_dataMap.get (arg);
        if (f == null)
        {
            f = fetchAndStore (arg);
        }

        return f.get ();
    }

    private SafeLazyValue<V> fetchAndStore (final A arg)
    {
        // Prepare the lazy fetcher to store in the map.
        final SafeLazyValue<V> vNew = new SafeLazyValue<V> ( () -> m_fetcher.apply (arg));

        SafeLazyValue<V> v = m_dataMap.putIfAbsent (arg, vNew);

        // For first time, vNew will be stored in the map. Otherwise vNew is discarded.
        if (v == null)
        {
            v = vNew;
        }

        return v;
    }

    public static <A, V> IndexedCache<A, V> of (final Function<A, V> f)
    {
        return new IndexedCache<A, V> (f);
    }

    private final ConcurrentMap<A, SafeLazyValue<V>> m_dataMap = GenericFactory.newConcurrentHashMap ();

    /** Function that retrieves values for the specified index */
    private final Function<A, V> m_fetcher;
}
