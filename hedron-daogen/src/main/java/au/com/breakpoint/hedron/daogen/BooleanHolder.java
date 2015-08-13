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

import java.io.Serializable;

public class BooleanHolder implements Serializable
{
    public BooleanHolder ()
    {
    }

    public BooleanHolder (final boolean value)
    {
        m_value = value;
    }

    public boolean getValue ()
    {
        return m_value;
    }

    public void setValue (final boolean value)
    {
        m_value = value;
    }

    @Override
    public String toString ()
    {
        return String.valueOf (m_value);
    }

    private boolean m_value;

    private static final long serialVersionUID = 551241245993755062L;
}
