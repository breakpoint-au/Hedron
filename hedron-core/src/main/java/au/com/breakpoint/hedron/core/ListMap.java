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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A map that contains a list of values for each key. A type of multimap.
 *
 * @param <K>
 *            Key type
 * @param <V>
 *            Value type stored in the list for each key.
 */
public class ListMap<K, V>
{
    public void clear ()
    {
        m_map.clear ();
    }

    public List<V> get (final K key)
    {
        return m_map.get (key);
    }

    public Set<K> getKeys ()
    {
        return m_map.keySet ();
    }

    public Map<K, List<V>> getMap ()
    {
        return m_map;
    }

    public void put (final K key, final V value)
    {
        List<V> list = m_map.get (key);
        if (list == null)
        {
            list = GenericFactory.newArrayList ();
            m_map.put (key, list);
        }

        list.add (value);
    }

    @Override
    public String toString ()
    {
        return Arrays.toString (m_map.entrySet ().toArray ());
    }

    public static <K, V> ListMap<K, V> of ()
    {
        return new ListMap<K, V> ();
    }

    private final Map<K, List<V>> m_map = GenericFactory.newHashMap ();
}
