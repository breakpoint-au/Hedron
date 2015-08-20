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

import org.junit.Test;
import au.com.breakpoint.hedron.core.HcUtilFile;
import au.com.breakpoint.hedron.core.SmartFile;

public class TupleCodeGen
{
    @Test
    public void test ()
    {
        if (GENERATE_CODE)
        {
            SmartFile f = null;
            try
            {
                f = new SmartFile ("c:/temp/TupleCode.java");

                for (int i = MIN_TUPLE; i <= MAX_TUPLE; ++i)
                {
                    generateCode (f, i);
                }

                for (int i = MIN_TUPLE; i <= MAX_TUPLE; ++i)
                {
                    generateTestCode (f, i);
                }
            }
            finally
            {
                HcUtilFile.safeClose (f);
            }
        }
    }

    private String commaSeparate (final String format, final int n)
    {
        final StringBuilder sb = new StringBuilder ();

        for (int i = 0; i < n; ++i)
        {
            sb.append (String.format (format, i, i, i, i, i, i));
            if (i < n - 1)
            {
                sb.append (", ");
            }
        }

        return sb.toString ();
    }

    private void generateCode (final SmartFile f, final int n)
    {
        final String tList = commaSeparate ("T%s", n);
        final String argTypeList = commaSeparate ("final T%s e%s", n);
        final String argRefList = commaSeparate ("e%s", n);
        final String memberList = commaSeparate ("m_e%s", n);
        final String memberListRhs = commaSeparate ("eRhs.m_e%s", n);

        f.printf ("    /**%n");
        f.printf ("     * Immutable typesafe tuple with %s elements.%n", n);
        f.printf ("     */%n");
        f.printf ("    final public static class E%s<%s> implements Serializable%n", n, tList);
        f.printf ("    {%n");
        f.printf ("        public E%s (%s)%n", n, argTypeList);
        f.printf ("        {%n");
        for (int i = 0; i < n; ++i)
        {
            f.printf ("            m_e%s = e%s;%n", i, i);
        }
        f.printf ("        }%n");
        for (int i = 0; i < n; ++i)
        {
            f.printf ("%n");
            f.printf ("        public T%s getE%s ()%n", i, i);
            f.printf ("        {%n");
            f.printf ("            return m_e%s;%n", i);
            f.printf ("        }%n");
        }
        f.printf ("%n");
        f.printf ("        /** DRY convenience factory */%n");
        f.printf ("        public static <%s> E%s<%s> of (%s)%n", tList, n, tList, argTypeList);
        f.printf ("        {%n");
        f.printf ("            return new E%s<%s> (%s);%n", n, tList, argRefList);
        f.printf ("        }%n");

        f.printf ("%n");
        f.printf ("        @Override%n");
        f.printf ("        public boolean equals (final Object o)%n");
        f.printf ("        {%n");
        f.printf ("            boolean isEqual = false;%n");
        f.printf ("            if (this == o)%n");
        f.printf ("            {%n");
        f.printf ("                isEqual = true;%n");
        f.printf ("            }%n");
        f.printf ("            else if (o != null && getClass () == o.getClass ())%n");
        f.printf ("            {%n");
        f.printf ("                @SuppressWarnings (\"unchecked\") final E%s<%s> eRhs = (E%s<%s>) o;%n", n, tList, n,
            tList);
        if (n == 1)
        {
            f.printf ("                isEqual = Objects.deepEquals (%s, %s);%n", memberList, memberListRhs);
        }
        else
        {
            f.printf ("                isEqual = Arrays.deepEquals (new Object[] { %s }, new Object[] { %s });%n",
                memberList, memberListRhs);
        }
        f.printf ("            }%n");
        f.printf ("            return isEqual;%n");
        f.printf ("        }%n");
        f.printf ("%n");
        f.printf ("        @Override%n");
        f.printf ("        public int hashCode ()%n");
        f.printf ("        {%n");
        f.printf ("            // See Effective Java 2nd edition Item 9.%n");
        f.printf ("            return HcUtil.deepHashCode (%s);%n", memberList);
        f.printf ("        }%n");
        f.printf ("%n");
        f.printf ("        @Override%n");
        f.printf ("        public String toString ()%n");
        f.printf ("        {%n");
        f.printf ("            return HcUtil.deepToString (%s);%n", memberList);
        f.printf ("        }%n");
        f.printf ("%n");
        for (int i = 0; i < n; ++i)
        {
            f.printf ("        private final T%s m_e%s;%n", i, i);
        }
        f.printf ("%n");
        f.printf ("        private static final long serialVersionUID = 1L;%n");
        f.printf ("    }%n");
        f.printf ("%n");
    }

    private void generateTestCode (final SmartFile f, final int n)
    {
        final String stringTuple = commaSeparate ("String", n);
        final String argValues = commaSeparate ("\"%s\"", n);

        f.printf ("%n");
        f.printf ("    @Test%n");
        f.printf ("    public void testE%s ()%n", n);
        f.printf ("    {%n");
        f.printf ("        final E%s<%s> p = E%s.of (%s);%n", n, stringTuple, n, argValues);
        for (int i = 0; i < n; ++i)
        {
            f.printf ("        assertEquals (\"%s\", p.getE%s ());%n", i, i);
        }
        f.printf ("%n");
        f.printf ("        assertEquals (p, E%s.of (%s));%n", n, argValues);
        f.printf ("    }%n");
    }

    private static final boolean GENERATE_CODE = false;

    private static final int MAX_TUPLE = 20;

    private static final int MIN_TUPLE = 1;
}
