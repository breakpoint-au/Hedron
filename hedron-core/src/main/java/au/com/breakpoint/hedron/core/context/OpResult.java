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
package au.com.breakpoint.hedron.core.context;

import java.io.Serializable;

public class OpResult implements Serializable
{
    public OpResult ()
    {
    }

    public OpResult (final Severity severity, final String description)
    {
        m_severity = severity;
        m_description = description;
    }

    public String getDescription ()
    {
        return m_description;
    }

    public Severity getSeverity ()
    {
        return m_severity;
    }

    public void setDescription (final String description)
    {
        m_description = description;
    }

    public void setSeverity (final Severity severity)
    {
        m_severity = severity;
    }

    public enum Severity
    {
        Error, Fault, Information, Warning
    }

    private String m_description;

    private Severity m_severity;

    private static final long serialVersionUID = 5108121386251308075L;
}
