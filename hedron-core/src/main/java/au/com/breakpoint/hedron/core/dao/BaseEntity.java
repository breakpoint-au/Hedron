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
import java.util.Arrays;
import au.com.breakpoint.hedron.core.HcUtil;

/**
 * Base class containing 'not implemented' versions of the IEntity methods where
 * appropriate.
 *
 * @param <TPrimaryKey>
 *            Type of the entity's primary key.
 */
public abstract class BaseEntity<TPrimaryKey> implements IEntity<TPrimaryKey>, Serializable
{
    @Override
    public boolean equals (final Object o)
    {
        boolean isEqual = false;

        if (this == o)
        {
            isEqual = true;
        }
        else if (o != null && getClass () == o.getClass ())
        {
            final BaseEntity<TPrimaryKey> eRhs = HcUtil.uncheckedCast (o);
            isEqual = Arrays.deepEquals (getColumnValues (ColumnType.All), eRhs.getColumnValues (ColumnType.All));
        }

        return isEqual;
    }

    @Override
    public int hashCode ()
    {
        // DAOs use byte[] for guids, so need to deep look inside arrays. Note: all columns
        // are used as hash values. When a class overrides equals, it must override
        // hashCode, and equals and hashCode must use the same set of fields.
        return Arrays.deepHashCode (getColumnValues (ColumnType.All));
    }

    public void setEnforceColumnLimits (final boolean enforceColumnLimits)
    {
        m_enforceColumnLimits = enforceColumnLimits;
    }

    public boolean shouldEnforceColumnLimits ()
    {
        return m_enforceColumnLimits;
    }

    @Override
    public String toString ()
    {
        return Arrays.deepToString (getColumnValues (ColumnType.All));
    }

    private transient boolean m_enforceColumnLimits = true;

    private static final long serialVersionUID = -738052437815749544L;
}
