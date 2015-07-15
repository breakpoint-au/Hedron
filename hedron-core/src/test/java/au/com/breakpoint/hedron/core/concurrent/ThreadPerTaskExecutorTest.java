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
package au.com.breakpoint.hedron.core.concurrent;

import org.junit.Test;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.concurrent.ThreadPerTaskExecutor;

public class ThreadPerTaskExecutorTest
{
    @Test
    public void testThreadCleanup ()
    {
        // Prove via task manager that threads clean up.
        if (m_performTest)
        {
            for (int i = 0; i < 1000; ++i)
            {
                new ThreadPerTaskExecutor ().execute ( () ->
                {
                });

                HcUtil.pause (1);
            }
        }
    }

    private static final boolean m_performTest = false;
}