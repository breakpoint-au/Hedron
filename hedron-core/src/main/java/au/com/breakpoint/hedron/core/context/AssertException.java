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
 * Derives from RuntimeException so is unchecked.
 */
public class AssertException extends ThreadContextException
{
    public AssertException (final OpResult.Severity severity, final String s, final boolean beenLogged)
    {
        super (s, beenLogged);
        m_severity = severity;
    }

    public OpResult.Severity getSeverity ()
    {
        return m_severity;
    }

    private final OpResult.Severity m_severity;

    private static final long serialVersionUID = -8027063802540492208L;
}
