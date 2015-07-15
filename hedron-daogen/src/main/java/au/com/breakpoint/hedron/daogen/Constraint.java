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

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.daogen.Util.EnumString;

public class Constraint
{
    public Constraint ()
    {
    }

    Constraint (final Node node)
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
                    m_columns.add (new Column (null, childNode));
                }
                else
                {
                    ThreadContext.assertError (false, "[%s] unknown option element [%s]", getClass ().getSimpleName (),
                        nodeName);
                }
            }
        }
    }

    public List<Column> getColumns ()
    {
        return m_columns;
    }

    public String getName ()
    {
        return m_name;
    }

    public ConstraintType getType ()
    {
        return m_type;
    }

    public void setName (final String name)
    {
        m_name = name;
    }

    public void setType (final ConstraintType type)
    {
        m_type = type;
    }

    private void getAttributes (final Node node)
    {
        final AttributeSet attributeSet = new AttributeSet (node);

        m_name = attributeSet.getAttributeString ("name", null);
        m_type = attributeSet.getAttributeEnum ("type", m_typeEnumStrings);

        ThreadContext.assertError (m_name != null, "<constraint> must have a 'name' attribute");

        attributeSet.validate (); // look for any unsupported attributes
    }

    public enum ConstraintType
    {
        Check, ForeignKey, Index, PrimaryKey, Uniqueness;
    }

    private final List<Column> m_columns = new ArrayList<Column> ();

    private String m_name;

    private ConstraintType m_type; // constraint type

    private static final EnumString[] m_typeEnumStrings = new EnumString[]
    {
            new EnumString (ConstraintType.PrimaryKey, "compoundprimarykey"),
            new EnumString (ConstraintType.ForeignKey, "compoundforeignkey"),
            new EnumString (ConstraintType.Uniqueness, "uniqueness"),
            new EnumString (ConstraintType.Check, "check"),
            new EnumString (ConstraintType.Index, "index")
    };
}
