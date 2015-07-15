//                       __________________________________
//                ______|         Copyright 2008           |______
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
package au.com.breakpoint.hedron.core.context;

import javax.sql.DataSource;

public class NamedDataSource
{
    public NamedDataSource ()
    {
    }

    public NamedDataSource (final String name, final DataSource dataSource)
    {
        m_name = name;
        m_dataSource = dataSource;
    }

    public DataSource getDataSource ()
    {
        return m_dataSource;
    }

    public String getName ()
    {
        return m_name;
    }

    public void setDataSource (final DataSource dataSource)
    {
        m_dataSource = dataSource;
    }

    public void setName (final String name)
    {
        m_name = name;
    }

    private DataSource m_dataSource;

    private String m_name;
}
