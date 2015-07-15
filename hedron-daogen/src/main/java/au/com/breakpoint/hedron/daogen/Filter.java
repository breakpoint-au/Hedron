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

import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import au.com.breakpoint.hedron.core.GenericFactory;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.daogen.strategy.IFilterRule;

public class Filter
{
    Filter (final Node node)
    {
        // First get the attributes of our own node.
        getAttributes (node);

        final NodeList childNodes = node.getChildNodes ();

        for (int i = 0; i < childNodes.getLength (); ++i)
        {
            final Node childNode = childNodes.item (i);

            if (childNode.getNodeType () == Node.ELEMENT_NODE)
            {
                final String nodeName = childNode.getNodeName ();
                if (nodeName.equals ("rule"))
                {
                    // Initialised to rule unused (false).
                    final FilterRuleStatus entry = new FilterRuleStatus (new FilterRule (childNode));
                    m_filterRules.add (entry);
                }
                else
                {
                    ThreadContext.assertError (false, "[%s] unknown option element [%s]", getClass ().getSimpleName (),
                        nodeName);
                }
            }
        }
    }

    public void evaluateFilter (final String type, final String name, final BooleanHolder ok,
        final List<Capability> capabilities)
    {
        // Make sure the filter applies to this type of dao (ie table, view,
        // storedprocedure).
        if (type.equalsIgnoreCase (m_type))
        {
            for (final FilterRuleStatus fr : m_filterRules)
            {
                if (fr.m_filterRule.evaluateFilter (name, ok, capabilities))
                {
                    fr.m_matched = true;    // filter rule matched
                }
            }
        }

        //		System.out.printf ("  Filter: %s %s %s pass: %s\n", m_type, type, name, ok);
    }

    public List<String> getUnusedFilterRules ()
    {
        final List<String> l = GenericFactory.newArrayList ();

        for (final FilterRuleStatus fr : m_filterRules)
        {
            if (!fr.m_matched)
            {
                final String description = String.format ("%s / %s", m_type, fr.m_filterRule);
                l.add (description);
            }
        }

        return l;
    }

    private void getAttributes (final Node node)
    {
        final AttributeSet attributeSet = new AttributeSet (node);

        m_type = attributeSet.getAttributeString ("type", null);
        attributeSet.validate (); // look for any unsupported attributes
    }

    private class FilterRuleStatus
    {
        FilterRuleStatus (final IFilterRule filterRule)
        {
            m_filterRule = filterRule;
        }

        final IFilterRule m_filterRule;

        boolean m_matched;
    }

    private final List<FilterRuleStatus> m_filterRules = GenericFactory.newArrayList ();

    // Attributes.
    private String m_type;
}
