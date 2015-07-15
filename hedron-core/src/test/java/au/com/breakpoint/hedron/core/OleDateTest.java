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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.Date;
import java.util.stream.IntStream;
import org.junit.Test;
import au.com.breakpoint.hedron.core.OleDate;

public class OleDateTest
{
    @Test
    public void test_convertDateTimeSsm ()
    {
        assertEquals (12345.0, OleDate.convertDateTimeSsm (12345, 0), .0000001);
        assertEquals (12345.25, OleDate.convertDateTimeSsm (12345, 86400 / 4), .0000001);
        assertEquals (12345.75, OleDate.convertDateTimeSsm (12345, 3 * 86400 / 4), .0000001);
    }

    @Test
    public void test_formatDateTime ()
    {
        final String s = OleDate.formatDateTime (41491.25);
        assertEquals ("05/08/2013 06:00:00", s);
    }

    @Test
    public void test_getMicrosoftDateTimeRepresentation ()
    {
        IntStream.of (0, 1, 2, -1, -2, -3).forEach (offset ->
        {
            for (int i = 0; i < 24; ++i)
            {
                for (int j = 0; j < 60; ++j)
                {
                    for (final int nanosecond : NANOSECONDS)
                    {
                        checkMicrosoftDateTimeRepresentation (offset, i, j, nanosecond);
                    }
                }
            }
        });
    }

    private void checkMicrosoftDateTimeRepresentation (final int offset, final int hour, final int minute, final int ns)
    {
        final int day = 7 + offset;
        final LocalDateTime ldt = LocalDateTime.of (2014, Month.APRIL, day, hour, minute, 50, ns);
        final Instant instant = ldt.atZone (ZoneId.systemDefault ()).toInstant ();
        final Date dt = Date.from (instant);

        final double actual = OleDate.getMicrosoftDateTimeRepresentation (dt);
        final double expected =
            41736.0 + offset + hour / 24.0 + minute / (24.0 * 60.0) + 50 / (24.0 * 60.0 * 60.0) + ns / (24.0 * 60.0 * 60.0 * 1_000_000_000.0);

        if (Math.abs (actual - expected) > 0.000_000_000_1)
        {
            System.out.printf ("%.3f expected / %.3f actual for %s %2d:%2d%n", expected, actual, day, hour, minute);
        }
        assertEquals (expected, actual, 0.000_000_000_1);
    }

    private static final int[] NANOSECONDS =
        {
                0,
                50,
                450,
                900,
                999
    };
}
