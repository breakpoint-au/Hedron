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
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import au.com.breakpoint.hedron.core.GenericFactory;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.daogen.Parameter.ParameterDirection;
import au.com.breakpoint.hedron.daogen.Util.EnumString;

public class StoredProcedure
{
    public StoredProcedure (final Node node)
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
                    if (!m_useReturnValue && m_procedureType != ProcedureType.FUNCTION && parameter.getDirection () == Parameter.ParameterDirection.RETURN)
                    {
                        // This is a hack required because DbAnalyse through the shabby Sybase JDBC driver
                        // can't determine complete stored proc metadata.
                        ThreadContext.assertInformation (false,
                            "Ignored bogus return parameter for stored procedure [%s]", m_name);
                    }
                    else
                    {
                        m_parameters.add (parameter);

                        // dbModeller doesn't require type=function on stored procs... only
                        // dbAnalyse puts that attribute in.
                        if (parameter.getDirection () == ParameterDirection.RETURN)
                        {
                            m_procedureType = ProcedureType.FUNCTION;
                        }
                    }
                }
                else
                {
                    ThreadContext.assertError (false, "[%s] unknown option element [%s]", getClass ().getSimpleName (),
                        nodeName);
                }
            }
        }
    }

    public void addFunctionReturnOutParameter (final String outParameterName, final ColumnAttributes columnAttributes)
    {
        // Fudge a new 'out' parameter corresponding to the return value.
        final Column c = new Column (null);
        c.setName (outParameterName);
        c.setPhysicalName (outParameterName);

        final ColumnAttributes ca = columnAttributes;
        c.setColumnAttributes (ca);

        final Parameter parameter = new Parameter (c);
        parameter.setDirection (Parameter.ParameterDirection.RETURN_AS_OUT);

        addParameter (parameter, true); // at front of collection
    }

    public void addParameter (final Parameter parameter, final boolean atFront)
    {
        if (atFront)
        {
            m_parameters.add (0, parameter);
        }
        else
        {
            m_parameters.add (parameter);
        }
    }

    public void addResultSet (final StoredProcedureResultSet sprs)
    {
        m_resultSets.add (sprs);
    }

    public String getCatalogName ()
    {
        return m_catalogName;
    }

    public List<String> getEntityStrings ()
    {
        final List<String> es = GenericFactory.newArrayList ();

        for (final Parameter p : m_parameters)
        {
            final String refcursorType = p.getColumn ().getColumnAttributes ().getRefcursorType ();
            if (refcursorType != null)
            {
                es.add (refcursorType);
            }
        }
        return es;
    }

    public List<Parameter> getInputParameters ()
    {
        final List<Parameter> ps = new ArrayList<Parameter> ();

        for (final Parameter p : m_parameters)
        {
            if (p.getDirection () == Parameter.ParameterDirection.IN || p.getDirection () == Parameter.ParameterDirection.IN_OUT)
            {
                ps.add (p);
            }
        }
        return ps;
    }

    public String getName ()
    {
        return m_name;
    }

    public List<Parameter> getOutputParameters ()
    {
        final List<Parameter> ps = new ArrayList<Parameter> ();

        for (final Parameter p : m_parameters)
        {
            if (p.getDirection () != Parameter.ParameterDirection.IN)
            {
                ps.add (p);
            }
        }
        return ps;
    }

    public int getParameterCount (final int pd)
    {
        int count = 0;

        for (final Parameter p : m_parameters)
        {
            if (p.getDirection () == pd)
            {
                ++count;
            }
        }
        return count;
    }

    public List<Parameter> getParameters ()
    {
        return m_parameters;
    }

    public String getPhysicalName ()
    {
        return m_physicalName;
    }

    public int getProcedureType ()
    {
        return m_procedureType;
    }

    public List<StoredProcedureResultSet> getResultSets ()
    {
        return m_resultSets;
    }

    public String getSchemaName ()
    {
        return m_schemaName;
    }

    public void setCatalogName (final String catalogName)
    {
        m_catalogName = catalogName;
    }

    public void setName (final String name)
    {
        m_name = name;
    }

    public void setPhysicalName (final String physicalName)
    {
        m_physicalName = physicalName;
    }

    public void setProcedureType (final int procedureType)
    {
        m_procedureType = procedureType;
    }

    public void setSchemaName (final String schemaName)
    {
        m_schemaName = schemaName;
    }

    public void setUseReturnValue (final boolean useReturnValue)
    {
        m_useReturnValue = useReturnValue;

        if (!m_useReturnValue)
        {
            // If the parameters have already been parsed, apply the override.
            Parameter returnParameter = null;
            for (final Iterator<Parameter> it = m_parameters.iterator (); returnParameter == null && it.hasNext ();)
            {
                final Parameter p = it.next ();
                if (p.getDirection () == Parameter.ParameterDirection.RETURN)
                {
                    returnParameter = p;
                }
            }

            if (returnParameter != null)
            {
                m_parameters.remove (returnParameter);
            }
        }
    }

    public boolean shouldUseReturnValue ()
    {
        return m_useReturnValue;
    }

    private void getAttributes (final Node node)
    {
        final AttributeSet attributeSet = new AttributeSet (node);

        m_name = attributeSet.getAttributeString ("name", null);
        m_physicalName = attributeSet.getAttributeString ("physicalname", null);
        m_catalogName = attributeSet.getAttributeString ("catalog", null);
        m_schemaName = attributeSet.getAttributeString ("schema", null);
        m_procedureType = attributeSet.getAttributeEnum ("type", m_procedureTypeEnumStrings, ProcedureType.PROCEDURE);

        if (m_physicalName == null)
        {
            // Default to upper case underscore separated.
            m_physicalName = Util.getDefaultPhysicalName (m_name);
        }

        m_columnAttributes.getAttributes (attributeSet);

        attributeSet.validate (); // look for any unsupported attributes
    }

    // ProcedureType values
    public static class ProcedureType
    {
        public static final int FUNCTION = 1;

        public static final int PROCEDURE = 0;
    }

    private String m_catalogName;

    private final ColumnAttributes m_columnAttributes = new ColumnAttributes ();

    private String m_name;

    private final List<Parameter> m_parameters = new ArrayList<Parameter> ();

    private String m_physicalName;

    private int m_procedureType;

    /**
     * These result sets are the non-parameter type like are returned by Sybase, and are
     * currently set only with the overrides mechanism:
     *
     * <overrides> <stored-procedure name='getAudit' override='result-sets'> <result-set
     * name='MessageAuditData' type='MessageAuditData' /> </stored-procedure> </overrides>
     *
     * But one day they will be supported as part of the schema <storedprocedure> element.
     */
    private final List<StoredProcedureResultSet> m_resultSets = new ArrayList<StoredProcedureResultSet> ();

    private String m_schemaName;

    private boolean m_useReturnValue = true;

    private static final EnumString[] m_procedureTypeEnumStrings = new EnumString[]
    {
            new EnumString (ProcedureType.PROCEDURE, "procedure"), new EnumString (ProcedureType.FUNCTION, "function")
    };
}
