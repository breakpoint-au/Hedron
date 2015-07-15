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

import static au.com.breakpoint.hedron.core.GenericFactory.newArrayList;
import java.io.Serializable;
import java.util.List;
import au.com.breakpoint.hedron.core.HcUtil;

/**
 * Example:
 *
 * final WhereSql sql = new WhereSql (SomeTable.Columns.Id).greaterThanOrEqual (6) .and
 * (SomeTable.Columns.Id).lessThan (2345);
 *
 * List<SomeTable> l = new SomeTableDao (dataSource).fetch (sql);
 */
public class WhereSql implements Serializable
{
    // TODO _ review fluent syntax
    public WhereSql ()
    {
    }

    public WhereSql (final int columnId)
    {
        and (columnId);
    }

    // Where clause support
    public WhereSql and (final int columnId)
    {
        final WhereElement sc = addSqlClause ();
        sc.setColumnId (columnId);

        return this;
    }

    public WhereSql equal (final Object value)
    {
        final WhereElement sc = getLastSqlClause ();
        sc.setOperator (WhereElement.Operator.Equal);
        sc.setValue (value);

        return this;
    }

    public WhereElement[] getWhereElements ()
    {
        return m_whereElements.toArray (new WhereElement[m_whereElements.size ()]);
    }

    public WhereSql greaterThan (final Object value)
    {
        final WhereElement sc = getLastSqlClause ();
        sc.setOperator (WhereElement.Operator.GreaterThan);
        sc.setValue (value);

        return this;
    }

    public WhereSql greaterThanOrEqual (final Object value)
    {
        final WhereElement sc = getLastSqlClause ();
        sc.setOperator (WhereElement.Operator.GreaterThanOrEqual);
        sc.setValue (value);

        return this;
    }

    public WhereSql lessThan (final Object value)
    {
        final WhereElement sc = getLastSqlClause ();
        sc.setOperator (WhereElement.Operator.LessThan);
        sc.setValue (value);

        return this;
    }

    public WhereSql lessThanOrEqual (final Object value)
    {
        final WhereElement sc = getLastSqlClause ();
        sc.setOperator (WhereElement.Operator.LessThanOrEqual);
        sc.setValue (value);

        return this;
    }

    public WhereSql like (final Object value)
    {
        final WhereElement sc = getLastSqlClause ();
        sc.setOperator (WhereElement.Operator.Like);
        sc.setValue (value);

        return this;
    }

    public WhereSql notEqual (final Object value)
    {
        final WhereElement sc = getLastSqlClause ();
        sc.setOperator (WhereElement.Operator.NotEqual);
        sc.setValue (value);

        return this;
    }

    @Override
    public String toString ()
    {
        return HcUtil.toString (m_whereElements);
    }

    private WhereElement addSqlClause ()
    {
        final WhereElement sc = new WhereElement ();
        m_whereElements.add (sc);

        return sc;
    }

    private WhereElement getLastSqlClause ()
    {
        return m_whereElements.get (m_whereElements.size () - 1);
    }

    private final List<WhereElement> m_whereElements = newArrayList ();

    private static final long serialVersionUID = 7990434835781817954L;
}
