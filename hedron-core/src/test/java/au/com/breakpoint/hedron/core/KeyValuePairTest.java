package au.com.breakpoint.hedron.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import au.com.breakpoint.hedron.core.KeyValuePair;

public class KeyValuePairTest
{
    @Test
    public void testEqualsObject ()
    {
        final KeyValuePair<String, Integer> kvp = KeyValuePair.of ("aa", 1);
        final KeyValuePair<String, Integer> kbp2 = KeyValuePair.of ("a" + "a", 1);

        assertEquals (kvp, kvp);
        assertEquals (kvp, kbp2);

        assertTrue (kvp.equals (kvp));
        assertTrue (kvp.equals (kbp2));

        assertTrue (!kvp.equals (KeyValuePair.of ("ab", 1)));
        assertTrue (!kvp.equals (KeyValuePair.of ("aa", 2)));

        assertTrue (!kvp.equals (null));
        assertTrue (!kvp.equals (4));
    }
}
