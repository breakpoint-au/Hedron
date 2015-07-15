package au.com.breakpoint.hedron.core;

public class UserFeedback implements ICloseable
{
    public UserFeedback (final boolean debug)
    {
        this (DEFAULT_WRITER, debug);
    }

    public UserFeedback (final IWriter writer, final boolean debug)
    {
        m_writer = writer;
        m_debug = debug;
    }

    @Override
    public void close ()
    {
        // Remove any temporary messages.
        outputMessage ("");
    }

    public void outputDebugMessage (final boolean startNewLine, final int indentLevel, final String format,
        final Object... args)
    {
        if (m_debug)
        {
            outputMessage (startNewLine, indentLevel, format, args);
        }
    }

    public void outputMessage (final boolean startNewLine, final int indentLevel, final String format,
        final Object... args)
    {
        outputMessage (String.format (format, args), startNewLine, indentLevel);
    }

    public void outputMessage (final String s)
    {
        outputMessage (s, true, 0);
    }

    public void outputMessage (final String s, final boolean startNewLine, final int indentLevel)
    {
        outputMessage (s, startNewLine ? Position.NextLine : Position.Flow, indentLevel);
    }

    public synchronized void outputMessage (final String s, final Position position, final int indentLevel)
    {
        final StringBuilder sb = new StringBuilder ();
        final int length = s.length ();

        switch (position)
        {
            case NextLine:
            case TemporaryLine:
            {
                if (m_currentLineLength == 0)
                {
                    appendString (sb, indentLevel, s);
                }
                else if (m_currentLineIsTemporary)
                {
                    // Overwrite the temporary line.
                    sb.append ("\r");
                    appendStringRightCleared (sb, indentLevel, s, m_currentLineLength);
                }
                else
                {
                    sb.append (HcUtil.NewLine);
                    appendString (sb, indentLevel, s);
                }

                m_currentLineLength = length + indentLevel * INDENT_SPACES.length ();
                m_currentLineIsTemporary = position == Position.TemporaryLine;
                if (!m_currentLineIsTemporary)
                {
                    m_lastIndentLevel = indentLevel;
                }
                break;
            }

            case Flow:
            {
                sb.append (s);
                m_currentLineLength += length;
                break;
            }
        }

        if (sb.length () > 0)
        {
            m_writer.print (sb.toString ());
        }
    }

    public void showProgress (final String s)
    {
        if (s != null)
        {
            outputMessage (s, Position.TemporaryLine, m_lastIndentLevel + 1);
            //HcUtil.pause (50);
        }
    }

    public void showProgress (final String format, final Object... args)
    {
        showProgress (String.format (format, args));
    }

    @FunctionalInterface
    public static interface IWriter
    {
        public void print (String s);
    }

    public enum Position
    {
        Flow, NextLine, TemporaryLine
    }

    private static void appendIndentation (final StringBuilder sb, final int indentLevel)
    {
        for (int i = 0; i < indentLevel; ++i)
        {
            sb.append (INDENT_SPACES);
        }
    }

    //public static void main (final String[] args)
    //{
    //    try (final UserFeedback fb = new UserFeedback (true))
    //    {
    //        fb.outputMessage ("test0");
    //        fb.outputMessage ("test1");
    //        fb.outputMessage ("test2");
    //        fb.showProgress ("a");
    //        final int N = 15_000;
    //        for (int i = 0; i < N; ++i)
    //        {
    //            fb.showProgress ("bbbbbbbbbbbbb" + (N - i) * 9);
    //        }
    //        fb.outputMessage ("test3");
    //        fb.outputMessage ("test4");
    //        fb.showProgress ("ccccccccccc");
    //        //fb.outputMessage ("test5");
    //    }
    //}

    private static void appendString (final StringBuilder sb, final int indentLevel, final String s)
    {
        appendIndentation (sb, indentLevel);
        sb.append (s);
    }

    private static String appendStringRightCleared (final StringBuilder sb, final int indentLevel, final String s,
        final int previousLineLength)
    {
        appendString (sb, indentLevel, s);

        final int spacesIndented = indentLevel * INDENT_SPACES.length ();
        final int additionalSpaces = previousLineLength - (spacesIndented + s.length ());

        if (additionalSpaces > 0)
        {
            // Wipe additional characters.
            for (int i = 0; i < additionalSpaces; ++i)
            {
                sb.append (' ');
            }

            // Move cursor back.
            for (int i = 0; i < additionalSpaces; ++i)
            {
                sb.append ('\b');
            }
        }

        return sb.toString ();
    }

    private volatile boolean m_currentLineIsTemporary;

    private volatile int m_currentLineLength;

    private final boolean m_debug;

    private volatile int m_lastIndentLevel;

    private final IWriter m_writer;

    /** Do console output asynchronously */
    public static final IWriter ASYNC_WRITER = s -> HcUtil.executeSerialAsync ( () -> System.out.print (s));

    /** Do console output synchronously */
    public static final IWriter DEFAULT_WRITER = System.out::print;

    private static final String INDENT_SPACES = "  ";
}
