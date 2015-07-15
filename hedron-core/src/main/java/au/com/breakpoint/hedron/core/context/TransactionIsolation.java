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
package au.com.breakpoint.hedron.core.context;

import java.sql.Connection;

/**
 * Transaction isolation level values. These map to the
 * Connection.TRANSACTION_READ_COMMITTED style values. Data only valid if
 * m_isTransactionRequired == true.
 */
public enum TransactionIsolation
{
    ReadCommitted (Connection.TRANSACTION_READ_COMMITTED),
    ReadUncommitted (Connection.TRANSACTION_READ_UNCOMMITTED),
    RepeatableRead (Connection.TRANSACTION_REPEATABLE_READ),
    Serialisable (Connection.TRANSACTION_SERIALIZABLE);

    private TransactionIsolation (final int jdbcValue)
    {
        m_jdbcValue = jdbcValue;
    }

    public int getJdbcValue ()
    {
        return m_jdbcValue;
    }

    private final int m_jdbcValue;
}