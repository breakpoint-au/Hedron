//                       __________________________________
//                ______|      Copyright 2008-2015         |______
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
