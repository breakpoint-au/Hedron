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
package au.com.breakpoint.hedron.core.log;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import au.com.breakpoint.hedron.core.ICloseable;
import au.com.breakpoint.hedron.core.context.LoggingSilenceScope;
import au.com.breakpoint.hedron.core.log.ConsoleLogger;
import au.com.breakpoint.hedron.core.log.ILogger;
import au.com.breakpoint.hedron.core.log.Level;
import au.com.breakpoint.hedron.core.log.Logging;

public class AbstractLoggerTest
{
    @Test
    public void testConfigureLevels1 ()
    {
        final ILogger logger = configure ("few");

        assertTrue (logger.isEnabled (Level.Fatal));
        assertTrue (logger.isEnabled (Level.Error));
        assertTrue (logger.isEnabled (Level.Warn));
        assertTrue (!logger.isEnabled (Level.Info));
        assertTrue (!logger.isEnabled (Level.Debug));
    }

    @Test
    public void testConfigureLevels2 ()
    {
        final ILogger logger = configure ("d");

        assertTrue (!logger.isEnabled (Level.Fatal));
        assertTrue (!logger.isEnabled (Level.Error));
        assertTrue (!logger.isEnabled (Level.Warn));
        assertTrue (!logger.isEnabled (Level.Info));
        assertTrue (logger.isEnabled (Level.Debug));
    }

    @Test
    public void testConfigureLevels3 ()
    {
        final ILogger logger = configure ("eit");

        assertTrue (!logger.isEnabled (Level.Fatal));
        assertTrue (logger.isEnabled (Level.Error));
        assertTrue (!logger.isEnabled (Level.Warn));
        assertTrue (logger.isEnabled (Level.Info));
        assertTrue (!logger.isEnabled (Level.Debug));
        assertTrue (logger.isEnabled (Level.Trace));
    }

    @Test
    public void testLogSilence ()
    {
        Logging.clearLoggers ();

        final DummyLogger logger = new DummyLogger ("fewi", null, null);
        Logging.addLogger (logger);

        checkLogging (logger, true);
        try (final ICloseable scope = new LoggingSilenceScope ())
        {
            checkLogging (logger, false);
        }

        checkLogging (logger, true);
    }

    private void checkLogging (final DummyLogger logger, final boolean expected)
    {
        final String message = "message";

        logger.clearLogging ();

        Logging.logFatalString (message);
        Logging.logErrorString (message);
        Logging.logWarnString (message);
        Logging.logInfoString (message);
        Logging.logDebugString (message);
        Logging.logTraceString (message);

        final int expectedCount = expected ? 1 : 0;

        assertEquals (expectedCount, logger.m_valueLogFatal.size ());
        assertEquals (expectedCount, logger.m_valueLogError.size ());
        assertEquals (expectedCount, logger.m_valueLogWarn.size ());
        assertEquals (expectedCount, logger.m_valueLogInfo.size ());

        assertEquals (0, logger.m_valueLogDebug.size ());
        assertEquals (0, logger.m_valueLogTrace.size ());
    }

    private ILogger configure (final String levels)
    {
        return new ConsoleLogger (levels);
    }
}
