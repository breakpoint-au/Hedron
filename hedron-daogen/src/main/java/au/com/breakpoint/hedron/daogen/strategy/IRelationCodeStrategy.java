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

import java.util.List;
import au.com.breakpoint.hedron.daogen.Capability;
import au.com.breakpoint.hedron.daogen.Command;
import au.com.breakpoint.hedron.daogen.CustomView;
import au.com.breakpoint.hedron.daogen.DbEnum;
import au.com.breakpoint.hedron.daogen.IRelation;
import au.com.breakpoint.hedron.daogen.Options;
import au.com.breakpoint.hedron.daogen.Schema;
import au.com.breakpoint.hedron.daogen.StoredProcedure;

public interface IRelationCodeStrategy
{
    List<String> generateDao (final Command c, final Schema schema);

    List<String> generateDao (final CustomView cv, final Schema schema);

    List<String> generateDao (final DbEnum en, final Schema schema);

    List<String> generateDao (final IRelation ir, final Schema schema, final List<Capability> capabilities);

    List<String> generateDao (final StoredProcedure sp, final Schema schema);

    List<String> generateEntity (final IRelation ir, final Schema schema);

    List<String> postGenerate ();

    List<String> preGenerate (final Options options);
}
