//                       __________________________________
//                ______|         Copyright 2008           |______
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
package au.com.breakpoint.hedron.core.context;

/**
 * Aggregation of interfaces for transactional context objects, implementing IScope for
 * scope management, IDataSourceRepository for caching of open database connections
 * between Spring JDBC actions, and ITransactionOutcome for controlling the outcome of
 * transactions (ie commit or rollback the transaction associated with the current scope).
 */
public interface ITransactionScope extends IScope, IDataSourceRepository, ITransactionOutcome
{
}
