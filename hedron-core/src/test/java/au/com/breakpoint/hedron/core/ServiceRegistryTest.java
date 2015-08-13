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
import au.com.breakpoint.hedron.core.context.FaultException;
import au.com.breakpoint.hedron.core.context.LoggingSilenceScope;
import au.com.breakpoint.hedron.core.service.ServiceRegistry;

public class ServiceRegistryTest
{
    @Test
    public void testGetInstance ()
    {
        if (true)
        {
            final ServiceRegistry sl = new ServiceRegistry ();
            sl.register (ITest.class, STest.class);

            final ITest o = sl.of (ITest.class);
            assertTrue (o instanceof STest);
        }

        if (true)
        {
            final ServiceRegistry sl = new ServiceRegistry ();
            sl.register (ITest.class, ETest.class);

            final ITest o = sl.of (ITest.class);
            assertTrue (o instanceof STest);
            assertTrue (o instanceof ETest);
        }
    }

    @Test
    public void testGetInstanceChained ()
    {
        if (true)
        {
            final ServiceRegistry sl0 = new ServiceRegistry ();
            sl0.register (ITest.class, STest.class);
            final ServiceRegistry sl1 = new ServiceRegistry (sl0);

            if (true)
            {
                final ITest o = sl0.of (ITest.class);
                assertTrue (o instanceof STest);
            }
            if (true)
            {
                final ITest o = sl1.of (ITest.class);
                assertTrue (o instanceof STest);
            }
        }

        if (true)
        {
            final ServiceRegistry sl0 = new ServiceRegistry ();
            sl0.register (ITest.class, ETest.class);
            final ServiceRegistry sl1 = new ServiceRegistry (sl0);

            if (true)
            {
                final ITest o = sl0.of (ITest.class);
                assertTrue (o instanceof STest);
                assertTrue (o instanceof ETest);
            }
            if (true)
            {
                final ITest o = sl1.of (ITest.class);
                assertTrue (o instanceof STest);
                assertTrue (o instanceof ETest);
            }
        }
    }

    @Test
    public void testGetInstanceChainedMulti ()
    {
        if (true)
        {
            final ServiceRegistry sl0 = new ServiceRegistry ();
            sl0.register (ITest.class, STest.class);
            final ServiceRegistry sl1 = new ServiceRegistry (sl0);
            final ServiceRegistry sl2 = new ServiceRegistry (sl1);
            final ServiceRegistry sl3 = new ServiceRegistry (sl2);
            sl3.register (ITest.class, ETest.class);

            assertTrue (sl0.of (ITest.class) instanceof STest);
            assertTrue (sl1.of (ITest.class) instanceof STest);
            assertTrue (sl2.of (ITest.class) instanceof STest);
            assertTrue (sl3.of (ITest.class) instanceof ETest);
        }
    }

    @Test (expected = FaultException.class)
    public void testGetInstanceDouble ()
    {
        final ServiceRegistry sl = new ServiceRegistry ();
        sl.register (ITest.class, STest.class);

        try (final LoggingSilenceScope ls = new LoggingSilenceScope ())
        {
            sl.register (ITest.class, ETest.class);
        }
    }

    @Test
    public void testRegisterClassFactory ()
    {
        final ServiceRegistry sl = new ServiceRegistry ();

        final ETest impl = new ETest ();
        sl.register (ITest.class, () -> impl);

        assertEquals (impl, sl.of (ITest.class));
        assertEquals (impl, sl.of (ITest.class));
        assertEquals (impl, sl.of (ITest.class));
    }

    @Test
    public void testRegisterClassFactoryByString ()
    {
        final ServiceRegistry sl = new ServiceRegistry ();

        final ETest impl = new ETest ();
        sl.register ("key", () -> impl);

        assertEquals (impl, sl.of ("key"));
        assertEquals (impl, sl.of ("key"));
        assertEquals (impl, sl.of ("key"));
    }

    @Test
    public void testRegisterSingleton ()
    {
        final ServiceRegistry sl = new ServiceRegistry ();
        sl.registerSingleton (ITest.class, ETest.class);

        final ITest impl = sl.of (ITest.class);
        assertTrue (impl instanceof STest);
        assertTrue (impl instanceof ETest);

        assertEquals (impl, sl.of (ITest.class));
        assertEquals (impl, sl.of (ITest.class));
        assertEquals (impl, sl.of (ITest.class));
    }

    @Test
    public void testRegisterSingletonByString ()
    {
        final ServiceRegistry sl = new ServiceRegistry ();
        sl.registerSingleton ("key", ETest.class);

        final ITest impl = sl.of ("key");
        assertTrue (impl instanceof STest);
        assertTrue (impl instanceof ETest);

        assertEquals (impl, sl.of ("key"));
        assertEquals (impl, sl.of ("key"));
        assertEquals (impl, sl.of ("key"));
    }

    @Test
    public void testRegisterTimeLimitedSingleton ()
    {
        final ServiceRegistry sl = new ServiceRegistry ();
        sl.registerTimeLimitedSingleton (ITest.class, ETest.class, 200);

        final ITest impl0 = sl.of (ITest.class);
        assertTrue (impl0 instanceof STest);
        assertTrue (impl0 instanceof ETest);

        for (int i = 0; i < 5; ++i)
        {
            assertTrue (impl0 == sl.of (ITest.class));
        }

        // Expire the cache.
        HcUtil.pause (250);

        final ITest impl1 = sl.of (ITest.class);
        assertTrue (impl1 != impl0);// new object
        assertTrue (impl1 instanceof STest);
        assertTrue (impl1 instanceof ETest);

        for (int i = 0; i < 5; ++i)
        {
            assertTrue (impl1 == sl.of (ITest.class));
        }
    }

    public static class ETest extends STest
    {
    }

    public static interface ITest
    {
    }

    public static class STest implements ITest
    {
    }
}
