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

import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.context.ExecutionScopes;
import au.com.breakpoint.hedron.core.log.Logging;

public class ExecutionTestApp
{
    public static void main (final String[] args)
    {
        ExecutionScopes.executeProgram ( () ->
        {
            Logging.logInfoString ("Async stuff kicked off...");

            HcUtil.pause (500);

            // Deliberate exceptions.
            //                ThreadContext.assertFaultNotNull (null);
            //                if (args != null)
            //                {
            //                    throw new NullPointerException ();
            //                }
        });
    }
}
