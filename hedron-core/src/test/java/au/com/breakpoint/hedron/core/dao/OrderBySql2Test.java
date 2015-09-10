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

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import au.com.breakpoint.hedron.core.dao.sample.dao.BlackList;

public class OrderBySql2Test
{
    @Test
    public void testOrderBySql ()
    {
        final OrderBySql<BlackList> sql = //
            new OrderBySql<BlackList> (BlackList.Columns.AvcId).ascending () //
                .then (BlackList.Columns.DateRequested).descending () //
                .then (BlackList.Columns.Reason).ascending ();

        final OrderByElement[] es = sql.getOrderByElements ();
        assertTrue (es.length == 3);
        assertTrue (es[0].getColumnId () == 0);
        assertTrue (es[0].isAscending ());
        assertTrue (es[1].getColumnId () == 1);
        assertTrue (!es[1].isAscending ());
        assertTrue (es[2].getColumnId () == 2);
        assertTrue (es[2].isAscending ());
    }
}
