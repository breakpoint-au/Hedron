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
package au.com.breakpoint.hedron.core.log;

import java.util.Collection;
import java.util.function.BiFunction;
import au.com.breakpoint.hedron.core.TimestampLevelFormatter;
import au.com.breakpoint.hedron.core.value.IValue;

/**
 * A logging implementation allowing late evaluation of the log string. Writes to the Java
 * console.
 */
public class ConsoleLogger extends AbstractLogger
{
    public ConsoleLogger (final BiFunction<Level, IValue<String>, String> formatter, final String levelsConfig,
        final Collection<? extends IStringLogger> slaves)
    {
        super (formatter, levelsConfig, slaves);
    }

    public ConsoleLogger (final String levelsConfig)
    {
        this (new TimestampLevelFormatter (), levelsConfig, null);
    }

    @Override
    public synchronized void logString (final String contextId, final Level level, final String s)
    {
        Logging.logToConsole (level, s);
    }
}
