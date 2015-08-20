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
package au.com.breakpoint.hedron.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import au.com.breakpoint.hedron.core.context.ThreadContext;

public class HcUtilHttp
{
    public static String performHttpPost (final String urlString, final String request, final String contentType)
    {
        String s = null;

        try
        {
            final URL url = new URL (urlString);

            // An URLConnection instance does not establish the actual network connection on
            // creation. This will happen only when calling URLConnection.connect().
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection ();

            connection.setConnectTimeout (15 * 1000);
            connection.setRequestProperty ("Content-Type", contentType);
            connection.setDoOutput (true);// causes HTTP post
            connection.setUseCaches (false);

            try (final OutputStreamWriter wr = new OutputStreamWriter (connection.getOutputStream ()))
            {
                wr.write (request);
                wr.flush ();

                // Get the response.
                final InputStream inputStream = connection.getInputStream ();
                final InputStreamReader inputStreamReader = new InputStreamReader (inputStream);
                try (final BufferedReader rd = new BufferedReader (inputStreamReader))
                {
                    final StringBuilder sb = new StringBuilder ();

                    String line;
                    boolean first = true;
                    while ((line = rd.readLine ()) != null)
                    {
                        if (first)
                        {
                            first = false;
                        }
                        else
                        {
                            sb.append ("\r\n");
                        }

                        sb.append (line);
                    }

                    s = sb.toString ();
                }
            }
        }
        catch (final MalformedURLException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }
        catch (final IOException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }

        return s;
    }

}
