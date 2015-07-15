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

public class Sql
{
    public Sql (final Node node)
    {
        // First get the attributes of our own node.
        getAttributes (node);

        final NodeList childNodes = node.getChildNodes ();

        for (int i = 0; i < childNodes.getLength (); ++i)
        {
            final Node childNode = childNodes.item (i);

            if (childNode.getNodeType () == Node.TEXT_NODE)
            {
                m_textSql = childNode.getTextContent ();
            }
        }
    }

    public String getTextSql ()
    {
        return m_textSql;
    }

    public void setTextSql (final String textSql)
    {
        m_textSql = textSql;
    }

    private void getAttributes (final Node node)
    {
        final AttributeSet attributeSet = new AttributeSet (node);

        // None as yet.

        attributeSet.validate (); // look for any unsupported attributes
    }

    private String m_textSql;
}
