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
import java.util.Date;
import org.junit.Test;
import au.com.breakpoint.hedron.core.log.FolderPerDayLogger;

public class FolderPerDayLoggerTest
{
    @Test
    public void testGetLogSubfolder ()
    {
        //System.out.println (new Date ().getTime ());
        final Date date = new Date (1331251144615L);
        final String s = FolderPerDayLogger.getLogSubfolder (date);
        assertEquals ("/2012-03-09", s);
    }
}
