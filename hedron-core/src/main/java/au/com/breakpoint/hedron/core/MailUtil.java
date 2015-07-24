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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import au.com.breakpoint.hedron.core.context.ThreadContext;

public class MailUtil
{
    private static class HTMLDataSource implements DataSource
    {
        public HTMLDataSource (final String htmlString)
        {
            m_html = htmlString;
        }

        @Override
        public String getContentType ()
        {
            return "text/html";
        }

        // Return html string in an InputStream. A new stream must be returned each time.
        @Override
        public InputStream getInputStream () throws IOException
        {
            if (m_html == null)
            {
                throw new IOException ("Null HTML");
            }

            return new ByteArrayInputStream (m_html.getBytes ());
        }

        @Override
        public String getName ()
        {
            return "text/html DataSource";
        }

        @Override
        public OutputStream getOutputStream () throws IOException
        {
            throw new IOException ("Not implemented");
        }

        private final String m_html;
    }

    //public static void main (final String[] args)
    //{
    //    final String host = "smtpserver.yourisp.invalid";
    //    final String from = "sendFromMailAddress";
    //    final String to = "lperry@breakpoint.com.au";
    //    final String cc = "";
    //    final String bcc = "";
    //    final String subject = "Test E-Mail through Java";
    //
    //    sendMail (host, from, to, cc, bcc, subject);
    //}

    /**
     * Send an email.
     *
     * @param mailServer
     *            smtp host address
     * @param port
     * @param password
     * @param userid
     * @param from
     *            sender email address
     * @param to
     *            addresses in RFC822 syntax, eg
     *            "Bill Gleason <billg@example.com>, someone@example.com"
     * @param cc
     *            addresses in RFC822 syntax
     * @param bcc
     *            addresses in RFC822 syntax
     * @param subject
     *            subject of the email
     * @param htmlContent
     *            content of the email
     * @param style
     */
    public static void sendMailHtmlSmtp (final String mailServer, final int port, final String password,
        final String userid, final String from, final String to, final String cc, final String bcc,
        final String subject, final String htmlContent, final String style)
    {
        // Create properties for the Session
        final Properties props = new Properties ();

        // If using static Transport.send(),
        // need to specify the mail server here
        props.put ("mail.smtp.host", mailServer);
        props.put ("mail.smtps.port", port);

        // To see what is going on behind the scene
        props.put ("mail.debug", "true");

        // Get a session
        final Session session = Session.getInstance (props);

        // Get a Transport object to send e-mail
        try
        {
            final Transport transport = session.getTransport ("smtp");

            // Connect only once here
            // Transport.send() disconnects after each send
            // Usually, no username and password is required for SMTP
            if (userid != null)
            {
                transport.connect (mailServer, userid, password);
            }
            else
            {
                transport.connect ();
            }

            // Instantiate a message
            final Message msg = new MimeMessage (session);
            msg.setFrom (new InternetAddress (from));

            // Parse comma/space-separated list.
            final Address[] recipientsTo = setAddresses (msg, to, Message.RecipientType.TO);

            if (HcUtil.safeGetLength (cc) > 0)
            {
                // Parse comma/space-separated list.
                setAddresses (msg, cc, Message.RecipientType.CC);
            }

            if (HcUtil.safeGetLength (bcc) > 0)
            {
                // Parse comma/space-separated list.
                setAddresses (msg, bcc, Message.RecipientType.BCC);
            }

            msg.setSubject (subject);
            msg.setSentDate (new Date ());

            final String html =
                String.format ("<html><head><title>%s</title><style>%s</style></head><body>%s</body></html>", subject,
                    style != null ? style : "", htmlContent);

            // HTMLDataSource is an inner class
            msg.setDataHandler (new DataHandler (new HTMLDataSource (html)));

            msg.saveChanges ();
            transport.sendMessage (msg, recipientsTo);

            transport.close ();
        }
        catch (final NoSuchProviderException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }
        catch (final MessagingException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }
    }

    private static InternetAddress[] setAddresses (final Message msg, final String addresses, final RecipientType type)
    {
        InternetAddress[] recipientsBcc = null;

        try
        {
            recipientsBcc = InternetAddress.parse (addresses, true);
            msg.setRecipients (type, recipientsBcc);
        }
        catch (final AddressException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }
        catch (final MessagingException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }

        return recipientsBcc;
    }
}
