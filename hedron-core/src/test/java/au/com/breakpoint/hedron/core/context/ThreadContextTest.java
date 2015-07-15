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
package au.com.breakpoint.hedron.core.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import java.util.Deque;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import au.com.breakpoint.hedron.core.ICloseable;
import au.com.breakpoint.hedron.core.context.AssertException;
import au.com.breakpoint.hedron.core.context.ContextData;
import au.com.breakpoint.hedron.core.context.FaultException;
import au.com.breakpoint.hedron.core.context.LoggingSilenceScope;
import au.com.breakpoint.hedron.core.context.NestedScopeData;
import au.com.breakpoint.hedron.core.context.OpResult;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.core.context.ThreadContextException;

public class ThreadContextTest
{
    @Test
    public void test_formThreadName ()
    {
        final ContextData c = new ContextData ();
        c.m_originalThreadName = "orig";

        final Deque<NestedScopeData> scopeNesting = c.m_scopeNesting;

        scopeNesting.push (new NestedScopeData (null, null, "name0", null));
        assertEquals ("orig:name0{cid0}", ThreadContext.formThreadName (c, "{cid0}"));

        scopeNesting.push (new NestedScopeData (null, null, null, null));
        assertEquals ("orig:name0{cid0:1}", ThreadContext.formThreadName (c, "{cid0:1}"));

        scopeNesting.push (new NestedScopeData (null, null, "name2", null));
        assertEquals ("orig:name0:name2{cid0:1:2}", ThreadContext.formThreadName (c, "{cid0:1:2}"));

        scopeNesting.push (new NestedScopeData (null, null, null, null));
        assertEquals ("orig:name0:name2{cid0:1:2:3}", ThreadContext.formThreadName (c, "{cid0:1:2:3}"));

        scopeNesting.push (new NestedScopeData (null, null, "name4", null));
        assertEquals ("orig:name0:name2:name4{cid0:1:2:3:4}", ThreadContext.formThreadName (c, "{cid0:1:2:3:4}"));
    }

    @Test
    public void test_getReportableException ()
    {
        final Throwable t0 = new Throwable ("t0");
        assertSame (t0, ThreadContext.getReportableException (t0));

        final Error err0 = new Error ("err0");
        assertSame (err0, ThreadContext.getReportableException (err0));

        final Exception ex0 = new Exception ("ex0");
        assertSame (ex0, ThreadContext.getReportableException (ex0));

        final RuntimeException r0 = new RuntimeException ("r0");
        assertSame (r0, ThreadContext.getReportableException (r0));

        final AssertException a0 = new AssertException (OpResult.Severity.Error, "a0", true);
        assertSame (a0, ThreadContext.getReportableException (a0));

        final FaultException f0 = new FaultException ("f0", true);
        assertSame (f0, ThreadContext.getReportableException (f0));

        final ExecutionException ee_t0 = new ExecutionException (t0);
        assertSame (t0, ThreadContext.getReportableException (ee_t0));

        final ExecutionException ee_err0 = new ExecutionException (err0);
        assertSame (err0, ThreadContext.getReportableException (ee_err0));

        final ExecutionException ee_ex0 = new ExecutionException (ex0);
        assertSame (ex0, ThreadContext.getReportableException (ee_ex0));

        final ExecutionException ee_r0 = new ExecutionException (r0);
        assertSame (r0, ThreadContext.getReportableException (ee_r0));

        final ExecutionException ee_a0 = new ExecutionException (a0);
        assertSame (a0, ThreadContext.getReportableException (ee_a0));

        final ExecutionException ee_f0 = new ExecutionException (f0);
        assertSame (f0, ThreadContext.getReportableException (ee_f0));

        final CompletionException ce_t0 = new CompletionException (t0);
        assertSame (t0, ThreadContext.getReportableException (ce_t0));

        final CompletionException ce_err0 = new CompletionException (err0);
        assertSame (err0, ThreadContext.getReportableException (ce_err0));

        final CompletionException ce_ex0 = new CompletionException (ex0);
        assertSame (ex0, ThreadContext.getReportableException (ce_ex0));

        final CompletionException ce_r0 = new CompletionException (r0);
        assertSame (r0, ThreadContext.getReportableException (ce_r0));

        final CompletionException ce_a0 = new CompletionException (a0);
        assertSame (a0, ThreadContext.getReportableException (ce_a0));

        final CompletionException ce_f0 = new CompletionException (f0);
        assertSame (f0, ThreadContext.getReportableException (ce_f0));

        try (ICloseable scope = new LoggingSilenceScope ())
        {
            // Check wrapping simple throwable types in a FaultException.
            final ThreadContextException w_t0 = ThreadContext.wrapRootThrowable (t0);
            assertSame (w_t0, ThreadContext.getReportableException (w_t0));

            final ThreadContextException w_err0 = ThreadContext.wrapRootThrowable (err0);
            assertSame (w_err0, ThreadContext.getReportableException (w_err0));

            final ThreadContextException w_ex0 = ThreadContext.wrapRootThrowable (ex0);
            assertSame (w_ex0, ThreadContext.getReportableException (w_ex0));

            final ThreadContextException w_r0 = ThreadContext.wrapRootThrowable (r0);
            assertSame (w_r0, ThreadContext.getReportableException (w_r0));

            final ThreadContextException w_a0 = ThreadContext.wrapRootThrowable (a0);
            assertSame (w_a0, ThreadContext.getReportableException (w_a0));

            // Check an FaultException doesn't get re-wrapped.
            final ThreadContextException w_f0 = ThreadContext.wrapRootThrowable (f0);
            assertSame (w_f0, ThreadContext.getReportableException (w_f0));

            // Check that ExecutionException is unwrapped and the underlying exception is wrapped.
            final ThreadContextException w_ee_t0 = ThreadContext.wrapRootThrowable (ee_t0);
            assertSame (w_ee_t0, ThreadContext.getReportableException (w_ee_t0));

            final ThreadContextException w_ee_err0 = ThreadContext.wrapRootThrowable (ee_err0);
            assertSame (w_ee_err0, ThreadContext.getReportableException (w_ee_err0));

            final ThreadContextException w_ee_ex0 = ThreadContext.wrapRootThrowable (ee_ex0);
            assertSame (w_ee_ex0, ThreadContext.getReportableException (w_ee_ex0));

            final ThreadContextException w_ee_r0 = ThreadContext.wrapRootThrowable (ee_r0);
            assertSame (w_ee_r0, ThreadContext.getReportableException (w_ee_r0));

            final ThreadContextException w_ee_a0 = ThreadContext.wrapRootThrowable (ee_a0);
            assertSame (w_ee_a0, ThreadContext.getReportableException (w_ee_a0));

            final ThreadContextException w_ee_f0 = ThreadContext.wrapRootThrowable (ee_f0);
            assertSame (w_ee_f0, ThreadContext.getReportableException (w_ee_f0));

            // Check that CompletionException is unwrapped and the underlying exception is wrapped.
            final ThreadContextException w_ce_t0 = ThreadContext.wrapRootThrowable (ce_t0);
            assertSame (w_ce_t0, ThreadContext.getReportableException (w_ce_t0));

            final ThreadContextException w_ce_err0 = ThreadContext.wrapRootThrowable (ce_err0);
            assertSame (w_ce_err0, ThreadContext.getReportableException (w_ce_err0));

            final ThreadContextException w_ce_ex0 = ThreadContext.wrapRootThrowable (ce_ex0);
            assertSame (w_ce_ex0, ThreadContext.getReportableException (w_ce_ex0));

            final ThreadContextException w_ce_r0 = ThreadContext.wrapRootThrowable (ce_r0);
            assertSame (w_ce_r0, ThreadContext.getReportableException (w_ce_r0));

            final ThreadContextException w_ce_a0 = ThreadContext.wrapRootThrowable (ce_a0);
            assertSame (w_ce_a0, ThreadContext.getReportableException (w_ce_a0));

            final ThreadContextException w_ce_f0 = ThreadContext.wrapRootThrowable (ce_f0);
            assertSame (w_ce_f0, ThreadContext.getReportableException (w_ce_f0));

            // Check that multi-level of ExecutionException is unwrapped and the underlying exception is wrapped.
            if (true)
            {
                final ExecutionException ee_w_t0 = new ExecutionException (w_t0);
                assertSame (w_t0, ThreadContext.getReportableException (ee_w_t0));

                final ExecutionException ee_w_err0 = new ExecutionException (w_err0);
                assertSame (w_err0, ThreadContext.getReportableException (ee_w_err0));

                final ExecutionException ee_w_ex0 = new ExecutionException (w_ex0);
                assertSame (w_ex0, ThreadContext.getReportableException (ee_w_ex0));

                final ExecutionException ee_w_r0 = new ExecutionException (w_r0);
                assertSame (w_r0, ThreadContext.getReportableException (ee_w_r0));

                final ExecutionException ee_w_a0 = new ExecutionException (w_a0);
                assertSame (w_a0, ThreadContext.getReportableException (ee_w_a0));

                final ExecutionException ee_w_f0 = new ExecutionException (w_f0);
                assertSame (w_f0, ThreadContext.getReportableException (ee_w_f0));

                final ThreadContextException w_ee_w_t0 = ThreadContext.wrapRootThrowable (ee_w_t0);
                assertSame (w_ee_w_t0, ThreadContext.getReportableException (w_ee_w_t0));

                final ThreadContextException w_ee_w_err0 = ThreadContext.wrapRootThrowable (ee_w_err0);
                assertSame (w_ee_w_err0, ThreadContext.getReportableException (w_ee_w_err0));

                final ThreadContextException w_ee_w_ex0 = ThreadContext.wrapRootThrowable (ee_w_ex0);
                assertSame (w_ee_w_ex0, ThreadContext.getReportableException (w_ee_w_ex0));

                final ThreadContextException w_ee_w_r0 = ThreadContext.wrapRootThrowable (ee_w_r0);
                assertSame (w_ee_w_r0, ThreadContext.getReportableException (w_ee_w_r0));

                final ThreadContextException w_ee_w_a0 = ThreadContext.wrapRootThrowable (ee_w_a0);
                assertSame (w_ee_w_a0, ThreadContext.getReportableException (w_ee_w_a0));

                final ThreadContextException w_ee_w_f0 = ThreadContext.wrapRootThrowable (ee_w_f0);
                assertSame (w_ee_w_f0, ThreadContext.getReportableException (w_ee_w_f0));

            }

            // Check that multi-level of CompletionException is unwrapped and the underlying exception is wrapped.
            if (true)
            {
                final CompletionException ce_w_t0 = new CompletionException (w_t0);
                assertSame (w_t0, ThreadContext.getReportableException (ce_w_t0));

                final CompletionException ce_w_err0 = new CompletionException (w_err0);
                assertSame (w_err0, ThreadContext.getReportableException (ce_w_err0));

                final CompletionException ce_w_ex0 = new CompletionException (w_ex0);
                assertSame (w_ex0, ThreadContext.getReportableException (ce_w_ex0));

                final CompletionException ce_w_r0 = new CompletionException (w_r0);
                assertSame (w_r0, ThreadContext.getReportableException (ce_w_r0));

                final CompletionException ce_w_a0 = new CompletionException (w_a0);
                assertSame (w_a0, ThreadContext.getReportableException (ce_w_a0));

                final CompletionException ce_w_f0 = new CompletionException (w_f0);
                assertSame (w_f0, ThreadContext.getReportableException (ce_w_f0));

                final ThreadContextException w_ce_w_t0 = ThreadContext.wrapRootThrowable (ce_w_t0);
                assertSame (w_ce_w_t0, ThreadContext.getReportableException (w_ce_w_t0));

                final ThreadContextException w_ce_w_err0 = ThreadContext.wrapRootThrowable (ce_w_err0);
                assertSame (w_ce_w_err0, ThreadContext.getReportableException (w_ce_w_err0));

                final ThreadContextException w_ce_w_ex0 = ThreadContext.wrapRootThrowable (ce_w_ex0);
                assertSame (w_ce_w_ex0, ThreadContext.getReportableException (w_ce_w_ex0));

                final ThreadContextException w_ce_w_r0 = ThreadContext.wrapRootThrowable (ce_w_r0);
                assertSame (w_ce_w_r0, ThreadContext.getReportableException (w_ce_w_r0));

                final ThreadContextException w_ce_w_a0 = ThreadContext.wrapRootThrowable (ce_w_a0);
                assertSame (w_ce_w_a0, ThreadContext.getReportableException (w_ce_w_a0));

                final ThreadContextException w_ce_w_f0 = ThreadContext.wrapRootThrowable (ce_w_f0);
                assertSame (w_ce_w_f0, ThreadContext.getReportableException (w_ce_w_f0));
            }

            final ThreadContextException xxx_t0 = ThreadContext.wrapRootThrowable (wrapToBuggery (t0));
            assertSame (xxx_t0, ThreadContext.getReportableException (xxx_t0));

            final ThreadContextException w_xxx_t0 = ThreadContext.wrapRootThrowable (wrapToBuggery (w_t0));
            assertSame (w_t0, ThreadContext.getReportableException (w_xxx_t0));
        }
    }

    @Test
    public void test_wrapRootException ()
    {
        final Throwable t0 = new Throwable ("t0");
        final Error err0 = new Error ("err0");
        final Exception ex0 = new Exception ("ex0");
        final RuntimeException r0 = new RuntimeException ("r0");
        final AssertException a0 = new AssertException (OpResult.Severity.Error, "a0", true);
        final FaultException f0 = new FaultException ("f0", true);

        final ExecutionException ee_t0 = new ExecutionException (t0);
        final ExecutionException ee_err0 = new ExecutionException (err0);
        final ExecutionException ee_ex0 = new ExecutionException (ex0);
        final ExecutionException ee_r0 = new ExecutionException (r0);
        final ExecutionException ee_a0 = new ExecutionException (a0);
        final ExecutionException ee_f0 = new ExecutionException (f0);

        final CompletionException ce_t0 = new CompletionException (t0);
        final CompletionException ce_err0 = new CompletionException (err0);
        final CompletionException ce_ex0 = new CompletionException (ex0);
        final CompletionException ce_r0 = new CompletionException (r0);
        final CompletionException ce_a0 = new CompletionException (a0);
        final CompletionException ce_f0 = new CompletionException (f0);

        try (ICloseable scope = new LoggingSilenceScope ())
        {
            // Check wrapping simple throwable types in a FaultException.
            final ThreadContextException w_t0 = ThreadContext.wrapRootThrowable (t0);
            checkFaultExceptionWrapper (t0, w_t0);

            final ThreadContextException w_err0 = ThreadContext.wrapRootThrowable (err0);
            checkFaultExceptionWrapper (err0, w_err0);

            final ThreadContextException w_ex0 = ThreadContext.wrapRootThrowable (ex0);
            checkFaultExceptionWrapper (ex0, w_ex0);

            final ThreadContextException w_r0 = ThreadContext.wrapRootThrowable (r0);
            checkFaultExceptionWrapper (r0, w_r0);

            // Check an AssertException doesn't get re-wrapped.
            final ThreadContextException w_a0 = ThreadContext.wrapRootThrowable (a0);
            checkAssertExceptionWrapperSame (a0, w_a0);

            // Check an FaultException doesn't get re-wrapped.
            final ThreadContextException w_f0 = ThreadContext.wrapRootThrowable (f0);
            checkFaultExceptionWrapperSame (f0, w_f0);

            // Check that ExecutionException is unwrapped and the underlying exception is wrapped.
            final ThreadContextException w_ee_t0 = ThreadContext.wrapRootThrowable (ee_t0);
            checkFaultExceptionWrapper (t0, w_ee_t0);

            final ThreadContextException w_ee_err0 = ThreadContext.wrapRootThrowable (ee_err0);
            checkFaultExceptionWrapper (err0, w_ee_err0);

            final ThreadContextException w_ee_ex0 = ThreadContext.wrapRootThrowable (ee_ex0);
            checkFaultExceptionWrapper (ex0, w_ee_ex0);

            final ThreadContextException w_ee_r0 = ThreadContext.wrapRootThrowable (ee_r0);
            checkFaultExceptionWrapper (r0, w_ee_r0);

            final ThreadContextException w_ee_a0 = ThreadContext.wrapRootThrowable (ee_a0);
            checkAssertExceptionWrapperSame (a0, w_ee_a0);

            final ThreadContextException w_ee_f0 = ThreadContext.wrapRootThrowable (ee_f0);
            checkFaultExceptionWrapperSame (f0, w_ee_f0);

            // Check that CompletionException is unwrapped and the underlying exception is wrapped.
            final ThreadContextException w_ce_t0 = ThreadContext.wrapRootThrowable (ce_t0);
            checkFaultExceptionWrapper (t0, w_ce_t0);

            final ThreadContextException w_ce_err0 = ThreadContext.wrapRootThrowable (ce_err0);
            checkFaultExceptionWrapper (err0, w_ce_err0);

            final ThreadContextException w_ce_ex0 = ThreadContext.wrapRootThrowable (ce_ex0);
            checkFaultExceptionWrapper (ex0, w_ce_ex0);

            final ThreadContextException w_ce_r0 = ThreadContext.wrapRootThrowable (ce_r0);
            checkFaultExceptionWrapper (r0, w_ce_r0);

            final ThreadContextException w_ce_a0 = ThreadContext.wrapRootThrowable (ce_a0);
            checkAssertExceptionWrapperSame (a0, w_ce_a0);

            final ThreadContextException w_ce_f0 = ThreadContext.wrapRootThrowable (ce_f0);
            checkFaultExceptionWrapperSame (f0, w_ce_f0);

            // Check that multi-level of ExecutionException is unwrapped and the underlying exception is wrapped.
            if (true)
            {
                final ExecutionException ee_w_t0 = new ExecutionException (w_t0);
                final ExecutionException ee_w_err0 = new ExecutionException (w_err0);
                final ExecutionException ee_w_ex0 = new ExecutionException (w_ex0);
                final ExecutionException ee_w_r0 = new ExecutionException (w_r0);
                final ExecutionException ee_w_a0 = new ExecutionException (w_a0);
                final ExecutionException ee_w_f0 = new ExecutionException (w_f0);

                final ThreadContextException w_ee_w_t0 = ThreadContext.wrapRootThrowable (ee_w_t0);
                assertSame (w_t0, w_ee_w_t0);

                final ThreadContextException w_ee_w_err0 = ThreadContext.wrapRootThrowable (ee_w_err0);
                assertSame (w_err0, w_ee_w_err0);

                final ThreadContextException w_ee_w_ex0 = ThreadContext.wrapRootThrowable (ee_w_ex0);
                assertSame (w_ex0, w_ee_w_ex0);

                final ThreadContextException w_ee_w_r0 = ThreadContext.wrapRootThrowable (ee_w_r0);
                assertSame (w_r0, w_ee_w_r0);

                final ThreadContextException w_ee_w_a0 = ThreadContext.wrapRootThrowable (ee_w_a0);
                assertSame (a0, w_ee_w_a0);

                final ThreadContextException w_ee_w_f0 = ThreadContext.wrapRootThrowable (ee_w_f0);
                assertSame (f0, w_ee_w_f0);
            }

            // Check that multi-level of CompletionException is unwrapped and the underlying exception is wrapped.
            if (true)
            {
                final CompletionException ce_w_t0 = new CompletionException (w_t0);
                final CompletionException ce_w_err0 = new CompletionException (w_err0);
                final CompletionException ce_w_ex0 = new CompletionException (w_ex0);
                final CompletionException ce_w_r0 = new CompletionException (w_r0);
                final CompletionException ce_w_a0 = new CompletionException (w_a0);
                final CompletionException ce_w_f0 = new CompletionException (w_f0);

                final ThreadContextException w_ce_w_t0 = ThreadContext.wrapRootThrowable (ce_w_t0);
                assertSame (w_t0, w_ce_w_t0);

                final ThreadContextException w_ce_w_err0 = ThreadContext.wrapRootThrowable (ce_w_err0);
                assertSame (w_err0, w_ce_w_err0);

                final ThreadContextException w_ce_w_ex0 = ThreadContext.wrapRootThrowable (ce_w_ex0);
                assertSame (w_ex0, w_ce_w_ex0);

                final ThreadContextException w_ce_w_r0 = ThreadContext.wrapRootThrowable (ce_w_r0);
                assertSame (w_r0, w_ce_w_r0);

                final ThreadContextException w_ce_w_a0 = ThreadContext.wrapRootThrowable (ce_w_a0);
                assertSame (w_a0, w_ce_w_a0);

                final ThreadContextException w_ce_w_f0 = ThreadContext.wrapRootThrowable (ce_w_f0);
                assertSame (w_f0, w_ce_w_f0);
            }

            checkFaultExceptionWrapper (t0, ThreadContext.wrapRootThrowable (wrapToBuggery (t0)));
            checkFaultExceptionWrapperSame (w_t0, ThreadContext.wrapRootThrowable (wrapToBuggery (w_t0)));
        }
    }

    private void checkAssertExceptionWrapperSame (final AssertException expectedCause,
        final ThreadContextException wrapped)
    {
        assertEquals (AssertException.class, wrapped.getClass ());
        assertSame (expectedCause, wrapped);// same object instance

        // Check that re-wrapping yields the same result.
        assertSame (wrapped, ThreadContext.wrapRootThrowable (wrapped));
    }

    private void checkFaultExceptionWrapper (final Throwable expectedCause, final ThreadContextException wrapped)
    {
        assertEquals (FaultException.class, wrapped.getClass ());
        assertSame (expectedCause, wrapped.getCause ());// same object instance

        // Check that re-wrapping yields the same result.
        assertSame (wrapped, ThreadContext.wrapRootThrowable (wrapped));
    }

    private void checkFaultExceptionWrapperSame (final ThreadContextException expectedCause,
        final ThreadContextException wrapped)
    {
        assertEquals (FaultException.class, wrapped.getClass ());
        assertSame (expectedCause, wrapped);// same object instance

        // Check that re-wrapping yields the same result.
        assertSame (wrapped, ThreadContext.wrapRootThrowable (wrapped));
    }

    private ExecutionException wrapToBuggery (final Throwable cause)
    {
        // Hugo crazy.
        return new ExecutionException (new CompletionException (new CompletionException (
            new ExecutionException (new ExecutionException (new CompletionException (new CompletionException (
                new CompletionException (new ExecutionException (new CompletionException (cause))))))))));
    }
}
