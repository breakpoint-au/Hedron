package au.com.breakpoint.hedron.indexer;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import au.com.breakpoint.hedron.core.args4j.HcUtilArgs4j;
import au.com.breakpoint.hedron.indexer.Options;

public class OptionsTest
{
    @Test
    public void testMode ()
    {
        final Options options = new Options ();
        final String[] args = new String[]
        {
                "-config", "someval", "-mode", "Index"
        };

        // Check args & prepare usage string (in thrown AssertException).
        HcUtilArgs4j.getProgramOptions (args, options);

        assertEquals (Options.Mode.Index, options.m_mode);
    }
}
