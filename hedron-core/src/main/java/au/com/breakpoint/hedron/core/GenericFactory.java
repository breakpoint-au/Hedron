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

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import au.com.breakpoint.hedron.core.Tuple.E2;

/**
 * Recommendation taken from Effective Java by Joshua Bloch.
 */
public class GenericFactory
{
    private static class CaseInsensitiveHashMap<V> extends HashMap<String, V>
    {
        @Override
        public V get (final Object key)
        {
            return super.get (((String) key).toLowerCase ());
        }

        @Override
        public V put (final String key, final V value)
        {
            return super.put (key.toLowerCase (), value);
        }

        private static final long serialVersionUID = 1432091148451327375L;
    }

    public static <E> ArrayBlockingQueue<E> newArrayBlockingQueue (final int capacity)
    {
        return new ArrayBlockingQueue<E> (capacity);
    }

    public static <E> ArrayList<E> newArrayList ()
    {
        return new ArrayList<E> ();
    }

    public static <E> ArrayList<E> newArrayList (final Collection<E> values)
    {
        return new ArrayList<E> (values);
    }

    @SafeVarargs
    public static <E> ArrayList<E> newArrayList (final E... values)
    {
        final ArrayList<E> c = newArrayList ();
        for (final E e : values)
        {
            c.add (e);
        }

        return c;
    }

    /**
     * Transfers an array or vararg list of (untyped) Objects to a typesafe List,
     * downcasting each element.
     *
     * @param <T>
     * @param values
     * @return
     */
    public static <T> ArrayList<T> newArrayListDowncast (final Object... values)
    {
        final ArrayList<T> c = GenericFactory.newArrayList ();

        for (final Object e : values)
        {
            c.add (HcUtil.<T> uncheckedCast (e));
        }

        return c;
    }

    public static <V> HashMap<String, V> newCaseInsensitiveHashMap ()
    {
        return new CaseInsensitiveHashMap<V> ();
    }

    @SafeVarargs
    public static <V> HashMap<String, V> newCaseInsensitiveHashMap (final E2<String, V>... values)
    {
        final HashMap<String, V> c = newCaseInsensitiveHashMap ();
        for (final E2<String, V> e : values)
        {
            c.put (e.getE0 (), e.getE1 ());
        }

        return c;
    }

    public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap ()
    {
        return new ConcurrentHashMap<K, V> ();
    }

    @SafeVarargs
    public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap (final E2<K, V>... values)
    {
        final ConcurrentHashMap<K, V> c = newConcurrentHashMap ();
        for (final E2<K, V> e : values)
        {
            c.put (e.getE0 (), e.getE1 ());
        }

        return c;
    }

    public static <T> Deque<T> newConcurrentLinkedDeque ()
    {
        return new ConcurrentLinkedDeque<T> ();
    }

    public static <E> ConcurrentLinkedQueue<E> newConcurrentLinkedQueue ()
    {
        return new ConcurrentLinkedQueue<E> ();
    }

    public static <E> CopyOnWriteArrayList<E> newCopyOnWriteArrayList ()
    {
        return new CopyOnWriteArrayList<E> ();
    }

    @SafeVarargs
    public static <E> CopyOnWriteArrayList<E> newCopyOnWriteArrayList (final E... values)
    {
        final CopyOnWriteArrayList<E> c = newCopyOnWriteArrayList ();
        for (final E e : values)
        {
            c.add (e);
        }

        return c;
    }

    public static <E extends Delayed> DelayQueue<E> newDelayQueue ()
    {
        return new DelayQueue<E> ();
    }

    public static <K extends Enum<K>, V> EnumMap<K, V> newEnumMap (final Class<K> keyType)
    {
        return new EnumMap<K, V> (keyType);
    }

    @SafeVarargs
    public static <K extends Enum<K>, V> EnumMap<K, V> newEnumMap (final Class<K> keyType, final E2<K, V>... values)
    {
        final EnumMap<K, V> c = newEnumMap (keyType);
        for (final E2<K, V> e : values)
        {
            c.put (e.getE0 (), e.getE1 ());
        }

        return c;
    }

    public static <K, V> HashMap<K, V> newHashMap ()
    {
        return new HashMap<K, V> ();
    }

    @SafeVarargs
    public static <K, V> HashMap<K, V> newHashMap (final E2<K, V>... values)
    {
        final HashMap<K, V> c = newHashMap ();
        for (final E2<K, V> e : values)
        {
            c.put (e.getE0 (), e.getE1 ());
        }

        return c;
    }

    public static <E> HashSet<E> newHashSet ()
    {
        return new HashSet<E> ();
    }

    public static <E> HashSet<E> newHashSet (final Collection<E> values)
    {
        return new HashSet<E> (values);
    }

    @SafeVarargs
    public static <E> HashSet<E> newHashSet (final E... values)
    {
        final HashSet<E> c = newHashSet ();
        for (final E e : values)
        {
            c.add (e);
        }

        return c;
    }

    public static <K, V> IdentityHashMap<K, V> newIdentityHashMap ()
    {
        return new IdentityHashMap<K, V> ();
    }

    @SafeVarargs
    public static <K, V> IdentityHashMap<K, V> newIdentityHashMap (final E2<K, V>... values)
    {
        final IdentityHashMap<K, V> c = newIdentityHashMap ();
        for (final E2<K, V> e : values)
        {
            c.put (e.getE0 (), e.getE1 ());
        }

        return c;
    }

    public static <E> LinkedBlockingQueue<E> newLinkedBlockingQueue ()
    {
        return new LinkedBlockingQueue<E> ();
    }

    public static <E> LinkedHashSet<E> newLinkedHashSet ()
    {
        return new LinkedHashSet<E> ();
    }

    @SafeVarargs
    public static <E> LinkedHashSet<E> newLinkedHashSet (final E... values)
    {
        final LinkedHashSet<E> c = newLinkedHashSet ();
        for (final E e : values)
        {
            c.add (e);
        }

        return c;
    }

    public static <E> LinkedList<E> newLinkedList ()
    {
        return new LinkedList<E> ();
    }

    @SafeVarargs
    public static <E> LinkedList<E> newLinkedList (final E... values)
    {
        final LinkedList<E> c = newLinkedList ();
        for (final E e : values)
        {
            c.add (e);
        }

        return c;
    }

    public static <E> PriorityBlockingQueue<E> newPriorityBlockingQueue ()
    {
        return new PriorityBlockingQueue<E> ();
    }

    public static <T> SoftReference<T> newSoftReference (final T t)
    {
        return new SoftReference<T> (t);
    }

    public static <E> SynchronousQueue<E> newSynchronousQueue ()
    {
        return new SynchronousQueue<E> ();
    }

    public static <K, V> TreeMap<K, V> newTreeMap ()
    {
        return new TreeMap<K, V> ();
    }

    @SafeVarargs
    public static <K, V> TreeMap<K, V> newTreeMap (final E2<K, V>... values)
    {
        final TreeMap<K, V> c = newTreeMap ();
        for (final E2<K, V> e : values)
        {
            c.put (e.getE0 (), e.getE1 ());
        }

        return c;
    }

    public static <E> TreeSet<E> newTreeSet ()
    {
        return new TreeSet<E> ();
    }

    public static <E> TreeSet<E> newTreeSet (final Collection<E> values)
    {
        return new TreeSet<E> (values);
    }

    @SafeVarargs
    public static <E> TreeSet<E> newTreeSet (final E... values)
    {
        final TreeSet<E> c = newTreeSet ();
        for (final E e : values)
        {
            c.add (e);
        }

        return c;
    }

    public static <K, V> WeakHashMap<K, V> newWeakHashMap ()
    {
        return new WeakHashMap<K, V> ();
    }

    @SafeVarargs
    public static <K, V> WeakHashMap<K, V> newWeakHashMap (final E2<K, V>... values)
    {
        final WeakHashMap<K, V> c = newWeakHashMap ();
        for (final E2<K, V> e : values)
        {
            c.put (e.getE0 (), e.getE1 ());
        }

        return c;
    }

    public static <T> WeakReference<T> newWeakReference (final T t)
    {
        return new WeakReference<T> (t);
    }
}
