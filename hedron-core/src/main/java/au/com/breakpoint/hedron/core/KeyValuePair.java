package au.com.breakpoint.hedron.core;

import java.io.Serializable;
import java.util.Objects;

/** Immutable */
final public class KeyValuePair<K, V> implements IIdentifiable<K>, Serializable
{
    public KeyValuePair (final K key, final V value)
    {
        m_key = key;
        m_value = value;
    }

    @Override
    public boolean equals (final Object o)
    {
        boolean isEqual = false;

        if (this == o)
        {
            isEqual = true;
        }
        else if (o != null && getClass () == o.getClass ())
        {
            final KeyValuePair<K, V> eRhs = HcUtil.uncheckedCast (o);

            // Key or value may be array so use deep version.
            isEqual = Objects.deepEquals (m_key, eRhs.m_key) && Objects.deepEquals (m_value, eRhs.m_value);
        }

        return isEqual;
    }

    public K getKey ()
    {
        return m_key;
    }

    @Override
    public K getPrimaryKey ()
    {
        return m_key;
    }

    public V getValue ()
    {
        return m_value;
    }

    @Override
    public int hashCode ()
    {
        // Key or value may be array so use deep version.
        return HcUtil.deepHashCode (m_key, m_value);
    }

    @Override
    public String toString ()
    {
        // Key or value may be array so use deep version.
        return HcUtil.deepToString (m_key, m_value);
    }

    public static <K, V> KeyValuePair<K, V> of (final K key, final V value)
    {
        return new KeyValuePair<> (key, value);
    }

    private final K m_key;

    private final V m_value;

    private static final long serialVersionUID = 6330769848591981480L;
}
