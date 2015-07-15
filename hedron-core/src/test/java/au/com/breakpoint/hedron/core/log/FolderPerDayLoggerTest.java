package au.com.breakpoint.hedron.core.log;

import static org.junit.Assert.assertEquals;
import java.util.Date;
import org.junit.Test;
import au.com.breakpoint.hedron.core.log.FolderPerDayLogger;

public class FolderPerDayLoggerTest
{
    @Test
    public void testGetLogSubfolder ()
    {
        //System.out.println (new Date ().getTime ());
        final Date date = new Date (1331251144615L);
        final String s = FolderPerDayLogger.getLogSubfolder (date);
        assertEquals ("/2012-03-09", s);
    }
}
