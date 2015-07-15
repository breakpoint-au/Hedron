//                       __________________________________
//                ______|         Copyright 2008           |______
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
package au.com.breakpoint.hedron.core;

import au.com.breakpoint.hedron.core.context.ThreadContext;

/**
 * Models the context and outcome of the exection of TimedScope.execute ().
 *
 * @param <T>
 *            The type of the output data returned from the scope execution.
 */
public class ScopeOutcome<T>
{
    public ScopeOutcome (final String name)
    {
        m_name = name;
    }

    public boolean didExpire ()
    {
        return m_expired;
    }

    public boolean didSucceed ()
    {
        return m_succeeded;
    }

    public Throwable getCaughtException ()
    {
        return m_caughtException;
    }

    public String getContextId ()
    {
        return m_contextId;
    }

    public long getExpiredMsec ()
    {
        return m_expiredMsec;
    }

    public long getMsecLimit ()
    {
        return m_msecLimit;
    }

    public String getName ()
    {
        return m_name;
    }

    public long getNsDuration ()
    {
        return m_nsDuration;
    }

    public long getNsStart ()
    {
        return m_nsStart;
    }

    public T getReturnedValue ()
    {
        return m_returnedValue;
    }

    public void setCaughtException (final Throwable e)
    {
        m_caughtException = e;
    }

    public void setExpiryDetails (final long nsElapsed)
    {
        m_expired = true;
        m_expiredMsec = HcUtil.nsToMsec (nsElapsed);
    }

    public void setMsecLimit (final long msecLimit)
    {
        m_msecLimit = msecLimit;
    }

    public void setNsDuration (final long nsDuration)
    {
        m_nsDuration = nsDuration;
    }

    public void setNsStart (final long nsStart)
    {
        m_nsStart = nsStart;
    }

    public void setReturnedValue (final T returnedValue)
    {
        m_succeeded = true;
        m_returnedValue = returnedValue;
    }

    private volatile Throwable m_caughtException;

    /**
     * Unique string to identify operation so that it can reported on expiry/completion
     * and tracked.
     */
    private final String m_contextId = ThreadContext.getContextId ();

    /** Timeout info */
    private volatile boolean m_expired;// volatile : updated by timer thread

    private volatile long m_expiredMsec;// volatile : updated by timer thread

    private long m_msecLimit;

    /** Name of the task being timed */
    private final String m_name;

    private volatile long m_nsDuration;

    private volatile long m_nsStart;

    /**
     * Value returned by the task being timed. NB: may be null when successfully executed.
     * Check m_succeeded to determine success status.
     */
    private volatile T m_returnedValue;

    /** Indicates success of the task being timed. */
    private volatile boolean m_succeeded;
}
