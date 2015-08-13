//                       __________________________________
//                ______|      Copyright 2008-2015         |______
//                \     |     Breakpoint Pty Limited       |     /
//                 \    |   http://www.breakpoint.com.au   |    /
//                 /    |__________________________________|    \
//                /_________/                          \_________\
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
// except in compliance with the License. You may obtain a copy of the License at
//	   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
// either express or implied. See the License for the specific language governing
// permissions and limitations under the License.
//
package au.com.breakpoint.hedron.core.context;

/**
 * Derives from RuntimeException so is unchecked. Wraps another exception keeping track of
 * whether it has been logged already.
 */
public class ThreadContextException extends RuntimeException
{
    public ThreadContextException (final String s, final boolean logged)
    {
        super (s);
        m_logged = logged;
    }

    public ThreadContextException (final Throwable e)
    {
        super (e);

        // Handle wrapping of another LogOnceException or subclass.
        m_logged = ThreadContext.hasBeenLogged (e);
    }

    public ThreadContextException (final Throwable e, final boolean logged)
    {
        super (e);
        m_logged = logged;
    }

    public boolean isLogged ()
    {
        return m_logged;
    }

    public void setLogged (final boolean logged)
    {
        m_logged = logged;
    }

    private boolean m_logged;

    private static final long serialVersionUID = -3445827352328358189L;
}
