//                       __________________________________
//                ______|      Copyright 2008-2015         |______
//                \     |     Breakpoint Pty Limited       |     /
//                 \    |   http://www.breakpoint.com.au   |    /
//                 /    |__________________________________|    \
//                /_________/                          \_________\
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
// except in compliance with the License. You may obtain a copy of the License at
//	   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
// either express or implied. See the License for the specific language governing
// permissions and limitations under the License.
//
package au.com.breakpoint.hedron.core.dao;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import au.com.breakpoint.hedron.core.dao.WhereElement;
import au.com.breakpoint.hedron.core.dao.WhereSql;
import au.com.breakpoint.hedron.core.dao.WhereElement.Operator;

public class WhereSqlTest
{
    @Test
    public void testWhereSql ()
    {
        final WhereSql sql = new WhereSql (2).equal ("asdf").and (3).notEqual (1).and (4).like ("Leigh%").and (5)
            .greaterThan (1).and (6).greaterThanOrEqual (1).and (7).lessThan (1).and (8).lessThanOrEqual (1);

        final WhereElement[] wes = sql.getWhereElements ();
        assertTrue (wes.length == 7);
        assertTrue (wes[0].getColumnId () == 2);
        assertTrue (wes[0].getOperator () == Operator.Equal);
        assertTrue (wes[0].getValue ().equals ("asdf"));
        assertTrue (wes[1].getColumnId () == 3);
        assertTrue (wes[1].getOperator () == Operator.NotEqual);
        assertTrue (((Integer) wes[1].getValue ()) == 1);
        assertTrue (wes[2].getColumnId () == 4);
        assertTrue (wes[2].getOperator () == Operator.Like);
        assertTrue (wes[2].getValue ().equals ("Leigh%"));
        assertTrue (wes[3].getColumnId () == 5);
        assertTrue (wes[3].getOperator () == Operator.GreaterThan);
        assertTrue (((Integer) wes[3].getValue ()) == 1);
        assertTrue (wes[4].getColumnId () == 6);
        assertTrue (wes[4].getOperator () == Operator.GreaterThanOrEqual);
        assertTrue (((Integer) wes[4].getValue ()) == 1);
        assertTrue (wes[5].getColumnId () == 7);
        assertTrue (wes[5].getOperator () == Operator.LessThan);
        assertTrue (((Integer) wes[5].getValue ()) == 1);
        assertTrue (wes[6].getColumnId () == 8);
        assertTrue (wes[6].getOperator () == Operator.LessThanOrEqual);
        assertTrue (((Integer) wes[6].getValue ()) == 1);
    }
}
