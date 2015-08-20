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

import au.com.breakpoint.hedron.core.value.IValue;

/**
 * An abstraction of logging allowing late evaluation of the log string. Use adaptor
 * delegates for log4j, Logback, slf4j, Java logging, etc.
 */
public interface ILogger
{
    public boolean isEnabled (final Level level);

    public void logDebug (final String contextId, final IValue<String> s);

    public void logError (final String contextId, final IValue<String> s);

    public void logFatal (final String contextId, final IValue<String> s);

    public void logInfo (final String contextId, final IValue<String> s);

    public void logTrace (final String contextId, final IValue<String> s);

    public void logWarn (final String contextId, final IValue<String> s);

    public void setLevels (final String levelsConfig);
}
