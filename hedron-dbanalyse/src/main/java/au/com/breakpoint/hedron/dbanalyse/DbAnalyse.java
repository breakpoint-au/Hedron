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
package au.com.breakpoint.hedron.dbanalyse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.kohsuke.args4j.Option;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import schemacrawler.schema.BaseColumn;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnDataType;
import schemacrawler.schema.DatabaseInfo;
import schemacrawler.schema.DatabaseObjectReference;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.ForeignKeyColumnReference;
import schemacrawler.schema.FunctionColumnType;
import schemacrawler.schema.ProcedureColumnType;
import schemacrawler.schema.Routine;
import schemacrawler.schema.RoutineColumn;
import schemacrawler.schema.RoutineColumnType;
import schemacrawler.schema.RoutineType;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableType;
import schemacrawler.schemacrawler.DatabaseConnectionOptions;
import schemacrawler.schemacrawler.InclusionRule;
import schemacrawler.schemacrawler.RegularExpressionExclusionRule;
import schemacrawler.schemacrawler.RegularExpressionInclusionRule;
import schemacrawler.schemacrawler.SchemaCrawlerException;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaInfoLevel;
import schemacrawler.utility.SchemaCrawlerUtility;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.Tuple.E2;
import au.com.breakpoint.hedron.core.args4j.HcUtilArgs4j;
import au.com.breakpoint.hedron.core.context.ExecutionScopes;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.dbanalyse.strategy.AllPassSchemaObjectFilterStrategy;
import au.com.breakpoint.hedron.dbanalyse.strategy.MixedCaseLogicalNameStrategy;
import au.com.breakpoint.hedron.dbanalyse.strategy.PhysicalAsLogicalNameStrategy;
import au.com.breakpoint.hedron.dbanalyse.strategy.SybaseStyleLogicalNameStrategy;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

public class DbAnalyse
{
    public void analyseSchema (final String[] args)
    {
        final CommandLine commandLine = new CommandLine ();

        // Check args & prepare usage string (in thrown AssertException).
        HcUtilArgs4j.getProgramOptions (args, commandLine);

        getOptionsFromFile (commandLine.m_optionsFile);

        analyseSchema ();
    }

    private void analyseSchema ()
    {
        try
        {
            final String jdbcUrl = m_options.m_jdbcUrl;
            final String schemaName = m_options.m_schemaName;

            System.out.printf ("Analysing schema [%s] at [%s]...%n", schemaName, jdbcUrl);
            final long msecStart = System.currentTimeMillis ();

            // Create a database connection
            final DataSource dataSource = new DatabaseConnectionOptions (jdbcUrl);
            final Connection connection = dataSource.getConnection (m_options.m_username, m_options.m_password);

            // Create the options
            final SchemaCrawlerOptions options = new SchemaCrawlerOptions ();

            // Set what details are required in the schema - this affects the
            // time taken to crawl the schema
            options.setSchemaInfoLevel (SchemaInfoLevel.standard ());
            options.setSchemaInclusionRule (new RegularExpressionInclusionRule (schemaName));

            final InclusionRule tableRule = new RegularExpressionExclusionRule (".+[\\$/].+");

            final InclusionRule procRule = s ->
            {
                //System.out.println (s);
                final String[] a =
                    new String[]
                    {
                            ".*" + "\\$" + ".*",
                            ".*" + "_RETRIGGER_EXPRESSION" + ".*",
                            ".*" + "BITOR" + ".*",
                            ".*" + "CENTRED" + ".*",
                            ".*" + "COMPLEMENT" + ".*",
                            ".*" + "D2" + ".*",
                            ".*" + "DDMMYYYYHHMMMSS" + ".*",
                            ".*" + "DOW" + ".*",
                            ".*" + "DSM" + ".*",
                            ".*" + "ENABLE" + ".*",
                            ".*" + "F2" + ".*",
                            ".*" + "GET_LAST_CHANGE_NUMBER" + ".*",
                            ".*" + "GROOM" + ".*",
                            ".*" + "IS_EVEN" + ".*",
                            ".*" + "IS_ODD" + ".*",
                            ".*" + "LEFT_PADDED" + ".*",
                            ".*" + "LOGICAL_" + ".*",
                            ".*" + "M2" + ".*",
                            ".*" + "MD5" + ".*",
                            ".*" + "MERGE_OSS_TRIP_LOCATION" + ".*",
                            ".*" + "MODF" + ".*",
                            ".*" + "NEXT_TIME_ABSOLUTE" + ".*",
                            ".*" + "NEXT_TIME_RELATIVE" + ".*",
                            ".*" + "RESERVE_FARM_STATUS" + ".*",
                            ".*" + "RIGHT_PADDED" + ".*",
                            ".*" + "S2" + ".*",
                            ".*" + "SCHEDULE_JOB" + ".*",
                            ".*" + "SNDF" + ".*",
                            ".*" + "SSM" + ".*",
                            ".*" + "XOR" + ".*",
                            ".*" + "XSD_TIMESTAMP" + ".*",
                            ".*" + "//dummy//" + ".*"
                    };
                return !HcUtil.containsWildcard (a, s);
            };
            options.setTableInclusionRule (tableRule);

            options.setRoutineInclusionRule (procRule); // new ExcludeAll ()

            // Get the schema definition
            final Catalog catalog = SchemaCrawlerUtility.getCatalog (connection, options);
            final long msecDuration = System.currentTimeMillis () - msecStart;

            final Collection<Schema> schemas = catalog.getSchemas ();
            ThreadContext.assertFault (schemas.size () == 1, "Expected one schema, got %s [%s]", schemas.size (),
                schemas.size ());

            final Schema[] a = schemas.toArray (new Schema[schemas.size ()]);
            final Schema schema = a[0];

            final DatabaseInfo di = catalog.getDatabaseInfo ();

            System.out.printf ("  catalog[%s] database[%s] version[%s]%n", schema.getFullName (), di.getProductName (),
                di.getProductVersion ());
            System.out.printf ("  %s tables%n", catalog.getTables (schema).size ());
            System.out.printf ("  %s stored procedures%n", catalog.getRoutines (schema).size ());
            System.out.printf ("...analysed in %s seconds%n", msecDuration / 1_000L);

            outputSchemaXml (catalog, schema);
            System.out.printf ("Schema file [%s] created.%n", m_options.m_outputFilename);
        }
        catch (final SchemaCrawlerException | SQLException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }
    }

    private Element createColumnElement (final Document dom, final Column column)
    {
        final Element eColumn = dom.createElement ("column");

        final String columnName = getPhysicalName (column.getName ());
        eColumn.setAttribute ("name", getLogicalName (columnName));
        eColumn.setAttribute ("physicalname", columnName);

        setColumnTypeAttributes (eColumn, column, "table");

        String requirement = null;
        if (column.isPartOfPrimaryKey ())
        {
            requirement = "primarykey";
        }
        else
        {
            requirement = column.isNullable () ? "optional" : "mandatory";
        }
        eColumn.setAttribute ("requirement", requirement);

        return eColumn;
    }

    private Element createParameterElement (final Document dom, final RoutineColumn<? extends Routine> parameter)
    {
        final Element eParameter = dom.createElement ("parameter");

        final String parameterName = getPhysicalName (parameter.getName ());
        eParameter.setAttribute ("name", getLogicalName (parameterName));
        eParameter.setAttribute ("physicalname", parameterName);

        setColumnTypeAttributes (eParameter, parameter, "stored procedure");

        String direction = null;

        final RoutineColumnType columnType = parameter.getColumnType ();
        if (columnType instanceof FunctionColumnType)
        {
            switch ((FunctionColumnType) columnType)
            {
                case unknown: // default to in
                case in:
                {
                    direction = "in";
                    break;
                }

                case inOut:
                {
                    direction = "inout";
                    break;
                }

                case out:
                {
                    direction = "out";
                    break;
                }

                case returnValue:
                {
                    direction = "return";
                    break;
                }

                case result:
                {
                    direction = "result";
                    break;
                }
            }
        }
        else if (columnType instanceof ProcedureColumnType)
        {
            switch ((ProcedureColumnType) columnType)
            {
                case unknown: // default to in
                case in:
                {
                    direction = "in";
                    break;
                }

                case inOut:
                {
                    direction = "inout";
                    break;
                }

                case out:
                {
                    direction = "out";
                    break;
                }

                case returnValue:
                {
                    direction = "return";
                    break;
                }

                case result:
                {
                    direction = "result";
                    break;
                }
            }
        }
        else
        {
            ThreadContext.assertFault (false, "Unknown proc columnType [%s] [%s] [%s]", parameterName, columnType,
                columnType.getClass ());
        }

        if (direction != null)
        {
            eParameter.setAttribute ("direction", direction);
        }

        return eParameter;
    }

    private Element createStoredProcedureElement (final Document dom, final Routine sp)
    {
        final Schema schema = sp.getSchema ();

        final Element eStoredProcedure = dom.createElement ("storedprocedure");

        final boolean isFunction = sp.getRoutineType () == RoutineType.function;
        eStoredProcedure.setAttribute ("type", isFunction ? "function" : "procedure");
        eStoredProcedure.setAttribute ("schema", schema.getName ());
        eStoredProcedure.setAttribute ("catalog", sp.getSchema ().getCatalogName ());
        //System.out.printf ("%s> [%s] [%s]%n", sp.getName (), sp.getDefinition (), sp.getRoutineBodyType ());

        final String storedProcedureName = getPhysicalName (sp.getName ());
        eStoredProcedure.setAttribute ("name", getLogicalName (storedProcedureName));
        eStoredProcedure.setAttribute ("physicalname", storedProcedureName);

        final List<? extends RoutineColumn<? extends Routine>> parameters = sp.getColumns ();
        for (final RoutineColumn<? extends Routine> parameter : parameters)
        {
            if (shouldIncludeParameter (sp, parameter))
            {
                final Element eParameter = createParameterElement (dom, parameter);
                eStoredProcedure.appendChild (eParameter);
            }
        }

        return eStoredProcedure;
    }

    private void createStoredProcedureElements (final Document dom, final Catalog catalog, final Element eRoot)
    {
        final Collection<Routine> storedProcedures = catalog.getRoutines ();
        for (final Routine sp : storedProcedures)
        {
            if (m_options.m_schemaObjectFilterStrategy.passesFilter (sp))
            {
                // For each StoredProcedure object create element and attach it to root.
                eRoot.appendChild (createStoredProcedureElement (dom, sp));
            }
            //else System.out.println (storedProcedure.getSchemaName ());
        }
    }

    private Element createTableElement (final Document dom, final Table table)
    {
        final Element eTable = dom.createElement ("table");
        final String tableName = getPhysicalName (table.getName ());

        eTable.setAttribute ("name", getLogicalName (tableName));
        eTable.setAttribute ("physicalname", tableName);

        final List<Column> columns = table.getColumns ();
        for (final Column column : columns)
        {
            final Element eColumn = createColumnElement (dom, column);
            eTable.appendChild (eColumn);
        }

        final Collection<ForeignKey> foreignKeys = table.getForeignKeys ();
        for (final ForeignKey foreignKey : foreignKeys)
        {
            // Schemacrawler lists all outgoing AND incoming foreign key references. Just grab the outgoing references.
            if (isOutgoingForeignKey (table, foreignKey))
            {
                final Element eForeignKey = createForeignKeyElement (dom, foreignKey);
                eTable.appendChild (eForeignKey);
            }
        }

        return eTable;
    }

    private void createTableElements (final Document dom, final Catalog catalog, final Schema schema,
        final Element eRoot)
    {
        final Collection<Table> tables = catalog.getTables (schema);
        for (final Table table : tables)
        {
            //if (table.getName ().equals ("MessageNote"))
            //{
            //    System.out.println ("Here");
            //}

            if (m_options.m_schemaObjectFilterStrategy.passesFilter (table))
            {
                final TableType tableType = table.getTableType ();
                final String tableNameUpperCase = tableType.getTableType ().toUpperCase ();

                if (tableType.isView ())
                {
                    eRoot.appendChild (createViewElement (dom, table));
                }
                else if (tableNameUpperCase.contains ("TABLE") || tableNameUpperCase.contains ("TEMPORARY"))
                {
                    // For each Table object create element and attach it to root.
                    eRoot.appendChild (createTableElement (dom, table));
                }
            }
            //else System.out.println (table.getSchemaName ());
        }
    }

    private Element createViewElement (final Document dom, final Table view)
    {
        final Element eView = dom.createElement ("view");

        final String viewName = getPhysicalName (view.getName ());
        eView.setAttribute ("name", getLogicalName (viewName));
        eView.setAttribute ("physicalname", viewName);

        final List<Column> columns = view.getColumns ();
        for (final Column column : columns)
        {
            final Element eColumn = createColumnElement (dom, column);
            eView.appendChild (eColumn);
        }

        return eView;
    }

    private void getConnectionProperties (final Node node)
    {
        final NodeList childNodes = node.getChildNodes ();

        for (int i = 0; i < childNodes.getLength (); ++i)
        {
            final Node childNode = childNodes.item (i);

            if (childNode.getNodeType () == Node.ELEMENT_NODE)
            {
                final String nodeName = childNode.getNodeName ();
                if (nodeName.equals ("property"))
                {
                    final NamedNodeMap attributes = childNode.getAttributes ();

                    Node n = attributes.getNamedItem ("name");
                    ThreadContext.assertError (n != null,
                        "DbAnalyse-options/connection-properties/property must contain a 'name' attribute");
                    final String name = n.getNodeValue ();

                    n = attributes.getNamedItem ("value");
                    ThreadContext.assertError (n != null,
                        "DbAnalyse-options/connection-properties/property must contain a 'value' attribute");
                    final String value = n.getNodeValue ();

                    m_options.m_connectionProperties.add (E2.of (name, value));
                }
                else
                {
                    ThreadContext.assertError (false, "[%s] unknown option element [%s]", getClass ().getSimpleName (),
                        nodeName);
                }
            }
        }
    }

    private String getLogicalName (final String physicalName)
    {
        return m_options.m_logicalNameStrategy.physicalToLogical (physicalName);
    }

    private void getOptionsFromFile (final String optionsFilename)
    {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance ();
        DocumentBuilder db = null;
        try
        {
            db = dbf.newDocumentBuilder ();
        }
        catch (final ParserConfigurationException e)
        {
            ThreadContext.assertFault (false, "XML parser error [%s]", e.getMessage ());
        }

        Document doc = null;
        try
        {
            final File file = new File (optionsFilename);
            doc = db.parse (file);
        }
        catch (final SAXException e)
        {
            ThreadContext.assertFault (false, "XML error [%s]", e.getMessage ());
        }
        catch (final IOException e)
        {
            ThreadContext.assertFault (false, "Cannot read file [%s]%n", optionsFilename);
        }
        doc.getDocumentElement ().normalize ();

        final NodeList topLevelNodes = doc.getDocumentElement ().getChildNodes ();

        m_options = new Options ();
        m_options.m_optionsFilename = optionsFilename;

        for (int i = 0; i < topLevelNodes.getLength (); ++i)
        {
            final Node node = topLevelNodes.item (i);

            if (node.getNodeType () == Node.ELEMENT_NODE)
            {
                final String nodeName = node.getNodeName ();
                final String value = getItem (node);

                if (nodeName.equals ("connection-driver"))
                {
                    m_options.m_connectionDriver = value;
                }
                else if (nodeName.equals ("jdbc-url"))
                {
                    m_options.m_jdbcUrl = value;
                }
                else if (nodeName.equals ("schema"))
                {
                    m_options.m_schemaName = value;
                }
                else if (nodeName.equals ("username"))
                {
                    m_options.m_username = value;
                }
                else if (nodeName.equals ("password"))
                {
                    m_options.m_password = value;
                }
                else if (nodeName.equals ("connection-properties"))
                {
                    getConnectionProperties (node);
                }
                else if (nodeName.equals ("filter-type"))
                {
                    final String filter = value;
                    if (filter.equals ("AllPass"))
                    {
                        m_options.m_schemaObjectFilterStrategy = new AllPassSchemaObjectFilterStrategy (); // set filter strategy
                    }
                    else
                    {
                        ThreadContext.assertError (false, "Unknown [%s] [%s]", nodeName, filter);
                    }
                }
                else if (nodeName.equals ("database-type"))
                {
                    try
                    {
                        m_options.m_databaseType = DatabaseType.valueOf (value);
                    }
                    catch (final IllegalArgumentException e)
                    {
                        ThreadContext.assertError (false, "Unsupported database type value [%s]", value);
                    }
                }
                else if (nodeName.equals ("database-version"))
                {
                    m_options.m_databaseVersion = value;
                }
                else if (nodeName.equals ("logical-name-style"))
                {
                    if (value.equals ("MixedCase"))
                    {
                        m_options.m_logicalNameStrategy = new MixedCaseLogicalNameStrategy ();
                    }
                    else if (value.equals ("SybaseStyle"))
                    {
                        m_options.m_logicalNameStrategy = new SybaseStyleLogicalNameStrategy ();
                    }
                    else if (value.equals ("PhysicalAsLogical"))
                    {
                        m_options.m_logicalNameStrategy = new PhysicalAsLogicalNameStrategy ();
                    }
                    else
                    {
                        ThreadContext.assertError (false, "Unknown [%s] [%s]", nodeName, value);
                    }
                }
                else if (nodeName.equals ("output-file"))
                {
                    m_options.m_outputFilename = value;
                }
                else
                {
                    ThreadContext.assertError (false, "[%s] unknown option element [%s]", getClass ().getSimpleName (),
                        nodeName);
                }
            }
        }

        // Set database-specific behaviour.
        switch (m_options.m_databaseType)
        {
            case Oracle:
            {
                if (m_options.m_logicalNameStrategy == null)
                {
                    // No specific naming strategy defined. Default to Oracle-style.
                    m_options.m_logicalNameStrategy = new MixedCaseLogicalNameStrategy ();
                }
                break;
            }

            case Sybase:
            {
                if (m_options.m_logicalNameStrategy == null)
                {
                    // No specific naming strategy defined. Default to Sybase-style.
                    m_options.m_logicalNameStrategy = new SybaseStyleLogicalNameStrategy ();
                }
                m_options.m_ignoreReturnParameterOnProcedure = true;
                break;
            }
        }
    }

    private String getPhysicalName (final String physicalName)
    {
        return m_options.m_logicalNameStrategy.refinePhysicalName (physicalName);
    }

    // <schema name='SOME_SCHEMA'>
    //     <table name='ACCOUNT'>
    //         <column name='ID' type='char' size='4' nullable='false'/>
    //         <column name='NAME' type='varchar' size='20' nullable='false'/>
    //         <column name='SOME_DATE' type='date' nullable='false'/>
    //         <column name='SOME_NUMBER' type='number' precision='12' scale='0' nullable='false'/>
    //         <column name='SOME_DECIMAL' type='number' precision='10' scale='2' nullable='false'/>
    //
    //         <primary-key>
    //             <column name='ID'/>
    //         </primary-key>
    //     </table>
    //
    //     <stored-proc name='DO_SOMETHING'>
    //         <parameter name='ID' type='char' size='4' nullable='false'/>
    //         <parameter name='NAME' type='varchar' size='20' nullable='false'/>
    //     </stored-proc>
    // </schema>
    private void outputSchemaXml (final Catalog catalog, final Schema schema)
    {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance ();
        DocumentBuilder db = null;
        try
        {
            db = dbf.newDocumentBuilder ();
        }
        catch (final ParserConfigurationException e)
        {
            ThreadContext.throwFault (e);
        }
        final Document dom = db.newDocument ();

        final Element eRoot = dom.createElement ("schema");
        eRoot.setAttribute ("name", m_options.m_schemaName);
        dom.appendChild (eRoot);
        eRoot.appendChild (dom.createComment (String.format (
            " This file is generated by Breakpoint tool DbAnalyse from [%s] ", m_options.m_optionsFilename)));

        createTableElements (dom, catalog, schema, eRoot);
        createStoredProcedureElements (dom, catalog, eRoot);

        final OutputFormat format = new OutputFormat (dom);
        format.setIndenting (true);
        format.setIndent (4);
        format.setLineWidth (150);

        XMLSerializer serializer = null;
        try
        {
            serializer = new XMLSerializer (new FileOutputStream (new File (m_options.m_outputFilename)), format);
        }
        catch (final FileNotFoundException e)
        {
            ThreadContext.throwFault (e);
        }

        try
        {
            serializer.serialize (dom);
        }
        catch (final IOException e)
        {
            ThreadContext.throwFault (e);
        }
    }

    private boolean shouldIncludeParameter (final Routine sp, final RoutineColumn<? extends Routine> parameter)
    {
        boolean should = true;

        if (m_options.m_ignoreReturnParameterOnProcedure)
        {
            final boolean isFunction = sp.getRoutineType () == RoutineType.function;

            if (!isFunction)
            {
                final RoutineColumnType columnType = parameter.getColumnType ();
                if (columnType instanceof ProcedureColumnType && (ProcedureColumnType) columnType == ProcedureColumnType.returnValue)
                {
                    should = false;
                }
            }
        }

        return should;
    }

    private class CommandLine
    {
        @Option (name = "-options", usage = "Specifies the name of the database analysis options XML file *REQUIRED*", required = true)
        private String m_optionsFile;
    }

    // Local connection factory using the Options configuration data.
    //    private class OptionDrivenDataSource implements DataSource
    //    {
    //        @Override
    //        @SuppressWarnings ("unchecked")
    //        public Connection getConnection () throws SQLException
    //        {
    //            if (!m_registered)
    //            {
    //                // This is comparable to Orion data-sources.xml.
    //                // java.sql.Driver driver = new oracle.jdbc.driver.OracleDriver ();
    //                Class<Driver> c = null;
    //                try
    //                {
    //                    c = (Class<Driver>) Class.forName (m_options.m_connectionDriver);
    //                }
    //                catch (final ClassNotFoundException e)
    //                {
    //                    ThreadContext.throwFault (e);
    //                }
    //
    //                Driver driver = null;
    //                try
    //                {
    //                    driver = c.newInstance ();
    //                }
    //                catch (final InstantiationException e)
    //                {
    //                    ThreadContext.throwFault (e);
    //                }
    //                catch (final IllegalAccessException e)
    //                {
    //                    ThreadContext.throwFault (e);
    //                }
    //
    //                DriverManager.registerDriver (driver);
    //                m_registered = true;
    //            }
    //
    //            //			return DriverManager.getConnection (m_options.m_jdbcUrl, m_options.m_username, m_options.m_password);
    //
    //            // Create the properties object that holds all database details
    //            final Properties connectionProperties = new Properties ();
    //            connectionProperties.put ("user", m_options.m_username);
    //            connectionProperties.put ("password", m_options.m_password);
    //
    //            for (final E2<?, ?> p : m_options.m_connectionProperties)
    //            {
    //                connectionProperties.put (p.getE0 (), p.getE1 ());
    //            }
    //
    //            return DriverManager.getConnection (m_options.m_jdbcUrl, connectionProperties);
    //        }
    //
    //        // Other methods are unsupported.
    //        @Override
    //        public Connection getConnection (final String arg0, final String arg1) throws SQLException
    //        {
    //            ThreadContext.assertFault (false, "Unimplemented method getConnection (String arg0, String arg1)");
    //            return null;
    //        }
    //
    //        @Override
    //        public int getLoginTimeout () throws SQLException
    //        {
    //            // Return 0, indicating the default system timeout is to be used.
    //            return 0;
    //        }
    //
    //        @Override
    //        public PrintWriter getLogWriter () throws SQLException
    //        {
    //            ThreadContext.assertFault (false, "Unimplemented method getLogWriter ()");
    //            return null;
    //        }
    //
    //        @Override
    //        public Logger getParentLogger () throws SQLFeatureNotSupportedException
    //        {
    //            return null;
    //        }
    //
    //        @Override
    //        public boolean isWrapperFor (final Class<?> arg0) throws SQLException
    //        {
    //            return false;
    //        }
    //
    //        @Override
    //        public void setLoginTimeout (final int arg0) throws SQLException
    //        {
    //            ThreadContext.assertFault (false, "Unimplemented method setLoginTimeout (int arg0)");
    //        }
    //
    //        @Override
    //        public void setLogWriter (final PrintWriter arg0) throws SQLException
    //        {
    //            ThreadContext.assertFault (false, "Unimplemented method setLogWriter (PrintWriter arg0)");
    //        }
    //
    //        @Override
    //        public <T> T unwrap (final Class<T> arg0) throws SQLException
    //        {
    //            return null;
    //        }
    //
    //        private boolean m_registered;
    //    }

    public static void main (final String[] args)
    {
        //Logging.getLoggers ().get (0).setLevels ("fewid");
        ExecutionScopes.executeProgram ( () -> new DbAnalyse ().analyseSchema (args));
    }

    private static Element createForeignKeyColumnElement (final Document dom, final ForeignKeyColumnReference fkcm)
    {
        final Element eForeignKey = dom.createElement ("column");
        final Column referencingColumn = fkcm.getForeignKeyColumn ();
        final Column referencedColumn = fkcm.getPrimaryKeyColumn ();

        eForeignKey.setAttribute ("name", referencingColumn.getName ());
        eForeignKey.setAttribute ("referencedtable", referencedColumn.getParent ().getName ());
        eForeignKey.setAttribute ("referencedcolumn", referencedColumn.getName ());

        return eForeignKey;
    }

    private static Element createForeignKeyElement (final Document dom, final ForeignKey foreignKey)
    {
        // <constraint name="C1" type="compoundforeignkey">
        //     <column name="ActivationDate" referencedtable="ActivationDate" referencedcolumn="Value"/>
        // </constraint>
        final Element eForeignKey = dom.createElement ("constraint");
        eForeignKey.setAttribute ("name", foreignKey.getName ());
        eForeignKey.setAttribute ("type", "compoundforeignkey");

        final List<ForeignKeyColumnReference> fkcms = foreignKey.getColumnReferences ();
        for (final ForeignKeyColumnReference fkcm : fkcms)
        {
            final Element eColumn = createForeignKeyColumnElement (dom, fkcm);
            eForeignKey.appendChild (eColumn);
        }

        return eForeignKey;
    }

    private static String getItem (final Node node)
    {
        return node.getTextContent ().trim ();
    }

    private static boolean isOutgoingForeignKey (final Table table, final ForeignKey foreignKey)
    {
        final List<ForeignKeyColumnReference> fkcms = foreignKey.getColumnReferences ();
        ThreadContext.assertFault (fkcms.size () > 0, "Table [%s] reports a foreign key [%s] with no columns",
            table.getName (), foreignKey.getName ());
        final ForeignKeyColumnReference fkcm = fkcms.get (0);

        final Column referencedColumn = fkcm.getPrimaryKeyColumn ();
        final String referencedtableName = referencedColumn.getParent ().getName ();

        return !referencedtableName.equals (table.getName ());
    }

    private static <P extends DatabaseObjectReference> void setCharacterColumnAttributes (final Element e,
        final BaseColumn<P> bc, final String charStyle)
    {
        e.setAttribute ("type", "char");
        e.setAttribute ("mode", charStyle);
        e.setAttribute ("size", String.valueOf (bc.getSize ()));
    }

    private static <P extends DatabaseObjectReference> void setColumnTypeAttributes (final Element e,
        final BaseColumn<P> bc, final String description)
    {
        final ColumnDataType cdt = bc.getColumnDataType ();

        //		if (bc.getParent ().getName ().equalsIgnoreCase ("MessageNote") && bc.getName ().equals ("Message_Id"))
        //		{
        //		    final Column column = (Column) bc;
        //			System.out.printf ("  Column [%s]%n", column.getName ());
        //
        //			System.out.printf ("    column.getDefaultValue [%s]%n", column.getDefaultValue ());
        //			System.out.printf ("    column.getPrivileges [%s]%n", column.getPrivileges ().toString ());
        //			System.out.printf ("    column.getReferencedColumn [%s]%n", column.getReferencedColumn ());
        //			System.out.printf ("    column.isPartOfForeignKey [%s]%n", column.isPartOfForeignKey ());
        //			System.out.printf ("    column.isPartOfPrimaryKey [%s]%n", column.isPartOfPrimaryKey ());
        //			System.out.printf ("    column.isPartOfUniqueIndex [%s]%n", column.isPartOfUniqueIndex ());
        //			System.out.printf ("    column.getDecimalDigits [%s]%n", column.getDecimalDigits ());
        //			System.out.printf ("    column.getOrdinalPosition [%s]%n", column.getOrdinalPosition ());
        //			System.out.printf ("    column.getSize [%s]%n", column.getSize ());
        //			System.out.printf ("    column.getWidth [%s]%n", column.getWidth ());
        //			System.out.printf ("    column.isNullable [%s]%n", column.isNullable ());
        //
        //			System.out.printf ("    cdt.getBaseType [%s]%n", cdt.getBaseType ());
        //			System.out.printf ("    cdt.getCreateParameters [%s]%n", cdt.getCreateParameters ());
        //			System.out.printf ("    cdt.getDatabaseSpecificTypeName [%s]%n", cdt.getDatabaseSpecificTypeName ());
        //			System.out.printf ("    cdt.getLiteralPrefix [%s]%n", cdt.getLiteralPrefix ());
        //			System.out.printf ("    cdt.getLiteralSuffix [%s]%n", cdt.getLiteralSuffix ());
        //			System.out.printf ("    cdt.getLocalTypeName [%s]%n", cdt.getLocalTypeName ());
        //			System.out.printf ("    cdt.getMaximumScale [%s]%n", cdt.getMaximumScale ());
        //			System.out.printf ("    cdt.getMinimumScale [%s]%n", cdt.getMinimumScale ());
        //			System.out.printf ("    cdt.getNumPrecisionRadix [%s]%n", cdt.getNumPrecisionRadix ());
        //			System.out.printf ("    cdt.getPrecision [%s]%n", cdt.getPrecision ());
        //			System.out.printf ("    cdt.getSearchable [%s]%n", cdt.getSearchable ());
        //			System.out.printf ("    cdt.getType [%s]%n", cdt.getType ());
        //			System.out.printf ("    cdt.getTypeClassName [%s]%n", cdt.getTypeClassName ());
        //			System.out.printf ("    cdt.getTypeName [%s]%n", cdt.getTypeName ());
        //			System.out.printf ("    cdt.isAutoIncrementable [%s]%n", cdt.isAutoIncrementable ());
        //			System.out.printf ("    cdt.isBinaryType [%s]%n", cdt.isBinaryType ());
        //			System.out.printf ("    cdt.isCaseSensitive [%s]%n", cdt.isCaseSensitive ());
        //			System.out.printf ("    cdt.isCharacterType [%s]%n", cdt.isCharacterType ());
        //			System.out.printf ("    cdt.isDateType [%s]%n", cdt.isDateType ());
        //			System.out.printf ("    cdt.isFixedPrecisionScale [%s]%n", cdt.isFixedPrecisionScale ());
        //			System.out.printf ("    cdt.isIntegralType [%s]%n", cdt.isIntegralType ());
        //			System.out.printf ("    cdt.isNullable [%s]%n", cdt.isNullable ());
        //			System.out.printf ("    cdt.isRealType [%s]%n", cdt.isRealType ());
        //			System.out.printf ("    cdt.isUnsigned [%s]%n", cdt.isUnsigned ());
        //			System.out.printf ("    cdt.isUserDefined [%s]%n", cdt.isUserDefined ());
        //		}

        final String typeName = cdt.getLocalTypeName ();
        if (typeName == null)
        {
            ThreadContext.assertInformation (false, "Unknown column type (null) for %s[%s] column[%s]", description, bc
                .getParent ().getName (), bc.getName ());
            e.setAttribute ("type", "(unknown)");
        }
        else if (typeName.equalsIgnoreCase ("char"))
        {
            setCharacterColumnAttributes (e, bc, "fixed");
        }
        else if (typeName.equalsIgnoreCase ("varchar") || typeName.equalsIgnoreCase ("varchar2"))
        {
            setCharacterColumnAttributes (e, bc, "varying");
        }
        else if (typeName.equalsIgnoreCase ("int"))
        {
            e.setAttribute ("type", "integer");
        }
        else if (typeName.equalsIgnoreCase ("number") || typeName.equalsIgnoreCase ("numeric"))
        {
            e.setAttribute ("type", "decimal");
            e.setAttribute ("precision", String.valueOf (bc.getSize ()));
            e.setAttribute ("scale", String.valueOf (bc.getDecimalDigits ()));
        }
        else if (typeName.equalsIgnoreCase ("float") || typeName.equalsIgnoreCase ("double precision"))
        {
            e.setAttribute ("type", "floatingpoint");
        }
        else if (typeName.equalsIgnoreCase ("money"))
        {
            //TODO 7 support sybase money
            e.setAttribute ("type", "floatingpoint");
        }
        else if (typeName.equalsIgnoreCase ("text"))
        {
            e.setAttribute ("type", "text");
        }
        else if (typeName.equalsIgnoreCase ("date") || typeName.equalsIgnoreCase ("datetime") || typeName
            .equalsIgnoreCase ("timestamp"))
        {
            e.setAttribute ("type", "date");
        }
        else if (typeName.equalsIgnoreCase ("long raw") || typeName.equalsIgnoreCase ("varbinary"))
        {
            e.setAttribute ("type", "blob");
        }
        else
        {
            ThreadContext.assertInformation (false, "Unsupported column type [%s] for %s[%s] column[%s]", typeName,
                description, bc.getParent ().getName (), bc.getName ());
            e.setAttribute ("type", "(unsupported)");
        }
    }

    private Options m_options;
}
