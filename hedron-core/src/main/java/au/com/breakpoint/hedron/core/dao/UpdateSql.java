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
 * UpdateSql = new UpdateSql (SomeTable.Columns.ShortName).set (e.getShortName ()) .and
 * (SomeTable.Columns.Name).set (e.getName ()) .where (SomeTable.Columns.Id).equal
 * (e.getId ());
 *
 * new SomeTableDao (dataSource).update (sql);
 */
public class UpdateSql<TEntity extends IEntity<?>> implements Serializable
{
    public UpdateSql ()
    {
    }

    public UpdateSql (final IColumnIndex<TEntity> columnId)
    {
        and (columnId);
    }

    public UpdateSql<TEntity> and (final IColumnIndex<TEntity> columnId)
    {
        if (m_gotWhere)
        {
            m_whereSql.and (columnId);
        }
        else
        {
            // Still in the 'set' part of the update statement.
            m_setSql.and (columnId);
        }

        return this;
    }

    public UpdateSql<TEntity> equal (final Object value)
    {
        m_whereSql.equal (value);
        return this;
    }

    public SetElement[] getSetElements ()
    {
        return m_setSql.getSetElements ();
    }

    public WhereElement[] getWhereElements ()
    {
        return m_whereSql.getWhereElements ();
    }

    public UpdateSql<TEntity> greaterThan (final Object value)
    {
        m_whereSql.greaterThan (value);
        return this;
    }

    public UpdateSql<TEntity> greaterThanOrEqual (final Object value)
    {
        m_whereSql.greaterThanOrEqual (value);
        return this;
    }

    public UpdateSql<TEntity> lessThan (final Object value)
    {
        m_whereSql.lessThan (value);
        return this;
    }

    public UpdateSql<TEntity> lessThanOrEqual (final Object value)
    {
        m_whereSql.lessThanOrEqual (value);
        return this;
    }

    public UpdateSql<TEntity> like (final Object value)
    {
        m_whereSql.like (value);
        return this;
    }

    public UpdateSql<TEntity> notEqual (final Object value)
    {
        m_whereSql.notEqual (value);
        return this;
    }

    // Set clause support
    public UpdateSql<TEntity> set (final Object value)
    {
        m_setSql.set (value);
        return this;
    }

    @Override
    public String toString ()
    {
        return HcUtil.toString (m_setSql, m_whereSql);
    }

    // Where clause support
    public UpdateSql<TEntity> where (final IColumnIndex<TEntity> columnId)
    {
        m_whereSql.and (columnId);
        m_gotWhere = true;
        return this;
    }

    private boolean m_gotWhere;

    private final SetSql<TEntity> m_setSql = new SetSql<> ();

    private final WhereSql<TEntity> m_whereSql = new WhereSql<> ();

    private static final long serialVersionUID = 1L;
}
