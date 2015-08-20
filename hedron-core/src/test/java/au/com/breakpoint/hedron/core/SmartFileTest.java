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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import org.junit.Test;
import au.com.breakpoint.hedron.core.context.ThreadContext;

public class SmartFileTest
{
    @Test
    public void testSmartFileChanged ()
    {
        try
        {
            writeSampleFile (TEMP_FILEPATH_1, LINE_COUNT);
            writeSampleFile (TEMP_FILEPATH_2, LINE_COUNT);
            assertTrue (HcUtilFile.areFilesIdentical (TEMP_FILEPATH_1, TEMP_FILEPATH_2));

            final boolean updated = writeSmartSampleFile (TEMP_FILEPATH_2, LINE_COUNT + 1);
            assertTrue (updated);
            assertTrue (!HcUtilFile.areFilesIdentical (TEMP_FILEPATH_1, TEMP_FILEPATH_2));
        }
        finally
        {
            HcUtilFile.safeDeleteFileNoThrow (TEMP_FILEPATH_1);
            HcUtilFile.safeDeleteFileNoThrow (TEMP_FILEPATH_2);
        }
    }

    @Test
    public void testSmartFileChanged_multi ()
    {
        try
        {
            final int count = 3;
            writeSampleFile (TEMP_FILEPATH_1, LINE_COUNT, count);
            writeSampleFile (TEMP_FILEPATH_2, LINE_COUNT, count);
            assertTrue (HcUtilFile.areFilesIdentical (TEMP_FILEPATH_1, TEMP_FILEPATH_2));

            final boolean updated = writeSmartSampleFile (TEMP_FILEPATH_2, LINE_COUNT + 1, count);
            assertTrue (updated);
            assertTrue (!HcUtilFile.areFilesIdentical (TEMP_FILEPATH_1, TEMP_FILEPATH_2));
        }
        finally
        {
            HcUtilFile.safeDeleteFileNoThrow (TEMP_FILEPATH_1);
            HcUtilFile.safeDeleteFileNoThrow (TEMP_FILEPATH_2);
        }
    }

    @Test
    public void testSmartFileUnchanged ()
    {
        try
        {
            writeSampleFile (TEMP_FILEPATH_1, LINE_COUNT);
            writeSampleFile (TEMP_FILEPATH_2, LINE_COUNT);
            assertTrue (HcUtilFile.areFilesIdentical (TEMP_FILEPATH_1, TEMP_FILEPATH_2));

            final boolean updated = writeSmartSampleFile (TEMP_FILEPATH_2, LINE_COUNT);
            assertTrue (!updated);
            assertTrue (HcUtilFile.areFilesIdentical (TEMP_FILEPATH_1, TEMP_FILEPATH_2));
        }
        finally
        {
            HcUtilFile.safeDeleteFileNoThrow (TEMP_FILEPATH_1);
            HcUtilFile.safeDeleteFileNoThrow (TEMP_FILEPATH_2);
        }
    }

    @Test
    public void testSmartFileUnchanged_multi ()
    {
        try
        {
            final int count = 3;
            writeSampleFile (TEMP_FILEPATH_1, LINE_COUNT, count);
            writeSampleFile (TEMP_FILEPATH_2, LINE_COUNT, count);
            assertTrue (HcUtilFile.areFilesIdentical (TEMP_FILEPATH_1, TEMP_FILEPATH_2));

            final boolean updated = writeSmartSampleFile (TEMP_FILEPATH_2, LINE_COUNT, count);
            assertTrue (!updated);
            assertTrue (HcUtilFile.areFilesIdentical (TEMP_FILEPATH_1, TEMP_FILEPATH_2));
        }
        finally
        {
            HcUtilFile.safeDeleteFileNoThrow (TEMP_FILEPATH_1);
            HcUtilFile.safeDeleteFileNoThrow (TEMP_FILEPATH_2);
        }
    }

    private static String getTempPath (final String filename)
    {
        return HcUtil.formFilepath (HcUtil.getTempDirectoryName (), filename);
    }

    private static void writeSampleFile (final String filepath, final int lines)
    {
        writeSampleFile (filepath, lines, 1);
    }

    private static void writeSampleFile (final String filepath, final int lines, final int count)
    {
        try (final PrintWriter pw = new PrintWriter (new FileWriter (filepath)))
        {
            writeSampleLines (pw, lines, count);
        }
        catch (final IOException e)
        {
            // Translate the exception.
            ThreadContext.throwFault (e);
        }
    }

    private static void writeSampleLines (final PrintWriter pw, final int lines, final int count)
    {
        for (int k = 0; k < count; ++k)
        {
            for (int i = 0; i < lines; ++i)
            {
                for (int j = 0; j < i; ++j)
                {
                    pw.print (k + ":line " + i + " ");
                }
                pw.println (k + ":line " + i);
            }
        }
    }

    private static boolean writeSmartSampleFile (final String filePath, final int lines)
    {
        boolean updated = false;

        SmartFile f = null;
        try
        {
            f = new SmartFile (filePath);
            for (int i = 0; i < lines; ++i)
            {
                for (int j = 0; j < i; ++j)
                {
                    f.print ("0:line " + i + " ");
                }
                f.printf ("0:line %s%n", i);
            }
        }
        finally
        {
            updated = HcUtilFile.safeClose (f);
        }

        return updated;
    }

    private static boolean writeSmartSampleFile (final String filePath, final int lines, final int count)
    {
        boolean updated = false;

        SmartFile f = null;
        try
        {
            f = new SmartFile (filePath);
            for (int k = 0; k < count; ++k)
            {
                f.setSectionNumber (k);

                for (int i = 0; i < lines; ++i)
                {
                    for (int j = 0; j < i; ++j)
                    {
                        f.print (k + ":line " + i + " ");
                    }
                    f.printf ("%s:line %s%n", k, i);
                }
            }
        }
        finally
        {
            updated = HcUtilFile.safeClose (f);
        }

        return updated;
    }

    private static final int LINE_COUNT = 3;

    private static final String TEMP_FILEPATH_1 = getTempPath ("SmartFileTest-1.temp");

    private static final String TEMP_FILEPATH_2 = getTempPath ("SmartFileTest-2.temp");
}
