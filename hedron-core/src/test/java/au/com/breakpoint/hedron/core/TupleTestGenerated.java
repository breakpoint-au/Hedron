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
package au.com.breakpoint.hedron.core;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import au.com.breakpoint.hedron.core.Tuple.E1;
import au.com.breakpoint.hedron.core.Tuple.E10;
import au.com.breakpoint.hedron.core.Tuple.E11;
import au.com.breakpoint.hedron.core.Tuple.E12;
import au.com.breakpoint.hedron.core.Tuple.E13;
import au.com.breakpoint.hedron.core.Tuple.E14;
import au.com.breakpoint.hedron.core.Tuple.E15;
import au.com.breakpoint.hedron.core.Tuple.E16;
import au.com.breakpoint.hedron.core.Tuple.E17;
import au.com.breakpoint.hedron.core.Tuple.E18;
import au.com.breakpoint.hedron.core.Tuple.E19;
import au.com.breakpoint.hedron.core.Tuple.E2;
import au.com.breakpoint.hedron.core.Tuple.E20;
import au.com.breakpoint.hedron.core.Tuple.E3;
import au.com.breakpoint.hedron.core.Tuple.E4;
import au.com.breakpoint.hedron.core.Tuple.E5;
import au.com.breakpoint.hedron.core.Tuple.E6;
import au.com.breakpoint.hedron.core.Tuple.E7;
import au.com.breakpoint.hedron.core.Tuple.E8;
import au.com.breakpoint.hedron.core.Tuple.E9;

public class TupleTestGenerated
{
    @Test
    public void testE1 ()
    {
        final E1<String> p = E1.of ("0");
        assertEquals ("0", p.getE0 ());

        assertEquals (p, E1.of ("0"));
    }

    @Test
    public void testE10 ()
    {
        final E10<String, String, String, String, String, String, String, String, String, String> p =
            E10.of ("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
        assertEquals ("0", p.getE0 ());
        assertEquals ("1", p.getE1 ());
        assertEquals ("2", p.getE2 ());
        assertEquals ("3", p.getE3 ());
        assertEquals ("4", p.getE4 ());
        assertEquals ("5", p.getE5 ());
        assertEquals ("6", p.getE6 ());
        assertEquals ("7", p.getE7 ());
        assertEquals ("8", p.getE8 ());
        assertEquals ("9", p.getE9 ());

        assertEquals (p, E10.of ("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"));
    }

    @Test
    public void testE11 ()
    {
        final E11<String, String, String, String, String, String, String, String, String, String, String> p =
            E11.of ("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
        assertEquals ("0", p.getE0 ());
        assertEquals ("1", p.getE1 ());
        assertEquals ("2", p.getE2 ());
        assertEquals ("3", p.getE3 ());
        assertEquals ("4", p.getE4 ());
        assertEquals ("5", p.getE5 ());
        assertEquals ("6", p.getE6 ());
        assertEquals ("7", p.getE7 ());
        assertEquals ("8", p.getE8 ());
        assertEquals ("9", p.getE9 ());
        assertEquals ("10", p.getE10 ());

        assertEquals (p, E11.of ("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"));
    }

    @Test
    public void testE12 ()
    {
        final E12<String, String, String, String, String, String, String, String, String, String, String, String> p =
            E12.of ("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11");
        assertEquals ("0", p.getE0 ());
        assertEquals ("1", p.getE1 ());
        assertEquals ("2", p.getE2 ());
        assertEquals ("3", p.getE3 ());
        assertEquals ("4", p.getE4 ());
        assertEquals ("5", p.getE5 ());
        assertEquals ("6", p.getE6 ());
        assertEquals ("7", p.getE7 ());
        assertEquals ("8", p.getE8 ());
        assertEquals ("9", p.getE9 ());
        assertEquals ("10", p.getE10 ());
        assertEquals ("11", p.getE11 ());

        assertEquals (p, E12.of ("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"));
    }

    @Test
    public void testE13 ()
    {
        final E13<String, String, String, String, String, String, String, String, String, String, String, String, String> p =
            E13.of ("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12");
        assertEquals ("0", p.getE0 ());
        assertEquals ("1", p.getE1 ());
        assertEquals ("2", p.getE2 ());
        assertEquals ("3", p.getE3 ());
        assertEquals ("4", p.getE4 ());
        assertEquals ("5", p.getE5 ());
        assertEquals ("6", p.getE6 ());
        assertEquals ("7", p.getE7 ());
        assertEquals ("8", p.getE8 ());
        assertEquals ("9", p.getE9 ());
        assertEquals ("10", p.getE10 ());
        assertEquals ("11", p.getE11 ());
        assertEquals ("12", p.getE12 ());

        assertEquals (p, E13.of ("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"));
    }

    @Test
    public void testE14 ()
    {
        final E14<String, String, String, String, String, String, String, String, String, String, String, String, String, String> p =
            E14.of ("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13");
        assertEquals ("0", p.getE0 ());
        assertEquals ("1", p.getE1 ());
        assertEquals ("2", p.getE2 ());
        assertEquals ("3", p.getE3 ());
        assertEquals ("4", p.getE4 ());
        assertEquals ("5", p.getE5 ());
        assertEquals ("6", p.getE6 ());
        assertEquals ("7", p.getE7 ());
        assertEquals ("8", p.getE8 ());
        assertEquals ("9", p.getE9 ());
        assertEquals ("10", p.getE10 ());
        assertEquals ("11", p.getE11 ());
        assertEquals ("12", p.getE12 ());
        assertEquals ("13", p.getE13 ());

        assertEquals (p, E14.of ("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13"));
    }

    @Test
    public void testE15 ()
    {
        final E15<String, String, String, String, String, String, String, String, String, String, String, String, String, String, String> p =
            E15.of ("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14");
        assertEquals ("0", p.getE0 ());
        assertEquals ("1", p.getE1 ());
        assertEquals ("2", p.getE2 ());
        assertEquals ("3", p.getE3 ());
        assertEquals ("4", p.getE4 ());
        assertEquals ("5", p.getE5 ());
        assertEquals ("6", p.getE6 ());
        assertEquals ("7", p.getE7 ());
        assertEquals ("8", p.getE8 ());
        assertEquals ("9", p.getE9 ());
        assertEquals ("10", p.getE10 ());
        assertEquals ("11", p.getE11 ());
        assertEquals ("12", p.getE12 ());
        assertEquals ("13", p.getE13 ());
        assertEquals ("14", p.getE14 ());

        assertEquals (p, E15.of ("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14"));
    }

    @Test
    public void testE16 ()
    {
        final E16<String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String> p =
            E16.of ("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15");
        assertEquals ("0", p.getE0 ());
        assertEquals ("1", p.getE1 ());
        assertEquals ("2", p.getE2 ());
        assertEquals ("3", p.getE3 ());
        assertEquals ("4", p.getE4 ());
        assertEquals ("5", p.getE5 ());
        assertEquals ("6", p.getE6 ());
        assertEquals ("7", p.getE7 ());
        assertEquals ("8", p.getE8 ());
        assertEquals ("9", p.getE9 ());
        assertEquals ("10", p.getE10 ());
        assertEquals ("11", p.getE11 ());
        assertEquals ("12", p.getE12 ());
        assertEquals ("13", p.getE13 ());
        assertEquals ("14", p.getE14 ());
        assertEquals ("15", p.getE15 ());

        assertEquals (p, E16.of ("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15"));
    }

    @Test
    public void testE17 ()
    {
        final E17<String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String> p =
            E17.of ("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16");
        assertEquals ("0", p.getE0 ());
        assertEquals ("1", p.getE1 ());
        assertEquals ("2", p.getE2 ());
        assertEquals ("3", p.getE3 ());
        assertEquals ("4", p.getE4 ());
        assertEquals ("5", p.getE5 ());
        assertEquals ("6", p.getE6 ());
        assertEquals ("7", p.getE7 ());
        assertEquals ("8", p.getE8 ());
        assertEquals ("9", p.getE9 ());
        assertEquals ("10", p.getE10 ());
        assertEquals ("11", p.getE11 ());
        assertEquals ("12", p.getE12 ());
        assertEquals ("13", p.getE13 ());
        assertEquals ("14", p.getE14 ());
        assertEquals ("15", p.getE15 ());
        assertEquals ("16", p.getE16 ());

        assertEquals (p,
            E17.of ("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16"));
    }

    @Test
    public void testE18 ()
    {
        final E18<String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String> p =
            E18.of ("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17");
        assertEquals ("0", p.getE0 ());
        assertEquals ("1", p.getE1 ());
        assertEquals ("2", p.getE2 ());
        assertEquals ("3", p.getE3 ());
        assertEquals ("4", p.getE4 ());
        assertEquals ("5", p.getE5 ());
        assertEquals ("6", p.getE6 ());
        assertEquals ("7", p.getE7 ());
        assertEquals ("8", p.getE8 ());
        assertEquals ("9", p.getE9 ());
        assertEquals ("10", p.getE10 ());
        assertEquals ("11", p.getE11 ());
        assertEquals ("12", p.getE12 ());
        assertEquals ("13", p.getE13 ());
        assertEquals ("14", p.getE14 ());
        assertEquals ("15", p.getE15 ());
        assertEquals ("16", p.getE16 ());
        assertEquals ("17", p.getE17 ());

        assertEquals (p,
            E18.of ("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17"));
    }

    @Test
    public void testE19 ()
    {
        final E19<String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String> p =
            E19.of ("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17",
                "18");
        assertEquals ("0", p.getE0 ());
        assertEquals ("1", p.getE1 ());
        assertEquals ("2", p.getE2 ());
        assertEquals ("3", p.getE3 ());
        assertEquals ("4", p.getE4 ());
        assertEquals ("5", p.getE5 ());
        assertEquals ("6", p.getE6 ());
        assertEquals ("7", p.getE7 ());
        assertEquals ("8", p.getE8 ());
        assertEquals ("9", p.getE9 ());
        assertEquals ("10", p.getE10 ());
        assertEquals ("11", p.getE11 ());
        assertEquals ("12", p.getE12 ());
        assertEquals ("13", p.getE13 ());
        assertEquals ("14", p.getE14 ());
        assertEquals ("15", p.getE15 ());
        assertEquals ("16", p.getE16 ());
        assertEquals ("17", p.getE17 ());
        assertEquals ("18", p.getE18 ());

        assertEquals (p, E19.of ("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15",
            "16", "17", "18"));
    }

    @Test
    public void testE2 ()
    {
        final E2<String, String> p = E2.of ("0", "1");
        assertEquals ("0", p.getE0 ());
        assertEquals ("1", p.getE1 ());

        assertEquals (p, E2.of ("0", "1"));
    }

    @Test
    public void testE20 ()
    {
        final E20<String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String> p =
            E20.of ("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17",
                "18", "19");
        assertEquals ("0", p.getE0 ());
        assertEquals ("1", p.getE1 ());
        assertEquals ("2", p.getE2 ());
        assertEquals ("3", p.getE3 ());
        assertEquals ("4", p.getE4 ());
        assertEquals ("5", p.getE5 ());
        assertEquals ("6", p.getE6 ());
        assertEquals ("7", p.getE7 ());
        assertEquals ("8", p.getE8 ());
        assertEquals ("9", p.getE9 ());
        assertEquals ("10", p.getE10 ());
        assertEquals ("11", p.getE11 ());
        assertEquals ("12", p.getE12 ());
        assertEquals ("13", p.getE13 ());
        assertEquals ("14", p.getE14 ());
        assertEquals ("15", p.getE15 ());
        assertEquals ("16", p.getE16 ());
        assertEquals ("17", p.getE17 ());
        assertEquals ("18", p.getE18 ());
        assertEquals ("19", p.getE19 ());

        assertEquals (p, E20.of ("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15",
            "16", "17", "18", "19"));
    }

    @Test
    public void testE3 ()
    {
        final E3<String, String, String> p = E3.of ("0", "1", "2");
        assertEquals ("0", p.getE0 ());
        assertEquals ("1", p.getE1 ());
        assertEquals ("2", p.getE2 ());

        assertEquals (p, E3.of ("0", "1", "2"));
    }

    @Test
    public void testE4 ()
    {
        final E4<String, String, String, String> p = E4.of ("0", "1", "2", "3");
        assertEquals ("0", p.getE0 ());
        assertEquals ("1", p.getE1 ());
        assertEquals ("2", p.getE2 ());
        assertEquals ("3", p.getE3 ());

        assertEquals (p, E4.of ("0", "1", "2", "3"));
    }

    @Test
    public void testE5 ()
    {
        final E5<String, String, String, String, String> p = E5.of ("0", "1", "2", "3", "4");
        assertEquals ("0", p.getE0 ());
        assertEquals ("1", p.getE1 ());
        assertEquals ("2", p.getE2 ());
        assertEquals ("3", p.getE3 ());
        assertEquals ("4", p.getE4 ());

        assertEquals (p, E5.of ("0", "1", "2", "3", "4"));
    }

    @Test
    public void testE6 ()
    {
        final E6<String, String, String, String, String, String> p = E6.of ("0", "1", "2", "3", "4", "5");
        assertEquals ("0", p.getE0 ());
        assertEquals ("1", p.getE1 ());
        assertEquals ("2", p.getE2 ());
        assertEquals ("3", p.getE3 ());
        assertEquals ("4", p.getE4 ());
        assertEquals ("5", p.getE5 ());

        assertEquals (p, E6.of ("0", "1", "2", "3", "4", "5"));
    }

    @Test
    public void testE7 ()
    {
        final E7<String, String, String, String, String, String, String> p = E7.of ("0", "1", "2", "3", "4", "5", "6");
        assertEquals ("0", p.getE0 ());
        assertEquals ("1", p.getE1 ());
        assertEquals ("2", p.getE2 ());
        assertEquals ("3", p.getE3 ());
        assertEquals ("4", p.getE4 ());
        assertEquals ("5", p.getE5 ());
        assertEquals ("6", p.getE6 ());

        assertEquals (p, E7.of ("0", "1", "2", "3", "4", "5", "6"));
    }

    @Test
    public void testE8 ()
    {
        final E8<String, String, String, String, String, String, String, String> p =
            E8.of ("0", "1", "2", "3", "4", "5", "6", "7");
        assertEquals ("0", p.getE0 ());
        assertEquals ("1", p.getE1 ());
        assertEquals ("2", p.getE2 ());
        assertEquals ("3", p.getE3 ());
        assertEquals ("4", p.getE4 ());
        assertEquals ("5", p.getE5 ());
        assertEquals ("6", p.getE6 ());
        assertEquals ("7", p.getE7 ());

        assertEquals (p, E8.of ("0", "1", "2", "3", "4", "5", "6", "7"));
    }

    @Test
    public void testE9 ()
    {
        final E9<String, String, String, String, String, String, String, String, String> p =
            E9.of ("0", "1", "2", "3", "4", "5", "6", "7", "8");
        assertEquals ("0", p.getE0 ());
        assertEquals ("1", p.getE1 ());
        assertEquals ("2", p.getE2 ());
        assertEquals ("3", p.getE3 ());
        assertEquals ("4", p.getE4 ());
        assertEquals ("5", p.getE5 ());
        assertEquals ("6", p.getE6 ());
        assertEquals ("7", p.getE7 ());
        assertEquals ("8", p.getE8 ());

        assertEquals (p, E9.of ("0", "1", "2", "3", "4", "5", "6", "7", "8"));
    }
}
