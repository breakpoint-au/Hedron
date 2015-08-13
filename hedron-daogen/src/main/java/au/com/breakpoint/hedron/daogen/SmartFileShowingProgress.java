//                       __________________________________
//                ______|      Copyright 2008-2015         |______
//                \     |     Breakpoint Pty Limited       |     /
//                 \    |   http://www.breakpoint.com.au   |    /
//                 /    |__________________________________|    \
//                /_________/                          \_________\
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
// except in compliance with the License. You may obtain a copy of the License at
//	   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
// either express or implied. See the License for the specific language governing
// permissions and limitations under the License.
//
package au.com.breakpoint.hedron.daogen;

import au.com.breakpoint.hedron.core.SmartFile;

public class SmartFileShowingProgress extends SmartFile
{
    public SmartFileShowingProgress (final String filepath)
    {
        super (filepath);
        m_filepath = filepath;

        DaoGen.getFeedback ().showProgress (m_filepath);
    }

    @Override
    public void close ()
    {
        super.close ();
        if (didUpdate ())
        {
            DaoGen.getFeedback ().outputMessage (true, 1, m_filepath);
        }
    }

    private final String m_filepath;
}
