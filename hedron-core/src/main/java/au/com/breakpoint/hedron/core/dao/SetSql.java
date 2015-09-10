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

import static au.com.breakpoint.hedron.core.GenericFactory.newArrayList;
import java.io.Serializable;
import java.util.List;
import au.com.breakpoint.hedron.core.HcUtil;

/**
 * Example:
 *
 * final SetSql sql = new SetSql (SomeTable.Columns.SomeType).set ("asdf") .and
 * (SomeTable.Columns.Id).set (15);
 *
 * List<SomeTable> l = new SomeTableDao (dataSource).fetch (sql);
 */
public class SetSql<TEntity extends IEntity<?>> implements Serializable
{
    public SetSql ()
    {
    }

    public SetSql (final IColumnIndex<TEntity> columnId)
    {
        and (columnId);
    }

    // Set clause support
    public SetSql<TEntity> and (final IColumnIndex<TEntity> columnId)
    {
        final SetElement ob = addSetElement ();
        ob.setColumnId (columnId.getColumnIndex ());

        return this;
    }

    public SetElement[] getSetElements ()
    {
        return m_setElements.toArray (new SetElement[m_setElements.size ()]);
    }

    public SetSql<TEntity> set (final Object value)
    {
        final SetElement ob = getLastSetElement ();
        ob.setValue (value);

        return this;
    }

    @Override
    public String toString ()
    {
        return HcUtil.toString (m_setElements);
    }

    private SetElement addSetElement ()
    {
        final SetElement sc = new SetElement ();
        m_setElements.add (sc);

        return sc;
    }

    private SetElement getLastSetElement ()
    {
        return m_setElements.get (m_setElements.size () - 1);
    }

    private final List<SetElement> m_setElements = newArrayList ();

    private static final long serialVersionUID = 1L;
}
