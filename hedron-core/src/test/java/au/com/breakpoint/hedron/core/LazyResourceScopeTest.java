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

import static org.junit.Assert.assertTrue;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.junit.Test;
import au.com.breakpoint.hedron.core.LazyResourceScope;

public class LazyResourceScopeTest
{
    @Test
    public void test_closeException ()
    {
        m_closeCalled = false;

        try
        {
            try (final LazyResourceScope<String> rs = LazyResourceScope.of (m_exceptionalOpener, m_indicateClosed))
            {
                m_closeCalled = false;
            }
        }
        catch (final Throwable e)
        {
            // Swallow.
        }

        assertTrue (!m_closeCalled);
    }

    @Test
    public void test_closeNotOpened ()
    {
        m_closeCalled = false;

        final Supplier<String> opener = () -> "item";
        try (final LazyResourceScope<String> rs = LazyResourceScope.of (opener, m_indicateClosed))
        {
        }

        assertTrue (!m_closeCalled);// hasn't been opened so not called
    }

    @Test
    public void test_closeOpened ()
    {
        m_closeCalled = false;

        final Supplier<String> opener = () -> "item";
        try (final LazyResourceScope<String> rs = LazyResourceScope.of (opener, m_indicateClosed))
        {
            final String val = rs.get ();// calls opener here
            System.out.println (val);
        }

        assertTrue (m_closeCalled);
    }

    private boolean m_closeCalled;

    private final Supplier<String> m_exceptionalOpener = () ->
    {
        throw new RuntimeException ();
    };

    private final Consumer<String> m_indicateClosed = s ->
    {
        m_closeCalled = true;
    };
}
