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
 * final OrderBySql sql = new OrderBySql (SomeTable.Columns.SomeType).ascending () .then
 * (SomeTable.Columns.Id).descending ();
 *
 * List<SomeTable> l = new SomeTableDao (dataSource).fetch (sql);
 */
public class OrderBySql implements Serializable
{
    // TODO _ review fluent syntax
    public OrderBySql ()
    {
    }

    public OrderBySql (final int columnId)
    {
        then (columnId);
    }

    public OrderBySql ascending ()
    {
        final OrderByElement ob = getLastOrderByElement ();
        ob.setAscending (true);

        return this;
    }

    public OrderBySql descending ()
    {
        final OrderByElement ob = getLastOrderByElement ();
        ob.setAscending (false);

        return this;
    }

    public OrderByElement[] getOrderByElements ()
    {
        return m_orderByElements.toArray (new OrderByElement[m_orderByElements.size ()]);
    }

    // Order by clause support
    public OrderBySql then (final int columnId)
    {
        final OrderByElement ob = addOrderByElement ();
        ob.setColumnId (columnId);

        return this;
    }

    @Override
    public String toString ()
    {
        return HcUtil.toString (m_orderByElements);
    }

    private OrderByElement addOrderByElement ()
    {
        final OrderByElement sc = new OrderByElement ();
        m_orderByElements.add (sc);

        return sc;
    }

    private OrderByElement getLastOrderByElement ()
    {
        return m_orderByElements.get (m_orderByElements.size () - 1);
    }

    private final List<OrderByElement> m_orderByElements = newArrayList ();

    private static final long serialVersionUID = 7990434835781817954L;
}
