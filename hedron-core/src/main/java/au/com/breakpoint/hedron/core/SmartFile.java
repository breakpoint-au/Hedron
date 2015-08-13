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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import au.com.breakpoint.hedron.core.context.ThreadContext;

public class SmartFile implements ICloseable
{
    public SmartFile (final String filePath)
    {
        m_targetFilePath = filePath;
        try
        {
            m_tempFile = File.createTempFile ("SmartFile", ".tmp");
            m_tempFile.deleteOnExit ();
            m_printwriter = new PrintWriter (m_tempFile);
        }
        catch (final IOException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }
    }

    @Override
    public void close ()
    {
        try
        {
            m_updated = updateTargetFile ();
        }
        finally
        {
            cleanupTemporaryFile ();
        }
    }

    public void flush ()
    {
        m_printwriter.flush ();
    }

    public boolean isCommitEnabled ()
    {
        return m_commitEnabled;
    }

    public boolean didUpdate ()
    {
        return m_updated;
    }

    public void print (final String s)
    {
        m_printwriter.print (s);
    }

    public void printf (final String format, final Object... args)
    {
        final String s = String.format (format, args);
        m_printwriter.print (s);
    }

    public void setCommitEnabled (final boolean commitEnabled)
    {
        m_commitEnabled = commitEnabled;
    }

    private void cleanupTemporaryFile ()
    {
        if (m_tempFile != null)
        {
            // Always get rid of the temporary file.
            final boolean ok = m_tempFile.delete ();
            ThreadContext.assertWarning (ok, "Error deleting temporary file");
            m_tempFile = null;
        }
    }

    private boolean updateTargetFile ()
    {
        boolean updated = false;

        if (m_printwriter != null)
        {
            m_printwriter.flush ();
            m_printwriter.close ();
            m_printwriter = null;

            if (m_commitEnabled)
            {
                final String tempFilePath = m_tempFile.getAbsolutePath ();
                updated = HcUtilFile.copyFileIfDifferent (tempFilePath, m_targetFilePath);
            }
        }

        return updated;
    }

    private boolean m_commitEnabled = true;

    private PrintWriter m_printwriter;

    private final String m_targetFilePath;

    private File m_tempFile;

    private boolean m_updated;
}
