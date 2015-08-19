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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SmartFile implements ICloseable
{
    public SmartFile (final String filePath)
    {
        m_targetFilePath = filePath;
    }

    @Override
    public void close ()
    {
        m_updated = updateTargetFile ();
    }

    public boolean didUpdate ()
    {
        return m_updated;
    }

    public int getSectionNumber ()
    {
        return m_sectionNumber;
    }

    public boolean isCommitEnabled ()
    {
        return m_commitEnabled;
    }

    public void print (final String s)
    {
        getBuffer ().add (s);
    }

    public void printf (final String format, final Object... args)
    {
        final String s = String.format (format, args);
        print (s);
    }

    public void setCommitEnabled (final boolean commitEnabled)
    {
        m_commitEnabled = commitEnabled;
    }

    public void setSectionNumber (final int sectionNumber)
    {
        m_sectionNumber = sectionNumber;
    }

    private List<String> getBuffer ()
    {
        return m_buffers.computeIfAbsent (m_sectionNumber, ArrayList::new);
    }

    private String getFileContents ()
    {
        // Append all the strings from the map of arrays.
        final String s = m_buffers.values ()//
            .stream ()//
            .flatMap (List::stream)//
            .collect (Collectors.joining ());

        return s;
    }

    private boolean updateTargetFile ()
    {
        boolean updated = false;

        if (m_commitEnabled)
        {
            final String prevContents = HcUtilFile.readTextFile (m_targetFilePath);
            final String newContents = getFileContents ();

            updated = !prevContents.equals (newContents);
            //System.out.printf ("prevContents %s%n[%s]%n", prevContents.length (), prevContents);
            //System.out.printf ("newContents %s%n[%s]%n", newContents.length (), newContents);

            if (updated)
            {
                HcUtilFile.writeTextFile (m_targetFilePath, newContents);
            }
        }

        return updated;
    }

    /** Ordered collection of buffers that are appended in order at the end */
    private final Map<Integer, List<String>> m_buffers = GenericFactory.newTreeMap ();

    private boolean m_commitEnabled = true;

    private int m_sectionNumber = DEFAULT_SECTION_NUMBER;

    private final String m_targetFilePath;

    private boolean m_updated;

    private static final int DEFAULT_SECTION_NUMBER = 1_000;
}
