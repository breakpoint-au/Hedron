package au.com.breakpoint.hedron.core;

import static org.junit.Assert.assertTrue;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.junit.Test;
import au.com.breakpoint.hedron.core.LazyResourceScope;

public class LazyResourceScopeTest
{
    @Test
    public void test_closeException ()
    {
        m_closeCalled = false;

        try
        {
            try (final LazyResourceScope<String> rs = LazyResourceScope.of (m_exceptionalOpener, m_indicateClosed))
            {
                m_closeCalled = false;
            }
        }
        catch (final Throwable e)
        {
            // Swallow.
        }

        assertTrue (!m_closeCalled);
    }

    @Test
    public void test_closeNotOpened ()
    {
        m_closeCalled = false;

        final Supplier<String> opener = () -> "item";
        try (final LazyResourceScope<String> rs = LazyResourceScope.of (opener, m_indicateClosed))
        {
        }

        assertTrue (!m_closeCalled);// hasn't been opened so not called
    }

    @Test
    public void test_closeOpened ()
    {
        m_closeCalled = false;

        final Supplier<String> opener = () -> "item";
        try (final LazyResourceScope<String> rs = LazyResourceScope.of (opener, m_indicateClosed))
        {
            final String val = rs.get ();// calls opener here
            System.out.println (val);
        }

        assertTrue (m_closeCalled);
    }

    private boolean m_closeCalled;

    private final Supplier<String> m_exceptionalOpener = () ->
    {
        throw new RuntimeException ();
    };

    private final Consumer<String> m_indicateClosed = s ->
    {
        m_closeCalled = true;
    };
}
