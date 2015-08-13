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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.Test;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.DirectoryTree;
import au.com.breakpoint.hedron.core.GenericFactory;
import au.com.breakpoint.hedron.core.JsonUtil;
import au.com.breakpoint.hedron.core.Tuple.E2;

public class JsonUtilTest
{
    //@Test
    // TODO 1 move to roottools
    public void testGsonReciprocality ()
    {
        final Gson gson = new GsonBuilder ().setPrettyPrinting ().create ();

        final String directoryPath = HcUtil.getProjectsDirectoryName () + "/Archiver/test";

        final DirectoryTree dt1 = new DirectoryTree (directoryPath);
        final DirectoryTree dt2 = new DirectoryTree (directoryPath);
        assertEquals (dt1.getFiles (), dt2.getFiles ());
        assertEquals (dt1.getAbsolutePaths (), dt2.getAbsolutePaths ());
        assertEquals (dt1.getRelativeFilepaths (), dt2.getRelativeFilepaths ());

        final List<String> fpsl = dt1.getAbsolutePaths ();

        if (true)
        {
            final String jsonArray = gson.toJson (fpsl);
            //System.out.println (jsonArray);

            final Type listType = new TypeToken<List<String>> ()
            {
            }.getType ();

            final List<String> fpsl2 = gson.fromJson (jsonArray, listType);
            assertEquals (fpsl, fpsl2);
        }

        if (true)
        {
            final String[] fps = fpsl.toArray (new String[fpsl.size ()]);
            final String jsonArray = gson.toJson (fps);
            //System.out.println (jsonArray);

            final String[] fps2 = gson.fromJson (jsonArray, String[].class);
            assertArrayEquals (fps, fps2);
        }
    }

    @Test
    public void testGsonReciprocalityMap ()
    {
        final Map<String, BigInteger> m1 = GenericFactory.newHashMap ();
        for (int i = 0; i < 5; ++i)
        {
            final String key = String.valueOf (i);
            m1.put (key, new BigInteger (key));
        }

        final String json = JsonUtil.toJson (m1);

        final Type genericType = new TypeToken<TreeMap<String, BigInteger>> ()
        {
        }.getType ();
        final Map<String, BigInteger> m2 = JsonUtil.fromJson (json, genericType);
        assertEquals (m1, m2);
    }

    @Test
    public void testStringArray ()
    {
        final String[][] a =
            {
                    {},
                    {
                            "b"
                },
                    {
                            "c",
                            "c",
                            "c"
                }
        };
        final String json = JsonUtil.toJson (a);
        //System.out.println (Arrays.deepToString (a));

        final String[][] a2 = JsonUtil.fromJson (json, a.getClass ());
        //System.out.println (Arrays.deepToString (a2));

        assertArrayEquals (a, a2);
    }

    @Test
    public void testTuple ()
    {
        final E2<String, String> t = E2.of ("asdf", "eeee");
        final String json = JsonUtil.toJson (t);
        //System.out.println (json);

        @SuppressWarnings ("unchecked")
        final E2<String, String> t2 = JsonUtil.fromJson (json, t.getClass ());
        //System.out.println (t2);

        assertEquals (t, t2);
    }

    // [Lists of Tuples don't work] @Test
    //public void testTuples ()
    //{
    //    final List<E2<String, String>> l =
    //        GenericFactory.newArrayList (E2.of ("asdf", "sss4"), E2.of ("bbbb", "dddddd"));
    //    final String json = JsonUtil.toJson (l);
    //    System.out.println (json);
    //
    //    @SuppressWarnings ("unchecked")
    //    final List<E2<String, String>> l2 = JsonUtil.fromJson (json, l.getClass ());
    //    System.out.println (l2);
    //
    //    assertEquals (l, l2);
    //}
}
