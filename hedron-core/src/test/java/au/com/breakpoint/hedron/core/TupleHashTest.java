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
package au.com.breakpoint.hedron.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import java.util.Map;
import org.junit.Test;
import au.com.breakpoint.hedron.core.GenericFactory;
import au.com.breakpoint.hedron.core.Tuple.E3;

public class TupleHashTest
{
    @Test
    public void testHash ()
    {
        final Map<E3<String, Integer, Long>, E3<String, Integer, Long>> map = GenericFactory.newHashMap ();

        final int count = 10000;
        for (int i = 0; i < count; ++i)
        {
            final E3<String, Integer, Long> t = getATuple (i);
            map.put (t, t);
            assertSame (t, map.get (t));
        }

        for (int i = 0; i < count; ++i)
        {
            final E3<String, Integer, Long> t = getATuple (i);
            assertEquals (t, map.get (t));
        }
    }

    private Long getALong (final int i)
    {
        return Long.valueOf (i + (long) Integer.MAX_VALUE);
    }

    private E3<String, Integer, Long> getATuple (final int i)
    {
        return E3.of (Integer.toBinaryString (i), Integer.valueOf (i), getALong (i));
    }
}
