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

import java.io.File;
import java.util.List;

public final class DirectoryTree
{
    public DirectoryTree (final String directoryPath)
    {
        this (directoryPath, null, null);
    }

    public DirectoryTree (final String directoryPath, final String[] pathsToIgnore, final String[] excludeFilenames)
    {
        m_basePath = HcUtil.removeTrailingPathSeparator (directoryPath);
        m_files = HcUtilFile.getRecursiveFileObjectsSorted (m_basePath, false, pathsToIgnore, excludeFilenames);
    }

    public List<String> getAbsolutePaths ()
    {
        final List<String> l = GenericFactory.newArrayList ();

        for (final File f : m_files)
        {
            final String path = HcUtilFile.getAbsolutePath (f);
            l.add (path);
        }

        return l;
    }

    public List<File> getFiles ()
    {
        return m_files;
    }

    public List<String> getRelativeFilepaths ()
    {
        final List<String> l = GenericFactory.newArrayList ();

        for (final File f : m_files)
        {
            final String relativePath = HcUtilFile.getRelativeFilepath (f, m_basePath);
            l.add (relativePath);
        }

        return l;
    }

    private final String m_basePath;

    private final List<File> m_files;
}
