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

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import au.com.breakpoint.hedron.core.context.ThreadContext;

public class DatagramClient implements AutoCloseable// AutoCloseable allows Java 7 try-with-resources
{
    public DatagramClient (final String hostName, final int port)
    {
        m_port = port;
        try
        {
            m_socket = new DatagramSocket ();
            m_address = InetAddress.getByName (hostName);
        }
        catch (final SocketException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }
        catch (final UnknownHostException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }
    }

    @Override
    public void close ()
    {
        if (m_socket != null)
        {
            m_socket.close ();
        }
    }

    public void send (final byte[] req)
    {
        final DatagramPacket packet = new DatagramPacket (req, req.length, m_address, m_port);

        try
        {
            m_socket.send (packet);
        }
        catch (final IOException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }
    }

    public void send (final Serializable o)
    {
        // Can't use ObjectOutputStream since the is sent as a data packet.
        send (HcUtilFile.serialiseObjectAsBytes (o));
    }

    public void send (final String s)
    {
        send (HcUtil.getNullTerminatedBytes (s));
    }

    private InetAddress m_address;

    private final int m_port;

    private DatagramSocket m_socket;
}
