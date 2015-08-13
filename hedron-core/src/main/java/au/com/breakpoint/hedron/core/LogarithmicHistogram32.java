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

import java.io.Serializable;
import java.util.function.BiFunction;

/**
 * Immutable class for accumulating 32 bins of histogram data from int values. Long values
 * over integer are stuffed into the 32nd bin.
 */
public class LogarithmicHistogram32 implements Serializable
{
    public LogarithmicHistogram32 ()
    {
        m_bins = new int[BIN_COUNT];
    }

    private LogarithmicHistogram32 (final int[] bins)
    {
        m_bins = bins;
    }

    //  Bin             min            max
    // -----------------------------------
    //     0              0              1
    //     1              2              3
    //     2              4              7
    //     3              8             15
    //     4             16             31
    //     5             32             63
    //     6             64            127
    //     7            128            255
    //     8            256            511
    //     9            512          1,023
    //    10          1,024          2,047
    //    11          2,048          4,095
    //    12          4,096          8,191
    //    13          8,192         16,383
    //    14         16,384         32,767
    //    15         32,768         65,535
    //    16         65,536        131,071
    //    17        131,072        262,143
    //    18        262,144        524,287
    //    19        524,288      1,048,575
    //    20      1,048,576      2,097,151
    //    21      2,097,152      4,194,303
    //    22      4,194,304      8,388,607
    //    23      8,388,608     16,777,215
    //    24     16,777,216     33,554,431
    //    25     33,554,432     67,108,863
    //    26     67,108,864    134,217,727
    //    27    134,217,728    268,435,455
    //    28    268,435,456    536,870,911
    //    29    536,870,912  1,073,741,823
    //    30  1,073,741,824  2,147,483,647

    public LogarithmicHistogram32 accumulate (final int value)
    {
        final int log = HcUtil.log2 (value);

        final int[] bins = m_bins.clone ();
        ++bins[log];

        return new LogarithmicHistogram32 (bins);
    }

    /**
     * Long values over integer-max are stuffed into the 32nd bin.
     */
    public LogarithmicHistogram32 accumulate (final long value)
    {
        return accumulate ((int) (value <= Integer.MAX_VALUE ? value : Integer.MAX_VALUE));
    }

    public int[] getHistogramBins ()
    {
        return m_bins;
    }

    public boolean isUnused ()
    {
        boolean isUnused = true;

        for (int i = 0; isUnused && i < m_bins.length; ++i)
        {
            isUnused = m_bins[i] == 0;
        }

        return isUnused;
    }

    @Override
    public String toString ()
    {
        final BiFunction<Integer, Boolean, String> formatter = (i, first) ->
        {
            String s = "";

            final int count = m_bins[i];

            if (count > 0)
            {
                final int minValue = getRangeMinValue (i);
                final int maxValue = getRangeMaxValue (i, minValue);

                s = String.format ("%s%s-%s:%s", first ? "" : "; ", minValue, maxValue, count);
            }

            return s;
        };

        return toStringFormatted (formatter);
    }

    public String toStringFormatted (final BiFunction<Integer, Boolean, String> formatter)
    {
        final StringBuilder sb = new StringBuilder ();

        boolean first = true;
        for (int i = 0; i < m_bins.length; ++i)
        {
            final String s = formatter.apply (i, first);
            if (first)
            {
                first = s.length () == 0;
            }
            sb.append (s);
        }

        return sb.toString ();
    }

    public static int getRangeMaxValue (final int i, final int minValue)
    {
        return i == 0 ? 1 : minValue * 2 - 1;// also handles last bin via overflow
    }

    public static int getRangeMinValue (final int i)
    {
        return i == 0 ? 0 : 1 << i;// also handles last bin via overflow.
    }

    private final int[] m_bins;

    private static final int BIN_COUNT = HcUtil.log2 (Integer.MAX_VALUE) + 1;

    private static final long serialVersionUID = -3502296987700339661L;
}
