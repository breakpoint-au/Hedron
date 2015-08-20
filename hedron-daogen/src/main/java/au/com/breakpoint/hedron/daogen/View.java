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

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import au.com.breakpoint.hedron.core.context.ThreadContext;

public class View implements IRelation
{
    View (final Node node)
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
                if (nodeName.equals ("column"))
                {
                    m_columns.add (new Column (this, childNode));
                }
                else
                {
                    ThreadContext.assertError (false, "[%s] unknown option element [%s]", getClass ().getSimpleName (),
                        nodeName);
                }
            }
        }
    }

    @Override
    public List<Column> getColumns ()
    {
        return m_columns;
    }

    @Override
    public String getEntityName ()
    {
        return m_entityName == null ? m_name : m_entityName;
    }

    @Override
    public String getName ()
    {
        return m_name;
    }

    @Override
    public Column getOptimisticLockColumn ()
    {
        return null;
    }

    @Override
    public String getPhysicalName ()
    {
        return m_physicalName;
    }

    @Override
    public Constraint getPrimaryConstraint ()
    {
        return null; // not applicable
    }

    @Override
    public String getRelationType ()
    {
        return "view";
    }

    public void setName (final String name)
    {
        m_name = name;
    }

    public void setPhysicalName (final String physicalName)
    {
        m_physicalName = physicalName;
    }

    public void setSharedEntityName (final String sharedEntityName)
    {
        m_entityName = sharedEntityName;
    }

    private void getAttributes (final Node node)
    {
        final AttributeSet attributeSet = new AttributeSet (node);

        m_name = attributeSet.getAttributeString ("name", null);
        m_physicalName = attributeSet.getAttributeString ("physicalname", null);
        m_entityName = attributeSet.getAttributeString ("entity", null);

        ThreadContext.assertError (m_name != null, "<view> must have a 'name' attribute");
        if (m_physicalName == null)
        {
            // Default to upper case underscore separated.
            m_physicalName = Util.getDefaultPhysicalName (m_name);
        }

        attributeSet.validate (); // look for any unsupported attributes
    }

    private final List<Column> m_columns = new ArrayList<Column> ();

    private String m_entityName;

    private String m_name;

    private String m_physicalName;
}
