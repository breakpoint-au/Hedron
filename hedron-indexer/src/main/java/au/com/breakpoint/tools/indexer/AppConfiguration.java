package au.com.breakpoint.tools.indexer;

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
