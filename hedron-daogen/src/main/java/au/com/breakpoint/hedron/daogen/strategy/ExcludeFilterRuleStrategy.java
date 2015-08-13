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
package au.com.breakpoint.hedron.daogen.strategy;

import java.util.List;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.daogen.BooleanHolder;
import au.com.breakpoint.hedron.daogen.Capability;

public class ExcludeFilterRuleStrategy implements IFilterRule
{
    public ExcludeFilterRuleStrategy (final String nameWildcard)
    {
        m_nameWildcard = nameWildcard;
    }

    @Override
    public boolean evaluateFilter (final String name, final BooleanHolder ok, final List<Capability> capabilities)
    {
        boolean matched = false;

        // Apply the filter only if there is a match.
        if (HcUtil.wildcardMatches (m_nameWildcard, name))
        {
            ok.setValue (false);		// exclude
            matched = true;
            //			System.out.printf ("      ExcludeFilterRuleStrategy: [%s] [%s] pass: %s\n", m_nameWildcard, name, ok);
        }

        return matched;
    }

    @Override
    public String toString ()
    {
        return String.format ("exclude / '%s'", m_nameWildcard);
    }

    private final String m_nameWildcard;
}
