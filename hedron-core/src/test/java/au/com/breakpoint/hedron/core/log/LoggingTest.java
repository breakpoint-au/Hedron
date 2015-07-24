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
