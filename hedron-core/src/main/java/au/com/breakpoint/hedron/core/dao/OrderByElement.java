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
package au.com.breakpoint.hedron.core.dao;

import java.io.Serializable;
import au.com.breakpoint.hedron.core.HcUtil;

public class OrderByElement implements Serializable
{
    public OrderByElement ()
    {
    }

    public OrderByElement (final int columnId)
    {
        this (columnId, true);// default to ascending
    }

    public OrderByElement (final int columnId, final boolean isAscending)
    {
        m_columnId = columnId;
        m_isAscending = isAscending;
    }

    public int getColumnId ()
    {
        return m_columnId;
    }

    public boolean isAscending ()
    {
        return m_isAscending;
    }

    public void setAscending (final boolean isAscending)
    {
        m_isAscending = isAscending;
    }

    public void setColumnId (final int columnId)
    {
        m_columnId = columnId;
    }

    @Override
    public String toString ()
    {
        return HcUtil.toString (m_columnId, m_isAscending);
    }

    private int m_columnId;

    private boolean m_isAscending = true;// default to ascending

    private static final long serialVersionUID = -5307537369411836281L;
}
