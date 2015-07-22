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
package au.com.breakpoint.hedron.core;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.HcUtilFile;
import au.com.breakpoint.hedron.core.SmartFile;

public class SmartFileTest
{
    @Test
    public void testSmartFileChanged ()
    {
        try
        {
            HcUtilFileTest.writeSampleFile (TEMP_FILEPATH_1, LINE_COUNT);
            HcUtilFileTest.writeSampleFile (TEMP_FILEPATH_2, LINE_COUNT);
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
    public void testSmartFileUnchanged ()
    {
        try
        {
            HcUtilFileTest.writeSampleFile (TEMP_FILEPATH_1, LINE_COUNT);
            HcUtilFileTest.writeSampleFile (TEMP_FILEPATH_2, LINE_COUNT);
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

    public static boolean writeSmartSampleFile (final String filePath, final int lines)
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
                    f.print ("line " + i + " ");
                }
                f.printf ("line " + i + "%n");
            }
        }
        finally
        {
            updated = HcUtilFile.safeClose (f);
        }

        return updated;
    }

    private static String getTempPath (final String filename)
    {
        return HcUtil.formFilepath (HcUtil.getTempDirectoryName (), filename);
    }

    private static final int LINE_COUNT = 100;

    private static final String TEMP_FILEPATH_1 = getTempPath ("SmartFileTest-1.temp");

    private static final String TEMP_FILEPATH_2 = getTempPath ("SmartFileTest-2.temp");
}
