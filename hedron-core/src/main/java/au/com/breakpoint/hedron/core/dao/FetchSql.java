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

/**
 * Example:
 *
 * final FetchSql sql = new FetchSql (SomeTable.Columns.Id).greaterThanOrEqual (6) .and
 * (SomeTable.Columns.Id).lessThan (2345) .orderBy (SomeTable.Columns.SomeType).ascending
 * () .then (SomeTable.Columns.Id).descending ();
 *
 * List<SomeTable> l = new SomeTableDao (dataSource).fetch (sql);
 */
public class FetchSql implements Serializable
{
    // TODO _ review fluent syntax
    public FetchSql ()
    {
    }

    public FetchSql (final int columnId)
    {
        and (columnId);
    }

    public FetchSql and (final int columnId)
    {
        m_whereSql.and (columnId);
        return this;
    }

    public FetchSql ascending ()
    {
        m_orderBySql.ascending ();
        return this;
    }

    public FetchSql descending ()
    {
        m_orderBySql.descending ();
        return this;
    }

    public FetchSql equal (final Object value)
    {
        m_whereSql.equal (value);
        return this;
    }

    public OrderByElement[] getOrderByElements ()
    {
        return m_orderBySql.getOrderByElements ();
    }

    // Where clause support
    public WhereElement[] getWhereElements ()
    {
        return m_whereSql.getWhereElements ();
    }

    public FetchSql greaterThan (final Object value)
    {
        m_whereSql.greaterThan (value);
        return this;
    }

    public FetchSql greaterThanOrEqual (final Object value)
    {
        m_whereSql.greaterThanOrEqual (value);
        return this;
    }

    public FetchSql lessThan (final Object value)
    {
        m_whereSql.lessThan (value);
        return this;
    }

    public FetchSql lessThanOrEqual (final Object value)
    {
        m_whereSql.lessThanOrEqual (value);
        return this;
    }

    public FetchSql like (final Object value)
    {
        m_whereSql.like (value);
        return this;
    }

    public FetchSql notEqual (final Object value)
    {
        m_whereSql.notEqual (value);
        return this;
    }

    // Order by clause support
    public FetchSql orderBy (final int columnId)
    {
        m_orderBySql.then (columnId);
        return this;
    }

    public FetchSql then (final int columnId)
    {
        m_orderBySql.then (columnId);
        return this;
    }

    @Override
    public String toString ()
    {
        return HcUtil.toString (m_whereSql, m_orderBySql);
    }

    private final OrderBySql m_orderBySql = new OrderBySql ();

    private final WhereSql m_whereSql = new WhereSql ();

    private static final long serialVersionUID = 7990434835781817954L;
}
