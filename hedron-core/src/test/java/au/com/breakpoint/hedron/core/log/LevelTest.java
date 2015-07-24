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
