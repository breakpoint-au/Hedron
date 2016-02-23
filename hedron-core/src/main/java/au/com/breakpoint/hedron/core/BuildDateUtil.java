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

import java.io.PrintWriter;
import org.kohsuke.args4j.Option;
import au.com.breakpoint.hedron.core.args4j.HcUtilArgs4j;
import au.com.breakpoint.hedron.core.context.ExecutionScopes;
import au.com.breakpoint.hedron.core.context.ThreadContext;

public class BuildDateUtil
{
    private static class Options
    {
        public enum Mode
        {
            JavaInfoFile
        }

        @Option (name = "-a", aliases =
        {
                "--appName"
        }, usage = "Specifies the name of the application", required = true)
        public String m_appName;

        @Option (name = "-b", aliases =
        {
                "--buildNumber"
        }, usage = "Specifies an optional build number attribute")
        public String m_buildNumber;

        @Option (name = "-c", aliases =
        {
                "--classname"
        }, usage = "Class name", required = true)
        public String m_className;

        @Option (name = "-i", aliases =
        {
                "--commitIdentifier"
        }, usage = "Specifies an optional commit identifier attribute")
        public String m_commitIdentifier;

        @Option (name = "-e", aliases =
        {
                "--echo"
        }, usage = "Echo contents of generated file to console")
        public boolean m_echo;

        @Option (name = "-t", aliases =
        {
                "--generateTimestamp"
        }, usage = "Specifies whether to create a timestamp attribute")
        public boolean m_generateTimestamp = true;

        @Option (name = "-m", aliases =
        {
                "--mode"
        }, usage = "Program mode; default is JavaInfoFile")
        public Mode m_mode = Mode.JavaInfoFile;

        @Option (name = "-o", aliases =
        {
                "--outfile"
        }, usage = "Specifies the name of the output file")
        public String m_outputFilename;

        @Option (name = "-p", aliases =
        {
                "--package"
        }, usage = "Package name ()")
        public String m_packageName;

        @Option (name = "-r", aliases =
        {
                "--releaseIdentifier"
        }, usage = "Specifies an optional release identifier attribute")

        public String m_releaseIdentifier;
    }

    public static void main (final String[] args)
    {
        ExecutionScopes.executeProgram ( () ->
        {
            final Options options = new Options ();

            // Check args & prepare usage string (in thrown AssertException).
            HcUtilArgs4j.getProgramOptions (args, options);

            switch (options.m_mode)
            {
                case JavaInfoFile:
                {
                    final String filepath = options.m_outputFilename;

                    try (final UserFeedback feedback = new UserFeedback (false))
                    {
                        feedback.showProgress (filepath);

                        final SmartFileWriter sw = new SmartFileWriter (filepath);

                        try (final PrintWriter pw = new PrintWriter (sw))
                        {
                            pw.printf ("package %s;%n", options.m_packageName);
                            pw.printf ("%n");
                            pw.printf ("public class %s%n", options.m_className);
                            pw.printf ("{%n");
                            String infoString = "";
                            if (options.m_appName != null)
                            {
                                infoString += options.m_appName;
                                pw.printf ("    public static String getAppName ()%n");
                                pw.printf ("    {%n");
                                pw.printf ("        return \"%s\";%n", options.m_appName);
                                pw.printf ("    }%n%n");
                            }
                            if (options.m_releaseIdentifier != null)
                            {
                                infoString += " " + options.m_releaseIdentifier;
                                pw.printf ("    public static String getReleaseIdentifier ()%n");
                                pw.printf ("    {%n");
                                pw.printf ("        return \"%s\";%n", options.m_releaseIdentifier);
                                pw.printf ("    }%n%n");
                            }
                            if (options.m_buildNumber != null)
                            {
                                infoString += "#" + options.m_buildNumber;
                                pw.printf ("    public static int getBuildNumber ()%n");
                                pw.printf ("    {%n");
                                pw.printf ("        return %s;%n", options.m_buildNumber);
                                pw.printf ("    }%n%n");
                            }
                            if (options.m_commitIdentifier != null)
                            {
                                infoString += " (" + options.m_commitIdentifier + ")";
                                pw.printf ("    public static String getCommitIdentifier ()%n");
                                pw.printf ("    {%n");
                                pw.printf ("        return \"%s\";%n", options.m_commitIdentifier);
                                pw.printf ("    }%n%n");
                            }
                            if (options.m_generateTimestamp)
                            {
                                final String timestampString = OleDate.formatDateTime (OleDate.getCurrentDATE ());
                                infoString += " (" + timestampString + ")";
                                pw.printf ("    public static String getTimestamp ()%n");
                                pw.printf ("    {%n");
                                pw.printf ("        return \"%s\";%n", timestampString);
                                pw.printf ("    }%n%n");
                            }

                            pw.printf ("    public static String getInfoString ()%n");
                            pw.printf ("    {%n");
                            pw.printf ("        return \"%s\";%n", infoString);
                            pw.printf ("    }%n");
                            pw.printf ("}%n");
                        }

                        if (sw.didUpdate ())
                        {
                            feedback.outputMessage (true, 1, filepath);
                        }

                        if (options.m_echo)
                        {
                            System.out.println (HcUtilFile.readTextFile (filepath));
                        }
                    }
                    break;
                }

                default:
                {
                    ThreadContext.assertFault (false, "Unsupported value [%s]", options.m_mode);
                    break;
                }
            }
        });
    }
}
