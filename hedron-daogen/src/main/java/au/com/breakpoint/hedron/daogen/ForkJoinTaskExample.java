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
package au.com.breakpoint.hedron.daogen;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import au.com.breakpoint.hedron.core.HcUtil;

public class ForkJoinTaskExample
{
    public static void main (final String[] args)
    {
        final Function<Double, String> work = input -> input.toString ();

        // Calculate four inputs
        final List<Double> inputs = Arrays.asList (1.0, 2.0, 3.0, 4.0, 5.0, 6.0);

        final List<String> result = HcUtil.executeConcurrently (inputs, work);
        System.out.println (result);
    }
}
