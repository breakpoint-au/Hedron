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
import java.util.List;
import au.com.breakpoint.hedron.daogen.strategy.IFilterRule;
import au.com.breakpoint.hedron.daogen.strategy.IOverrideStrategy;
import au.com.breakpoint.hedron.daogen.strategy.IRelationCodeStrategy;
import au.com.breakpoint.hedron.daogen.strategy.SpringJdbcTemplateCodeStrategy;

public class Options
{
    public String m_additionalEntityFilename;   // filename of additional entity definitions

    public IRelationCodeStrategy m_codeStrategy = new SpringJdbcTemplateCodeStrategy (); // strategy pattern

    // Database-specific behaviour.
    public DatabaseType m_databaseType;

    public String m_databaseVersion;

    public List<IFilterRule> m_filters = new ArrayList<IFilterRule> ();

    public boolean m_generateBeanStyleDefinitions;

    public String m_optionsFilename;

    public String m_outputBaseFilepath;

    public String m_outputPackage;

    public List<IOverrideStrategy> m_overrides = new ArrayList<IOverrideStrategy> ();

    public String m_schemaFilename; // the main schema definition
}
