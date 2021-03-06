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
package au.com.breakpoint.hedron.indexer;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import au.com.breakpoint.hedron.core.args4j.HcUtilArgs4j;

public class OptionsTest
{
    @Test
    public void testMode ()
    {
        final Options options = new Options ();
        final String[] args = new String[]
        {
                "--config",
                "someval",
                "--mode",
                "Index"
        };

        // Check args & prepare usage string (in thrown AssertException).
        HcUtilArgs4j.getProgramOptions (args, options);

        assertEquals (Options.Mode.Index, options.m_mode);
    }
}
