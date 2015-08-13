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

import au.com.breakpoint.hedron.core.JsonUtil;

public class AppConfiguration
{
    private AppConfiguration ()
    {
    }

    public String getDestination ()
    {
        return destination;
    }

    public String getKeywordFilename ()
    {
        return keywordFilename;
    }

    public SourcePath[] getSourcePaths ()
    {
        return sourcePaths;
    }

    public static class SourcePath
    {
        public String[] getExcludeFiles ()
        {
            return excludeFiles;
        }

        public String[] getIncludeFiles ()
        {
            return includeFiles;
        }

        public String getPath ()
        {
            return path;
        }

        private String[] excludeFiles;

        private String[] includeFiles;

        private String path;
    }

    public static AppConfiguration readJsonFile (final String configFilename)
    {
        final AppConfiguration config = JsonUtil.fromJsonFile (configFilename, AppConfiguration.class, null);

        return config;
    }

    private String destination;

    private String keywordFilename;

    private SourcePath[] sourcePaths;
}
