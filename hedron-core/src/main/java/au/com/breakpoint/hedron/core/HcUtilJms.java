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
package au.com.breakpoint.hedron.core;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import au.com.breakpoint.hedron.core.context.ThreadContext;

public class HcUtilJms
{
    public static void safeClose (final Connection connection)
    {
        if (connection != null)
        {
            try
            {
                connection.close ();
            }
            catch (final JMSException e)
            {
                // Propagate exception as unchecked fault up to the fault barrier.
                ThreadContext.throwFault (e);
            }
        }
    }

    public static void safeClose (final Connection connection, final Session session, final MessageConsumer consumer)
    {
        safeClose (consumer);
        safeClose (session);
        safeClose (connection);
    }

    public static void safeClose (final Connection connection, final Session session, final MessageProducer producer)
    {
        safeClose (producer);
        safeClose (session);
        safeClose (connection);
    }

    public static void safeClose (final MessageConsumer consumer)
    {
        if (consumer != null)
        {
            try
            {
                consumer.close ();
            }
            catch (final JMSException e)
            {
                // Propagate exception as unchecked fault up to the fault barrier.
                ThreadContext.throwFault (e);
            }
        }
    }

    public static void safeClose (final MessageProducer producer)
    {
        if (producer != null)
        {
            try
            {
                producer.close ();
            }
            catch (final JMSException e)
            {
                // Propagate exception as unchecked fault up to the fault barrier.
                ThreadContext.throwFault (e);
            }
        }
    }

    public static void safeClose (final Session session)
    {
        if (session != null)
        {
            try
            {
                session.close ();
            }
            catch (final JMSException e)
            {
                // Propagate exception as unchecked fault up to the fault barrier.
                ThreadContext.throwFault (e);
            }
        }
    }

    public static void silentClose (final Connection connection)
    {
        if (connection != null)
        {
            try
            {
                connection.close ();
            }
            catch (final JMSException e)
            {
                // Propagate exception as unchecked fault up to the fault barrier.
                ThreadContext.throwFault (e);
            }
        }
    }

    public static void silentClose (final Connection connection, final Session session, final MessageConsumer consumer)
    {
        silentClose (consumer);
        silentClose (session);
        silentClose (connection);
    }

    public static void silentClose (final Connection connection, final Session session, final MessageProducer producer)
    {
        silentClose (producer);
        silentClose (session);
        silentClose (connection);
    }

    public static void silentClose (final MessageConsumer consumer)
    {
        if (consumer != null)
        {
            try
            {
                consumer.close ();
            }
            catch (final JMSException e)
            {
                // Swallow this exception.
            }
        }
    }

    public static void silentClose (final MessageProducer producer)
    {
        if (producer != null)
        {
            try
            {
                producer.close ();
            }
            catch (final JMSException e)
            {
                // Swallow this exception.
            }
        }
    }

    public static void silentClose (final Session session)
    {
        if (session != null)
        {
            try
            {
                session.close ();
            }
            catch (final JMSException e)
            {
                // Swallow this exception.
            }
        }
    }
}
