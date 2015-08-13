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

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.core.log.Logging;

public class HcUtilJaxb
{
    public static String marshalXml (final Writer writer, final Object o, final boolean prettyPrint)
    {
        String xml = null;

        try
        {
            final String packageName = getPackageName (o.getClass ());
            final JAXBContext jaxbContext = JAXBContext.newInstance (packageName);
            final Marshaller marshaller = jaxbContext.createMarshaller ();
            if (prettyPrint)
            {
                marshaller.setProperty (Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            }

            marshaller.marshal (o, writer);
            xml = writer.toString ();
        }
        catch (final JAXBException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }

        return xml;
    }

    public static void marshalXmlFile (final String xmlFilename, final Object o)
    {
        try
        {
            final String packageName = getPackageName (o.getClass ());
            final JAXBContext jaxbContext = JAXBContext.newInstance (packageName);
            final Marshaller marshaller = jaxbContext.createMarshaller ();

            final File file = getFile (xmlFilename);
            marshaller.marshal (o, file);// different marshal() overload
        }
        catch (final JAXBException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }
    }

    public static String marshalXmlString (final Object o)
    {
        final Writer writer = new StringWriter ();
        marshalXml (writer, o, false);

        return writer.toString ();
    }

    public static <T> T unmarshalXmlFile (final String xmlFilename, final Class<T> theClass)
    {
        T d = null;

        // Get object from xml file via JAXB mapping code.
        try
        {
            final String packageName = getPackageName (theClass);
            final JAXBContext jaxbContext = JAXBContext.newInstance (packageName);
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller ();

            final File file = getFile (xmlFilename);
            @SuppressWarnings ("unchecked")
            final T unchecked = (T) unmarshaller.unmarshal (file);
            d = unchecked;
        }
        catch (final JAXBException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }

        return d;
    }

    public static <T> T unmarshalXmlString (final String xml, final Class<T> theClass)
    {
        T d = null;

        // Get object from xml file via JAXB mapping code.
        try
        {
            final String packageName = getPackageName (theClass);
            final JAXBContext jaxbContext = JAXBContext.newInstance (packageName);
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller ();

            final Reader reader = new StringReader (xml);
            @SuppressWarnings ("unchecked")
            final T unchecked = (T) unmarshaller.unmarshal (reader);
            d = unchecked;
        }
        catch (final JAXBException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            Logging.logError ("Unmarshallable XML %n    [%s]", HcUtil.abbreviate (xml, 500));
            ThreadContext.throwFault (e);
        }

        return d;
    }

    private static File getFile (final String xmlFilename)
    {
        final File file = HcResourceUtil.getFile (xmlFilename, true);
        ThreadContext.assertError (file != null, "Cannot find file [%s]", xmlFilename);
        return file;
    }

    private static String getPackageName (final Class<?> theClass)
    {
        return theClass.getPackage ().getName ();
    }
}
