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
import org.w3c.dom.NodeList;
import au.com.breakpoint.hedron.core.GenericFactory;
import au.com.breakpoint.hedron.core.context.ThreadContext;

public class Command
{
    public Command (final Node node)
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
                if (nodeName.equals ("parameter"))
                {
                    final Parameter parameter = new Parameter (childNode);
                    m_parameters.add (parameter);
                }
                else if (nodeName.equals ("sql"))
                {
                    m_sql = new Sql (childNode);
                }
                else
                {
                    ThreadContext.assertError (false, "[%s] unknown option element [%s]", getClass ().getSimpleName (),
                        nodeName);
                }
            }
        }
    }

    public String getName ()
    {
        return m_name;
    }

    public List<Parameter> getParameters ()
    {
        return m_parameters;
    }

    public String getSqlText ()
    {
        return m_sql.getTextSql ();
    }

    public void setName (final String name)
    {
        m_name = name;
    }

    public void setShouldPreserveNewLinesInSQL (final boolean shouldPreserveNewLinesInSQL)
    {
        m_shouldPreserveNewLinesInSQL = shouldPreserveNewLinesInSQL;
    }

    public void setSql (final Sql sql)
    {
        m_sql = sql;
    }

    public boolean shouldPreserveNewLinesInSQL ()
    {
        return m_shouldPreserveNewLinesInSQL;
    }

    private void getAttributes (final Node node)
    {
        final AttributeSet attributeSet = new AttributeSet (node);

        m_name = attributeSet.getAttributeString ("name", null);
        m_shouldPreserveNewLinesInSQL = attributeSet.getAttributeBoolean ("preservenewlinesinsql", false);

        attributeSet.validate (); // look for any unsupported attributes
    }

    private String m_name;

    private final List<Parameter> m_parameters = GenericFactory.newArrayList ();

    private boolean m_shouldPreserveNewLinesInSQL;

    private Sql m_sql;
}
