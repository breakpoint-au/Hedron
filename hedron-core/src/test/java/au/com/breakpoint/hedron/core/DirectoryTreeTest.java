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

import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class DirectoryTreeTest
{
    @Test
    public void testDirectoryTree ()
    {
        if (m_performTest)
        {
            final String directoryPath = projDirFilename ("testdata");

            final DirectoryTree dt1 = new DirectoryTree (directoryPath);
            final List<String> a1FilePaths = dt1.getAbsolutePaths ();

            final DirectoryTree dt2 = new DirectoryTree (directoryPath);
            final List<String> a2FilePaths = dt2.getAbsolutePaths ();

            for (@SuppressWarnings ("unused")
            final String f : a1FilePaths)
            {
                //System.out.println (f);
            }

            assertEquals (EXPECTED_PATHS, a2FilePaths);
            assertEquals (a1FilePaths, a2FilePaths);
        }
    }

    @Test
    public void testDirectoryTreeExcludeFiles1 ()
    {
        if (m_performTest)
        {
            final String directoryPath = projDirFilename ("testdata");

            final DirectoryTree dt = new DirectoryTree (directoryPath, null, new String[]
            {
                    ".+f0"
            });

            final List<String> aFilePaths = dt.getAbsolutePaths ();
            for (@SuppressWarnings ("unused")
            final String f : aFilePaths)
            {
                //System.out.println (f);
            }

            assertEquals (EXPECTED_PATHS_EX_F0, aFilePaths);
        }
    }

    @Test
    public void testDirectoryTreeExcludeFiles2 ()
    {
        if (m_performTest)
        {
            final String directoryPath = projDirFilename ("testdata");

            final DirectoryTree dt = new DirectoryTree (directoryPath, null, new String[]
            {
                    ".+f0",
                    ".+f2"
            });

            final List<String> aFilePaths = dt.getAbsolutePaths ();
            for (@SuppressWarnings ("unused")
            final String f : aFilePaths)
            {
                //System.out.println (f);
            }

            assertEquals (EXPECTED_PATHS_EX_F0_F2, aFilePaths);
        }
    }

    @Test
    public void testDirectoryTreeExcludeSource ()
    {
        if (m_performTest)
        {
            final String directoryPath = projDirFilename ("testdata");

            final DirectoryTree dt = new DirectoryTree (directoryPath, new String[]
            {
                    getProjectsPath ("hedron-core/testdata/source")
            }, null);

            final List<String> aFilePaths = dt.getAbsolutePaths ();
            for (@SuppressWarnings ("unused")
            final String f : aFilePaths)
            {
                //System.out.println (f);
            }

            assertEquals (EXPECTED_PATHS_EX_SOURCE, aFilePaths);
        }
    }

    @Test
    public void testDirectoryTreeExcludeSourceD1 ()
    {
        if (m_performTest)
        {
            final String directoryPath = projDirFilename ("testdata");

            final DirectoryTree dt = new DirectoryTree (directoryPath, new String[]
            {
                    getProjectsPath ("hedron-core/testdata/source/d1")
            }, null);

            final List<String> aFilePaths = dt.getAbsolutePaths ();
            for (@SuppressWarnings ("unused")
            final String f : aFilePaths)
            {
                //System.out.println (f);
            }

            assertEquals (EXPECTED_PATHS_EX_SOURCE_D1, aFilePaths);
        }
    }

    private String projDirFilename (final String suffix)
    {
        return HcUtil.formFilepath (PROJ_DIR, suffix);
    }

    private static String getProjectsPath (final String subpath)
    {
        return HcUtil.formFilepath (HcUtil.getProjectsDirectoryName (), subpath);
    }

    private static final List<String> EXPECTED_PATHS = Arrays.asList (new String[]
    {
            getProjectsPath ("hedron-core/testdata/dest/d1/d1f0"),
            getProjectsPath ("hedron-core/testdata/dest/d1/d1f1"),
            getProjectsPath ("hedron-core/testdata/dest/d1/d1f2"),
            getProjectsPath ("hedron-core/testdata/dest/d2/d2f0"),
            getProjectsPath ("hedron-core/testdata/dest/d2/d2f1"),
            getProjectsPath ("hedron-core/testdata/source/d0/d0f0"),
            getProjectsPath ("hedron-core/testdata/source/d0/d0f1"),
            getProjectsPath ("hedron-core/testdata/source/d1/d1f0"),
            getProjectsPath ("hedron-core/testdata/source/d1/d1f1"),
            getProjectsPath ("hedron-core/testdata/source/d1/d1f2")
    });

    private static final List<String> EXPECTED_PATHS_EX_F0 = Arrays.asList (new String[]
    {
            getProjectsPath ("hedron-core/testdata/dest/d1/d1f1"),
            getProjectsPath ("hedron-core/testdata/dest/d1/d1f2"),
            getProjectsPath ("hedron-core/testdata/dest/d2/d2f1"),
            getProjectsPath ("hedron-core/testdata/source/d0/d0f1"),
            getProjectsPath ("hedron-core/testdata/source/d1/d1f1"),
            getProjectsPath ("hedron-core/testdata/source/d1/d1f2")
    });

    private static final List<String> EXPECTED_PATHS_EX_F0_F2 = Arrays.asList (new String[]
    {
            getProjectsPath ("hedron-core/testdata/dest/d1/d1f1"),
            getProjectsPath ("hedron-core/testdata/dest/d2/d2f1"),
            getProjectsPath ("hedron-core/testdata/source/d0/d0f1"),
            getProjectsPath ("hedron-core/testdata/source/d1/d1f1"),
    });

    private static final List<String> EXPECTED_PATHS_EX_SOURCE = Arrays.asList (new String[]
    {
            getProjectsPath ("hedron-core/testdata/dest/d1/d1f0"),
            getProjectsPath ("hedron-core/testdata/dest/d1/d1f1"),
            getProjectsPath ("hedron-core/testdata/dest/d1/d1f2"),
            getProjectsPath ("hedron-core/testdata/dest/d2/d2f0"),
            getProjectsPath ("hedron-core/testdata/dest/d2/d2f1")
    });

    private static final List<String> EXPECTED_PATHS_EX_SOURCE_D1 = Arrays.asList (new String[]
    {
            getProjectsPath ("hedron-core/testdata/dest/d1/d1f0"),
            getProjectsPath ("hedron-core/testdata/dest/d1/d1f1"),
            getProjectsPath ("hedron-core/testdata/dest/d1/d1f2"),
            getProjectsPath ("hedron-core/testdata/dest/d2/d2f0"),
            getProjectsPath ("hedron-core/testdata/dest/d2/d2f1"),
            getProjectsPath ("hedron-core/testdata/source/d0/d0f0"),
            getProjectsPath ("hedron-core/testdata/source/d0/d0f1")
    });

    private static final boolean m_performTest = true;

    private static final String PROJ_DIR = HcUtil.getBputilProjectDirectoryName ();
}
