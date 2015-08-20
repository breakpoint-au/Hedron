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
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import au.com.breakpoint.hedron.core.GenericFactory;
import au.com.breakpoint.hedron.core.context.LoggingOperationScope;
import au.com.breakpoint.hedron.core.value.FormattedStringValue;
import au.com.breakpoint.hedron.core.value.IValue;

/**
 * A logging implementation allowing late evaluation of the log string. Relies on subclass
 * to write the formatted string somewhere.
 */
public abstract class AbstractLogger implements ILogger, IStringLogger
{
    public AbstractLogger (final BiFunction<Level, IValue<String>, String> formatter, final String levelsConfig,
        final Collection<? extends IStringLogger> slaves)
    {
        m_formatter = formatter;
        setLevels (levelsConfig);

        if (slaves != null)
        {
            m_slaves.addAll (slaves);
        }
    }

    @Override
    public boolean isEnabled (final Level level)
    {
        try (final LoggingOperationScope ls = new LoggingOperationScope ())
        {
            return isLevelEnabled (level);
        }
    }

    @Override
    public void logDebug (final String contextId, final IValue<String> s)
    {
        try (final LoggingOperationScope ls = new LoggingOperationScope ())
        {
            if (isLevelEnabled (Level.Debug))
            {
                formatAndLogString (contextId, Level.Debug, s);
            }
        }
    }

    @Override
    public void logError (final String contextId, final IValue<String> s)
    {
        try (final LoggingOperationScope ls = new LoggingOperationScope ())
        {
            if (isLevelEnabled (Level.Error))
            {
                formatAndLogString (contextId, Level.Error, s);
            }
        }
    }

    @Override
    public void logFatal (final String contextId, final IValue<String> s)
    {
        try (final LoggingOperationScope ls = new LoggingOperationScope ())
        {
            if (isLevelEnabled (Level.Fatal))
            {
                formatAndLogString (contextId, Level.Fatal, s);
            }
        }
    }

    @Override
    public void logInfo (final String contextId, final IValue<String> s)
    {
        try (final LoggingOperationScope ls = new LoggingOperationScope ())
        {
            if (isLevelEnabled (Level.Info))
            {
                formatAndLogString (contextId, Level.Info, s);
            }
        }
    }

    @Override
    public void logTrace (final String contextId, final IValue<String> s)
    {
        try (final LoggingOperationScope ls = new LoggingOperationScope ())
        {
            if (isLevelEnabled (Level.Trace))
            {
                formatAndLogString (contextId, Level.Trace, s);
            }
        }
    }

    @Override
    public void logWarn (final String contextId, final IValue<String> s)
    {
        try (final LoggingOperationScope ls = new LoggingOperationScope ())
        {
            if (isLevelEnabled (Level.Warn))
            {
                formatAndLogString (contextId, Level.Warn, s);
            }
        }
    }

    @Override
    public void setLevels (final String levelsConfig)
    {
        final boolean[] levelEnableFlags = new boolean[Level.getMaxIntValue () + 1];

        // Set any that are enabled.
        final Level[] enabledLevels = Logging.parseLevelString (levelsConfig);
        for (final Level enabledLevel : enabledLevels)
        {
            final int intValue = enabledLevel.getIntValue ();
            levelEnableFlags[intValue] = true;
        }

        m_enabledLevels.set (levelEnableFlags);
    }

    private void formatAndLogString (final String contextId, final Level level, final IValue<String> s)
    {
        // Intercept the string and prepend context id.
        final IValue<String> fs = new FormattedStringValue (s.get ());

        final String formattedString = m_formatter.apply (level, fs);
        logString (contextId, level, formattedString);

        // Pass to slave loggers too.
        m_slaves.forEach (isl -> isl.logString (contextId, level, formattedString));
    }

    private boolean isLevelEnabled (final Level level)
    {
        return m_enabledLevels.get ()[level.getIntValue ()];
    }

    /** Which logging levels are enabled */
    private final AtomicReference<boolean[]> m_enabledLevels = new AtomicReference<boolean[]> ();

    /** Message formatting policy */
    private final BiFunction<Level, IValue<String>, String> m_formatter;

    /** A collection of slave string loggers that receive the same formatted messages */
    private final List<IStringLogger> m_slaves = GenericFactory.newArrayList ();
}
