package au.com.breakpoint.hedron.core;

import static org.junit.Assert.assertTrue;
import java.util.function.Consumer;
import org.junit.Test;
import au.com.breakpoint.hedron.core.ResourceScope;

public class ResourceScopeTest
{
    @Test
    public void test_close ()
    {
        try (final ResourceScope<String> rs = ResourceScope.of ("item", m_indicateClosed))
        {
            m_closeCalled = false;
        }

        assertTrue (m_closeCalled);
    }

    private boolean m_closeCalled;

    private final Consumer<String> m_indicateClosed = s ->
    {
        m_closeCalled = true;
    };
}
