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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.junit.Test;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.GenericFactory;
import au.com.breakpoint.hedron.core.IIdentifiable;
import au.com.breakpoint.hedron.core.KeyValuePair;
import au.com.breakpoint.hedron.core.Tuple.E2;
import au.com.breakpoint.hedron.core.Tuple.E3;
import au.com.breakpoint.hedron.core.context.AssertException;
import au.com.breakpoint.hedron.core.context.ExecutionScope;
import au.com.breakpoint.hedron.core.context.FaultException;
import au.com.breakpoint.hedron.core.context.IScope;
import au.com.breakpoint.hedron.core.context.LoggingSilenceScope;
import au.com.breakpoint.hedron.core.context.OpResult;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.core.context.OpResult.Severity;

public class HgUtilTest
{
    //@Test
    //public void test_getStackTrace ()
    //{
    //    final Exception e2 = new Exception ("e2");
    //    final Exception e1 = new Exception (e2);
    //    final RuntimeException e0 = new RuntimeException (e1);
    //
    //    final String s = HcUtil.getStackTrace (e0);
    //    System.out.println (s);
    //}
    @Test
    public void test_coalesce ()
    {
        assertEquals ((Object) null, HcUtil.coalesce ((Object) null));

        final String s = HcUtil.coalesce ("a");
        assertEquals ("a", s);

        assertEquals ("a", HcUtil.coalesce (null, null, "a"));
        assertEquals ("a", HcUtil.coalesce (null, "a", null));
    }

    @Test
    public void test_coalesce_supplier ()
    {
        final Supplier<String> ns = () -> null;
        final Supplier<String> as = () -> "a";

        assertEquals (null, HcUtil.coalesce (ns));
        assertEquals ("a", HcUtil.coalesce (HcUtil.coalesce (as)));
        assertEquals ("a", HcUtil.coalesce (ns, ns, as));
        assertEquals ("a", HcUtil.coalesce (ns, as, ns));
    }

    @Test
    public void test_executeConcurrently ()
    {
        final List<Callable<String>> tasks = GenericFactory.<Callable<String>> newArrayList ( () ->
        {
            HcUtil.pause (100);
            return "100";
        } , () ->
        {
            HcUtil.pause (40);
            return "40";
        });

        final List<String> resultInCompletionOrder = HcUtil.executeConcurrently (tasks, 2, true);
        System.out.printf ("result %s%n", resultInCompletionOrder);
        assertArrayEquals (new String[]
        {
                "40",
                "100"
        }, resultInCompletionOrder.toArray (new String[resultInCompletionOrder.size ()]));

        final List<String> resultNotCompletionOrder = HcUtil.executeConcurrently (tasks, 2, false);
        System.out.printf ("result %s%n", resultNotCompletionOrder);
        assertArrayEquals (new String[]
        {
                "100",
                "40"
        }, resultNotCompletionOrder.toArray (new String[resultNotCompletionOrder.size ()]));
    }

    @Test
    public void test_executeConcurrentlyForkJoin ()
    {
        final List<Integer> inputs = asList (1, 2, 3, 4, 5, 6, 7, 8, 9);

        final List<String> results = HcUtil.executeConcurrently (inputs, a -> String.valueOf (a));
        System.out.printf ("results %s%n", results);
    }

    @Test
    public void test_findAncestralDirectory ()
    {
        if (true)
        {
            final String d = "c:/aaa/bbb/cccccccccccc/d/ee/f";

            assertEquals ("c:/aaa/bbb/cccccccccccc/d/ee/f", HcUtil.findAncestralDirectory (d, "f"));
            assertEquals ("c:/aaa/bbb/cccccccccccc/d/ee", HcUtil.findAncestralDirectory (d, "ee"));
            assertEquals (null, HcUtil.findAncestralDirectory (d, "e"));
            assertEquals ("c:/aaa/bbb/cccccccccccc/d", HcUtil.findAncestralDirectory (d, "d"));
            assertEquals (null, HcUtil.findAncestralDirectory (d, ""));
            assertEquals ("c:/aaa/bbb/cccccccccccc", HcUtil.findAncestralDirectory (d, "cccccccccccc"));
            assertEquals (null, HcUtil.findAncestralDirectory (d, "ccccccccccc"));
            assertEquals ("c:/aaa/bbb", HcUtil.findAncestralDirectory (d, "bbb"));
            assertEquals (null, HcUtil.findAncestralDirectory (d, "bb"));
            assertEquals ("c:/aaa", HcUtil.findAncestralDirectory (d, "aaa"));
            assertEquals (null, HcUtil.findAncestralDirectory (d, "aa"));
            assertEquals (null, HcUtil.findAncestralDirectory (d, "asdf"));
        }

        if (true)
        {
            final String d = "/aaa/bbb/cccccccccccc/d/ee/f";

            assertEquals ("/aaa/bbb/cccccccccccc/d/ee/f", HcUtil.findAncestralDirectory (d, "f"));
            assertEquals ("/aaa/bbb/cccccccccccc/d/ee", HcUtil.findAncestralDirectory (d, "ee"));
            assertEquals (null, HcUtil.findAncestralDirectory (d, "e"));
            assertEquals ("/aaa/bbb/cccccccccccc/d", HcUtil.findAncestralDirectory (d, "d"));
            assertEquals (null, HcUtil.findAncestralDirectory (d, ""));
            assertEquals ("/aaa/bbb/cccccccccccc", HcUtil.findAncestralDirectory (d, "cccccccccccc"));
            assertEquals (null, HcUtil.findAncestralDirectory (d, "ccccccccccc"));
            assertEquals ("/aaa/bbb", HcUtil.findAncestralDirectory (d, "bbb"));
            assertEquals (null, HcUtil.findAncestralDirectory (d, "bb"));
            assertEquals ("/aaa", HcUtil.findAncestralDirectory (d, "aaa"));
            assertEquals (null, HcUtil.findAncestralDirectory (d, "aa"));
            assertEquals (null, HcUtil.findAncestralDirectory (d, "asdf"));
        }
    }

    @Test
    public void test_getStandardSubstitutions ()
    {
        final String hostName = "anneHost";
        HcUtil.setThisHost (hostName);

        final List<E2<String, String>> ss = HcUtil.getStandardSubstitutions ();
        assertEquals (2, ss.size ());
        assertEquals (E2.of ("${hostName}", hostName), ss.get (0));
        //assertEquals (E2.newInstance ("${driveName}", "B:"), ss.get (1));
    }

    @Test
    public void test_safeApply ()
    {
        assertEquals (null, HcUtil.safeApply (null, String::length));
        assertEquals (null, HcUtil.safeApply ("12", HgUtilTest::getNullString));
        assertEquals (Integer.valueOf (2), HcUtil.safeApply ("12", String::length));

        assertEquals (null, HcUtil.safeApply (null, String::length, HgUtilTest::bitCount));
        assertEquals (null, HcUtil.safeApply ("123", HgUtilTest::getNullInteger, HgUtilTest::bitCount));
        assertEquals (null, HcUtil.safeApply ("123", String::length, HgUtilTest::getNullString));
        assertEquals (Integer.valueOf (2), HcUtil.safeApply ("123", String::length, HgUtilTest::bitCount));
        assertEquals (Integer.valueOf (1), HcUtil.safeApply ("1234", String::length, HgUtilTest::bitCount));

        assertEquals (null, HcUtil.safeApply (null, String::length, HgUtilTest::bitCount, Object::toString));
        assertEquals (null,
            HcUtil.safeApply ("1234", HgUtilTest::getNullInteger, HgUtilTest::bitCount, Object::toString));
        assertEquals (null, HcUtil.safeApply ("1234", String::length, HgUtilTest::getNullInteger, Object::toString));
        assertEquals (null, HcUtil.safeApply ("1234", String::length, HgUtilTest::bitCount, HgUtilTest::getNullString));
        assertEquals ("1", HcUtil.safeApply ("1234", String::length, HgUtilTest::bitCount, Object::toString));

        assertEquals (null,
            HcUtil.safeApply (null, String::length, HgUtilTest::bitCount, Object::toString, String::length));
        assertEquals (null, HcUtil.safeApply ("1234", HgUtilTest::getNullInteger, HgUtilTest::bitCount,
            Object::toString, String::length));
        assertEquals (null,
            HcUtil.safeApply ("1234", String::length, HgUtilTest::getNullInteger, Object::toString, String::length));
        assertEquals (null,
            HcUtil.safeApply ("1234", String::length, HgUtilTest::bitCount, HgUtilTest::getNullString, String::length));
        assertEquals (null, HcUtil.safeApply ("1234", String::length, HgUtilTest::bitCount, Object::toString,
            HgUtilTest::getNullString));
        assertEquals (Integer.valueOf (1),
            HcUtil.safeApply ("1234", String::length, HgUtilTest::bitCount, Object::toString, String::length));
    }

    @Test
    public void testAbbreviate ()
    {
        //final Callable<Runnable> c = () -> () -> System.out.println ();

        assertEquals ((String) null, HcUtil.abbreviate ((String) null, 20));
        assertEquals ("", HcUtil.abbreviate ("", 20));
        assertEquals ("0", HcUtil.abbreviate ("0", 20));
        assertEquals ("01", HcUtil.abbreviate ("01", 20));
        assertSelf (10, 20);
        assertSelf (18, 20);
        assertSelf (19, 20);
        assertSelf (20, 20);

        assertEquals ("0123...7890", HcUtil.abbreviate (formString (11), 10));
        assertEquals ("012...890", HcUtil.abbreviate (formString (11), 9));

        assertEquals ("0123...8901", HcUtil.abbreviate (formString (12), 10));
        assertEquals ("012...901", HcUtil.abbreviate (formString (12), 9));

        final String s10 = "0123...9012";
        assertEquals (s10, HcUtil.abbreviate (formString (13), 11));
        assertEquals (s10, HcUtil.abbreviate (formString (13), 10));

        assertEquals (s10, HcUtil.abbreviate (formString (113), 11));
        assertEquals (s10, HcUtil.abbreviate (formString (113), 10));

        assertEquals (s10, HcUtil.abbreviate (formString (113), 11));
        assertEquals (s10, HcUtil.abbreviate (formString (113), 10));

        assertEquals (s10, HcUtil.abbreviate (formString (513), 11));
        assertEquals (s10, HcUtil.abbreviate (formString (513), 10));

        assertEquals (s10, HcUtil.abbreviate (formString (1013), 11));
        assertEquals (s10, HcUtil.abbreviate (formString (1013), 10));

        final String s10000 = formString (10013);
        if (true)
        {
            assertEquals (s10, HcUtil.abbreviate (s10000, 11));
            assertEquals (s10, HcUtil.abbreviate (s10000, 10));
        }

        if (true)
        {
            final String expected = formString (50) + "0123...9" + formString (50) + "012";
            assertEquals (expected, HcUtil.abbreviate (s10000, 111));
            assertEquals (expected, HcUtil.abbreviate (s10000, 110));
        }

        if (true)
        {
            final String expected = formString (250) + "0123...9" + formString (250) + "012";
            assertEquals (expected, HcUtil.abbreviate (s10000, 511));
            assertEquals (expected, HcUtil.abbreviate (s10000, 510));
        }
    }

    @Test
    public void testAnalyseDifferences ()
    {
        final TestEntity c1 = new TestEntity ();
        c1.setId (1);
        c1.setName ("Entity 1");

        final TestEntity c2 = new TestEntity ();
        c2.setId (2);
        c2.setName ("Entity 2");

        final TestEntity c3 = new TestEntity ();
        c3.setId (3);
        c3.setName ("Entity 3");
        final TestEntity c3new = new TestEntity ();
        c3new.setId (3);
        c3new.setName ("Entity 3new");

        final TestEntity c4 = new TestEntity ();
        c4.setId (4);
        c4.setName ("Entity 4");

        final TestEntity c5 = new TestEntity ();
        c5.setId (5);
        c5.setName ("Entity 5");

        final List<TestEntity> before = GenericFactory.newArrayList (c1, c2, c3);

        final TestEntity c2Clone = new TestEntity (c2);
        final List<TestEntity> after = GenericFactory.newArrayList (c2Clone, c3new, c4, c5);

        final E3<List<TestEntity>, List<TestEntity>, List<TestEntity>> result =
            HcUtil.analyseDifferences (before, after);

        final List<TestEntity> listInsert = result.getE0 ();
        assertEquals (2, listInsert.size ());
        assertTrue (listInsert.contains (c4));
        assertTrue (listInsert.contains (c5));

        final List<TestEntity> listUpdate = result.getE1 ();
        assertEquals (1, listUpdate.size ());
        assertTrue (listUpdate.contains (c3new));

        final List<TestEntity> listDelete = result.getE2 ();
        assertEquals (1, listDelete.size ());
        assertTrue (listDelete.contains (c1));
    }

    @Test
    public void testAnalyseDifferencesListList ()
    {
        final KeyValuePair<String, BigInteger> before_e_0_0 = KeyValuePair.of ("k00", BigInteger.valueOf (0));
        final KeyValuePair<String, BigInteger> after_e_0_0 = KeyValuePair.of ("k00", BigInteger.valueOf (100_0));
        final KeyValuePair<String, BigInteger> e_0_1 = KeyValuePair.of ("k01", BigInteger.valueOf (1));

        final KeyValuePair<String, BigInteger> e_1 = KeyValuePair.of ("k1", BigInteger.valueOf (2));

        final KeyValuePair<String, BigInteger> before_e_2 = KeyValuePair.of ("k2", BigInteger.valueOf (3));
        final KeyValuePair<String, BigInteger> after_e_2 = KeyValuePair.of ("k2", BigInteger.valueOf (100_3));

        final KeyValuePair<String, BigInteger> e_3_0 = KeyValuePair.of ("k30", BigInteger.valueOf (30));
        final KeyValuePair<String, BigInteger> before_e_3_1 = KeyValuePair.of ("k31", BigInteger.valueOf (31));
        final KeyValuePair<String, BigInteger> after_e_3_1 = KeyValuePair.of ("k31", BigInteger.valueOf (100_31));
        final KeyValuePair<String, BigInteger> e_3_2 = KeyValuePair.of ("k32", BigInteger.valueOf (32));

        final KeyValuePair<String, BigInteger> e_4_0 = KeyValuePair.of ("k40", BigInteger.valueOf (40));
        final KeyValuePair<String, BigInteger> e_4_1 = KeyValuePair.of ("k41", BigInteger.valueOf (41));

        final List<KeyValuePair<String, BigInteger>> before_l_0 = GenericFactory.newArrayList (before_e_0_0, e_0_1);
        final List<KeyValuePair<String, BigInteger>> after_l_0 = GenericFactory.newArrayList (after_e_0_0, e_0_1);

        final List<KeyValuePair<String, BigInteger>> l_1 = GenericFactory.newArrayList (e_1);

        final List<KeyValuePair<String, BigInteger>> before_l_2 = GenericFactory.newArrayList (before_e_2);
        final List<KeyValuePair<String, BigInteger>> after_l_2 = GenericFactory.newArrayList (after_e_2);

        final List<KeyValuePair<String, BigInteger>> before_l_3 = GenericFactory.newArrayList (e_3_0, before_e_3_1);
        final List<KeyValuePair<String, BigInteger>> after_l_3 = GenericFactory.newArrayList (e_3_0, after_e_3_1);
        final List<KeyValuePair<String, BigInteger>> after_more_l_3 = GenericFactory.newArrayList (e_3_0, e_3_2);

        final List<KeyValuePair<String, BigInteger>> l_4 = GenericFactory.newArrayList (e_4_0, e_4_1);

        // k0 has nested k00 and k01
        final KeyValuePair<String, List<KeyValuePair<String, BigInteger>>> before_f_0 =
            KeyValuePair.of ("k0", before_l_0);
        final KeyValuePair<String, List<KeyValuePair<String, BigInteger>>> after_f_0 =
            KeyValuePair.of ("k0", after_l_0);

        final KeyValuePair<String, List<KeyValuePair<String, BigInteger>>> f_1 = KeyValuePair.of ("k1", l_1);

        final KeyValuePair<String, List<KeyValuePair<String, BigInteger>>> before_f_2 =
            KeyValuePair.of ("k2", before_l_2);
        final KeyValuePair<String, List<KeyValuePair<String, BigInteger>>> after_f_2 =
            KeyValuePair.of ("k2", after_l_2);
        // k3 nested k3 and k31
        final KeyValuePair<String, List<KeyValuePair<String, BigInteger>>> before_f_3 =
            KeyValuePair.of ("k3", before_l_3);
        final KeyValuePair<String, List<KeyValuePair<String, BigInteger>>> after_f_3 =
            KeyValuePair.of ("k3", after_l_3);
        final KeyValuePair<String, List<KeyValuePair<String, BigInteger>>> after_more_f_3 =
            KeyValuePair.of ("k3", after_more_l_3);

        final KeyValuePair<String, List<KeyValuePair<String, BigInteger>>> f_4 = KeyValuePair.of ("k4", l_4);

        assertTrue (GenericFactory.newArrayList (before_f_0, f_1, before_f_2, before_f_3)
            .equals (GenericFactory.newArrayList (before_f_0, f_1, before_f_2, before_f_3)));
        assertTrue (!GenericFactory.newArrayList (before_f_0, f_1, before_f_2, before_f_3)
            .equals (GenericFactory.newArrayList (before_f_0, f_1, after_f_2, before_f_3)));

        if (true)
        {
            final E3<List<KeyValuePair<String, List<KeyValuePair<String, BigInteger>>>>, List<KeyValuePair<String, List<KeyValuePair<String, BigInteger>>>>, List<KeyValuePair<String, List<KeyValuePair<String, BigInteger>>>>> result =
                HcUtil.analyseDifferences (GenericFactory.newArrayList (before_f_0, f_1, before_f_2, before_f_3),
                    GenericFactory.newArrayList (before_f_0, f_1, before_f_2, before_f_3));

            final List<KeyValuePair<String, List<KeyValuePair<String, BigInteger>>>> listInsert = result.getE0 ();
            assertEquals (0, listInsert.size ());

            final List<KeyValuePair<String, List<KeyValuePair<String, BigInteger>>>> listUpdate = result.getE1 ();
            assertEquals (0, listUpdate.size ());

            final List<KeyValuePair<String, List<KeyValuePair<String, BigInteger>>>> listDelete = result.getE2 ();
            assertEquals (0, listDelete.size ());
        }

        if (true)
        {
            final E3<List<KeyValuePair<String, List<KeyValuePair<String, BigInteger>>>>, List<KeyValuePair<String, List<KeyValuePair<String, BigInteger>>>>, List<KeyValuePair<String, List<KeyValuePair<String, BigInteger>>>>> result =
                HcUtil.analyseDifferences (GenericFactory.newArrayList (before_f_0, f_1, before_f_2, before_f_3),
                    GenericFactory.newArrayList (after_f_0, f_1, after_f_2, after_f_3, f_4));

            final List<KeyValuePair<String, List<KeyValuePair<String, BigInteger>>>> listInsert = result.getE0 ();
            assertEquals (1, listInsert.size ());
            assertTrue (listInsert.contains (f_4));

            final List<KeyValuePair<String, List<KeyValuePair<String, BigInteger>>>> listUpdate = result.getE1 ();
            assertEquals (3, listUpdate.size ());
            assertTrue (listUpdate.contains (after_f_0));
            assertTrue (listUpdate.contains (after_f_2));
            assertTrue (listUpdate.contains (after_f_3));

            final List<KeyValuePair<String, List<KeyValuePair<String, BigInteger>>>> listDelete = result.getE2 ();
            assertEquals (0, listDelete.size ());
        }

        if (true)
        {
            final E3<List<KeyValuePair<String, List<KeyValuePair<String, BigInteger>>>>, List<KeyValuePair<String, List<KeyValuePair<String, BigInteger>>>>, List<KeyValuePair<String, List<KeyValuePair<String, BigInteger>>>>> result =
                HcUtil.analyseDifferences (GenericFactory.newArrayList (before_f_0, f_1, before_f_2, before_f_3),
                    GenericFactory.newArrayList (f_4));

            final List<KeyValuePair<String, List<KeyValuePair<String, BigInteger>>>> listInsert = result.getE0 ();
            assertEquals (1, listInsert.size ());
            assertTrue (listInsert.contains (f_4));

            final List<KeyValuePair<String, List<KeyValuePair<String, BigInteger>>>> listUpdate = result.getE1 ();
            assertEquals (0, listUpdate.size ());

            final List<KeyValuePair<String, List<KeyValuePair<String, BigInteger>>>> listDelete = result.getE2 ();
            assertEquals (4, listDelete.size ());
            assertTrue (listDelete.contains (f_1));
            assertTrue (listDelete.contains (before_f_2));
            assertTrue (listDelete.contains (before_f_3));
        }

        if (true)
        {
            final E3<List<KeyValuePair<String, List<KeyValuePair<String, BigInteger>>>>, List<KeyValuePair<String, List<KeyValuePair<String, BigInteger>>>>, List<KeyValuePair<String, List<KeyValuePair<String, BigInteger>>>>> result =
                HcUtil.analyseDifferences (GenericFactory.newArrayList (after_f_3),
                    GenericFactory.newArrayList (after_more_f_3));

            final List<KeyValuePair<String, List<KeyValuePair<String, BigInteger>>>> listInsert = result.getE0 ();
            assertEquals (0, listInsert.size ());

            final List<KeyValuePair<String, List<KeyValuePair<String, BigInteger>>>> listUpdate = result.getE1 ();
            assertEquals (1, listUpdate.size ());
            assertTrue (listUpdate.contains (after_more_f_3));

            final List<KeyValuePair<String, List<KeyValuePair<String, BigInteger>>>> listDelete = result.getE2 ();
            assertEquals (0, listDelete.size ());
        }
    }

    @Test
    public void testAscendingDescendingComparators ()
    {
        final Function<CompTestEntity, String> extractorS1 = a -> a.m_s1;
        final Function<CompTestEntity, Integer> extractorI = a -> a.m_i;
        final Function<CompTestEntity, String> extractorS2 = a -> a.m_s2;
        final CompTestEntity aa10zz = new CompTestEntity ("aa", 10, "zz");
        final CompTestEntity aa11zz = new CompTestEntity ("aa", 11, "zz");
        final CompTestEntity aa12zz = new CompTestEntity ("aa", 12, "zz");
        final CompTestEntity ab12zy = new CompTestEntity ("ab", 12, "zy");
        final CompTestEntity ac15zx = new CompTestEntity ("ac", 15, "zx");
        final CompTestEntity ad14zw = new CompTestEntity ("ad", 14, "zw");
        final CompTestEntity ad13zw = new CompTestEntity ("ad", 13, "zw");
        final CompTestEntity ad12zw = new CompTestEntity ("ad", 12, "zw");
        final CompTestEntity ad11zw = new CompTestEntity ("ad", 11, "zw");

        checkComparator (new CompTestEntity[]
        {
                aa10zz,
                ab12zy,
                ac15zx,
                ad14zw,
        }, extractorS1);

        checkComparator (new CompTestEntity[]
        {
                aa10zz,
                aa11zz,
                aa12zz,
                ab12zy,
                ac15zx,
                ad14zw,
        }, extractorS1, extractorI);

        checkComparator (new CompTestEntity[]
        {
                aa10zz,
                ab12zy,
                ac15zx,
                ad14zw,
        }, extractorS1, extractorS2);

        checkComparator (new CompTestEntity[]
        {
                ad14zw,
                ac15zx,
                ab12zy,
                aa10zz,
        }, extractorS2, extractorS1);

        checkComparator (new CompTestEntity[]
        {
                ad11zw,
                ad12zw,
                ad13zw,
                ad14zw,
                ac15zx,
                ab12zy,
                aa10zz,
        }, extractorS2, extractorI);
    }

    @Test
    public void testcaseBreakToUnderScoreBreak ()
    {
        assertEquals ("ABC_TELEVISION", HcUtil.caseBreakToUnderScoreBreak ("abcTelevision"));
        assertEquals ("ABC_TELEVISION", HcUtil.caseBreakToUnderScoreBreak ("AbcTelevision"));
        assertEquals ("ABC_TELEVISION", HcUtil.caseBreakToUnderScoreBreak ("ABCTelevision"));
        assertEquals ("ABC_TELEVISION_T", HcUtil.caseBreakToUnderScoreBreak ("ABCTelevisionT"));
        assertEquals ("THE_ABC_TELEVISION", HcUtil.caseBreakToUnderScoreBreak ("TheABCTelevision"));
        assertEquals ("ABC_TELE_VISION", HcUtil.caseBreakToUnderScoreBreak ("ABCTeleVISION"));
        assertEquals ("VISION", HcUtil.caseBreakToUnderScoreBreak ("VISION"));
        assertEquals ("VISIO_NT", HcUtil.caseBreakToUnderScoreBreak ("VISIONt"));
        assertEquals ("BRANCH4", HcUtil.caseBreakToUnderScoreBreak ("Branch4"));
    }

    @Test
    public void testCompareProperties ()
    {
        final Source lhs = setupSource ();
        final Source rhs = setupSource ();
        assertTrue (HcUtil.compareProperties (lhs, lhs, false).size () == 0);
        assertTrue (HcUtil.compareProperties (lhs, lhs, true).size () == 0);
        assertTrue (HcUtil.compareProperties (lhs, rhs, false).size () == 0);
        assertTrue (HcUtil.compareProperties (lhs, rhs, true).size () == 0);

        rhs.setInt (rhs.getInt () + 1);
        final List<E3<String, Object, Object>> diffs = HcUtil.compareProperties (lhs, rhs, true);
        assertTrue (diffs.size () == 1);
        final E3<String, Object, Object> diff = diffs.get (0);
        assertEquals ("int", diff.getE0 ());
        assertEquals (lhs.getInt (), diff.getE1 ());
        assertEquals (rhs.getInt (), diff.getE2 ());
    }

    @Test
    public void testContainsByteIntrinsic ()
    {
        final byte[] a =
            {
                    12,
                    45,
                    78
        };
        assertTrue (HcUtil.contains (a, (byte) 12));
        assertTrue (HcUtil.contains (a, (byte) 45));
        assertTrue (HcUtil.contains (a, (byte) 78));
        assertTrue (!HcUtil.contains (a, (byte) 71));
        assertTrue (!HcUtil.contains (a, (byte) 7));
    }

    @Test
    public void testContainsDoubleIntrinsic ()
    {
        final double[] a =
            {
                    123,
                    4567,
                    7890
        };
        assertTrue (HcUtil.contains (a, 123));
        assertTrue (HcUtil.contains (a, 4567));
        assertTrue (HcUtil.contains (a, 7890));
        assertTrue (!HcUtil.contains (a, 7891));
        assertTrue (!HcUtil.contains (a, 7));
    }

    @Test
    public void testContainsFloatIntrinsic ()
    {
        final float[] a =
            {
                    123,
                    4567,
                    7890
        };
        assertTrue (HcUtil.contains (a, 123));
        assertTrue (HcUtil.contains (a, 4567));
        assertTrue (HcUtil.contains (a, 7890));
        assertTrue (!HcUtil.contains (a, 7891));
        assertTrue (!HcUtil.contains (a, 7));
    }

    @Test
    public void testContainsIntegerObject ()
    {
        final Integer[] a =
            {
                    123,
                    4567,
                    7890
        };
        assertTrue (HcUtil.contains (a, 123));
        assertTrue (HcUtil.contains (a, 4567));
        assertTrue (HcUtil.contains (a, 7890));
        assertTrue (!HcUtil.contains (a, 7891));
        assertTrue (!HcUtil.contains (a, 7));
    }

    @Test
    public void testContainsIntIntrinsic ()
    {
        final int[] a =
            {
                    123,
                    4567,
                    7890
        };
        assertTrue (HcUtil.contains (a, 123));
        assertTrue (HcUtil.contains (a, 4567));
        assertTrue (HcUtil.contains (a, 7890));
        assertTrue (!HcUtil.contains (a, 7891));
        assertTrue (!HcUtil.contains (a, 7));
    }

    @Test
    public void testContainsShortIntrinsic ()
    {
        final short[] a =
            {
                    123,
                    4567,
                    7890
        };
        assertTrue (HcUtil.contains (a, (short) 123));
        assertTrue (HcUtil.contains (a, (short) 4567));
        assertTrue (HcUtil.contains (a, (short) 7890));
        assertTrue (!HcUtil.contains (a, (short) 7891));
        assertTrue (!HcUtil.contains (a, (short) 7));
    }

    @Test
    public void testContainsString ()
    {
        final String[] a =
            {
                    "aa",
                    "ab",
                    "ac"
        };
        assertTrue (HcUtil.contains (a, "aa"));
        assertTrue (HcUtil.contains (a, "ab"));
        assertTrue (HcUtil.contains (a, "ac"));
        assertTrue (!HcUtil.contains (a, "ad"));
        assertTrue (!HcUtil.contains (a, "ae"));
    }

    @Test
    public void testCountLeadingSpaces ()
    {
        assertEquals (0, HcUtil.countLeadingSpaces (""));
        assertEquals (0, HcUtil.countLeadingSpaces ("a"));
        assertEquals (0, HcUtil.countLeadingSpaces ("aa"));

        assertEquals (1, HcUtil.countLeadingSpaces (" aa"));
        assertEquals (2, HcUtil.countLeadingSpaces ("  aa"));
    }

    @Test
    public void testDayRange ()
    {
        assertTrue (HcUtil.inDateTimeRange (DT_20110520_102030, new GregorianCalendar (2011, 5 - 1, 20, 0, 0, 0),
            new GregorianCalendar (2011, 5 - 1, 21, 0, 0, 0)));
    }

    @Test
    public void testDuplicate ()
    {
        final Timestamp ts = new Timestamp (34567);
        ts.setNanos (868686);
        assertEquals (ts, ts.clone ());
        assertEquals (ts, HcUtil.duplicate (ts));

        final byte[] ba =
            {
                    1,
                    2,
                    3,
                    4
        };
        final byte[] duplicate = HcUtil.duplicate (ba);
        assertFalse (ba == duplicate);
        assertArrayEquals (ba, duplicate);

        final byte[] clone = ba.clone ();
        assertFalse (ba == clone);
        assertArrayEquals (ba, clone);
    }

    @Test
    public void testExists1 ()
    {
        assertTrue (HcUtil.exists (asList (2, 3, 4, 1, 5), 1));
        assertTrue (HcUtil.exists (asList (1, 2, 3, 4, 5), 1));
        assertTrue (HcUtil.exists (asList (2, 3, 4, 5, 1), 1));
        assertTrue (HcUtil.exists (asList (1), 1));
    }

    @Test
    public void testExists2 ()
    {
        assertTrue (!HcUtil.exists (asList (2, 3, 4, 5), 1));
        assertTrue (!HcUtil.exists (asList (2), 1));
    }

    @Test
    public void testExists3 ()
    {
        assertTrue (HcUtil.exists (asList ("2", "3", "4", "1", "5"), "1"));
        assertTrue (HcUtil.exists (asList ("1", "2", "3", "4", "5"), "1"));
        assertTrue (HcUtil.exists (asList ("2", "3", "4", "5", "1"), "1"));
        assertTrue (HcUtil.exists (asList ("1"), "1"));
    }

    @Test
    public void testExists4 ()
    {
        assertTrue (!HcUtil.exists (asList ("2", "3", "4", "5"), "1"));
        assertTrue (!HcUtil.exists (asList ("2"), "1"));
    }

    @Test
    public void testExists5 ()
    {
        assertTrue (!HcUtil.exists (asList ("2", "A", "4", "5"), "a"));
        final String[] values =
            {
                    "2",
                    "A",
                    "4",
                    "5"
        };

        final List<String> list = Arrays.asList (values);// outlined to help javac bug
        final Predicate<? super String> criterion = v -> v.equalsIgnoreCase ("a");// outlined to help javac bug
        assertTrue (HcUtil.exists (list, criterion));
    }

    @Test
    public void testExtractVersionString ()
    {
        assertEquals ("SomeApp 9.0#35",
            HcUtil.extractVersionString ("SomeApp 9.0#35 (20/05/2013 17:50) by Breakpoint Pty Limited"));
        assertEquals ("SomeApp 9.0#3",
            HcUtil.extractVersionString ("SomeApp 9.0#3 (20/05/2013 17:50) by Breakpoint Pty Limited"));
        assertEquals ("TLS9.0#35",
            HcUtil.extractVersionString ("TLS9.0#35 (20/05/2013 17:50) by Breakpoint Pty Limited"));
        assertEquals ("0#35", HcUtil.extractVersionString ("0#35 (20/05/2013 17:50) by Breakpoint Pty Limited"));
        assertEquals ("#35", HcUtil.extractVersionString ("#35 (20/05/2013 17:50) by Breakpoint Pty Limited"));
    }

    @Test
    public void testFilter1 ()
    {
        final List<String> list = GenericFactory.newArrayList ("", "a", "aa", "aaa", "bb", "b");
        final List<String> filtered = HcUtil.filter (list, a -> a.length () == 1);
        assertTrue (filtered.size () == 2);
        assertEquals ("a", filtered.get (0));
        assertEquals ("b", filtered.get (1));
    }

    @Test
    public void testFilter2 ()
    {
        final List<Integer> list = GenericFactory.newArrayList (1, 2, 3, 4, 5, 6);
        final List<Integer> filtered = HcUtil.filter (list, a -> a % 2 == 0);
        assertTrue (filtered.size () == 3);
        assertEquals ((Integer) 2, filtered.get (0));
        assertEquals ((Integer) 4, filtered.get (1));
        assertEquals ((Integer) 6, filtered.get (2));
    }

    @Test
    public void testFilter3 ()
    {
        final List<Integer> list = GenericFactory.newArrayList (1, 2, 3, 4, 5, 6);
        final List<Integer> filtered = HcUtil.filter (list, a -> a.intValue () % 2 == 0);
        assertTrue (filtered.size () == 3);
        assertEquals ((Number) 2, filtered.get (0));
        assertEquals ((Number) 4, filtered.get (1));
        assertEquals ((Number) 6, filtered.get (2));
    }

    @Test
    public void testFind1 ()
    {
        final List<Integer> list = GenericFactory.newArrayList (1, 2, 3, 4, 5, 6);
        final int filtered = HcUtil.find (list, a -> a.intValue () % 2 == 0);
        assertEquals (2, filtered);
    }

    @Test
    public void testFind2 ()
    {
        final List<Integer> list = GenericFactory.newArrayList (1, 2, 3, 4, 5, 6);
        final int filtered = HcUtil.find (list, a -> a.intValue () % 2 == 0);
        assertEquals (2, filtered);
    }

    @Test
    public void testFind2Lambda ()
    {
        final List<Integer> list = GenericFactory.newArrayList (1, 2, 3, 4, 5, 6);

        final int filtered = HcUtil.find (list, a -> a.intValue () % 2 == 0);

        assertEquals (2, filtered);
    }

    @Test
    public void testFormatBytes ()
    {
        assertEquals ("0 bytes", HcUtil.formatBytes (0L));
        assertEquals ("999 bytes", HcUtil.formatBytes (999L));

        assertEquals ("1.0 KB", HcUtil.formatBytes (1000L));
        assertEquals ("1.0 KB", HcUtil.formatBytes (1049L));
        assertEquals ("1.0 KB", HcUtil.formatBytes (1050L));
        assertEquals ("1.1 KB", HcUtil.formatBytes (1100L));
        assertEquals ("9.8 KB", HcUtil.formatBytes (9850L));
        assertEquals ("9.9 KB", HcUtil.formatBytes (9900L));
        assertEquals ("9.9 KB", HcUtil.formatBytes (9949L));
        assertEquals ("9.9 KB", HcUtil.formatBytes (9999L));

        assertEquals ("1.0 MB", HcUtil.formatBytes (1000000L));
        assertEquals ("1.0 MB", HcUtil.formatBytes (1049000L));
        assertEquals ("1.0 MB", HcUtil.formatBytes (1050000L));
        assertEquals ("1.1 MB", HcUtil.formatBytes (1100000L));
        assertEquals ("9.8 MB", HcUtil.formatBytes (9850000L));
        assertEquals ("9.9 MB", HcUtil.formatBytes (9900000L));
        assertEquals ("9.9 MB", HcUtil.formatBytes (9949000L));
        assertEquals ("9.9 MB", HcUtil.formatBytes (9999000L));

        assertEquals ("1.0 GB", HcUtil.formatBytes (1000000000L));
        assertEquals ("1.0 GB", HcUtil.formatBytes (1049000000L));
        assertEquals ("1.0 GB", HcUtil.formatBytes (1050000000L));
        assertEquals ("1.1 GB", HcUtil.formatBytes (1100000000L));
        assertEquals ("9.8 GB", HcUtil.formatBytes (9850000000L));
        assertEquals ("9.9 GB", HcUtil.formatBytes (9900000000L));
        assertEquals ("9.9 GB", HcUtil.formatBytes (9949000000L));
        assertEquals ("9.9 GB", HcUtil.formatBytes (9999000000L));

        assertEquals ("1.0 TB", HcUtil.formatBytes (1000000000000L));
        assertEquals ("1.0 TB", HcUtil.formatBytes (1049000000000L));
        assertEquals ("1.0 TB", HcUtil.formatBytes (1050000000000L));
        assertEquals ("1.1 TB", HcUtil.formatBytes (1100000000000L));
        assertEquals ("9.8 TB", HcUtil.formatBytes (9850000000000L));
        assertEquals ("9.9 TB", HcUtil.formatBytes (9900000000000L));
        assertEquals ("9.9 TB", HcUtil.formatBytes (9949000000000L));
        assertEquals ("9.9 TB", HcUtil.formatBytes (9999000000000L));

        assertEquals ("10.0 TB", HcUtil.formatBytes (10000000000000L));
        assertEquals ("100.0 TB", HcUtil.formatBytes (100000000000000L));
    }

    @Test
    public void testFormatIntegralDate ()
    {
        final GregorianCalendar gc = new GregorianCalendar (2010, 0, 2);// 0 == january
        final String s = HcUtil.formatIntegralDate (gc.getTime ());

        assertEquals ("20100102", s);
    }

    @Test
    public void testFormatMessageScoped ()
    {
        final String format = "xxx %s yyy";

        // Local IScope.
        try (final IScope scope = new ExecutionScope ())
        {
            assertEndsWith ("xxx zzz yyy", HcUtil.contextFormatMessage (format, "zzz"));
            assertEndsWith ("xxx # yyy", HcUtil.contextFormatMessage (format, "#"));
            assertEndsWith ("xxx TGR#$%#$%#$%# yyy", HcUtil.contextFormatMessage (format, TRICKY_FORMAT_STRING));

            try
            {
                ThreadContext.assertError (false, "Value [%s] etc %s characters", TRICKY_FORMAT_STRING, 6);
            }
            catch (final AssertException e)
            {
                // Expected.
                assertEndsWith ("Value [" + TRICKY_FORMAT_STRING + "] etc 6 characters", e.getMessage ());
            }
            catch (final Throwable e)
            {
                // Unexpected.
                assertTrue (false);
            }

            if (true)
            {
                final boolean prev = ThreadContext.setPolicyErrorIncludeStackTrace (true);

                try (final LoggingSilenceScope ls = new LoggingSilenceScope ())
                {
                    try
                    {
                        ThreadContext.assertError (false, "Value [%s] etc %s characters", TRICKY_FORMAT_STRING, 6);
                    }
                    catch (final AssertException e)
                    {
                        // Expected.
                        assertEquals ("Value [" + TRICKY_FORMAT_STRING + "] etc 6 characters", e.getMessage ());
                    }
                    catch (final Throwable e)
                    {
                        // Unexpected.
                        assertTrue (false);
                    }
                }
                finally
                {
                    ThreadContext.setPolicyErrorIncludeStackTrace (prev);
                }
            }
        }
        catch (final AssertException e)
        {
            // Failure encountered during execution scope (scope.assertXxxx), and control
            // is transferred to this point. Information about the exception (OpResults)
            // will be returned to the caller.
        }
    }

    @Test
    public void testFormatMessageUnscoped ()
    {
        final String format = "xxx %s yyy";

        if (true)
        {
            assertEquals ("xxx zzz yyy", HcUtil.contextFormatMessage (format, "zzz"));
            assertEquals ("xxx # yyy", HcUtil.contextFormatMessage (format, "#"));
            assertEquals ("xxx TGR#$%#$%#$%# yyy", HcUtil.contextFormatMessage (format, TRICKY_FORMAT_STRING));

            try
            {
                ThreadContext.assertError (false, "Value [%s] etc %s characters", TRICKY_FORMAT_STRING, 6);
            }
            catch (final AssertException e)
            {
                // Expected.
                assertEquals ("Value [" + TRICKY_FORMAT_STRING + "] etc 6 characters", e.getMessage ());
            }
            catch (final Throwable e)
            {
                // Unexpected.
                assertTrue (false);
            }
        }

        if (true)
        {
            final boolean prev = ThreadContext.setPolicyErrorIncludeStackTrace (true);

            try (final LoggingSilenceScope ls = new LoggingSilenceScope ())
            {
                try
                {
                    ThreadContext.assertError (false, "Value [%s] etc %s characters", TRICKY_FORMAT_STRING, 6);
                }
                catch (final AssertException e)
                {
                    // Expected.
                    assertEquals ("Value [" + TRICKY_FORMAT_STRING + "] etc 6 characters", e.getMessage ());
                }
                catch (final Throwable e)
                {
                    // Unexpected.
                    assertTrue (false);
                }
            }
            finally
            {
                ThreadContext.setPolicyErrorIncludeStackTrace (prev);
            }
        }
    }

    @Test
    public void testFormatNanoseconds ()
    {
        assertEquals ("0ns", HcUtil.formatNanoseconds (0L));
        assertEquals ("1ns", HcUtil.formatNanoseconds (1L));
        assertEquals ("999ns", HcUtil.formatNanoseconds (999L));
        assertEquals ("1us", HcUtil.formatNanoseconds (1000L));
        assertEquals ("999us", HcUtil.formatNanoseconds (999L * 1000L));
        assertEquals ("1ms", HcUtil.formatNanoseconds (1000L * 1000L));
        assertEquals ("999ms", HcUtil.formatNanoseconds (999L * 1000L * 1000L));
        assertEquals ("1.0s", HcUtil.formatNanoseconds (1000L * 1000L * 1000L));
        assertEquals ("1.1s", HcUtil.formatNanoseconds (1100L * 1000L * 1000L));
        assertEquals ("59.9s", HcUtil.formatNanoseconds (59900L * 1000L * 1000L));
        assertEquals ("1.0m", HcUtil.formatNanoseconds (60 * 1000L * 1000L * 1000L));
        assertEquals ("1.1m", HcUtil.formatNanoseconds (60 * 1100L * 1000L * 1000L));
        assertEquals ("59.9m", HcUtil.formatNanoseconds (60 * 59900L * 1000L * 1000L));
        assertEquals ("1.0h", HcUtil.formatNanoseconds (60 * 60 * 1000L * 1000L * 1000L));
        assertEquals ("1.1h", HcUtil.formatNanoseconds (60 * 60 * 1100L * 1000L * 1000L));
        assertEquals ("23.9h", HcUtil.formatNanoseconds (23900L * 60 * 60 * 1000L * 1000L));
        assertEquals ("1.0d", HcUtil.formatNanoseconds (24 * 60 * 60 * 1000L * 1000L * 1000L));
        assertEquals ("1.1d", HcUtil.formatNanoseconds (24 * 60 * 60 * 1100L * 1000L * 1000L));
        assertEquals ("8.8d", HcUtil.formatNanoseconds (8L * 24 * 60 * 60 * 1100L * 1000L * 1000L));
    }

    @Test
    public void testFormFilepath ()
    {
        assertEquals ("/filename.txt", HcUtil.formFilepath ("", "filename.txt"));
        assertEquals ("/filename.txt", HcUtil.formFilepath ("/", "filename.txt"));

        assertEquals ("c:/filename.txt", HcUtil.formFilepath ("c:", "filename.txt"));
        assertEquals ("c:/filename.txt", HcUtil.formFilepath ("c:/", "filename.txt"));

        assertEquals ("q:/temp/filename.txt", HcUtil.formFilepath ("q:/temp", "filename.txt"));
        assertEquals ("q:/temp/filename.txt", HcUtil.formFilepath ("q:/temp/", "filename.txt"));

        assertEquals ("/filename.txt", HcUtil.formFilepath ("", "filename.txt"));
        assertEquals ("/filename.txt", HcUtil.formFilepath ("\\", "filename.txt"));

        assertEquals ("c:/filename.txt", HcUtil.formFilepath ("c:", "filename.txt"));
        assertEquals ("c:/filename.txt", HcUtil.formFilepath ("c:\\", "filename.txt"));

        assertEquals ("q:/temp/filename.txt", HcUtil.formFilepath ("q:\\temp", "filename.txt"));
        assertEquals ("q:/temp/filename.txt", HcUtil.formFilepath ("q:\\temp\\", "filename.txt"));
    }

    @Test
    public void testFormFilepathMultiple ()
    {
        assertEquals ("/dir1/dir2/filename.txt", HcUtil.formFilepath ("", "dir1", "dir2", "filename.txt"));
        assertEquals ("/dir1/dir2/filename.txt", HcUtil.formFilepath ("/", "dir1", "dir2", "filename.txt"));

        assertEquals ("c:/dir1/dir2/filename.txt", HcUtil.formFilepath ("c:", "dir1", "dir2", "filename.txt"));
        assertEquals ("c:/dir1/dir2/filename.txt", HcUtil.formFilepath ("c:/", "dir1", "dir2", "filename.txt"));

        assertEquals ("q:/temp/dir1/dir2/filename.txt",
            HcUtil.formFilepath ("q:/temp", "dir1", "dir2", "filename.txt"));
        assertEquals ("q:/temp/dir1/dir2/filename.txt",
            HcUtil.formFilepath ("q:/temp/", "dir1", "dir2", "filename.txt"));

        assertEquals ("/dir1/dir2/filename.txt", HcUtil.formFilepath ("", "dir1", "dir2", "filename.txt"));
        assertEquals ("/dir1/dir2/filename.txt", HcUtil.formFilepath ("\\", "dir1", "dir2", "filename.txt"));

        assertEquals ("c:/dir1/dir2/filename.txt", HcUtil.formFilepath ("c:", "dir1", "dir2", "filename.txt"));
        assertEquals ("c:/dir1/dir2/filename.txt", HcUtil.formFilepath ("c:\\", "dir1", "dir2", "filename.txt"));

        assertEquals ("q:/temp/dir1/dir2/filename.txt",
            HcUtil.formFilepath ("q:\\temp", "dir1", "dir2", "filename.txt"));
        assertEquals ("q:/temp/dir1/dir2/filename.txt",
            HcUtil.formFilepath ("q:\\temp\\", "dir1", "dir2", "filename.txt"));
    }

    @Test
    public void testGetApplicationObject ()
    {
        final Integer o = 1;
        final String key = "sss";

        HcUtil.putApplicationObject (key, o);
        final Integer o1 = HcUtil.getApplicationObject (key);
        assertEquals (o, o1);

        final Object oMissing = HcUtil.getApplicationObject (Integer.class);
        assertEquals (null, oMissing);
    }

    @Test (expected = FaultException.class)
    public void testGetApplicationObjectException ()
    {
        try (final LoggingSilenceScope ls = new LoggingSilenceScope ())
        {
            HcUtil.getApplicationObjectAssert (Integer.class);
        }
    }

    @Test
    public void testGetByteValue ()
    {
        assertEquals ((byte) 0, HcUtil.getByteValue (Byte.valueOf ((byte) 0)));
        assertEquals ((byte) 1, HcUtil.getByteValue (Byte.valueOf ((byte) 1)));
        assertEquals ((byte) -1, HcUtil.getByteValue (Byte.valueOf ((byte) -1)));
        assertEquals (Byte.MAX_VALUE, HcUtil.getByteValue (Byte.valueOf (Byte.MAX_VALUE)));
        assertEquals (Byte.MIN_VALUE, HcUtil.getByteValue (Byte.valueOf (Byte.MIN_VALUE)));
    }

    @Test
    public void testGetCause ()
    {
        final IOException e1 = new IOException ();
        final SomeDerivedAssertException e2 = new SomeDerivedAssertException (OpResult.Severity.Error, "asdf");
        e2.initCause (e1);
        final Exception e3 = new Exception (e2);

        assertEquals (e3, HcUtil.getCause (e3, Exception.class));
        assertEquals (e3, HcUtil.getCauseAllowSubclass (e3, Exception.class));

        assertEquals (e2, HcUtil.getCause (e3, SomeDerivedAssertException.class));
        assertEquals (null, HcUtil.getCause (e3, AssertException.class));
        assertEquals (e2, HcUtil.getCauseAllowSubclass (e3, AssertException.class));

        assertEquals (e1, HcUtil.getCause (e3, IOException.class));
        assertEquals (e1, HcUtil.getCauseAllowSubclass (e3, IOException.class));

        assertEquals (e1, HcUtil.getRootCause (e3));
    }

    @Test
    public void testGetDifferences1 ()
    {
        final List<String> lhs = GenericFactory.newArrayList ("a", "b", "c", "d");
        final List<String> rhs = GenericFactory.newArrayList ("a", "b", "c", "d");
        final E3<List<String>, List<String>, List<String>> result = HcUtil.getDifferences (lhs, rhs);
        assertEquals (0, result.getE0 ().size ());
        assertEquals (4, result.getE1 ().size ());
        assertEquals (0, result.getE2 ().size ());
    }

    @Test
    public void testGetDifferences2 ()
    {
        final List<String> lhs = GenericFactory.newArrayList ("b", "c", "d");
        final List<String> rhs = GenericFactory.newArrayList ("a", "b", "c");
        final E3<List<String>, List<String>, List<String>> result = HcUtil.getDifferences (lhs, rhs);
        final List<String> leftOnly = result.getE0 ();
        assertEquals (1, leftOnly.size ());
        assertEquals ("d", leftOnly.get (0));

        final List<String> shared = result.getE1 ();
        assertEquals (2, shared.size ());
        assertEquals ("b", shared.get (0));
        assertEquals ("c", shared.get (1));

        final List<String> rightOnly = result.getE2 ();
        assertEquals (1, rightOnly.size ());
        assertEquals ("a", rightOnly.get (0));
    }

    @Test
    public void testGetDifferences3 ()
    {
        final List<String> lhs = GenericFactory.newArrayList ("a", "b", "c");
        final List<String> rhs = GenericFactory.newArrayList ("1", "2", "3");
        final E3<List<String>, List<String>, List<String>> result = HcUtil.getDifferences (lhs, rhs);
        assertEquals (3, result.getE0 ().size ());
        assertEquals (0, result.getE1 ().size ());
        assertEquals (3, result.getE2 ().size ());
    }

    @Test
    public void testGetDifferences4 ()
    {
        final List<Integer> lhs = GenericFactory.newArrayList (1, 2, 3);
        final List<Integer> rhs = GenericFactory.newArrayList (3, 4, 5);
        final E3<List<Integer>, List<Integer>, List<Integer>> result = HcUtil.getDifferences (lhs, rhs);

        final List<Integer> leftOnly = result.getE0 ();
        assertEquals (2, leftOnly.size ());
        assertEquals (1, leftOnly.get (0).intValue ());
        assertEquals (2, leftOnly.get (1).intValue ());

        final List<Integer> shared = result.getE1 ();
        assertEquals (1, shared.size ());
        assertEquals (3, shared.get (0).intValue ());

        final List<Integer> rightOnly = result.getE2 ();
        assertEquals (2, rightOnly.size ());
        assertEquals (4, rightOnly.get (0).intValue ());
        assertEquals (5, rightOnly.get (1).intValue ());
    }

    @Test
    public void testGetExceptionDetails ()
    {
        final IScope scope = new ExecutionScope ();

        // Local IScope.
        try
        {
            final String preamble = TRICKY_FORMAT_STRING;

            String s = HcUtil.getExceptionDetails (new Exception (TRICKY_FORMAT_STRING), preamble, false);
            assertNotNull (s);

            s = HcUtil.getExceptionDetails (new Exception (TRICKY_FORMAT_STRING), preamble, true);
            assertNotNull (s);

        }
        catch (final AssertException e)
        {
        }
        finally
        {
            scope.close ();
        }
    }

    @Test
    public void testGetFractionalComponentDouble ()
    {
        assertTrue (HcUtil.isNear (HcUtil.getFractionalComponent (0.0), 0.0, 0.00000001));
        assertTrue (HcUtil.isNear (HcUtil.getFractionalComponent (0.1), 0.1, 0.00000001));

        assertTrue (HcUtil.isNear (HcUtil.getFractionalComponent (1.0), 0.0, 0.00000001));
        assertTrue (HcUtil.isNear (HcUtil.getFractionalComponent (1.1), 0.1, 0.00000001));

        assertTrue (HcUtil.isNear (HcUtil.getFractionalComponent (-1.0), 0.0, 0.00000001));
        assertTrue (HcUtil.isNear (HcUtil.getFractionalComponent (-1.1), -0.1, 0.00000001));
    }

    @Test
    public void testGetIntegerComponentDouble ()
    {
        assertEquals (0L, HcUtil.getIntegerComponent (0.0));
        assertEquals (0L, HcUtil.getIntegerComponent (0.1));

        assertEquals (1L, HcUtil.getIntegerComponent (1.0));
        assertEquals (1L, HcUtil.getIntegerComponent (1.1));

        assertEquals (-1L, HcUtil.getIntegerComponent (-1.0));
        assertEquals (-1L, HcUtil.getIntegerComponent (-1.1));
    }

    @Test
    public void testGetIntValue ()
    {
        assertEquals ((byte) 0, HcUtil.getIntValue (Byte.valueOf ((byte) 0)));
        assertEquals ((byte) 1, HcUtil.getIntValue (Byte.valueOf ((byte) 1)));
        assertEquals ((byte) -1, HcUtil.getIntValue (Byte.valueOf ((byte) -1)));
        assertEquals (Byte.MAX_VALUE, HcUtil.getIntValue (Byte.valueOf (Byte.MAX_VALUE)));
        assertEquals (Byte.MIN_VALUE, HcUtil.getIntValue (Byte.valueOf (Byte.MIN_VALUE)));
    }

    @Test
    public void testGetLongValue ()
    {
        assertEquals ((byte) 0, HcUtil.getLongValue (Byte.valueOf ((byte) 0)));
        assertEquals ((byte) 1, HcUtil.getLongValue (Byte.valueOf ((byte) 1)));
        assertEquals ((byte) -1, HcUtil.getLongValue (Byte.valueOf ((byte) -1)));
        assertEquals (Byte.MAX_VALUE, HcUtil.getLongValue (Byte.valueOf (Byte.MAX_VALUE)));
        assertEquals (Byte.MIN_VALUE, HcUtil.getLongValue (Byte.valueOf (Byte.MIN_VALUE)));

        assertEquals ((short) 0, HcUtil.getLongValue (Short.valueOf ((short) 0)));
        assertEquals ((short) 1, HcUtil.getLongValue (Short.valueOf ((short) 1)));
        assertEquals ((short) -1, HcUtil.getLongValue (Short.valueOf ((short) -1)));
        assertEquals (Short.MAX_VALUE, HcUtil.getLongValue (Short.valueOf (Short.MAX_VALUE)));
        assertEquals (Short.MIN_VALUE, HcUtil.getLongValue (Short.valueOf (Short.MIN_VALUE)));

        assertEquals (0, HcUtil.getLongValue (Integer.valueOf (0)));
        assertEquals (1, HcUtil.getLongValue (Integer.valueOf (1)));
        assertEquals (-1, HcUtil.getLongValue (Integer.valueOf (-1)));
        assertEquals (Integer.MAX_VALUE, HcUtil.getLongValue (Integer.valueOf (Integer.MAX_VALUE)));
        assertEquals (Integer.MIN_VALUE, HcUtil.getLongValue (Integer.valueOf (Integer.MIN_VALUE)));

        assertEquals (0L, HcUtil.getLongValue (Long.valueOf (0)));
        assertEquals (1L, HcUtil.getLongValue (Long.valueOf (1)));
        assertEquals (-1L, HcUtil.getLongValue (Long.valueOf (-1)));
        assertEquals (Long.MAX_VALUE, HcUtil.getLongValue (Long.valueOf (Long.MAX_VALUE)));
        assertEquals (Long.MIN_VALUE, HcUtil.getLongValue (Long.valueOf (Long.MIN_VALUE)));

        assertEquals (0, HcUtil.getLongValue (BigDecimal.valueOf (0)));
        assertEquals (1, HcUtil.getLongValue (BigDecimal.valueOf (1)));
        assertEquals (-1, HcUtil.getLongValue (BigDecimal.valueOf (-1)));
        assertEquals (Long.MAX_VALUE, HcUtil.getLongValue (BigDecimal.valueOf (Long.MAX_VALUE)));
        assertEquals (Long.MIN_VALUE, HcUtil.getLongValue (BigDecimal.valueOf (Long.MIN_VALUE)));

        assertTrue (HcUtil.getLongValue (Float.valueOf ((float) 0.0)) == 0);
        assertTrue (HcUtil.getLongValue (Float.valueOf ((float) 1.0)) == 1);
        assertTrue (HcUtil.getLongValue (Float.valueOf ((float) -1.0)) == -1);
        long v = HcUtil.getIntegerComponent (Float.MAX_VALUE);
        assertEquals (v, HcUtil.getLongValue (Float.valueOf (v)));
        v = HcUtil.getIntegerComponent (Float.MIN_VALUE);
        assertEquals (v, HcUtil.getLongValue (v));

        assertEquals (0, HcUtil.getLongValue (Double.valueOf (0.0)));
        assertEquals (1, HcUtil.getLongValue (Double.valueOf (1.0)));
        assertEquals (-1, HcUtil.getLongValue (Double.valueOf (-1.0)));
        v = HcUtil.getIntegerComponent (Double.MAX_VALUE);
        assertEquals (v, HcUtil.getLongValue (Double.valueOf (v)));
        v = HcUtil.getIntegerComponent (Double.MIN_VALUE);
        assertEquals (v, HcUtil.getLongValue (v));
    }

    @Test
    public void testGetNullTerminatedByteString ()
    {
        final byte[] bytes = HcUtil.getNullTerminatedBytes ("0123");
        assertArrayEquals (new byte[]
        {
                0x30,
                0x31,
                0x32,
                0x33,
                0
        }, bytes);

        final String s = HcUtil.getNullTerminatedString (bytes);
        assertEquals ("0123", s);
    }

    @Test
    public void testGetNullTerminatedByteStringEmpty ()
    {
        final byte[] bytes = HcUtil.getNullTerminatedBytes ("");
        assertArrayEquals (new byte[]
        {
                0
        }, bytes);

        final String s = HcUtil.getNullTerminatedString (bytes);
        assertEquals ("", s);
    }

    @Test
    public void testGetRootCause ()
    {
        final Exception e1 = new Exception ();
        final Exception e2 = new Exception (e1);
        final Exception e3 = new Exception (e2);
        final Exception e4 = new RuntimeException (e3);
        final Exception e5 = new ExecutionException (e4);
        final Exception e6 = new CompletionException (e5);

        assertEquals (e1, HcUtil.getRootCause (e1));
        assertEquals (e1, HcUtil.getRootCause (e2));
        assertEquals (e1, HcUtil.getRootCause (e3));
        assertEquals (e1, HcUtil.getRootCause (e4));
        assertEquals (e1, HcUtil.getRootCause (e5));
        assertEquals (e1, HcUtil.getRootCause (e6));
    }

    @Test
    public void testGetShortValue ()
    {
        assertEquals ((byte) 0, HcUtil.getShortValue (Byte.valueOf ((byte) 0)));
        assertEquals ((byte) 1, HcUtil.getShortValue (Byte.valueOf ((byte) 1)));
        assertEquals ((byte) -1, HcUtil.getShortValue (Byte.valueOf ((byte) -1)));
        assertEquals (Byte.MAX_VALUE, HcUtil.getShortValue (Byte.valueOf (Byte.MAX_VALUE)));
        assertEquals (Byte.MIN_VALUE, HcUtil.getShortValue (Byte.valueOf (Byte.MIN_VALUE)));
    }

    @Test
    public void testGetSortedDistinct ()
    {
        final List<String> values = GenericFactory.newArrayList ("c", "a", "b", "a", "c", "a", "d");
        final List<String> l = HcUtil.getSortedDistinct (values);
        assertEquals (4, l.size ());
        assertEquals ("a", l.get (0));
        assertEquals ("b", l.get (1));
        assertEquals ("c", l.get (2));
        assertEquals ("d", l.get (3));
    }

    @Test
    public void testGetUnderlyingClass ()
    {
        assertEquals (null, HcUtil.getUnderlyingClass (null));
        assertEquals (String.class, HcUtil.getUnderlyingClass ("asdf"));
        assertEquals (Integer.class, HcUtil.getUnderlyingClass (1));
        assertEquals (String[].class, HcUtil.getUnderlyingClass (new String[]
        {}));
        assertEquals (String.class, HcUtil.getUnderlyingClass (new String[]
        {
                "asdf"
        }));
    }

    @Test
    public void testHourRange ()
    {
        assertTrue (HcUtil.inDateTimeRange (DT_20110520_102030, new GregorianCalendar (2011, 5 - 1, 20, 10, 0, 0),
            new GregorianCalendar (2011, 5 - 1, 20, 11, 0, 0)));
    }

    @Test
    public void testjoinArray ()
    {
        final String sa = "a";
        final String sb = "b";
        final String sc = "c";

        final String[] abc =
            {
                    sa,
                    sb,
                    sc
        };
        final String[] ab =
            {
                    sa,
                    sb
        };
        final String[] bc =
            {
                    sb,
                    sc
        };
        final String[] a =
            {
                    sa
        };
        final String[] b =
            {
                    sb
        };
        final String[] c =
            {
                    sc
        };

        assertArrayEquals (new String[0], HcUtil.joinArrays (null, null));
        assertArrayEquals (new String[0], HcUtil.joinArrays (null, null, null, null, null));
        assertArrayEquals (abc, HcUtil.joinArrays (abc, null));
        assertArrayEquals (abc, HcUtil.joinArrays (null, abc));
        assertArrayEquals (abc, HcUtil.joinArrays (ab, c));
        assertArrayEquals (abc, HcUtil.joinArrays (a, bc));
        assertArrayEquals (abc, HcUtil.joinArrays (a, b, c));

    }

    @Test
    public void testLeftPadding ()
    {
        assertEquals (null, HcUtil.getLeftPaddedString (null, 4));
        assertEquals ("    ", HcUtil.getLeftPaddedString ("", 4));
        assertEquals ("   a", HcUtil.getLeftPaddedString ("a", 4));
        assertEquals ("  aa", HcUtil.getLeftPaddedString ("aa", 4));
        assertEquals (" aaa", HcUtil.getLeftPaddedString ("aaa", 4));
        assertEquals ("aaaa", HcUtil.getLeftPaddedString ("aaaa", 4));
        assertEquals ("aaaaa", HcUtil.getLeftPaddedString ("aaaaa", 4));
        assertEquals ("aaaaaa", HcUtil.getLeftPaddedString ("aaaaaa", 4));
    }

    @Test
    public void testLimitSize ()
    {
        final String s0 = "s0";
        final String s1 = "s1";
        final String s2 = "s2";
        final String s3 = "s3";
        final String s4 = "s4";

        List<String> l = GenericFactory.newArrayList (s0, s1, s2, s3, s4);
        assertTrue (l.size () == 5);

        l = HcUtil.limitSize (l, 6);
        assertTrue (l.size () == 5);
        assertEquals (l.get (0), s0);
        assertEquals (l.get (1), s1);
        assertEquals (l.get (2), s2);
        assertEquals (l.get (3), s3);
        assertEquals (l.get (4), s4);

        l = HcUtil.limitSize (l, 5);
        assertTrue (l.size () == 5);
        assertEquals (l.get (0), s0);
        assertEquals (l.get (1), s1);
        assertEquals (l.get (2), s2);
        assertEquals (l.get (3), s3);
        assertEquals (l.get (4), s4);

        l = HcUtil.limitSize (l, 3);
        assertTrue (l.size () == 3);
        assertEquals (l.get (0), s0);
        assertEquals (l.get (1), s1);
        assertEquals (l.get (2), s2);

        l = HcUtil.limitSize (l, 4);
        assertTrue (l.size () == 3);
        assertEquals (l.get (0), s0);
        assertEquals (l.get (1), s1);
        assertEquals (l.get (2), s2);

        l = HcUtil.limitSize (l, 1);
        assertTrue (l.size () == 1);
        assertEquals (l.get (0), s0);

        l = HcUtil.limitSize (l, 0);
        assertTrue (l.size () == 0);
    }

    @Test
    public void testLog2 ()
    {
        assertEquals (0, HcUtil.log2 (0));
        assertEquals (0, HcUtil.log2 (1));
        assertEquals (1, HcUtil.log2 (2));
        assertEquals (1, HcUtil.log2 (3));
        assertEquals (2, HcUtil.log2 (4));
        assertEquals (2, HcUtil.log2 (5));
        assertEquals (2, HcUtil.log2 (6));
        assertEquals (2, HcUtil.log2 (7));
        assertEquals (3, HcUtil.log2 (8));
        assertEquals (3, HcUtil.log2 (9));
        assertEquals (3, HcUtil.log2 (10));
        assertEquals (3, HcUtil.log2 (11));
        assertEquals (3, HcUtil.log2 (12));
        assertEquals (3, HcUtil.log2 (13));
        assertEquals (3, HcUtil.log2 (14));
        assertEquals (3, HcUtil.log2 (15));
        assertEquals (4, HcUtil.log2 (16));
        assertEquals (4, HcUtil.log2 (17));
        assertEquals (4, HcUtil.log2 (31));
        assertEquals (5, HcUtil.log2 (32));
        assertEquals (5, HcUtil.log2 (33));
        assertEquals (5, HcUtil.log2 (63));
        assertEquals (6, HcUtil.log2 (64));
        assertEquals (6, HcUtil.log2 (65));
        assertEquals (6, HcUtil.log2 (127));
        assertEquals (7, HcUtil.log2 (128));
        assertEquals (7, HcUtil.log2 (129));
        assertEquals (7, HcUtil.log2 (255));
        assertEquals (8, HcUtil.log2 (256));
        assertEquals (8, HcUtil.log2 (257));
    }

    @Test
    public void testLowerCaseAcronyms ()
    {
        assertEquals ("abcTelevision", HcUtil.lowerCaseAcronyms ("abcTelevision"));
        assertEquals ("AbcTelevision", HcUtil.lowerCaseAcronyms ("AbcTelevision"));
        assertEquals ("AbcTelevision", HcUtil.lowerCaseAcronyms ("ABCTelevision"));
        assertEquals ("AbcTelevisionT", HcUtil.lowerCaseAcronyms ("ABCTelevisionT"));
        assertEquals ("TheAbcTelevision", HcUtil.lowerCaseAcronyms ("TheABCTelevision"));
        assertEquals ("AbcTeleVision", HcUtil.lowerCaseAcronyms ("ABCTeleVISION"));
        assertEquals ("Vision", HcUtil.lowerCaseAcronyms ("VISION"));
        assertEquals ("VisioNt", HcUtil.lowerCaseAcronyms ("VISIONt"));
        assertEquals ("Branch4", HcUtil.lowerCaseAcronyms ("BRANCH4"));
    }

    @Test
    public void testMinRange ()
    {
        assertTrue (HcUtil.inDateTimeRange (DT_20110520_102030, new GregorianCalendar (2011, 5 - 1, 20, 10, 19, 0),
            new GregorianCalendar (2011, 5 - 1, 20, 10, 21, 0)));
    }

    @Test
    public void testMisc ()
    {
        assertTrue (Object[].class.isAssignableFrom (String[].class));// same class or super
        assertTrue (!String[].class.isAssignableFrom (Object[].class));// same class or super

        //        final List<String> list = GenericFactory.newArrayList ("a", "b", "c");
        //        final String[] array = HgUtil.toArray (list);
        //        assertTrue (list.size () == array.length);
        //        assertTrue (list.get (2).equals (array[2]));
    }

    @Test
    public void testNullifyStrings1 ()
    {
        final Source source = setupSource ();
        HcUtil.nullifyStrings (source);
        assertEquals (4, source.getInt ());
        assertEquals ("Something", source.getString ());
        assertEquals (Double.valueOf (1234.5), source.getDouble ());
    }

    @Test
    public void testNullifyStrings2 ()
    {
        final Source source = setupSource ();
        source.setString ("");
        HcUtil.nullifyStrings (source);
        assertEquals (4, source.getInt ());
        assertEquals (null, source.getString ());
        assertEquals (Double.valueOf (1234.5), source.getDouble ());

        source.setString ("    ");
        HcUtil.nullifyStrings (source);
        assertEquals (4, source.getInt ());
        assertEquals (null, source.getString ());
        assertEquals (Double.valueOf (1234.5), source.getDouble ());
    }

    @Test
    public void testNullifyStrings3 ()
    {
        final TestThis tt = new TestThis ();
        tt.setS1 ("");
        tt.setS2 (" ");
        tt.setS3 (" a ");
        tt.setS4 ("a");
        tt.setI1 (5);
        HcUtil.nullifyStrings (tt);
        assertEquals (null, tt.getS1 ());
        assertEquals (null, tt.getS2 ());
        assertEquals (" a ", tt.getS3 ());
        assertEquals ("a", tt.getS4 ());
        assertEquals (5, tt.getI1 ());
    }

    @Test
    public void testObjectEquals ()
    {
        assertTrue (HcUtil.objectEquals (null, null));
        assertTrue (!HcUtil.objectEquals (null, "x"));
        assertTrue (!HcUtil.objectEquals ("x", null));
        assertTrue (HcUtil.objectEquals ("x", "x"));
        assertTrue (HcUtil.objectEquals (new String ("x"), new String ("x")));

        final StringBuilder sb1 = new StringBuilder ();
        sb1.append ("Some");
        sb1.append ("thing");
        final String s1 = sb1.toString ();
        final StringBuilder sb2 = new StringBuilder ();
        sb2.append ("Some");
        sb2.append ("thing");
        final String s2 = sb1.toString ();
        assertTrue (HcUtil.objectEquals (s1, s2));

        assertTrue (!HcUtil.objectEquals (new Integer (23456), new Long (23456)));

        assertTrue (HcUtil.objectEquals (new Byte ((byte) 45), new Byte ((byte) 45)));
        assertTrue (!HcUtil.objectEquals (new Byte ((byte) 67), new Byte ((byte) 68)));
        assertTrue (HcUtil.objectEquals (new Short ((short) 12345), new Short ((short) 12345)));
        assertTrue (!HcUtil.objectEquals (new Short ((short) 34567), new Short ((short) 22345)));
        assertTrue (HcUtil.objectEquals (new Integer (12345), new Integer (12345)));
        assertTrue (!HcUtil.objectEquals (new Integer (34567), new Integer (22345)));
        assertTrue (HcUtil.objectEquals (new Long (12345), new Long (12345)));
        assertTrue (!HcUtil.objectEquals (new Long (34567), new Long (22345)));
        assertTrue (HcUtil.objectEquals (new Double (12345), new Double (12345)));
        assertTrue (!HcUtil.objectEquals (new Double (34567), new Double (22345)));
    }

    @Test
    public void testRemoveLeadingAndTrailingBlankLines ()
    {
        if (true)
        {
            final List<String> lines = GenericFactory.newArrayList ("a", "b", "c");
            HcUtil.removeLeadingAndTrailingBlankLines (lines);
            assertEquals (3, lines.size ());
        }

        if (true)
        {
            final List<String> lines = GenericFactory.newArrayList ("a", "b", "c", "    ", "    ");
            HcUtil.removeLeadingAndTrailingBlankLines (lines);
            assertEquals (3, lines.size ());
        }

        if (true)
        {
            final List<String> lines = GenericFactory.newArrayList ("  ", "", "a", "b", "c");
            HcUtil.removeLeadingAndTrailingBlankLines (lines);
            assertEquals (3, lines.size ());
        }

        if (true)
        {
            final List<String> lines = GenericFactory.newArrayList ("  ", "", "a", "  ", "b", "c");
            HcUtil.removeLeadingAndTrailingBlankLines (lines);
            assertEquals (4, lines.size ());
        }
    }

    @Test
    public void testRemoveLeadingSpaces ()
    {
        assertEquals ("a", HcUtil.removeLeadingSpaces ("    a", 4));
        assertEquals ("a", HcUtil.removeLeadingSpaces ("    a", 5));
        assertEquals ("a", HcUtil.removeLeadingSpaces ("    a", 6));

        assertEquals (" a", HcUtil.removeLeadingSpaces ("    a", 3));
        assertEquals ("  a", HcUtil.removeLeadingSpaces ("    a", 2));
        assertEquals ("   a", HcUtil.removeLeadingSpaces ("    a", 1));
        assertEquals ("    a", HcUtil.removeLeadingSpaces ("    a", 0));
    }

    @Test
    public void testRemoveUnsafeFilenameChars ()
    {
        assertEquals ("01", HcUtil.removeUnsafeFilenameChars ("01"));
        assertEquals ("01", HcUtil.removeUnsafeFilenameChars ("0:1"));
        assertEquals ("01", HcUtil.removeUnsafeFilenameChars ("0/1"));
        assertEquals ("01", HcUtil.removeUnsafeFilenameChars ("0\\:\\1"));
        assertEquals ("01", HcUtil.removeUnsafeFilenameChars ("\\:\\01"));
        assertEquals ("01", HcUtil.removeUnsafeFilenameChars ("01\\:\\"));
    }

    @Test
    public void testRightPadding ()
    {
        assertEquals (null, HcUtil.getRightPaddedString (null, 4));
        assertEquals ("    ", HcUtil.getRightPaddedString ("", 4));
        assertEquals ("a   ", HcUtil.getRightPaddedString ("a", 4));
        assertEquals ("aa  ", HcUtil.getRightPaddedString ("aa", 4));
        assertEquals ("aaa ", HcUtil.getRightPaddedString ("aaa", 4));
        assertEquals ("aaaa", HcUtil.getRightPaddedString ("aaaa", 4));
        assertEquals ("aaaaa", HcUtil.getRightPaddedString ("aaaaa", 4));
        assertEquals ("aaaaaa", HcUtil.getRightPaddedString ("aaaaaa", 4));
    }

    @Test
    public void testSafeEquals ()
    {
        if (true)
        {
            final byte v = (byte) 1;
            assertTrue (HcUtil.safeEquals (v, v));
            assertTrue (HcUtil.safeEquals (new Byte (v), new Byte (v)));
            assertTrue (!HcUtil.safeEquals (new Byte (v), new Byte ((byte) (v + (byte) 1))));
            assertTrue (HcUtil.safeEquals ((Byte) null, (Byte) null));
            assertTrue (!HcUtil.safeEquals ((Byte) null, new Byte (v)));
            assertTrue (!HcUtil.safeEquals (new Byte (v), (Byte) null));
        }

        if (true)
        {
            final short v = (short) 1;
            assertTrue (HcUtil.safeEquals (v, v));
            assertTrue (HcUtil.safeEquals (new Short (v), new Short (v)));
            assertTrue (!HcUtil.safeEquals (new Short (v), new Short ((short) (v + (short) 1))));
            assertTrue (HcUtil.safeEquals ((Short) null, (Short) null));
            assertTrue (!HcUtil.safeEquals ((Short) null, new Short (v)));
            assertTrue (!HcUtil.safeEquals (new Short (v), (Short) null));
        }

        if (true)
        {
            final int v = 1;
            assertTrue (HcUtil.safeEquals (v, v));
            assertTrue (HcUtil.safeEquals (new Integer (v), new Integer (v)));
            assertTrue (!HcUtil.safeEquals (new Integer (v), new Integer (v + 1)));
            assertTrue (HcUtil.safeEquals ((Integer) null, (Integer) null));
            assertTrue (!HcUtil.safeEquals ((Integer) null, new Integer (v)));
            assertTrue (!HcUtil.safeEquals (new Integer (v), (Integer) null));
        }

        if (true)
        {
            final long v = 1;
            assertTrue (HcUtil.safeEquals (v, v));
            assertTrue (HcUtil.safeEquals (new Long (v), new Long (v)));
            assertTrue (!HcUtil.safeEquals (new Long (v), new Long (v + 1)));
            assertTrue (HcUtil.safeEquals ((Long) null, (Long) null));
            assertTrue (!HcUtil.safeEquals ((Long) null, new Long (v)));
            assertTrue (!HcUtil.safeEquals (new Long (v), (Long) null));
        }

        if (true)
        {
            final double v = 1.0;
            assertTrue (HcUtil.safeEquals (v, v));
            assertTrue (HcUtil.safeEquals (new Double (v), new Double (v)));
            assertTrue (!HcUtil.safeEquals (new Double (v), new Double (v + 1)));
            assertTrue (HcUtil.safeEquals ((Double) null, (Double) null));
            assertTrue (!HcUtil.safeEquals ((Double) null, new Double (v)));
            assertTrue (!HcUtil.safeEquals (new Double (v), (Double) null));
        }
    }

    @Test
    public void testSafeEqualsArraysBoolean ()
    {
        assertTrue (HcUtil.safeEquals ((boolean[]) null, (boolean[]) null));
        assertTrue (!HcUtil.safeEquals (new boolean[]
        {
                true
        }, null));
        assertTrue (!HcUtil.safeEquals (null, new boolean[]
        {
                true
        }));
        assertTrue (!HcUtil.safeEquals (null, new boolean[]
        {}));
        assertTrue (HcUtil.safeEquals (new boolean[]
        {
                true
        }, new boolean[]
        {
                true
        }));
        assertTrue (!HcUtil.safeEquals (new boolean[]
        {
                true
        }, new boolean[]
        {
                false
        }));
        assertTrue (!HcUtil.safeEquals (new boolean[]
        {
                true
        }, new boolean[]
        {
                true,
                false
        }));
    }

    @Test
    public void testSafeEqualsArraysDouble ()
    {
        assertTrue (HcUtil.safeEquals ((double[]) null, (double[]) null));
        assertTrue (!HcUtil.safeEquals (new double[]
        {
                1.0
        }, null));
        assertTrue (!HcUtil.safeEquals (null, new double[]
        {
                1.0
        }));
        assertTrue (!HcUtil.safeEquals (null, new double[]
        {}));
        assertTrue (HcUtil.safeEquals (new double[]
        {
                1.0
        }, new double[]
        {
                1.0
        }));
        assertTrue (!HcUtil.safeEquals (new double[]
        {
                1.0
        }, new double[]
        {
                0.0
        }));
        assertTrue (!HcUtil.safeEquals (new double[]
        {
                1.0
        }, new double[]
        {
                1.0,
                0.0
        }));
    }

    @Test
    public void testSafeEqualsArraysFloat ()
    {
        assertTrue (HcUtil.safeEquals ((float[]) null, (float[]) null));
        assertTrue (!HcUtil.safeEquals (new float[]
        {
                (float) 1.0
        }, null));
        assertTrue (!HcUtil.safeEquals (null, new float[]
        {
                (float) 1.0
        }));
        assertTrue (!HcUtil.safeEquals (null, new float[]
        {}));
        assertTrue (HcUtil.safeEquals (new float[]
        {
                (float) 1.0
        }, new float[]
        {
                (float) 1.0
        }));
        assertTrue (!HcUtil.safeEquals (new float[]
        {
                (float) 1.0
        }, new float[]
        {
                (float) 0.0
        }));
        assertTrue (!HcUtil.safeEquals (new float[]
        {
                (float) 1.0
        }, new float[]
        {
                (float) 1.0,
                (float) 0.0
        }));
    }

    @Test
    public void testSafeEqualsArraysInt ()
    {
        assertTrue (HcUtil.safeEquals ((int[]) null, (int[]) null));
        assertTrue (!HcUtil.safeEquals (new int[]
        {
                1
        }, null));
        assertTrue (!HcUtil.safeEquals (null, new int[]
        {
                1
        }));
        assertTrue (!HcUtil.safeEquals (null, new int[]
        {}));
        assertTrue (HcUtil.safeEquals (new int[]
        {
                1
        }, new int[]
        {
                1
        }));
        assertTrue (!HcUtil.safeEquals (new int[]
        {
                1
        }, new int[]
        {}));
        assertTrue (HcUtil.safeEquals (new int[]
        {
                1
        }, new int[]
        {
                1
        }));
    }

    @Test
    public void testSafeEqualsArraysLong ()
    {
        assertTrue (HcUtil.safeEquals ((long[]) null, (long[]) null));
        assertTrue (!HcUtil.safeEquals (new long[]
        {
                1
        }, null));
        assertTrue (!HcUtil.safeEquals (null, new long[]
        {
                1
        }));
        assertTrue (!HcUtil.safeEquals (null, new long[]
        {}));
        assertTrue (HcUtil.safeEquals (new long[]
        {
                1
        }, new long[]
        {
                1
        }));
        assertTrue (!HcUtil.safeEquals (new long[]
        {
                1
        }, new long[]
        {}));
        assertTrue (HcUtil.safeEquals (new long[]
        {
                1
        }, new long[]
        {
                1
        }));
    }

    @Test
    public void testSafeEqualsArraysShort ()
    {
        assertTrue (HcUtil.safeEquals ((short[]) null, (short[]) null));
        assertTrue (!HcUtil.safeEquals (new short[]
        {
                1
        }, null));
        assertTrue (!HcUtil.safeEquals (null, new short[]
        {
                1
        }));
        assertTrue (!HcUtil.safeEquals (null, new short[]
        {}));
        assertTrue (HcUtil.safeEquals (new short[]
        {
                1
        }, new short[]
        {
                1
        }));
        assertTrue (!HcUtil.safeEquals (new short[]
        {
                1
        }, new short[]
        {}));
        assertTrue (HcUtil.safeEquals (new short[]
        {
                1
        }, new short[]
        {
                1
        }));
    }

    @Test
    public void testSafeEqualsBoolean ()
    {
        assertTrue (HcUtil.safeEquals (true, true));
        assertTrue (HcUtil.safeEquals (false, false));
        assertTrue (!HcUtil.safeEquals (true, false));
    }

    @Test
    public void testSafeEqualsInt ()
    {
        assertTrue (HcUtil.safeEquals (1, 1));
        assertTrue (HcUtil.safeEquals (0, 0));
        assertTrue (!HcUtil.safeEquals (1, 0));
    }

    @Test
    public void testSafeEqualsLong ()
    {
        assertTrue (HcUtil.safeEquals (1, 1));
        assertTrue (HcUtil.safeEquals (0, 0));
        assertTrue (!HcUtil.safeEquals (1, 0));
    }

    @Test
    public void testSafeEqualsObject ()
    {
        assertTrue (HcUtil.safeEquals ((Object) null, (Object) null));
        assertTrue (!HcUtil.safeEquals ("asdf", null));
        assertTrue (!HcUtil.safeEquals (null, "as" + "df"));
        assertTrue (HcUtil.safeEquals ("a" + "sdf", "asd" + "f"));
        assertTrue (!HcUtil.safeEquals ("asdf", "asdff"));
    }

    @Test
    public void testSafeGetLengthString ()
    {
        assertEquals (0, HcUtil.safeGetLength ((String) null));
        assertEquals (0, HcUtil.safeGetLength (""));
        assertEquals (3, HcUtil.safeGetLength ("GLE"));
    }

    @Test
    public void testSafeGetLengthTArray ()
    {
        assertEquals (0, HcUtil.safeGetLength ((Integer[]) null));
        assertEquals (0, HcUtil.safeGetLength (new Integer[0]));
        assertEquals (4, HcUtil.safeGetLength (new HgUtilTest[4]));
    }

    @Test
    public void testSecRange ()
    {
        assertTrue (HcUtil.inDateTimeRange (DT_20110520_102030, new GregorianCalendar (2011, 5 - 1, 20, 10, 20, 29),
            new GregorianCalendar (2011, 5 - 1, 20, 10, 20, 31)));
    }

    @Test
    public void testSecRangeNot ()
    {
        assertTrue (
            !HcUtil.inDateTimeRange (DT_20110520_102030, new GregorianCalendar (2011, 5 - 1, 20, 10, 20, 29 + 1),
                new GregorianCalendar (2011, 5 - 1, 20, 10, 20, 31)));
        assertTrue (!HcUtil.inDateTimeRange (DT_20110520_102030, new GregorianCalendar (2011, 5 - 1, 20, 10, 20, 29),
            new GregorianCalendar (2011, 5 - 1, 20, 10, 20, 31 - 1)));
    }

    @Test
    public void testSplitStringIntoLines ()
    {
        assertSplitIntoLines ("", "");
        assertSplitIntoLines ("anb", "anb");
        assertSplitIntoLines ("a\nb", "a", "b");
        assertSplitIntoLines ("a\r\nb", "a", "b");
        assertSplitIntoLines ("a111\r\nb222\nc3333", "a111", "b222", "c3333");
    }

    @Test
    public void testToFirstCharUpperCase ()
    {
        assertEquals ("Abc", HcUtil.toFirstCharUpperCase ("abc"));
        assertEquals ("A", HcUtil.toFirstCharUpperCase ("a"));
    }

    @Test
    public void testTransferProperties ()
    {
        final Source source = setupSource ();
        final Target target = new Target ();
        HcUtil.transferProperties (source, target, false);

        assertEquals (source.getInt (), target.getInt ());
        assertTrue (target.getString ().equals (source.getString ()));
    }

    @Test (expected = FaultException.class)
    public void testTransferPropertiesStrict ()
    {
        try (final LoggingSilenceScope ls = new LoggingSilenceScope ())
        {
            final Source source = setupSource ();
            final Target target = new Target ();
            HcUtil.transferProperties (source, target, true);
        }
    }

    @Test
    public void testTrimLeft ()
    {
        // Untouched.
        assertEquals ("", HcUtil.trimLeft (""));
        assertEquals ("a", HcUtil.trimLeft ("a"));
        assertEquals ("as", HcUtil.trimLeft ("as"));
        assertEquals ("asd", HcUtil.trimLeft ("asd"));
        assertEquals ("asd ", HcUtil.trimLeft ("asd "));
        assertEquals ("asd  ", HcUtil.trimLeft ("asd  "));

        // Trimmed.
        assertEquals ("", HcUtil.trimLeft (" "));
        assertEquals ("a", HcUtil.trimLeft (" a"));
        assertEquals ("as", HcUtil.trimLeft (" as"));
        assertEquals ("asd", HcUtil.trimLeft (" asd"));
        assertEquals ("", HcUtil.trimLeft ("  "));
        assertEquals ("a", HcUtil.trimLeft ("  a"));
        assertEquals ("as", HcUtil.trimLeft ("  as"));
        assertEquals ("asd", HcUtil.trimLeft ("  asd"));
        assertEquals ("asd", HcUtil.trimLeft (" \n\t asd"));
    }

    @Test
    public void testTrimRight ()
    {
        // Untouched.
        assertEquals ("", HcUtil.trimRight (""));
        assertEquals ("a", HcUtil.trimRight ("a"));
        assertEquals ("as", HcUtil.trimRight ("as"));
        assertEquals ("asd", HcUtil.trimRight ("asd"));
        assertEquals (" asd", HcUtil.trimRight (" asd"));
        assertEquals ("  asd", HcUtil.trimRight ("  asd"));

        // Trimmed.
        assertEquals ("", HcUtil.trimRight (" "));
        assertEquals ("a", HcUtil.trimRight ("a "));
        assertEquals ("as", HcUtil.trimRight ("as "));
        assertEquals ("asd", HcUtil.trimRight ("asd "));
        assertEquals ("", HcUtil.trimRight ("  "));
        assertEquals ("a", HcUtil.trimRight ("a  "));
        assertEquals ("as", HcUtil.trimRight ("as  "));
        assertEquals ("asd", HcUtil.trimRight ("asd  "));
        assertEquals ("asd", HcUtil.trimRight ("asd \n\t "));
    }

    @Test
    public void testUnderScoreBreakToCaseBreak ()
    {
        assertEquals ("AbcTelevision", HcUtil.underScoreBreakToCaseBreak ("ABC_TELEVISION"));
        assertEquals ("AbcTelevisionT", HcUtil.underScoreBreakToCaseBreak ("ABC_TELEVISION_T"));
        assertEquals ("TheAbcTelevision", HcUtil.underScoreBreakToCaseBreak ("THE_ABC_TELEVISION"));
        assertEquals ("Vision", HcUtil.underScoreBreakToCaseBreak ("VISION"));
        assertEquals ("VisioNt", HcUtil.underScoreBreakToCaseBreak ("VISIO_NT"));
        assertEquals ("Branch4", HcUtil.underScoreBreakToCaseBreak ("BRANCH4"));
    }

    @Test
    public void testValuesToList1 ()
    {
        final Map<Integer, String> m = GenericFactory.newHashMap ();
        for (int i = 0; i < 5; ++i)
        {
            m.put (i, Integer.toString (i));
        }

        final List<String> l = HcUtil.valuesToList (m);
        for (int i = 0; i < 5; ++i)
        {
            assertEquals (Integer.toString (i), l.get (i));
        }
    }

    @Test
    public void testWildcardMatches ()
    {
        assertTrue (HcUtil.wildcardMatches ("abc", "abc"));
        assertTrue (HcUtil.wildcardMatches ("abc.+", "abcdef"));
        assertTrue (HcUtil.wildcardMatches ("abc.+qwer", "abcdefqwer"));
        assertTrue (HcUtil.wildcardMatches ("abc.+qwer", "abcdeflk;asdlkjasdfqwer"));
    }

    private void assertEndsWith (final String suffix, final String s)
    {
        assertTrue (s.endsWith (suffix));
    }

    private void assertSelf (final int length, final int maxLength)
    {
        final String s = formString (length);
        assertEquals (s, HcUtil.abbreviate (s, maxLength));
    }

    private void assertSplitIntoLines (final String s, final String... expected)
    {
        final List<String> lines = HcUtil.splitStringIntoLines (s);
        assertEquals (expected.length, lines.size ());

        int i = 0;
        for (final Iterator<String> it = lines.iterator (); it.hasNext (); ++i)
        {
            assertEquals (expected[i], it.next ());
        }
    }

    private String formString (final int length)
    {
        final StringBuilder sb = new StringBuilder ();
        for (int i = 0; i < length; ++i)
        {
            sb.append ("0123456789".charAt (i % 10));
        }

        return sb.toString ();
    }

    private Source setupSource ()
    {
        final Source source = new Source ();
        source.setInt (4);
        source.setString ("Something");
        source.setDouble (1234.5);
        return source;
    }

    public static class Source
    {
        public Double getDouble ()
        {
            return m_double;
        }

        public int getInt ()
        {
            return m_int;
        }

        public String getString ()
        {
            return m_string;
        }

        public void setDouble (final Double d)
        {
            m_double = d;
        }

        public void setInt (final int i)
        {
            m_int = i;
        }

        public void setString (final String s)
        {
            m_string = s;
        }

        private Double m_double;

        private int m_int;

        private String m_string;
    }

    public static class Target
    {
        public int getInt ()
        {
            return m_int;
        }

        public String getString ()
        {
            return m_string;
        }

        public void setInt (final int i)
        {
            m_int = i;
        }

        public void setString (final String string)
        {
            m_string = string;
        }

        private int m_int;

        private String m_string;
    }

    public static class TestEntity implements IIdentifiable<Integer>
    {
        public TestEntity ()
        {
        }

        public TestEntity (final TestEntity rhs)
        {
            m_id = rhs.m_id;
            m_name = rhs.m_name;
        }

        @Override
        public boolean equals (final Object o)
        {
            boolean isEqual = false;
            if (this == o)
            {
                isEqual = true;
            }
            else if (o != null && getClass () == o.getClass ())
            {
                final TestEntity eRhs = (TestEntity) o;
                isEqual = m_id == eRhs.m_id && HcUtil.safeEquals (m_name, eRhs.m_name);
            }
            return isEqual;
        }

        @Override
        public Integer getPrimaryKey ()
        {
            return m_id;
        }

        @Override
        public int hashCode ()
        {
            // Key or value may be array so use deep version.
            return HcUtil.deepHashCode (m_id, m_name);
        }

        public void setId (final int i)
        {
            m_id = i;
        }

        public void setName (final String s)
        {
            m_name = s;
        }

        private int m_id;

        private String m_name;
    }

    public static class TestThis
    {
        public int getI1 ()
        {
            return m_i1;
        }

        public String getS1 ()
        {
            return m_s1;
        }

        public String getS2 ()
        {
            return m_s2;
        }

        public String getS3 ()
        {
            return m_s3;
        }

        public String getS4 ()
        {
            return m_s4;
        }

        public void setI1 (final int i1)
        {
            m_i1 = i1;
        }

        public void setS1 (final String s1)
        {
            m_s1 = s1;
        }

        public void setS2 (final String s2)
        {
            m_s2 = s2;
        }

        public void setS3 (final String s3)
        {
            m_s3 = s3;
        }

        public void setS4 (final String s4)
        {
            m_s4 = s4;
        }

        private int m_i1;

        private String m_s1;

        private String m_s2;

        private String m_s3;

        private String m_s4;
    }

    private static class CompTestEntity
    {
        public CompTestEntity (final String s1, final Integer i, final String s2)
        {
            m_s1 = s1;
            m_i = i;
            m_s2 = s2;
        }

        @Override
        public String toString ()
        {
            return String.format ("%s%s%s", m_s1, m_i, m_s2);
        }

        public final Integer m_i;

        public final String m_s1;

        public final String m_s2;
    }

    @SuppressWarnings ("serial")
    private class SomeDerivedAssertException extends AssertException
    {
        public SomeDerivedAssertException (final Severity severity, final String s)
        {
            super (severity, s, false);
        }
    }

    private static int bitCount (final Integer i)
    {
        return Integer.bitCount (i);
    }

    @SafeVarargs
    private static void checkComparator (final CompTestEntity[] expected,
        final Function<CompTestEntity, ? extends Comparable<?>>... ex)
    {
        final List<CompTestEntity> ls1 = GenericFactory.newArrayList (expected);

        // Check ascending sort
        for (int i = 0; i < 100; ++i)
        {
            Collections.shuffle (ls1);
            ls1.sort (HcUtil.multilevelComparator (ex));

            assertArrayEquals (expected, ls1.toArray ());
        }

        // Check descending sort
        final CompTestEntity[] reversed = new CompTestEntity[expected.length];
        for (int i = 0, j = expected.length - 1; i < expected.length; i++, j--)
        {
            reversed[i] = expected[j];
        }

        for (int i = 0; i < 100; ++i)
        {
            Collections.shuffle (ls1);
            ls1.sort (HcUtil.descendingComparator (ex));

            assertArrayEquals (reversed, ls1.toArray ());
        }
    }

    private static Integer getNullInteger (@SuppressWarnings ("unused") final Object i)
    {
        return null;
    }

    private static String getNullString (@SuppressWarnings ("unused") final Object i)
    {
        return null;
    }

    // 2011-05-20T10:20:30
    private static final GregorianCalendar DT_20110520_102030 = new GregorianCalendar (2011, 5 - 1, 20, 10, 20, 30);

    private static final String TRICKY_FORMAT_STRING = "TGR#$%#$%#$%#";
}
