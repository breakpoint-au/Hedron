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
package au.com.breakpoint.hedron.core.value;

import java.util.concurrent.FutureTask;
import java.util.function.Supplier;
import au.com.breakpoint.hedron.core.HcUtil;

/**
 * Immutable. Value evaluated as requested. Multiple requests result in it being evaluated
 * only once; subsequent requests retrieve the first value again. Threadsafe lock-free
 * through FutureTask.
 *
 * @param <T>
 */
public class SafeLazyValue<T> extends AbstractValue<T>
{
    public SafeLazyValue (final Supplier<? extends T> fetcher)
    {
        m_futureTask = new FutureTask<T> (fetcher::get);
    }

    @Override
    public T get ()
    {
        // Calculate the value if not already done. Does nothing if already
        // calculated.
        m_futureTask.run ();

        return HcUtil.waitForFuture (m_futureTask);
    }

    public static <T> SafeLazyValue<T> of (final Supplier<? extends T> fetcher)
    {
        return new SafeLazyValue<T> (fetcher);
    }

    /** Threadsafe lock-free Future<T> implementation */
    private final FutureTask<T> m_futureTask;
}
