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

/** Threadsafe */
public class FactoryMap<K, V> extends ForwardingMap<K, V>
{
    public FactoryMap (final IFactory<V> factory)
    {
        super (GenericFactory.<K, V> newConcurrentHashMap ());

        m_factory = factory;
    }

    @Override
    public V get (final Object key)
    {
        // Lock-free check and put.
        V e = m_map.get (key);
        if (e == null)
        {
            // Does not yet exist.
            final K k = HcUtil.uncheckedCast (key);
            final ConcurrentMap<K, V> m = HcUtil.uncheckedCast (m_map);

            final V newInstance = m_factory.newInstance ();

            // Returns the previous value associated with the specified key (ie race
            // condition occurred), or null if there was no mapping for the key.
            e = m.putIfAbsent (k, newInstance);
            if (e == null)
            {
                // No race condition: put succeeded, use new value.
                e = newInstance;
            }
        }

        return e;
    }

    /** Factory method */
    public static <K, V> FactoryMap<K, V> of (final IFactory<V> factory)
    {
        return new FactoryMap<K, V> (factory);
    }

    private final IFactory<V> m_factory;
}
