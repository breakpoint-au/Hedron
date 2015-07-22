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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionHandlerFilter;
import org.kohsuke.args4j.OptionHandlerRegistry;
import org.kohsuke.args4j.ParserProperties;
import au.com.breakpoint.hedron.core.context.ThreadContext;

public class HcUtilArgs4j
{
    public static void getProgramOptions (final String[] args, final Object options)
    {
        final CmdLineParser parser = getCmdLineParser (options);
        try
        {
            parser.parseArgument (args);
        }
        catch (final CmdLineException e)
        {
            // Problem in the command line. Create an error message.
            final String usageMessage = getUsageMessage (parser, e.getMessage ());
            ThreadContext.assertError (false, usageMessage);
        }
    }

    public static void getProgramOptionsProgrammatic (final String[] args, final Object options,
        final boolean ignoreUnknown)
    {
        if (!ignoreUnknown)
        {
            // Parse all at once. An unknown option will create a fault exception.
            final CmdLineParser parser = getCmdLineParser (options);
            try
            {
                parser.parseArgument (args);
            }
            catch (final CmdLineException e)
            {
                ThreadContext.assertFault (false, e.getMessage ());
            }
        }
        else
        {
            // Parse each option individually in case an unknown option generates an exception.
            for (int i = 0; i < args.length;)
            {
                final String optionName = args[i++];
                final String optionValue = args[i++];
                parseIgnoreException (optionName, optionValue, options);
            }
        }
    }

    public static String getUsageMessage (final CmdLineParser parser, final String message)
    {
        final Writer sw = new StringWriter ();
        final PrintWriter pw = new PrintWriter (sw);
        parser.printUsage (pw, null);
        final String usageString = sw.toString ();

        final String printExample = parser.printExample (OptionHandlerFilter.REQUIRED);// ALL
        final String programName = "[program]";
        final String s = String.format ("%s%n%nUsage: java %s [options...] arguments%n%s%nExample:%n  java %s%s%n",
            message, programName, usageString, programName, printExample);

        return s;
    }

    /**
     * Call this before getProgramOptions () to enforce explicit boolean arguments, eg
     * -update true etc. Otherwise, boolean options work via -update meaning true, and
     * omitting the arg for false.
     */
    public static void setExplicitBooleanOptionStyle ()
    {
        // Custom handlers added here.
        registerHandler (Boolean.class, BistateBooleanOptionHandler.class);
        registerHandler (boolean.class, BistateBooleanOptionHandler.class);
    }

    private static CmdLineParser getCmdLineParser (final Object options)
    {
        return new CmdLineParser (options, getParserProperties ());
    }

    private static ParserProperties getParserProperties ()
    {
        return ParserProperties.defaults ().withUsageWidth (120);
    }

    private static void parseIgnoreException (final String optionName, final String optionValue, final Object options)
    {
        final CmdLineParser parser = new CmdLineParser (options);
        try
        {
            parser.parseArgument (optionName, optionValue);
        }
        catch (final CmdLineException e)
        {
            // Ignore.
        }
    }

    private static void registerHandler (final Class<Boolean> valueType,
        final Class<BistateBooleanOptionHandler> handlerClass)
    {
        OptionHandlerRegistry.getRegistry ().registerHandler (valueType, handlerClass);
    }
}
