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

import javax.sql.DataSource;

/**
 * Base class containing the data source for the IEntityDao methods.
 *
 * @param <TEntity>
 *            Type of the entity.
 */
public abstract class BaseEntityDao<TEntity extends IEntity<TPrimaryKey>, TPrimaryKey>
    implements IEntityDao<TEntity, TPrimaryKey>
{
    public BaseEntityDao ()
    {
        m_dataSource = null;
    }

    public BaseEntityDao (final DataSource dataSource)
    {
        m_dataSource = dataSource;
    }

    /**
     * Accessor for the data source object.
     *
     * @return data source.
     */
    @Override
    public DataSource getDataSource ()
    {
        return m_dataSource;
    }

    protected final DataSource m_dataSource;
}
