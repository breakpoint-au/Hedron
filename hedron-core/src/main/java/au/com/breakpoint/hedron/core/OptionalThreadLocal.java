package au.com.breakpoint.hedron.core;

import java.util.Objects;
import java.util.function.Supplier;

/** ThreadLocal implementation that can be tested to see if it has been instantiated */
public class OptionalThreadLocal<T> extends ThreadLocal<T>
{
    @Override
    public T get ()
    {
        m_isInitalised = true;
        return super.get ();
    }

    public boolean isInitalised ()
    {
        return m_isInitalised;
    }

    @Override
    public void remove ()
    {
        super.remove ();
        m_isInitalised = false;
    }

    /**
     * An extension of ThreadLocal that obtains its initial value from the specified
     * {@code Supplier}.
     */
    private static final class SuppliedOptionalThreadLocal<T> extends OptionalThreadLocal<T>
    {
        private SuppliedOptionalThreadLocal (final Supplier<? extends T> supplier)
        {
            this.m_supplier = Objects.requireNonNull (supplier);
        }

        @Override
        protected T initialValue ()
        {
            return m_supplier.get ();
        }

        private final Supplier<? extends T> m_supplier;
    }

    /**
     * Creates an optional thread local variable. The initial value of the variable is
     * determined by invoking the {@code get} method on the {@code Supplier}.
     *
     * @param <S>
     *            the type of the thread local's value
     * @param supplier
     *            the supplier to be used to determine the initial value
     * @return a new thread local variable
     * @throws NullPointerException
     *             if the specified supplier is null
     */
    public static <S> OptionalThreadLocal<S> withInitial (final Supplier<? extends S> supplier)
    {
        return new SuppliedOptionalThreadLocal<> (supplier);
    }

    private boolean m_isInitalised;
}
