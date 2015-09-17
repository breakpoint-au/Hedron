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
package au.com.breakpoint.hedron.core.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.function.Function;
import org.junit.Test;
import au.com.breakpoint.hedron.core.HcUtil;

public class ThreadContextStressTest
{
    @Test
    public void testMany ()
    {
        if (m_performTestMany)
        {
            final int countTasks = 5_000;
            final int nrThreads = 250;
            final int times = 5_000;
            final String key = "KEY";

            final Function<Integer, String> task = i ->
            {
                for (int j = 0; j < times; ++j)
                {
                    try (IScope scope = new ExecutionScope ())
                    {
                        ThreadContext.putThreadObject (key, key);
                        assertEquals (key, ThreadContext.getThreadObject (key));
                    }
                }

                return "";
            };

            HcUtil.executeConcurrently (countTasks, task, nrThreads, true);
        }

        assertTrue (true);
    }

    @Test
    public void testOne ()
    {
        if (m_performTestOne)
        {
            final int times = 500_000;
            final String key = "KEY";

            for (int j = 0; j < times; ++j)
            {
                try (IScope scope = new ExecutionScope ())
                {
                    ThreadContext.putThreadObject (key, key);
                    assertEquals (key, ThreadContext.getThreadObject (key));
                }
            }
        }

        assertTrue (true);
    }

    private static final boolean m_performTestOne = false;

    private static final boolean m_performTestMany = false;
}
