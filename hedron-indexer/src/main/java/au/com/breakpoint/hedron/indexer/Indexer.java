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
package au.com.breakpoint.hedron.indexer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import com.google.gson.reflect.TypeToken;
import au.com.breakpoint.hedron.core.GenericFactory;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.HcUtilFile;
import au.com.breakpoint.hedron.core.HcUtilHtml;
import au.com.breakpoint.hedron.core.JsonUtil;
import au.com.breakpoint.hedron.core.ListMap;
import au.com.breakpoint.hedron.core.SmartFile;
import au.com.breakpoint.hedron.core.UserFeedback;
import au.com.breakpoint.hedron.core.args4j.HcUtilArgs4j;
import au.com.breakpoint.hedron.core.context.ExecutionScopes;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.indexer.AppConfiguration.SourcePath;

// TODO 0 remove obsolete files from the index output dir

public class Indexer
{
    private void analyseAndGenerateIndexFiles ()
    {
        HcUtilFile.ensureDirectoryExists (m_appConfig.getDestination ());

        final IndexInfo index = new IndexInfo ();

        // Get the collection of keywords to index on.
        getKeywords (index);

        for (final SourcePath sp : m_appConfig.getSourcePaths ())
        {
            analyseAndGenerateIndexFiles (sp, index);
        }
    }

    private void analyseAndGenerateIndexFiles (final SourcePath sp, final IndexInfo index)
    {
        m_feedback.outputMessage (true, 0, "%nIndexing %s", sp.getPath ());

        final File dir = HcUtilFile.getDirectory (sp.getPath ());
        final List<File> sourceFiles =
            HcUtilFile.getFileObjects (dir, false, sp.getIncludeFiles (), sp.getExcludeFiles ());

        // Gather index info.
        for (final File f : sourceFiles)
        {
            final String filepath = HcUtil.toSlashPath (f.getAbsolutePath ());
            //System.out.printf ("fp %s%n", filepath);

            indexFile (index, filepath);
            m_feedback.showProgress (m_options.m_debug ? filepath : null);
        }

        // Output index info.
        final String destination = m_appConfig.getDestination ();
        m_feedback.outputMessage (true, 0, "%nGenerating %s -> %s", sp.getPath (), destination);
        generateIndexFiles (index);

        m_feedback.outputMessage (true, 1, "%s source files", sourceFiles.size ());
    }

    private void generateFileHtml (final String sourceFilepath)
    {
        final String outputFilepath = getOutputFilepath (sourceFilepath);

        SmartFile pw = null;
        try
        {
            pw = new SmartFile (outputFilepath);

            pw.printf ("<html>%n");
            pw.printf ("<head>%n");
            pw.printf ("<link type='text/css' rel='stylesheet' href='css/keyword.css'>%n");
            pw.printf ("<script type='text/javascript' src='jscript/jquery-1.6.2.js'></script>%n");
            pw.printf ("<script type='text/javascript' src='jscript/jquery.highlight-4.js'></script>%n");
            pw.printf ("<script type='text/javascript' src='jscript/keyword.js'></script>%n");
            pw.printf ("<script>%n");
            pw.printf ("$ (function ()%n");
            pw.printf ("{%n");
            pw.printf ("var p = getParameterByName ('kw');%n");
            pw.printf ("if (p != null)%n");
            pw.printf ("{%n");
            pw.printf ("$ ('pre').highlight (p);%n");
            pw.printf ("}%n");
            pw.printf ("});%n");
            pw.printf ("</script>%n");
            pw.printf ("</head>%n");
            pw.printf ("<body>%n");

            try (final Stream<String> lines = Files.lines (Paths.get (sourceFilepath)))
            {
                final AtomicInteger lineNumber = new AtomicInteger (0);
                final SmartFile pwf = pw;
                lines.forEach (line -> outputLineNumberHyperlink (pwf, line, lineNumber.incrementAndGet ()));
            }
            catch (final IOException e)
            {
                // Propagate exception as unchecked fault up to the fault barrier.
                ThreadContext.throwFault (e);
            }

            //            try (final ReadLineIterable rli = new FileReadLineIterable (sourceFilepath))
            //            {
            //                int lineNumber = 0;
            //                for (final String line : rli)
            //                {
            //                    ++lineNumber;
            //                    pw.printf ("<a id='%s'><pre>%5d: %s</pre></a>%n", lineNumber, lineNumber,
            //                        HcUtilHtml.htmlEscape (line));
            //                }
            //            }

            pw.printf ("</body>%n");
            pw.printf ("</html>%n");
        }
        finally
        {
            final boolean updated = HcUtilFile.safeClose (pw);
            m_feedback.showProgress (updated ? outputFilepath : null);
        }
    }

    private void generateIndexFiles (final IndexInfo index)
    {
        // Generate keyword table.
        final String tableFilepath = HcUtil.formFilepath (m_appConfig.getDestination (), "keywords.generated.html");
        SmartFile pw = null;
        try
        {
            pw = new SmartFile (tableFilepath);

            final Map<String, KeywordReference> mapKeywords = index.getKeywords ();
            final Map<String, String> handledFilepaths = GenericFactory.newHashMap ();

            pw.printf ("<html>%n");
            pw.printf ("<head>%n");
            pw.printf ("<link type='text/css' rel='stylesheet' href='css/keyword.css'>%n");
            pw.printf ("</head>%n");
            pw.printf ("<body>%n");
            pw.printf ("<table class='boxy refs'>%n");
            pw.printf ("<thead>%n");
            pw.printf ("<tr>%n");
            pw.printf ("<th>Type</td>%n");
            pw.printf ("<th>Keyword</td>%n");
            pw.printf ("<th>Filename</td>%n");
            pw.printf ("<th>Line numbers</td>%n");
            pw.printf ("</tr>%n");
            pw.printf ("</thead>%n");

            // TODO _ use thead/th
            for (final Entry<String, String[]> ks : index.getKeywordSets ().entrySet ())
            {
                // Keyword set.
                // TODO _ separator row
                final String keywordType = ks.getKey ();
                m_feedback.outputDebugMessage (true, 0, "'%s':", keywordType);

                for (final String keyword : ks.getValue ())
                {
                    // Keyword.
                    m_feedback.outputDebugMessage (true, 1, "'%s':", keyword);

                    final KeywordReference kr = mapKeywords.get (keyword);
                    final ListMap<String, Integer> refListmap = kr.getFileLineReferences ();

                    final Map<String, List<Integer>> refMap = refListmap.getMap ();
                    final Set<Entry<String, List<Integer>>> refMapEntrySet = refMap.entrySet ();
                    final int countFiles = refMapEntrySet.size ();

                    int i = 0;
                    for (final Entry<String, List<Integer>> fileAndLines : refMapEntrySet)
                    {
                        // File.
                        final String filepath = fileAndLines.getKey ();
                        final String filename = new File (filepath).getName ();
                        m_feedback.outputDebugMessage (true, 2, "'%s':", filepath);

                        pw.printf ("<tr class='vtop'>%n");
                        if (i++ == 0)
                        {
                            pw.printf ("<td rowspan='%s'>%s</td>%n", countFiles, keywordType);
                            pw.printf ("<td rowspan='%s'><b>%s</b></td>%n", countFiles, keyword);
                        }

                        pw.printf ("<td>%s</td>%n", filename);
                        pw.printf ("<td>%n");

                        final List<Integer> lineNumbers = fileAndLines.getValue ();
                        for (final Integer lineNumber : lineNumbers)
                        {
                            // Line number.
                            m_feedback.outputDebugMessage (false, 0, " %s", lineNumber);

                            final String outputFilepath = getOutputFilepath (filepath);
                            pw.printf ("<a href='file:///%s?kw=%s#%s'>%s</a>%n", outputFilepath, keyword, lineNumber,
                                lineNumber);
                        }
                        pw.printf ("</td>%n");
                        pw.printf ("</tr>%n");

                        final String storedFilepath = handledFilepaths.get (filename);
                        if (storedFilepath == null)
                        {
                            // First reference to this file. Output the HTML for it.
                            generateFileHtml (filepath);

                            // Store the original filepath associated with the filename so can detect clash.
                            handledFilepaths.put (filename, filepath);
                        }
                        else
                        {
                            ThreadContext.assertFault (storedFilepath.equals (filepath),
                                "Two files have the same name [%s] and [%s]", storedFilepath, filepath);
                        }
                    }
                }
            }

            pw.printf ("</table>%n");
            pw.printf ("</body>%n");
            pw.printf ("</html>%n");
        }
        finally
        {
            final boolean updated = HcUtilFile.safeClose (pw);
            m_feedback.showProgress (updated ? tableFilepath : null);
        }
    }

    private void getKeywords (final IndexInfo index)
    {
        final Type genericType = new TypeToken<TreeMap<String, String[]>> ()
        {
        }.getType ();

        final Map<String, String[]> keywordSets =
            JsonUtil.fromJsonFile (m_appConfig.getKeywordFilename (), genericType, null);

        index.setKeywordSets (keywordSets);

        for (final Entry<String, String[]> ks : keywordSets.entrySet ())
        {
            for (final String keyword : ks.getValue ())
            {
                final KeywordReference kr = new KeywordReference ();
                index.m_keywords.put (keyword, kr);
            }
        }
    }

    private String getOutputFilepath (final String sourceFilepath)
    {
        final String filename = new File (sourceFilepath).getName ();
        return HcUtil.formFilepath (m_appConfig.getDestination (), filename + ".html");
    }

    private void indexFile (final IndexInfo index, final String filepath)
    {
        try (final Stream<String> lines = Files.lines (Paths.get (filepath)))
        {
            final AtomicInteger lineNumber = new AtomicInteger (0);
            lines.forEach (line -> indexLine (index, filepath, lineNumber.incrementAndGet (), line));
        }
        catch (final IOException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }

        //        try (final ReadLineIterable rli = new FileReadLineIterable (filepath))
        //        {
        //            int lineNumber = 0;
        //            for (final String line : rli)
        //            {
        //                ++lineNumber;
        //                indexLine (index, filepath, lineNumber, line);
        //            }
        //        }
    }

    private void indexLine (final IndexInfo index, final String filepath, final int lineNumber, final String line)
    {
        final String[] tokens = line.split (DELIMITERS);
        //m_feedback.outputDebugMessage (true, 0, "[%s]", line);
        //m_feedback.outputDebugMessage (true, 1, "tokens %s", Arrays.toString (tokens));

        final Map<String, KeywordReference> allKeywords = index.getKeywords ();
        for (final String t : tokens)
        {
            final KeywordReference kr = allKeywords.get (t);

            if (kr != null)
            {
                final ListMap<String, Integer> refs = kr.getFileLineReferences ();
                refs.put (filepath, lineNumber);
            }
        }
    }

    private void outputLineNumberHyperlink (final SmartFile pw, final String line, final int lineNumber)
    {
        pw.printf ("<a id='%s'><pre>%5d: %s</pre></a>%n", lineNumber, lineNumber, HcUtilHtml.htmlEscape (line));
    }

    private void run (final String[] args)
    {
        // Check args & prepare usage string (in thrown AssertException).
        HcUtilArgs4j.getProgramOptions (args, m_options);
        m_feedback.outputMessage (true, 0, "%s [%s]", BUILD_STRING, m_options.m_configFilename);

        // Set up logging.
        //            HcUtilLog4j.startLogging (m_options.m_log4jConfigFile, MSEC_LOG4J_REFRESH_CHECK, true);

        setConfigFile (m_options.m_configFilename);

        switch (m_options.m_mode)
        {
            case Index:
            {
                analyseAndGenerateIndexFiles ();
                break;
            }

            default:
            {
                ThreadContext.assertFault (false, "Unsupported value [%s]", m_options.m_mode);
                break;
            }
        }

        m_feedback.outputDebugMessage (true, 0, "%n%n%s%n", HcUtil.getSummaryData (true));
        m_feedback.outputMessage (true, 0, "%nDone.%n");
    }

    private void setConfigFile (final String configFilename)
    {
        m_appConfig = AppConfiguration.readJsonFile (configFilename);
    }

    public static class IndexInfo
    {
        public Map<String, KeywordReference> getKeywords ()
        {
            return m_keywords;
        }

        public Map<String, String[]> getKeywordSets ()
        {
            return m_keywordSets;
        }

        public void setKeywordSets (final Map<String, String[]> keywordSets)
        {
            m_keywordSets = keywordSets;
        }

        private final Map<String, KeywordReference> m_keywords = GenericFactory.newTreeMap ();

        private Map<String, String[]> m_keywordSets;
    }

    public static class KeywordReference
    {
        public ListMap<String, Integer> getFileLineReferences ()
        {
            return m_fileLineReferences;
        }

        private final ListMap<String, Integer> m_fileLineReferences = ListMap.of ();
    }

    public static void main (final String[] args)
    {
        ExecutionScopes.executeProgram ( () -> new Indexer ().run (args));
    }

    private AppConfiguration m_appConfig;

    private final Options m_options = new Options ();

    private static final String BUILD_STRING = Indexer.class.getSimpleName () + " (2012-12-03 14:45)";

    private static final String DELIMITERS = "[ .,?!\t\\[\\]\\(\\)\"\'%/\\\\;:=\\-@^\\&\\*\\+\\|{}]+";

    private final static UserFeedback m_feedback = new UserFeedback (false);
}
