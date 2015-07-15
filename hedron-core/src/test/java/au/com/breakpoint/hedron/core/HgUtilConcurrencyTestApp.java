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

import java.util.List;
import java.util.concurrent.Callable;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.GenericFactory;
import au.com.breakpoint.hedron.core.context.ThreadContext;

public class HgUtilConcurrencyTestApp
{
    public static void main (final String[] args)
    {
        final List<Callable<String>> tasks = GenericFactory.<Callable<String>> newArrayList ( () ->
        {
            HcUtil.pause (1000);
            return "success";
        } , () ->
        {
            HcUtil.pause (400);
            throw new NullPointerException ();
        });

        try
        {
            HcUtil.executeConcurrently (tasks, 2, true);
        }
        catch (final Throwable e)
        {
            ThreadContext.logException (e);
        }

        try
        {
            HcUtil.executeConcurrently (tasks, 2, false);
        }
        catch (final Throwable e)
        {
            ThreadContext.logException (e);
        }
    }
}
