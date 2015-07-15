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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class ForwardingMap<K, V> implements Map<K, V>
{
    public ForwardingMap (final Map<K, V> map)
    {
        m_map = map;
    }

    @Override
    public void clear ()
    {
        m_map.clear ();
    }

    @Override
    public boolean containsKey (final Object key)
    {
        return m_map.containsKey (key);
    }

    @Override
    public boolean containsValue (final Object value)
    {
        return m_map.containsValue (value);
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet ()
    {
        return m_map.entrySet ();
    }

    @Override
    public boolean equals (final Object obj)
    {
        return m_map.equals (obj);
    }

    @Override
    public V get (final Object key)
    {
        return m_map.get (key);
    }

    @Override
    public int hashCode ()
    {
        return m_map.hashCode ();
    }

    @Override
    public boolean isEmpty ()
    {
        return m_map.isEmpty ();
    }

    @Override
    public Set<K> keySet ()
    {
        return m_map.keySet ();
    }

    @Override
    public V put (final K key, final V value)
    {
        return m_map.put (key, value);
    }

    @Override
    public void putAll (final Map<? extends K, ? extends V> m)
    {
        m_map.putAll (m);
    }

    @Override
    public V remove (final Object key)
    {
        return m_map.remove (key);
    }

    @Override
    public int size ()
    {
        return m_map.size ();
    }

    @Override
    public String toString ()
    {
        return m_map.toString ();
    }

    @Override
    public Collection<V> values ()
    {
        return m_map.values ();
    }

    protected final Map<K, V> m_map;
}
