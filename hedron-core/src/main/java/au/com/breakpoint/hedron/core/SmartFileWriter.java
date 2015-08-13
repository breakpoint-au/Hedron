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
import java.io.Writer;

/**
 * java.io.Writer implementation wrapped around a SmartFile.
 */
public class SmartFileWriter extends Writer
{
    public SmartFileWriter (final String filePath)
    {
        m_smartFile = new SmartFile (filePath);
    }

    @Override
    public void close () throws IOException
    {
        m_smartFile.close ();
        m_updated = m_smartFile.didUpdate ();
    }

    public boolean didUpdate ()
    {
        return m_updated;
    }

    @Override
    public void flush () throws IOException
    {
        m_smartFile.flush ();
    }

    @Override
    public void write (final char[] cbuf, final int off, final int len) throws IOException
    {
        if (len > 0)
        {
            final StringBuilder sb = new StringBuilder (len);
            for (int i = off; i < off + len; ++i)
            {
                sb.append (cbuf[i]);
            }

            m_smartFile.print (sb.toString ());
        }
    }

    private final SmartFile m_smartFile;

    private boolean m_updated;
}
