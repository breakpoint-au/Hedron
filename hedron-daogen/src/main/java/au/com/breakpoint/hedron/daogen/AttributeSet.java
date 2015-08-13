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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.daogen.Util.EnumString;

public class AttributeSet
{
    public AttributeSet (final Node node)
    {
        m_node = node;
    }

    public boolean getAttributeBoolean (final String name)
    {
        final String s = getAttributeString (name);

        return parseBoolString (s);
    }

    public boolean getAttributeBoolean (final String name, final boolean defaultValue)
    {
        final String s = getAttributeString (name, defaultValue ? Util.STRING_YES : Util.STRING_NO);

        return parseBoolString (s);
    }

    public double getAttributeDouble (final String name)
    {
        final String s = getAttributeString (name);

        return Double.parseDouble (s);
    }

    public double getAttributeDouble (final String name, final double defaultValue)
    {
        final String s = getAttributeString (name, String.valueOf (defaultValue));

        return Double.parseDouble (s);
    }

    public <T extends Enum<T>> T getAttributeEnum (final Class<T> enumType, final String value)
    {
        return Enum.valueOf (enumType, value);
    }

    public <T> T getAttributeEnum (final String name, final EnumString[] enumStrings)
    {
        final String s = getAttributeString (name); // must exist

        // Translate the string to the enum name.
        T e = null;
        for (int i = 0; e == null && i < enumStrings.length; ++i)
        {
            final EnumString enumString = enumStrings[i];
            if (enumString.m_enumString.equals (s))
            {
                @SuppressWarnings ("unchecked")
                final T t = (T) enumString.m_enumValueEnum;
                e = t;
            }
        }

        return e;
    }

    public int getAttributeEnum (final String name, final EnumString[] enumStrings, final int defaultValue)
    {
        final String s = getAttributeString (name, null); // can be absent

        // attribute match known values
        return (s == null ? defaultValue : Util.decodeEnum (s, enumStrings, true));
    }

    public int getAttributeEnumInt (final String name, final EnumString[] enumStrings)
    {
        final String s = getAttributeString (name); // must exist
        return Util.decodeEnum (s, enumStrings, true); // attribute match known values
    }

    public int getAttributeInt (final String name)
    {
        final String s = getAttributeString (name);

        return Integer.parseInt (s);
    }

    public int getAttributeInt (final String name, final int defaultValue)
    {
        final String s = getAttributeString (name, String.valueOf (defaultValue));

        return Integer.parseInt (s);
    }

    public long getAttributeLong (final String name)
    {
        final String s = getAttributeString (name);

        return Long.parseLong (s);
    }

    public long getAttributeLong (final String name, final long defaultValue)
    {
        final String s = getAttributeString (name, String.valueOf (defaultValue));

        return Long.parseLong (s);
    }

    public String getAttributeString (final String name)
    {
        m_handledAttributeNames.add (name);

        final NamedNodeMap attributes = m_node.getAttributes ();
        final Node n = attributes.getNamedItem (name);
        ThreadContext.assertError (n != null, "Missing mandatory attribute '%s' for XML element '%s'", name,
            m_node.getNodeName ());

        return n.getNodeValue ();
    }

    public String getAttributeString (final String name, final String defaultValue)
    {
        m_handledAttributeNames.add (name);

        final NamedNodeMap attributes = m_node.getAttributes ();
        final Node n = attributes.getNamedItem (name);
        return n == null ? defaultValue : n.getNodeValue ();
    }

    public void validate ()
    {
        final NamedNodeMap attributes = m_node.getAttributes ();

        // Check that each attribute is allowable.
        for (int i = 0; i < attributes.getLength (); ++i)
        {
            final Node attribute = attributes.item (i);

            final String attributeName = attribute.getNodeName ();
            final Object lazyString = new Object ()
            {
                @Override
                public String toString ()
                {
                    final Node parentNode = m_node.getParentNode ();
                    return String.format ("Unknown attribute '%s' for XML element:%n  %s%nparent is%n  %s",
                        attributeName, formatNode (m_node), parentNode == null ? "(none)" : formatNode (parentNode));
                }

                private String formatNode (final Node node)
                {
                    final StringBuilder sb = new StringBuilder ();
                    final NamedNodeMap as = node.getAttributes ();

                    // Check that each attribute is allowable.
                    for (int j = 0; j < as.getLength (); ++j)
                    {
                        if (j > 0)
                        {
                            sb.append (" ");
                        }

                        final Node a = as.item (j);
                        sb.append (a.getNodeName ());
                        sb.append ("='");
                        sb.append (a.getNodeValue ());
                        sb.append ("'");
                    }
                    return String.format ("<%s %s>", node.getNodeName (), sb);
                }
            };
            ThreadContext.assertError (hasMatchingString (m_handledAttributeNames, attributeName), "%s", lazyString);
        }
    }

    public static boolean parseBoolString (final String s)
    {
        ThreadContext.assertError (s.equals (Util.STRING_YES) || s.equals (Util.STRING_NO),
            "Unrecognised boolean value %s should be %s or %s", s, Util.STRING_YES, Util.STRING_NO);
        return s.equals (Util.STRING_YES);
    }

    private static boolean hasMatchingString (final List<String> handledAttributeNames, final String attributeName)
    {
        boolean has = false;
        for (final String s : handledAttributeNames)
        {
            has = has || s.equals (attributeName);
        }

        return has;
    }

    private final List<String> m_handledAttributeNames = new ArrayList<String> ();

    private final Node m_node;
}
