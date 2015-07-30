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

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.stream.Collectors.joining;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import au.com.breakpoint.hedron.core.Tuple.E2;
import au.com.breakpoint.hedron.core.Tuple.E3;
import au.com.breakpoint.hedron.core.context.ThreadContext;

public class HcUtilFile
{
    public static void appendStringToFile (final String filepath, final String formatString, final Object... args)
    {
        final boolean shouldFormat = args != null && !(args.length == 1 && args[0] == null);// avoid null being passed as args array or as only arg
        final String s = shouldFormat ? String.format (formatString, args) : formatString;

        appendToFile (filepath, s);
    }

    public static void appendToFile (final String filepath, final String s)
    {
        // Convert the string to a byte array.
        final byte data[] = s.getBytes ();

        writeFile (filepath, data, CREATE, APPEND);
    }

    /**
     * From some sample on the web somewhere. Hence not coded to company conventions.
     *
     * @param file1Path
     * @param file2Path
     * @return
     */
    public static boolean areFilesIdentical (final String file1Path, final String file2Path)
    {
        final File file1 = new File (file1Path);
        final File file2 = new File (file2Path);

        if (!file1.exists () || !file1.exists () || file1.length () != file2.length ())
        {
            return false;
        }

        FileInputStream s1 = null;
        FileInputStream s2 = null;
        try
        {
            s1 = new FileInputStream (file1);
            s2 = new FileInputStream (file2);
        }
        catch (final FileNotFoundException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }

        try
        {
            // Gradually ramp up buffer size to reduce redundant reads for files that
            // are different in the first n bytes.
            final int maxFileBufferSize = 50_000;
            int bufferSize = 1_000;

            final byte[] buffer1 = new byte[bufferSize / 2];
            final byte[] buffer2 = new byte[buffer1.length];

            for (int count1 = 0; (count1 = s1.read (buffer1)) > 0;)
            {
                int bytesRead = 0;
                for (int count2 = 0; (count2 = s2.read (buffer2, bytesRead, count1 - bytesRead)) > 0;)
                {
                    bytesRead += count2;
                }

                for (int byteIndex = 0; byteIndex < count1; ++byteIndex)
                {
                    if (buffer1[byteIndex] != buffer2[byteIndex])
                    {
                        return false;
                    }
                }

                if (bufferSize <= maxFileBufferSize / 2)
                {
                    bufferSize *= 2;
                }
            }
        }
        catch (final IOException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }
        finally
        {
            safeClose (s1);
            safeClose (s2);
        }
        return true;
    }

    public static boolean areFilesSameSize (final String file1Path, final String file2Path)
    {
        final File file1 = new File (file1Path);
        final File file2 = new File (file2Path);

        return file1.exists () && file1.exists () && file1.length () == file2.length ();
    }

    public static byte[] asciiHexStringToByteArray (final String s)
    {
        ThreadContext.assertFault (s.length () % 2 == 0, "ASCII hex string [%s] is invalid", s);
        final byte[] bytes = new byte[s.length () / 2];

        for (int i = 0, j = 0; i < bytes.length; ++i)
        {
            final byte upper = HcUtil.hexToNibble (s.charAt (j++));
            final byte lower = HcUtil.hexToNibble (s.charAt (j++));

            bytes[i] = (byte) ((upper << 4) | lower);
        }

        return bytes;
    }

    public static String byteArrayToAsciiHexString (final byte[] bytes)
    {
        final StringBuilder sb = new StringBuilder ();

        for (final byte b : bytes)
        {
            if (true)
            {
                final char c = HcUtil.nibbleToHex ((byte) (b >> 4));
                sb.append (c);
            }
            if (true)
            {
                final char c = HcUtil.nibbleToHex ((byte) (b & 0x0F));
                sb.append (c);
            }
        }

        return sb.toString ();
    }

    public static BigInteger calculateFileMd5Checksum (final String filepath)
    {
        BigInteger checksum = null;

        // Read stream to EOF, passing through the digest accumulator.
        try
        {
            final File file = new File (filepath);
            final FileInputStream fileStream = new FileInputStream (file);

            checksum = calculateFileStreamMd5Checksum (fileStream);
        }
        catch (final FileNotFoundException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }
        finally
        {
        }

        ThreadContext.assertFaultNotNull (checksum);

        return checksum;
    }

    public static BigInteger calculateFileStreamMd5Checksum (final FileInputStream fileStream)
    {
        BigInteger checksum = null;

        try
        {
            final MessageDigest md = MessageDigest.getInstance ("MD5");
            try (final InputStream is = new DigestInputStream (fileStream, md))
            {
                final byte[] buffer = new byte[100_000];
                while (is.read (buffer) != -1)
                {
                }
            }
            catch (final IOException e)
            {
                // Propagate exception as unchecked fault up to the fault barrier.
                ThreadContext.throwFault (e);
            }

            final byte[] digest = md.digest ();

            checksum = new BigInteger (1, digest);
        }
        catch (final NoSuchAlgorithmException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }

        return checksum;
    }

    public static E3<List<String>, List<String>, List<String>> compareSubdirectories (final String sourceBaseDirectory,
        final String destBaseDirectory, final String[] sourcePathsToIgnore, final String[] destPathsToIgnore,
        final String[] excludeFilenames)
    {
        final E2<List<String>, List<String>> fileLists = recurseSubdirectories (sourceBaseDirectory, destBaseDirectory,
            sourcePathsToIgnore, destPathsToIgnore, excludeFilenames);

        return HcUtil.getDifferences (fileLists.getE0 (), fileLists.getE1 ());
    }

    public static void copyFile (final String from, final String to)
    {
        final Path fromPath = getPathObject (from);
        final Path toPath = getPathObject (to);
        ensureDirectoryExists (toPath.getParent ().toString ());

        try
        {
            // Not using REPLACE_EXISTING now since using COPY_ATTRIBUTES to preserve date/time.
            Files.deleteIfExists (toPath);

            Files.copy (fromPath, toPath, StandardCopyOption.COPY_ATTRIBUTES);// REPLACE_EXISTING
        }
        catch (final IOException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }
    }

    public static boolean copyFileIfDifferent (final String tempFilePath, final String targetFilePath)
    {
        boolean updated = false;

        final boolean identical = areFilesIdentical (targetFilePath, tempFilePath);
        if (!identical)
        {
            copyFile (tempFilePath, targetFilePath);
            updated = true;
        }

        return updated;
    }

    public static boolean copyFileIfDifferentSize (final String tempFilePath, final String targetFilePath)
    {
        boolean updated = false;

        final boolean sameSize = areFilesSameSize (targetFilePath, tempFilePath);
        if (!sameSize)
        {
            copyFile (tempFilePath, targetFilePath);
            updated = true;
        }

        return updated;
    }

    public static List<String> deleteEmptyDirectories (final String directoryPath,
        final boolean includeTopLevelDirectory)
    {
        final File dir = getDirectory (directoryPath);

        final List<String> deletedDirectories = GenericFactory.newArrayList ();
        deleteEmptyDirectories (deletedDirectories, dir, includeTopLevelDirectory, 0);

        return deletedDirectories;
    }

    public static void deleteFileOrDirectory (final File file)
    {
        if (file.isDirectory ())
        {
            // Delete the contents of the directory first.
            final String[] children = file.list ();
            for (final String element : children)
            {
                deleteFileOrDirectory (new File (file, element));
            }
        }

        final boolean success = file.delete ();
        ThreadContext.assertFault (success, "Unable to delete [%s]", file.getAbsolutePath ());
    }

    public static void deleteFileOrDirectory (final String filepath)
    {
        deleteFileOrDirectory (new File (filepath));
    }

    public static <T extends Serializable> T deserialiseAsciiHexStringAsObject (final String string)
    {
        final byte[] bytes = asciiHexStringToByteArray (string);
        return deserialiseBytesAsObject (bytes);
    }

    ///**
    // * Optimised implementation based on Sun info here:
    // *
    // * <pre>
    // * http://java.sun.com/docs/books/performance/1st_edition/html/JPIOPerformance.fm.html
    // * </pre>
    // *
    // * @param from
    // * @param to
    // */
    //public static void copyFilePreJdk7 (final String from, final String to)
    //{
    //    boolean copyStarted = false;
    //    boolean copyCompleted = false;
    //
    //    final byte[] buffer = new byte[FILE_BUFFER_SIZE];
    //
    //    InputStream in = null;
    //    OutputStream out = null;
    //    try
    //    {
    //        in = new FileInputStream (from);
    //        out = new FileOutputStream (to);
    //
    //        while (!copyCompleted)
    //        {
    //            final int amountRead = in.read (buffer);
    //            if (amountRead == -1)
    //            {
    //                copyCompleted = true;
    //            }
    //            else
    //            {
    //                copyStarted = true;
    //                out.write (buffer, 0, amountRead);
    //            }
    //        }
    //    }
    //    catch (final FileNotFoundException e)
    //    {
    //        // Propagate exception as unchecked fault up to the fault barrier.
    //        ThreadContext.throwFault (e);
    //    }
    //    catch (final IOException e)
    //    {
    //        // Propagate exception as unchecked fault up to the fault barrier.
    //        ThreadContext.throwFault (e);
    //    }
    //    finally
    //    {
    //        safeClose (in);
    //        safeClose (out);
    //
    //        if (copyStarted && !copyCompleted)
    //        {
    //            safeDeleteFileNoThrow (to);
    //        }
    //    }
    //}

    public static <T extends Serializable> T deserialiseBase64StringAsObject (final String string)
    {
        final byte[] bytes = Base64.getDecoder ().decode (string);
        return deserialiseBytesAsObject (bytes);
    }

    public static <T extends Serializable> T deserialiseBytesAsObject (final byte[] b)
    {
        T object = null;

        try (final ObjectInputStream s = new ObjectInputStream (new ByteArrayInputStream (b)))
        {
            object = HcUtil.uncheckedCast (s.readObject ());
        }
        catch (final IOException | ClassNotFoundException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }

        return object;
    }

    public static boolean doesDirectoryExist (final String directoryPath)
    {
        final File f = new File (directoryPath);
        return f.exists () && f.isDirectory () && f.canRead ();
    }

    public static boolean doesFileExist (final String path)
    {
        final File f = new File (path);
        return f.exists () && f.canRead ();
    }

    public static boolean doesFileOrDirectoryExist (final String path)
    {
        final File f = new File (path);
        return f.exists ();
    }

    public static void ensureDirectoryExists (final String dir)
    {
        ensureDirectoryExists (dir, false);// all users
    }

    public static void ensureDirectoryExists (final String dir, final boolean ownerOnly)
    {
        final File dirFile = new File (dir);
        if (!dirFile.exists ())
        {
            final boolean ok = dirFile.mkdirs ();
            dirFile.setWritable (true, ownerOnly);
            ThreadContext.assertWarning (ok, "Error creating [%s]", dir);
        }
    }

    public static String getAbsolutePath (final File f)
    {
        return HcUtil.toSlashPath (f.getAbsolutePath ());
    }

    public static String getCurrentDirectory ()
    {
        return HcUtil.toSlashPath (System.getProperty ("user.dir"));
    }

    public static String getCurrentDrive ()
    {
        return getRootName (HcUtil.getCurrentDirectory ());
    }

    public static File getDirectory (final String directoryPath)
    {
        final File dir = new File (directoryPath);
        ThreadContext.assertError (dir.exists (), "Non-existent directory [%s]", directoryPath);
        ThreadContext.assertError (dir.isDirectory (), "Non-directory [%s]", directoryPath);
        ThreadContext.assertError (dir.canRead (), "Non-readable directory [%s]", directoryPath);

        return dir;
    }

    public static BigInteger getFileCrc (final String filepath)
    {
        return doesFileExist (filepath) ? calculateFileMd5Checksum (filepath) : null;
    }

    public static List<KeyValuePair<String, BigInteger>> getFileCrcs (final String basePath)
    {
        return getFileCrcs (basePath, null, null);
    }

    public static List<KeyValuePair<String, BigInteger>> getFileCrcs (final String basePath,
        final String[] pathsToIgnore, final String[] excludeFilenames)
    {
        final List<KeyValuePair<String, BigInteger>> results = GenericFactory.newArrayList ();

        final DirectoryTree dtSource = new DirectoryTree (basePath, pathsToIgnore, excludeFilenames);

        final List<String> filepaths = dtSource.getAbsolutePaths ();
        for (final String filepath : filepaths)
        {
            final BigInteger digest = calculateFileMd5Checksum (filepath);
            final KeyValuePair<String, BigInteger> result = KeyValuePair.of (filepath, digest);
            results.add (result);
        }

        return results;
    }

    public static String getFilename (final String filepath)
    {
        final File f = new File (filepath);
        return f.getName ();
    }

    /**
     * Gets the file objects from a directory (non-recursive).
     *
     * @param dir
     *            name of the directory
     * @param includeDirectories
     *            whether to include dirctory objects in the results
     * @param includeFilenames
     *            set of wildcards that must be matched to include the file/directory
     *            names in the results. Null means include all.
     * @param excludeFilenames
     *            set of wildcards that when matched will exclude the file/directory names
     *            from the results. Null means exclude nothing.
     * @return the file objects
     */
    public static List<File> getFileObjects (final File dir, final boolean includeDirectories,
        final String[] includeFilenames, final String[] excludeFilenames)
    {
        final List<File> result = new ArrayList<File> ();

        final File[] filesAndDirs = dir.listFiles ();
        if (filesAndDirs != null)
        {
            for (final File file : filesAndDirs)
            {
                final String filename = file.getName ();
                if (passesInclusionFilter (filename, includeFilenames) && passesExclusionFilter (filename,
                    excludeFilenames) && (includeDirectories || file.isFile ()))
                {
                    result.add (file);
                }
            }
        }

        return result;
    }

    public static Stream<String> getFileStreamRemoveCommentLines (final String filename,
        final List<E2<String, String>> substitutions) throws IOException
    {
        return Files.lines (Paths.get (filename))//
            .filter (line -> !line.trim ().startsWith ("//"))//
            .map (s -> HcUtil.substituteSymbols (s, substitutions));
    }

    public static String getNativePath (final String filepath)
    {
        return new File (filepath).getAbsolutePath ();
    }

    public static Path getPathObject (final String from)
    {
        return FileSystems.getDefault ().getPath (from);
    }

    public static List<File> getRecursiveFileObjects (final File dir, final boolean includeDirectories,
        final String[] pathsToIgnore, final String[] excludeFilenames)
    {
        final List<File> result = new ArrayList<File> ();

        final File[] filesAndDirs = dir.listFiles ();
        if (filesAndDirs != null)
        {
            for (final File file : filesAndDirs)
            {
                if (!file.isFile ())
                {
                    // Must be a directory. Avoid any paths listed in pathsToIgnore (assumed to
                    // be in forward slash separator format).
                    final String path = HcUtil.toSlashPath (file.getAbsolutePath ());

                    if (passesExclusionFilter (path, pathsToIgnore))
                    {
                        if (includeDirectories)
                        {
                            result.add (file);
                        }

                        // Recurse.
                        final List<File> deeperList =
                            getRecursiveFileObjects (file, includeDirectories, pathsToIgnore, excludeFilenames);

                        result.addAll (deeperList);
                    }
                }
                else
                {
                    // Is a file.
                    if (passesExclusionFilter (file.getName (), excludeFilenames))
                    {
                        result.add (file);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Recursive a directory tree and return a list of all Files found; the list is sorted
     * using File.compareTo ().
     *
     * @param directoryPath
     *            is a valid directory, which can be read.
     * @param pathsToIgnore
     * @param excludeFilenames
     * @param
     */
    public static List<File> getRecursiveFileObjectsSorted (final String directoryPath,
        final boolean includeDirectories, final String[] pathsToIgnore, final String[] excludeFilenames)
    {
        ThreadContext.assertError (HcUtil.safeGetLength (directoryPath) > 0, "Invalid directory name");

        final File dir = getDirectory (directoryPath);
        final List<File> result = getRecursiveFileObjects (dir, includeDirectories, pathsToIgnore, excludeFilenames);
        Collections.sort (result);

        return result;
    }

    public static String getRelativeFilepath (final File f, final String basePath)
    {
        final String filepath = getAbsolutePath (f);
        //ThreadContext.assertFault (path.startsWith (m_basePath), "Logic error [%s] should have started with [%s]", path, m_basePath);

        final String subpath = filepath.substring (basePath.length () + 1);
        return subpath;
    }

    public static String getRelativeFilepath (final String filename, final String basePath)
    {
        return getRelativeFilepath (new File (filename), basePath);
    }

    public static Path getRoot (final String pathName)
    {
        return getPathObject (pathName).getRoot ();
    }

    public static String getRootName (String pathName)
    {
        String root = null;

        Path rootPath = getRoot (pathName);
        if (rootPath == null)
        {
            // If the pathname is relative, prepend the current working directory.
            final String currentDir = getCurrentDirectory ();
            pathName = HcUtil.formFilepath (currentDir, pathName);
        }

        final boolean isWindows = HcUtil.isOperatingSystemWindows ();
        if (!isWindows)
        {
            // Handle /Volumes/Diskxxx/xxxxxx
            if (pathName.startsWith (VOLUME_ROOT_PREFIX))
            {
                final String s = pathName.substring (VOLUME_ROOT_PREFIX.length ());
                final int count = s.indexOf ('/');
                final String volName = count == -1 ? s : s.substring (0, count);
                root = String.format ("%s%s/", VOLUME_ROOT_PREFIX, volName);
            }
        }

        if (root == null)
        {
            rootPath = getRoot (pathName);
            if (rootPath != null)
            {
                root = HcUtil.toSlashPath (rootPath.toString ());
            }
        }

        return root == null ? "" : root;
    }

    public static boolean isFileNewer (final String absoluteFilepath1, final String absoluteFilepath2)
    {
        final long timestamp1 = new File (absoluteFilepath1).lastModified ();
        final long timestamp2 = new File (absoluteFilepath2).lastModified ();

        return timestamp1 > timestamp2;
    }

    public static boolean isSameDrive (final String sourceBaseDirectory, final String destBaseDirectory)
    {
        final String rootSource = getRootName (sourceBaseDirectory);
        final String rootDestination = getRootName (destBaseDirectory);

        final boolean sameRoot = HcUtil.safeEquals (rootSource, rootDestination);
        return sameRoot;
    }

    public static void moveFile (final String from, final String to)
    {
        final Path fromPath = getPathObject (from);
        final Path toPath = getPathObject (to);
        ensureDirectoryExists (toPath.getParent ().toString ());
        try
        {
            // Try an atomic move and fallback to non-atomic if the operation is
            // not supported on this operating system.
            try
            {
                Files.move (fromPath, toPath, StandardCopyOption.ATOMIC_MOVE);
            }
            catch (final AtomicMoveNotSupportedException e)
            {
                Files.move (fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (final IOException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }
    }

    public static boolean passesExclusionFilter (final String filename, final String[] excludeFilenames)
    {
        return excludeFilenames == null || !HcUtil.containsWildcard (excludeFilenames, filename);
    }

    public static boolean passesInclusionFilter (final String filename, final String[] includeFilenames)
    {
        return includeFilenames == null || HcUtil.containsWildcard (includeFilenames, filename);
    }

    public static byte[] readBinaryFile (final String filepath)
    {
        final File file = new File (filepath);

        final long size = file.length ();
        final byte[] data = new byte[(int) size];

        try (final FileInputStream in = new FileInputStream (file))
        {
            final int readBytes = in.read (data);

            ThreadContext.assertFault (readBytes == size, "File [%s] expected %s bytes but read %s", filepath, size,
                readBytes);
        }
        catch (final IOException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }

        return data;
    }

    public static String readFileRemoveCommentLines (final String filename,
        final List<E2<String, String>> substitutions)
    {
        String fileContents = null;

        try (final Stream<String> lines = getFileStreamRemoveCommentLines (filename, substitutions))
        {
            fileContents = lines.collect (joining (HcUtil.NewLine));
        }
        catch (final IOException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }

        return fileContents;
    }

    public static String readTextFile (final String filename)
    {
        String fileContents = null;

        try (final Stream<String> lines = Files.lines (Paths.get (filename)))
        {
            // @formatter:off
            fileContents = lines
                .collect (joining (HcUtil.NewLine));
            // @formatter:on
        }
        catch (final IOException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }

        return fileContents;
    }

    public static void recurseFileObjects (final File dir, final String[] pathsToIgnore,
        final String[] excludeFilenames, final BiConsumer<Boolean, File> handler)
    {
        final File[] filesAndDirs = dir.listFiles ();
        if (filesAndDirs != null)
        {
            for (final File file : filesAndDirs)
            {
                if (!file.isFile ())
                {
                    // Must be a directory. Avoid any paths listed in pathsToIgnore (assumed to
                    // be in forward slash separator format).
                    final String path = HcUtil.toSlashPath (file.getAbsolutePath ());

                    if (passesExclusionFilter (path, pathsToIgnore))
                    {
                        handler.accept (true, file);// true means is directory

                        // Recurse.
                        recurseFileObjects (file, pathsToIgnore, excludeFilenames, handler);
                    }
                }
                else
                {
                    // Is a file.
                    if (passesExclusionFilter (file.getName (), excludeFilenames))
                    {
                        handler.accept (false, file);// false means is a file
                    }
                }
            }
        }
    }

    public static E2<List<String>, List<String>> recurseSubdirectories (final String sourceBaseDirectory,
        final String destBaseDirectory, final String[] sourcePathsToIgnore, final String[] destPathsToIgnore,
        final String[] excludeFilenames)
    {
        E2<List<String>, List<String>> fileLists;

        final boolean sameRoot = isSameDrive (sourceBaseDirectory, destBaseDirectory);
        if (sameRoot)
        {
            final DirectoryTree dtSource =
                new DirectoryTree (sourceBaseDirectory, sourcePathsToIgnore, excludeFilenames);
            final DirectoryTree dtDest = new DirectoryTree (destBaseDirectory, destPathsToIgnore, null);

            final List<String> filesSource = dtSource.getRelativeFilepaths ();
            final List<String> filesDest = dtDest.getRelativeFilepaths ();

            fileLists = E2.of (filesSource, filesDest);
        }
        else
        {
            // Recurse the source and destination paths concurrently.
            final Callable<List<String>> taskSource = () ->
            {
                final DirectoryTree dtSource =
                    new DirectoryTree (sourceBaseDirectory, sourcePathsToIgnore, excludeFilenames);
                return dtSource.getRelativeFilepaths ();
            };
            final Callable<List<String>> taskDest = () ->
            {
                final DirectoryTree dtDest = new DirectoryTree (destBaseDirectory, destPathsToIgnore, null);
                return dtDest.getRelativeFilepaths ();
            };
            final List<Callable<List<String>>> tasks = Arrays.asList (taskSource, taskDest);

            final List<List<String>> results = HcUtil.executeConcurrently (tasks, tasks.size (), false);

            final List<String> filesSource = results.get (0);
            final List<String> filesDest = results.get (1);

            fileLists = E2.of (filesSource, filesDest);
        }

        return fileLists;
    }

    public static boolean renameDirectory (final String toDir, final String fromDir)
    {
        final File from = getDirectory (fromDir);
        final File to = new File (toDir);

        return from.renameTo (to);
    }

    public static void safeClose (final Closeable c)
    {
        if (c != null)
        {
            try
            {
                c.close ();
            }
            catch (final IOException e)
            {
                // Propagate exception as unchecked fault up to the fault barrier.
                ThreadContext.throwFault (e);
            }
        }
    }

    public static void safeClose (final ServerSocket socket)
    {
        if (socket != null)
        {
            try
            {
                socket.close ();
            }
            catch (final IOException e)
            {
                // Propagate exception as unchecked fault up to the fault barrier.
                ThreadContext.throwFault (e);
            }
        }
    }

    public static boolean safeClose (final SmartFile s)
    {
        boolean updated = false;
        if (s != null)
        {
            s.close ();
            updated = s.didUpdate ();
        }

        return updated;
    }

    public static void safeClose (final Socket connection)
    {
        if (connection != null)
        {
            try
            {
                connection.close ();
            }
            catch (final IOException e)
            {
                // Propagate exception as unchecked fault up to the fault barrier.
                ThreadContext.throwFault (e);
            }
        }
    }

    public static void safeDeleteFile (final String filePath)
    {
        if (filePath != null)
        {
            try
            {
                new File (filePath).delete ();
            }
            catch (final Throwable e)
            {
                // Propagate exception as unchecked fault up to the fault barrier. Use
                // throwRootFault to handle the caught exception according to its type.
                ThreadContext.throwFault (e);
            }
        }
    }

    public static void safeDeleteFileNoThrow (final String filePath)
    {
        if (filePath != null)
        {
            try
            {
                new File (filePath).delete ();
            }
            catch (final Throwable e)
            {
            }
        }
    }

    /**
     * Sends a request object message to a socket server and waits for an object message
     * back.
     *
     * @param socket
     * @param req
     * @return
     */
    public static <TResponse> TResponse sendReceiveObjects (final Socket socket, final Object req)
    {
        TResponse response = null;

        try (final ObjectOutputStream oos = new ObjectOutputStream (socket.getOutputStream ()))
        {
            oos.writeObject (req);

            try (final ObjectInputStream ois = new ObjectInputStream (socket.getInputStream ()))
            {
                final Object replyObject = ois.readObject ();

                response = HcUtil.uncheckedCast (replyObject);
            }
            catch (final ClassNotFoundException e)
            {
                // Propagate exception as unchecked fault up to the fault barrier.
                ThreadContext.throwFault (e);
            }
        }
        catch (final IOException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }

        return response;
    }

    public static String serialiseObjectAsAsciiHexString (final Serializable object)
    {
        final byte[] bytes = serialiseObjectAsBytes (object);
        return byteArrayToAsciiHexString (bytes);
    }

    public static String serialiseObjectAsBase64String (final Serializable object)
    {
        final byte[] bytes = serialiseObjectAsBytes (object);
        return Base64.getEncoder ().encodeToString (bytes);
    }

    public static byte[] serialiseObjectAsBytes (final Serializable object)
    {
        final ByteArrayOutputStream out = new ByteArrayOutputStream (100);
        try (final ObjectOutputStream s = new ObjectOutputStream (out))
        {
            s.writeObject (object);
            s.flush ();
        }
        catch (final IOException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }

        return out.toByteArray ();
    }

    public static void writeFile (final String filepath, final byte[] data, final OpenOption... options)
    {
        final String destLocalAbsolutePath = new File (filepath).getParent ();
        ensureDirectoryExists (destLocalAbsolutePath);

        final Path file = Paths.get (filepath);

        // NIO.2
        try (final OutputStream out = new BufferedOutputStream (Files.newOutputStream (file, options)))
        {
            out.write (data, 0, data.length);
        }
        catch (final IOException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }
    }

    public static void writeFile (final String filepath, final List<String> lines)
    {
        final String fileContents = lines.stream ().collect (joining (HcUtil.NewLine));
        writeFile (filepath, fileContents);

    }

    public static void writeFile (final String filepath, final String contents)
    {
        writeFile (filepath, contents.getBytes (), CREATE, TRUNCATE_EXISTING);
    }

    public static void writeTextFile (final String filepath, final String contents)
    {
        writeFile (filepath, contents);
    }

    public static boolean writeTextFileSmart (final String outputFilename, final String s)
    {
        final SmartFileWriter sw = new SmartFileWriter (outputFilename);

        try (final PrintWriter out = new PrintWriter (sw))
        {
            out.print (s);
        }

        return sw.didUpdate ();
    }

    private static void deleteEmptyDirectories (final List<String> deletedDirectories, final File dir,
        final boolean includeTopLevelDirectory, final int depth)
    {
        File[] filesAndDirs = dir.listFiles ();
        if (filesAndDirs != null)
        {
            if (filesAndDirs.length > 0)
            {
                for (final File file : filesAndDirs)
                {
                    if (!file.isFile ())
                    {
                        // Must be a directory - recurse.
                        deleteEmptyDirectories (deletedDirectories, file, includeTopLevelDirectory, depth + 1);
                    }
                }

                // Re-evaluate for removed directories.
                filesAndDirs = dir.listFiles ();
            }

            // See if this dir is now empty.
            if (filesAndDirs.length == 0 && (includeTopLevelDirectory || depth > 0))
            {
                final String absolutePath = dir.getAbsolutePath ();
                deletedDirectories.add (absolutePath);

                final boolean ok = dir.delete ();
                ThreadContext.assertWarning (ok, "Error deleting [%s]", absolutePath);
            }
        }
    }

    private static final String VOLUME_ROOT_PREFIX = "/Volumes/";
}
