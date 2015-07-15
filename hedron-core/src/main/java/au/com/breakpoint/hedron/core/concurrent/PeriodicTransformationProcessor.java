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
package au.com.breakpoint.hedron.core.concurrent;

import java.util.List;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.IDataPersistence;
import au.com.breakpoint.hedron.core.IDataTransformation;
import au.com.breakpoint.hedron.core.ITransformationQueue;
import au.com.breakpoint.hedron.core.NullDataTransformation;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.core.log.Logging;
import au.com.breakpoint.hedron.core.value.LazyValue;

/**
 * Periodically performs the 3 part process (poll:IDataTransformationQueue,
 * transform:IDataTransformation, persist:IDataPersistence). Uses ForwardingProcessor for
 * composition of PeriodicTransformationProcessor rather than inheritance, as per
 * Effective Java 2nd Edition, Item 16: Favour composition over inheritance.
 *
 * @param <C>
 *            type of the data to be transformed
 * @param <T>
 *            type of the data to be transformed into
 */
public class PeriodicTransformationProcessor<C, T> extends ForwardingProcessor
{
    public PeriodicTransformationProcessor (final String name, final ITransformationQueue<C> pendingPoller,
        final IDataTransformation<C, T> dataTransformation, final IDataPersistence<T> persister, final int periodMsec)
    {
        super (new PeriodicProcessor (name, periodMsec));

        m_pendingQueue = pendingPoller;
        m_dataTransformation = dataTransformation;
        m_persister = persister;

        ((PeriodicProcessor) getProcessor ()).setTask (this::handleProcessing);
    }

    public IDataTransformation<C, T> getDataTransformation ()
    {
        return m_dataTransformation;
    }

    public ITransformationQueue<C> getPendingQueue ()
    {
        return m_pendingQueue;
    }

    public IDataPersistence<T> getPersister ()
    {
        return m_persister;
    }

    public void handleProcessing ()
    {
        // Get input data to be handled.
        final List<C> listPending = m_pendingQueue.getDataToTransform ();
        ThreadContext.assertFaultNotNull (listPending);

        final int size = listPending.size ();
        if (size > 0)
        {
            Logging.logDebug ("PeriodicTransformationProcessor poll returned %s records to handle", size);

            for (final C c : listPending)
            {
                Logging.logDebug ("Handling transformation for data [%s]",
                    LazyValue.of ( () -> HcUtil.abbreviate (c.toString (), 500)));

                final T d = m_dataTransformation.getValue (c);
                if (d == null)
                {
                    Logging.logInfo ("No related data transformation for input data [%s]", c);
                }
                else
                {
                    m_persister.persist (d);
                }
            }
        }
    }

    public static <C, T> PeriodicTransformationProcessor<C, T> of (final String name,
        final ITransformationQueue<C> pendingPoller, final IDataTransformation<C, T> dataTransformation,
        final IDataPersistence<T> persister, final int periodMsec)
    {
        return new PeriodicTransformationProcessor<C, T> (name, pendingPoller, dataTransformation, persister,
            periodMsec);
    }

    public static <T> PeriodicTransformationProcessor<T, T> of (final String name,
        final ITransformationQueue<T> pendingPoller, final IDataPersistence<T> persister, final int periodMsec)
    {
        final IDataTransformation<T, T> dt = NullDataTransformation.<T> of ();
        return new PeriodicTransformationProcessor<T, T> (name, pendingPoller, dt, persister, periodMsec);
    }

    private final IDataTransformation<C, T> m_dataTransformation;

    private final ITransformationQueue<C> m_pendingQueue;

    private final IDataPersistence<T> m_persister;
}
