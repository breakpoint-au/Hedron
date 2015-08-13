//                       __________________________________
//                ______|      Copyright 2008-2015         |______
//                \     |     Breakpoint Pty Limited       |     /
//                 \    |   http://www.breakpoint.com.au   |    /
//                 /    |__________________________________|    \
//                /_________/                          \_________\
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
// except in compliance with the License. You may obtain a copy of the License at
//	   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
// either express or implied. See the License for the specific language governing
// permissions and limitations under the License.
//
package au.com.breakpoint.hedron.core;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Useful for scoped closeable tasks. Allows multiple calls to close (), with the
 * subsequent calls being benign. Lazy instantiation of the resource, and it is never
 * instantiated if get () is not called.
 *
 * Not threadsafe.
 *
 * @param <T>
 *            Type of the held instance
 */
public class LazyResourceScope<T> implements ICloseable
{
    /**
     * @param opener
     *            used to create the instance of T on first call to get ()
     * @param closer
     *            used to close the resource, typically at the end of a try-with-resources
     *            block
     */
    private LazyResourceScope (final Supplier<? extends T> opener, final Consumer<? super T> closer)
    {
        this (opener, closer, null);
    }

    /**
     *
     * @param opener
     *            used to create the instance of T on first call to get ()
     * @param closer
     *            used to close the resource, typically at the end of a try-with-resources
     *            block
     * @param details
     *            a string used to identify the resource for debugging purposes etc
     */
    private LazyResourceScope (final Supplier<? extends T> opener, final Consumer<? super T> closer,
        final String details)
    {
        m_opener = opener;
        m_closer = closer;
        m_details = details == null ? HcUtil._UNKNOWN : details;
    }

    @Override
    public void close ()
    {
        if (!m_closed)
        {
            // If never opened then nothing to do.
            if (m_t != null)
            {
                m_closer.accept (m_t);
            }

            m_closed = true;
        }
    }

    public T get ()
    {
        if (m_t == null)
        {
            m_t = m_opener.get ();
        }

        return m_t;
    }

    public String getDetails ()
    {
        return m_details;
    }

    public static <T> LazyResourceScope<T> of (final Supplier<? extends T> opener, final Consumer<? super T> closer)
    {
        return new LazyResourceScope<> (opener, closer);
    }

    public static <T> LazyResourceScope<T> of (final Supplier<? extends T> opener, final Consumer<? super T> closer,
        final String details)
    {
        return new LazyResourceScope<> (opener, closer, details);
    }

    private boolean m_closed;

    private final Consumer<? super T> m_closer;

    private final String m_details;

    private final Supplier<? extends T> m_opener;

    private T m_t;
}
