package au.com.breakpoint.hedron.core;

import java.io.FileWriter;
import java.io.IOException;
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
            BuildDate
        }

        @Option (name = "-classname", usage = "Class name", required = true)
        public String m_className;

        @Option (name = "-echo", usage = "Echo contents of generated file to console")
        public boolean m_echo;

        @Option (name = "-mode", usage = "Program mode; default is BuildDate")
        public Mode m_mode = Mode.BuildDate;

        @Option (name = "-outfile", usage = "Specifies the name of the output file ** MANDATORY for Summary **")
        public String m_outputFilename;

        @Option (name = "-package", usage = "Package name ()")
        public String m_packageName;
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
                case BuildDate:
                {
                    try (final PrintWriter pw = new PrintWriter (new FileWriter (options.m_outputFilename)))
                    {
                        pw.printf ("package %s;%n", options.m_packageName);
                        pw.printf ("%n");
                        pw.printf ("public class %s%n", options.m_className);
                        pw.printf ("{%n");
                        pw.printf ("    public static String getInfoString ()%n");
                        pw.printf ("    {%n");
                        pw.printf ("        return INFO_STRING;%n");
                        pw.printf ("    }%n%n");
                        pw.printf ("    private static final String INFO_STRING = \"%s\";%n",
                            OleDate.formatDateTime (OleDate.getCurrentDATE ()));
                        pw.printf ("}%n");
                    }
                    catch (final IOException e)
                    {
                        // Propagate exception as unchecked fault up to the fault barrier.
                        ThreadContext.throwFault (e);
                    }

                    if (options.m_echo)
                    {
                        System.out.println (HcUtilFile.readTextFile (options.m_outputFilename));
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
