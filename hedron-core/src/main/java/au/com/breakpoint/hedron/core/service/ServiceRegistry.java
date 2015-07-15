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
package au.com.breakpoint.hedron.core.service;

import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.GenericFactory;
import au.com.breakpoint.hedron.core.IFactory;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.core.value.IValue;
import au.com.breakpoint.hedron.core.value.SafeLazyValue;
import au.com.breakpoint.hedron.core.value.TimeLimitedLazyValue;

/**
 * A collection describing the implementation to be used for associated interfaces. Used
 * for service locator pattern instances (eg used as alternative to dependency injection
 * approach).
 */
public class ServiceRegistry
{
    /**
     * Clear registered services.
     */
    public void clear ()
    {
        m_registry.clear ();
    }

    /**
     * Retrieve an object instance registered against the specified key.
     *
     * @param key
     *            specified key
     * @return object instance
     */
    public <T> T of (final Object key)
    {
        final IFactory<Object> factory = m_registry.get (key);
        ThreadContext.assertFault (factory != null, "No implementation has been registered for key[%s]", key);

        return HcUtil.uncheckedCast (factory.newInstance ());
    }

    /**
     * Register an object factory against the specified key. The object factory
     * instantiates a new object using reflection and the specified Class.
     *
     * @param key
     *            specified key
     * @param implClass
     *            class name used to instantiate an object on request
     */
    public void register (final Object key, final Class<?> implClass)
    {
        register (key, implClass, getReflectionFactory (implClass));
    }

    /**
     * Register an object factory against the specified key. Register the concrete class
     * also so that it can also be used as key.
     *
     * @param key
     *            specified key
     * @param implClass
     *            class name used to instantiate an object on request
     * @param factory
     */
    public void register (final Object key, final Class<?> implClass, final IFactory<Object> factory)
    {
        checkAndPut (key, factory);

        // Allow the concrete class to be instantiated too.
        if (implClass != key)
        {
            checkAndPut (implClass, factory);
        }
    }

    /**
     * Register a lazy-singleton factory against the specified key. Register the concrete
     * class also so that it can also be used as key.
     *
     * @param key
     *            specified key
     * @param implClass
     *            class name used to instantiate an object on request
     * @param factory
     *            a factory object that will be used to lazy-instantiate the singleton on
     *            first use
     */
    public <T> void registerSingleton (final Object key, final Class<? extends T> implClass, final IFactory<T> factory)
    {
        doRegisterSingleton (key, implClass, factory, -1);
    }

    /**
     * Register a lazy-singleton factory against the specified key. Register the concrete
     * class also so that it can also be used as key. The object factory instantiates a
     * new object using reflection and the specified Class.
     *
     * @param key
     *            specified key
     * @param implClass
     *            class name used to instantiate an object on request
     */
    public void registerSingleton (final Object key, final Class<?> implClass)
    {
        registerSingleton (key, implClass, getReflectionFactory (implClass));
    }

    /**
     * Register a time-limited lazy-singleton factory against the specified key. Register
     * the concrete class also so that it can also be used as key.
     *
     * @param key
     *            specified key
     * @param implClass
     *            class name used to instantiate an object on request
     * @param factory
     *            a factory object that will be used to lazy-instantiate the singleton on
     *            first use
     * @param lifetimeMsec
     *            lifetime of the singleton in milliseconds
     */
    public <T> void registerTimeLimitedSingleton (final Object key, final Class<? extends T> implClass,
        final IFactory<T> factory, final long lifetimeMsec)
    {
        doRegisterSingleton (key, implClass, factory, lifetimeMsec);
    }

    /**
     * Register a time-limited lazy-singleton factory against the specified key. Register
     * the concrete class also so that it can also be used as key. The object factory
     * instantiates a new object using reflection and the specified Class.
     *
     * @param key
     *            specified key
     * @param implClass
     *            class name used to instantiate an object on request
     * @param lifetimeMsec
     *            lifetime of the singleton in milliseconds
     */
    public void registerTimeLimitedSingleton (final Object key, final Class<?> implClass, final long lifetimeMsec)
    {
        registerTimeLimitedSingleton (key, implClass, getReflectionFactory (implClass), lifetimeMsec);
    }

    /**
     * Register a factory against specified key, checking that a factory hasn't already
     * been registered.
     *
     * @param key
     *            specified key
     * @param factory
     *            a factory object that will be used to instantiate the object
     */
    private void checkAndPut (final Object key, final IFactory<Object> factory)
    {
        final IFactory<Object> storedValue = m_registry.put (key, factory);
        ThreadContext.assertFault (storedValue == null, "An implementation has already been registered for key[%s]",
            key);
    }

    /**
     * Register an optionally time-limited lazy-singleton factory against the specified
     * key. Register the concrete class also so that it can also be used as key.
     *
     * @param key
     *            specified key
     * @param implClass
     *            class name used to instantiate an object on request
     * @param factory
     *            a factory object that will be used to lazy-instantiate the singleton on
     *            first use
     * @param lifetimeMsec
     *            lifetime of the singleton in milliseconds, or -1 for non-time-limited
     */
    private <T> void doRegisterSingleton (final Object key, final Class<? extends T> implClass,
        final IFactory<T> factory, final long lifetimeMsec)
    {
        // Create a threadsafe singleton creator that uses the supplied factory once.
        final Supplier<T> singletonInstantiator = factory::newInstance;

        // Implement lifetime policy at this layer. Either singleton or time-limited singleton.
        final IValue<T> singletonValue =
            lifetimeMsec == -1L ? SafeLazyValue.of (singletonInstantiator) : TimeLimitedLazyValue
                .of (ServiceRegistry.class, "timeLimitedSingleton", singletonInstantiator, lifetimeMsec);

        // Wrap it in a factory so it can be stored in the map.
        final IFactory<Object> factoryWrapper = singletonValue::get;

        checkAndPut (key, factoryWrapper);

        // Allow the concrete class to be instantiated too.
        if (implClass != key)
        {
            checkAndPut (implClass, factoryWrapper);
        }
    }

    /**
     * Create a factory that instantiates objects via reflection.
     *
     * @param implClass
     *            class name used to instantiate an object on request
     * @return the created object instance
     */
    private IFactory<Object> getReflectionFactory (final Class<?> implClass)
    {
        return () -> HcUtil.instantiate (implClass);
    }

    /** The key-factory registry */
    private final ConcurrentMap<Object, IFactory<Object>> m_registry = GenericFactory.newConcurrentHashMap ();
}
