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
package au.com.breakpoint.hedron.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.junit.Test;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.IndexedCache;

public class IndexedCacheTest
{
    @Test
    public void testExecute ()
    {
        final Function<Integer, String> f = a -> Integer.toString (a);
        final IndexedCache<Integer, String> m = IndexedCache.of (f);
        final String s1 = m.get (12);
        final String s2 = m.get (12);
        assertSame (s1, s2);

        final String s3 = m.get (34);
        final String s4 = m.get (34);
        assertSame (s3, s4);

        assertFalse (s1 == s3);
    }

    @Test
    public void testGetValueConcurrently ()
    {
        final int threadCount = 100;
        final int msecDuration = 200;

        final int[] countsMapEntries =
            {
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7,
                    8,
                    9,
                    10,
                    15,
                    20,
                    50,
                    100
        };
        for (final int countMapEntries : countsMapEntries)
        {
            CalculatedString.m_countCalcs.set (0);
            CalculatedString.m_countGets.set (0);

            final IndexedCache<Integer, CalculatedString> cache = IndexedCache.of (a ->
            {
                // Lazy evaluate for the cache.
                final CalculatedString cs = new CalculatedString ();
                cs.m_s = String.valueOf ((int) a);
                CalculatedString.m_countCalcs.incrementAndGet ();

                return cs;
            });

            final Runnable task = () ->
            {
                for (int j = 0; j < countMapEntries; ++j)
                {
                    cache.get (j);
                    CalculatedString.m_countGets.incrementAndGet ();
                }
            };
            HcUtil.executeManyConcurrentlyFor (task, threadCount, msecDuration, 0);

            // Should only have ever calculated once per value.
            final int calcCount = CalculatedString.m_countCalcs.get ();
            //            System.out.printf ("Discrepancy %s for count %s performed %s calcs out of %s gets%n", calcCount - countMapEntries, countMapEntries,
            //                calcCount, CalculatedString.m_countGets.get ());

            assertEquals (countMapEntries, calcCount);
        }
    }

    private static class CalculatedString
    {
        @SuppressWarnings ("unused")
        private String m_s;

        private static AtomicInteger m_countCalcs = new AtomicInteger ();

        private static AtomicInteger m_countGets = new AtomicInteger ();
    }
}
