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

/**
 * Example:
 *
 * final FetchSql sql = new FetchSql (SomeTable.Columns.Id).greaterThanOrEqual (6) .and
 * (SomeTable.Columns.Id).lessThan (2345) .orderBy (SomeTable.Columns.SomeType).ascending
 * () .then (SomeTable.Columns.Id).descending ();
 *
 * List<SomeTable> l = new SomeTableDao (dataSource).fetch (sql);
 */
public class FetchSql<TEntity extends IEntity<?>> implements Serializable
{
    public FetchSql ()
    {
    }

    public FetchSql (final IColumnIndex<TEntity> columnId)
    {
        and (columnId);
    }

    public FetchSql<TEntity> and (final IColumnIndex<TEntity> columnId)
    {
        m_whereSql.and (columnId);
        return this;
    }

    public FetchSql<TEntity> ascending ()
    {
        m_orderBySql.ascending ();
        return this;
    }

    public FetchSql<TEntity> descending ()
    {
        m_orderBySql.descending ();
        return this;
    }

    public FetchSql<TEntity> equal (final Object value)
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

    public FetchSql<TEntity> greaterThan (final Object value)
    {
        m_whereSql.greaterThan (value);
        return this;
    }

    public FetchSql<TEntity> greaterThanOrEqual (final Object value)
    {
        m_whereSql.greaterThanOrEqual (value);
        return this;
    }

    public FetchSql<TEntity> lessThan (final Object value)
    {
        m_whereSql.lessThan (value);
        return this;
    }

    public FetchSql<TEntity> lessThanOrEqual (final Object value)
    {
        m_whereSql.lessThanOrEqual (value);
        return this;
    }

    public FetchSql<TEntity> like (final Object value)
    {
        m_whereSql.like (value);
        return this;
    }

    public FetchSql<TEntity> notEqual (final Object value)
    {
        m_whereSql.notEqual (value);
        return this;
    }

    // Order by clause support
    public FetchSql<TEntity> orderBy (final IColumnIndex<TEntity> columnId)
    {
        m_orderBySql.then (columnId);
        return this;
    }

    public FetchSql<TEntity> then (final IColumnIndex<TEntity> columnId)
    {
        m_orderBySql.then (columnId);
        return this;
    }

    @Override
    public String toString ()
    {
        return HcUtil.toString (m_whereSql, m_orderBySql);
    }

    private final OrderBySql<TEntity> m_orderBySql = new OrderBySql<> ();

    private final WhereSql<TEntity> m_whereSql = new WhereSql<> ();

    private static final long serialVersionUID = 1L;
}
