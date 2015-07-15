//                       __________________________________
//                ______|         Copyright 2008           |______
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
package au.com.breakpoint.hedron.core;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import au.com.breakpoint.hedron.core.context.ThreadContext;

public class HcResourceUtil
{
    public static ClassLoader getDefaultClassLoader ()
    {
        ClassLoader cl = null;
        try
        {
            cl = Thread.currentThread ().getContextClassLoader ();
        }
        catch (final Throwable ex)
        {
            // Cannot access thread context ClassLoader - falling back to system class loader...
        }
        if (cl == null)
        {
            // No thread context class loader -> use class loader of this class.
            cl = HcResourceUtil.class.getClassLoader ();
        }
        return cl;
    }

    public static File getFile (final String location, final boolean allowMissingFile)
    {
        boolean proceed = false;

        File file = new File (location);
        if (file.exists ())
        {
            proceed = true;
        }
        else
        {
            // Try to find it using the class loader.
            final URL url = getDefaultClassLoader ().getResource (location);
            ThreadContext.assertError (allowMissingFile || url != null, "[%s] cannot be resolved to absolute file path",
                location);

            if (url != null) // test again for allowMissingFile case
            {
                try
                {
                    file = new File (toURI (url).getSchemeSpecificPart ());
                }
                catch (final URISyntaxException e)
                {
                    // Propagate exception as unchecked fault up to the fault barrier.
                    ThreadContext.throwFault (e);
                }

                //ThreadContext.assertError (file.exists (), "File [%s] not found", location);
                proceed = file.exists ();// not accessible if inside jar file etc
            }
        }

        return proceed ? file : null;
    }

    public static URI toURI (final String location) throws URISyntaxException
    {
        return new URI (replace (location, " ", "%20"));
    }

    public static URI toURI (final URL url) throws URISyntaxException
    {
        return toURI (url.toString ());
    }

    // Helper lifted from Spring.
    private static String replace (final String inString, final String oldPattern, final String newPattern)
    {
        if (inString == null)
        {
            return null;
        }
        if (oldPattern == null || newPattern == null)
        {
            return inString;
        }

        final StringBuilder sbuf = new StringBuilder ();
        // output StringBuilder we'll build up
        int pos = 0;// our position in the old string
        int index = inString.indexOf (oldPattern);
        // the index of an occurrence we've found, or -1
        final int patLen = oldPattern.length ();
        while (index >= 0)
        {
            sbuf.append (inString.substring (pos, index));
            sbuf.append (newPattern);
            pos = index + patLen;
            index = inString.indexOf (oldPattern, pos);
        }
        sbuf.append (inString.substring (pos));

        // remember to append any characters to the right of a match
        return sbuf.toString ();
    }
}
