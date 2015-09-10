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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import au.com.breakpoint.hedron.core.GenericFactory;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.dao.DaoUtil.SqlData;
import au.com.breakpoint.hedron.core.dao.WhereElement.Operator;
import au.com.breakpoint.hedron.core.dao.sample.dao.BlackList;

public class DaoUtilTest
{
    @Test
    public void testAppendOrderBySqlClause ()
    {
        final String sql = DaoUtil.appendOrderBySqlClause (m_sqlFragment, m_columnPhysicalNames, m_orderByElements);
        assertTrue (sql.equals (m_sqlFragment + " order by Column0, Column1, Column2 desc"));
    }

    @Test
    public void testDumpCreateEntityCode ()
    {
        final BlackList e = new BlackList ();
        e.setAvcId ("columnAvcId");
        e.setActionId (1);

        @SuppressWarnings ("unused")
        final String s = DaoUtil.dumpCreateEntityCode (e, "e");
        //System.out.println (s);
    }

    @Test
    public void testGetWhereExpressionSqlData ()
    {
        final SqlData sd = DaoUtil.getWhereExpressionSqlData (m_sqlFragment, m_columnPhysicalNames, m_whereElements);
        assertTrue (sd.getSql ().equals (m_sqlFragment + " where Column0 = ? and Column1 = ? and Column2 > ?"));
    }

    @Test
    public void testToMap ()
    {
        final List<MockEntity> list =
            GenericFactory.newArrayList (new MockEntity ("a"), new MockEntity ("b"), new MockEntity ("c"));
        final Map<String, MockEntity> m = HcUtil.toMap (list);
        assertEquals ("a", m.get ("a").getPrimaryKey ());
        assertEquals ("b", m.get ("b").getPrimaryKey ());
        assertEquals ("c", m.get ("c").getPrimaryKey ());
        assertEquals (null, m.get ("d"));
    }

    //    public static <TDao extends IEntityDao<T, K>, T extends IEntity<K>, K>
    //    void updateEntities (final TDao dao, final List<T> entities)
    @Test
    public void testUpdateEntities ()
    {
        final List<MockEntity> list =
            GenericFactory.newArrayList (new MockEntity ("a"), new MockEntity ("b"), new MockEntity ("c"));
        new MockEntityDao ().update (list);
    }

    private static class MockEntity extends BaseEntity<String>
    {
        public MockEntity (final String s)
        {
            m_s = s;
        }

        @Override
        public void copyFrom (final IEntity<String> rhs)
        {
            m_s = ((MockEntity) rhs).m_s;
        }

        @Override
        public Object[] getColumnValues (final ColumnType columnType)
        {
            return new Object[]
            {
                    m_s
            };
        }

        @Override
        public String getPrimaryKey ()
        {
            return m_s;
        }

        private String m_s;

        private static final long serialVersionUID = 1L;
    }

    private static class MockEntityDao extends BaseEntityDao<MockEntity, String>
    {
        @Override
        public MockEntity newEntityInstance ()
        {
            return new MockEntity (null);
        }

        @Override
        public void update (final List<MockEntity> es)
        {
        }

        @Override
        public int update (final SetElement[] newValues, final WhereElement[] whereElements)
        {
            return 1;
        }

    }

    //public static <K, T extends IEntity<K>> Map<K, T> toMap (final List<? extends T> list)
    //{
    //    final Function<T, K> keyExtractor = DaoFunctionObject.fExtractPrimaryKey ();
    //    return HcUtil.toMap (list, keyExtractor);
    //}
    //
    //public static <TDao extends IEntityDao<T, K>, T extends IEntity<K>, K>
    //    void updateEntities (final TDao dao, final List<T> entities)
    //{
    //    for (final T e : entities)
    //    {
    //        dao.update (e);
    //    }
    //}

    private static final String[] m_columnPhysicalNames =
        {
                "Column0",
                "Column1",
                "Column2"
    };

    private static final OrderByElement[] m_orderByElements = new OrderByElement[]
    {
            new OrderByElement (0),
            new OrderByElement (1, true),
            new OrderByElement (2, false),
    };

    private static final String m_sqlFragment = "select Column0, Column1, Column2 from Blah";

    private static final WhereElement[] m_whereElements = new WhereElement[]
    {
            new WhereElement (0, 0),
            new WhereElement (1, Operator.Equal, 0),
            new WhereElement (2, Operator.GreaterThan, 0)
    };
}
