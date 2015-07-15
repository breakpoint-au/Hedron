package au.com.breakpoint.hedron.core.log;

/**
 * Logging levels.
 */
public enum Level
{
    // Can't use the VALUE_xxx values because of Eclipse class sorting.
    Debug (4), Error (1), Fatal (0), Info (3), Trace (5), Warn (2);

    private Level (final int intValue)
    {
        m_intValue = intValue;
    }

    public int getIntValue ()
    {
        return m_intValue;
    }

    public static int getMaxIntValue ()
    {
        return Trace.getIntValue ();
    }

    public static String getString (final Level level)
    {
        return level.toString ().toUpperCase ();
    }

    private final int m_intValue;

    public static final int VALUE_DEBUG = 4;

    /** A validation-type failure prevented an operation from completing */
    public static final int VALUE_ERROR = 1;

    /** An unexpected event prevented an operation from completing */
    public static final int VALUE_FATAL = 0;

    public static final int VALUE_INFO = 3;

    public static final int VALUE_TRACE = 5;

    /** A warning that did not prevent an operation from completing */
    public static final int VALUE_WARN = 2;
}
