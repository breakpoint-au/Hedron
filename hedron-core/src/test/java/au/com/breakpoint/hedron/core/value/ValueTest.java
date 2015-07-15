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
package au.com.breakpoint.hedron.core.value;

import static org.junit.Assert.assertEquals;
import java.util.function.Supplier;
import org.junit.Test;
import au.com.breakpoint.hedron.core.log.Logging;
import au.com.breakpoint.hedron.core.value.ComputedValue;
import au.com.breakpoint.hedron.core.value.EagerValue;
import au.com.breakpoint.hedron.core.value.FormattedStringValue;
import au.com.breakpoint.hedron.core.value.HeldValue;
import au.com.breakpoint.hedron.core.value.IValue;
import au.com.breakpoint.hedron.core.value.IndirectValue;
import au.com.breakpoint.hedron.core.value.SafeLazyValue;

public class ValueTest
{
    @Test
    public void testGetLazyValue ()
    {
        m_count = 0;

        final IValue<String> v = SafeLazyValue.of ( () -> "ab" + "cd" + m_count++);

        // Prove ok to call run multiple times.
        for (int i = 0; i < 100; ++i)
        {
            assertEquals ("abcd0", v.get ());
        }
    }

    @Test
    public void testValue ()
    {
        final IValue<String> v1 = new ComputedValue<String> (new StringEvaluator (12));
        final IValue<String> v2 = new HeldValue<String> ("CD");
        final StringEvaluator evaluator56 = new StringEvaluator (56);
        final SafeLazyValue<String> lazy56 = new SafeLazyValue<String> (evaluator56);
        final IValue<String> v3 = new ComputedValue<String> (new IndirectValue<String> (lazy56));// evaluator56 not evaluated here
        final IValue<String> v4 = new ComputedValue<String> (new IndirectValue<String> (v1));
        final IValue<String> v5 = new EagerValue<String> (evaluator56);// evaluator56 evaluated here

        Logging.logDebug ("About to get...");
        final String s1 = v1.get ();
        final String s2 = v2.get ();
        String s3 = v3.get ();
        s3 = v3.get ();
        final String s4 = v4.get ();
        final String s5 = v5.get ();

        final String s = s1 + s2 + s3 + s4 + s5;
        assertEquals ("12CD561256", s);
    }

    @Test
    public void testValueStringFormatter ()
    {
        final Object i = new Object ()
        {
            @Override
            public String toString ()
            {
                return Integer.toString (++m_i);
            }

            int m_i;
        };
        final IValue<String> v = new FormattedStringValue ("%s", i);
        assertEquals ("1", v.get ());
        assertEquals ("2", i.toString ());// show value bumps up but

        assertEquals ("1", v.get ());// not reevaluated
        assertEquals ("1", v.get ());// not reevaluated
        assertEquals ("1", v.get ());// not reevaluated
        assertEquals ("1", v.get ());// not reevaluated
    }

    @Test
    public void testValueToString ()
    {
        final SafeLazyValue<String> v = new SafeLazyValue<String> (new StringEvaluator (56));
        final String s = String.format ("value:%s", v);
        assertEquals ("value:56", s);
    }

    private static class StringEvaluator implements Supplier<String>
    {
        public StringEvaluator (final int arg)
        {
            m_arg = arg;
        }

        @Override
        public String get ()
        {
            Logging.logDebug ("StringEvaluator (%s)", m_arg);
            return Integer.toString (m_arg);
        }

        private final int m_arg;
    }

    private int m_count;
}
