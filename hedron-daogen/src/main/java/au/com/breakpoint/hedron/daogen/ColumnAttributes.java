//                       __________________________________
//                ______|      Copyright 2008-2015         |______
//                \     |     Breakpoint Pty Limited       |     /
//                 \    |   http://www.breakpoint.com.au   |    /
//                 /    |__________________________________|    \
//                /_________/                          \_________\
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
// except in compliance with the License. You may obtain a copy of the License at
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
// either express or implied. See the License for the specific language governing
// permissions and limitations under the License.
//
package au.com.breakpoint.hedron.daogen;

import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.context.ThreadContext;

public class ColumnAttributes
{
    public void getAttributes (final AttributeSet attributeSet)
    {
        getAttributes (attributeSet, "");
    }

    public void getAttributes (final AttributeSet attributeSet, final String prefix)
    {
        m_type = attributeSet.getAttributeString (prefix + "type", null);
        m_mode = attributeSet.getAttributeString (prefix + "mode", null);
        m_size = attributeSet.getAttributeInt (prefix + "size", 0);
        m_scale = attributeSet.getAttributeInt (prefix + "scale", 0);
        m_precision = attributeSet.getAttributeInt (prefix + "precision", 0);
        m_referencedTableName = attributeSet.getAttributeString (prefix + "referencedtable", null);
        m_referencedColumnName = attributeSet.getAttributeString (prefix + "referencedcolumn", null);
        m_refcursorType = attributeSet.getAttributeString (prefix + REFCURSOR_TYPE, null);

        if (HcUtil.safeEquals (m_type, ORACLE_REFCURSOR))
        {
            ThreadContext
                .assertError (
                    m_refcursorType != null,
                    "You must specify the " + prefix + REFCURSOR_TYPE + " attribute for " + prefix + "type='" + ORACLE_REFCURSOR + "'");
        }
    }

    public String getMode ()
    {
        return m_mode;
    }

    public int getPrecision ()
    {
        return m_precision;
    }

    public String getRefcursorType ()
    {
        return m_refcursorType;
    }

    public String getReferencedColumnName ()
    {
        return m_referencedColumnName;
    }

    public String getReferencedTableName ()
    {
        return m_referencedTableName;
    }

    public int getScale ()
    {
        return m_scale;
    }

    public int getSize ()
    {
        return m_size;
    }

    public String getType ()
    {
        return m_type;
    }

    public void setMode (final String mode)
    {
        m_mode = mode;
    }

    public void setPrecision (final int precision)
    {
        m_precision = precision;
    }

    public void setReferencedColumnName (final String referencedColumnName)
    {
        m_referencedColumnName = referencedColumnName;
    }

    public void setReferencedTableName (final String referencedTableName)
    {
        m_referencedTableName = referencedTableName;
    }

    public void setScale (final int scale)
    {
        m_scale = scale;
    }

    public void setSize (final int size)
    {
        m_size = size;
    }

    public void setType (final String type)
    {
        m_type = type;
    }

    private String m_mode; // char mode="varying"

    private int m_precision; // numeric

    private String m_refcursorType; // when m_type is "oracle-refcursor"

    private String m_referencedColumnName; // ForeignKey

    private String m_referencedTableName; // ForeignKey

    private int m_scale; // numeric

    private int m_size; // char/varchar width

    private String m_type; // eg char or decimal or date

    private static final String ORACLE_REFCURSOR = "oracle-refcursor";

    private static final String REFCURSOR_TYPE = "refcursor-type";
}
