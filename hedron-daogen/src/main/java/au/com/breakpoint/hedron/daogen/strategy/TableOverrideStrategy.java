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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.daogen.AttributeSet;
import au.com.breakpoint.hedron.daogen.Options;
import au.com.breakpoint.hedron.daogen.SchemaObjects;
import au.com.breakpoint.hedron.daogen.Table;

public class TableOverrideStrategy implements IOverrideStrategy
{
    public TableOverrideStrategy (@SuppressWarnings ("unused") final Options options, final Node node)
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
                ThreadContext.assertError (false, "[%s] unknown option element [%s]", getClass ().getSimpleName (),
                    nodeName);
            }
        }
    }

    @Override
    public void override (final SchemaObjects schemaObjects)
    {
        final Table t = schemaObjects.m_tables.get (m_tableName);

        // Don't assert non-null. Allow all the overrides to be set up, but only selected
        // stored procs to be generated.
        if (t != null)
        {
            if (m_overrideType.equals ("optimistic-lock"))
            {
                ThreadContext.assertError (m_columnName != null,
                    "column-name must be specified for optimistic-lock override for table [%s]", m_tableName);
                t.setOptimisticLockColumn (m_columnName);
            }
            else if (m_overrideType.equals ("identity"))
            {
                ThreadContext.assertError (m_columnName != null,
                    "column-name must be specified for identity override for table [%s]", m_tableName);
                t.findColumn (m_columnName).setIdentity (true);
            }
            else
            {
                ThreadContext.assertError (false, "Unknown override value [%s]", m_overrideType);
            }
        }
    }

    private void getAttributes (final Node node)
    {
        final AttributeSet attributeSet = new AttributeSet (node);

        m_tableName = attributeSet.getAttributeString ("name", null);
        m_overrideType = attributeSet.getAttributeString ("override", null);

        m_columnName = attributeSet.getAttributeString ("column-name", null);

        attributeSet.validate (); // look for any unsupported attributes
    }

    private String m_columnName;

    private String m_overrideType;

    // Attributes.
    private String m_tableName;
}
