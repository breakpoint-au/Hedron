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

import static java.util.Comparator.comparing;
import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import org.junit.Test;
import au.com.breakpoint.hedron.core.Tuple.E1;
import au.com.breakpoint.hedron.core.Tuple.E2;
import au.com.breakpoint.hedron.core.Tuple.E3;

public class TupleTest
{
    //    @Test
    //    public void testComparableInteger ()
    //    {
    //        assertEquals (-1, Tuple.compare (E3.of (1, 2, 3), E3.of (1, 2, 4)));
    //        assertEquals (0, Tuple.compare (E3.of (1, 2, 3), E3.of (1, 2, 3)));
    //        assertEquals (1, Tuple.compare (E3.of (1, 2, 3), E3.of (1, 2, 2)));
    //    }
    //
    //    @Test (expected = FaultException.class)
    //    public void testComparableInvalid1 ()
    //    {
    //        try (ICloseable scope = new LoggingSilenceScope ())
    //        {
    //            Tuple.compare (E3.of (1, 2, 3), E2.of (1, 2));
    //        }
    //        assertTrue (false); // shouldn't get here
    //    }
    //
    //    @Test (expected = FaultException.class)
    //    public void testComparableInvalid2 ()
    //    {
    //        try (ICloseable scope = new LoggingSilenceScope ())
    //        {
    //            Tuple.compare (E3.of (1, 2, 3), E3.of (1, 2, "1234"));
    //        }
    //        assertTrue (false); // shouldn't get here
    //    }
    //
    //    @Test
    //    public void testComparableString ()
    //    {
    //        assertEquals (-1, Tuple.compare (E3.of ("1", "2", "3"), E3.of ("1", "2", "4")));
    //        assertEquals (0, Tuple.compare (E3.of ("1", "2", "3"), E3.of ("1", "2", "3")));
    //        assertEquals (1, Tuple.compare (E3.of ("1", "2", "3"), E3.of ("1", "2", "2")));
    //    }

    @Test
    public void testToString ()
    {
        assertEquals ("[qqq]", E1.of ("qqq").toString ());
        assertEquals ("[qqq, 4]", E2.of ("qqq", 4).toString ());
        assertEquals ("[qqq, 4, xxx]", E3.of ("qqq", 4, "xxx").toString ());
    }

    @Test
    public void testTupleAsMapKey ()
    {
        for (int limitA = 0; limitA < 30; ++limitA)
        {
            for (int limitB = 0; limitB < 120; ++limitB)
            {
                testTupleAsMapKey (limitA, limitB);
            }
        }
    }

    private enum MapKey
    {
        A, B
    }

    private static void checkEntries (final Map<E2<MapKey, Integer>, Integer> map, final MapKey e, final int limit)
    {
        if (true)
        {
            final List<Entry<E2<MapKey, Integer>, Integer>> entries = new ArrayList<> (map.entrySet ());

            // Sort the results for reliable comparison.
            sortEntries (limit, entries);

            for (int i = 0, keyI = 0; i < limit; ++i)
            {
                final Entry<E2<MapKey, Integer>, Integer> entry = entries.get (i);

                if (entry.getKey ().getE0 () == e)
                {
                    final E2<MapKey, Integer> expectedKey = E2.of (e, keyI);
                    final int expectedValue = getValueToStore (e, limit, keyI);

                    assertEquals (expectedKey, entry.getKey ());
                    assertEquals (expectedValue, (int) entry.getValue ());
                    ++keyI;
                }
            }
        }

        if (true)
        {
            for (int i = 0; i < limit; ++i)
            {
                final E2<MapKey, Integer> expectedKey = E2.of (e, i);
                final Integer entry = map.get (expectedKey);

                final int expectedValue = getValueToStore (e, limit, i);
                assertEquals (expectedValue, (int) entry);
            }
        }
    }

    private static void checkKeys (final Map<E2<MapKey, Integer>, Integer> map, final MapKey e, final int limit,
        final int expectedKeyCount)
    {
        final List<E2<MapKey, Integer>> keys = new ArrayList<> (map.keySet ());
        assertEquals (expectedKeyCount, keys.size ());

        // Sort the results for reliable comparison.
        sortKeys (limit, keys);

        for (int i = 0, keyI = 0; i < limit; ++i)
        {
            final E2<MapKey, Integer> key = keys.get (i);

            if (key.getE0 () == e)
            {
                assertEquals ((int) key.getE1 (), keyI);
                ++keyI;
            }
        }
    }

    private static int getComparisonValue (final MapKey e, final int e1)
    {
        // Make global ranking assuming max limit of 10000
        final int base = e == MapKey.A ? 1 : 10000;
        return base + e1;
    }

    private static int getMultiplier (final MapKey e)
    {
        return e == MapKey.A ? 7 : 11;
    }

    private static int getValueToStore (final MapKey e, final int limit, final int i)
    {
        return limit * getMultiplier (e) + i;
    }

    private static void sortEntries (@SuppressWarnings ("unused") final int limit,
        final List<Entry<E2<MapKey, Integer>, Integer>> entries)
    {
        final Function<Entry<E2<MapKey, Integer>, Integer>, Integer> extractor = e ->
        {
            final E2<MapKey, Integer> t = e.getKey ();
            return getComparisonValue (t.getE0 (), t.getE1 ());
        };
        entries.sort (comparing (extractor));
    }

    private static void sortKeys (@SuppressWarnings ("unused") final int limit, final List<E2<MapKey, Integer>> keys)
    {
        final Function<E2<MapKey, Integer>, Integer> extractor = t -> getComparisonValue (t.getE0 (), t.getE1 ());
        keys.sort (comparing (extractor));
    }

    private static void stuffMap (final Map<E2<MapKey, Integer>, Integer> map, final MapKey e, final int limit)
    {
        for (int i = 0; i < limit; ++i)
        {
            final E2<MapKey, Integer> key = E2.of (e, i);
            map.put (key, getValueToStore (e, limit, i));
        }
    }

    private static void testTupleAsMapKey (final int limitA, final int limitB)
    {
        //System.out.printf ("limitA %s limitB %s%n", limitA, limitB);

        final Map<E2<MapKey, Integer>, Integer> map = new HashMap<> ();
        if (true)
        {
            stuffMap (map, MapKey.A, limitA);

            // Should contain keys for 0-10
            assertEquals (limitA, map.size ());

            checkKeys (map, MapKey.A, limitA, limitA);
            checkEntries (map, MapKey.A, limitA);
        }

        if (true)
        {
            stuffMap (map, MapKey.B, limitB);

            // Should contain keys for A[0-9] and B[0-4]
            assertEquals (limitA + limitB, map.size ());

            checkKeys (map, MapKey.A, limitA, limitA + limitB);
            checkEntries (map, MapKey.A, limitA);

            checkKeys (map, MapKey.B, limitB, limitA + limitB);
            checkEntries (map, MapKey.B, limitB);
        }

        map.clear ();
    }
}
