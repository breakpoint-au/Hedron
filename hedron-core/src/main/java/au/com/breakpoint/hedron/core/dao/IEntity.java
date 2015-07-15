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

import au.com.breakpoint.hedron.core.IIdentifiable;
import au.com.breakpoint.hedron.core.context.ThreadContext;

/**
 * Interface to database table/view entities.
 *
 * @param <TPrimaryKey>
 *            Type of the primary key, eg Integer or String. If the primary key is
 *            compound, a tuple is used, eg E2<Integer, String>.
 */
public interface IEntity<TPrimaryKey> extends IIdentifiable<TPrimaryKey>
{
    /**
     * Copy the contents of another entity of the same type.
     *
     * @param rhs
     *            the other entity ('right hand side' of the assignment lhs = rhs)
     */
    void copyFrom (final IEntity<TPrimaryKey> rhs);

    /**
     * Gets all the column values as an object array. Useful for batch DAO operations etc.
     *
     * @return all the column values as an object array
     */
    Object[] getColumnValues (final ColumnType columnType);

    /** Default fail implementation for IEntity */
    @Override
    default TPrimaryKey getPrimaryKey ()
    {
        ThreadContext.assertError (false, "getPrimaryKey () is not implemented for class [%s]",
            getClass ().getCanonicalName ());
        return null;
    }

    public enum ColumnType
    {
        /** All columns */
        All,

        /** All columns except identity columns */
        NonIdentity,

        /** Only the primary key values */
        PrimaryKey,

        /** Columns that are not primary key or identity, so can be updated */
        Updatable
    }
}
