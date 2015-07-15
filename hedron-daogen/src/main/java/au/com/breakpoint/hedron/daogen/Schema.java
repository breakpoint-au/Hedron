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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import au.com.breakpoint.hedron.core.context.ThreadContext;

public class Schema
{
    public Schema (final Node node)
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
                switch (nodeName)
                {
                    case "enum":
                    {
                        final DbEnum o = new DbEnum (childNode);
                        m_schemaObjects.m_enums.put (o.getName (), o);
                        break;
                    }

                    case "table":
                    {
                        final Table o = new Table (childNode);
                        m_schemaObjects.m_tables.put (o.getName (), o);
                        break;
                    }

                    case "view":
                    {
                        final View o = new View (childNode);
                        m_schemaObjects.m_views.put (o.getName (), o);
                        break;
                    }

                    case "storedprocedure":
                    {
                        final StoredProcedure o = new StoredProcedure (childNode);
                        m_schemaObjects.m_storedProcedures.put (o.getName (), o);
                        break;
                    }

                    case "command":
                    {
                        final Command o = new Command (childNode);
                        m_schemaObjects.m_commands.put (o.getName (), o);
                        break;
                    }

                    case "customview":
                    {
                        final CustomView o = new CustomView (childNode, this);
                        m_schemaObjects.m_customViews.put (o.getName (), o);
                        break;
                    }

                    default:
                    {
                        ThreadContext.assertError (false, "[%s] unknown option element [%s]", getClass ()
                            .getSimpleName (), nodeName);
                        break;
                    }
                }
            }
        }
    }

    public void addSchemaObjects (final Schema additionalSchema)
    {
        m_schemaObjects.m_tables.putAll (additionalSchema.m_schemaObjects.m_tables);
        m_schemaObjects.m_views.putAll (additionalSchema.m_schemaObjects.m_views);
        m_schemaObjects.m_storedProcedures.putAll (additionalSchema.m_schemaObjects.m_storedProcedures);
    }

    public IRelation getIRelationNoThrow (final String typeName)
    {
        IRelation ir = m_schemaObjects.m_tables.get (typeName);
        if (ir == null)
        {
            ir = m_schemaObjects.m_views.get (typeName);
        }

        return ir;
    }

    public String getName ()
    {
        return m_name;
    }

    public SchemaObjects getSchemaObjects ()
    {
        return m_schemaObjects;
    }

    private void getAttributes (final Node node)
    {
        final AttributeSet attributeSet = new AttributeSet (node);

        m_name = attributeSet.getAttributeString ("name", null);

        ThreadContext.assertError (m_name != null, "<schema> must have a 'name' attribute");

        attributeSet.validate (); // look for any unsupported attributes
    }

    // Attributes
    private String m_name;

    private final SchemaObjects m_schemaObjects = new SchemaObjects ();
}
