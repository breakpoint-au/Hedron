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
import org.junit.Test;
import au.com.breakpoint.hedron.core.context.ExecutionScope;
import au.com.breakpoint.hedron.core.context.IScope;
import au.com.breakpoint.hedron.core.context.ThreadContext;

public class ExecutionScopeTest
{
    @Test
    public void testNestedExecutionScopeContextId ()
    {
        for (int i = 0; i < 4; ++i)
        {
            try (IScope scopeOuter = new ExecutionScope ())
            {
                final String contextIdOuter = ThreadContext.getContextId ();
                System.out.printf ("contextId0   %s%n", contextIdOuter);
                assertEquals (contextIdOuter, ThreadContext.getContextId ());

                for (int j = 0; j < 2; ++j)
                {
                    try (IScope scopeInner = new ExecutionScope ())
                    {
                        final String contextIdInner = ThreadContext.getContextId ();
                        System.out.printf ("contextId01  %s%n", contextIdInner);
                        assertTrue (!contextIdOuter.equals (contextIdInner));

                        for (int k = 0; k < 3; ++k)
                        {
                            try (IScope scopeInnerInner = new ExecutionScope ())
                            {
                                final String contextIdInnerInner = ThreadContext.getContextId ();
                                System.out.printf ("contextId012 %s%n", contextIdInnerInner);
                                assertTrue (!contextIdInner.equals (contextIdInnerInner));
                                assertTrue (!contextIdOuter.equals (contextIdInnerInner));
                            }
                        }

                    }
                }
            }

            // No scope left.
            assertEquals ("{}", ThreadContext.getContextId ());
        }
    }
}
