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

import java.util.Map;
import au.com.breakpoint.hedron.core.GenericFactory;

/**
 * Use sorted maps for consistent output.
 */
public class SchemaObjects
{
    public final Map<String, Command> m_commands = GenericFactory.newTreeMap ();

    public final Map<String, Table> m_customEntities = GenericFactory.newTreeMap ();

    public final Map<String, CustomView> m_customViews = GenericFactory.newTreeMap ();

    public final Map<String, DbEnum> m_enums = GenericFactory.newTreeMap ();

    public final Map<String, StoredProcedure> m_storedProcedures = GenericFactory.newTreeMap ();

    public final Map<String, Table> m_tables = GenericFactory.newTreeMap ();

    public final Map<String, View> m_views = GenericFactory.newTreeMap ();
}
