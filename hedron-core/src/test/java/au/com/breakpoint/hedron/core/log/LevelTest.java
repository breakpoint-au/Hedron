package au.com.breakpoint.hedron.core.log;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import au.com.breakpoint.hedron.core.log.Level;

public class LevelTest
{
    @Test
    public void testLevelToString ()
    {
        assertEquals ("Trace", Level.Trace.toString ());
        assertEquals ("Debug", Level.Debug.toString ());
        assertEquals ("Error", Level.Error.toString ());
        assertEquals ("Fatal", Level.Fatal.toString ());
        assertEquals ("Info", Level.Info.toString ());
        assertEquals ("Warn", Level.Warn.toString ());
    }

    @Test
    public void testLevelValues ()
    {
        assertEquals (Level.Trace.getIntValue (), Level.VALUE_TRACE);
        assertEquals (Level.Debug.getIntValue (), Level.VALUE_DEBUG);
        assertEquals (Level.Error.getIntValue (), Level.VALUE_ERROR);
        assertEquals (Level.Fatal.getIntValue (), Level.VALUE_FATAL);
        assertEquals (Level.Info.getIntValue (), Level.VALUE_INFO);
        assertEquals (Level.Warn.getIntValue (), Level.VALUE_WARN);
    }
}
