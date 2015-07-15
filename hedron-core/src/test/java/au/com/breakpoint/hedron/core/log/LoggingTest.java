package au.com.breakpoint.hedron.core.log;

import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;
import au.com.breakpoint.hedron.core.context.FaultException;
import au.com.breakpoint.hedron.core.context.LoggingSilenceScope;
import au.com.breakpoint.hedron.core.log.Level;
import au.com.breakpoint.hedron.core.log.Logging;

public class LoggingTest
{
    @Test
    public void testParseLevelString ()
    {
        assertArrayEquals (new Level[]
        {}, Logging.parseLevelString (""));
        assertArrayEquals (new Level[]
        {
                Level.Debug
        }, Logging.parseLevelString ("d"));
        assertArrayEquals (new Level[]
        {
                Level.Debug,
                Level.Info
        }, Logging.parseLevelString ("di"));
        assertArrayEquals (new Level[]
        {
                Level.Debug,
                Level.Info,
                Level.Warn
        }, Logging.parseLevelString ("diw"));
        assertArrayEquals (new Level[]
        {
                Level.Debug,
                Level.Info,
                Level.Warn,
                Level.Error
        }, Logging.parseLevelString ("diwe"));
        assertArrayEquals (new Level[]
        {
                Level.Debug,
                Level.Info,
                Level.Warn,
                Level.Error,
                Level.Fatal
        }, Logging.parseLevelString ("diwef"));
    }

    @Test (expected = FaultException.class)
    public void testParseLevelStringUnknown ()
    {
        try (final LoggingSilenceScope ls = new LoggingSilenceScope ())
        {
            Logging.parseLevelString ("q");
        }
    }
}
