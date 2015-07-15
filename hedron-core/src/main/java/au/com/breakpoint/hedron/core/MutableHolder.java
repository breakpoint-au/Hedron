package au.com.breakpoint.hedron.core;

/**
 * For those occasions where you need a mutable holder for an out parameter.
 *
 * @param <T>
 */
public class MutableHolder<T>
{
    public MutableHolder ()
    {
    }

    public MutableHolder (final T value)
    {
        m_value = value;
    }

    public T getValue ()
    {
        return m_value;
    }

    public void setValue (final T value)
    {
        m_value = value;
    }

    public static <T> MutableHolder<T> of ()
    {
        return new MutableHolder<> ();
    }

    public static <T> MutableHolder<T> of (final T value)
    {
        return new MutableHolder<> (value);
    }

    private T m_value;
}
