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
package au.com.breakpoint.hedron.core.concurrent;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.util.function.Consumer;
import au.com.breakpoint.hedron.core.JsonUtil;
import au.com.breakpoint.hedron.core.Tuple.E2;
import au.com.breakpoint.hedron.core.context.ThreadContext;

/**
 * DatagramProcessor which deserialises the incoming message as a TRequest object and
 * despatches for handling.
 */
public class JsonDatagramProcessor<TRequest> extends DatagramProcessor
{
    public JsonDatagramProcessor (final Class<?> c, final int port, final int threads,
        final Class<TRequest> classOfTRequest)
    {
        super (c, port, threads);

        m_classOfTRequest = classOfTRequest;
    }

    public JsonDatagramProcessor (final String name, final int port, final int threads,
        final Class<TRequest> classOfTRequest)
    {
        super (name, port, threads);

        m_classOfTRequest = classOfTRequest;
    }

    @Override
    protected void onRequest (final String clientIpAddress, final DatagramPacket packet)
    {
        final byte[] data = packet.getData ();
        final int length = packet.getLength ();

        // The datagram bytes are raw json string.
        String jsonString = null;
        try
        {
            jsonString = new String (data, 0, length, "UTF-8");
        }
        catch (final UnsupportedEncodingException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }

        // Unmarshall back to the expected object type.
        final TRequest r = JsonUtil.fromJson (jsonString, m_classOfTRequest);

        // Pass to the handler.
        m_messageHandler.accept (E2.of (clientIpAddress, r));
    }

    protected void setMessageHandler (final Consumer<E2<String, TRequest>> messageHandler)
    {
        m_messageHandler = messageHandler;
    }

    /** Holds the class details of the request for Gson unmarshalling */
    private final Class<TRequest> m_classOfTRequest;

    /** Handler for incoming datagrams. Takes IP address + deserialised request object */
    private Consumer<E2<String, TRequest>> m_messageHandler;
}
