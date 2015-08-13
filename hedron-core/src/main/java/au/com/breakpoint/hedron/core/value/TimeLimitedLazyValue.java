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

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.Counter;

/**
 * Effectively immutable, although internal state recalculates when cached data expires.
 * Value evaluated as requested. Multiple requests result in it being evaluated only once;
 * subsequent requests retrieve the first value again. Threadsafe lock-free through
 * FutureTask.
 *
 * @param <T>
 */
public class TimeLimitedLazyValue<T> extends AbstractValue<T>
{
    public TimeLimitedLazyValue (final Class<?> usingClass, final String name, final Supplier<T> fetcher,
        final long lifetimeMsec)
    {
        m_lifetimeNs = HcUtil.msecToNs (lifetimeMsec);
        m_fetcher = fetcher;

        final Context context = new Context (new SafeLazyValue<> (fetcher), System.nanoTime () + m_lifetimeNs);
        m_context = new AtomicReference<> (context);

        // eg TimeLimitedLazyValue.cacheFetch.DataManager.mapData
        m_countSnapshotFetch =
            Counter.of (TimeLimitedLazyValue.class, HcUtil.qualifyName ("cacheFetch", usingClass, name));
        m_countSnapshotGet = Counter.of (TimeLimitedLazyValue.class, HcUtil.qualifyName ("cacheGet", usingClass, name));
    }

    @Override
    public T get ()
    {
        reviewContext ();

        final Context c = m_context.get ();
        final T value = c.m_lazyValue.get ();

        m_countSnapshotGet.increment ();

        return value;
    }

    private void reviewContext ()
    {
        // Lock-free implementation (optimistic 'locking' with retry).
        boolean done;
        boolean isStale;
        do
        {
            final Context prevContext = m_context.get ();

            final long nsExpiry = prevContext.m_systemTimeNsExpiry;
            isStale = !HcUtil.nsIsBefore (System.nanoTime (), nsExpiry);
            if (!isStale)
            {
                done = true;
            }
            else
            {
                // Chuck the previous LazyValue (FutureTask) so that it gets reevaluated.
                final Context newContext =
                    new Context (SafeLazyValue.of (m_fetcher), System.nanoTime () + m_lifetimeNs);

                done = m_context.compareAndSet (prevContext, newContext);
            }
        }
        while (!done);

        if (isStale)
        {
            m_countSnapshotFetch.increment ();
        }
    }

    /** Context data, immutable for concurrency reasons */
    public class Context
    {
        private Context (final SafeLazyValue<T> lazyValue, final long systemTimeNsExpiry)
        {
            m_lazyValue = lazyValue;
            m_systemTimeNsExpiry = systemTimeNsExpiry;
        }

        public final SafeLazyValue<T> m_lazyValue;

        public final long m_systemTimeNsExpiry;
    }

    public static <T> TimeLimitedLazyValue<T> of (final Class<?> usingClass, final String name,
        final Supplier<T> evaluator, final long lifetimeMsec)
    {
        return new TimeLimitedLazyValue<> (usingClass, name, evaluator, lifetimeMsec);
    }

    /** Gather context in lock-free / immutable manner for max concurrency */
    private final AtomicReference<Context> m_context;

    private final Counter m_countSnapshotFetch;

    private final Counter m_countSnapshotGet;

    private final Supplier<T> m_fetcher;

    private final long m_lifetimeNs;
}
