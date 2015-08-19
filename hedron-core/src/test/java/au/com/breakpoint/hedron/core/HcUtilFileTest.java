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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import au.com.breakpoint.hedron.core.Tuple.E3;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.core.log.Logging;
import au.com.breakpoint.hedron.core.value.IValue;
import au.com.breakpoint.hedron.core.value.SafeLazyValue;

public class HcUtilFileTest
{
    @Test
    public void test_asciiHexStringToByteArray ()
    {
        final String s = "0102030405060708090A0B0C0D17212B353F49535D67717B7C7D7E7F";
        final byte[] bytes = HcUtilFile.asciiHexStringToByteArray (s);
        assertArrayEquals (new byte[]
        {
                1,
                2,
                3,
                4,
                5,
                6,
                7,
                8,
                9,
                10,
                11,
                12,
                13,
                23,
                33,
                43,
                53,
                63,
                73,
                83,
                93,
                103,
                113,
                123,
                124,
                125,
                126,
                127
        }, bytes);
    }

    @Test
    public void test_byteArrayToAsciiHexString ()
    {
        final byte[] bytes =
            {
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7,
                    8,
                    9,
                    10,
                    11,
                    12,
                    13,
                    23,
                    33,
                    43,
                    53,
                    63,
                    73,
                    83,
                    93,
                    103,
                    113,
                    123,
                    124,
                    125,
                    126,
                    127
        };
        final String s = HcUtilFile.byteArrayToAsciiHexString (bytes);
        assertEquals ("0102030405060708090A0B0C0D17212B353F49535D67717B7C7D7E7F", s);
    }

    @Test
    public void testCompareDirectories ()
    {
        if (m_performTest)
        {
            final String inputBaseDirectory = projDirFilename ("testdata/source");
            final String outputBaseDirectory = projDirFilename ("testdata/dest");

            final E3<List<String>, List<String>, List<String>> diffs =
                HcUtilFile.compareSubdirectories (inputBaseDirectory, outputBaseDirectory, null, null, null);

            final List<String> sourceOnly = diffs.getE0 ();
            assertEquals (2, sourceOnly.size ());
            assertEquals ("d0/d0f0", sourceOnly.get (0));
            assertEquals ("d0/d0f1", sourceOnly.get (1));

            final List<String> shared = diffs.getE1 ();
            assertEquals (3, shared.size ());
            assertEquals ("d1/d1f0", shared.get (0));
            assertEquals ("d1/d1f1", shared.get (1));
            assertEquals ("d1/d1f2", shared.get (2));

            final List<String> destOnly = diffs.getE2 ();
            assertEquals (2, destOnly.size ());
            assertEquals ("d2/d2f1", destOnly.get (0));
            assertEquals ("d2/d2f0", destOnly.get (1));
        }
    }

    @Test
    public void testDeleteEmptyDirectories1 ()
    {
        createTestDirs ();

        final List<String> deletedDirs = HcUtilFile.deleteEmptyDirectories (TEMP_FILEPATH_PRUNE, true);
        Logging.logInfo (deletedDirs.toString ());

        assertTrue (!new File (TEMP_FILEPATH_PRUNE).exists ());
    }

    @Test
    public void testDeleteEmptyDirectories2 ()
    {
        createTestDirs ();

        final List<String> deletedDirs = HcUtilFile.deleteEmptyDirectories (TEMP_FILEPATH_PRUNE, false);
        Logging.logInfo (deletedDirs.toString ());

        assertTrue (new File (TEMP_FILEPATH_PRUNE).exists ());
    }

    @Test
    public void testDeleteEmptyDirectories3 ()
    {
        createTestDirs ();

        final String remainingFilepath = getTempPath ("HcUtilTest-prune.temp/d1/d11/remaining-file.txt");
        writeSampleFile (remainingFilepath, 10);

        final List<String> deletedDirs = HcUtilFile.deleteEmptyDirectories (TEMP_FILEPATH_PRUNE, true);
        Logging.logInfo (deletedDirs.toString ());

        final File remainingFile = new File (remainingFilepath);
        assertTrue (remainingFile.exists ());

        // Clean up.
        remainingFile.delete ();
        HcUtilFile.deleteEmptyDirectories (TEMP_FILEPATH_PRUNE, true);
    }

    @Test
    public void testFileDifferent ()
    {
        writeSampleFile (TEMP_FILEPATH_2, LINE_COUNT * 2);
        assertTrue (!HcUtilFile.areFilesIdentical (TEMP_FILEPATH_1, TEMP_FILEPATH_2));

        HcUtilFile.safeDeleteFileNoThrow (TEMP_FILEPATH_2);
    }

    @Test
    public void testFileIdentical ()
    {
        assertTrue (HcUtilFile.areFilesIdentical (TEMP_FILEPATH_1, TEMP_FILEPATH_1));

        writeSampleFile (TEMP_FILEPATH_2, LINE_COUNT);
        assertTrue (HcUtilFile.areFilesIdentical (TEMP_FILEPATH_1, TEMP_FILEPATH_2));

        HcUtilFile.safeDeleteFileNoThrow (TEMP_FILEPATH_2);
    }

    @Test
    public void testGetFileObjects ()
    {
        final String dirName = projDirFilename ("testdata/dest/d1");

        final String[] includeFiles1 =
            {
                    "d.+"
        };
        final String[] includeFiles2 =
            {
                    "d..[0-1]",
                    "d..2"
        };
        final String[] includeFiles3 =
            {
                    "d..[0-1]",
                    "d..3"
        };
        final String[] excludeFiles =
            {
                    ".+1"
        };

        final File d1f0 = projDirFile ("testdata/dest/d1/d1f0");
        final File d1f1 = projDirFile ("testdata/dest/d1/d1f1");
        final File d1f2 = projDirFile ("testdata/dest/d1/d1f2");

        final File dir = HcUtilFile.getDirectory (dirName);

        //    B:/Projects/hedron/testdata/dest/d1/d1f0
        //    B:/Projects/hedron/testdata/dest/d1/d1f1  <- exclude
        //    B:/Projects/hedron/testdata/dest/d1/d1f2
        assertEquals (Arrays.asList (d1f0, d1f2), HcUtilFile.getFileObjects (dir, false, includeFiles1, excludeFiles));

        assertEquals (Arrays.asList (d1f0, d1f1, d1f2), HcUtilFile.getFileObjects (dir, false, includeFiles1, null));
        assertEquals (Arrays.asList (d1f0, d1f1, d1f2), HcUtilFile.getFileObjects (dir, false, includeFiles2, null));
        assertEquals (Arrays.asList (d1f0, d1f1), HcUtilFile.getFileObjects (dir, false, includeFiles3, null));
        assertEquals (Arrays.asList (d1f0, d1f1, d1f2), HcUtilFile.getFileObjects (dir, false, null, null));
        assertEquals (Arrays.asList (d1f0), HcUtilFile.getFileObjects (dir, false, includeFiles3, excludeFiles));
    }

    @Test
    public void testGetRootName ()
    {
        // TODO 3 /Volumes/Internal-2TB/ on os x
        //final String projectsRoot = HcUtil.toSlashPath (HcUtilFile.getRoot (HcUtil.getProjectsDirectoryName ()).toString ());
        //assertEquals (projectsRoot, HcUtilFile.getRootName ("ahem/blah/blah"));

        assertEquals ("/", HcUtilFile.getRootName ("/ahem/blah/blah"));

        if (HcUtil.isOperatingSystemWindows ())
        {
            assertEquals ("A:/", HcUtilFile.getRootName ("A:/temp"));
            assertEquals ("Q:/", HcUtilFile.getRootName ("Q:/temp"));
            assertEquals ("C:/", HcUtilFile.getRootName ("C:/temp/nonexistent"));
            assertEquals ("C:/", HcUtilFile.getRootName ("C:/temp/nonexistent"));
        }
        else
        {
            assertEquals ("/Volumes/ahem/", HcUtilFile.getRootName ("/Volumes/ahem/blah/blah"));
            assertEquals ("/Volumes/leigh/", HcUtilFile.getRootName ("/Volumes/leigh"));

            assertEquals ("/", HcUtilFile.getRootName ("/Volume/ahem/blah/blah"));
            assertEquals ("/", HcUtilFile.getRootName ("/Volumess/ahem/blah/blah"));
        }
    }

    @Test
    public void testSerialiseDeserialise ()
    {
        final int numRuns = 10;

        final java.util.Random rnd = new java.util.Random ();
        for (int i = 0, numBytes = 999; i < numRuns; ++i, --numBytes)
        {
            for (int j = 0; j < numBytes; j++)
            {
                final byte[] arr = new byte[j];
                for (int k = 0; k < j; ++k)
                {
                    arr[k] = (byte) rnd.nextInt ();
                }

                if (true)
                {
                    final String s = HcUtilFile.serialiseObjectAsBase64String (arr);
                    final byte[] b = HcUtilFile.deserialiseBase64StringAsObject (s);
                    assertTrue (Arrays.equals (arr, b));
                }

                if (true)
                {
                    final String s = HcUtilFile.serialiseObjectAsAsciiHexString (arr);
                    final byte[] b = HcUtilFile.deserialiseAsciiHexStringAsObject (s);
                    assertTrue (Arrays.equals (arr, b));
                }

                if (true)
                {
                    final byte[] s = HcUtilFile.serialiseObjectAsBytes (arr);
                    final byte[] b = HcUtilFile.deserialiseBytesAsObject (s);
                    assertTrue (Arrays.equals (arr, b));
                }
            }
        }
    }

    private void createTestDirs ()
    {
        if (HcUtilFile.doesDirectoryExist (TEMP_FILEPATH_PRUNE))
        {
            HcUtilFile.deleteFileOrDirectory (TEMP_FILEPATH_PRUNE);
        }

        HcUtilFile.ensureDirectoryExists (TEMP_FILEPATH_PRUNE);
        HcUtilFile.ensureDirectoryExists (TEMP_FILEPATH_PRUNE + "/d0/d00");
        HcUtilFile.ensureDirectoryExists (TEMP_FILEPATH_PRUNE + "/d0/d01");
        HcUtilFile.ensureDirectoryExists (TEMP_FILEPATH_PRUNE + "/d0/d02");
        HcUtilFile.ensureDirectoryExists (TEMP_FILEPATH_PRUNE + "/d1/d10");
        HcUtilFile.ensureDirectoryExists (TEMP_FILEPATH_PRUNE + "/d1/d11");
        HcUtilFile.ensureDirectoryExists (TEMP_FILEPATH_PRUNE + "/d1/d12");
        HcUtilFile.ensureDirectoryExists (TEMP_FILEPATH_PRUNE + "/d2");
    }

    private File projDirFile (final String suffix)
    {
        return new File (HcUtil.formFilepath (getProjectDir (), suffix));
    }

    private String projDirFilename (final String suffix)
    {
        return HcUtil.formFilepath (getProjectDir (), suffix);
    }

    @BeforeClass
    public static void setUpBeforeClass () throws Exception
    {
        writeSampleFile (TEMP_FILEPATH_1, LINE_COUNT);
    }

    @AfterClass
    public static void tearDownAfterClass () throws Exception
    {
        HcUtilFile.safeDeleteFileNoThrow (TEMP_FILEPATH_1);
        HcUtilFile.safeDeleteFileNoThrow (TEMP_FILEPATH_2);
    }

    private static String getProjectDir ()
    {
        return m_projectDirValue.get ();
    }

    private static String getTempPath (final String filename)
    {
        return HcUtil.formFilepath (HcUtil.getTempDirectoryName (), filename);
    }

    private static void writeSampleFile (final String filepath, final int lines)
    {
        try (final PrintWriter pw = new PrintWriter (new FileWriter (filepath)))
        {
            for (int i = 0; i < lines; ++i)
            {
                for (int j = 0; j < i; ++j)
                {
                    pw.print ("line " + i + " ");
                }
                pw.println ("line " + i);
            }
        }
        catch (final IOException e)
        {
            // Translate the exception.
            ThreadContext.throwFault (e);
        }
    }

    private static final int LINE_COUNT = 100;

    private static final boolean m_performTest = true;

    private static final IValue<String> m_projectDirValue =
        SafeLazyValue.of ( () -> HcUtil.getHcUtilProjectDirectoryName ());

    private static final String TEMP_FILEPATH_1 = getTempPath ("HcUtilTest-1.temp");

    private static final String TEMP_FILEPATH_2 = getTempPath ("HcUtilTest-2.temp");

    private static final String TEMP_FILEPATH_PRUNE = getTempPath ("HcUtilTest-prune.temp");
}
