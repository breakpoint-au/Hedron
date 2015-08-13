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
package au.com.breakpoint.hedron.core.value;

/**
 * Not threadsafe. Provides lazy string formatting using toString ().
 */
public class ToStringValue extends AbstractValue<String>
{
    public ToStringValue (final Object object)
    {
        m_object = object;
    }

    @Override
    public String get ()
    {
        // Calculate the value if not already done. Does nothing if already calculated.
        if (m_value == null)
        {
            m_value = m_object.toString ();
        }

        return m_value;
    }

    private final Object m_object;

    private String m_value;
}
