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

import au.com.breakpoint.hedron.core.HcUtil;

/**
 * Forwarding implementation as per Effective Java 2nd Edition, Item 16: Favour
 * composition over inheritance.
 */
public class ForwardingProcessorInit<T> extends ForwardingProcessor implements IProcessorInit<T>
{
    public ForwardingProcessorInit (final IProcessorInit<T> processor)
    {
        super (processor);
    }

    public T getContext ()
    {
        return m_context;
    }

    @Override
    public void initialise (final T context)
    {
        m_context = context;

        final IProcessorInit<T> p = HcUtil.uncheckedCast (getProcessor ());

        p.initialise (context);
    }

    private volatile T m_context;
}
