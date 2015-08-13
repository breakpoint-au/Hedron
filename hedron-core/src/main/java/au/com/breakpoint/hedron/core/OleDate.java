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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.core.value.AbstractValue;

/**
 * Minimal subset of breakpoint.jar brought in to support Microsoft-style OLE dates.
 */
public class OleDate
{
    /**
     * Lazy date & time formatting support.
     */
    public static class FormattedDateTimeValue extends AbstractValue<String>
    {
        public FormattedDateTimeValue (final double dateTime)
        {
            m_dateTime = dateTime;
        }

        @Override
        public String get ()
        {
            // Calculate the value if not already done. Does nothing if already calculated.
            if (m_value == null)
            {
                m_value = formatDateTime (m_dateTime);
            }

            return m_value;
        }

        private final double m_dateTime;

        private String m_value;
    }

    /**
     * Lazy date formatting support.
     */
    public static class FormattedDateValue extends AbstractValue<String>
    {
        public FormattedDateValue (final double date)
        {
            m_date = date;
        }

        @Override
        public String get ()
        {
            // Calculate the value if not already done. Does nothing if already calculated.
            if (m_value == null)
            {
                m_value = formatDate (m_date, false);
            }

            return m_value;
        }

        private final double m_date;

        private String m_value;
    }

    public static final class Index
    {
        public static final int INDEX_DAY = 2;

        public static final int INDEX_DAY_OF_WEEK = 6;

        public static final int INDEX_HOUR = 3;

        public static final int INDEX_MINUTE = 4;

        public static final int INDEX_MONTH = 1;

        public static final int INDEX_SECOND = 5;

        public static final int INDEX_YEAR = 0;
    }

    private static class BadParameter extends Exception
    {
        private static final long serialVersionUID = 1L;
    }

    public static int[] convertDateTime (final double dtSrc)
    {
        int st[] = null;

        if (!Double.isNaN (dtSrc))
        {
            try
            {
                st = decomposeDATE (dtSrc);
            }
            catch (final BadParameter e)
            {
                // Propagate exception as unchecked fault up to the fault barrier.
                ThreadContext.throwFault (e);
            }
        }

        return st;
    }

    // wMonth is 1-based
    public static double convertDateTime (final int wYear, final int wMonth, final int wDay, final int wHour,
        final int wMinute, final int wSecond)
    {
        double date = Double.NaN;

        try
        {
            date = composeDATE (wYear, wMonth, wDay, wHour, wMinute, wSecond);
        }
        catch (final BadParameter e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }

        return date;
    }

    public static double convertDateTime (final int[] st)
    {
        return convertDateTime (st[Index.INDEX_YEAR], st[Index.INDEX_MONTH], st[Index.INDEX_DAY], st[Index.INDEX_HOUR],
            st[Index.INDEX_MINUTE], st[Index.INDEX_SECOND]);
    }

    public static double convertDateTimeSsm (final int dateComponent, final int ssm)
    {
        return (dateComponent + ((double) ssm / (double) SECONDS_IN_DAY));
    }

    public static int convertDateToIntegral (final double date)
    {
        final int[] st = convertDateTime (date);

        // yyyymmdd
        return st != null ? (st[Index.INDEX_YEAR] * 10000 + st[Index.INDEX_MONTH] * 100 + st[Index.INDEX_DAY]) : -1;
    }

    public static String formatDate (final double dateTime, final boolean yearFirst)
    {
        return formatDate (convertDateToIntegral (dateTime), yearFirst);
    }

    public static String formatDate (final int integralDate, final boolean yearFirst)
    {
        final String date = prependLeadingZeroes (Integer.toString (integralDate), 8);

        return yearFirst ? date.substring (0, 4) + "/" + date.substring (4, 6) + "/" + date.substring (6, 8)
            : date.substring (6, 8) + "/" + date.substring (4, 6) + "/" + date.substring (0, 4);
    }

    public static String formatDate (final java.util.Date dateTime, final boolean yearFirst)
    {
        // java.util.Date objects are represented internally as the number of
        // milliseconds that have passed since January 1, 1970 00:00:00.000 GMT.
        return formatDate (getMicrosoftDateTimeRepresentation (dateTime), yearFirst);
    }

    public static String formatDateTime (final double dateTime)
    {
        String formattedDateTime = "?";

        final int[] st = convertDateTime (dateTime);

        if (st != null)
        {
            final String time = formatTimeComponent (st[Index.INDEX_HOUR]) + ":" + formatTimeComponent (
                st[Index.INDEX_MINUTE]) + ":" + formatTimeComponent (st[Index.INDEX_SECOND]);
            formattedDateTime = formatDate (dateTime, false) + " " + time;
        }

        return formattedDateTime;
    }

    // This value is equivalent to the OLE DATE value.
    @SuppressWarnings ("deprecation")
    public static double getCurrentDATE ()
    {
        double currentDateTimeAsDATE = Double.NaN;

        final java.util.Date localDate = getCurrentDateTimeAsDate ();
        try
        {
            currentDateTimeAsDATE = composeDATE (localDate.getYear () + 1900, localDate.getMonth () + 1, // convert from 0-based
                localDate.getDate (), localDate.getHours (), localDate.getMinutes (), localDate.getSeconds ());
        }
        catch (final BadParameter e)
        {
        }

        return currentDateTimeAsDATE;
    };

    public static synchronized java.util.Date getCurrentDateTimeAsDate ()
    {
        // Get the current date/time in the default time zone
        // N.B. This relies on the timezone, date and time being correctly set
        // on the machine on which this code executes.
        // e.g. on a UNIX machine the TZ environment variable will
        // need to be set to something like
        // TZ=AET
        // export TZ

        return new java.util.Date ();
    }

    public static java.util.Date getJavaDateTimeRepresentation (final double dateTime)
    {
        // java.util.Date objects are represented internally as the number of
        // milliseconds that have passed since January 1, 1970 00:00:00.000 GMT.
        final long totalSeconds = (long) (dateTime * SECONDS_IN_DAY);
        final long totalMilliseconds = (long) (dateTime * MILLISECONDS_IN_DAY);

        final long milliseconds = totalMilliseconds - (totalSeconds * 1000);

        final java.util.Date date = new java.util.Date (getJDBCDateTimeRepresentation (dateTime) + milliseconds);
        return date;
    }

    public static long getJDBCDateTimeRepresentation (final double dateTime)
    {
        // To conform with the definition of SQL DATE, the millisecond values
        // wrapped by a java.sql.Date instance must be 'normalized' by setting
        // the hours, minutes, seconds, and milliseconds to zero in the
        // particular time zone with which the instance is associated.
        final int[] st = convertDateTime (dateTime);

        final java.util.Calendar calendar =
            new java.util.GregorianCalendar (st[Index.INDEX_YEAR], st[Index.INDEX_MONTH] - 1, // DECEMBER = 11
                st[Index.INDEX_DAY], st[Index.INDEX_HOUR], st[Index.INDEX_MINUTE], st[Index.INDEX_SECOND]);

        return calendar.getTime ().getTime ();
    }

    public static double getMicrosoftDateTimeRepresentation (final Date dt)
    {
        final Instant instant = dt.toInstant ();
        final LocalDateTime l = LocalDateTime.ofInstant (instant, ZoneId.systemDefault ());

        final double ns = l.getNano () / 1_000_000_000.0;
        final double oleDateTime = convertDateTime (l.getYear (), l.getMonthValue (), l.getDayOfMonth (), l.getHour (),
            l.getMinute (), l.getSecond ()) + ns;

        return oleDateTime;
    }

    //////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////
    // The following methods are a minimal port of the implementation in MFC.
    //////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////
    private static double composeDATE (final int wYear, final int wMonth, final int wDay, final int wHour,
        final int wMinute, final int wSecond) throws BadParameter
    {
        // Validate year and month (ignore day of week and milliseconds)
        if (wYear > 9999 || wMonth < 1 || wMonth > 12)
        {
            throw new BadParameter ();
        }

        //  Check for leap year and set the number of days in the month
        final boolean bLeapYear = ((wYear & 3) == 0) && ((wYear % 100) != 0 || (wYear % 400) == 0);

        final int nDaysInMonth =
            _afxMonthDays[wMonth] - _afxMonthDays[wMonth - 1] + ((bLeapYear && wDay == 29 && wMonth == 2) ? 1 : 0);

        // Finish validating the date
        if (wDay < 1 || wDay > nDaysInMonth || wHour > 23 || wMinute > 59 || wSecond > 59)
        {
            throw new BadParameter ();
        }

        // Cache the date in days and time in fractional days
        int nDate;
        double dblTime;

        //It is a valid date; make Jan 1, 1AD be 1
        nDate = wYear * 365 + wYear / 4 - wYear / 100 + wYear / 400 + _afxMonthDays[wMonth - 1] + wDay;

        //  If leap year and it's before March, subtract 1:
        if (wMonth <= 2 && bLeapYear)
        {
            --nDate;
        }

        //  Offset so that 12/30/1899 is 0
        nDate -= 693959;

        dblTime = ((wHour * 3600) + // hrs in seconds
        (wMinute * 60) + // mins in seconds
        (wSecond)) / 86400.;

        return nDate + ((nDate >= 0) ? dblTime : -dblTime);
    }

    private static int[] decomposeDATE (final double dtSrc) throws BadParameter
    {
        final int st[] = new int[DATE_TIME_ELEMENTS];

        // The legal range does not actually span year 0 to 9999.
        if (dtSrc > MAX_DATE || dtSrc < MIN_DATE)
        {
            throw new BadParameter ();
        }

        int nDaysAbsolute;// Number of days since 1/1/0
        int nSecsInDay;// Time in seconds since midnight
        int nMinutesInDay;// Minutes in day

        int n400Years;// Number of 400 year increments since 1/1/0
        int n400Century;// Century within 400 year block (0,1,2 or 3)
        int n4Years;// Number of 4 year increments since 1/1/0
        int n4Day;// Day within 4 year block (0 is 1/1/yr1, 1460 is 12/31/yr4)
        int n4Yr;// Year within 4 year block (0,1,2 or 3)
        boolean bLeap4 = true;// true if 4 year block includes leap year

        double dblDate = dtSrc;// tempory serial date

        // Round to the second
        dblDate += ((dtSrc > 0.0) ? HALF_SECOND : -HALF_SECOND);

        nDaysAbsolute = (int) dblDate + 693959;// Add days from 1/1/0 to 12/30/1899

        dblDate = Math.abs (dblDate);
        nSecsInDay = (int) ((dblDate - Math.floor (dblDate)) * 86400.);

        // Calculate the day of week (sun=1, mon=2...)
        //   -1 because 1/1/0 is Sat.  +1 because we want 1-based
        st[Index.INDEX_DAY_OF_WEEK] = ((nDaysAbsolute - 1) % 7) + 1;

        // Leap years every 4 yrs except centuries not multiples of 400.
        n400Years = (nDaysAbsolute / 146097);

        // Set nDaysAbsolute to day within 400-year block
        nDaysAbsolute %= 146097;

        // -1 because first century has extra day
        n400Century = ((nDaysAbsolute - 1) / 36524);

        // Non-leap century
        if (n400Century != 0)
        {
            // Set nDaysAbsolute to day within century
            nDaysAbsolute = (nDaysAbsolute - 1) % 36524;

            // +1 because 1st 4 year increment has 1460 days
            n4Years = ((nDaysAbsolute + 1) / 1461);

            if (n4Years != 0)
            {
                n4Day = ((nDaysAbsolute + 1) % 1461);
            }
            else
            {
                bLeap4 = false;
                n4Day = nDaysAbsolute;
            }
        }
        else
        {
            // Leap century - not special case!
            n4Years = (nDaysAbsolute / 1461);
            n4Day = (nDaysAbsolute % 1461);
        }

        if (bLeap4)
        {
            // -1 because first year has 366 days
            n4Yr = (n4Day - 1) / 365;

            if (n4Yr != 0)
            {
                n4Day = (n4Day - 1) % 365;
            }
        }
        else
        {
            n4Yr = n4Day / 365;
            n4Day %= 365;
        }

        // n4Day is now 0-based day of year. Save 1-based day of year, year number
        st[Index.INDEX_DAY_OF_WEEK] = n4Day + 1;
        st[Index.INDEX_YEAR] = n400Years * 400 + n400Century * 100 + n4Years * 4 + n4Yr;

        // Handle leap year: before, on, and after Feb. 29.
        boolean gotoDoTime = false;
        if (n4Yr == 0 && bLeap4)
        {
            // Leap Year
            if (n4Day == 59)
            {
                // Feb. 29
                st[Index.INDEX_MONTH] = 2;
                st[Index.INDEX_DAY] = 29;
                gotoDoTime = true;// port of 'goto DoTime'
            }

            if (!gotoDoTime)
            {
                // Pretend it's not a leap year for month/day comp.
                if (n4Day >= 60)
                {
                    --n4Day;
                }
            }
        }

        // Make n4DaY a 1-based day of non-leap year and compute
        //  month/day for everything but Feb. 29.
        if (!gotoDoTime)
        {
            ++n4Day;

            // Month number always >= n/32, so save some loop time
            for (st[Index.INDEX_MONTH] =
                (n4Day >> 5) + 1; n4Day > _afxMonthDays[st[Index.INDEX_MONTH]]; st[Index.INDEX_MONTH]++)
            {
            }

            st[Index.INDEX_DAY] = (n4Day - _afxMonthDays[st[Index.INDEX_MONTH] - 1]);
        }

        // DoTime:
        if (nSecsInDay == 0)
        {
            st[Index.INDEX_HOUR] = st[Index.INDEX_MINUTE] = st[Index.INDEX_SECOND] = 0;
        }
        else
        {
            st[Index.INDEX_SECOND] = nSecsInDay % 60;
            nMinutesInDay = nSecsInDay / 60;
            st[Index.INDEX_MINUTE] = nMinutesInDay % 60;
            st[Index.INDEX_HOUR] = nMinutesInDay / 60;
        }

        return st;
    }

    private static String formatTimeComponent (final int value)
    {
        String valueText = Integer.toString (value);

        if (valueText.length () < 2)
        {
            valueText = "0" + valueText;
        }

        return valueText;
    }

    private static String prependLeadingZeroes (final String source, final int totalLength)
    {
        String target = null;
        final int sourceLength = source.length ();

        if (sourceLength == totalLength)
        {
            target = source;
        }
        else if (sourceLength > 0 && sourceLength < totalLength)
        {
            final int numberOfLeadingZeroes = totalLength - sourceLength;
            final StringBuilder leadingZeroes = new StringBuilder (numberOfLeadingZeroes);
            leadingZeroes.setLength (numberOfLeadingZeroes);

            for (int i = 0; i < numberOfLeadingZeroes; ++i)
            {
                leadingZeroes.setCharAt (i, '0');
            }

            target = leadingZeroes.toString () + source;
        }
        else
        {
            // If no source string was supplied (i.e. zero length) or the
            // length of the source was greater than the total length
            // supplied, then do nothing.
            target = source;
        }

        return target;
    }

    public static final double DOUBLE_SECONDS_IN_DAY = 86400.0;

    public static final int SECONDS_IN_DAY = 86400;

    // One-based array of days in year at month start
    private static int _afxMonthDays[] =
        {
                0,
                31,
                59,
                90,
                120,
                151,
                181,
                212,
                243,
                273,
                304,
                334,
                365
    };

    // Definitions for the array of integers returned from methods such as
    // int[] ConvertDateTime (double dtSrc).
    private static final int DATE_TIME_ELEMENTS = 7;

    // Half a second, expressed in days
    private static final double HALF_SECOND = 1.0 / 172800.0;

    private static final int MAX_DATE = 2958465;// about year 9999

    private static final int MILLISECONDS_IN_DAY = 86400000;

    private static final int MIN_DATE = -657434;// about year 100;
}
