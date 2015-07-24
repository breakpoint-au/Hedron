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

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.kohsuke.args4j.Option;
import au.com.breakpoint.hedron.core.Tuple.E3;
import au.com.breakpoint.hedron.core.args4j.HcUtilArgs4j;
import au.com.breakpoint.hedron.core.context.ThreadContext;

public class ArchiveUtil
{
    public void smartCopyJarfile ()
    {
        final String srcJar = HcUtil.toSlashPath (m_options.m_sourcePath);
        final String destJar = HcUtil.toSlashPath (m_options.m_destinationPath);

        final boolean shouldCopy = !HcUtilFile.doesFileExist (destJar) || areLocalArchivesDifferent (srcJar, destJar);
        if (shouldCopy)
        {
            HcUtilFile.copyFile (srcJar, destJar);
            m_feedback.outputMessage (true, 0, "Copied [%s] to [%s]", srcJar, destJar);
        }
        else
        {
            m_feedback.outputMessage (true, 0, "[%s] is up to date", destJar);
        }

        System.out.println ();
    }

    private boolean areArchivesDifferent (final List<KeyValuePair<String, BigInteger>> srcResults,
        final List<KeyValuePair<String, BigInteger>> destResults)
    {
        final E3<List<KeyValuePair<String, BigInteger>>, List<KeyValuePair<String, BigInteger>>, List<KeyValuePair<String, BigInteger>>> diffs =
            HcUtil.analyseDifferences (destResults, srcResults);

        final List<KeyValuePair<String, BigInteger>> inserts = diffs.getE0 ();
        final List<KeyValuePair<String, BigInteger>> updates = diffs.getE1 ();
        final List<KeyValuePair<String, BigInteger>> deletes = diffs.getE2 ();

        listChanges (inserts, updates, deletes);

        return isDifferent (inserts, updates, deletes, true);
    }

    private boolean areLocalArchivesDifferent (final String srcJar, final String destJar)
    {
        final List<KeyValuePair<String, BigInteger>> srcResults = getLocalArchiveCrcs (srcJar);
        final List<KeyValuePair<String, BigInteger>> destResults = getLocalArchiveCrcs (destJar);

        return areArchivesDifferent (srcResults, destResults);
    }

    private List<KeyValuePair<String, BigInteger>> getLocalArchiveCrcs (final String archiveFilepath)
    {
        final List<KeyValuePair<String, BigInteger>> result =
            ArchiveUtil.getArchiveCrcs (archiveFilepath, getWorkDirectory ());

        m_feedback.outputMessage (true, 0, "Local-scanned [%s] %s files", archiveFilepath, result.size ());

        return makeRelativePaths (result, archiveFilepath);
    }

    private void listChange (final List<KeyValuePair<String, BigInteger>> inserts, final String title)
    {
        m_feedback.outputMessage (true, 1, "%s: %s %s", title, inserts.size (), summarise (inserts, 5));
    }

    private void listChanges (final List<KeyValuePair<String, BigInteger>> inserts,
        final List<KeyValuePair<String, BigInteger>> updates, final List<KeyValuePair<String, BigInteger>> deletes)
    {
        listChange (inserts, "inserts");
        listChange (updates, "updates");
        listChange (deletes, "deletes");
    }

    private static class Options
    {
        @Option (name = "-dest", usage = "Specifies the destination path for file comparison", required = true)
        public String m_destinationPath;

        @Option (name = "-src", usage = "Specifies the source path for file comparison", required = true)
        public String m_sourcePath;
    }

    /** Exposed for unit testing */
    public static boolean areAllUpdatesIgnorable (final List<KeyValuePair<String, BigInteger>> updates)
    {
        // Treat as not changed if all updates are to files in this collection.
        final String[] ignoreCriteria =
            {
                    "CompilationDate.class",
                    "META-INF/MANIFEST.MF"
        };

        // @formatter:off
        final boolean are = updates.stream ()
            .map (KeyValuePair::getKey) // filepath within archive
            .allMatch (p -> canIgnore (p, ignoreCriteria));
        // @formatter:on

        return are;
    }

    public static boolean canIgnore (final String filepath, final String[] ignoreCriteria)
    {
        // @formatter:off
        final boolean notIgnorable = Arrays.stream (ignoreCriteria)
            .noneMatch (s -> filepath.endsWith (s));
        // @formatter:on

        return !notIgnorable;
    }

    public static List<KeyValuePair<String, BigInteger>> getArchiveCrcs (final String absolutePath,
        final String extractionPath)
    {
        List<KeyValuePair<String, BigInteger>> results = null;
        if (HcUtilFile.doesFileExist (absolutePath))
        {
            try
            {
                // Recursively extract the archive to a temp location.
                recursiveExtractArchive (absolutePath, extractionPath);

                final List<KeyValuePair<String, BigInteger>> resultsFullPath = HcUtilFile.getFileCrcs (extractionPath);

                // Change the file paths to be relative to the original archive path.
                // @formatter:off
                results =
                    resultsFullPath.stream ()
                    .map (e -> KeyValuePair.of (replacePath (e.getKey (), extractionPath, absolutePath), e.getValue ()))
                    .collect (toList ());
                // @formatter:on
            }
            finally
            {
                // Clean up the temp extracted data.
                HcUtilFile.deleteFileOrDirectory (extractionPath);
            }
        }

        return results;
    }

    public static String getFilenameOnly (final String p)
    {
        final int i = p.lastIndexOf ('/');
        return i == -1 ? p : p.substring (i + 1);
    }

    public static String getWorkDirectory ()
    {
        return HcUtil.getTemporarySubdirectory ("c:/temp/worklocal");
    }

    public static boolean isArchive (final String filepath)
    {
        final String lc = filepath.toLowerCase ();
        final boolean isArchive =
            lc.endsWith (".zip") || lc.endsWith (".jar") || lc.endsWith (".war") || lc.endsWith (".ear");

        return isArchive;
    }

    public static boolean isDifferent (final List<KeyValuePair<String, BigInteger>> inserts,
        final List<KeyValuePair<String, BigInteger>> updates, final List<KeyValuePair<String, BigInteger>> deletes,
        final boolean ignoreTimeSensitive)
    {
        boolean is = false;

        if (!ignoreTimeSensitive)
        {
            // Any change counts.
            is = inserts.size () > 0 || updates.size () > 0 || deletes.size () > 0;
        }
        else
        {
            if (inserts.size () > 0 || deletes.size () > 0)
            {
                is = true;
            }
            else
            {
                is = !areAllUpdatesIgnorable (updates);
            }
        }

        return is;
    }

    public static void main (final String[] args)
    {
        //m_feedback.outputMessage (true, 0, "LogTool %s", m_options.m_mode);
        HcUtilArgs4j.getProgramOptions (args, m_options);
        new ArchiveUtil ().smartCopyJarfile ();
    }

    public static KeyValuePair<String, BigInteger> makeRelativePath (final KeyValuePair<String, BigInteger> kv,
        final String basePath)
    {
        return KeyValuePair.of (HcUtilFile.getRelativeFilepath (kv.getKey (), basePath), kv.getValue ());
    }

    public static List<KeyValuePair<String, BigInteger>> makeRelativePaths (
        final List<KeyValuePair<String, BigInteger>> inputList, final String basePath)
    {
        // @formatter:off
        final List<KeyValuePair<String, BigInteger>> resultList =
            inputList.stream ()
            .map (kv -> makeRelativePath (kv, basePath))
            .sorted (m_keyValueAscendingComparator)
            .collect (toList ());
        // @formatter:on

        return resultList;
    }

    public static void recursiveExtractArchive (final String zipFileName, final String extractionPath)
    {
        final File file = new File (zipFileName);

        try (final ZipFile zip = new ZipFile (file))
        {
            //final String outputPath = zipFileName.substring (0, zipFileName.length () - 4);

            final Enumeration<? extends ZipEntry> zipFileEntries = zip.entries ();

            // Process each entry
            while (zipFileEntries.hasMoreElements ())
            {
                // grab a zip file entry
                final ZipEntry entry = zipFileEntries.nextElement ();
                final String currentEntry = entry.getName ();
                //System.out.printf ("currentEntry %s%n", currentEntry);

                final File destFile = new File (extractionPath, currentEntry);

                if (!entry.isDirectory ())
                {
                    try (final BufferedInputStream is = new BufferedInputStream (zip.getInputStream (entry)))
                    {
                        // Handle the entry, eg write the file out or calculate MD5 etc.
                        writeFileFromStream (is, destFile);
                    }
                    catch (final IOException e)
                    {
                        // Propagate exception as unchecked fault up to the fault barrier.
                        ThreadContext.throwFault (e);
                    }
                }

                // Nested archives are also written out as file. Recurse into them and delete afterwards.
                if (isArchive (currentEntry))
                {
                    // Recurse zip file.
                    final String extractedArchivePath = destFile.getAbsolutePath ();

                    final String directory = HcUtil.getDirectory (extractedArchivePath);
                    final String filename = HcUtil.getFilename (extractedArchivePath);
                    final String jarfilename = filename.substring (0, filename.length () - 4);

                    final String nestedExtractionPath = HcUtil.formFilepath (directory, jarfilename);
                    recursiveExtractArchive (extractedArchivePath, nestedExtractionPath);

                    // Remove the nested archive that has been extracted.
                    HcUtilFile.deleteFileOrDirectory (extractedArchivePath);
                }
            }
        }
        catch (final IOException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }
    }

    /** Exposed for unit testing */
    public static <T> String summarise (final List<KeyValuePair<String, T>> list, final int limit)
    {
        String s = "";

        final int count = list.size ();
        if (count > 0)
        {
            // @formatter:off
            s = list.stream ()
                .map (KeyValuePair::getKey)
                .map (p -> getFilenameOnly (p))
                .limit (limit)
                .collect (joining (", "));
            // @formatter:on

            s = count > limit ? String.format ("(%s ... %s more)", s, count - limit) : String.format ("(%s)", s);
        }

        return s;
    }

    //    /**
    //     * Gets the CRCs for all files in a directory and its subdirectories. Where a file is
    //     * an archive, recursively extracts and calculates CRCs for the files in the archive.
    //     *
    //     * @param basePath
    //     *            the directory
    //     * @param extractionPath
    //     *            for temporary workspace when drilling into archives
    //     * @return a list of key value pairs, one per file in the directory and its
    //     *         subdirectories. The key is the absolute filename, the value is a list of
    //     *         the file's CRCs. For a simple file, the list contains one entry, one per
    //     *         file. For an archive, it contains a key value pair per file in the archive.
    //     *         This nested KVP has key of absolute filename of the file inside the
    //     *         archive, and value of its CRC.
    //     */
    //    public static List<KeyValuePair<String, List<KeyValuePair<String, BigInteger>>>> getFileOrArchiveCrcs (
    //        final String basePath, final String extractionPath)
    //    {
    //        final List<KeyValuePair<String, List<KeyValuePair<String, BigInteger>>>> results =
    //            GenericFactory.newArrayList ();
    //
    //        final DirectoryTree dtSource = new DirectoryTree (basePath, null, null);
    //
    //        final List<String> filepaths = dtSource.getAbsolutePaths ();
    //        for (final String filepath : filepaths)
    //        {
    //            KeyValuePair<String, List<KeyValuePair<String, BigInteger>>> entryForFile = null;
    //
    //            if (isArchive (filepath))
    //            {
    //                // Archive. Recurse into it.
    //                final List<KeyValuePair<String, BigInteger>> entriesForArchive =
    //                    getArchiveCrcs (filepath, extractionPath);
    //
    //                entryForFile = KeyValuePair.newInstance (filepath, entriesForArchive);
    //            }
    //            else
    //            {
    //                // Simple file.
    //                final BigInteger digest = HcUtilFile.calculateFileMd5Checksum (filepath);
    //
    //                // Store a KVP with key is the absolute filename, the value is a list of
    //                // the file's CRCs. For a simple file, the list contains one entry for the file.
    //                final KeyValuePair<String, BigInteger> singleEntry = KeyValuePair.newInstance ("", digest);
    //                final ArrayList<KeyValuePair<String, BigInteger>> entriesForSimpleFile =
    //                    GenericFactory.newArrayList (singleEntry);
    //
    //                entryForFile = KeyValuePair.newInstance (filepath, entriesForSimpleFile);
    //            }
    //
    //            results.add (entryForFile);
    //        }
    //
    //        return results;
    //    }

    private static String replacePath (final String path, final String fromPath, final String toPath)
    {
        ThreadContext.assertFault (path.startsWith (fromPath), "Logic error [%s]", path);
        final String rebased = HcUtil.formFilepath (toPath, path.substring (fromPath.length () + 1));

        return rebased;
    }

    private static boolean writeFileFromStream (final InputStream is, final File destFile)
    {
        boolean ok = false;

        final File destinationParent = destFile.getParentFile ();

        // create the parent directory structure if needed
        destinationParent.mkdirs ();

        try
        {
            // write the current file to disk
            final byte data[] = new byte[2048];
            final FileOutputStream fos = new FileOutputStream (destFile);

            try (final BufferedOutputStream dest = new BufferedOutputStream (fos, data.length))
            {
                // read and write until last byte is encountered
                int currentByte;
                while ((currentByte = is.read (data, 0, data.length)) != -1)
                {
                    dest.write (data, 0, currentByte);
                }

                dest.flush ();
                ok = true;
            }
            catch (final IOException e)
            {
                // Propagate exception as unchecked fault up to the fault barrier.
                ThreadContext.throwFault (e);
            }
        }
        catch (final FileNotFoundException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }

        return ok;
    }

    public final static Comparator<KeyValuePair<String, ?>> m_keyValueAscendingComparator =
        Comparator.comparing (KeyValuePair::getKey);

    private final static UserFeedback m_feedback = new UserFeedback (false);

    private final static Options m_options = new Options ();
}
