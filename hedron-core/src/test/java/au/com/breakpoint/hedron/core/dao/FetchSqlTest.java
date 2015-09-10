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
import au.com.breakpoint.hedron.core.dao.WhereElement.Operator;
import au.com.breakpoint.hedron.core.dao.sample.dao.BlackList;

public class FetchSqlTest
{
    @Test
    public void testFetchSql ()
    {
        final FetchSql<BlackList> sql = //
            new FetchSql<> (BlackList.Column.AvcId).equal ("asdf") //
                .and (BlackList.Column.DateRequested).notEqual (1) //
                .and (BlackList.Column.OperatorId).like ("Leigh%") //
                .and (BlackList.Column.ReferenceId).greaterThan (1) //
                .and (BlackList.Column.ActionId).greaterThanOrEqual (1) //
                .orderBy (BlackList.Column.AvcId).ascending () //
                .then (BlackList.Column.DateRequested).descending () //
                .then (BlackList.Column.OperatorId).ascending ();

        final WhereElement[] wes = sql.getWhereElements ();
        assertTrue (wes.length == 5);
        assertTrue (wes[0].getColumnId () == 0);
        assertTrue (wes[0].getOperator () == Operator.Equal);
        assertTrue (wes[0].getValue ().equals ("asdf"));
        assertTrue (wes[1].getColumnId () == 1);
        assertTrue (wes[1].getOperator () == Operator.NotEqual);
        assertTrue (((Integer) wes[1].getValue ()) == 1);
        assertTrue (wes[2].getColumnId () == 3);
        assertTrue (wes[2].getOperator () == Operator.Like);
        assertTrue (wes[2].getValue ().equals ("Leigh%"));
        assertTrue (wes[3].getColumnId () == 4);
        assertTrue (wes[3].getOperator () == Operator.GreaterThan);
        assertTrue (((Integer) wes[3].getValue ()) == 1);
        assertTrue (wes[4].getColumnId () == 5);
        assertTrue (wes[4].getOperator () == Operator.GreaterThanOrEqual);
        assertTrue (((Integer) wes[4].getValue ()) == 1);

        final OrderByElement[] es = sql.getOrderByElements ();
        assertTrue (es.length == 3);
        assertTrue (es[0].getColumnId () == 0);
        assertTrue (es[0].isAscending ());
        assertTrue (es[1].getColumnId () == 1);
        assertTrue (!es[1].isAscending ());
        assertTrue (es[2].getColumnId () == 3);
        assertTrue (es[2].isAscending ());
    }
}
