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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.kohsuke.args4j.Option;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import au.com.breakpoint.hedron.core.GenericFactory;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.HcUtilFile;
import au.com.breakpoint.hedron.core.JsonUtil;
import au.com.breakpoint.hedron.core.SmartFile;
import au.com.breakpoint.hedron.core.args4j.HgUtilArgs4j;
import au.com.breakpoint.hedron.core.context.ExecutionScopes;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.core.log.ConsoleLogger;
import au.com.breakpoint.hedron.core.log.Logging;
import au.com.breakpoint.hedron.daogen.strategy.IOverrideStrategy;
import au.com.breakpoint.hedron.daogen.strategy.IRelationCodeStrategy;
import au.com.breakpoint.hedron.daogen.strategy.SpringJdbcTemplateCodeStrategy;
import au.com.breakpoint.hedron.daogen.strategy.StoredProcedureOverrideStrategy;
import au.com.breakpoint.hedron.daogen.strategy.TableOverrideStrategy;

public class DaoGen
{
    public void generateDaos (final String[] args)
    {
        final CommandLine commandLine = new CommandLine ();

        // Check args & prepare usage string (in thrown AssertException).
        HgUtilArgs4j.getProgramOptions (args, commandLine);

        getOptionsFromFile (commandLine.m_optionsFile);

        getSchema ();

        final List<String> feedback = GenericFactory.newArrayList ();

        // Take any actions prior to generation.
        feedback.addAll (m_options.m_codeStrategy.preGenerate (m_options));

        feedback.addAll (generateDaos ());
        feedback.addAll (generateKeywordList ());

        // Take any actions after generation.
        feedback.addAll (m_options.m_codeStrategy.postGenerate ());

        for (final String s : feedback)
        {
            if (HcUtil.safeGetLength (s) > 0)
            {
                System.out.printf ("%n  %s", s);
            }
            else
            {
                System.out.print (".");
            }
        }

        // Output any unused filter rules for info only.
        showUnusedFilterRules ();
    }

    private void addOverrides (final Node parentNode)
    {
        final Options options = m_options;

        final NodeList childNodes = parentNode.getChildNodes ();
        for (int s = 0; s < childNodes.getLength (); ++s)
        {
            final Node node = childNodes.item (s);

            if (node.getNodeType () == Node.ELEMENT_NODE)
            {
                final String nodeName = node.getNodeName ();
                if (nodeName.equals ("stored-procedure"))
                {
                    final IOverrideStrategy fo = new StoredProcedureOverrideStrategy (options, node);
                    options.m_overrides.add (fo);
                }
                else if (nodeName.equals ("table"))
                {
                    final IOverrideStrategy fo = new TableOverrideStrategy (options, node);
                    options.m_overrides.add (fo);
                }
                else
                {
                    ThreadContext.assertError (false, "[%s] unknown option element [%s]", getClass ().getSimpleName (),
                        nodeName);
                }
            }
        }
    }

    private void applyUserOverrides ()
    {
        final SchemaObjects schemaObjects = m_schema.getSchemaObjects ();
        for (final IOverrideStrategy io : m_options.m_overrides)
        {
            io.override (schemaObjects);
        }
    }

    private void createCustomViewEntity (final CustomView cv)
    {
        final SchemaObjects schemaObjects = m_schema.getSchemaObjects ();

        final Table e = new Table (cv);

        final String typeName = cv.getEntityName ();
        schemaObjects.m_customEntities.put (typeName, e);
    }

    private List<String> generateCommandCode (final Command o)
    {
        return m_options.m_codeStrategy.generateDao (o, m_schema);
    }

    private List<String> generateCustomViewCode (final CustomView o)
    {
        return m_options.m_codeStrategy.generateDao (o, m_schema);
    }

    private List<String> generateDaos ()
    {
        final SchemaObjects schemaObjects = m_schema.getSchemaObjects ();

        // Generate concurrently.
        final List<Callable<List<String>>> tasks = GenericFactory.newArrayList ();

        for (final Map.Entry<String, DbEnum> entry : schemaObjects.m_enums.entrySet ())
        {
            final DbEnum en = entry.getValue ();
            if (shouldGenerateDao (en))
            {
                tasks.add ( () -> generateIRelationCode (en));
            }
        }

        for (final Map.Entry<String, Table> entry : schemaObjects.m_tables.entrySet ())
        {
            final IRelation ir = entry.getValue ();
            final List<Capability> capabilities = GenericFactory.newArrayList ();// to be filled in by shouldGenerateDao
            if (shouldGenerateDao (capabilities, ir))
            {
                tasks.add ( () -> generateIRelationCode (ir, capabilities));
            }
        }

        for (final Map.Entry<String, Table> entry : schemaObjects.m_customEntities.entrySet ())
        {
            final IRelation ir = entry.getValue ();

            // Generate entity for this relation.
            final List<Capability> noCapabilities = GenericFactory.newArrayList ();// to be filled in by shouldGenerateDao
            tasks.add ( () -> generateIRelationCode (ir, noCapabilities));
        }

        for (final Map.Entry<String, View> entry : schemaObjects.m_views.entrySet ())
        {
            final IRelation ir = entry.getValue ();
            final List<Capability> capabilities = GenericFactory.newArrayList ();// to be filled in by shouldGenerateDao
            if (shouldGenerateDao (capabilities, ir))
            {
                tasks.add ( () -> generateIRelationCode (ir, capabilities));
            }
        }

        for (final Map.Entry<String, StoredProcedure> entry : schemaObjects.m_storedProcedures.entrySet ())
        {
            final StoredProcedure o = entry.getValue ();
            if (shouldGenerateDao (o))
            {
                tasks.add ( () -> generateStoredProcedureCode (o));
            }
        }

        for (final Map.Entry<String, CustomView> entry : schemaObjects.m_customViews.entrySet ())
        {
            final CustomView o = entry.getValue ();
            if (shouldGenerateDao (o))
            {
                tasks.add ( () -> generateCustomViewCode (o));
            }
        }

        for (final Map.Entry<String, Command> entry : schemaObjects.m_commands.entrySet ())
        {
            final Command o = entry.getValue ();
            if (shouldGenerateDao (o))
            {
                tasks.add ( () -> generateCommandCode (o));
            }
        }

        // Execute all the code generation concurrently.
        final int nrThreads = 20;
        final List<List<String>> results = HcUtil.executeConcurrently (tasks, nrThreads, true);

        //        // Execute and gather output using fork-join.
        //        final Function<Callable<List<String>>, List<String>> work =
        //            new Function<Callable<List<String>>, List<String>> ()
        //            {
        //                @Override
        //                public List<String> getValue (final Callable<List<String>> a)
        //                {
        //                    List<String> v = null;
        //                    try
        //                    {
        //                        v = a.call ();
        //                    }
        //                    catch (final Exception e)
        //                    {
        //                        // Propagate exception as unchecked fault up to the fault barrier.
        //                        ThreadContext.throwFault (e);
        //                    }
        //                    return v;
        //                }
        //            };
        //        final List<List<String>> results = HcUtil.executeConcurrently (tasks, work);

        final List<String> feedback = HcUtil.mergeLists (results);
        return feedback;
    }

    private List<String> generateIRelationCode (final DbEnum en)
    {
        final List<String> results = GenericFactory.newArrayList ();

        results.addAll (m_options.m_codeStrategy.generateDao (en, m_schema));
        return results;
    }

    private List<String> generateIRelationCode (final IRelation ir, final List<Capability> capabilities)
    {
        final List<String> results = GenericFactory.newArrayList ();

        // Entity first. If the relation shares another relation's entity, don't
        // generate an entity here.
        if (!EntityUtil.mapsToAnotherEntity (ir))
        {
            // Generate entity for this relation.
            results.addAll (m_options.m_codeStrategy.generateEntity (ir, m_schema));
        }

        // Then DAO.
        results.addAll (m_options.m_codeStrategy.generateDao (ir, m_schema, capabilities));
        return results;
    }

    private List<String> generateKeywordList ()
    {
        final List<String> feedback = GenericFactory.newArrayList ();
        final String filepath = HcUtil.formFilepath (".", "keywords.generated.json");

        final Map<String, String[]> keywordMap = getSchemaKeywords ();
        final String json = JsonUtil.toJson (keywordMap);

        SmartFile pw = null;
        try
        {
            pw = new SmartFile (filepath);
            pw.print (json);
        }
        finally
        {
            final boolean updated = HcUtilFile.safeClose (pw);
            feedback.add (updated ? filepath : "");
        }

        return feedback;
    }

    private List<String> generateStoredProcedureCode (final StoredProcedure o)
    {
        return m_options.m_codeStrategy.generateDao (o, m_schema);
    }

    private IRelation getIRelation (final String typeName)
    {
        final IRelation ir = m_schema.getIRelationNoThrow (typeName);
        ThreadContext.assertError (ir != null, "Unknown typeName [%s]", typeName);

        return ir;
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
            ThreadContext.assertFault (false, "Cannot read file [%s]:%n%s", optionsFilename, e.getMessage ());
        }
        doc.getDocumentElement ().normalize ();

        final NodeList topLevelNodes = doc.getDocumentElement ().getChildNodes ();

        m_options.m_optionsFilename = optionsFilename;

        for (int s = 0; s < topLevelNodes.getLength (); ++s)
        {
            final Node node = topLevelNodes.item (s);

            if (node.getNodeType () == Node.ELEMENT_NODE)
            {
                final String nodeName = node.getNodeName ();
                final String value = HcUtil.getTrimmedText (node);
                if (nodeName.equals ("output-base-filepath"))
                {
                    m_options.m_outputBaseFilepath = value;
                }
                else if (nodeName.equals ("output-package"))
                {
                    m_options.m_outputPackage = value;
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
                else if (nodeName.equals ("schema-filename"))
                {
                    m_options.m_schemaFilename = value;
                }
                else if (nodeName.equals ("additional-schema-filename"))
                {
                    m_options.m_additionalEntityFilename = value;
                }
                else if (nodeName.equals ("filter"))
                {
                    m_filters.add (new Filter (node));
                }
                else if (nodeName.equals ("bean-style-definitions"))
                {
                    m_options.m_generateBeanStyleDefinitions = Boolean.valueOf (value);
                }
                else if (nodeName.equals ("overrides"))
                {
                    addOverrides (node);
                }
                else if (nodeName.equals ("code-strategy"))
                {
                    final String strategyClassName =
                        SpringJdbcTemplateCodeStrategy.class.getPackage ().getName () + "." + value + "CodeStrategy";
                    final Class<?> c = HcUtil.getClassObject (strategyClassName);
                    m_options.m_codeStrategy = (IRelationCodeStrategy) HcUtil.instantiate (c);
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
                // TODO 3 review -- temporarily turned off
                SpringJdbcTemplateCodeStrategy.setShouldUseSimpleJdbcInsert (false);
                SpringJdbcTemplateCodeStrategy.setShouldUseSimpleJdbcCall (false);
                break;
            }

            case Sybase:
            {
                SpringJdbcTemplateCodeStrategy.setShouldUseSimpleJdbcInsert (false);
                SpringJdbcTemplateCodeStrategy.setShouldUseSimpleJdbcCall (false);
                break;
            }
        }
    }

    private void getSchema ()
    {
        m_schema = parseSchemaDefinitions (m_options.m_schemaFilename);
        if (m_options.m_additionalEntityFilename != null)
        {
            // Copy in any additional entities.
            final Schema additionalSchema = parseSchemaDefinitions (m_options.m_additionalEntityFilename);
            m_schema.addSchemaObjects (additionalSchema);
        }

        // Now apply any user-specified overrides.
        applyUserOverrides ();

        // Ensure any extra entities required by stored procedures/custom views are
        // marked to be generated.
        markEntitiesForGeneration ();
    }

    private Map<String, String[]> getSchemaKeywords ()
    {
        final List<String> enums = GenericFactory.newArrayList ();
        final List<String> tables = GenericFactory.newArrayList ();
        final List<String> views = GenericFactory.newArrayList ();
        final List<String> storedProcedures = GenericFactory.newArrayList ();

        final SchemaObjects schemaObjects = m_schema.getSchemaObjects ();

        // Generate concurrently.
        for (final Map.Entry<String, DbEnum> entry : schemaObjects.m_enums.entrySet ())
        {
            final DbEnum e = entry.getValue ();
            enums.add (HcUtil.caseBreakToUnderScoreBreak (e.getName ()));
        }

        for (final Map.Entry<String, Table> entry : schemaObjects.m_tables.entrySet ())
        {
            final Table e = entry.getValue ();
            tables.add (e.getPhysicalName ());
        }

        for (final Map.Entry<String, View> entry : schemaObjects.m_views.entrySet ())
        {
            final View e = entry.getValue ();
            views.add (e.getPhysicalName ());
        }

        for (final Map.Entry<String, StoredProcedure> entry : schemaObjects.m_storedProcedures.entrySet ())
        {
            final StoredProcedure e = entry.getValue ();
            final String physicalName = e.getPhysicalName ();

            // Remove any package prefix.
            final int index = physicalName.lastIndexOf ('.');
            final String procName = index == -1 ? physicalName : physicalName.substring (index + 1);
            storedProcedures.add (procName);
        }

        final Map<String, String[]> e = GenericFactory.newTreeMap ();
        e.put ("enum", toSortedArray (enums));
        e.put ("table", toSortedArray (tables));
        e.put ("view", toSortedArray (views));
        e.put ("storedProcedure", toSortedArray (storedProcedures));

        return e;
    }

    private void markEntitiesForGeneration ()
    {
        final SchemaObjects schemaObjects = m_schema.getSchemaObjects ();

        // Look for tables that map to another entity.
        for (final Map.Entry<String, Table> entry : schemaObjects.m_tables.entrySet ())
        {
            final IRelation ir = entry.getValue ();
            if (shouldGenerateDao (ir))
            {
                if (EntityUtil.mapsToAnotherEntity (ir))
                {
                    // Shared entity to be generated.
                    m_setAdditionalTypes.add (ir.getEntityName ());
                }
            }
        }

        // Look for custom views that reference an existing entity or create a custom entity.
        for (final Entry<String, CustomView> entry : schemaObjects.m_customViews.entrySet ())
        {
            final CustomView cv = entry.getValue ();

            if (shouldGenerateDao (cv))
            {
                final String customEntityName = cv.getCustomEntity ();
                if (cv.getName ().equals ("OnTimeRunningCriteriaActivationDateStart"))
                {
                    System.out.println ();
                }

                if (customEntityName != null)
                {
                    final IRelation ir = m_schema.getIRelationNoThrow (customEntityName);
                    ThreadContext.assertError (ir == null,
                        "Custom view %s defines custom entity %s. An entity with the name %s already exists",
                        cv.getName (), customEntityName, customEntityName);

                    // Synthesise the entity now.
                    //System.out.println (customEntityName);
                    createCustomViewEntity (cv);

                    m_setAdditionalTypes.add (customEntityName);
                }
                else
                {
                    final String typeName = cv.getEntityName ();

                    final IRelation ir = getIRelation (typeName);
                    if (!shouldGenerateDao (ir))
                    {
                        // The entity wasn't in use. Generate a readonly version of the entity
                        // for use by the stored proc.
                        m_setAdditionalTypes.add (ir.getEntityName ());
                    }
                }
            }
        }

        // Look for stored procedures that reference entities in output result sets.
        for (final Map.Entry<String, StoredProcedure> entry : schemaObjects.m_storedProcedures.entrySet ())
        {
            final StoredProcedure sp = entry.getValue ();
            final List<StoredProcedureResultSet> resultSets = sp.getResultSets ();
            for (final StoredProcedureResultSet sprs : resultSets)
            {
                // Here's a referenced type.
                final String typeName = sprs.getType ();
                final IRelation ir = getIRelation (typeName);

                if (!shouldGenerateDao (ir))
                {
                    // The entity wasn't in use. Generate a readonly version of the entity
                    // for use by the stored proc.
                    m_setAdditionalTypes.add (typeName);
                }
            }
        }
    }

    private Schema parseSchemaDefinitions (final String filename)
    {
        final Element documentElement = parseXml (filename);
        final String documentElementNodeName = documentElement.getNodeName ();
        ThreadContext.assertError (documentElementNodeName.equals ("schema"),
            "Document element must be 'schema', not '%s'", documentElementNodeName);

        return new Schema (documentElement);
    }

    private Element parseXml (final String filename)
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
            final File file = new File (filename);
            doc = db.parse (file);
        }
        catch (final SAXException e)
        {
            ThreadContext.assertFault (false, "XML error [%s]", e.getMessage ());
        }
        catch (final IOException e)
        {
            ThreadContext.assertFault (false, "Cannot read file [%s]%n", filename);
        }

        final Element documentElement = doc.getDocumentElement ();
        documentElement.normalize ();

        return documentElement;
    }

    private boolean shouldGenerateDao (final Command o)
    {
        return shouldGenerateDao ("command", o.getName ());
    }

    private boolean shouldGenerateDao (final CustomView o)
    {
        //System.out.printf ("cv [%s] %s%n", o.getName (), shouldGenerateDao);
        return shouldGenerateDao ("customview", o.getName ());
    }

    private boolean shouldGenerateDao (@SuppressWarnings ("unused") final DbEnum en)
    {
        // TODO 0 filterable by user input file?
        return true;
    }

    private boolean shouldGenerateDao (final IRelation ir)
    {
        final List<Capability> capabilities = GenericFactory.newArrayList ();// to be filled in by shouldGenerateDao; ignored here
        return shouldGenerateDao (capabilities, ir);
    }

    private boolean shouldGenerateDao (final List<Capability> capabilities, final IRelation ir)
    {
        final BooleanHolder okHolder = new BooleanHolder ();
        final String name = ir.getName ();
        if (name.equals ("OptionalActivationDate"))
        {
            System.out.println ();
        }

        for (final Filter f : m_filters)
        {
            f.evaluateFilter (ir.getRelationType (), name, okHolder, capabilities);
        }

        boolean ok = okHolder.getValue ();
        if (!ok)
        {
            // No dao generation specified by filter rules. See if one is required
            // by a stored proc.
            ok = m_setAdditionalTypes.contains (name);
        }
        return ok;
    }

    private boolean shouldGenerateDao (final StoredProcedure o)
    {
        return shouldGenerateDao ("storedprocedure", o.getName ());
    }

    private boolean shouldGenerateDao (final String type, final String name)
    {
        final BooleanHolder ok = new BooleanHolder ();

        final List<Capability> capabilities = GenericFactory.newArrayList ();// to be filled in by shouldGenerateDao

        for (final Filter f : m_filters)
        {
            f.evaluateFilter (type, name, ok, capabilities);
        }

        //      System.out.printf ("DaoGen: sproc %s pass: %s%n", sp.getName (), ok);
        return ok.getValue ();
    }

    private void showUnusedFilterRules ()
    {
        final List<String> unusedRules = GenericFactory.newArrayList ();

        for (final Filter f : m_filters)
        {
            unusedRules.addAll (f.getUnusedFilterRules ());
        }

        if (unusedRules.size () > 0)
        {
            System.out.printf ("%nWarning - unused filter rules:%n");
            for (final String s : unusedRules)
            {
                System.out.printf ("  %s%n", s);
            }
        }
    }

    private String[] toSortedArray (final List<String> l)
    {
        Collections.sort (l);

        return l.toArray (new String[l.size ()]);
    }

    private class CommandLine
    {
        @Option (name = "-options", usage = "Specifies the name of the DAO generation options XML file *REQUIRED*", required = true)
        private String m_optionsFile;
    }

    public static void main (final String[] args)
    {
        Logging.addLogger (new ConsoleLogger ("fewi"));
        ExecutionScopes.executeProgram ( () -> new DaoGen ().generateDaos (args));
    }

    private final List<Filter> m_filters = GenericFactory.newArrayList ();

    private final Options m_options = new Options ();

    private Schema m_schema;

    private final Set<String> m_setAdditionalTypes = GenericFactory.newHashSet ();

    //    private static final List<Capability> m_readonlyCapabilities = GenericFactory.newArrayList (Capability.READ);
}
