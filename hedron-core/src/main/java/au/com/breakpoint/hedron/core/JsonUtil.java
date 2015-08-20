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

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.kohsuke.args4j.Option;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import au.com.breakpoint.hedron.core.Tuple.E2;
import au.com.breakpoint.hedron.core.args4j.HcUtilArgs4j;
import au.com.breakpoint.hedron.core.context.ExecutionScopes;
import au.com.breakpoint.hedron.core.context.ThreadContext;

public class JsonUtil
{
    private static class Options
    {
        public enum Mode
        {
            JsonValidation
        }

        @Option (name = "-c", aliases =
        {
                "--classname"
        }, usage = "Class name", required = true)
        public String m_className;

        @Option (name = "-i", aliases =
        {
                "--infile"
        }, usage = "Specifies the name of the input file", required = true)
        public String m_inputFilename;

        @Option (name = "-l", aliases =
        {
                "--listJsonOnError"
        }, usage = "Whether to output comment-stripped json on error for line number correlation with the GSON messages")
        public boolean m_listJsonOnError = true;

        @Option (name = "-m", aliases =
        {
                "--mode"
        }, usage = "Program mode; default is JsonValidation")
        public Mode m_mode = Mode.JsonValidation;

        @Option (name = "-p", aliases =
        {
                "--pauseOnError"
        }, usage = "Whether to wait for user input on error")
        public boolean m_pauseOnError;
    }

    public static <T> T fromJson (final String json, final Class<T> classOfT)
    {
        return new Gson ().fromJson (json, classOfT);
    }

    /**
     * Used to create s generic type from JSON.
     *
     * @param json
     *            JSON
     * @param genericType
     *            eg
     *            <code>new TypeToken<TreeMap<String, BigInteger>> () {}.getType ()</code>
     * @return the created object
     */
    public static <T> T fromJson (final String json, final Type genericType)
    {
        final Gson gson = new Gson ();
        final T m = gson.fromJson (json, genericType);

        return m;
    }

    public static <T> T fromJsonFile (final String filename, final Class<T> classOfT,
        final List<E2<String, String>> substitutions)
    {
        final String json = HcUtilFile.readFileRemoveCommentLines (filename, substitutions);
        //System.out.println (json);
        final T t = fromJson (json, classOfT);

        return t;
    }

    /**
     * Used to create s generic type from JSON.
     *
     * @param filename
     *            Path of the JSON file
     * @param genericType
     *            eg
     *            <code>new TypeToken<TreeMap<String, BigInteger>> () {}.getType ();</code>
     * @return the created object
     */
    public static <T> T fromJsonFile (final String filename, final Type genericType,
        final List<E2<String, String>> substitutions)
    {
        final String json = HcUtilFile.readFileRemoveCommentLines (filename, substitutions);
        //System.out.println (json);

        final T t = fromJson (json, genericType);
        return t;
    }

    public static <T> T fromJsonFileCopy (final String filepath, final Class<T> theClass,
        final List<E2<String, String>> substitutions)
    {
        final String filenameLocalCopy =
            HcUtil.formFilepath (HcUtil.getTempDirectoryName (), HcUtil.removeUnsafeFilenameChars (filepath));
        HcUtilFile.copyFile (filepath, filenameLocalCopy);

        return fromJsonFile (filenameLocalCopy, theClass, substitutions);
    }

    public static void listJson (final String filepath)
    {
        try (final Stream<String> lines =
            HcUtilFile.getFileStreamRemoveCommentLines (filepath, HcUtil.getStandardSubstitutions ()))
        {
            int i = 0;
            for (final String line : lines.collect (Collectors.toList ()))
            {
                System.out.printf ("%3d %s%n", i++, line);
            }
        }
        catch (final IOException e)
        {
            // Swallow exception, eg file missing
        }
    }

    public static void main (final String[] args)
    {
        ExecutionScopes.executeProgram ( () ->
        {
            final int exitValue = validateJson (args);

            System.exit (exitValue);
        });
    }

    public static void setShouldFormatGson (final boolean shouldFormatGson)
    {
        m_shouldFormatGson = shouldFormatGson;
    }

    public static String toJson (final Object e)
    {
        final Gson gson = getGsonCreator ();
        final String json = gson.toJson (e);

        return json;
    }

    public static void validateJson (final String className, final String jsonFilename)
    {
        try
        {
            // Throws exception if invalid.
            fromJsonFile (jsonFilename, HcUtil.getClassObject (className), HcUtil.getStandardSubstitutions ());
        }
        catch (final JsonSyntaxException e)
        {
            // not valid
            ThreadContext.assertError (false,
                "The file [%s] is not valid JSON for class[%s]:%n%s%n  (NB: line numbers assume comment lines are first removed from the JSON file.)",
                jsonFilename, className, e.getMessage ());
        }
        catch (final Throwable e)
        {
            // Propagate exception as unchecked fault up to the fault barrier. Use
            // throwRootFault to handle the caught exception according to its type.
            ThreadContext.throwFault (e);
        }
    }

    public static int validateJson (final String[] args)
    {
        final Options options = new Options ();

        // Check args & prepare usage string (in thrown AssertException).
        HcUtilArgs4j.getProgramOptions (args, options);

        int exitValue = 0;
        switch (options.m_mode)
        {
            case JsonValidation:
            {
                try
                {
                    validateJson (options.m_className, options.m_inputFilename);
                }
                catch (final Throwable e)
                {
                    if (options.m_listJsonOnError)
                    {
                        listJson (options.m_inputFilename);
                    }

                    if (options.m_pauseOnError)
                    {
                        try
                        {
                            System.in.read ();// have to hit enter
                        }
                        catch (final IOException e1)
                        {
                        }
                    }
                    else
                    {
                        HcUtil.pause (1000);// hack to allow logging to complete in ANT build
                    }
                    exitValue = 1;
                }
                break;
            }

            default:
            {
                ThreadContext.assertFault (false, "Unsupported value [%s]", options.m_mode);
                break;
            }
        }
        return exitValue;
    }

    /**
     * Use when converting objects to JSON, so formatting policy is respected.
     */
    private static Gson getGsonCreator ()
    {
        return m_shouldFormatGson ? new GsonBuilder ().setPrettyPrinting ().create () : new Gson ();
    }

    private static boolean m_shouldFormatGson = false;
}
