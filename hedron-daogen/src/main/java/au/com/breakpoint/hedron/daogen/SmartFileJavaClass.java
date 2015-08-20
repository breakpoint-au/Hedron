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

import java.util.List;
import java.util.Set;
import au.com.breakpoint.hedron.core.GenericFactory;
import au.com.breakpoint.hedron.core.HcUtil;

public class SmartFileJavaClass extends SmartFileShowingProgress
{
    public SmartFileJavaClass (final String filepath)
    {
        super (filepath);
    }

    public void addClassImport (final String format, final Object... args)
    {
        final String className = String.format (format, args);
        m_imports.add (className);
    }

    public void addClassImports (final List<String> imports)
    {
        for (final String s : imports)
        {
            addClassImport (s);
        }

    }

    @Override
    public void close ()
    {
        // Output the gathered imports at this stage.
        setSectionImports ();
        print (HcUtil.NewLine);

        for (final String importedClass : m_imports)
        {
            printf ("import %s;%n", importedClass);
        }

        print (HcUtil.NewLine);

        super.close ();
    }

    public void setSectionClassCode ()
    {
        setSectionNumber (sectionClass);
    }

    public void setSectionPackage ()
    {
        setSectionNumber (sectionPackage);
    }

    private void setSectionImports ()
    {
        setSectionNumber (sectionImports);
    }

    private final Set<String> m_imports = GenericFactory.newTreeSet ();

    private static final int sectionClass = 2;

    private static final int sectionImports = 1;

    private static final int sectionPackage = 0;
}
