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

import java.util.List;
import au.com.breakpoint.hedron.core.GenericFactory;
import au.com.breakpoint.hedron.core.Tuple.E2;
import au.com.breakpoint.hedron.dbanalyse.strategy.AllPassSchemaObjectFilterStrategy;
import au.com.breakpoint.hedron.dbanalyse.strategy.INamingStrategy;
import au.com.breakpoint.hedron.dbanalyse.strategy.ISchemaObjectFilterStrategy;

public class Options
{
    public String m_connectionDriver;

    public final List<E2<String, String>> m_connectionProperties = GenericFactory.newArrayList ();

    // Database-specific behaviour.
    public DatabaseType m_databaseType;

    public String m_databaseVersion;

    public boolean m_ignoreReturnParameterOnProcedure;  // hack around Sybase driver weirdness

    public String m_jdbcUrl;

    public INamingStrategy m_logicalNameStrategy; // strategy pattern

    public String m_optionsFilename;

    public String m_outputFilename;

    public String m_password;

    public String m_schemaName;

    public ISchemaObjectFilterStrategy m_schemaObjectFilterStrategy = new AllPassSchemaObjectFilterStrategy (); // strategy pattern

    public String m_username;

}
