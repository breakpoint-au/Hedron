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

public class SetSqlTest
{
    @Test
    public void testSetSql ()
    {
        final SetSql<BlackList> sql = //
            new SetSql<> (BlackList.Column.AvcId).set ("asdf") //
                .and (BlackList.Column.DateRequested).set (15);

        final SetElement[] es = sql.getSetElements ();
        assertTrue (es.length == 2);
        assertTrue (es[0].getColumnId () == 0);
        assertTrue (es[0].getValue ().equals ("asdf"));
        assertTrue (es[1].getColumnId () == 1);
        assertTrue (((Integer) es[1].getValue ()) == 15);
    }
}
