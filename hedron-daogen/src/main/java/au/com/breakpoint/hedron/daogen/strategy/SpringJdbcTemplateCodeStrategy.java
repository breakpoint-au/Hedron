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
package au.com.breakpoint.hedron.daogen.strategy;

import static java.util.Comparator.comparing;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import au.com.breakpoint.hedron.core.GenericFactory;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.HcUtilFile;
import au.com.breakpoint.hedron.core.SmartFile;
import au.com.breakpoint.hedron.core.Tuple.E3;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.daogen.Attribute;
import au.com.breakpoint.hedron.daogen.Capability;
import au.com.breakpoint.hedron.daogen.Column;
import au.com.breakpoint.hedron.daogen.Command;
import au.com.breakpoint.hedron.daogen.Constraint;
import au.com.breakpoint.hedron.daogen.CustomView;
import au.com.breakpoint.hedron.daogen.DbEnum;
import au.com.breakpoint.hedron.daogen.EntityUtil;
import au.com.breakpoint.hedron.daogen.EnumValue;
import au.com.breakpoint.hedron.daogen.IRelation;
import au.com.breakpoint.hedron.daogen.Options;
import au.com.breakpoint.hedron.daogen.Parameter;
import au.com.breakpoint.hedron.daogen.Schema;
import au.com.breakpoint.hedron.daogen.SmartFileShowingProgress;
import au.com.breakpoint.hedron.daogen.StoredProcedure;
import au.com.breakpoint.hedron.daogen.StoredProcedureResultSet;

public class SpringJdbcTemplateCodeStrategy implements IRelationCodeStrategy
{
    @Override
    public List<String> generateDao (final Command c, final Schema schema)
    {
        final String sqlString = c.getSqlText ();

        return generateSqlExecutionDao (c, sqlString, c.shouldPreserveNewLinesInSQL (), c.getParameters (), "Cmd");
    }

    @Override
    public List<String> generateDao (final CustomView cv, final Schema schema)
    {
        final ArrayList<Capability> capabilities = GenericFactory.newArrayList (Capability.READ);
        final String sqlString = cv.getSqlText ();

        return generateRelationalDao (cv, capabilities, sqlString, cv.shouldPreserveNewLinesInSQL (),
            cv.getParameters (), "Custom");
    }

    @Override
    public List<String> generateDao (final DbEnum en, final Schema schema)
    {
        m_enums.add (en);
        return GenericFactory.newArrayList ();
    }

    @Override
    public List<String> generateDao (final IRelation ir, final Schema schema, final List<Capability> capabilities)
    {
        final List<Column> columns = ir.getColumns ();
        final String entityPhysicalName = ir.getPhysicalName ();
        final String sqlString =
            String.format ("select %s from %s", EntityUtil.getStringColumnPhysicalNames (columns), entityPhysicalName);

        return generateRelationalDao (ir, capabilities, sqlString, false, null, "");
    }

    @Override
    public List<String> generateDao (final StoredProcedure sp, final Schema schema)
    {
        final List<String> result = GenericFactory.newArrayList ();

        final String filepath = getDaoFilepath (String.format ("%sStoredProcDao.java", sp.getName ()));
        final String outputPackage = m_options.m_outputPackage;

        final String storedProcedurePhysicalName = sp.getPhysicalName ();
        final String storedProcedureName = sp.getName ();
        final List<Parameter> parameters = sp.getParameters ();
        final List<Parameter> inParameters = sp.getInputParameters ();
        final List<Parameter> outParameters = sp.getOutputParameters ();
        final List<StoredProcedureResultSet> resultSets = sp.getResultSets ();
        final boolean shouldReturnValues = outParameters.size () > 0 || resultSets.size () > 0;
        final String returnTypeString = shouldReturnValues ? "Result" : "void";
        final String parametersClassName = EntityUtil.getParametersClass (inParameters);

        try (final SmartFile pw = new SmartFileShowingProgress (filepath))
        {
            pw.printf ("package %s.dao;%n", outputPackage);
            pw.printf ("%n");
            pw.printf ("import java.util.*;%n");
            pw.printf ("import javax.sql.*;%n");

            final List<String> es = sp.getEntityStrings ();
            for (final String e : es)
            {
                pw.printf ("import %s.entity.%s;%n", outputPackage, e);
            }

            if (m_shouldUseSimpleJdbcCall)
            {
                pw.printf ("import org.springframework.jdbc.core.namedparam.*;%n");
                pw.printf ("import org.springframework.jdbc.core.simple.*;%n");
            }
            else
            {
                if (sp.getParameterCount (Parameter.ParameterDirection.IN) > 0)
                {
                    pw.printf ("import org.springframework.jdbc.core.*;%n");
                }
                if (sp.getParameterCount (Parameter.ParameterDirection.OUT) > 0 || sp
                    .getParameterCount (Parameter.ParameterDirection.RETURN_AS_OUT) > 0 || sp
                        .getParameterCount (Parameter.ParameterDirection.RETURN) > 0)
                {
                    pw.printf ("import org.springframework.jdbc.core.SqlOutParameter;%n");
                }
                if (sp.getParameterCount (Parameter.ParameterDirection.IN_OUT) > 0)
                {
                    pw.printf ("import org.springframework.jdbc.core.*;%n");
                }
                if (resultSets.size () > 0)
                {
                    pw.printf ("import java.util.List;%n");
                    pw.printf ("import org.springframework.jdbc.core.*;%n");
                    final Set<String> specifiedEntityNames = new HashSet<String> ();
                    for (final StoredProcedureResultSet sprs : resultSets)
                    {
                        final String type = sprs.getType ();
                        if (!specifiedEntityNames.contains (type))
                        {
                            pw.printf ("import %s.entity.%s;%n", outputPackage, type);
                            specifiedEntityNames.add (type);
                        }
                    }
                }
                pw.printf ("import org.springframework.jdbc.object.*;%n");
            }
            pw.printf ("import au.com.breakpoint.hedron.core.*;%n");
            pw.printf ("import au.com.breakpoint.hedron.core.dao.*;%n");

            pw.printf ("%n");
            pw.printf ("/**%n");
            pw.printf (" * Low-level DAO object encapsulating the %s stored procedure. Encapsulates%n",
                storedProcedurePhysicalName);
            pw.printf (" * Spring JDBC template database access.%n");
            pw.printf (" */%n");
            // TODO 1 implement Function too if shouldReturnValues
            pw.printf ("public class %sStoredProcDao extends BaseExecutableDao<%s>%n", storedProcedureName,
                parametersClassName);
            pw.printf ("{%n");
            pw.printf ("    public %sStoredProcDao (final DataSource dataSource)%n", storedProcedureName);
            pw.printf ("    {%n");
            pw.printf ("        super (dataSource);%n");
            pw.printf ("    }%n");
            if (shouldReturnValues)
            {
                pw.printf ("%n");
                pw.printf ("    /** Data structure for out/return stored procedure parameters & result sets */%n");
                pw.printf ("    public static class Result%n");
                pw.printf ("    {%n");
                for (final Parameter p : outParameters)
                {
                    final Column c = p.getColumn ();
                    final ColumnTypeInfo jti = EntityUtil.getColumnTypeInfo (c);

                    pw.printf ("        public %s m_value%s;%n", jti.m_javaType, c.getName ());
                }
                for (final StoredProcedureResultSet sprs : resultSets)
                {
                    pw.printf ("        public List<%s> m_resultSet%s;%n", sprs.getType (), sprs.getName ());
                }
                pw.printf ("    }%n");
            }

            if (EntityUtil.hasConvenienceTypes (inParameters))
            {
                pw.printf ("%n");
                pw.printf ("    /**%n");
                pw.printf (
                    "     * Convenience overload using intrinsic values. You must make sure the parameter values%n");
                pw.printf ("     * will fit into the intrinsic parameters.%n");
                pw.printf ("     */%n");
                pw.printf ("    public %s execute (%s)%n", returnTypeString,
                    EntityUtil.getStringConvenienceParametersArgs (inParameters));
                pw.printf ("    {%n");
                pw.printf ("        %sexecute (", shouldReturnValues ? "return " : "");
                pw.print (EntityUtil.getStringConvenienceParameters (inParameters));
                pw.printf (");%n");
                pw.printf ("    }%n");
            }
            pw.printf ("%n");
            pw.printf ("    /**%n");
            pw.printf ("     * Calls the %s stored procedure using the specified parameter values.%n",
                storedProcedurePhysicalName);
            pw.printf ("     */%n");
            pw.printf ("    public %s execute (%s)%n", returnTypeString,
                EntityUtil.getStringParametersArgs (inParameters));
            pw.printf ("    {%n");
            if (m_shouldUseSimpleJdbcCall)
            {
                pw.printf ("        // Set up in parameters.%n");
                pw.printf ("        final SimpleJdbcCall sjc = new SimpleJdbcCall (m_dataSource);%n");
                if (HcUtil.safeGetLength (schema.getName ()) > 0)
                {
                    pw.printf ("        sjc = sjc.withSchemaName (\"%s\");%n", schema.getName ());
                }
                final String catalogName = sp.getCatalogName ();
                if (catalogName != null)
                {
                    pw.printf ("        sjc = sjc.withCatalogName (\"%s\");%n", catalogName);
                }
                pw.printf ("        sjc = sjc.with%sName (STORED_PROCEDURE_NAME);%n",
                    sp.getProcedureType () == StoredProcedure.ProcedureType.FUNCTION ? "Function" : "Procedure");
                pw.printf ("%n");
                pw.printf ("        final MapSqlParameterSource sps = new MapSqlParameterSource ();%n");

                if (true)
                {
                    for (final Parameter p : inParameters)
                    {
                        final Column c = p.getColumn ();
                        pw.printf ("        sps = sps.addValue (\"%s\", value%s);%n", c.getPhysicalName (),
                            c.getName ());
                    }
                }
                pw.printf ("%n");
                pw.printf ("        // Call the stored procedure.%n");
                if (!shouldReturnValues)
                {
                    pw.printf ("        sjc.execute (sps);%n");
                }
                else
                {
                    pw.printf ("        final Map<String, Object> outValues = sjc.execute (sps);%n");
                    pw.printf ("%n");
                    pw.printf ("        // Gather out/return parameters.%n");
                    pw.printf ("        final Result r = new Result ();%n");
                    for (final Parameter p : outParameters)
                    {
                        final Column c = p.getColumn ();
                        final ColumnTypeInfo jti = EntityUtil.getColumnTypeInfo (c);

                        if (jti.m_javaConversionMethod != null)
                        {
                            pw.printf ("        r.m_value%s = %s (outValues.get (\"%s\"));%n", c.getName (),
                                jti.m_javaConversionMethod, c.getPhysicalName ());
                        }
                        else
                        {
                            pw.printf ("        r.m_value%s = (%s) outValues.get (\"%s\");%n", c.getName (),
                                jti.m_jdbcType, c.getPhysicalName ());
                        }
                    }

                    pw.printf ("%n");
                    pw.printf ("        return r;%n");
                }
            }
            else
            {
                pw.printf ("        final TypesafeStoredProcedure sp = new TypesafeStoredProcedure (m_dataSource);%n");
                pw.printf ("        %ssp.execute (%s);%n", shouldReturnValues ? "return " : "",
                    EntityUtil.getStringParametersArgsRef (inParameters));
                pw.printf ("    }%n");
                pw.printf ("%n");
                pw.printf ("    /** IExecutableDao implementatio */%n");
                pw.printf ("    @Override%n");
                pw.printf ("    public void performExecute (final %s p)%n", parametersClassName);
                pw.printf ("    {%n");
                pw.printf ("        execute (%s);%n", EntityUtil.getStringParameterClassParameters (inParameters, "p"));
                pw.printf ("    }%n");
                pw.printf ("%n");
                pw.printf (
                    "    /** Create a subclass of Spring's StoredProcedure to use its protected execute () method */%n");
                pw.printf ("    private static class TypesafeStoredProcedure extends StoredProcedure%n");
                pw.printf ("    {%n");
                pw.printf ("        public TypesafeStoredProcedure (final DataSource ds)%n");
                pw.printf ("        {%n");
                pw.printf ("            setDataSource (ds);%n");
                pw.printf ("            setFunction (%s);%n",
                    sp.getProcedureType () == StoredProcedure.ProcedureType.FUNCTION);
                pw.printf ("            setSql (STORED_PROCEDURE_NAME);%n");
                if (resultSets.size () > 0)
                {
                    pw.printf ("%n");
                    pw.printf ("            // Result sets.%n");
                    for (final StoredProcedureResultSet sprs : resultSets)
                    {
                        pw.printf (
                            "            declareParameter (new SqlReturnResultSet (VARIABLE_NAME_%s, %sDao.ROW_MAPPER));%n",
                            sprs.getName (), sprs.getType ());
                    }
                }
                if (parameters.size () > 0)
                {
                    pw.printf ("%n");
                    pw.printf ("            // Parameters.%n");
                    for (final Parameter p : parameters)
                    {
                        final Column c = p.getColumn ();

                        final String sqlParameterTypeName = EntityUtil.getSqlParameterTypeName (p.getDirection ());
                        if (sqlParameterTypeName != null)
                        {
                            final ColumnTypeInfo jti = EntityUtil.getColumnTypeInfo (c);
                            if (jti.m_rowMapperType != null)
                            {
                                pw.printf (
                                    "            declareParameter (new %s (VARIABLE_NAME_%s, %s, %sDao.ROW_MAPPER));%n",
                                    sqlParameterTypeName, c.getName (), jti.m_jdbcJavaSqlType, jti.m_rowMapperType);
                            }
                            else
                            {
                                pw.printf ("            declareParameter (new %s (VARIABLE_NAME_%s, %s));%n",
                                    sqlParameterTypeName, c.getName (), jti.m_jdbcJavaSqlType);
                            }
                        }
                    }
                }
                pw.printf ("%n");
                pw.printf ("            compile ();%n");
                pw.printf ("        }%n");
                pw.printf ("%n");
                pw.printf ("        public %s execute (%s)%n", returnTypeString,
                    EntityUtil.getStringParametersArgs (inParameters));
                pw.printf ("        {%n");
                pw.printf ("            final Map<String, Object> inParams = new HashMap<String, Object> ();%n");
                for (final Parameter p : inParameters)
                {
                    final Column c = p.getColumn ();

                    final String name = c.getName ();
                    pw.printf ("            inParams.put (VARIABLE_NAME_%s, value%s);%n", name, name);
                }
                pw.printf ("%n");
                if (!shouldReturnValues)
                {
                    pw.printf ("            DaoUtil.performExecute (this, inParams, STORED_PROCEDURE_NAME);%n");
                }
                else
                {
                    pw.printf (
                        "            final Map<?, ?> outParams = DaoUtil.performExecute (this, inParams, STORED_PROCEDURE_NAME);%n");
                    pw.printf ("%n");
                    pw.printf ("            final Result r = new Result ();%n");
                    for (final Parameter p : outParameters)
                    {
                        final Column c = p.getColumn ();
                        final ColumnTypeInfo jti = EntityUtil.getColumnTypeInfo (c);
                        final String name = c.getName ();

                        //                        pw.printf ("            r.m_value%s = %s outParams.get (VARIABLE_NAME_%s);%n", name, jti.m_javaCastExpression, name);
                        pw.printf (
                            "            r.m_value%s = %sDaoUtil.getOutParameter (outParams, VARIABLE_NAME_%s, STORED_PROCEDURE_NAME);%n",
                            name, jti.m_javaCastExpression == null ? "" : " " + jti.m_javaCastExpression, name);
                    }

                    for (final StoredProcedureResultSet sprs : resultSets)
                    {
                        final String name = sprs.getName ();
                        pw.printf (
                            "            r.m_resultSet%s = au.com.breakpoint.hedron.core.HcUtil.uncheckedCast (outParams.get (VARIABLE_NAME_%s));%n",
                            name, name);
                    }

                    pw.printf ("%n");
                    pw.printf ("            return r;%n");
                }
                pw.printf ("        }%n");
                pw.printf ("%n");
                pw.printf ("        /** Name of the stored procedure in the database */%n");
                pw.printf ("        private static final String STORED_PROCEDURE_NAME = \"%s\";%n",
                    storedProcedurePhysicalName);
                if (parameters.size () > 0 || resultSets.size () > 0)
                {
                    pw.printf ("%n");
                    pw.printf ("        /** Variable names used by the stored procedure */%n");
                    for (final Parameter p : parameters)
                    {
                        final Column c = p.getColumn ();

                        final String name = c.getName ();
                        pw.printf ("        private static final String VARIABLE_NAME_%s = \"%s\";%n", name, name);
                    }
                    for (final StoredProcedureResultSet sprs : resultSets)
                    {
                        final String name = sprs.getName ();
                        pw.printf ("        private static final String VARIABLE_NAME_%s = \"%s\";%n", name, name);
                    }
                }
            }
            pw.printf ("    }%n");
            pw.printf ("}%n");
        }

        return result;
    }

    @Override
    public List<String> generateEntity (final IRelation ir, final Schema schema)
    {
        final List<String> result = GenericFactory.newArrayList ();

        final String entityName = ir.getEntityName ();
        final String filepath = getEntityFilepath (String.format ("%s.java", entityName));
        final String outputPackage = m_options.m_outputPackage;

        try (final SmartFile pw = new SmartFileShowingProgress (filepath))
        {
            final List<Column> columns = ir.getColumns ();
            final List<Column> nonIdentityColumns = EntityUtil.getNonIdentityColumns (columns);
            final List<Column> nonPrimaryKeyNonIdentityColumns = EntityUtil.getNonPrimaryKeyNonIdentityColumns (ir);
            final Constraint pk = ir.getPrimaryConstraint ();// may be null
            final List<Column> pkColumns = pk != null ? pk.getColumns () : new ArrayList<Column> ();
            final String pkClassName = EntityUtil.getPrimaryKeyClass (ir);
            final String pkClassInstance = EntityUtil.getPrimaryKeyInstanceString (ir);

            pw.printf ("package %s.entity;%n", outputPackage);
            pw.printf ("%n");
            //            pw.printf ("import java.io.Serializable;%n");
            pw.printf ("import au.com.breakpoint.hedron.core.HcUtil;%n");
            pw.printf ("import au.com.breakpoint.hedron.core.dao.IEntity;%n");
            pw.printf ("import au.com.breakpoint.hedron.core.dao.BaseEntity;%n");
            if (pk != null && pk.getColumns ().size () > 1)
            {
                pw.printf ("import au.com.breakpoint.hedron.core.Tuple;%n");
            }
            pw.printf ("%n");
            pw.printf ("/**%n");
            pw.printf (" * Low-level entity object encapsulating a %s row.%n", ir.getPhysicalName ());
            pw.printf (" */%n");
            pw.printf ("public class %s extends BaseEntity<%s>%n", entityName, pkClassName);
            pw.printf ("{%n");
            pw.printf ("    /** Default constructor */%n");
            pw.printf ("    public %s ()%n", entityName);
            pw.printf ("    {%n");
            pw.printf ("    }%n");
            pw.printf ("%n");
            pw.printf ("    /**%n");
            pw.printf ("     * Copy constructor.%n");
            pw.printf ("     * @param rhs The object being copied (ie the right hand side of the assignment '=').%n");
            pw.printf ("     */%n");
            pw.printf ("    public %s (final %s rhs)%n", entityName, entityName);
            pw.printf ("    {%n");
            pw.printf ("        copyFrom (rhs);%n");
            pw.printf ("    }%n");
            pw.printf ("%n");
            pw.printf ("    /**%n");
            pw.printf ("     * IEntity implementation of data copy.%n");
            pw.printf ("     * @param rhs The object being copied (ie the right hand side of the assignment '=').%n");
            pw.printf ("     */%n");
            pw.printf ("    @Override%n");
            pw.printf ("    public void copyFrom (final IEntity<%s> rhs)%n", pkClassName);
            pw.printf ("    {%n");
            pw.printf ("        copyFrom ((%s) rhs);%n", entityName);
            pw.printf ("    }%n");
            pw.printf ("%n");
            pw.printf ("    /**%n");
            pw.printf ("     * Typesafe implementation of data copy.%n");
            pw.printf ("     * @param rhs The object being copied (ie the right hand side of the assignment '=').%n");
            pw.printf ("     */%n");
            pw.printf ("    public void copyFrom (final %s rhs)%n", entityName);
            pw.printf ("    {%n");
            for (final Column c : columns)
            {
                final String columnName = c.getName ();
                final ColumnTypeInfo jti = EntityUtil.getColumnTypeInfo (c);
                switch (jti.m_copyStyle)
                {
                    case ShallowCopy:
                    {
                        pw.printf ("        m_column%s = rhs.m_column%s;%n", columnName, columnName);
                        break;
                    }

                    case Duplicate:
                    {
                        pw.printf (
                            "        m_column%s = au.com.breakpoint.hedron.core.HcUtil.duplicate (rhs.m_column%s);%n",
                            columnName, columnName);
                        break;
                    }

                    default:
                    {
                        ThreadContext.assertFault (false, "Unsupported value [%s]", jti.m_copyStyle);
                        break;
                    }
                }
            }
            pw.printf ("    }%n");
            pw.printf ("%n");

            for (final Column c : columns)
            {
                final String columnName = c.getName ();
                final ColumnTypeInfo jti = EntityUtil.getColumnTypeInfo (c);

                pw.printf ("    /**%n");
                pw.printf ("     * Column %s accessors.%n", columnName);
                pw.printf ("     */%n");
                pw.printf ("    public %s get%s ()%n", jti.m_javaType, columnName);
                pw.printf ("    {%n");
                pw.printf ("        return m_column%s;%n", columnName);
                pw.printf ("    }%n");
                pw.printf ("%n");
                pw.printf ("    public void set%s (final %s column%s)%n", columnName, jti.m_javaType, columnName);
                pw.printf ("    {%n");
                if (jti.m_minValue < Long.MAX_VALUE || jti.m_maxValue > Long.MIN_VALUE || jti.m_size > -1)
                {
                    // Generate runtime checks on the data value.
                    if (c.isNullable ())
                    {
                        pw.printf ("        if (column%s != null && shouldEnforceColumnLimits ())%n", columnName);
                        pw.printf ("        {%n");
                        if (jti.m_minValue < Long.MAX_VALUE)
                        {
                            pw.printf (
                                "            au.com.breakpoint.hedron.core.context.ThreadContext.assertError (column%s >= MinValue%s, \"Column %s.%s value %%s cannot be less than %%s\", column%s, MinValue%s);%n",
                                columnName, columnName, c.getParent ().getEntityName (), columnName, columnName,
                                columnName, columnName);
                        }
                        if (jti.m_maxValue > Long.MIN_VALUE)
                        {
                            pw.printf (
                                "            au.com.breakpoint.hedron.core.context.ThreadContext.assertError (column%s <= MaxValue%s, \"Column %s.%s value %%s cannot be greater than %%s\", column%s, MaxValue%s);%n",
                                columnName, columnName, c.getParent ().getEntityName (), columnName, columnName,
                                columnName, columnName);
                        }
                        if (jti.m_size > -1)
                        {
                            pw.printf (
                                "            au.com.breakpoint.hedron.core.context.ThreadContext.assertError (column%s.length () <= Size%s, \"Column %s.%s value [%%s] cannot be longer than %%s characters\", column%s, Size%s);%n",
                                columnName, columnName, c.getParent ().getEntityName (), columnName, columnName,
                                columnName);
                        }
                        pw.printf ("        }%n");
                    }
                    else
                    {
                        if (jti.m_nonPrimitiveTypeJavaLangType)
                        {
                            pw.printf (
                                "        au.com.breakpoint.hedron.core.context.ThreadContext.assertError (column%s != null, \"Column %s.%s value is classified as 'mandatory'; it cannot be null\");%n",
                                columnName, c.getParent ().getEntityName (), columnName);
                        }
                        if (jti.m_minValue < Long.MAX_VALUE)
                        {
                            pw.printf (
                                "        au.com.breakpoint.hedron.core.context.ThreadContext.assertError (!shouldEnforceColumnLimits () || column%s >= MinValue%s, \"Column %s.%s value %%s cannot be less than %%s\", column%s, MinValue%s);%n",
                                columnName, columnName, c.getParent ().getEntityName (), columnName, columnName,
                                columnName);
                        }
                        if (jti.m_maxValue > Long.MIN_VALUE)
                        {
                            pw.printf (
                                "        au.com.breakpoint.hedron.core.context.ThreadContext.assertError (!shouldEnforceColumnLimits () || column%s <= MaxValue%s, \"Column %s.%s value %%s cannot be greater than %%s\", column%s, MaxValue%s);%n",
                                columnName, columnName, c.getParent ().getEntityName (), columnName, columnName,
                                columnName);
                        }
                        if (jti.m_size > -1)
                        {
                            pw.printf (
                                "        au.com.breakpoint.hedron.core.context.ThreadContext.assertError (!shouldEnforceColumnLimits () || column%s.length () <= Size%s, \"Column %s.%s value [%%s] cannot be longer than %%s characters\", column%s, Size%s);%n",
                                columnName, columnName, c.getParent ().getEntityName (), columnName, columnName,
                                columnName);
                        }
                    }
                }
                pw.printf ("        m_column%s = column%s;%n", columnName, columnName);
                pw.printf ("    }%n");
                pw.printf ("%n");
            }
            pw.printf ("%n");
            pw.printf ("    /** IEntity implementation of getPrimaryKey () */%n");
            pw.printf ("    @Override%n");
            pw.printf ("    public %s getPrimaryKey ()%n", pkClassName);
            pw.printf ("    {%n");
            pw.printf ("        return %s;%n", pkClassInstance);
            pw.printf ("    }%n");
            pw.printf ("%n");
            pw.printf ("    /** IEntity implementation of getColumnValues () */%n");
            pw.printf ("    @Override%n");
            pw.printf ("    @SuppressWarnings (\"incomplete-switch\")%n");
            pw.printf ("    public Object[] getColumnValues (final ColumnType columnType)%n");
            pw.printf ("    {%n");
            pw.printf ("        Object[] values = null;%n");
            pw.printf ("%n");
            pw.printf ("        switch (columnType)%n");
            pw.printf ("        {%n");
            final boolean allAreNonIdentity = columns.size () == nonIdentityColumns.size ();
            final boolean allAreUpdatable = columns.size () == nonPrimaryKeyNonIdentityColumns.size ();
            pw.printf ("            case All:%n");
            if (allAreNonIdentity)
            {
                pw.printf ("            case NonIdentity:%n");
            }
            if (allAreUpdatable)
            {
                pw.printf ("            case Updatable:%n");
            }
            pw.printf ("            {%n");
            pw.printf ("                values = new Object[]%n");
            pw.printf ("                {%n");
            generateColumnValueCode (pw, columns);
            pw.printf ("                };%n");
            pw.printf ("                break;%n");
            pw.printf ("            }%n");
            if (pk != null)
            {
                pw.printf ("%n");
                pw.printf ("            case PrimaryKey:%n");
                pw.printf ("            {%n");
                pw.printf ("                values = new Object[]%n");
                pw.printf ("                {%n");
                generateColumnValueCode (pw, pkColumns);
                pw.printf ("                };%n");
                pw.printf ("                break;%n");
                pw.printf ("            }%n");
            }
            if (!allAreNonIdentity)
            {
                pw.printf ("%n");
                pw.printf ("            case NonIdentity:%n");
                pw.printf ("            {%n");
                pw.printf ("                values = new Object[]%n");
                pw.printf ("                {%n");
                generateColumnValueCode (pw, nonIdentityColumns);
                pw.printf ("                };%n");
                pw.printf ("                break;%n");
                pw.printf ("            }%n");
            }
            if (!allAreUpdatable)
            {
                pw.printf ("%n");
                pw.printf ("            case Updatable:%n");
                pw.printf ("            {%n");
                pw.printf ("                values = new Object[]%n");
                pw.printf ("                {%n");
                generateColumnValueCode (pw, nonPrimaryKeyNonIdentityColumns);
                pw.printf ("                };%n");
                pw.printf ("                break;%n");
                pw.printf ("            }%n");
            }
            pw.printf ("        }%n");
            pw.printf ("        return values;%n");
            pw.printf ("    }%n");
            ////// now in BaseEntity
            //pw.printf ("%n");
            //pw.printf ("    @Override%n");
            //pw.printf ("    public boolean equals (final Object o)%n");
            //pw.printf ("    {%n");
            //pw.printf ("        boolean isEqual = false;%n");
            //pw.printf ("        if (this == o)%n");
            //pw.printf ("        {%n");
            //pw.printf ("            isEqual = true;%n");
            //pw.printf ("        }%n");
            //pw.printf ("        else if (o != null && getClass () == o.getClass ())%n");
            //pw.printf ("        {%n");
            //pw.printf ("            final %s eRhs = (%s) o;%n", entityName, entityName);
            //pw.printf ("            isEqual = ");
            //
            //if (true)
            //{
            //    int i = 0;
            //    for (final Column c : columns)
            //    {
            //        final String columnName = c.getName ();
            //        final ColumnTypeInfo jti = EntityUtil.getColumnTypeInfo (c);
            //
            //        if (i > 0)
            //        {
            //            pw.printf (" &&%n                ");
            //        }
            //        pw.printf (jti.m_equalityExpression, columnName, columnName);
            //        ++i;
            //    }
            //}
            //
            //pw.printf (";%n");
            //pw.printf ("        }%n");
            //pw.printf ("        return isEqual;%n");
            //pw.printf ("    }%n");
            //pw.printf ("%n");
            //pw.printf ("    @Override%n");
            //pw.printf ("    public int hashCode ()%n");
            //pw.printf ("    {%n");
            //pw.printf ("        // See Effective Java 2nd edition Item 9.%n");
            //pw.printf ("        int result = 17;%n");
            //for (final Column c : columns)
            //{
            //    final String columnName = c.getName ();
            //    final ColumnTypeInfo jti = EntityUtil.getColumnTypeInfo (c);
            //
            //    pw.printf ("        result = 31 * result + %s;%n",
            //        String.format (jti.m_hashCodeExpression, columnName, columnName));
            //}
            //pw.printf ("        return result;%n");
            //pw.printf ("    }%n");

            // Change to generic tostring in BaseEntity
            //pw.printf ("%n");
            //pw.printf ("    @Override%n");
            //pw.printf ("    public String toString ()%n");
            //pw.printf ("    {%n");
            //pw.printf ("        return String.format (\"%s =", entityName);
            //for (final Column c : columns)
            //{
            //    final String columnName = c.getName ();
            //    pw.printf (" %s[%%s]", columnName);
            //}
            //pw.printf ("\"");
            //for (final Column c : columns)
            //{
            //    final String columnName = c.getName ();
            //    pw.printf (", m_column%s", columnName);
            //}
            //pw.printf (");%n");
            //pw.printf ("    }%n");
            //pw.printf ("%n");

            pw.printf ("%n");
            pw.printf ("    /** Logical identifiers for the columns, used in WhereElement, SetElement, etc */%n");
            pw.printf ("    public static class Columns%n");
            pw.printf ("    {%n");
            if (true)
            {
                int i = 0;
                for (final Column c : columns)
                {
                    final String columnName = c.getName ();
                    pw.printf ("        public static final int %s = %d;%n", columnName, i);
                    ++i;
                }
            }
            pw.printf ("    }%n");
            for (final Column c : columns)
            {
                final String columnName = c.getName ();
                final ColumnTypeInfo jti = EntityUtil.getColumnTypeInfo (c);

                pw.printf ("%n");
                pw.printf ("    /**%n", columnName);
                pw.printf ("     * Column %s.%n", columnName);
                pw.printf ("     */%n", columnName);

                if (jti.m_minValue < Long.MAX_VALUE)
                {
                    final String valueSymbol = String.format ("MinValue%s", columnName);
                    pw.printf ("    public static final %s %s = %s%s;%n", jti.m_javaTypeOfLimitValue, valueSymbol,
                        jti.m_minValue, jti.m_javaTypeConstantSuffix);
                    m_attributes.add (new Attribute (entityName, valueSymbol, "long"));
                }
                if (jti.m_maxValue > Long.MIN_VALUE)
                {
                    final String valueSymbol = String.format ("MaxValue%s", columnName);
                    pw.printf ("    public static final %s %s = %s%s;%n", jti.m_javaTypeOfLimitValue, valueSymbol,
                        jti.m_maxValue, jti.m_javaTypeConstantSuffix);
                    m_attributes.add (new Attribute (entityName, valueSymbol, "long"));
                }
                if (jti.m_precision != null)
                {
                    final String valueSymbol = String.format ("Precision%s", columnName);
                    pw.printf ("    public static final int %s = %s;%n", valueSymbol, jti.m_precision);
                    m_attributes.add (new Attribute (entityName, valueSymbol, "int"));
                }
                if (jti.m_scale != null)
                {
                    final String valueSymbol = String.format ("Scale%s", columnName);
                    pw.printf ("    public static final int %s = %s;%n", valueSymbol, jti.m_scale);
                    m_attributes.add (new Attribute (entityName, valueSymbol, "int"));
                }
                if (jti.m_size != -1)
                {
                    final String valueSymbol = String.format ("Size%s", columnName);
                    pw.printf ("    public static final int %s = %s;%n", valueSymbol, jti.m_size);
                    m_attributes.add (new Attribute (entityName, valueSymbol, "int"));
                }

                final String valueSymbol = String.format ("IsNullable%s", columnName);
                pw.printf ("    public static final boolean %s = %s;%n", valueSymbol, c.isNullable ());
                m_attributes.add (new Attribute (entityName, valueSymbol, "boolean"));

                pw.printf ("    private %s m_column%s;%s%n", jti.m_javaType, columnName,
                    "\t// " + c.getRequirementDescription ());
            }

            pw.printf ("%n");
            pw.printf ("    private static final long serialVersionUID = 4508429214973765867L;%n");
            pw.printf ("}%n");
        }

        return result;
    }

    @Override
    public List<String> postGenerate ()
    {
        final List<String> feedback = GenericFactory.newArrayList ();

        if (true)
        {
            // Generate Definitions.java
            final String filepath = EntityUtil.getFilepath (m_options.m_outputBaseFilepath, "Definitions.java");
            feedback.addAll (generateDefinitions (filepath, m_options.m_outputPackage));
        }

        if (true)
        {
            // Generate Enums.java
            final String filepath = EntityUtil.getFilepath (m_options.m_outputBaseFilepath, "Enums.java");
            feedback.addAll (generateEnums (filepath, m_options.m_outputPackage));
        }

        return feedback;
    }

    @Override
    public List<String> preGenerate (final Options options)
    {
        m_options = options;

        m_entityDirectory = String.format ("%s/%s", m_options.m_outputBaseFilepath, DIRECTORY_ENTITY);
        HcUtilFile.ensureDirectoryExists (m_entityDirectory);

        m_daoDirectory = String.format ("%s/%s", m_options.m_outputBaseFilepath, DIRECTORY_DAO);
        HcUtilFile.ensureDirectoryExists (m_daoDirectory);

        return GenericFactory.newArrayList ();
    }

    private void generateColumnValueCode (final SmartFile pw, final List<Column> columns)
    {
        int i = 0;
        for (final Column c : columns)
        {
            final String columnName = c.getName ();
            pw.printf ("                    get%s ()%s%n", columnName, i == columns.size () - 1 ? "" : ",");
            ++i;
        }
    }

    private List<String> generateDefinitions (final String filepath, final String outputPackage)
    {
        final List<String> result = GenericFactory.newArrayList ();

        try (final SmartFile pw = new SmartFileShowingProgress (filepath))
        {
            pw.printf ("package %s;%n", outputPackage);
            pw.printf ("%n");
            pw.printf ("/**%n");
            pw.printf (" * Class containing database metrics as properties.%n");
            pw.printf (" */%n");
            pw.printf ("public class Definitions%n");
            pw.printf ("{%n");
            pw.printf ("    // Selected entity properties for the schema.%n");

            if (m_options.m_generateBeanStyleDefinitions)
            {
                final List<Attribute> attributes = GenericFactory.newArrayList (m_attributes);

                // Sort by entity, name.
                Collections.sort (attributes, comparing (Attribute::getEntityName).thenComparing (Attribute::getName));

                for (final Attribute a : attributes)
                {
                    pw.printf ("%n");
                    pw.printf ("    public %s get%s_%s ()%n", a.getType (), a.getEntityName (), a.getName ());
                    pw.printf ("    {%n");
                    pw.printf ("        return %s.entity.%s.%s;%n", outputPackage, a.getEntityName (), a.getName ());
                    pw.printf ("    }%n");
                }
            }

            // Sort enums alphabetically, since m_enums is filled in asynchronously they are in any order.
            Collections.sort (m_enums, comparing (DbEnum::getName));

            if (true)
            {
                final List<EnumValue> enumValues = GenericFactory.newArrayList ();

                for (final DbEnum en : m_enums)
                {
                    for (final EnumValue v : en.getEnumValues ())
                    {
                        enumValues.add (v);
                    }
                }

                // Sort the results by name.
                final Comparator<EnumValue> cmd =
                    comparing ( (final EnumValue a) -> a.getParent ().getName ()).thenComparing (EnumValue::getValue);
                Collections.sort (enumValues, cmd);

                pw.printf ("%n");
                pw.printf ("    // Enumeration constant values.%n");
                for (final EnumValue v : enumValues)
                {
                    final String symbol = stringToSymbol (v.getTitle ());
                    pw.printf ("    public static final int VALUE_%s_%s = %s;%n", v.getParent ().getName (), symbol,
                        v.getValue ());
                }

                // TODO _ handle sparse values or values that don't start at 0
                pw.printf ("%n");
                pw.printf ("    // Enumeration constant description strings.%n");
                for (final DbEnum en : m_enums)
                {
                    final String symbol = en.getName ();
                    pw.printf ("    public static final String[] ENUM_DESCRIPTIONS_%s =%n", symbol);
                    pw.printf ("    {%n");

                    int i = 0;
                    for (final EnumValue v : en.getEnumValues ())
                    {
                        pw.printf ("        \"%s\"%s   // %s%n", v.getTitle (),
                            i++ < en.getEnumValues ().size () - 1 ? "," : "", v.getValue ());
                    }

                    pw.printf ("    };%n");
                }
            }

            pw.printf ("}%n");
        }

        return result;
    }

    private List<String> generateEnums (final String filepath, final String outputPackage)
    {
        final List<String> result = GenericFactory.newArrayList ();

        try (final SmartFile pw = new SmartFileShowingProgress (filepath))
        {
            pw.printf ("package %s;%n", outputPackage);
            pw.printf ("%n");
            pw.printf ("/**%n");
            pw.printf (" * Class containing database metrics as enums.%n");
            pw.printf (" */%n");
            pw.printf ("public class Enums%n");
            pw.printf ("{%n");
            pw.printf ("    // Selected entity properties for the schema.%n");

            // Sort enums alphabetically, since m_enums is filled in asynchronously they are in any order.
            Collections.sort (m_enums, comparing (DbEnum::getName));

            int i = 0;
            for (final DbEnum en : m_enums)
            {
                if (i++ > 0)
                {
                    pw.printf ("%n");
                }

                final String enumName = en.getName ();
                pw.printf ("    public static enum %s%n", enumName);
                pw.printf ("    {%n");
                pw.printf ("        ");

                int j = 0;
                for (final EnumValue e : en.getEnumValues ())
                {
                    if (j++ > 0)
                    {
                        pw.printf (",%n");
                        pw.printf ("        ");
                    }
                    final String symbol = stringToSymbol (e.getTitle ());
                    pw.printf ("%s (%s)", symbol, e.getValue ());
                }
                pw.printf (";%n");
                pw.printf ("%n");
                pw.printf ("        private %s (int value)%n", enumName);
                pw.printf ("        {%n");
                pw.printf ("            m_value = value;%n");
                pw.printf ("        }%n");
                pw.printf ("%n");
                pw.printf ("        public int getValue ()%n");
                pw.printf ("        {%n");
                pw.printf ("            return m_value;%n");
                pw.printf ("        }%n");
                pw.printf ("%n");
                pw.printf ("        private final int m_value;%n");
                pw.printf ("    }%n");
            }

            pw.printf ("}%n");
        }

        return result;
    }

    private List<String> generateRelationalDao (final IRelation ir, final List<Capability> capabilities,
        final String sqlString, final boolean shouldPreserveNewLinesInSQL, final List<Parameter> parameters,
        final String suffix)
    {
        final List<String> result = GenericFactory.newArrayList ();

        final List<Column> columns = ir.getColumns ();
        final String entityPhysicalName = ir.getPhysicalName ();

        final boolean mapsToAnotherEntity = EntityUtil.mapsToAnotherEntity (ir);
        final String daoName = ir.getName ();
        final String entityName = ir.getEntityName ();
        final String filepath = getDaoFilepath (String.format ("%s%sDao.java", daoName, suffix));
        final String outputPackage = m_options.m_outputPackage;

        final List<Column> nonIdentityColumns = EntityUtil.getNonIdentityColumns (columns);
        final Constraint pk = ir.getPrimaryConstraint ();// may be null
        final List<Column> pkColumns = pk != null ? pk.getColumns () : new ArrayList<Column> ();
        final List<Column> nonPkColumns = EntityUtil.getNonPrimaryKeyColumns (ir);
        final boolean canUpdate = capabilities.contains (Capability.UPDATE) && pkColumns.size () > 0;
        final boolean needUpdate = canUpdate && nonPkColumns.size () > 0;
        final boolean canCreate = capabilities.contains (Capability.CREATE);
        final boolean canRead = capabilities.contains (Capability.READ);
        final boolean canDelete = capabilities.contains (Capability.DELETE);
        //final boolean canCrud = canCreate && canRead && needUpdate && canDelete;
        final String pkClassName = EntityUtil.getPrimaryKeyClass (ir);

        try (final SmartFile pw = new SmartFileShowingProgress (filepath))
        {
            pw.printf ("package %s.dao;%n", outputPackage);
            pw.printf ("%n");
            pw.printf ("import java.sql.ResultSet;%n");
            pw.printf ("import java.sql.SQLException;%n");
            pw.printf ("import javax.sql.DataSource;%n");
            pw.printf ("import java.util.List;%n");
            pw.printf ("import org.springframework.jdbc.core.RowMapper;%n");
            if (pk != null && pk.getColumns ().size () > 1)
            {
                pw.printf ("import au.com.breakpoint.hedron.core.Tuple;%n");
            }
            pw.printf ("import au.com.breakpoint.hedron.core.dao.OrderByElement;%n");
            pw.printf ("import au.com.breakpoint.hedron.core.dao.WhereElement;%n");
            pw.printf ("import au.com.breakpoint.hedron.core.dao.FetchSql;%n");
            pw.printf ("import au.com.breakpoint.hedron.core.dao.UpdateSql;%n");
            pw.printf ("import au.com.breakpoint.hedron.core.dao.BaseEntityDao;%n");
            pw.printf ("import au.com.breakpoint.hedron.core.dao.DaoUtil;%n");
            pw.printf ("import au.com.breakpoint.hedron.core.dao.DaoUtil.SqlData;%n");
            pw.printf ("import %s.entity.%s;%n", outputPackage, entityName);
            if (canUpdate)
            {
                pw.printf ("import au.com.breakpoint.hedron.core.dao.SetElement;%n");
            }
            if (canCreate)
            {
                if (m_shouldUseSimpleJdbcInsert)
                {
                    pw.printf ("import java.util.HashMap;%n");
                    pw.printf ("import java.util.Map;%n");
                    pw.printf ("import org.springframework.jdbc.core.simple.SimpleJdbcInsert;%n");
                }
                else
                {
                    pw.printf ("import au.com.breakpoint.hedron.core.context.ThreadContext;%n");
                }
            }
            pw.printf ("%n");
            pw.printf ("/**%n");
            pw.printf (
                " * Low-level DAO object encapsulating the %s relation. Encapsulates Spring JDBC template database access.%n",
                entityPhysicalName);
            pw.printf (" */%n");
            pw.printf ("@SuppressWarnings (\"unused\")%n");
            pw.printf ("public class %s%sDao extends BaseEntityDao<%s, %s>%n", daoName, suffix, entityName,
                pkClassName);
            pw.printf ("{%n");
            if (!capabilities.isEmpty ())
            {
                pw.printf ("    public %s%sDao (final DataSource dataSource)%n", daoName, suffix);
                pw.printf ("    {%n");
                pw.printf ("        super (dataSource);%n");
                pw.printf ("    }%n");
            }
            if (canRead)
            {
                if (parameters == null)
                {
                    pw.printf ("%n");
                    pw.printf ("    /**%n");
                    pw.printf ("     * Fetches all rows of the %s relation.%n", entityPhysicalName);
                    pw.printf ("     * %n");
                    pw.printf ("     * @return Collection of %s entities%n", entityName);
                    pw.printf ("     */%n");
                    pw.printf ("    @Override%n");
                    pw.printf ("    public List<%s> fetch (final OrderByElement... orderByElements)%n", entityName);
                    pw.printf ("    {%n");
                    pw.printf (
                        "        return DaoUtil.performFetch (m_dataSource, %sDao.ROW_MAPPER, SQL_FRAGMENT_SELECT_FROM, COLUMN_NAMES, orderByElements);%n",
                        entityName);
                    pw.printf ("    }%n");
                    pw.printf ("%n");
                    pw.printf ("    /**%n");
                    pw.printf (
                        "     * Fetches the rows of the %s relation that satisfy the criteria in the <i>whereElements</i> parameter.%n",
                        entityPhysicalName);
                    pw.printf ("     * %n");
                    pw.printf ("     * @param whereElements%n");
                    pw.printf (
                        "     *     Specified criteria to be added into the where clause. These result in SQL 'and' clauses. There is%n");
                    pw.printf (
                        "     *     currently no way to directly express 'or' clauses, or more complex expressions. Use a custom query or%n");
                    pw.printf ("     *     stored procedure instead.%n");
                    pw.printf ("     * %n");
                    pw.printf ("     * @return Collection of %s entities%n", entityName);
                    pw.printf ("     */%n");
                    pw.printf ("    @Override%n");
                    pw.printf (
                        "    public List<%s> fetch (final WhereElement[] whereElements, final OrderByElement... orderByElements)%n",
                        entityName);
                    pw.printf ("    {%n");
                    pw.printf (
                        "        return DaoUtil.performFetch (m_dataSource, %sDao.ROW_MAPPER, SQL_FRAGMENT_SELECT_FROM, COLUMN_NAMES, whereElements, orderByElements);%n",
                        entityName);
                    pw.printf ("    }%n");
                    pw.printf ("%n");
                    pw.printf ("    /**%n");
                    pw.printf (
                        "     * Fetches the rows of the %s relation that satisfy the criteria in the <i>whereElements</i> parameter.%n",
                        entityPhysicalName);
                    pw.printf ("     * %n");
                    pw.printf ("     * @param sql%n");
                    pw.printf (
                        "     *     A convenient readable encapsulation of sql where clauses and order by statements.%n");
                    pw.printf ("     * %n");
                    pw.printf ("     * @return Collection of %s entities%n", entityName);
                    pw.printf ("     */%n");
                    pw.printf ("    public List<%s> fetch (final FetchSql sql)%n", entityName);
                    pw.printf ("    {%n");
                    pw.printf ("        return fetch (sql.getWhereElements (), sql.getOrderByElements ());%n");
                    pw.printf ("    }%n");
                }
                else
                {
                    if (EntityUtil.hasConvenienceTypes (parameters))
                    {
                        pw.printf ("%n");
                        pw.printf ("    /**%n");
                        pw.printf (
                            "     * Convenience overload using intrinsic values. You must make sure the parameter values%n");
                        pw.printf ("     * will fit into the intrinsic parameters.%n");
                        pw.printf ("     */%n");
                        pw.printf ("    public List<%s> fetch (%s)%n", entityName,
                            EntityUtil.getStringConvenienceParametersArgs (parameters));
                        pw.printf ("    {%n");
                        pw.printf ("        return fetch (%s);%n",
                            EntityUtil.getStringConvenienceParameters (parameters));
                        pw.printf ("    }%n");
                    }

                    pw.printf ("%n");
                    pw.printf ("    /**%n");
                    pw.printf ("     * Fetches all rows resulting from the SQL query.%n");
                    pw.printf ("     * %n");
                    pw.printf ("     * @return Collection of %s entities%n", entityName);
                    pw.printf ("     */%n");
                    pw.printf ("    public List<%s> fetch (%s)%n", entityName,
                        EntityUtil.getStringParametersArgs (parameters));
                    pw.printf ("    {%n");
                    pw.printf (
                        "        return DaoUtil.performFetch (m_dataSource, %sDao.ROW_MAPPER, SQL_FRAGMENT_SELECT_FROM, %s);%n",
                        entityName, EntityUtil.getStringParameterValues (parameters));
                    pw.printf ("    }%n");
                }
                if (pk != null)
                {
                    final boolean canOverload = EntityUtil.allowsOverloadDisambiguation (pkColumns);
                    final boolean generateSeparateOverload = canOverload || pkColumns.size () > 1;

                    pw.printf ("%n");
                    pw.printf ("    /**%n");
                    pw.printf ("     * Fetches the row of the %s relation for the specified primary key.%n",
                        entityPhysicalName);
                    pw.printf ("     * %n");
                    pw.printf ("     * @return The specified %s entity, or null if it does not exist%n", entityName);
                    pw.printf ("     */%n");
                    if (!generateSeparateOverload)
                    {
                        pw.printf ("    @Override%n");
                    }
                    pw.printf ("    public %s fetchByPrimaryKey (%s)%n", entityName,
                        EntityUtil.getStringColumnsArgsWithType (pkColumns));
                    pw.printf ("    {%n");
                    pw.printf ("        final WhereElement[] whereElements = { %s };%n",
                        EntityUtil.getStringColumnClauses (entityName, pkColumns, false, "WhereElement"));
                    pw.printf ("        final List<%s> results = fetch (whereElements);%n", entityName);
                    pw.printf ("%n");
                    pw.printf ("        return results.size () == 1 ? results.get (0) : null;%n");
                    pw.printf ("    }%n");
                    pw.printf ("%n");

                    // Don't generate this method if javatype==javaobjecttype since it leads to
                    // an overload problem. The native fetchByPrimaryKey () satisfies the IEntityDao
                    // method signature already.
                    if (generateSeparateOverload)
                    {
                        pw.printf ("    /**%n");
                        pw.printf (
                            "     * IEntityDao implementation, fetches the row of the entity table for the specified primary key.%n");
                        pw.printf ("     *%n");
                        pw.printf ("     * @return The specified TEntity entity, or null if it does not exist%n");
                        pw.printf ("     */%n");
                        pw.printf ("    @Override%n");
                        pw.printf ("    public %s fetchByPrimaryKey (final %s id)%n", entityName, pkClassName);
                        pw.printf ("    {%n");
                        if (pkColumns.size () > 1)
                        {
                            // Tuple handling
                            pw.printf ("        return fetchByPrimaryKey (");

                            for (int i = 0; i < pkColumns.size (); ++i)
                            {
                                pw.printf ("id.getE%s ()%s", i, i == pkColumns.size () - 1 ? "" : ", ");
                            }
                            pw.printf (");%n");
                        }
                        else
                        {
                            final ColumnTypeInfo jti = EntityUtil.getColumnTypeInfo (pkColumns.get (0));

                            // Only 1 arg: must be casting an intrinsic.
                            pw.printf ("        return fetchByPrimaryKey ((%s) id);%n", jti.m_javaType);

                        }
                        pw.printf ("    }%n");
                    }
                }
            }
            if (canCreate)
            {
                pw.printf ("%n");
                pw.printf ("    /**%n");
                pw.printf ("     * Inserts multiple rows into the %s table.%n", entityPhysicalName);
                pw.printf ("     * @param es entities corresponding to the rows to be inserted%n");
                pw.printf ("     */%n");
                pw.printf ("    @Override%n");
                pw.printf ("    public void insert (final List<%s> es)%n", entityName);
                pw.printf ("    {%n");
                pw.printf (
                    "        DaoUtil.performInsertBatch (m_dataSource, es.toArray (new %s[es.size ()]), SQL_INSERT);%n",
                    entityName);
                pw.printf ("    }%n");
                pw.printf ("%n");
                pw.printf ("    /**%n");
                pw.printf ("     * Inserts a row into the %s table.%n", entityPhysicalName);
                pw.printf ("     * %n");
                pw.printf ("     * @param e entity corresponding to the row to be inserted.%n");
                pw.printf ("     */%n");
                pw.printf ("    @Override%n");
                pw.printf ("    public void insert (final %s e)%n", entityName);
                pw.printf ("    {%n");
                if (m_shouldUseSimpleJdbcInsert)
                {
                    /////////// NOT CURRENTLY USED //////////
                    pw.printf (
                        "        SimpleJdbcInsert sji = new SimpleJdbcInsert (m_dataSource).withTableName (ENTITY_NAME);%n");
                    pw.printf ("    %n");
                    pw.printf ("        final Map<String, Object> columnValues = new HashMap<String, Object> (%s);%n",
                        columns.size ());
                    for (final Column c : nonIdentityColumns)
                    {
                        final String columnName = c.getName ();
                        pw.printf ("        columnValues.put (COLUMN_NAMES[%s.Columns.%s], e.get%s ());%n", entityName,
                            columnName, columnName);
                    }
                    pw.printf ("    %n");
                    pw.printf ("        sji.execute (columnValues);%n");
                }
                else
                {
                    pw.printf (
                        "        final int updateCount = DaoUtil.performInsert (m_dataSource, e, SQL_INSERT);%n");
                    pw.printf ("        ThreadContext.assertError (updateCount == 1, \"%sDao insert failed\");%n",
                        entityName);
                }
                pw.printf ("    }%n");
            }
            if (needUpdate)
            {
                pw.printf ("%n");
                pw.printf ("    /**%n");
                pw.printf ("     * Updates multiple rows in the %s table.%n", entityPhysicalName);
                pw.printf ("     * @param es entities corresponding to the rows to be inserted%n");
                pw.printf ("     * %n");
                pw.printf ("     * @return the numbers of rows affected by the update%n");
                pw.printf ("     */%n");
                pw.printf ("    @Override%n");
                pw.printf ("    public void update (final List<%s> es)%n", entityName);
                pw.printf ("    {%n");
                pw.printf (
                    "        DaoUtil.performUpdateBatch (m_dataSource, es.toArray (new %s[es.size ()]), SQL_UPDATE_ALL_NON_PK);%n",
                    entityName);
                pw.printf ("    }%n");
                pw.printf ("%n");
                pw.printf ("    /**%n");
                pw.printf (
                    "     * Updates the %s table columns specified by newValues according to the criteria in whereElements.%n",
                    entityPhysicalName);
                pw.printf ("     * %n");
                pw.printf ("     * @param newValues%n");
                pw.printf (
                    "     *     Collection of column/value pairs corresponding to the 'set' part of the update SQL.%n");
                pw.printf ("     * @param whereElements%n");
                pw.printf (
                    "     *     Specified criteria to be added into the where clause. These result in SQL 'and' clauses. There is%n");
                pw.printf (
                    "     *     currently no way to directly express 'or' clauses, or more complex expressions. Use a custom query or%n");
                pw.printf ("     *     stored procedure instead.%n");
                pw.printf ("     * %n");
                pw.printf ("     * @return the numbers of rows affected by the update%n");
                pw.printf ("     */%n");
                pw.printf ("    @Override%n");
                pw.printf (
                    "    public int update (final SetElement[] newValues, final WhereElement[] whereElements)%n");
                pw.printf ("    {%n");
                pw.printf (
                    "        return DaoUtil.performUpdate (m_dataSource, ENTITY_NAME, COLUMN_NAMES, newValues, whereElements);%n");
                pw.printf ("    }%n");
                pw.printf ("%n");
                pw.printf ("    /**%n");
                pw.printf (
                    "     * Updates the %s table columns specified by newValues according to the criteria in whereElements.%n",
                    entityPhysicalName);
                pw.printf ("     * %n");
                pw.printf ("     * @param sql%n");
                pw.printf (
                    "     *     A convenient readable encapsulation of update set statements and where clauses.%n");
                pw.printf ("     * %n");
                pw.printf ("     * @return the numbers of rows affected by the update%n");
                pw.printf ("     */%n");
                pw.printf ("    public int update (final UpdateSql sql)%n");
                pw.printf ("    {%n");
                pw.printf ("        return update (sql.getSetElements (), sql.getWhereElements ());%n");
                pw.printf ("    }%n");
                if (pk != null)
                {
                    pw.printf ("%n");
                    pw.printf ("    /**%n");
                    pw.printf (
                        "     * Updates the %s table row corresponding to the specified entity using its primary key value.%n",
                        entityPhysicalName);
                    pw.printf ("     * %n");
                    pw.printf ("     * @param e entity corresponding to the row to be updated.%n");
                    pw.printf ("     * %n");
                    pw.printf ("     * @return whether or not a row was affected by the update%n");
                    pw.printf ("     */%n");
                    pw.printf ("    @Override%n");
                    pw.printf ("    public boolean update (final %s e)%n", entityName);
                    pw.printf ("    {%n");
                    //                    pw.printf ("        final SetElement[] newValues = { %s };%n", getStringColumnClauses (entityName, nonPkColumns, true, "SetElement"));
                    //                    pw.printf ("        final WhereElement[] whereElements = { %s };%n", getStringColumnClauses (entityName, pkColumns, true, "WhereElement"));
                    //                    pw.printf ("        return update (newValues, whereElements) == 1;%n");

                    //                    pw.printf ("%s%n", EntityUtil.getStringUpdateClauses (entityName, pkColumns, nonPkColumns, true, null, "sql"));
                    //                    pw.printf ("%n");
                    pw.printf ("        return DaoUtil.performUpdate (m_dataSource, e, SQL_UPDATE_ALL_NON_PK) == 1;%n");
                    pw.printf ("    }%n");
                    final Column optimisticLockColumn = ir.getOptimisticLockColumn ();
                    if (optimisticLockColumn != null)
                    {
                        final String optimisticLockColumnName = optimisticLockColumn.getName ();
                        final ColumnTypeInfo jti = EntityUtil.getColumnTypeInfo (optimisticLockColumn);

                        pw.printf ("%n");
                        pw.printf ("    /**%n");
                        pw.printf (
                            "     * Updates the %s table row corresponding to the specified entity using its primary key value,%n",
                            entityPhysicalName);
                        pw.printf ("     * subject to the currentness of the optimistic lock value.%n");
                        pw.printf ("     * %n");
                        pw.printf ("     * @param e Data entity corresponding to the row to be updated.%n");
                        pw.printf (
                            "     * @param column%s The optimistic lock value. If this is not current, the update will not proceed.%n",
                            optimisticLockColumnName);
                        pw.printf ("     * %n");
                        pw.printf ("     * @return whether or not a row was affected by the update%n");
                        pw.printf ("     */%n");
                        final String optimisticLockArg =
                            String.format ("final %s column%s", jti.m_javaType, optimisticLockColumnName);
                        pw.printf ("    public boolean update (final %s e, %s)%n", entityName, optimisticLockArg);
                        pw.printf ("    {%n");
                        pw.printf ("%s%n", EntityUtil.getStringUpdateClauses (entityName, pkColumns, nonPkColumns, true,
                            optimisticLockColumn, "sql"));
                        pw.printf ("%n");
                        pw.printf ("        return update (sql.getSetElements (), sql.getWhereElements ()) == 1;%n");
                        pw.printf ("    }%n");
                    }
                }
            }
            if (canDelete)
            {
                pw.printf ("%n");
                pw.printf ("    /**%n");
                pw.printf ("     * Deletes multiple rows from the %s table.%n", entityPhysicalName);
                pw.printf ("     * @param es entities corresponding to the rows to be delete%n");
                pw.printf ("     * %n");
                pw.printf ("     * @return the numbers of rows affected by the update%n");
                pw.printf ("     */%n");
                pw.printf ("    @Override%n");
                pw.printf ("    public void delete (final List<%s> es)%n", entityName);
                pw.printf ("    {%n");
                pw.printf (
                    "        DaoUtil.performDeleteBatch (m_dataSource, es.toArray (new %s[es.size ()]), SQL_DELETE);%n",
                    entityName);
                pw.printf ("    }%n");
                pw.printf ("%n");
                pw.printf ("    /**%n");
                pw.printf ("     * Deletes rows from the %s table according to the criteria in whereElements.%n",
                    entityPhysicalName);
                pw.printf ("     * %n");
                pw.printf ("     * @param whereElements%n");
                pw.printf (
                    "     *     Specified criteria to be added into the where clause. These result in SQL 'and' clauses. There is%n");
                pw.printf (
                    "     *     currently no way to directly express 'or' clauses, or more complex expressions. Use a custom query or%n");
                pw.printf ("     *     stored procedure instead.%n");
                pw.printf ("     * %n");
                pw.printf ("     * @return number of rows affected by the delete%n");
                pw.printf ("     */%n");
                pw.printf ("    @Override%n");
                pw.printf ("    public int delete (final WhereElement[] whereElements)%n");
                pw.printf ("    {%n");
                pw.printf (
                    "        return DaoUtil.performDelete (m_dataSource, SQL_FRAGMENT_DELETE, COLUMN_NAMES, whereElements);%n");
                pw.printf ("    }%n");
                if (pk != null)
                {
                    final boolean existsIntrinsic = EntityUtil.allowsOverloadDisambiguation (pkColumns);
                    final boolean generateSeparateOverload = existsIntrinsic || pkColumns.size () > 1;

                    pw.printf ("%n");
                    pw.printf ("    /**%n");
                    pw.printf ("     * Deletes the %s table row corresponding to the specified entity.%n",
                        entityPhysicalName);
                    pw.printf ("     * %n");
                    pw.printf ("     * @param id Type of the primary key.%n");
                    pw.printf ("     * @return whether or not a row was affected by the delete%n");
                    pw.printf ("     */%n");
                    pw.printf ("    @Override%n");
                    pw.printf ("    public boolean delete (final %s e)%n", entityName);
                    pw.printf ("    {%n");
                    pw.printf ("        return DaoUtil.performDelete (m_dataSource, e, SQL_DELETE) == 1;%n");
                    pw.printf ("    }%n");
                    pw.printf ("%n");
                    pw.printf ("    /**%n");
                    pw.printf ("     * Deletes the %s table row corresponding to the specified primary key.%n",
                        entityPhysicalName);
                    pw.printf ("     * %n");
                    pw.printf ("     * @return whether or not a row was affected by the delete%n");
                    pw.printf ("     */%n");
                    if (!generateSeparateOverload)
                    {
                        pw.printf ("    @Override%n");
                    }
                    pw.printf ("    public boolean deleteByPrimaryKey (%s)%n",
                        EntityUtil.getStringColumnsArgsWithType (pkColumns));
                    pw.printf ("    {%n");
                    pw.printf ("        final WhereElement[] whereElements = { %s };%n",
                        EntityUtil.getStringColumnClauses (entityName, pkColumns, false, "WhereElement"));
                    pw.printf ("        return delete (whereElements) == 1;%n");
                    pw.printf ("    }%n");

                    // Don't generate this method if javatype==javaobjecttype since it leads to
                    // an overload problem. The native deleteByPrimaryKey () satisfies the IEntityDao
                    // method signature already.
                    if (generateSeparateOverload)
                    {
                        pw.printf ("%n");
                        pw.printf ("    /**%n");
                        pw.printf (
                            "     * IEntityDao implementation, deletes the %s table row corresponding to the specified primary key.%n",
                            entityPhysicalName);
                        pw.printf ("     * %n");
                        pw.printf ("     * @return whether or not a row was affected by the delete%n");
                        pw.printf ("     */%n");
                        pw.printf ("    @Override%n");
                        pw.printf ("    public boolean deleteByPrimaryKey (final %s id)%n", pkClassName);
                        pw.printf ("    {%n");
                        if (pkColumns.size () > 1)
                        {
                            // Tuple handling
                            pw.printf ("        return deleteByPrimaryKey (");

                            for (int i = 0; i < pkColumns.size (); ++i)
                            {
                                pw.printf ("id.getE%s ()%s", i, i == pkColumns.size () - 1 ? "" : ", ");
                            }
                            pw.printf (");%n");
                        }
                        else
                        {
                            final ColumnTypeInfo jti = EntityUtil.getColumnTypeInfo (pkColumns.get (0));

                            // Only 1 arg: must be casting an intrinsic.
                            pw.printf ("        return deleteByPrimaryKey ((%s) id);%n", jti.m_javaType);

                        }
                        pw.printf ("    }%n");
                    }
                }
            }
            pw.printf ("%n");
            pw.printf ("    /**%n");
            pw.printf ("     * Factory method for the %s entity.%n", entityName);
            pw.printf ("     * %n");
            pw.printf ("     * @return instance of the %s entity.%n", entityName);
            pw.printf ("     */%n");
            pw.printf ("    @Override%n");
            pw.printf ("    public %s newEntityInstance ()%n", entityName);
            pw.printf ("    {%n");
            pw.printf ("        return new %s ();%n", entityName);
            pw.printf ("    }%n");
            if (needUpdate || m_shouldUseSimpleJdbcInsert && canCreate)
            {
                pw.printf ("%n");
                pw.printf ("    /** The physical name of the database entity */%n", entityName);
                pw.printf ("    private static final String ENTITY_NAME = \"%s\";%n", entityPhysicalName);
            }
            pw.printf ("%n");
            pw.printf ("    /** Physical column names corresponding to the %s.Columns values */%n", entityName);
            pw.printf ("    public static final String[] COLUMN_NAMES = { ");
            if (true)
            {
                int i = 0;
                for (final Column c : columns)
                {
                    final String columnPhysicalName = c.getPhysicalName ();
                    pw.printf ("\"%s\"%s", columnPhysicalName, i == columns.size () - 1 ? "" : ", ");
                    ++i;
                }
            }
            pw.printf (" };%n");
            if (!mapsToAnotherEntity && parameters == null) // don't generate row mapper for custom views (which have parameters != null)
            {
                pw.printf ("%n");
                pw.printf ("    /**%n");
                pw.printf ("     * Function to map %s entities from %s result sets.%n", entityName, entityPhysicalName);
                pw.printf ("     */%n");
                pw.printf ("    public static final RowMapper<%s> ROW_MAPPER = (rs, rowNum) ->%n", entityName);
                pw.printf ("    {%n");
                pw.printf ("        final %s e = new %s ();%n", entityName, entityName);
                pw.printf ("%n");
                for (final Column c : columns)
                {
                    final String columnName = c.getName ();
                    final ColumnTypeInfo jti = EntityUtil.getColumnTypeInfo (c);

                    final Function<E3<String, String, String>, String> formatter =
                        jti.m_jdbcResultSetAccessorFormatter != null ? jti.m_jdbcResultSetAccessorFormatter
                            : m_accessorDefaultFormatter;// fall back to simple rs.getXxxx ()

                    final String accessorCode =
                        formatter.apply (E3.of (jti.m_jdbcResultSetAccessor, entityName, columnName));

                    if (c.isNullable ())
                    {
                        // Nullable.
                        pw.printf ("        %s value%s = %s;%n", jti.m_javaType, columnName, accessorCode);
                        pw.printf ("        if (!rs.wasNull ())%n");
                        pw.printf ("        {%n");
                        pw.printf ("            e.set%s (value%s);%n", columnName, columnName);
                        pw.printf ("        }%n");
                    }
                    else
                    {
                        pw.printf ("        e.set%s (%s);%n", columnName, accessorCode);
                    }
                }
                pw.printf ("%n");
                pw.printf ("        return e;%n");
                pw.printf ("    };%n");
            }
            if (canRead)
            {
                final String sqlLines = prepareSqlOutputLines (sqlString, shouldPreserveNewLinesInSQL);
                pw.printf ("    private static final String SQL_FRAGMENT_SELECT_FROM =%n%s;%n", sqlLines);
            }
            if (canCreate)
            {
                if (!m_shouldUseSimpleJdbcInsert)
                {
                    pw.printf ("    private static final String SQL_INSERT = \"insert into %s (%s) values (",
                        entityPhysicalName, EntityUtil.getStringColumnPhysicalNames (nonIdentityColumns));
                    for (int i = 0; i < nonIdentityColumns.size (); ++i)
                    {
                        pw.printf ("?%s", i == nonIdentityColumns.size () - 1 ? "" : ", ");
                    }
                    pw.printf (")\";%n");
                }
            }
            if (needUpdate)
            {
                pw.printf ("    private static final String SQL_UPDATE_ALL_NON_PK = \"update %s set ",
                    entityPhysicalName);

                final List<Column> updateableColumns = EntityUtil.getNonPrimaryKeyNonIdentityColumns (ir);
                if (true)
                {
                    int i = 0;
                    for (final Column c : updateableColumns)
                    {
                        pw.printf ("%s = ?%s", c.getPhysicalName (), i == updateableColumns.size () - 1 ? "" : ", ");
                        ++i;
                    }
                }
                generateWhereClauseCode (pw, pkColumns);
                pw.printf ("\";%n");
            }
            if (canDelete)
            {
                pw.printf ("    private static final String SQL_DELETE = \"delete from %s", entityPhysicalName);
                generateWhereClauseCode (pw, pkColumns);
                pw.printf ("\";%n");
                pw.printf ("    private static final String SQL_FRAGMENT_DELETE = \"delete from %s\";%n",
                    entityPhysicalName);
            }
            pw.printf ("}%n");
        }

        return result;
    }

    private List<String> generateSqlExecutionDao (final Command ir, final String sqlString,
        final boolean shouldPreserveNewLinesInSQL, final List<Parameter> parameters, final String suffix)
    {
        final List<String> result = GenericFactory.newArrayList ();

        final String daoName = ir.getName ();
        final String filepath = getDaoFilepath (String.format ("%s%sDao.java", daoName, suffix));
        final String outputPackage = m_options.m_outputPackage;

        try (final SmartFile pw = new SmartFileShowingProgress (filepath))
        {
            pw.printf ("package %s.dao;%n", outputPackage);
            pw.printf ("%n");
            pw.printf ("import java.sql.SQLException;%n");
            pw.printf ("import javax.sql.DataSource;%n");
            pw.printf ("import java.util.List;%n");
            pw.printf ("import au.com.breakpoint.hedron.core.Tuple;%n");
            pw.printf ("import au.com.breakpoint.hedron.core.dao.DaoUtil;%n");
            pw.printf ("import au.com.breakpoint.hedron.core.dao.BaseExecutableDao;%n");
            pw.printf ("%n");
            pw.printf ("/**%n");
            pw.printf (" * Low-level DAO object encapsulating the %s SQL action%n", daoName);
            pw.printf (" */%n");
            pw.printf ("@SuppressWarnings (\"unused\")%n");
            final String parametersClassName = EntityUtil.getParametersClass (parameters);
            pw.printf ("public class %s%sDao extends BaseExecutableDao<%s>%n", daoName, suffix, parametersClassName);
            pw.printf ("{%n");
            pw.printf ("    public %s%sDao (final DataSource dataSource)%n", daoName, suffix);
            pw.printf ("    {%n");
            pw.printf ("        super (dataSource);%n");
            pw.printf ("    }%n");
            if (EntityUtil.hasConvenienceTypes (parameters))
            {
                pw.printf ("%n");
                pw.printf ("    /**%n");
                pw.printf (
                    "     * Convenience overload using intrinsic values. You must make sure the parameter values%n");
                pw.printf ("     * will fit into the intrinsic parameters.%n");
                pw.printf ("     */%n");
                pw.printf ("    public int execute (%s)%n", EntityUtil.getStringConvenienceParametersArgs (parameters));
                pw.printf ("    {%n");
                pw.printf ("        return execute (%s);%n", EntityUtil.getStringConvenienceParameters (parameters));
                pw.printf ("    }%n");
            }
            pw.printf ("%n");
            pw.printf ("    /** Executes the SQL action */%n");
            pw.printf ("    public int execute (%s)%n", EntityUtil.getStringParametersArgs (parameters));
            pw.printf ("    {%n");
            pw.printf ("        return DaoUtil.performExecuteSql (m_dataSource, SQL_STRING, %s);%n",
                EntityUtil.getStringParametersArgsRef (parameters));
            pw.printf ("    }%n");
            pw.printf ("%n");
            pw.printf ("    /** IExecutableDao implementation */%n");
            pw.printf ("    @Override%n");
            pw.printf ("    public void performExecute (final %s p)%n", parametersClassName);
            pw.printf ("    {%n");
            pw.printf ("        execute (%s);%n", EntityUtil.getStringParameterClassParameters (parameters, "p"));
            pw.printf ("    }%n");
            pw.printf ("%n");
            final String sqlLines = prepareSqlOutputLines (sqlString, shouldPreserveNewLinesInSQL);
            pw.printf ("    private static final String SQL_STRING =%n%s;%n", sqlLines);
            pw.printf ("}%n");
        }

        return result;
    }

    private void generateWhereClauseCode (final SmartFile pw, final List<Column> pkColumns)
    {
        pw.printf (" where ");
        if (true)
        {
            int i = 0;
            for (final Column c : pkColumns)
            {
                pw.printf ("%s = ?%s", c.getPhysicalName (), i == pkColumns.size () - 1 ? "" : " and ");
                ++i;
            }
        }
    }

    private String getDaoFilepath (final String filename)
    {
        return EntityUtil.getFilepath (m_daoDirectory, filename);
    }

    private String getEntityFilepath (final String filename)
    {
        return EntityUtil.getFilepath (m_entityDirectory, filename);
    }

    private String prepareSqlOutputLines (final String sqlString, final boolean shouldPreserveNewLinesInSQL)
    {
        final int paddingSpaces = 8;

        String s = "";

        final List<String> lines = HcUtil.splitStringIntoLines (sqlString);
        HcUtil.removeLeadingAndTrailingBlankLines (lines);

        if (lines.size () > 0)
        {
            // Find the number of leading spaces on the first line.
            final int spacesToRemove = HcUtil.countLeadingSpaces (lines.get (0));

            final List<String> processedLines = GenericFactory.newArrayList ();

            for (final String line : lines)
            {
                // Remove the base number of leading spaces (the number taken from the first
                // line). Trim trailing spaces too. Note: there may be some leading spaces
                // that were in excess of the 'spacesToRemove' count.
                String lineContent = spacesToRemove == 0 ? line : HcUtil.removeLeadingSpaces (line, spacesToRemove);
                lineContent = HcUtil.trimRight (lineContent);
                processedLines.add (lineContent);
            }
            //foreach (string l in processedLines) System.Diagnostics.Debug.WriteLine (l);

            s = renderMultilineString (processedLines, paddingSpaces, shouldPreserveNewLinesInSQL);
        }

        return s;
    }

    public static String renderMultilineString (final List<String> processedLines, final int paddingSpaces,
        final boolean preserveNewLinesInSQL)
    {
        final StringBuilder sb = new StringBuilder ();

        int lineNumber = 0;
        for (final String l : processedLines)
        {
            final String line = HcUtil.trimRight (l);

            final boolean isFirstLine = lineNumber == 0;
            final boolean isLastLine = lineNumber == processedLines.size () - 1;

            if (!isFirstLine)
            {
                sb.append ("\n");
            }

            if (HcUtil.trimLeft (l).length () > 0)
            {
                // Pad the the required number of spaces.
                appendSpaces (sb, paddingSpaces);

                // Generating SQL for embedding in a java/c# custom view/command etc.
                if (preserveNewLinesInSQL)
                {
                    sb.append ('\"');
                    sb.append (line);
                    sb.append (!isLastLine ? " \\n\" +" : "\"");
                }
                else
                {
                    // Suppress redundant leading spaces from appearing inside the
                    // string. eg instead of generating:
                    //      "    0 as TOTAL_IGNORED_SEGMENTS, " +
                    // generate this:
                    //          "0 as TOTAL_IGNORED_SEGMENTS, " +
                    final int leadingSpaces = HcUtil.countLeadingSpaces (line);

                    appendSpaces (sb, leadingSpaces);
                    sb.append ('\"');
                    sb.append (line.substring (leadingSpaces));
                    sb.append (!isLastLine ? " \" +" : "\"");
                }
            }

            ++lineNumber;
        }

        return sb.toString ();
    }

    public static void setShouldUseSimpleJdbcCall (final boolean shouldUseSimpleJdbcCall)
    {
        m_shouldUseSimpleJdbcCall = shouldUseSimpleJdbcCall;
    }

    public static void setShouldUseSimpleJdbcInsert (final boolean shouldUseSimpleJdbcInsert)
    {
        m_shouldUseSimpleJdbcInsert = shouldUseSimpleJdbcInsert;
    }

    public static boolean shouldUseSimpleJdbcCall ()
    {
        return m_shouldUseSimpleJdbcCall;
    }

    public static boolean shouldUseSimpleJdbcInsert ()
    {
        return m_shouldUseSimpleJdbcInsert;
    }

    private static void appendSpaces (final StringBuilder sb, final int paddingSpaces)
    {
        for (int i = 0; i < paddingSpaces; ++i)
        {
            sb.append (' ');
        }
    }

    private static String stringToSymbol (final String symbolString)
    {
        final StringBuilder sb = new StringBuilder ();

        boolean upperCaseNextOne = true;

        for (int i = 0; i < symbolString.length (); ++i)
        {
            char c = symbolString.charAt (i);

            // Ignore non alphanumeric.
            if (!Character.isLetterOrDigit (c))
            {
                upperCaseNextOne = true;// require case break
            }
            else
            {
                if (upperCaseNextOne)
                {
                    c = Character.toUpperCase (c);
                    upperCaseNextOne = false;
                }

                sb.append (c);
            }
        }

        return sb.toString ();
    }

    final Function<E3<String, String, String>, String> m_accessorDefaultFormatter =
        e3 -> String.format ("rs.%s (COLUMN_NAMES[%s.Columns.%s])", e3.getE0 (), e3.getE1 (), e3.getE2 ());

    private final List<Attribute> m_attributes = Collections.synchronizedList (new ArrayList<Attribute> ());// accumulated during output of entities

    private String m_daoDirectory;

    private String m_entityDirectory;

    private final List<DbEnum> m_enums = Collections.synchronizedList (new ArrayList<DbEnum> ());// accumulated during output of entities

    private Options m_options;

    private static final String DIRECTORY_DAO = "dao";

    private static final String DIRECTORY_ENTITY = "entity";

    private static boolean m_shouldUseSimpleJdbcCall;// false because doesn't work with Sybase

    private static boolean m_shouldUseSimpleJdbcInsert;
}
