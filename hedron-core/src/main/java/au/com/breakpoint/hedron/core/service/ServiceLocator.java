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

import au.com.breakpoint.hedron.core.HcUtil;

public class ServiceLocator
{
    /**
     * A class encapsulating ServiceRegistry configuration. Inherit this and implement run
     * method to set up ServiceRegistry instance(s).
     */
    public static abstract class ConfigurableModule implements Runnable
    {
        public Object getConfigurationItem ()
        {
            return m_configurationItem;
        }

        public void setConfigurationItem (final Object configurationItem)
        {
            m_configurationItem = configurationItem;
        }

        /**
         * A configuration item, eg name of file containing application configuration, or
         * a URL, or a JSON object retrieved from MongoDB, etc.
         */
        private Object m_configurationItem;
    }

    /**
     * For each specified module, sets the configuration filename and invokes the module's
     * run () method to allow it to set up ServiceRegistry instance(s).
     *
     * @param configItem
     *            name of the configuration file
     * @param modules
     *            modules to be configured
     */
    public static void configureServices (final Object configItem, final ConfigurableModule... modules)
    {
        // Set up any extra modules.
        for (final ConfigurableModule m : modules)
        {
            configureAndRun (configItem, m);
        }
    }

    /**
     * The set of module is specified by a primary configuration module (created using
     * reflection and moduleClassName) plus optional extra modules. For each module, sets
     * the configuration filename and invokes the module's run () method to allow it to
     * set up ServiceRegistry instance(s).
     *
     * @param moduleClassName
     *            name of the primary configuration module
     * @param configItem
     *            name of the configuration file
     * @param extraModules
     *            extra modules to be configured
     */
    public static void configureServices (final String moduleClassName, final Object configItem,
        final ConfigurableModule... extraModules)
    {
        // Set up the main module.
        final ConfigurableModule mainModule = HcUtil.instantiate (moduleClassName);
        configureAndRun (configItem, mainModule);

        // And the extras.
        configureServices (configItem, extraModules);
    }

    /**
     * Gets a shared system-wide global service registry that can be enough for average
     * applications.
     *
     * @return the global ServiceRegistry
     */
    public static ServiceRegistry global ()
    {
        return m_globalInstance;
    }

    /**
     * Using the global registry, retrieve an object instance registered against the
     * specified key.
     *
     * @param key
     *            specified key
     * @return object instance
     */
    public static <T> T of (final Object key)
    {
        return m_globalInstance.of (key);
    }

    private static void configureAndRun (final Object configItem, final ConfigurableModule m)
    {
        m.setConfigurationItem (configItem);
        m.run ();
    }

    /** The shared system-wide global service registry */
    private static final ServiceRegistry m_globalInstance = new ServiceRegistry ();
}
