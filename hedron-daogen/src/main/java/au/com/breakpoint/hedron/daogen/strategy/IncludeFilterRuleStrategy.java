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
package au.com.breakpoint.hedron.daogen.strategy;

import java.util.List;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.GenericFactory;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.daogen.BooleanHolder;
import au.com.breakpoint.hedron.daogen.Capability;

public class IncludeFilterRuleStrategy implements IFilterRule
{
    public IncludeFilterRuleStrategy (final String nameWildcard, final String capabilities)
    {
        m_nameWildcard = nameWildcard;
        m_capabilities = parseCapabilitiesString (capabilities);
    }

    @Override
    public boolean evaluateFilter (final String name, final BooleanHolder ok, final List<Capability> capabilities)
    {
        boolean matched = false;

        // Apply the filter only if there is a match.
        if (HcUtil.wildcardMatches (m_nameWildcard, name))
        {
            ok.setValue (true);		// include

            // Replace the capabilities defined by more general
            capabilities.clear ();
            capabilities.addAll (m_capabilities);
            matched = true;
        }

        return matched;
    }

    public boolean hasCapability (final Capability c)
    {
        return m_capabilities.contains (c);
    }

    @Override
    public String toString ()
    {
        return String.format ("include / '%s'", m_nameWildcard);
    }

    private static List<Capability> parseCapabilitiesString (final String capabilities)
    {
        final List<Capability> l = GenericFactory.newArrayList ();
        final int length = capabilities.length ();
        for (int i = 0; i < length; ++i)
        {
            final String s = capabilities.substring (i, i + 1);
            final Capability c = Capability.fromString (s);
            ThreadContext.assertError (c != null, "Unsupported capability [%s]", s);

            l.add (c);
        }

        return l;
    }

    private final List<Capability> m_capabilities;

    private final String m_nameWildcard;
}
