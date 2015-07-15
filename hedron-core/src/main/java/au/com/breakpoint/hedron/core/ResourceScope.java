//                       __________________________________
//                ______|         Copyright 2008           |______
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

/**
 * Useful for scoped closeable tasks. Allows multiple calls to close (), with the
 * subsequent calls being benign.
 *
 * Not threadsafe.
 *
 * @param <T>
 *            Type of the held instance
 */
public class ResourceScope<T> implements ICloseable
{
    private ResourceScope (final T t, final Consumer<? super T> closer)
    {
        this (t, closer, null);
    }

    private ResourceScope (final T t, final Consumer<? super T> closer, final String details)
    {
        m_t = t;
        m_closer = closer;
        m_details = details == null ? HcUtil._UNKNOWN : details;
    }

    @Override
    public void close ()
    {
        if (!m_closed)
        {
            m_closer.accept (m_t);
            m_closed = true;
        }
    }

    public T get ()
    {
        return m_t;
    }

    public Consumer<? super T> getCloser ()
    {
        return m_closer;
    }

    public String getDetails ()
    {
        return m_details;
    }

    public static <T> ResourceScope<T> of (final T t, final Consumer<? super T> closer)
    {
        return new ResourceScope<> (t, closer);
    }

    public static <T> ResourceScope<T> of (final T t, final Consumer<? super T> closer, final String details)
    {
        return new ResourceScope<> (t, closer, details);
    }

    private boolean m_closed;

    private final Consumer<? super T> m_closer;

    private final String m_details;

    private final T m_t;
}
