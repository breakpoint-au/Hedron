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
package au.com.breakpoint.hedron.core.args4j;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.BooleanOptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

/**
 * Boolean OptionHandler that takes an argment true or false.
 */
public class BistateBooleanOptionHandler extends BooleanOptionHandler
{
    public BistateBooleanOptionHandler (final CmdLineParser parser, final OptionDef od, final Setter<? super Boolean> s)
    {
        super (parser, od, s);
    }

    @Override
    public int parseArguments (final Parameters params) throws CmdLineException
    {
        int argsUsed = 0;

        if (option.isArgument ())
        {
            argsUsed = super.parseArguments (params);
        }
        else
        {
            final String token = params.getParameter (0);
            boolean value = false;
            if (token.equalsIgnoreCase ("Y") || token.equalsIgnoreCase ("Yes"))
            {
                value = true;
            }
            else
            {
                value = Boolean.valueOf (token);
            }

            setter.addValue (value);
            argsUsed = 1;// skip this argument
        }

        return argsUsed;
    }
}
