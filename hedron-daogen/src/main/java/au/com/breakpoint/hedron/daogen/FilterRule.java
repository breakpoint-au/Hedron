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
package au.com.breakpoint.hedron.daogen;

import java.util.List;
import org.w3c.dom.Node;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.daogen.Util.EnumString;
import au.com.breakpoint.hedron.daogen.strategy.ExcludeFilterRuleStrategy;
import au.com.breakpoint.hedron.daogen.strategy.IFilterRule;
import au.com.breakpoint.hedron.daogen.strategy.IncludeFilterRuleStrategy;

public class FilterRule implements IFilterRule
{
    public FilterRule (final Node node)
    {
        // First get the attributes of our own node.
        getAttributes (node);
    }

    @Override
    public boolean evaluateFilter (final String name, final BooleanHolder ok, final List<Capability> capabilities)
    {
        // Delegate to strategy implementation.
        return m_filterRuleStrategy.evaluateFilter (name, ok, capabilities);
    }

    @Override
    public String toString ()
    {
        return m_filterRuleStrategy.toString ();
    }

    private void getAttributes (final Node node)
    {
        final AttributeSet attributeSet = new AttributeSet (node);

        m_filterRuleType =
            attributeSet.getAttributeEnum ("action", m_filterRuleTypeEnumStrings, FilterRuleType.INCLUDE);
        m_name = attributeSet.getAttributeString ("name", null);
        m_capabilities = attributeSet.getAttributeString ("capabilities", "CRUD"); // default to all

        ThreadContext
            .assertError (m_name != null, "a name attribute is required for <rule> in the DaoGen options file");

        switch (m_filterRuleType)
        {
            case FilterRuleType.INCLUDE:
            {
                m_filterRuleStrategy = new IncludeFilterRuleStrategy (m_name, m_capabilities);
                break;
            }

            case FilterRuleType.EXCLUDE:
            {
                m_filterRuleStrategy = new ExcludeFilterRuleStrategy (m_name);
                break;
            }
        }

        attributeSet.validate (); // look for any unsupported attributes
    }

    // Parameter direction values
    public static class FilterRuleType
    {
        public static final int EXCLUDE = 1;

        public static final int INCLUDE = 0;
    }

    private String m_capabilities;

    // Implementation corresponding to m_filterRuleType.
    private IFilterRule m_filterRuleStrategy;

    // Attributes
    private int m_filterRuleType;

    private String m_name;

    private static final EnumString[] m_filterRuleTypeEnumStrings = new EnumString[]
    {
            new EnumString (FilterRuleType.INCLUDE, "include"), new EnumString (FilterRuleType.EXCLUDE, "exclude"),
    };
}
