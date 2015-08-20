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

import java.util.List;
import javax.sql.DataSource;
import au.com.breakpoint.hedron.core.context.ThreadContext;

/**
 * Basic methods implemented by an entity dao.
 *
 * @param <TEntity>
 *            Type of the entity.
 * @param <TPrimaryKey>
 *            Type of the primary key, eg Integer or String. If the primary key is
 *            compound, a tuple is used, eg E2<Integer, String>.
 */
public interface IEntityDao<TEntity extends IEntity<TPrimaryKey>, TPrimaryKey>
{
    /**
     * Deletes the entity table rows corresponding to the specified entities using primary
     * key values.
     *
     * @param es
     *            entities corresponding to the rows to be deleted.
     *
     * @return number of rows affected by the delete
     */
    default void delete (final List<TEntity> es)
    {
        ThreadContext.assertError (false, "delete () is not implemented for class [%s]",
            getClass ().getCanonicalName ());
    }

    /**
     * Deletes the entity table row corresponding to the specified entity.
     *
     * @return whether or not a row was affected by the delete
     */
    default boolean delete (@SuppressWarnings ("unused") final TEntity e)
    {
        ThreadContext.assertError (false, "delete () is not implemented for class [%s]",
            getClass ().getCanonicalName ());
        return false;
    }

    /**
     * Deletes rows from the entity table according to the criteria in whereElements.
     *
     * @param whereElements
     *            Specified criteria to be added into the where clause. These result in
     *            SQL 'and' clauses. There is currently no way to directly express 'or'
     *            clauses, or more complex expressions. Use a custom query or stored
     *            procedure instead.
     *
     * @return whether or not a row was affected by the delete
     */
    default int delete (final WhereElement[] whereElements)
    {
        ThreadContext.assertError (false, "delete () is not implemented for class [%s]",
            getClass ().getCanonicalName ());
        return 0;
    }

    /**
     * Deletes the entity table row corresponding to the specified primary key.
     *
     * @return whether or not a row was affected by the delete
     */
    default boolean deleteByPrimaryKey (@SuppressWarnings ("unused") final TPrimaryKey id)
    {
        ThreadContext.assertError (false, "deleteByPrimaryKey () is not implemented for class [%s]",
            getClass ().getCanonicalName ());
        return false;
    }

    /**
     * Fetches all rows of the entity table.
     *
     * @return Collection of TEntity entities
     */
    default List<TEntity> fetch (@SuppressWarnings ("unused") final OrderByElement... orderByElements)
    {
        ThreadContext.assertError (false, "fetch (OrderByElement... orderByElements) is not implemented for class [%s]",
            getClass ().getCanonicalName ());
        return null;
    }

    /**
     * Fetches the rows of the entity table that satisfy the criteria in the
     * <i>whereElements</i> parameter.
     *
     * @param whereElements
     *            Specified criteria to be added into the where clause. These result in
     *            SQL 'and' clauses. There is currently no way to directly express 'or'
     *            clauses, or more complex expressions. Use a custom query or stored
     *            procedure instead.
     *
     * @return Collection of TEntity entities
     */
    default List<TEntity> fetch (final WhereElement[] whereElements,
        @SuppressWarnings ("unused") final OrderByElement... orderByElements)
    {
        ThreadContext.assertError (false,
            "fetch (WhereElement[] whereElements, OrderByElement... orderByElements) is not implemented for class [%s]",
            getClass ().getCanonicalName ());
        return null;
    }

    /**
     * Fetches the row of the entity table for the specified primary key.
     *
     * @return The specified TEntity entity, or null if it does not exist
     */
    default TEntity fetchByPrimaryKey (@SuppressWarnings ("unused") final TPrimaryKey id)
    {
        ThreadContext.assertError (false, "fetchByPrimaryKey () is not implemented for class [%s]",
            getClass ().getCanonicalName ());
        return null;
    }

    /**
     * Accessor for the associated data source
     *
     * @return associated data source
     */
    DataSource getDataSource ();

    /**
     * Inserts the entity table rows corresponding to the specified entities.
     *
     * @param es
     *            entities corresponding to the rows to be inserted.
     * @return
     *
     * @return success result
     */
    default void insert (final List<TEntity> es)
    {
        ThreadContext.assertError (false, "insert () is not implemented for class [%s]",
            getClass ().getCanonicalName ());
    }

    /**
     * Inserts a row into the entity table.
     *
     * @param Data
     *            entity corresponding to the row to be inserted.
     */
    default void insert (@SuppressWarnings ("unused") final TEntity e)
    {
        ThreadContext.assertError (false, "insert () is not implemented for class [%s]",
            getClass ().getCanonicalName ());
    }

    /**
     * Factory method for the AccountingExtraction entity.
     *
     * @return instance of the AccountingExtraction entity.
     */
    TEntity newEntityInstance ();

    /**
     * Updates the entity table rows corresponding to the specified entities using primary
     * key values.
     *
     * @param es
     *            entities corresponding to the rows to be updated.
     */
    default void update (final List<TEntity> es)
    {
        ThreadContext.assertError (false, "update () is not implemented for class [%s]",
            getClass ().getCanonicalName ());
    }

    /**
     * Updates the entity table columns specified by newValues according to the criteria
     * in whereElements.
     *
     * @param newValues
     *            Collection of column/value pairs corresponding to the 'set' part of the
     *            update SQL.
     * @param whereElements
     *            Specified criteria to be added into the where clause. These result in
     *            SQL 'and' clauses. There is currently no way to directly express 'or'
     *            clauses, or more complex expressions. Use a custom query or stored
     *            procedure instead.
     *
     * @return the numbers of rows affected by the update
     */
    default int update (final SetElement[] newValues, final WhereElement[] whereElements)
    {
        ThreadContext.assertError (false, "update () is not implemented for class [%s]",
            getClass ().getCanonicalName ());
        return 0;
    }

    /**
     * Updates the entity table row corresponding to the specified entity using its
     * primary key value.
     *
     * @param Data
     *            entity corresponding to the row to be updated.
     *
     * @return whether or not a row was affected by the update
     */
    default boolean update (@SuppressWarnings ("unused") final TEntity e)
    {
        ThreadContext.assertError (false, "update () is not implemented for class [%s]",
            getClass ().getCanonicalName ());
        return false;
    }
}
