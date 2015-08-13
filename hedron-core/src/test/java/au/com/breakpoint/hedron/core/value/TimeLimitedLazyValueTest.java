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
package au.com.breakpoint.hedron.core.value;

import static org.junit.Assert.assertEquals;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;
import org.junit.Test;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.value.TimeLimitedLazyValue;

public class TimeLimitedLazyValueTest
{
    @Test
    public void testGetValueExpiry ()
    {
        if (m_performTest)
        {
            final int threadCount = 300;
            final int expectedCalcs = 200;
            final int lifetimeMsec = 20;
            final int shortfall = lifetimeMsec / 4;
            final int msecDuration = lifetimeMsec * expectedCalcs - shortfall;

            // Lazy evaluate for the cache.
            final Supplier<String> evaluator = () ->
            {
                m_countCalcs.increment ();
                return m_countCalcs.toString ();
            };
            final TimeLimitedLazyValue<String> lv =
                TimeLimitedLazyValue.of (TimeLimitedLazyValueTest.class, "cacheName", evaluator, lifetimeMsec);

            HcUtil.executeManyConcurrentlyFor ( () ->
            {
                lv.get ();
                m_countGets.increment ();
            } , threadCount, msecDuration, 0);

            //            System.out.printf ("%s calcs out of %s gets (lifetimeMsec:%s msecDuration:%s)%n", m_countCalcs.get (),
            //                m_countGets.get (), lifetimeMsec, msecDuration);

            assertEquals (expectedCalcs, m_countCalcs.longValue ());

            System.out.println (HcUtil.getSummaryData (true));
        }
    }

    private final LongAdder m_countCalcs = new LongAdder ();

    private final LongAdder m_countGets = new LongAdder ();

    private static final boolean m_performTest = false;// enable this on demand... test can be unreliable because of timing dependencies... but useful for dev testing
}
