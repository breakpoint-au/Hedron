//                       __________________________________
//                ______|      Copyright 2008-2015         |______
//                \     |     Breakpoint Pty Limited       |     /
//                 \    |   http://www.breakpoint.com.au   |    /
//                 /    |__________________________________|    \
//                /_________/                          \_________\
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
// except in compliance with the License. You may obtain a copy of the License at
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
// either express or implied. See the License for the specific language governing
// permissions and limitations under the License.
//
package au.com.breakpoint.hedron.indexer;

import org.kohsuke.args4j.Option;

public class Options
{
    public enum Mode
    {
        Index
    }

    @Option (name = "-c", aliases =
    {
            "--config"
    }, usage = "Specifies the name of an application-specific configuration file ** MANDATORY **", required = true)
    public String m_configFilename;

    @Option (name = "-d", aliases =
    {
            "--debug"
    }, usage = "If true, outputs debug information; default is false")
    public boolean m_debug;

    @Option (name = "-m", aliases =
    {
            "--mode"
    }, usage = "Program mode; default is Archive")
    public Mode m_mode = Mode.Index;
}
