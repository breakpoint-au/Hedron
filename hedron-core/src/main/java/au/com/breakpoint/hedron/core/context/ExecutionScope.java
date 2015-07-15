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
 * ExecutionScope is an implementation of IScope (@see IScope for overview).
 *
 * The outermostExecutionScope sees that it is not nested within another IScope, and
 * allocates a request id that is included on all logging during the execution of the code
 * in the try block, including nested ExecutionScope blocks. ExecutionScope keeps track of
 * the scope nesting, and maintains the same request context across them until the outer
 * scope is closed, so logging entries will consistently show the overall request context
 * identifier.
 *
 * This capability yields dividends in a transactional service environment. That is, where
 * an outer level software layer such as web tier calls nested transactional services. See
 * ITransactionScope and its subclasses.
 */
public class ExecutionScope implements IScope
{
    public ExecutionScope ()
    {
        this (null);
    }

    public ExecutionScope (final String name)
    {
        // Add this IScope to the stack of nested IScopes.
        ThreadContext.addIScope (this, name);
    }

    @Override
    public void close ()
    {
        // Remove this IScope from the stack of nested IScopes.
        ThreadContext.removeIScope (this);
    }
}
