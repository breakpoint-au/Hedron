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
package au.com.breakpoint.hedron.core.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.Test;

public class ExecutionScopesTest
{
    @Test
    public void testExecuteFaultBarrierFaultIExecutable ()
    {
        final Optional<String> result = ExecutionScopes.executeFaultBarrier (new ExceptionalSupplier (true));
        assertTrue (!result.isPresent ()); // exception got caught ok, null returned
    }

    @Test
    public void testExecuteFaultBarrierFaultRunnable ()
    {
        final boolean ok = ExecutionScopes.executeFaultBarrier (new ExceptionalRunnable (true));
        assertTrue (!ok); // exception got caught ok
    }

    @Test
    public void testExecuteFaultBarrierIExecutable ()
    {
        final Optional<String> result = ExecutionScopes.executeFaultBarrier (new ExceptionalSupplier (false));
        assertTrue (!result.isPresent ()); // exception got caught ok, null returned
    }

    @Test
    public void testExecuteFaultBarrierNoFault ()
    {
        final Optional<String> result = ExecutionScopes.executeFaultBarrier ( () -> "success");
        assertTrue (result.isPresent ()); // no exception
        assertEquals ("success", result.get ());
    }

    @Test
    public void testExecuteFaultBarrierRemoveRunnable ()
    {
        final boolean ok = ExecutionScopes.executeFaultBarrier (new ExceptionalRunnable (false));
        assertTrue (!ok); // exception got caught ok
    }

    @Test
    public void testExecuteFaultBarrierRunnable ()
    {
        final boolean ok = ExecutionScopes.executeFaultBarrier (new ExceptionalRunnable (false));
        assertTrue (!ok); // exception got caught ok
    }

    @Test
    public void testExecuteFaultBarrierRunnableNoFault ()
    {
        final boolean ok = ExecutionScopes.executeFaultBarrier ( () ->
        {
        });
        assertTrue (ok); // no exception
    }

    @Test
    public void visualiseContextIdLevels ()
    {
        System.out.println ("--------------");

        for (int i = 0; i < 3; ++i)
        {
            try (IScope is0 = new ExecutionScope ("is0"))
            {
                System.out.printf ("is0 %s%n", ThreadContext.getContextId ());

                for (int j = 0; j < 2; ++j)
                {
                    try (IScope is1 = new ExecutionScope ("is1"))
                    {
                        System.out.printf ("is1 %s%n", ThreadContext.getContextId ());

                        for (int k = 0; k < 2; ++k)
                        {
                            try (IScope is2 = new ExecutionScope ("is2"))
                            {
                                System.out.printf ("is2 %s%n", ThreadContext.getContextId ());
                            }
                        }
                    }
                }
            }
        }
        System.out.println ("--------------");
    }

    private static class ExceptionalRunnable implements Runnable
    {
        public ExceptionalRunnable (final boolean wrapAsFault)
        {
            m_wrapAsFault = wrapAsFault;
        }

        @Override
        public void run ()
        {
            if (m_wrapAsFault)
            {
                ThreadContext.throwFault (new NullPointerException ());
            }
            else
            {
                throw new NullPointerException ();
            }
        }

        private final boolean m_wrapAsFault;
    }

    private static class ExceptionalSupplier implements Supplier<String>
    {
        public ExceptionalSupplier (final boolean wrapAsFault)
        {
            m_wrapAsFault = wrapAsFault;
        }

        @Override
        public String get ()
        {
            if (m_wrapAsFault)
            {
                ThreadContext.throwFault (new NullPointerException ());
            }
            else
            {
                throw new NullPointerException ();
            }

            return "never-gets-here";
        }

        private final boolean m_wrapAsFault;
    }
}
