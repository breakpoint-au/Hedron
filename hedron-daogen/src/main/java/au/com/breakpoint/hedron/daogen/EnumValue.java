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

public class EnumValue
{
    public EnumValue (final DbEnum parent, final Node node)
    {
        m_parent = parent;

        // First get the attributes of our own node.
        getAttributes (node);
    }

    public DbEnum getParent ()
    {
        return m_parent;
    }

    public String getTitle ()
    {
        return m_title;
    }

    public int getValue ()
    {
        return m_value;
    }

    public void setTitle (final String title)
    {
        m_title = title;
    }

    public void setValue (final int value)
    {
        m_value = value;
    }

    private void getAttributes (final Node node)
    {
        final AttributeSet attributeSet = new AttributeSet (node);
        setTitle (attributeSet.getAttributeString ("title"));
        setValue (attributeSet.getAttributeInt ("override"));

        attributeSet.validate (); // look for any unsupported attributes
    }

    private final DbEnum m_parent;

    private String m_title;

    private int m_value;
}
