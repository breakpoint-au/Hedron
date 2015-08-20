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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import org.junit.Test;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.GenericFactory;
import au.com.breakpoint.hedron.core.TimedScope;
import au.com.breakpoint.hedron.core.Tuple.E3;
import au.com.breakpoint.hedron.core.context.ThreadContext;

public class HcUtilGetDifferencesTest
{
    @Test
    public void testGetDifferencesCorrectnessCustom ()
    {
        testGetDifferencesCorrectness (DiffMode.Custom);
    }

    @Test
    public void testGetDifferencesCorrectnessList ()
    {
        testGetDifferencesCorrectness (DiffMode.ListBased);
    }

    @Test
    public void testGetDifferencesCorrectnessSet ()
    {
        testGetDifferencesCorrectness (DiffMode.SetBased);
    }

    @Test
    public void testGetDifferencesSpeed ()
    {
        if (m_performTest)
        {
            final long overlapCount = COUNT / 2L;
            System.out.println ("Building lists");
            final List<String> lhs = getList (START_VALUE);
            final List<String> rhs = getList (START_VALUE - overlapCount);

            runTest (lhs, rhs, overlapCount, DiffMode.ListBased);
            runTest (lhs, rhs, overlapCount, DiffMode.SetBased);
            runTest (lhs, rhs, overlapCount, DiffMode.Custom);

            System.out.println (HcUtil.getSummaryData (true));
        }
    }

    private <T> E3<List<T>, List<T>, List<T>> getDiffs (final List<T> lhs, final List<T> rhs, final DiffMode mode)
    {
        E3<List<T>, List<T>, List<T>> result = null;
        switch (mode)
        {
            case ListBased:
            {
                result = getDifferencesListBased (lhs, rhs);
                break;
            }

            case SetBased:
            {
                result = getDifferencesSetBased (lhs, rhs);
                break;
            }

            case Custom:
            {
                result = getDifferencesCustom (lhs, rhs);
                break;
            }

            default:
            {
                ThreadContext.assertFault (false, "Unsupported value [%s]", mode);
                break;
            }
        }

        return result;
    }

    private ArrayList<String> getList (final long startValue)
    {
        final ArrayList<String> l = GenericFactory.newArrayList ();
        for (long i = 0; i < COUNT; ++i)
        {
            final String s = Long.toString (startValue + i);
            l.add (s);
        }

        return l;
    }

    private void runTest (final List<String> lhs, final List<String> rhs, final long overlapCount, final DiffMode mode)
    {
        System.out.printf ("Testing mode %s%n", mode);

        final E3<List<String>, List<String>, List<String>> result = getDiffs (lhs, rhs, mode);

        final List<String> leftOnly = result.getE0 ();
        assertEquals (overlapCount, leftOnly.size ());

        final List<String> shared = result.getE1 ();
        assertEquals (overlapCount, shared.size ());

        final List<String> rightOnly = result.getE2 ();
        assertEquals (overlapCount, rightOnly.size ());
    }

    private void testGetDifferencesCorrectness (final DiffMode mode)
    {
        if (true)
        {
            final List<String> lhs = GenericFactory.newArrayList ("a", "b", "c", "d");
            final List<String> rhs = GenericFactory.newArrayList ("a", "b", "c", "d");
            final E3<List<String>, List<String>, List<String>> result = getDiffs (lhs, rhs, mode);
            assertEquals (0, result.getE0 ().size ());
            assertEquals (4, result.getE1 ().size ());
            assertEquals (0, result.getE2 ().size ());
        }

        if (true)
        {
            final List<String> lhs = GenericFactory.newArrayList ("b", "c", "d");
            final List<String> rhs = GenericFactory.newArrayList ("a", "b", "c");
            final E3<List<String>, List<String>, List<String>> result = getDiffs (lhs, rhs, mode);
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

        if (true)
        {
            final List<String> lhs = GenericFactory.newArrayList ("a", "b", "c");
            final List<String> rhs = GenericFactory.newArrayList ("1", "2", "3");
            final E3<List<String>, List<String>, List<String>> result = getDiffs (lhs, rhs, mode);
            assertEquals (3, result.getE0 ().size ());
            assertEquals (0, result.getE1 ().size ());
            assertEquals (3, result.getE2 ().size ());
        }

        if (true)
        {
            final List<Integer> lhs = GenericFactory.newArrayList (1, 2, 3);
            final List<Integer> rhs = GenericFactory.newArrayList (3, 4, 5);
            final E3<List<Integer>, List<Integer>, List<Integer>> result = getDiffs (lhs, rhs, mode);

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
    }

    private enum DiffMode
    {
        Custom, ListBased, SetBased;
    }

    private static <T> E3<List<T>, List<T>, List<T>> getDifferencesCustom (final List<T> lhs, final List<T> rhs)
    {
        final Supplier<? extends E3<List<T>, List<T>, List<T>>> task = () ->
        {
            final List<T> leftOnlyList = GenericFactory.newArrayList ();
            final List<T> sharedList = GenericFactory.newArrayList ();

            final Set<T> rhsSet = GenericFactory.newHashSet (rhs);
            for (final T t : lhs)
            {
                if (rhsSet.remove (t))
                {
                    /* It was in the rhs, hence shared */
                    sharedList.add (t);
                }
                else
                {
                    /* It was not in the rhs, hence left only */
                    leftOnlyList.add (t);
                }
            }

            // Anything left in the rhsSet is rhs-only.
            final List<T> rightOnlyList = GenericFactory.newArrayList (rhsSet);

            return E3.of (leftOnlyList, sharedList, rightOnlyList);
        };

        return m_timedScopeGetDifferencesCustom.execute (task);
    }

    private static <T> E3<List<T>, List<T>, List<T>> getDifferencesListBased (final List<T> lhs, final List<T> rhs)
    {
        final Supplier<? extends E3<List<T>, List<T>, List<T>>> task = () ->
        {
            final List<T> leftOnly = GenericFactory.newArrayList (lhs);
            leftOnly.removeAll (rhs);

            final List<T> shared = GenericFactory.newArrayList (lhs);
            shared.removeAll (leftOnly);

            final List<T> rightOnly = GenericFactory.newArrayList (rhs);
            rightOnly.removeAll (lhs);

            return E3.of (leftOnly, shared, rightOnly);
        };

        return m_timedScopeGetDifferencesListBased.execute (task);
    }

    private static <T> E3<List<T>, List<T>, List<T>> getDifferencesSetBased (final List<T> lhs, final List<T> rhs)
    {
        final Supplier<? extends E3<List<T>, List<T>, List<T>>> task = () ->
        {
            final Set<T> leftOnly = GenericFactory.newHashSet (lhs);
            leftOnly.removeAll (rhs);

            final Set<T> shared = GenericFactory.newHashSet (lhs);
            shared.removeAll (leftOnly);

            final Set<T> rightOnly = GenericFactory.newHashSet (rhs);
            rightOnly.removeAll (lhs);

            final List<T> leftOnlyList = GenericFactory.newArrayList (leftOnly);
            final List<T> sharedList = GenericFactory.newArrayList (shared);
            final List<T> rightOnlyList = GenericFactory.newArrayList (rightOnly);
            return E3.of (leftOnlyList, sharedList, rightOnlyList);
        };

        return m_timedScopeGetDifferencesSetBased.execute (task);
    }

    private static final long COUNT = 30_000L;

    //TimedScope,msecTotal,Executions,Successful,Slow,msecMin,msecMax,msecAverage,msecInitAverage,msecHistogram
    //"HcUtilGetDifferencesTest.getDifferencesCustom",19.049039,5,5,0,0.020684,18.350371,3.8098077999999997,0.174667,"[0-1:4; 16-31:1]"
    //"HcUtilGetDifferencesTest.getDifferencesListBased",12546.297656,5,5,0,0.012641,12546.219898,2509.2595312,0.0194395,"[0-1:4; 8192-16383:1]"
    //"HcUtilGetDifferencesTest.getDifferencesSetBased",9648.624517,5,5,0,0.045199,9648.398139,1929.7249034000001,0.0565945,"[0-1:4; 8192-16383:1]"

    private static final boolean m_performTest = false;

    private static final TimedScope m_timedScopeGetDifferencesCustom =
        TimedScope.of (HcUtilGetDifferencesTest.class, "getDifferencesCustom");

    private static final TimedScope m_timedScopeGetDifferencesListBased =
        TimedScope.of (HcUtilGetDifferencesTest.class, "getDifferencesListBased");

    private static final TimedScope m_timedScopeGetDifferencesSetBased =
        TimedScope.of (HcUtilGetDifferencesTest.class, "getDifferencesSetBased");

    private static final long START_VALUE = Long.MAX_VALUE / 2;
}
