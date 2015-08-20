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
import au.com.breakpoint.hedron.core.HcUtil;

public class WhereElement implements Serializable
{
    public WhereElement ()
    {
    }

    public WhereElement (final int columnId, final Object value)
    {
        this (columnId, Operator.Equal, value);
    }

    public WhereElement (final int columnId, final Operator operator, final Object value)
    {
        m_columnId = columnId;
        m_operator = operator;
        m_value = value;
    }

    public String getClauseSql ()
    {
        return m_operator.getSymbol ();
    }

    public int getColumnId ()
    {
        return m_columnId;
    }

    public Operator getOperator ()
    {
        return m_operator;
    }

    public Object getValue ()
    {
        return m_value;
    }

    public void setColumnId (final int columnId)
    {
        m_columnId = columnId;
    }

    public void setOperator (final Operator operator)
    {
        m_operator = operator;
    }

    public void setValue (final Object value)
    {
        m_value = value;
    }

    @Override
    public String toString ()
    {
        return HcUtil.toString (m_columnId, m_operator, m_value);
    }

    public enum Operator
    {
        Equal ("="),
        GreaterThan (">"),
        GreaterThanOrEqual (">="),
        LessThan ("<"),
        LessThanOrEqual ("<="),
        Like ("like"),
        NotEqual ("<>");

        private Operator (final String symbol)
        {
            m_symbol = symbol;
        }

        public String getSymbol ()
        {
            return m_symbol;
        }

        private final String m_symbol;
    };

    private int m_columnId;

    private Operator m_operator;

    private Object m_value;

    private static final long serialVersionUID = 7990434835781817954L;
}
