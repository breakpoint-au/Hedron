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
import static org.junit.Assert.assertSame;
import org.junit.Test;
import au.com.breakpoint.hedron.core.MaxCounter;
import au.com.breakpoint.hedron.core.log.Logging;

public class MaxCounterTest
{
    @Test
    public void testGetResults ()
    {
        // Profiling wrapper. Timed wrapper for the inserts and the stored proc.
        final MaxCounter ts1 = MaxCounter.of ("ts1");
        final MaxCounter ts2 = MaxCounter.of ("ts2");

        for (int i = 0; i < 10; ++i)
        {
            ts1.increment ();
            if (i % 2 == 0)
            {
                ts2.increment ();
            }
        }
        assertEquals (10, ts1.get ());
        assertEquals (5, ts2.get ());

        Logging.logInfo (MaxCounter.getResults (false));
    }

    @Test
    public void testInitialState ()
    {
        final MaxCounter ts = MaxCounter.of ("ts");
        assertEquals (0, ts.get ());
    }

    @Test
    public void testMax ()
    {
        final MaxCounter ts = MaxCounter.of ("tsmaxtest");
        for (int i = 0; i < 5; ++i)
        {
            ts.increment ();
            final int v = i + 1;
            assertEquals (v, ts.get ());
            assertEquals (v, ts.getMax ());
        }
        for (int i = 0; i < 5; ++i)
        {
            ts.decrement ();
            final int v = 5 - i - 1;
            assertEquals (v, ts.get ());
            assertEquals (5, ts.getMax ());
        }

        Logging.logInfo (MaxCounter.getResults (false));
    }

    @Test
    public void testnewInstance ()
    {
        final MaxCounter ts1 = MaxCounter.getCounter ("drhrrfrrfrr");
        final MaxCounter ts2 = MaxCounter.getCounter ("drhrr" + "frrfrr");
        assertSame (ts1, ts2);
    }
}
