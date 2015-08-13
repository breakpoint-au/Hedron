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

import java.io.File;
import java.util.List;
import au.com.breakpoint.hedron.core.GenericFactory;
import au.com.breakpoint.hedron.core.SmartFile;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.daogen.Attribute;
import au.com.breakpoint.hedron.daogen.Capability;
import au.com.breakpoint.hedron.daogen.Column;
import au.com.breakpoint.hedron.daogen.Command;
import au.com.breakpoint.hedron.daogen.CustomView;
import au.com.breakpoint.hedron.daogen.DbEnum;
import au.com.breakpoint.hedron.daogen.EntityUtil;
import au.com.breakpoint.hedron.daogen.IRelation;
import au.com.breakpoint.hedron.daogen.Options;
import au.com.breakpoint.hedron.daogen.Schema;
import au.com.breakpoint.hedron.daogen.SmartFileShowingProgress;
import au.com.breakpoint.hedron.daogen.StoredProcedure;

public class GwtEntityCodeStrategy implements IRelationCodeStrategy
{
    @Override
    public List<String> generateDao (final Command o, final Schema schema)
    {
        // Only entities are generated.
        return GenericFactory.newArrayList ();
    }

    @Override
    public List<String> generateDao (final CustomView o, final Schema schema)
    {
        // Only entities are generated.
        return GenericFactory.newArrayList ();
    }

    @Override
    public List<String> generateDao (final DbEnum en, final Schema schema)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> generateDao (final IRelation ir, final Schema schema, final List<Capability> capabilities)
    {
        // Only entities are generated.
        return GenericFactory.newArrayList ();
    }

    @Override
    public List<String> generateDao (final StoredProcedure sp, final Schema schema)
    {
        // Only entities are generated.
        return GenericFactory.newArrayList ();
    }

    @Override
    public List<String> generateEntity (final IRelation ir, final Schema schema)
    {
        String result = null;

        m_relations.add (ir);

        final String entityName = CLASSNAME_PREFIX + ir.getEntityName ();
        final String filepath = getEntityFilepath (String.format ("%s%s.java", CLASSNAME_PREFIX, entityName));
        final String outputPackage = m_options.m_outputPackage;
        result = filepath;

        try (final SmartFile pw = new SmartFileShowingProgress (filepath))
        {
            final List<Column> columns = ir.getColumns ();
            pw.printf ("package %s.%s;%n", outputPackage, DIRECTORY_ENTITY);
            pw.printf ("%n");
            pw.printf ("import com.google.gwt.user.client.rpc.IsSerializable;%n");
            pw.printf ("%n");
            pw.printf ("/**%n");
            pw.printf (
                " * Low-level entity object encapsulating a %s row, restricted to GWT-javascript capabilities.%n",
                ir.getPhysicalName ());
            pw.printf (" *%n");
            pw.printf (" * @author Breakpoint's DaoGen tool%n");
            pw.printf (" */%n");
            pw.printf ("public class %s implements IsSerializable%n", entityName);
            pw.printf ("{%n");

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
                pw.printf ("        m_column%s = column%s;%n", columnName, columnName);
                pw.printf ("    }%n");
                pw.printf ("%n");
            }
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
            pw.printf ("}%n");
        }

        return GenericFactory.newArrayList (result);
    }

    public void generateMappings ()
    {
        final String filepath = EntityUtil.getFilepath (m_options.m_outputBaseFilepath, "GwtMapping.java");
        final String outputPackage = m_options.m_outputPackage;

        try (final SmartFile pw = new SmartFileShowingProgress (filepath))
        {
            pw.printf ("package %s;%n", outputPackage);
            pw.printf ("%n");
            for (final IRelation ir : m_relations)
            {
                final String entityName = ir.getEntityName ();
                pw.printf ("import %s.entity.%s;%n", m_options.m_outputPackage, entityName);
                pw.printf ("import %s.%s.%s%s;%n", m_options.m_outputPackage, DIRECTORY_ENTITY, CLASSNAME_PREFIX,
                    entityName);
            }
            pw.printf ("%n");
            pw.printf ("/**%n");
            pw.printf (" * Methods for mapping between GWT-javascript supported entities and %n");
            pw.printf (" * the richer server-side entities.%n");
            pw.printf (" * %n");
            pw.printf (" * @author Breakpoint's DaoGen tool%n");
            pw.printf (" */%n");
            pw.printf ("public class GwtMapping%n");
            pw.printf ("{%n");
            for (final IRelation ir : m_relations)
            {
                generateRelationMapping (pw, ir);
            }
            pw.printf ("}%n");
        }
    }

    @Override
    public List<String> postGenerate ()
    {
        generateMappings ();
        return GenericFactory.newArrayList ();
    }

    @Override
    public List<String> preGenerate (final Options options)
    {
        m_options = options;

        m_entityDirectory = String.format ("%s/%s", m_options.m_outputBaseFilepath, DIRECTORY_ENTITY);
        if (m_entityDirectory != null)
        {
            final File fileEntityDir = new File (m_entityDirectory);
            if (!fileEntityDir.exists ())
            {
                ThreadContext.assertFault (fileEntityDir.mkdir (), "Could not create directory [%s]",
                    m_entityDirectory);
            }
        }

        return GenericFactory.newArrayList ();
    }

    private void generateRelationMapping (final SmartFile pw, final IRelation ir)
    {
        final String entityName = ir.getEntityName ();
        final List<Column> columns = ir.getColumns ();

        pw.printf ("    public static %s%s[] transform (final %s[] rhs)%n", CLASSNAME_PREFIX, entityName, entityName);
        pw.printf ("    {%n");
        pw.printf ("        final %s%s[] lhs = new %s%s[rhs.length];%n", CLASSNAME_PREFIX, entityName, CLASSNAME_PREFIX,
            entityName);
        pw.printf ("        for (int i = 0; i < lhs.length; ++i)%n");
        pw.printf ("        {%n");
        pw.printf ("            lhs[i] = transform (rhs[i]);%n");
        pw.printf ("        }%n");
        pw.printf ("        return lhs;%n");
        pw.printf ("    }%n");
        pw.printf ("%n");
        pw.printf ("    public static %s%s transform (final %s rhs)%n", CLASSNAME_PREFIX, entityName, entityName);
        pw.printf ("    {%n");
        pw.printf ("        final %s%s lhs = new %s%s ();%n", CLASSNAME_PREFIX, entityName, CLASSNAME_PREFIX,
            entityName);
        pw.printf ("        copyFrom (lhs, rhs);%n");
        pw.printf ("%n");
        pw.printf ("        return lhs;%n");
        pw.printf ("    }%n");
        pw.printf ("%n");
        pw.printf ("    public static void copyFrom (final %s%s lhs, final %s rhs)%n", CLASSNAME_PREFIX, entityName,
            entityName);
        pw.printf ("    {%n");
        for (final Column c : columns)
        {
            final String columnName = c.getName ();
            pw.printf ("        lhs.set%s (rhs.get%s ());%n", columnName, columnName);
        }
        pw.printf ("    }%n");
        pw.printf ("    %n");
        pw.printf ("    public static %s[] transform (final %s%s[] rhs)%n", entityName, CLASSNAME_PREFIX, entityName);
        pw.printf ("    {%n");
        pw.printf ("        final %s[] lhs = new %s[rhs.length];%n", entityName, entityName);
        pw.printf ("        for (int i = 0; i < lhs.length; ++i)%n");
        pw.printf ("        {%n");
        pw.printf ("            lhs[i] = transform (rhs[i]);%n");
        pw.printf ("        }%n");
        pw.printf ("        return lhs;%n");
        pw.printf ("    }%n");
        pw.printf ("%n");
        pw.printf ("    public static %s transform (final %s%s rhs)%n", entityName, CLASSNAME_PREFIX, entityName);
        pw.printf ("    {%n");
        pw.printf ("        final %s lhs = new %s ();%n", entityName, entityName);
        pw.printf ("        copyFrom (lhs, rhs);%n");
        pw.printf ("%n");
        pw.printf ("        return lhs;%n");
        pw.printf ("    }%n");
        pw.printf ("%n");
        pw.printf ("    public static void copyFrom (final %s lhs, final %s%s rhs)%n", entityName, CLASSNAME_PREFIX,
            entityName);
        pw.printf ("    {%n");
        for (final Column c : columns)
        {
            final String columnName = c.getName ();
            pw.printf ("        lhs.set%s (rhs.get%s ());%n", columnName, columnName);
        }
        pw.printf ("    }%n");
        pw.printf ("%n");
    }

    private String getEntityFilepath (final String filename)
    {
        return EntityUtil.getFilepath (m_entityDirectory, filename);
    }

    private final List<Attribute> m_attributes = GenericFactory.newArrayList ();

    private String m_entityDirectory;

    private Options m_options;

    private final List<IRelation> m_relations = GenericFactory.newArrayList ();

    private static final String CLASSNAME_PREFIX = "Gwt";

    private static final String DIRECTORY_ENTITY = "gwtentity";
}
