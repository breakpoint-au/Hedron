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
package au.com.breakpoint.hedron.core.dao;

import java.io.Serializable;
import au.com.breakpoint.hedron.core.HcUtil;

public class SetElement implements Serializable
{
    public SetElement ()
    {
    }

    public SetElement (final int columnId, final Object value)
    {
        m_columnId = columnId;
        m_value = value;
    }

    public int getColumnId ()
    {
        return m_columnId;
    }

    public Object getValue ()
    {
        return m_value;
    }

    public void setColumnId (final int columnId)
    {
        m_columnId = columnId;
    }

    public void setValue (final Object value)
    {
        m_value = value;
    }

    @Override
    public String toString ()
    {
        // m_value might be an array so use deep version.
        return HcUtil.deepToString (m_columnId, m_value);
    }

    private int m_columnId;

    private Object m_value;

    private static final long serialVersionUID = -8271529206307676918L;
}
