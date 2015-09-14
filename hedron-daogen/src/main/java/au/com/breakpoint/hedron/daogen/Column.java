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

import java.util.function.Predicate;
import org.w3c.dom.Node;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.daogen.Util.EnumString;

public class Column
{
    public Column (final IRelation parent)
    {
        m_parent = parent;
    }

    public Column (final IRelation parent, final Node node)
    {
        this (parent);

        // First get the attributes of our own node.
        getAttributes (node);
    }

    public ColumnAttributes getColumnAttributes ()
    {
        return m_columnAttributes;
    }

    public String getEnumName ()
    {
        return m_enumName;
    }

    public String getName ()
    {
        return m_name;
    }

    public IRelation getParent ()
    {
        return m_parent;
    }

    public String getPhysicalName ()
    {
        return m_physicalName;
    }

    public int getRequirement ()
    {
        return m_requirement;
    }

    public String getRequirementDescription ()
    {
        return m_requirementDescription[m_requirement];
    }

    public boolean isIdentity ()
    {
        return m_identity;
    }

    public boolean isNullable ()
    {
        return m_requirement == Requirement.OPTIONAL;
    }

    public void setColumnAttributes (final ColumnAttributes columnAttributes)
    {
        m_columnAttributes = columnAttributes;
    }

    public void setIdentity (final boolean identity)
    {
        m_identity = identity;
    }

    public void setName (final String name)
    {
        m_name = Util.capitalise (name);
    }

    public void setPhysicalName (final String physicalName)
    {
        m_physicalName = physicalName;
    }

    public void setRequirement (final int requirement)
    {
        m_requirement = requirement;
    }

    private void getAttributes (final Node node)
    {
        final AttributeSet attributeSet = new AttributeSet (node);
        setName (attributeSet.getAttributeString ("name", null));
        m_physicalName = attributeSet.getAttributeString ("physicalname", null);
        m_requirement = attributeSet.getAttributeEnum ("requirement", m_requirementEnumStrings, Requirement.MANDATORY);
        m_identity = attributeSet.getAttributeBoolean ("identity", false);
        m_enumName = attributeSet.getAttributeString ("enumname", null);

        ThreadContext.assertError (m_name != null, "<column> must have a 'name' attribute");
        if (m_physicalName == null)
        {
            // Default to upper case underscore separated.
            m_physicalName = Util.getDefaultPhysicalName (m_name);
        }

        m_columnAttributes.getAttributes (attributeSet);

        attributeSet.validate (); // look for any unsupported attributes
    }

    // Requirement values
    public static class Requirement
    {
        public static final int MANDATORY = 1;

        public static final int OPTIONAL = 2;

        public static final int PRIMARYKEY = 0;
    }

    public static Predicate<Column> getFinder (final String columnName)
    {
        return e -> e.getName ().equals (columnName);
    }

    public static String[] getRequirementdescription ()
    {
        return m_requirementDescription;
    }

    public static EnumString[] getRequirementenumstrings ()
    {
        return m_requirementEnumStrings;
    }

    private String m_enumName;

    private ColumnAttributes m_columnAttributes = new ColumnAttributes ();

    private boolean m_identity;

    private String m_name;

    private final IRelation m_parent;

    private String m_physicalName;

    private int m_requirement;

    private static final String[] m_requirementDescription =
        {
                "primary key",
                "non-nullable",
                "nullable"
    };

    private static final EnumString[] m_requirementEnumStrings = new EnumString[]
    {
            new EnumString (Requirement.PRIMARYKEY, "primarykey"),
            new EnumString (Requirement.MANDATORY, "mandatory"),
            new EnumString (Requirement.OPTIONAL, "optional")
    };
}
