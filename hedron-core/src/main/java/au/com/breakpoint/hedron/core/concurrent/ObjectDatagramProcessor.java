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

import java.net.DatagramPacket;
import java.util.function.Consumer;
import au.com.breakpoint.hedron.core.HcUtilFile;
import au.com.breakpoint.hedron.core.Tuple.E2;

/**
 * DatagramProcessor which deserialises the incoming message as a TRequest object and
 * despatches for handling.
 */
public class ObjectDatagramProcessor<TRequest> extends DatagramProcessor
{
    public ObjectDatagramProcessor (final Class<?> c, final int port, final int threads,
        final Consumer<E2<String, TRequest>> messageHandler)
    {
        super (c, port, threads);
        m_messageHandler = messageHandler;
    }

    public ObjectDatagramProcessor (final String name, final int port, final int threads,
        final Consumer<E2<String, TRequest>> messageHandler)
    {
        super (name, port, threads);
        m_messageHandler = messageHandler;
    }

    @Override
    protected void onRequest (final String clientIpAddress, final DatagramPacket packet)
    {
        final byte[] data = packet.getData ();

        // The datagram bytes are raw bytes of a serialised object. Unmarshall back to the
        // expected object type.
        final TRequest r = HcUtilFile.deserialiseBytesAsObject (data);

        // Pass to the handler.
        m_messageHandler.accept (E2.of (clientIpAddress, r));
    }

    /**
     * Handler for incoming datagrams. Takes IP address + deserialised request object. It
     * is called concurrently as soon as the socket receives the datagram.
     */
    private final Consumer<E2<String, TRequest>> m_messageHandler;
}
