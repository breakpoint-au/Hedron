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
package au.com.breakpoint.hedron.daogen;

public enum Capability
{
    CREATE ("C"), DELETE ("D"), READ ("R"), UPDATE ("U");

    private Capability (final String stringValue)
    {
        m_stringValue = stringValue;
    }

    public String getStringValue ()
    {
        return m_stringValue;
    }

    public static Capability fromString (final String s)
    {
        Capability e = null;

        final Object enumValues[] = Capability.class.getEnumConstants ();

        for (int i = 0; e == null && i < enumValues.length; ++i)
        {
            final Capability o = (Capability) enumValues[i];

            if (o.getStringValue ().equals (s))
            {
                e = o;
            }
        }

        return e;
    }

    private final String m_stringValue;
}
