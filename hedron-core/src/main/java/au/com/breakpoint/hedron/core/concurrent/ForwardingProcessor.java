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

/**
 * Forwarding implementation as per Effective Java 2nd Edition, Item 16: Favour
 * composition over inheritance.
 */
public class ForwardingProcessor implements IProcessor
{
    public ForwardingProcessor (final IProcessor processor)
    {
        m_processor = processor;
    }

    @Override
    public void awaitShutdownComplete ()
    {
        m_processor.awaitShutdownComplete ();
    }

    public IProcessor getProcessor ()
    {
        return m_processor;
    }

    @Override
    public void processUntilShutdown ()
    {
        m_processor.processUntilShutdown ();
    }

    @Override
    public void signalShutdown ()
    {
        m_processor.signalShutdown ();
    }

    @Override
    public String toString ()
    {
        return m_processor.toString ();
    }

    private final IProcessor m_processor;
}
