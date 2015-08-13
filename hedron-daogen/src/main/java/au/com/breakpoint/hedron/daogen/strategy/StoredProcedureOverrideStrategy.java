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
package au.com.breakpoint.hedron.daogen.strategy;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.daogen.AttributeSet;
import au.com.breakpoint.hedron.daogen.ColumnAttributes;
import au.com.breakpoint.hedron.daogen.Options;
import au.com.breakpoint.hedron.daogen.Parameter;
import au.com.breakpoint.hedron.daogen.SchemaObjects;
import au.com.breakpoint.hedron.daogen.StoredProcedure;
import au.com.breakpoint.hedron.daogen.StoredProcedureResultSet;

public class StoredProcedureOverrideStrategy implements IOverrideStrategy
{
    public StoredProcedureOverrideStrategy (final Options options, final Node node)
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
                if (nodeName.equals ("result-set"))
                {
                    final StoredProcedureResultSet sprs = new StoredProcedureResultSet (options, childNode);
                    m_resultSets.add (sprs);
                }
                else
                {
                    ThreadContext.assertError (false, "[%s] unknown option element [%s]", getClass ().getSimpleName (),
                        nodeName);
                }
            }
        }
    }

    @Override
    public void override (final SchemaObjects schemaObjects)
    {
        final StoredProcedure sp = schemaObjects.m_storedProcedures.get (m_storedProcedureName);

        // Don't assert non-null. Allow all the overrides to be set up, but only selected
        // stored procs to be generated.
        if (sp != null)
        {
            if (m_overrideType.equals ("is-function"))
            {
                sp.setUseReturnValue (false);   // remove the current return value if any (Sybase always shows return type)
                sp.setProcedureType (StoredProcedure.ProcedureType.FUNCTION);
                addFunctionReturnOutParameter (sp);
            }
            else if (m_overrideType.equals ("set-parameter-direction"))
            {
                final Parameter p = findParameter (sp, m_parameterName);
                p.setDirection (m_direction);
            }
            else if (m_overrideType.equals ("result-sets"))
            {
                // Insert the result set information into the stored procedure definition.
                for (final StoredProcedureResultSet sprs : m_resultSets)
                {
                    sp.addResultSet (sprs);
                }
            }
            else if (m_overrideType.equals ("ignore-return-value"))
            {
                sp.setUseReturnValue (Boolean.valueOf (m_value));
            }
            else
            {
                ThreadContext.assertError (false, "Unknown override value [%s]", m_overrideType);
            }
        }
    }

    private void addFunctionReturnOutParameter (final StoredProcedure sp)
    {
        sp.addFunctionReturnOutParameter (m_outParameterName, m_columnAttributes);
    }

    private void getAttributes (final Node node)
    {
        final AttributeSet attributeSet = new AttributeSet (node);

        m_storedProcedureName = attributeSet.getAttributeString ("name", null);
        m_overrideType = attributeSet.getAttributeString ("override", null);
        m_value = attributeSet.getAttributeString ("value", null);

        m_parameterName = attributeSet.getAttributeString ("parameter-name", null);
        m_outParameterName = attributeSet.getAttributeString ("out-parameter-name", null);

        m_columnAttributes.getAttributes (attributeSet);

        attributeSet.validate (); // look for any unsupported attributes
    }

    private static Parameter findParameter (final StoredProcedure sp, final String parameterName)
    {
        Parameter parameter = null;

        for (final Parameter p : sp.getParameters ())
        {
            if (p.getColumn ().getName ().equals (parameterName))
            {
                parameter = p;
            }
        }

        ThreadContext.assertError (parameter != null, "Cannot find parameter [%s] in stored procedure [%s]",
            parameterName, sp.getName ());
        return parameter;
    }

    private ColumnAttributes m_columnAttributes;

    private int m_direction;

    private String m_outParameterName;

    private String m_overrideType;

    private String m_parameterName;

    private final List<StoredProcedureResultSet> m_resultSets = new ArrayList<StoredProcedureResultSet> ();

    private String m_storedProcedureName;

    private String m_value;

}
