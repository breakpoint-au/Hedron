package au.com.breakpoint.hedron.indexer;

import org.kohsuke.args4j.Option;

public class Options
{
    public enum Mode
    {
        Index
    }

    @Option (name = "-config", usage = "Specifies the name of an application-specific configuration file ** MANDATORY **", required = true)
    public String m_configFilename;

    @Option (name = "-debug", usage = "If true, outputs debug information; default is false")
    public boolean m_debug;

    @Option (name = "-mode", usage = "Program mode; default is Archive")
    public Mode m_mode = Mode.Index;
}
