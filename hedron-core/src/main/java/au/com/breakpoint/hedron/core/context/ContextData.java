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

import java.util.ArrayDeque;
import java.util.Deque;

/** A set of data associated with the thread's context */
class ContextData
{
    /**
     * Counter used to track nested logging operations in order to prevent logging of
     * exceptions encountered within logging operations, since this can cause an infinite
     * loop.
     */
    int m_loggingOperationDepth;

    /**
     * Counter used to track nested logging operations in order to prevent logging of
     * exceptions encountered within logging operations, since this can cause an infinite
     * loop.
     */
    int m_loggingSilenceDepth;

    /** Support thread naming for JStack visibility. Mutable for unit testing */
    String m_originalThreadName = Thread.currentThread ().getName ();

    /** A stack representing the current set of nested scopes */
    final Deque<NestedScopeData> m_scopeNesting = new ArrayDeque<> ();
}
