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

import java.util.function.Consumer;
import au.com.breakpoint.hedron.core.JsonUtil;
import au.com.breakpoint.hedron.core.Tuple.E2;

/**
 * DatagramProcessor which deserialises the incoming message as a TRequest object and
 * despatches for handling.
 */
public class JsonDatagramProcessor<TRequest> extends DatagramProcessor
{
    public JsonDatagramProcessor (final Class<?> c, final int port, final int threads,
        final Consumer<E2<String, TRequest>> messageHandler, final Class<TRequest> classOfTRequest)
    {
        super (c, port, threads);

        m_messageHandler = messageHandler;
        m_classOfTRequest = classOfTRequest;
    }

    public JsonDatagramProcessor (final String name, final int port, final int threads,
        final Consumer<E2<String, TRequest>> messageHandler, final Class<TRequest> classOfTRequest)
    {
        super (name, port, threads);

        m_messageHandler = messageHandler;
        m_classOfTRequest = classOfTRequest;
    }

    @Override
    protected void onRequest (final String clientIpAddress, final byte[] data)
    {
        // The datagram bytes are raw json string.
        final String jsonString = new String (data);

        // Unmarshall back to the expected object type.
        final TRequest r = JsonUtil.fromJson (jsonString, m_classOfTRequest);

        // Pass to the handler.
        m_messageHandler.accept (E2.of (clientIpAddress, r));
    }

    /** Holds the class details of the request for Gson unmarshalling */
    private final Class<TRequest> m_classOfTRequest;

    /**
     * Handler for incoming datagrams. Takes IP address + deserialised request object. It
     * is called concurrently as soon as the socket receives the datagram.
     */
    private final Consumer<E2<String, TRequest>> m_messageHandler;
}
