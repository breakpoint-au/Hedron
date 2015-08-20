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
import java.util.stream.Collectors;
import org.junit.Test;
import au.com.breakpoint.hedron.core.TreeCollection;

public class TreeCollectionTest
{
    @Test
    public void testFlatten ()
    {
        final TreeCollection<String> tc = getSampleTree ();

        // @formatter:off
        final String joined = tc.flattened ()
            .map (TreeCollection::getValue)
            .collect(Collectors.joining ("/"));
         // @formatter:on

        assertEquals ("tc/0/00/1/10/11/12/120/121/122/2/20", joined);
    }

    @Test
    public void testRecurse ()
    {
        final TreeCollection<String> tc = getSampleTree ();

        if (true)
        {
            final StringBuilder sb = new StringBuilder ();
            tc.recurse (s -> getTestString (sb, s), true);
            assertEquals ("/tc/0/00/1/10/11/12/120/121/122/2/20", sb.toString ());
        }

        System.out.println ();

        final StringBuilder sb = new StringBuilder ();
        tc.recurse (s -> getTestString (sb, s), false);
        assertEquals ("/00/0/10/11/120/121/122/12/1/20/2/tc", sb.toString ());
    }

    private TreeCollection<String> getSampleTree ()
    {
        final TreeCollection<String> n120 = new TreeCollection<String> ("120");
        final TreeCollection<String> n121 = new TreeCollection<String> ("121");
        final TreeCollection<String> n122 = new TreeCollection<String> ("122");

        final TreeCollection<String> n00 = new TreeCollection<String> ("00");
        final TreeCollection<String> n10 = new TreeCollection<String> ("10");
        final TreeCollection<String> n11 = new TreeCollection<String> ("11");
        final TreeCollection<String> n12 = new TreeCollection<String> ("12", n120, n121, n122);
        final TreeCollection<String> n20 = new TreeCollection<String> ("20");

        final TreeCollection<String> n0 = new TreeCollection<String> ("0", n00);
        final TreeCollection<String> n1 = new TreeCollection<String> ("1", n10, n11, n12);
        final TreeCollection<String> n2 = new TreeCollection<String> ("2", n20);

        return TreeCollection.of ("tc", n0, n1, n2);
    }

    private StringBuilder getTestString (final StringBuilder sb, final TreeCollection<String> s)
    {
        return sb.append (String.format ("/%s", s.getValue ()));
    }
}
