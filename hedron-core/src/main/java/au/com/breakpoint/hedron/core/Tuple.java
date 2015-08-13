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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class Tuple
{
    /**
     * Immutable typesafe tuple with 1 elements.
     */
    final public static class E1<T0> implements Serializable
    {
        public E1 (final T0 e0)
        {
            m_e0 = e0;
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
                @SuppressWarnings ("unchecked")
                final E1<T0> eRhs = (E1<T0>) o;
                isEqual = Objects.deepEquals (m_e0, eRhs.m_e0);
            }
            return isEqual;
        }

        public T0 getE0 ()
        {
            return m_e0;
        }

        @Override
        public int hashCode ()
        {
            // See Effective Java 2nd edition Item 9.
            return HcUtil.deepHashCode (m_e0);
        }

        @Override
        public String toString ()
        {
            return HcUtil.deepToString (m_e0);
        }

        /** DRY convenience factory */
        public static <T0> E1<T0> of (final T0 e0)
        {
            return new E1<T0> (e0);
        }

        private final T0 m_e0;

        private static final long serialVersionUID = 1L;
    }

    /**
     * Immutable typesafe tuple with 10 elements.
     */
    final public static class E10<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> implements Serializable
    {
        public E10 (final T0 e0, final T1 e1, final T2 e2, final T3 e3, final T4 e4, final T5 e5, final T6 e6,
            final T7 e7, final T8 e8, final T9 e9)
        {
            m_e0 = e0;
            m_e1 = e1;
            m_e2 = e2;
            m_e3 = e3;
            m_e4 = e4;
            m_e5 = e5;
            m_e6 = e6;
            m_e7 = e7;
            m_e8 = e8;
            m_e9 = e9;
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
                @SuppressWarnings ("unchecked")
                final E10<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> eRhs =
                    (E10<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9>) o;
                isEqual = Arrays.deepEquals (new Object[]
                {
                        m_e0,
                        m_e1,
                        m_e2,
                        m_e3,
                        m_e4,
                        m_e5,
                        m_e6,
                        m_e7,
                        m_e8,
                        m_e9
                }, new Object[]
                {
                        eRhs.m_e0,
                        eRhs.m_e1,
                        eRhs.m_e2,
                        eRhs.m_e3,
                        eRhs.m_e4,
                        eRhs.m_e5,
                        eRhs.m_e6,
                        eRhs.m_e7,
                        eRhs.m_e8,
                        eRhs.m_e9
                });
            }
            return isEqual;
        }

        public T0 getE0 ()
        {
            return m_e0;
        }

        public T1 getE1 ()
        {
            return m_e1;
        }

        public T2 getE2 ()
        {
            return m_e2;
        }

        public T3 getE3 ()
        {
            return m_e3;
        }

        public T4 getE4 ()
        {
            return m_e4;
        }

        public T5 getE5 ()
        {
            return m_e5;
        }

        public T6 getE6 ()
        {
            return m_e6;
        }

        public T7 getE7 ()
        {
            return m_e7;
        }

        public T8 getE8 ()
        {
            return m_e8;
        }

        public T9 getE9 ()
        {
            return m_e9;
        }

        @Override
        public int hashCode ()
        {
            // See Effective Java 2nd edition Item 9.
            return HcUtil.deepHashCode (m_e0, m_e1, m_e2, m_e3, m_e4, m_e5, m_e6, m_e7, m_e8, m_e9);
        }

        @Override
        public String toString ()
        {
            return HcUtil.deepToString (m_e0, m_e1, m_e2, m_e3, m_e4, m_e5, m_e6, m_e7, m_e8, m_e9);
        }

        /** DRY convenience factory */
        public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> E10<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> of (
            final T0 e0, final T1 e1, final T2 e2, final T3 e3, final T4 e4, final T5 e5, final T6 e6, final T7 e7,
            final T8 e8, final T9 e9)
        {
            return new E10<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> (e0, e1, e2, e3, e4, e5, e6, e7, e8, e9);
        }

        private final T0 m_e0;

        private final T1 m_e1;

        private final T2 m_e2;

        private final T3 m_e3;

        private final T4 m_e4;

        private final T5 m_e5;

        private final T6 m_e6;

        private final T7 m_e7;

        private final T8 m_e8;

        private final T9 m_e9;

        private static final long serialVersionUID = 1L;
    }

    /**
     * Immutable typesafe tuple with 11 elements.
     */
    final public static class E11<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> implements Serializable
    {
        public E11 (final T0 e0, final T1 e1, final T2 e2, final T3 e3, final T4 e4, final T5 e5, final T6 e6,
            final T7 e7, final T8 e8, final T9 e9, final T10 e10)
        {
            m_e0 = e0;
            m_e1 = e1;
            m_e2 = e2;
            m_e3 = e3;
            m_e4 = e4;
            m_e5 = e5;
            m_e6 = e6;
            m_e7 = e7;
            m_e8 = e8;
            m_e9 = e9;
            m_e10 = e10;
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
                @SuppressWarnings ("unchecked")
                final E11<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> eRhs =
                    (E11<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>) o;
                isEqual = Arrays.deepEquals (new Object[]
                {
                        m_e0,
                        m_e1,
                        m_e2,
                        m_e3,
                        m_e4,
                        m_e5,
                        m_e6,
                        m_e7,
                        m_e8,
                        m_e9,
                        m_e10
                }, new Object[]
                {
                        eRhs.m_e0,
                        eRhs.m_e1,
                        eRhs.m_e2,
                        eRhs.m_e3,
                        eRhs.m_e4,
                        eRhs.m_e5,
                        eRhs.m_e6,
                        eRhs.m_e7,
                        eRhs.m_e8,
                        eRhs.m_e9,
                        eRhs.m_e10
                });
            }
            return isEqual;
        }

        public T0 getE0 ()
        {
            return m_e0;
        }

        public T1 getE1 ()
        {
            return m_e1;
        }

        public T10 getE10 ()
        {
            return m_e10;
        }

        public T2 getE2 ()
        {
            return m_e2;
        }

        public T3 getE3 ()
        {
            return m_e3;
        }

        public T4 getE4 ()
        {
            return m_e4;
        }

        public T5 getE5 ()
        {
            return m_e5;
        }

        public T6 getE6 ()
        {
            return m_e6;
        }

        public T7 getE7 ()
        {
            return m_e7;
        }

        public T8 getE8 ()
        {
            return m_e8;
        }

        public T9 getE9 ()
        {
            return m_e9;
        }

        @Override
        public int hashCode ()
        {
            // See Effective Java 2nd edition Item 9.
            return HcUtil.deepHashCode (m_e0, m_e1, m_e2, m_e3, m_e4, m_e5, m_e6, m_e7, m_e8, m_e9, m_e10);
        }

        @Override
        public String toString ()
        {
            return HcUtil.deepToString (m_e0, m_e1, m_e2, m_e3, m_e4, m_e5, m_e6, m_e7, m_e8, m_e9, m_e10);
        }

        /** DRY convenience factory */
        public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> E11<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> of (
            final T0 e0, final T1 e1, final T2 e2, final T3 e3, final T4 e4, final T5 e5, final T6 e6, final T7 e7,
            final T8 e8, final T9 e9, final T10 e10)
        {
            return new E11<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> (e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10);
        }

        private final T0 m_e0;

        private final T1 m_e1;

        private final T10 m_e10;

        private final T2 m_e2;

        private final T3 m_e3;

        private final T4 m_e4;

        private final T5 m_e5;

        private final T6 m_e6;

        private final T7 m_e7;

        private final T8 m_e8;

        private final T9 m_e9;

        private static final long serialVersionUID = 1L;
    }

    /**
     * Immutable typesafe tuple with 12 elements.
     */
    final public static class E12<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> implements Serializable
    {
        public E12 (final T0 e0, final T1 e1, final T2 e2, final T3 e3, final T4 e4, final T5 e5, final T6 e6,
            final T7 e7, final T8 e8, final T9 e9, final T10 e10, final T11 e11)
        {
            m_e0 = e0;
            m_e1 = e1;
            m_e2 = e2;
            m_e3 = e3;
            m_e4 = e4;
            m_e5 = e5;
            m_e6 = e6;
            m_e7 = e7;
            m_e8 = e8;
            m_e9 = e9;
            m_e10 = e10;
            m_e11 = e11;
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
                @SuppressWarnings ("unchecked")
                final E12<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> eRhs =
                    (E12<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>) o;
                isEqual = Arrays.deepEquals (new Object[]
                {
                        m_e0,
                        m_e1,
                        m_e2,
                        m_e3,
                        m_e4,
                        m_e5,
                        m_e6,
                        m_e7,
                        m_e8,
                        m_e9,
                        m_e10,
                        m_e11
                }, new Object[]
                {
                        eRhs.m_e0,
                        eRhs.m_e1,
                        eRhs.m_e2,
                        eRhs.m_e3,
                        eRhs.m_e4,
                        eRhs.m_e5,
                        eRhs.m_e6,
                        eRhs.m_e7,
                        eRhs.m_e8,
                        eRhs.m_e9,
                        eRhs.m_e10,
                        eRhs.m_e11
                });
            }
            return isEqual;
        }

        public T0 getE0 ()
        {
            return m_e0;
        }

        public T1 getE1 ()
        {
            return m_e1;
        }

        public T10 getE10 ()
        {
            return m_e10;
        }

        public T11 getE11 ()
        {
            return m_e11;
        }

        public T2 getE2 ()
        {
            return m_e2;
        }

        public T3 getE3 ()
        {
            return m_e3;
        }

        public T4 getE4 ()
        {
            return m_e4;
        }

        public T5 getE5 ()
        {
            return m_e5;
        }

        public T6 getE6 ()
        {
            return m_e6;
        }

        public T7 getE7 ()
        {
            return m_e7;
        }

        public T8 getE8 ()
        {
            return m_e8;
        }

        public T9 getE9 ()
        {
            return m_e9;
        }

        @Override
        public int hashCode ()
        {
            // See Effective Java 2nd edition Item 9.
            return HcUtil.deepHashCode (m_e0, m_e1, m_e2, m_e3, m_e4, m_e5, m_e6, m_e7, m_e8, m_e9, m_e10, m_e11);
        }

        @Override
        public String toString ()
        {
            return HcUtil.deepToString (m_e0, m_e1, m_e2, m_e3, m_e4, m_e5, m_e6, m_e7, m_e8, m_e9, m_e10, m_e11);
        }

        /** DRY convenience factory */
        public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> E12<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> of (
            final T0 e0, final T1 e1, final T2 e2, final T3 e3, final T4 e4, final T5 e5, final T6 e6, final T7 e7,
            final T8 e8, final T9 e9, final T10 e10, final T11 e11)
        {
            return new E12<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> (e0, e1, e2, e3, e4, e5, e6, e7, e8, e9,
                e10, e11);
        }

        private final T0 m_e0;

        private final T1 m_e1;

        private final T10 m_e10;

        private final T11 m_e11;

        private final T2 m_e2;

        private final T3 m_e3;

        private final T4 m_e4;

        private final T5 m_e5;

        private final T6 m_e6;

        private final T7 m_e7;

        private final T8 m_e8;

        private final T9 m_e9;

        private static final long serialVersionUID = 1L;
    }

    /**
     * Immutable typesafe tuple with 13 elements.
     */
    final public static class E13<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> implements Serializable
    {
        public E13 (final T0 e0, final T1 e1, final T2 e2, final T3 e3, final T4 e4, final T5 e5, final T6 e6,
            final T7 e7, final T8 e8, final T9 e9, final T10 e10, final T11 e11, final T12 e12)
        {
            m_e0 = e0;
            m_e1 = e1;
            m_e2 = e2;
            m_e3 = e3;
            m_e4 = e4;
            m_e5 = e5;
            m_e6 = e6;
            m_e7 = e7;
            m_e8 = e8;
            m_e9 = e9;
            m_e10 = e10;
            m_e11 = e11;
            m_e12 = e12;
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
                @SuppressWarnings ("unchecked")
                final E13<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> eRhs =
                    (E13<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>) o;
                isEqual = Arrays.deepEquals (new Object[]
                {
                        m_e0,
                        m_e1,
                        m_e2,
                        m_e3,
                        m_e4,
                        m_e5,
                        m_e6,
                        m_e7,
                        m_e8,
                        m_e9,
                        m_e10,
                        m_e11,
                        m_e12
                }, new Object[]
                {
                        eRhs.m_e0,
                        eRhs.m_e1,
                        eRhs.m_e2,
                        eRhs.m_e3,
                        eRhs.m_e4,
                        eRhs.m_e5,
                        eRhs.m_e6,
                        eRhs.m_e7,
                        eRhs.m_e8,
                        eRhs.m_e9,
                        eRhs.m_e10,
                        eRhs.m_e11,
                        eRhs.m_e12
                });
            }
            return isEqual;
        }

        public T0 getE0 ()
        {
            return m_e0;
        }

        public T1 getE1 ()
        {
            return m_e1;
        }

        public T10 getE10 ()
        {
            return m_e10;
        }

        public T11 getE11 ()
        {
            return m_e11;
        }

        public T12 getE12 ()
        {
            return m_e12;
        }

        public T2 getE2 ()
        {
            return m_e2;
        }

        public T3 getE3 ()
        {
            return m_e3;
        }

        public T4 getE4 ()
        {
            return m_e4;
        }

        public T5 getE5 ()
        {
            return m_e5;
        }

        public T6 getE6 ()
        {
            return m_e6;
        }

        public T7 getE7 ()
        {
            return m_e7;
        }

        public T8 getE8 ()
        {
            return m_e8;
        }

        public T9 getE9 ()
        {
            return m_e9;
        }

        @Override
        public int hashCode ()
        {
            // See Effective Java 2nd edition Item 9.
            return HcUtil.deepHashCode (m_e0, m_e1, m_e2, m_e3, m_e4, m_e5, m_e6, m_e7, m_e8, m_e9, m_e10, m_e11,
                m_e12);
        }

        @Override
        public String toString ()
        {
            return HcUtil.deepToString (m_e0, m_e1, m_e2, m_e3, m_e4, m_e5, m_e6, m_e7, m_e8, m_e9, m_e10, m_e11,
                m_e12);
        }

        /** DRY convenience factory */
        public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> E13<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> of (
            final T0 e0, final T1 e1, final T2 e2, final T3 e3, final T4 e4, final T5 e5, final T6 e6, final T7 e7,
            final T8 e8, final T9 e9, final T10 e10, final T11 e11, final T12 e12)
        {
            return new E13<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> (e0, e1, e2, e3, e4, e5, e6, e7, e8,
                e9, e10, e11, e12);
        }

        private final T0 m_e0;

        private final T1 m_e1;

        private final T10 m_e10;

        private final T11 m_e11;

        private final T12 m_e12;

        private final T2 m_e2;

        private final T3 m_e3;

        private final T4 m_e4;

        private final T5 m_e5;

        private final T6 m_e6;

        private final T7 m_e7;

        private final T8 m_e8;

        private final T9 m_e9;

        private static final long serialVersionUID = 1L;
    }

    /**
     * Immutable typesafe tuple with 14 elements.
     */
    final public static class E14<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> implements Serializable
    {
        public E14 (final T0 e0, final T1 e1, final T2 e2, final T3 e3, final T4 e4, final T5 e5, final T6 e6,
            final T7 e7, final T8 e8, final T9 e9, final T10 e10, final T11 e11, final T12 e12, final T13 e13)
        {
            m_e0 = e0;
            m_e1 = e1;
            m_e2 = e2;
            m_e3 = e3;
            m_e4 = e4;
            m_e5 = e5;
            m_e6 = e6;
            m_e7 = e7;
            m_e8 = e8;
            m_e9 = e9;
            m_e10 = e10;
            m_e11 = e11;
            m_e12 = e12;
            m_e13 = e13;
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
                @SuppressWarnings ("unchecked")
                final E14<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> eRhs =
                    (E14<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>) o;
                isEqual = Arrays.deepEquals (new Object[]
                {
                        m_e0,
                        m_e1,
                        m_e2,
                        m_e3,
                        m_e4,
                        m_e5,
                        m_e6,
                        m_e7,
                        m_e8,
                        m_e9,
                        m_e10,
                        m_e11,
                        m_e12,
                        m_e13
                }, new Object[]
                {
                        eRhs.m_e0,
                        eRhs.m_e1,
                        eRhs.m_e2,
                        eRhs.m_e3,
                        eRhs.m_e4,
                        eRhs.m_e5,
                        eRhs.m_e6,
                        eRhs.m_e7,
                        eRhs.m_e8,
                        eRhs.m_e9,
                        eRhs.m_e10,
                        eRhs.m_e11,
                        eRhs.m_e12,
                        eRhs.m_e13
                });
            }
            return isEqual;
        }

        public T0 getE0 ()
        {
            return m_e0;
        }

        public T1 getE1 ()
        {
            return m_e1;
        }

        public T10 getE10 ()
        {
            return m_e10;
        }

        public T11 getE11 ()
        {
            return m_e11;
        }

        public T12 getE12 ()
        {
            return m_e12;
        }

        public T13 getE13 ()
        {
            return m_e13;
        }

        public T2 getE2 ()
        {
            return m_e2;
        }

        public T3 getE3 ()
        {
            return m_e3;
        }

        public T4 getE4 ()
        {
            return m_e4;
        }

        public T5 getE5 ()
        {
            return m_e5;
        }

        public T6 getE6 ()
        {
            return m_e6;
        }

        public T7 getE7 ()
        {
            return m_e7;
        }

        public T8 getE8 ()
        {
            return m_e8;
        }

        public T9 getE9 ()
        {
            return m_e9;
        }

        @Override
        public int hashCode ()
        {
            // See Effective Java 2nd edition Item 9.
            return HcUtil.deepHashCode (m_e0, m_e1, m_e2, m_e3, m_e4, m_e5, m_e6, m_e7, m_e8, m_e9, m_e10, m_e11, m_e12,
                m_e13);
        }

        @Override
        public String toString ()
        {
            return HcUtil.deepToString (m_e0, m_e1, m_e2, m_e3, m_e4, m_e5, m_e6, m_e7, m_e8, m_e9, m_e10, m_e11, m_e12,
                m_e13);
        }

        /** DRY convenience factory */
        public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> E14<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> of (
            final T0 e0, final T1 e1, final T2 e2, final T3 e3, final T4 e4, final T5 e5, final T6 e6, final T7 e7,
            final T8 e8, final T9 e9, final T10 e10, final T11 e11, final T12 e12, final T13 e13)
        {
            return new E14<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> (e0, e1, e2, e3, e4, e5, e6, e7,
                e8, e9, e10, e11, e12, e13);
        }

        private final T0 m_e0;

        private final T1 m_e1;

        private final T10 m_e10;

        private final T11 m_e11;

        private final T12 m_e12;

        private final T13 m_e13;

        private final T2 m_e2;

        private final T3 m_e3;

        private final T4 m_e4;

        private final T5 m_e5;

        private final T6 m_e6;

        private final T7 m_e7;

        private final T8 m_e8;

        private final T9 m_e9;

        private static final long serialVersionUID = 1L;
    }

    /**
     * Immutable typesafe tuple with 15 elements.
     */
    final public static class E15<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>
        implements Serializable
    {
        public E15 (final T0 e0, final T1 e1, final T2 e2, final T3 e3, final T4 e4, final T5 e5, final T6 e6,
            final T7 e7, final T8 e8, final T9 e9, final T10 e10, final T11 e11, final T12 e12, final T13 e13,
            final T14 e14)
        {
            m_e0 = e0;
            m_e1 = e1;
            m_e2 = e2;
            m_e3 = e3;
            m_e4 = e4;
            m_e5 = e5;
            m_e6 = e6;
            m_e7 = e7;
            m_e8 = e8;
            m_e9 = e9;
            m_e10 = e10;
            m_e11 = e11;
            m_e12 = e12;
            m_e13 = e13;
            m_e14 = e14;
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
                @SuppressWarnings ("unchecked")
                final E15<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> eRhs =
                    (E15<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>) o;
                isEqual = Arrays.deepEquals (new Object[]
                {
                        m_e0,
                        m_e1,
                        m_e2,
                        m_e3,
                        m_e4,
                        m_e5,
                        m_e6,
                        m_e7,
                        m_e8,
                        m_e9,
                        m_e10,
                        m_e11,
                        m_e12,
                        m_e13,
                        m_e14
                }, new Object[]
                {
                        eRhs.m_e0,
                        eRhs.m_e1,
                        eRhs.m_e2,
                        eRhs.m_e3,
                        eRhs.m_e4,
                        eRhs.m_e5,
                        eRhs.m_e6,
                        eRhs.m_e7,
                        eRhs.m_e8,
                        eRhs.m_e9,
                        eRhs.m_e10,
                        eRhs.m_e11,
                        eRhs.m_e12,
                        eRhs.m_e13,
                        eRhs.m_e14
                });
            }
            return isEqual;
        }

        public T0 getE0 ()
        {
            return m_e0;
        }

        public T1 getE1 ()
        {
            return m_e1;
        }

        public T10 getE10 ()
        {
            return m_e10;
        }

        public T11 getE11 ()
        {
            return m_e11;
        }

        public T12 getE12 ()
        {
            return m_e12;
        }

        public T13 getE13 ()
        {
            return m_e13;
        }

        public T14 getE14 ()
        {
            return m_e14;
        }

        public T2 getE2 ()
        {
            return m_e2;
        }

        public T3 getE3 ()
        {
            return m_e3;
        }

        public T4 getE4 ()
        {
            return m_e4;
        }

        public T5 getE5 ()
        {
            return m_e5;
        }

        public T6 getE6 ()
        {
            return m_e6;
        }

        public T7 getE7 ()
        {
            return m_e7;
        }

        public T8 getE8 ()
        {
            return m_e8;
        }

        public T9 getE9 ()
        {
            return m_e9;
        }

        @Override
        public int hashCode ()
        {
            // See Effective Java 2nd edition Item 9.
            return HcUtil.deepHashCode (m_e0, m_e1, m_e2, m_e3, m_e4, m_e5, m_e6, m_e7, m_e8, m_e9, m_e10, m_e11, m_e12,
                m_e13, m_e14);
        }

        @Override
        public String toString ()
        {
            return HcUtil.deepToString (m_e0, m_e1, m_e2, m_e3, m_e4, m_e5, m_e6, m_e7, m_e8, m_e9, m_e10, m_e11, m_e12,
                m_e13, m_e14);
        }

        /** DRY convenience factory */
        public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> E15<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> of (
            final T0 e0, final T1 e1, final T2 e2, final T3 e3, final T4 e4, final T5 e5, final T6 e6, final T7 e7,
            final T8 e8, final T9 e9, final T10 e10, final T11 e11, final T12 e12, final T13 e13, final T14 e14)
        {
            return new E15<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> (e0, e1, e2, e3, e4, e5, e6,
                e7, e8, e9, e10, e11, e12, e13, e14);
        }

        private final T0 m_e0;

        private final T1 m_e1;

        private final T10 m_e10;

        private final T11 m_e11;

        private final T12 m_e12;

        private final T13 m_e13;

        private final T14 m_e14;

        private final T2 m_e2;

        private final T3 m_e3;

        private final T4 m_e4;

        private final T5 m_e5;

        private final T6 m_e6;

        private final T7 m_e7;

        private final T8 m_e8;

        private final T9 m_e9;

        private static final long serialVersionUID = 1L;
    }

    /**
     * Immutable typesafe tuple with 16 elements.
     */
    final public static class E16<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>
        implements Serializable
    {
        public E16 (final T0 e0, final T1 e1, final T2 e2, final T3 e3, final T4 e4, final T5 e5, final T6 e6,
            final T7 e7, final T8 e8, final T9 e9, final T10 e10, final T11 e11, final T12 e12, final T13 e13,
            final T14 e14, final T15 e15)
        {
            m_e0 = e0;
            m_e1 = e1;
            m_e2 = e2;
            m_e3 = e3;
            m_e4 = e4;
            m_e5 = e5;
            m_e6 = e6;
            m_e7 = e7;
            m_e8 = e8;
            m_e9 = e9;
            m_e10 = e10;
            m_e11 = e11;
            m_e12 = e12;
            m_e13 = e13;
            m_e14 = e14;
            m_e15 = e15;
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
                @SuppressWarnings ("unchecked")
                final E16<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> eRhs =
                    (E16<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>) o;
                isEqual = Arrays.deepEquals (new Object[]
                {
                        m_e0,
                        m_e1,
                        m_e2,
                        m_e3,
                        m_e4,
                        m_e5,
                        m_e6,
                        m_e7,
                        m_e8,
                        m_e9,
                        m_e10,
                        m_e11,
                        m_e12,
                        m_e13,
                        m_e14,
                        m_e15
                }, new Object[]
                {
                        eRhs.m_e0,
                        eRhs.m_e1,
                        eRhs.m_e2,
                        eRhs.m_e3,
                        eRhs.m_e4,
                        eRhs.m_e5,
                        eRhs.m_e6,
                        eRhs.m_e7,
                        eRhs.m_e8,
                        eRhs.m_e9,
                        eRhs.m_e10,
                        eRhs.m_e11,
                        eRhs.m_e12,
                        eRhs.m_e13,
                        eRhs.m_e14,
                        eRhs.m_e15
                });
            }
            return isEqual;
        }

        public T0 getE0 ()
        {
            return m_e0;
        }

        public T1 getE1 ()
        {
            return m_e1;
        }

        public T10 getE10 ()
        {
            return m_e10;
        }

        public T11 getE11 ()
        {
            return m_e11;
        }

        public T12 getE12 ()
        {
            return m_e12;
        }

        public T13 getE13 ()
        {
            return m_e13;
        }

        public T14 getE14 ()
        {
            return m_e14;
        }

        public T15 getE15 ()
        {
            return m_e15;
        }

        public T2 getE2 ()
        {
            return m_e2;
        }

        public T3 getE3 ()
        {
            return m_e3;
        }

        public T4 getE4 ()
        {
            return m_e4;
        }

        public T5 getE5 ()
        {
            return m_e5;
        }

        public T6 getE6 ()
        {
            return m_e6;
        }

        public T7 getE7 ()
        {
            return m_e7;
        }

        public T8 getE8 ()
        {
            return m_e8;
        }

        public T9 getE9 ()
        {
            return m_e9;
        }

        @Override
        public int hashCode ()
        {
            // See Effective Java 2nd edition Item 9.
            return HcUtil.deepHashCode (m_e0, m_e1, m_e2, m_e3, m_e4, m_e5, m_e6, m_e7, m_e8, m_e9, m_e10, m_e11, m_e12,
                m_e13, m_e14, m_e15);
        }

        @Override
        public String toString ()
        {
            return HcUtil.deepToString (m_e0, m_e1, m_e2, m_e3, m_e4, m_e5, m_e6, m_e7, m_e8, m_e9, m_e10, m_e11, m_e12,
                m_e13, m_e14, m_e15);
        }

        /** DRY convenience factory */
        public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> E16<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> of (
            final T0 e0, final T1 e1, final T2 e2, final T3 e3, final T4 e4, final T5 e5, final T6 e6, final T7 e7,
            final T8 e8, final T9 e9, final T10 e10, final T11 e11, final T12 e12, final T13 e13, final T14 e14,
            final T15 e15)
        {
            return new E16<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> (e0, e1, e2, e3, e4,
                e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15);
        }

        private final T0 m_e0;

        private final T1 m_e1;

        private final T10 m_e10;

        private final T11 m_e11;

        private final T12 m_e12;

        private final T13 m_e13;

        private final T14 m_e14;

        private final T15 m_e15;

        private final T2 m_e2;

        private final T3 m_e3;

        private final T4 m_e4;

        private final T5 m_e5;

        private final T6 m_e6;

        private final T7 m_e7;

        private final T8 m_e8;

        private final T9 m_e9;

        private static final long serialVersionUID = 1L;
    }

    /**
     * Immutable typesafe tuple with 17 elements.
     */
    final public static class E17<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>
        implements Serializable
    {
        public E17 (final T0 e0, final T1 e1, final T2 e2, final T3 e3, final T4 e4, final T5 e5, final T6 e6,
            final T7 e7, final T8 e8, final T9 e9, final T10 e10, final T11 e11, final T12 e12, final T13 e13,
            final T14 e14, final T15 e15, final T16 e16)
        {
            m_e0 = e0;
            m_e1 = e1;
            m_e2 = e2;
            m_e3 = e3;
            m_e4 = e4;
            m_e5 = e5;
            m_e6 = e6;
            m_e7 = e7;
            m_e8 = e8;
            m_e9 = e9;
            m_e10 = e10;
            m_e11 = e11;
            m_e12 = e12;
            m_e13 = e13;
            m_e14 = e14;
            m_e15 = e15;
            m_e16 = e16;
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
                @SuppressWarnings ("unchecked")
                final E17<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> eRhs =
                    (E17<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>) o;
                isEqual = Arrays.deepEquals (new Object[]
                {
                        m_e0,
                        m_e1,
                        m_e2,
                        m_e3,
                        m_e4,
                        m_e5,
                        m_e6,
                        m_e7,
                        m_e8,
                        m_e9,
                        m_e10,
                        m_e11,
                        m_e12,
                        m_e13,
                        m_e14,
                        m_e15,
                        m_e16
                }, new Object[]
                {
                        eRhs.m_e0,
                        eRhs.m_e1,
                        eRhs.m_e2,
                        eRhs.m_e3,
                        eRhs.m_e4,
                        eRhs.m_e5,
                        eRhs.m_e6,
                        eRhs.m_e7,
                        eRhs.m_e8,
                        eRhs.m_e9,
                        eRhs.m_e10,
                        eRhs.m_e11,
                        eRhs.m_e12,
                        eRhs.m_e13,
                        eRhs.m_e14,
                        eRhs.m_e15,
                        eRhs.m_e16
                });
            }
            return isEqual;
        }

        public T0 getE0 ()
        {
            return m_e0;
        }

        public T1 getE1 ()
        {
            return m_e1;
        }

        public T10 getE10 ()
        {
            return m_e10;
        }

        public T11 getE11 ()
        {
            return m_e11;
        }

        public T12 getE12 ()
        {
            return m_e12;
        }

        public T13 getE13 ()
        {
            return m_e13;
        }

        public T14 getE14 ()
        {
            return m_e14;
        }

        public T15 getE15 ()
        {
            return m_e15;
        }

        public T16 getE16 ()
        {
            return m_e16;
        }

        public T2 getE2 ()
        {
            return m_e2;
        }

        public T3 getE3 ()
        {
            return m_e3;
        }

        public T4 getE4 ()
        {
            return m_e4;
        }

        public T5 getE5 ()
        {
            return m_e5;
        }

        public T6 getE6 ()
        {
            return m_e6;
        }

        public T7 getE7 ()
        {
            return m_e7;
        }

        public T8 getE8 ()
        {
            return m_e8;
        }

        public T9 getE9 ()
        {
            return m_e9;
        }

        @Override
        public int hashCode ()
        {
            // See Effective Java 2nd edition Item 9.
            return HcUtil.deepHashCode (m_e0, m_e1, m_e2, m_e3, m_e4, m_e5, m_e6, m_e7, m_e8, m_e9, m_e10, m_e11, m_e12,
                m_e13, m_e14, m_e15, m_e16);
        }

        @Override
        public String toString ()
        {
            return HcUtil.deepToString (m_e0, m_e1, m_e2, m_e3, m_e4, m_e5, m_e6, m_e7, m_e8, m_e9, m_e10, m_e11, m_e12,
                m_e13, m_e14, m_e15, m_e16);
        }

        /** DRY convenience factory */
        public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> E17<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> of (
            final T0 e0, final T1 e1, final T2 e2, final T3 e3, final T4 e4, final T5 e5, final T6 e6, final T7 e7,
            final T8 e8, final T9 e9, final T10 e10, final T11 e11, final T12 e12, final T13 e13, final T14 e14,
            final T15 e15, final T16 e16)
        {
            return new E17<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> (e0, e1, e2, e3,
                e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16);
        }

        private final T0 m_e0;

        private final T1 m_e1;

        private final T10 m_e10;

        private final T11 m_e11;

        private final T12 m_e12;

        private final T13 m_e13;

        private final T14 m_e14;

        private final T15 m_e15;

        private final T16 m_e16;

        private final T2 m_e2;

        private final T3 m_e3;

        private final T4 m_e4;

        private final T5 m_e5;

        private final T6 m_e6;

        private final T7 m_e7;

        private final T8 m_e8;

        private final T9 m_e9;

        private static final long serialVersionUID = 1L;
    }

    /**
     * Immutable typesafe tuple with 18 elements.
     */
    final public static class E18<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17>
        implements Serializable
    {
        public E18 (final T0 e0, final T1 e1, final T2 e2, final T3 e3, final T4 e4, final T5 e5, final T6 e6,
            final T7 e7, final T8 e8, final T9 e9, final T10 e10, final T11 e11, final T12 e12, final T13 e13,
            final T14 e14, final T15 e15, final T16 e16, final T17 e17)
        {
            m_e0 = e0;
            m_e1 = e1;
            m_e2 = e2;
            m_e3 = e3;
            m_e4 = e4;
            m_e5 = e5;
            m_e6 = e6;
            m_e7 = e7;
            m_e8 = e8;
            m_e9 = e9;
            m_e10 = e10;
            m_e11 = e11;
            m_e12 = e12;
            m_e13 = e13;
            m_e14 = e14;
            m_e15 = e15;
            m_e16 = e16;
            m_e17 = e17;
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
                @SuppressWarnings ("unchecked")
                final E18<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17> eRhs =
                    (E18<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17>) o;
                isEqual = Arrays.deepEquals (new Object[]
                {
                        m_e0,
                        m_e1,
                        m_e2,
                        m_e3,
                        m_e4,
                        m_e5,
                        m_e6,
                        m_e7,
                        m_e8,
                        m_e9,
                        m_e10,
                        m_e11,
                        m_e12,
                        m_e13,
                        m_e14,
                        m_e15,
                        m_e16,
                        m_e17
                }, new Object[]
                {
                        eRhs.m_e0,
                        eRhs.m_e1,
                        eRhs.m_e2,
                        eRhs.m_e3,
                        eRhs.m_e4,
                        eRhs.m_e5,
                        eRhs.m_e6,
                        eRhs.m_e7,
                        eRhs.m_e8,
                        eRhs.m_e9,
                        eRhs.m_e10,
                        eRhs.m_e11,
                        eRhs.m_e12,
                        eRhs.m_e13,
                        eRhs.m_e14,
                        eRhs.m_e15,
                        eRhs.m_e16,
                        eRhs.m_e17
                });
            }
            return isEqual;
        }

        public T0 getE0 ()
        {
            return m_e0;
        }

        public T1 getE1 ()
        {
            return m_e1;
        }

        public T10 getE10 ()
        {
            return m_e10;
        }

        public T11 getE11 ()
        {
            return m_e11;
        }

        public T12 getE12 ()
        {
            return m_e12;
        }

        public T13 getE13 ()
        {
            return m_e13;
        }

        public T14 getE14 ()
        {
            return m_e14;
        }

        public T15 getE15 ()
        {
            return m_e15;
        }

        public T16 getE16 ()
        {
            return m_e16;
        }

        public T17 getE17 ()
        {
            return m_e17;
        }

        public T2 getE2 ()
        {
            return m_e2;
        }

        public T3 getE3 ()
        {
            return m_e3;
        }

        public T4 getE4 ()
        {
            return m_e4;
        }

        public T5 getE5 ()
        {
            return m_e5;
        }

        public T6 getE6 ()
        {
            return m_e6;
        }

        public T7 getE7 ()
        {
            return m_e7;
        }

        public T8 getE8 ()
        {
            return m_e8;
        }

        public T9 getE9 ()
        {
            return m_e9;
        }

        @Override
        public int hashCode ()
        {
            // See Effective Java 2nd edition Item 9.
            return HcUtil.deepHashCode (m_e0, m_e1, m_e2, m_e3, m_e4, m_e5, m_e6, m_e7, m_e8, m_e9, m_e10, m_e11, m_e12,
                m_e13, m_e14, m_e15, m_e16, m_e17);
        }

        @Override
        public String toString ()
        {
            return HcUtil.deepToString (m_e0, m_e1, m_e2, m_e3, m_e4, m_e5, m_e6, m_e7, m_e8, m_e9, m_e10, m_e11, m_e12,
                m_e13, m_e14, m_e15, m_e16, m_e17);
        }

        /** DRY convenience factory */
        public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17> E18<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17> of (
            final T0 e0, final T1 e1, final T2 e2, final T3 e3, final T4 e4, final T5 e5, final T6 e6, final T7 e7,
            final T8 e8, final T9 e9, final T10 e10, final T11 e11, final T12 e12, final T13 e13, final T14 e14,
            final T15 e15, final T16 e16, final T17 e17)
        {
            return new E18<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17> (e0, e1, e2,
                e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16, e17);
        }

        private final T0 m_e0;

        private final T1 m_e1;

        private final T10 m_e10;

        private final T11 m_e11;

        private final T12 m_e12;

        private final T13 m_e13;

        private final T14 m_e14;

        private final T15 m_e15;

        private final T16 m_e16;

        private final T17 m_e17;

        private final T2 m_e2;

        private final T3 m_e3;

        private final T4 m_e4;

        private final T5 m_e5;

        private final T6 m_e6;

        private final T7 m_e7;

        private final T8 m_e8;

        private final T9 m_e9;

        private static final long serialVersionUID = 1L;
    }

    /**
     * Immutable typesafe tuple with 19 elements.
     */
    final public static class E19<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18>
        implements Serializable
    {
        public E19 (final T0 e0, final T1 e1, final T2 e2, final T3 e3, final T4 e4, final T5 e5, final T6 e6,
            final T7 e7, final T8 e8, final T9 e9, final T10 e10, final T11 e11, final T12 e12, final T13 e13,
            final T14 e14, final T15 e15, final T16 e16, final T17 e17, final T18 e18)
        {
            m_e0 = e0;
            m_e1 = e1;
            m_e2 = e2;
            m_e3 = e3;
            m_e4 = e4;
            m_e5 = e5;
            m_e6 = e6;
            m_e7 = e7;
            m_e8 = e8;
            m_e9 = e9;
            m_e10 = e10;
            m_e11 = e11;
            m_e12 = e12;
            m_e13 = e13;
            m_e14 = e14;
            m_e15 = e15;
            m_e16 = e16;
            m_e17 = e17;
            m_e18 = e18;
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
                @SuppressWarnings ("unchecked")
                final E19<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18> eRhs =
                    (E19<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18>) o;
                isEqual = Arrays.deepEquals (new Object[]
                {
                        m_e0,
                        m_e1,
                        m_e2,
                        m_e3,
                        m_e4,
                        m_e5,
                        m_e6,
                        m_e7,
                        m_e8,
                        m_e9,
                        m_e10,
                        m_e11,
                        m_e12,
                        m_e13,
                        m_e14,
                        m_e15,
                        m_e16,
                        m_e17,
                        m_e18
                }, new Object[]
                {
                        eRhs.m_e0,
                        eRhs.m_e1,
                        eRhs.m_e2,
                        eRhs.m_e3,
                        eRhs.m_e4,
                        eRhs.m_e5,
                        eRhs.m_e6,
                        eRhs.m_e7,
                        eRhs.m_e8,
                        eRhs.m_e9,
                        eRhs.m_e10,
                        eRhs.m_e11,
                        eRhs.m_e12,
                        eRhs.m_e13,
                        eRhs.m_e14,
                        eRhs.m_e15,
                        eRhs.m_e16,
                        eRhs.m_e17,
                        eRhs.m_e18
                });
            }
            return isEqual;
        }

        public T0 getE0 ()
        {
            return m_e0;
        }

        public T1 getE1 ()
        {
            return m_e1;
        }

        public T10 getE10 ()
        {
            return m_e10;
        }

        public T11 getE11 ()
        {
            return m_e11;
        }

        public T12 getE12 ()
        {
            return m_e12;
        }

        public T13 getE13 ()
        {
            return m_e13;
        }

        public T14 getE14 ()
        {
            return m_e14;
        }

        public T15 getE15 ()
        {
            return m_e15;
        }

        public T16 getE16 ()
        {
            return m_e16;
        }

        public T17 getE17 ()
        {
            return m_e17;
        }

        public T18 getE18 ()
        {
            return m_e18;
        }

        public T2 getE2 ()
        {
            return m_e2;
        }

        public T3 getE3 ()
        {
            return m_e3;
        }

        public T4 getE4 ()
        {
            return m_e4;
        }

        public T5 getE5 ()
        {
            return m_e5;
        }

        public T6 getE6 ()
        {
            return m_e6;
        }

        public T7 getE7 ()
        {
            return m_e7;
        }

        public T8 getE8 ()
        {
            return m_e8;
        }

        public T9 getE9 ()
        {
            return m_e9;
        }

        @Override
        public int hashCode ()
        {
            // See Effective Java 2nd edition Item 9.
            return HcUtil.deepHashCode (m_e0, m_e1, m_e2, m_e3, m_e4, m_e5, m_e6, m_e7, m_e8, m_e9, m_e10, m_e11, m_e12,
                m_e13, m_e14, m_e15, m_e16, m_e17, m_e18);
        }

        @Override
        public String toString ()
        {
            return HcUtil.deepToString (m_e0, m_e1, m_e2, m_e3, m_e4, m_e5, m_e6, m_e7, m_e8, m_e9, m_e10, m_e11, m_e12,
                m_e13, m_e14, m_e15, m_e16, m_e17, m_e18);
        }

        /** DRY convenience factory */
        public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18> E19<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18> of (
            final T0 e0, final T1 e1, final T2 e2, final T3 e3, final T4 e4, final T5 e5, final T6 e6, final T7 e7,
            final T8 e8, final T9 e9, final T10 e10, final T11 e11, final T12 e12, final T13 e13, final T14 e14,
            final T15 e15, final T16 e16, final T17 e17, final T18 e18)
        {
            return new E19<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18> (e0, e1,
                e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16, e17, e18);
        }

        private final T0 m_e0;

        private final T1 m_e1;

        private final T10 m_e10;

        private final T11 m_e11;

        private final T12 m_e12;

        private final T13 m_e13;

        private final T14 m_e14;

        private final T15 m_e15;

        private final T16 m_e16;

        private final T17 m_e17;

        private final T18 m_e18;

        private final T2 m_e2;

        private final T3 m_e3;

        private final T4 m_e4;

        private final T5 m_e5;

        private final T6 m_e6;

        private final T7 m_e7;

        private final T8 m_e8;

        private final T9 m_e9;

        private static final long serialVersionUID = 1L;
    }

    /**
     * Immutable typesafe tuple with 2 elements.
     */
    final public static class E2<T0, T1> implements Serializable
    {
        public E2 (final T0 e0, final T1 e1)
        {
            m_e0 = e0;
            m_e1 = e1;
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
                @SuppressWarnings ("unchecked")
                final E2<T0, T1> eRhs = (E2<T0, T1>) o;
                isEqual = Arrays.deepEquals (new Object[]
                {
                        m_e0,
                        m_e1
                }, new Object[]
                {
                        eRhs.m_e0,
                        eRhs.m_e1
                });
            }
            return isEqual;
        }

        public T0 getE0 ()
        {
            return m_e0;
        }

        public T1 getE1 ()
        {
            return m_e1;
        }

        @Override
        public int hashCode ()
        {
            // See Effective Java 2nd edition Item 9.
            return HcUtil.deepHashCode (m_e0, m_e1);
        }

        @Override
        public String toString ()
        {
            return HcUtil.deepToString (m_e0, m_e1);
        }

        /** DRY convenience factory */
        public static <T0, T1> E2<T0, T1> of (final T0 e0, final T1 e1)
        {
            return new E2<T0, T1> (e0, e1);
        }

        private final T0 m_e0;

        private final T1 m_e1;

        private static final long serialVersionUID = 1L;
    }

    /**
     * Immutable typesafe tuple with 20 elements.
     */
    final public static class E20<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19>
        implements Serializable
    {
        public E20 (final T0 e0, final T1 e1, final T2 e2, final T3 e3, final T4 e4, final T5 e5, final T6 e6,
            final T7 e7, final T8 e8, final T9 e9, final T10 e10, final T11 e11, final T12 e12, final T13 e13,
            final T14 e14, final T15 e15, final T16 e16, final T17 e17, final T18 e18, final T19 e19)
        {
            m_e0 = e0;
            m_e1 = e1;
            m_e2 = e2;
            m_e3 = e3;
            m_e4 = e4;
            m_e5 = e5;
            m_e6 = e6;
            m_e7 = e7;
            m_e8 = e8;
            m_e9 = e9;
            m_e10 = e10;
            m_e11 = e11;
            m_e12 = e12;
            m_e13 = e13;
            m_e14 = e14;
            m_e15 = e15;
            m_e16 = e16;
            m_e17 = e17;
            m_e18 = e18;
            m_e19 = e19;
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
                @SuppressWarnings ("unchecked")
                final E20<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19> eRhs =
                    (E20<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19>) o;
                isEqual = Arrays.deepEquals (new Object[]
                {
                        m_e0,
                        m_e1,
                        m_e2,
                        m_e3,
                        m_e4,
                        m_e5,
                        m_e6,
                        m_e7,
                        m_e8,
                        m_e9,
                        m_e10,
                        m_e11,
                        m_e12,
                        m_e13,
                        m_e14,
                        m_e15,
                        m_e16,
                        m_e17,
                        m_e18,
                        m_e19
                }, new Object[]
                {
                        eRhs.m_e0,
                        eRhs.m_e1,
                        eRhs.m_e2,
                        eRhs.m_e3,
                        eRhs.m_e4,
                        eRhs.m_e5,
                        eRhs.m_e6,
                        eRhs.m_e7,
                        eRhs.m_e8,
                        eRhs.m_e9,
                        eRhs.m_e10,
                        eRhs.m_e11,
                        eRhs.m_e12,
                        eRhs.m_e13,
                        eRhs.m_e14,
                        eRhs.m_e15,
                        eRhs.m_e16,
                        eRhs.m_e17,
                        eRhs.m_e18,
                        eRhs.m_e19
                });
            }
            return isEqual;
        }

        public T0 getE0 ()
        {
            return m_e0;
        }

        public T1 getE1 ()
        {
            return m_e1;
        }

        public T10 getE10 ()
        {
            return m_e10;
        }

        public T11 getE11 ()
        {
            return m_e11;
        }

        public T12 getE12 ()
        {
            return m_e12;
        }

        public T13 getE13 ()
        {
            return m_e13;
        }

        public T14 getE14 ()
        {
            return m_e14;
        }

        public T15 getE15 ()
        {
            return m_e15;
        }

        public T16 getE16 ()
        {
            return m_e16;
        }

        public T17 getE17 ()
        {
            return m_e17;
        }

        public T18 getE18 ()
        {
            return m_e18;
        }

        public T19 getE19 ()
        {
            return m_e19;
        }

        public T2 getE2 ()
        {
            return m_e2;
        }

        public T3 getE3 ()
        {
            return m_e3;
        }

        public T4 getE4 ()
        {
            return m_e4;
        }

        public T5 getE5 ()
        {
            return m_e5;
        }

        public T6 getE6 ()
        {
            return m_e6;
        }

        public T7 getE7 ()
        {
            return m_e7;
        }

        public T8 getE8 ()
        {
            return m_e8;
        }

        public T9 getE9 ()
        {
            return m_e9;
        }

        @Override
        public int hashCode ()
        {
            // See Effective Java 2nd edition Item 9.
            return HcUtil.deepHashCode (m_e0, m_e1, m_e2, m_e3, m_e4, m_e5, m_e6, m_e7, m_e8, m_e9, m_e10, m_e11, m_e12,
                m_e13, m_e14, m_e15, m_e16, m_e17, m_e18, m_e19);
        }

        @Override
        public String toString ()
        {
            return HcUtil.deepToString (m_e0, m_e1, m_e2, m_e3, m_e4, m_e5, m_e6, m_e7, m_e8, m_e9, m_e10, m_e11, m_e12,
                m_e13, m_e14, m_e15, m_e16, m_e17, m_e18, m_e19);
        }

        /** DRY convenience factory */
        public static <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19> E20<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19> of (
            final T0 e0, final T1 e1, final T2 e2, final T3 e3, final T4 e4, final T5 e5, final T6 e6, final T7 e7,
            final T8 e8, final T9 e9, final T10 e10, final T11 e11, final T12 e12, final T13 e13, final T14 e14,
            final T15 e15, final T16 e16, final T17 e17, final T18 e18, final T19 e19)
        {
            return new E20<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19> (
                e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16, e17, e18, e19);
        }

        private final T0 m_e0;

        private final T1 m_e1;

        private final T10 m_e10;

        private final T11 m_e11;

        private final T12 m_e12;

        private final T13 m_e13;

        private final T14 m_e14;

        private final T15 m_e15;

        private final T16 m_e16;

        private final T17 m_e17;

        private final T18 m_e18;

        private final T19 m_e19;

        private final T2 m_e2;

        private final T3 m_e3;

        private final T4 m_e4;

        private final T5 m_e5;

        private final T6 m_e6;

        private final T7 m_e7;

        private final T8 m_e8;

        private final T9 m_e9;

        private static final long serialVersionUID = 1L;
    }

    /**
     * Immutable typesafe tuple with 3 elements.
     */
    final public static class E3<T0, T1, T2> implements Serializable
    {
        public E3 (final T0 e0, final T1 e1, final T2 e2)
        {
            m_e0 = e0;
            m_e1 = e1;
            m_e2 = e2;
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
                @SuppressWarnings ("unchecked")
                final E3<T0, T1, T2> eRhs = (E3<T0, T1, T2>) o;
                isEqual = Arrays.deepEquals (new Object[]
                {
                        m_e0,
                        m_e1,
                        m_e2
                }, new Object[]
                {
                        eRhs.m_e0,
                        eRhs.m_e1,
                        eRhs.m_e2
                });
            }
            return isEqual;
        }

        public T0 getE0 ()
        {
            return m_e0;
        }

        public T1 getE1 ()
        {
            return m_e1;
        }

        public T2 getE2 ()
        {
            return m_e2;
        }

        @Override
        public int hashCode ()
        {
            // See Effective Java 2nd edition Item 9.
            return HcUtil.deepHashCode (m_e0, m_e1, m_e2);
        }

        @Override
        public String toString ()
        {
            return HcUtil.deepToString (m_e0, m_e1, m_e2);
        }

        /** DRY convenience factory */
        public static <T0, T1, T2> E3<T0, T1, T2> of (final T0 e0, final T1 e1, final T2 e2)
        {
            return new E3<T0, T1, T2> (e0, e1, e2);
        }

        private final T0 m_e0;

        private final T1 m_e1;

        private final T2 m_e2;

        private static final long serialVersionUID = 1L;
    }

    /**
     * Immutable typesafe tuple with 4 elements.
     */
    final public static class E4<T0, T1, T2, T3> implements Serializable
    {
        public E4 (final T0 e0, final T1 e1, final T2 e2, final T3 e3)
        {
            m_e0 = e0;
            m_e1 = e1;
            m_e2 = e2;
            m_e3 = e3;
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
                @SuppressWarnings ("unchecked")
                final E4<T0, T1, T2, T3> eRhs = (E4<T0, T1, T2, T3>) o;
                isEqual = Arrays.deepEquals (new Object[]
                {
                        m_e0,
                        m_e1,
                        m_e2,
                        m_e3
                }, new Object[]
                {
                        eRhs.m_e0,
                        eRhs.m_e1,
                        eRhs.m_e2,
                        eRhs.m_e3
                });
            }
            return isEqual;
        }

        public T0 getE0 ()
        {
            return m_e0;
        }

        public T1 getE1 ()
        {
            return m_e1;
        }

        public T2 getE2 ()
        {
            return m_e2;
        }

        public T3 getE3 ()
        {
            return m_e3;
        }

        @Override
        public int hashCode ()
        {
            // See Effective Java 2nd edition Item 9.
            return HcUtil.deepHashCode (m_e0, m_e1, m_e2, m_e3);
        }

        @Override
        public String toString ()
        {
            return HcUtil.deepToString (m_e0, m_e1, m_e2, m_e3);
        }

        /** DRY convenience factory */
        public static <T0, T1, T2, T3> E4<T0, T1, T2, T3> of (final T0 e0, final T1 e1, final T2 e2, final T3 e3)
        {
            return new E4<T0, T1, T2, T3> (e0, e1, e2, e3);
        }

        private final T0 m_e0;

        private final T1 m_e1;

        private final T2 m_e2;

        private final T3 m_e3;

        private static final long serialVersionUID = 1L;
    }

    /**
     * Immutable typesafe tuple with 5 elements.
     */
    final public static class E5<T0, T1, T2, T3, T4> implements Serializable
    {
        public E5 (final T0 e0, final T1 e1, final T2 e2, final T3 e3, final T4 e4)
        {
            m_e0 = e0;
            m_e1 = e1;
            m_e2 = e2;
            m_e3 = e3;
            m_e4 = e4;
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
                @SuppressWarnings ("unchecked")
                final E5<T0, T1, T2, T3, T4> eRhs = (E5<T0, T1, T2, T3, T4>) o;
                isEqual = Arrays.deepEquals (new Object[]
                {
                        m_e0,
                        m_e1,
                        m_e2,
                        m_e3,
                        m_e4
                }, new Object[]
                {
                        eRhs.m_e0,
                        eRhs.m_e1,
                        eRhs.m_e2,
                        eRhs.m_e3,
                        eRhs.m_e4
                });
            }
            return isEqual;
        }

        public T0 getE0 ()
        {
            return m_e0;
        }

        public T1 getE1 ()
        {
            return m_e1;
        }

        public T2 getE2 ()
        {
            return m_e2;
        }

        public T3 getE3 ()
        {
            return m_e3;
        }

        public T4 getE4 ()
        {
            return m_e4;
        }

        @Override
        public int hashCode ()
        {
            // See Effective Java 2nd edition Item 9.
            return HcUtil.deepHashCode (m_e0, m_e1, m_e2, m_e3, m_e4);
        }

        @Override
        public String toString ()
        {
            return HcUtil.deepToString (m_e0, m_e1, m_e2, m_e3, m_e4);
        }

        /** DRY convenience factory */
        public static <T0, T1, T2, T3, T4> E5<T0, T1, T2, T3, T4> of (final T0 e0, final T1 e1, final T2 e2,
            final T3 e3, final T4 e4)
        {
            return new E5<T0, T1, T2, T3, T4> (e0, e1, e2, e3, e4);
        }

        private final T0 m_e0;

        private final T1 m_e1;

        private final T2 m_e2;

        private final T3 m_e3;

        private final T4 m_e4;

        private static final long serialVersionUID = 1L;
    }

    /**
     * Immutable typesafe tuple with 6 elements.
     */
    final public static class E6<T0, T1, T2, T3, T4, T5> implements Serializable
    {
        public E6 (final T0 e0, final T1 e1, final T2 e2, final T3 e3, final T4 e4, final T5 e5)
        {
            m_e0 = e0;
            m_e1 = e1;
            m_e2 = e2;
            m_e3 = e3;
            m_e4 = e4;
            m_e5 = e5;
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
                @SuppressWarnings ("unchecked")
                final E6<T0, T1, T2, T3, T4, T5> eRhs = (E6<T0, T1, T2, T3, T4, T5>) o;
                isEqual = Arrays.deepEquals (new Object[]
                {
                        m_e0,
                        m_e1,
                        m_e2,
                        m_e3,
                        m_e4,
                        m_e5
                }, new Object[]
                {
                        eRhs.m_e0,
                        eRhs.m_e1,
                        eRhs.m_e2,
                        eRhs.m_e3,
                        eRhs.m_e4,
                        eRhs.m_e5
                });
            }
            return isEqual;
        }

        public T0 getE0 ()
        {
            return m_e0;
        }

        public T1 getE1 ()
        {
            return m_e1;
        }

        public T2 getE2 ()
        {
            return m_e2;
        }

        public T3 getE3 ()
        {
            return m_e3;
        }

        public T4 getE4 ()
        {
            return m_e4;
        }

        public T5 getE5 ()
        {
            return m_e5;
        }

        @Override
        public int hashCode ()
        {
            // See Effective Java 2nd edition Item 9.
            return HcUtil.deepHashCode (m_e0, m_e1, m_e2, m_e3, m_e4, m_e5);
        }

        @Override
        public String toString ()
        {
            return HcUtil.deepToString (m_e0, m_e1, m_e2, m_e3, m_e4, m_e5);
        }

        /** DRY convenience factory */
        public static <T0, T1, T2, T3, T4, T5> E6<T0, T1, T2, T3, T4, T5> of (final T0 e0, final T1 e1, final T2 e2,
            final T3 e3, final T4 e4, final T5 e5)
        {
            return new E6<T0, T1, T2, T3, T4, T5> (e0, e1, e2, e3, e4, e5);
        }

        private final T0 m_e0;

        private final T1 m_e1;

        private final T2 m_e2;

        private final T3 m_e3;

        private final T4 m_e4;

        private final T5 m_e5;

        private static final long serialVersionUID = 1L;
    }

    /**
     * Immutable typesafe tuple with 7 elements.
     */
    final public static class E7<T0, T1, T2, T3, T4, T5, T6> implements Serializable
    {
        public E7 (final T0 e0, final T1 e1, final T2 e2, final T3 e3, final T4 e4, final T5 e5, final T6 e6)
        {
            m_e0 = e0;
            m_e1 = e1;
            m_e2 = e2;
            m_e3 = e3;
            m_e4 = e4;
            m_e5 = e5;
            m_e6 = e6;
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
                @SuppressWarnings ("unchecked")
                final E7<T0, T1, T2, T3, T4, T5, T6> eRhs = (E7<T0, T1, T2, T3, T4, T5, T6>) o;
                isEqual = Arrays.deepEquals (new Object[]
                {
                        m_e0,
                        m_e1,
                        m_e2,
                        m_e3,
                        m_e4,
                        m_e5,
                        m_e6
                }, new Object[]
                {
                        eRhs.m_e0,
                        eRhs.m_e1,
                        eRhs.m_e2,
                        eRhs.m_e3,
                        eRhs.m_e4,
                        eRhs.m_e5,
                        eRhs.m_e6
                });
            }
            return isEqual;
        }

        public T0 getE0 ()
        {
            return m_e0;
        }

        public T1 getE1 ()
        {
            return m_e1;
        }

        public T2 getE2 ()
        {
            return m_e2;
        }

        public T3 getE3 ()
        {
            return m_e3;
        }

        public T4 getE4 ()
        {
            return m_e4;
        }

        public T5 getE5 ()
        {
            return m_e5;
        }

        public T6 getE6 ()
        {
            return m_e6;
        }

        @Override
        public int hashCode ()
        {
            // See Effective Java 2nd edition Item 9.
            return HcUtil.deepHashCode (m_e0, m_e1, m_e2, m_e3, m_e4, m_e5, m_e6);
        }

        @Override
        public String toString ()
        {
            return HcUtil.deepToString (m_e0, m_e1, m_e2, m_e3, m_e4, m_e5, m_e6);
        }

        /** DRY convenience factory */
        public static <T0, T1, T2, T3, T4, T5, T6> E7<T0, T1, T2, T3, T4, T5, T6> of (final T0 e0, final T1 e1,
            final T2 e2, final T3 e3, final T4 e4, final T5 e5, final T6 e6)
        {
            return new E7<T0, T1, T2, T3, T4, T5, T6> (e0, e1, e2, e3, e4, e5, e6);
        }

        private final T0 m_e0;

        private final T1 m_e1;

        private final T2 m_e2;

        private final T3 m_e3;

        private final T4 m_e4;

        private final T5 m_e5;

        private final T6 m_e6;

        private static final long serialVersionUID = 1L;
    }

    /**
     * Immutable typesafe tuple with 8 elements.
     */
    final public static class E8<T0, T1, T2, T3, T4, T5, T6, T7> implements Serializable
    {
        public E8 (final T0 e0, final T1 e1, final T2 e2, final T3 e3, final T4 e4, final T5 e5, final T6 e6,
            final T7 e7)
        {
            m_e0 = e0;
            m_e1 = e1;
            m_e2 = e2;
            m_e3 = e3;
            m_e4 = e4;
            m_e5 = e5;
            m_e6 = e6;
            m_e7 = e7;
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
                @SuppressWarnings ("unchecked")
                final E8<T0, T1, T2, T3, T4, T5, T6, T7> eRhs = (E8<T0, T1, T2, T3, T4, T5, T6, T7>) o;
                isEqual = Arrays.deepEquals (new Object[]
                {
                        m_e0,
                        m_e1,
                        m_e2,
                        m_e3,
                        m_e4,
                        m_e5,
                        m_e6,
                        m_e7
                }, new Object[]
                {
                        eRhs.m_e0,
                        eRhs.m_e1,
                        eRhs.m_e2,
                        eRhs.m_e3,
                        eRhs.m_e4,
                        eRhs.m_e5,
                        eRhs.m_e6,
                        eRhs.m_e7
                });
            }
            return isEqual;
        }

        public T0 getE0 ()
        {
            return m_e0;
        }

        public T1 getE1 ()
        {
            return m_e1;
        }

        public T2 getE2 ()
        {
            return m_e2;
        }

        public T3 getE3 ()
        {
            return m_e3;
        }

        public T4 getE4 ()
        {
            return m_e4;
        }

        public T5 getE5 ()
        {
            return m_e5;
        }

        public T6 getE6 ()
        {
            return m_e6;
        }

        public T7 getE7 ()
        {
            return m_e7;
        }

        @Override
        public int hashCode ()
        {
            // See Effective Java 2nd edition Item 9.
            return HcUtil.deepHashCode (m_e0, m_e1, m_e2, m_e3, m_e4, m_e5, m_e6, m_e7);
        }

        @Override
        public String toString ()
        {
            return HcUtil.deepToString (m_e0, m_e1, m_e2, m_e3, m_e4, m_e5, m_e6, m_e7);
        }

        /** DRY convenience factory */
        public static <T0, T1, T2, T3, T4, T5, T6, T7> E8<T0, T1, T2, T3, T4, T5, T6, T7> of (final T0 e0, final T1 e1,
            final T2 e2, final T3 e3, final T4 e4, final T5 e5, final T6 e6, final T7 e7)
        {
            return new E8<T0, T1, T2, T3, T4, T5, T6, T7> (e0, e1, e2, e3, e4, e5, e6, e7);
        }

        private final T0 m_e0;

        private final T1 m_e1;

        private final T2 m_e2;

        private final T3 m_e3;

        private final T4 m_e4;

        private final T5 m_e5;

        private final T6 m_e6;

        private final T7 m_e7;

        private static final long serialVersionUID = 1L;
    }

    /**
     * Immutable typesafe tuple with 9 elements.
     */
    final public static class E9<T0, T1, T2, T3, T4, T5, T6, T7, T8> implements Serializable
    {
        public E9 (final T0 e0, final T1 e1, final T2 e2, final T3 e3, final T4 e4, final T5 e5, final T6 e6,
            final T7 e7, final T8 e8)
        {
            m_e0 = e0;
            m_e1 = e1;
            m_e2 = e2;
            m_e3 = e3;
            m_e4 = e4;
            m_e5 = e5;
            m_e6 = e6;
            m_e7 = e7;
            m_e8 = e8;
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
                @SuppressWarnings ("unchecked")
                final E9<T0, T1, T2, T3, T4, T5, T6, T7, T8> eRhs = (E9<T0, T1, T2, T3, T4, T5, T6, T7, T8>) o;
                isEqual = Arrays.deepEquals (new Object[]
                {
                        m_e0,
                        m_e1,
                        m_e2,
                        m_e3,
                        m_e4,
                        m_e5,
                        m_e6,
                        m_e7,
                        m_e8
                }, new Object[]
                {
                        eRhs.m_e0,
                        eRhs.m_e1,
                        eRhs.m_e2,
                        eRhs.m_e3,
                        eRhs.m_e4,
                        eRhs.m_e5,
                        eRhs.m_e6,
                        eRhs.m_e7,
                        eRhs.m_e8
                });
            }
            return isEqual;
        }

        public T0 getE0 ()
        {
            return m_e0;
        }

        public T1 getE1 ()
        {
            return m_e1;
        }

        public T2 getE2 ()
        {
            return m_e2;
        }

        public T3 getE3 ()
        {
            return m_e3;
        }

        public T4 getE4 ()
        {
            return m_e4;
        }

        public T5 getE5 ()
        {
            return m_e5;
        }

        public T6 getE6 ()
        {
            return m_e6;
        }

        public T7 getE7 ()
        {
            return m_e7;
        }

        public T8 getE8 ()
        {
            return m_e8;
        }

        @Override
        public int hashCode ()
        {
            // See Effective Java 2nd edition Item 9.
            return HcUtil.deepHashCode (m_e0, m_e1, m_e2, m_e3, m_e4, m_e5, m_e6, m_e7, m_e8);
        }

        @Override
        public String toString ()
        {
            return HcUtil.deepToString (m_e0, m_e1, m_e2, m_e3, m_e4, m_e5, m_e6, m_e7, m_e8);
        }

        /** DRY convenience factory */
        public static <T0, T1, T2, T3, T4, T5, T6, T7, T8> E9<T0, T1, T2, T3, T4, T5, T6, T7, T8> of (final T0 e0,
            final T1 e1, final T2 e2, final T3 e3, final T4 e4, final T5 e5, final T6 e6, final T7 e7, final T8 e8)
        {
            return new E9<T0, T1, T2, T3, T4, T5, T6, T7, T8> (e0, e1, e2, e3, e4, e5, e6, e7, e8);
        }

        private final T0 m_e0;

        private final T1 m_e1;

        private final T2 m_e2;

        private final T3 m_e3;

        private final T4 m_e4;

        private final T5 m_e5;

        private final T6 m_e6;

        private final T7 m_e7;

        private final T8 m_e8;

        private static final long serialVersionUID = 1L;
    }
}
