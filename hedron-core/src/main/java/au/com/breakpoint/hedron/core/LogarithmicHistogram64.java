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

/**
 * Immutable class for accumulating 64 bins of histogram data from long values.
 */
public class LogarithmicHistogram64
{
    // TODO _ look into HdrHistogram
    public LogarithmicHistogram64 ()
    {
        m_bins = new int[BIN_COUNT];
    }

    private LogarithmicHistogram64 (final int[] bins)
    {
        m_bins = bins;
    }

    public LogarithmicHistogram64 accumulate (final long value)
    {
        final int log = HcUtil.log2 (value);

        final int[] bins = m_bins.clone ();
        ++bins[log];

        return new LogarithmicHistogram64 (bins);
    }

    public int[] getHistogramBins ()
    {
        return m_bins;
    }

    @Override
    public String toString ()
    {
        final StringBuilder sb = new StringBuilder ();

        for (int i = 0; i < m_bins.length; ++i)
        {
            final int count = m_bins[i];
            if (count > 0)
            {
                if (sb.length () > 0)
                {
                    sb.append ("; ");
                }

                final long minValue;
                final long maxValue;
                if (i == 0)
                {
                    minValue = 0;
                    maxValue = 1;
                }
                else
                {
                    // Also handles last bin via overflow.
                    minValue = 1L << i;
                    maxValue = minValue * 2 - 1;
                }

                sb.append (String.format ("%s-%s:%s", minValue, maxValue, count));
            }
        }

        return sb.toString ();
    }

    private final int[] m_bins;

    private static final int BIN_COUNT = HcUtil.log2 (Long.MAX_VALUE) + 1;
}
