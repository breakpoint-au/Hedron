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

import org.w3c.dom.Node;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.daogen.Column.Requirement;
import au.com.breakpoint.hedron.daogen.Util.EnumString;

public class Parameter
{
    public Parameter (final Column c)
    {
        m_column = c;
    }

    public Parameter (final Node node)
    {
        // First get the attributes of our own node.
        getAttributes (node);
    }

    public Column getColumn ()
    {
        return m_column;
    }

    public int getDirection ()
    {
        return m_direction;
    }

    public void setDirection (final int direction)
    {
        m_direction = direction;
    }

    private void getAttributes (final Node node)
    {
        final AttributeSet attributeSet = new AttributeSet (node);

        final String name = attributeSet.getAttributeString ("name", null);
        final String physicalName = attributeSet.getAttributeString ("physicalname", null);
        m_direction =
            attributeSet.getAttributeEnum ("direction", m_parameterDirectionEnumStrings, ParameterDirection.IN);

        ThreadContext.assertError (m_direction == ParameterDirection.IN || name != null,
            "<parameter> inout / out / return parameters must explicitly specify a 'name' attribute");

        m_column = new Column (null);
        m_column.setName (name);
        m_column.setPhysicalName (physicalName);

        final ColumnAttributes ca = new ColumnAttributes ();
        ca.getAttributes (attributeSet);
        final String type = ca.getType ();
        ThreadContext.assertError (type != null, "a type attribute is required for <parameter>");
        ThreadContext.assertError (type == null || name != null,
            "<parameter> specifying a type attribute (instead of table/column) must also have a name attribute");
        m_column.setColumnAttributes (ca);

        //        m_column.setRequirement (Requirement.MANDATORY);
        m_column.setRequirement (Requirement.OPTIONAL); // can always pass null to stored proc parameter

        attributeSet.validate (); // look for any unsupported attributes
    }

    // Parameter direction values
    public static class ParameterDirection
    {
        public static final int IN = 0;

        public static final int IN_OUT = 2;

        public static final int OUT = 1;

        public static final int RETURN = 3;

        public static final int RETURN_AS_OUT = 4; // for Oracle, which doesn't report return parameters to DbAnalyse
    }

    public static EnumString[] getParameterDirectionEnumStrings ()
    {
        return m_parameterDirectionEnumStrings;
    }

    private Column m_column;

    // Attributes
    private int m_direction;

    private static final EnumString[] m_parameterDirectionEnumStrings = new EnumString[]
    {
            new EnumString (ParameterDirection.IN, "in"),
            new EnumString (ParameterDirection.OUT, "out"),
            new EnumString (ParameterDirection.IN_OUT, "inout"),
            new EnumString (ParameterDirection.RETURN, "return")
    };
}
