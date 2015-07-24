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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.BiFunction;
import au.com.breakpoint.hedron.core.log.Level;
import au.com.breakpoint.hedron.core.value.IValue;

public class TimestampLevelFormatter implements BiFunction<Level, IValue<String>, String>
{
    @Override
    public String apply (final Level level, final IValue<String> v)
    {
        final String levelString = Level.getString (level);
        final String timestamp = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss").format (new Date ());

        return timestamp + " " + levelString + " " + v.get ();
    }
}
