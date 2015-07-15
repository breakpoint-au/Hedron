package au.com.breakpoint.hedron.core.concurrent;

import java.util.concurrent.Semaphore;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.concurrent.Concurrency;
import au.com.breakpoint.hedron.core.context.ExecutionScope;
import au.com.breakpoint.hedron.core.context.IScope;
import au.com.breakpoint.hedron.core.context.ThreadContext;

public class ConcurrencyTest
{
    @Test
    public void testExecuteRestricted ()
    {
        if (m_performTest)
        {
            for (int limit = 1; limit < 10; limit += 2)
            {
                System.out.printf ("[%s]%n", limit);
                testSemaphoreRestriction (limit, false);
            }

            testSemaphoreRestriction (10, false);
        }
    }

    private void testSemaphoreRestriction (final int limit, final boolean simulateException)
    {
        final Semaphore semaphore = new Semaphore (limit);
        final int nrTasks = limit * 3;

        HcUtil.executeManyConcurrently ( () ->
        {
            Concurrency.executeRestricted (semaphore, () ->
            {
                try (final IScope scope = new ExecutionScope ())
                {
                    final String s = ThreadContext.getContextId ();
                    System.out.printf ("+%s%n", s);
                    HcUtil.pause (200);
                    System.out.printf ("-%s%n", s);

                    if (simulateException)
                    {
                        throw new RuntimeException ();
                    }
                }

                return null;
            });
        } , nrTasks, nrTasks);
    }

    @BeforeClass
    public static void setUpBeforeClass () throws Exception
    {
    }

    @AfterClass
    public static void tearDownAfterClass () throws Exception
    {
    }

    private static final boolean m_performTest = false;
}
