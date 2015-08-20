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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import au.com.breakpoint.hedron.core.KeyValuePair;

public class KeyValuePairTest
{
    @Test
    public void testEqualsObject ()
    {
        final KeyValuePair<String, Integer> kvp = KeyValuePair.of ("aa", 1);
        final KeyValuePair<String, Integer> kbp2 = KeyValuePair.of ("a" + "a", 1);

        assertEquals (kvp, kvp);
        assertEquals (kvp, kbp2);

        assertTrue (kvp.equals (kvp));
        assertTrue (kvp.equals (kbp2));

        assertTrue (!kvp.equals (KeyValuePair.of ("ab", 1)));
        assertTrue (!kvp.equals (KeyValuePair.of ("aa", 2)));

        assertTrue (!kvp.equals (null));
        assertTrue (!kvp.equals (4));
    }
}
