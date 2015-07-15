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
package au.com.breakpoint.hedron.core.log;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.function.BiFunction;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.HcUtilFile;
import au.com.breakpoint.hedron.core.TimedScope;
import au.com.breakpoint.hedron.core.TimestampLevelFormatter;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.core.value.IValue;

/**
 * A logging implementation allowing late evaluation of the log string. Writes to the a
 * base log folder with a folder per day containing a file per hour, for example, with
 * base folder c:\temp\alogFolder:
 *
 * <pre>
 *      c:\temp\alogFolder\2012-04-19
 *      c:\temp\alogFolder\2012-04-19\13.log
 *      c:\temp\alogFolder\2012-04-19\14.log
 * </pre>
 *
 * Formatting of the text as as per the configured strategy object, eg NullLogFormatter
 * for text written as is, without any additional timestamping or formatting, or
 * ContextLogFormatter for timestamp and ThreadContext id.
 */
public class FolderPerDayLogger extends AbstractLogger
{
    /**
     * Allows explicit specification of the formatting strategy.
     *
     * @param logFolder
     *            Base folder for the daily folders
     * @param formatter
     * @param levelsConfig
     *            fewi for Fatal..Info, etc
     * @param extension
     * @param slaves
     */
    public FolderPerDayLogger (final String logFolder, final BiFunction<Level, IValue<String>, String> formatter,
        final String levelsConfig, final int logDaysToKeep, final String extension,
        final Collection<? extends IStringLogger> slaves)
    {
        super (formatter, levelsConfig, slaves);

        m_logFolder = logFolder;
        m_extension = extension;
        configureLogCleanup (logDaysToKeep);
    }

    /**
     * Default to formatting messages with timestamp and context id.
     *
     * @param logFolder
     *            Base folder for the daily folders
     * @param levelsConfig
     *            fewi for Fatal..Info, etc
     * @param slaves
     */
    public FolderPerDayLogger (final String logFolder, final String levelsConfig, final int logDaysToKeep,
        final Collection<? extends IStringLogger> slaves)
    {
        this (logFolder, new TimestampLevelFormatter (), levelsConfig, logDaysToKeep, "log", slaves);
    }

    @Override
    public synchronized void logString (final String contextId, final Level level, final String s)
    {
        m_scopeLogMessage.execute ( () ->
        {
            HcUtilFile.appendToFile (getLogfilePath (), s + HcUtil.NewLine);
        });
    }

    private void configureLogCleanup (final int logDaysToKeep)
    {
        final long periodMinutes = 30L;
        HcUtil.schedulePeriodically ( () -> cleanUpLogFiles (m_logFolder, logDaysToKeep), periodMinutes * 60 * 1000);
    }

    private String getLogfilePath ()
    {
        final Date date = new Date ();

        // eg c:/temp/logFolder/2012-03-09/10.log for 10am to 11am
        final String folder = m_logFolder + getLogSubfolder (date);
        if (!folder.equals (m_lastFolder))
        {
            HcUtilFile.ensureDirectoryExists (folder);
            m_lastFolder = folder;
        }

        final String filename = new SimpleDateFormat ("HH'." + m_extension + "'").format (date);

        return HcUtil.formFilepath (folder, filename);
    }

    public static String getLogSubfolder (final Date date)
    {
        return "/" + getLogFolderDateFormat ().format (date);
    }

    public static Date parseLogSubfolderDate (final String name)
    {
        Date date = null;

        try
        {
            date = getLogFolderDateFormat ().parse (name);
        }
        catch (final ParseException e)
        {
            // leave as null so can ignore unrecognised folders
        }

        return date;
    }

    private static void cleanUpLogFiles (final String logFolder, final int logDaysToKeep)
    {
        final int daysToKeep = Math.max (logDaysToKeep, 1);// don't delete today's directory
        final long now = System.currentTimeMillis ();

        // Get list of subdirectories. For those over daysToKeep old, remove them.
        final File dir = new File (logFolder);

        final File[] filesAndDirs = dir.listFiles ();
        if (filesAndDirs != null)
        {
            for (final File file : filesAndDirs)
            {
                if (!file.isFile ())
                {
                    // Must be a directory.
                    final String name = file.getName ();
                    final Date date = parseLogSubfolderDate (name);

                    final long ageMsec = now - date.getTime ();
                    final long ageDays = ageMsec / (1000L * 60L * 60L * 24L);
                    if (ageDays > daysToKeep)
                    {
                        // Purge the directory.
                        try
                        {
                            HcUtilFile.deleteFileOrDirectory (file);
                        }
                        catch (final Throwable e)
                        {
                            // Log if not already logged. Try to carry on and delete the rest.
                            ThreadContext.logException (e);
                        }
                    }
                }
            }
        }
    }

    private static SimpleDateFormat getLogFolderDateFormat ()
    {
        // NB don't share instances - not threadsafe.
        return new SimpleDateFormat ("yyyy-MM-dd");
    }

    private final String m_extension;

    private String m_lastFolder;

    private final String m_logFolder;

    private static final TimedScope m_scopeLogMessage = TimedScope.of (FolderPerDayLogger.class, "logMessage");
}
