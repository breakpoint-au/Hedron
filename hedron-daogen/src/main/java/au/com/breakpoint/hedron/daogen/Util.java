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

import au.com.breakpoint.hedron.core.context.ThreadContext;

public class Util
{
    public static class EnumString
    {
        public EnumString (final Enum<?> enumValue, final String enumString)
        {
            m_enumValueEnum = enumValue;
            m_enumString = enumString;
        }

        public EnumString (final int enumValue, final String enumString)
        {
            m_enumValueInt = enumValue;
            m_enumString = enumString;
        }

        public String m_enumString;

        public Enum<?> m_enumValueEnum;

        public int m_enumValueInt;
    }

    public static String capitalise (final String name)
    {
        String s = name;

        switch (s.length ())
        {
            case 0:
            {
                break;
            }

            case 1:
            {
                s = name.toUpperCase ();
                break;
            }

            default:
            {
                s = name.substring (0, 1).toUpperCase () + name.substring (1);
                break;
            }
        }

        return s;
    }

    public static int decodeEnum (final String value, final EnumString[] enumStrings)
    {
        return decodeEnum (value, enumStrings, -1, true);
    }

    public static int decodeEnum (final String value, final EnumString[] enumStrings, final boolean enforceMatch)
    {
        return decodeEnum (value, enumStrings, -1, enforceMatch);
    }

    public static int decodeEnum (final String value, final EnumString[] enumStrings, final int defaultValue,
        final boolean enforceMatch)
    {
        int intValue = defaultValue;

        boolean found = false;
        for (int i = 0; !found && i < enumStrings.length; ++i)
        {
            if (value.equals (enumStrings[i].m_enumString))
            {
                found = true;
                intValue = enumStrings[i].m_enumValueInt;
            }
        }

        if (enforceMatch)
        {
            ThreadContext.assertError (found, "Unknown enum value: '%s'", value);
        }

        return intValue;
    }

    public static String getDefaultPhysicalName (final String logicalName)
    {
        final StringBuilder sb = new StringBuilder ();

        final char[] chars = logicalName.toCharArray ();

        boolean prevWasLowerCase = false;
        for (int i = 0; i < chars.length; ++i)
        {
            final char c = chars[i];
            final char uc = Character.toUpperCase (c);

            if (Character.isLowerCase (c))
            {
                sb.append (uc);
                prevWasLowerCase = true;
            }
            else
            {
                if (Character.isUpperCase (c) && prevWasLowerCase ||
                // Handle ABCBlah as ABC_BLAH
                Character.isUpperCase (c) && i > 0 && i < chars.length - 1 && Character.isLowerCase (chars[i + 1]))
                {
                    sb.append ('_');
                    sb.append (uc);
                }
                else
                {
                    sb.append (uc);
                }

                prevWasLowerCase = false;
            }
        }

        return sb.toString ();
    }

    public static final String STRING_NO = "no";

    public static final String STRING_YES = "yes";
}
