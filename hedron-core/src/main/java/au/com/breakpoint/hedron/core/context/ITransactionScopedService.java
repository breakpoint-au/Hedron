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
 * Basic signature of business services executed inside of a transaction scope.
 *
 * @param <TOutput>
 *            Type of the output data returned by the business service. By convention, use
 *            Void if no output data is required, and return null.
 */
public interface ITransactionScopedService<TOutput>
{
    /**
     * The business service method.
     *
     * @param scope
     *            The active transaction scope.
     * @return Output data returned by the business service. By convention, if no output
     *         data is required, return null.
     */
    TOutput execute (final ITransactionScope scope);
}
