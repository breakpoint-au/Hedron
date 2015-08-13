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
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.HcUtilFile;
import au.com.breakpoint.hedron.core.GenericFactory;
import au.com.breakpoint.hedron.core.NullFormatter;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.core.log.AbstractLogger;
import au.com.breakpoint.hedron.core.log.IStringLogger;
import au.com.breakpoint.hedron.core.log.Level;
import au.com.breakpoint.hedron.core.log.Logging;

public class DummyLogger extends AbstractLogger
{
    public DummyLogger (final String levelsConfig, final Collection<? extends IStringLogger> slaves,
        final String logToFilename)
    {
        super (new NullFormatter (), levelsConfig, slaves);
        m_logToFilename = logToFilename;

        HcUtilFile.safeDeleteFileNoThrow (m_logToFilename);
    }

    public void clearLogging ()
    {
        m_valueLogDebug.clear ();
        m_valueLogError.clear ();
        m_valueLogFatal.clear ();
        m_valueLogInfo.clear ();
        m_valueLogTrace.clear ();
        m_valueLogWarn.clear ();
    }

    @Override
    public void logString (final String contextId, final Level level, final String s)
    {
        switch (level)
        {
            case Debug:
            {
                m_valueLogDebug.add (s);
                //System.out.printf ("m_valueLogDebug %s%n", m_valueLogDebug);
                break;
            }

            case Error:
            {
                m_valueLogError.add (s);
                //System.out.printf ("m_valueLogError %s%n", m_valueLogError);
                break;
            }

            case Fatal:
            {
                m_valueLogFatal.add (s);
                //System.out.printf ("m_valueLogFatal %s%n", m_valueLogFatal);
                break;
            }

            case Info:
            {
                m_valueLogInfo.add (s);
                //System.out.printf ("m_valueLogInfo %s%n", m_valueLogInfo);
                break;
            }

            case Trace:
            {
                m_valueLogTrace.add (s);
                //System.out.printf ("m_valueLogTrace %s%n", m_valueLogTrace);
                break;
            }

            case Warn:
            {
                m_valueLogWarn.add (s);
                //System.out.printf ("m_valueLogWarn %s%n", m_valueLogWarn);
                break;
            }

            default:
            {
                ThreadContext.assertFault (false, "Unsupported value [%s]", level);
                break;
            }
        }

        writeToFile (level, s);
    }

    protected void writeToFile (final Level level, final String s)
    {
        if (m_logToFilename != null)
        {
            final String indented = Logging.prefixWithLevel (level, s);
            HcUtilFile.appendToFile (m_logToFilename,
                indented + HcUtil.NewLine + "------------------------------------" + HcUtil.NewLine);
        }
    }

    final List<String> m_valueLogDebug = GenericFactory.newArrayList ();

    final List<String> m_valueLogError = GenericFactory.newArrayList ();

    final List<String> m_valueLogFatal = GenericFactory.newArrayList ();

    final List<String> m_valueLogInfo = GenericFactory.newArrayList ();

    final List<String> m_valueLogTrace = GenericFactory.newArrayList ();

    final List<String> m_valueLogWarn = GenericFactory.newArrayList ();

    private final String m_logToFilename;
}
