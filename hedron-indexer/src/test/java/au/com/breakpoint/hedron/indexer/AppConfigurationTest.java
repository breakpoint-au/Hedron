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
package au.com.breakpoint.hedron.indexer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.indexer.AppConfiguration;
import au.com.breakpoint.hedron.indexer.AppConfiguration.SourcePath;

public class AppConfigurationTest
{
    @Test
    public void testReadConfiguration ()
    {
        final String path = HcUtil.getProjectsDirectoryName () + "/hedron-indexer/src/test/resources/config-test.json";

        final AppConfiguration c = AppConfiguration.readJsonFile (path);

        //        {
        //            // Individual source paths, associating with a logical name.
        //            sourcePaths:
        //            [
        //                {
        //                    name: 'sp1',
        //                    path: 'B:/Projects/sr/TLSOTR/sp1',
        //                    excludePaths: [ 'uat', 'prod' ],
        //                    includeFiles: [ '*.sql' ]
        //                    excludeFiles: [ 'somefile.sql' ]
        //                },
        //                {
        //                    name: 'sp2',
        //                    path: 'B:/Projects/sr/TLSOTR/sp2',
        //                    includeFiles: [ '*.sql' ]
        //                }
        //            ],
        //
        //            // Overall configuration.
        //            indexing:
        //            [
        //                {
        //                    sourcePaths: [ 'sp1', 'sp2' ],
        //                    destination: 'B:/Projects/sr/TLSOTR/sql/index'
        //                }
        //            ]
        //        }

        final String[] excludeFiles1 = new String[]
        {
                "excludeFile1a.sql",
                "excludeFile1b.sql"
        };
        final String[] includeFiles1 = new String[]
        {
                "includeFile1a.sql",
                "includeFile1b.sql"
        };
        checkSourcePath (c, 0, "B:/Projects/sr/TLSOTR/sp1", includeFiles1, excludeFiles1);

        final String[] excludeFiles2 = new String[]
        {
                "excludeFile2a.sql",
                "excludeFile2b.sql"
        };
        final String[] includeFiles2 = new String[]
        {
                "includeFile2a.sql",
                "includeFile2b.sql"
        };
        checkSourcePath (c, 1, "B:/Projects/sr/TLSOTR/sp2", includeFiles2, excludeFiles2);
    }

    private void checkSourcePath (final AppConfiguration c, final int i, final String path, final String[] includeFiles,
        final String[] excludeFiles)
    {
        final SourcePath sp = c.getSourcePaths ()[i];
        assertEquals (path, sp.getPath ());
        assertArrayEquals (includeFiles, sp.getIncludeFiles ());
        assertArrayEquals (excludeFiles, sp.getExcludeFiles ());
    }
}
