/*
* Copyright 2008-2012 Jeff McWhirter/ramadda.org
*                     Don Murray/CU-CIRES
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this 
* software and associated documentation files (the "Software"), to deal in the Software 
* without restriction, including without limitation the rights to use, copy, modify, 
* merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
* permit persons to whom the Software is furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies 
* or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
* PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
* OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
* DEALINGS IN THE SOFTWARE.
*/

package org.ramadda.repository.admin;


import org.ramadda.repository.*;

import org.ramadda.repository.auth.*;
import org.ramadda.repository.database.*;
import org.ramadda.repository.ftp.FtpManager;

import org.ramadda.repository.harvester.*;

import org.ramadda.repository.output.*;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;

import org.w3c.dom.*;



import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;

import ucar.unidata.util.StringUtil;

import java.io.*;


import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;

import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;


import javax.mail.internet.MimeMessage;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;


/**
 * Class Admin
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class MailManager extends RepositoryManager {

    public static final String PROP_SMTP_USER = "ramadda.admin.smtp.user";
    public static final String PROP_SMTP_PASSWORD = "ramadda.admin.smtp.password";
    public static final String PROP_SMTP_STARTTLS = "ramadda.admin.smtp.starttls";

    /**
     * _more_
     *
     * @param repository _more_
     */
    public MailManager(Repository repository) {
        super(repository);

    }


    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public void addAdminSettings(Request request, StringBuffer sb) throws Exception {
        sb.append(
                  HtmlUtils.formEntry(
                                      msgLabel("Mail Server"), HtmlUtils.input(
                                                                               PROP_ADMIN_SMTP, getProperty(
                                                                                                            PROP_ADMIN_SMTP, ""), HtmlUtils.SIZE_40) + " "
                                      + msg("For sending password reset messages")));
    }

    public void applyAdminConfig(Request request) throws Exception {
        getRepository().writeGlobal(request, PROP_ADMIN_SMTP, true);
    }


    public String getSmtpServer() {
        return getPropertyFromTree(PROP_ADMIN_SMTP,"");
    }

    public String getAdminEmail() {
        return getPropertyFromTree(PROP_ADMIN_EMAIL, "");
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isEmailCapable() {
        if(getRepository().getParentRepository()!=null) {
            return getRepository().getParentRepository().getMailManager().isEmailCapable();
        }

        String smtpServer = getSmtpServer();
        String serverAdmin = getAdminEmail();
        if ((serverAdmin.length() == 0) || (smtpServer.length() == 0)) {
            return false;
        }

        return true;
    }

    /**
     * _more_
     *
     * @param to _more_
     * @param subject _more_
     * @param contents _more_
     * @param asHtml _more_
     *
     * @throws Exception _more_
     */
    public void sendEmail(String to, String subject, String contents,
                          boolean asHtml)
            throws Exception {
        sendEmail(to, getAdminEmail(), subject, contents, asHtml);
    }


    /**
     * _more_
     *
     * @param to _more_
     * @param from _more_
     * @param subject _more_
     * @param contents _more_
     * @param asHtml _more_
     *
     * @throws Exception _more_
     */
    public void sendEmail(String to, String from, String subject,
                          String contents, boolean asHtml)
            throws Exception {
        sendEmail((List<Address>) Misc.newList(new InternetAddress(to)),
                  new InternetAddress(from), subject, contents, false,
                  asHtml);
    }


    /**
     * _more_
     *
     * @param to _more_
     * @param from _more_
     * @param subject _more_
     * @param contents _more_
     * @param bcc _more_
     * @param asHtml _more_
     *
     * @throws Exception _more_
     */
    public void sendEmail(List<Address> to, InternetAddress from,
                          String subject, String contents, boolean bcc,
                          boolean asHtml)
            throws Exception {
        if(getRepository().getParentRepository()!=null) {
            getRepository().getParentRepository().getMailManager().sendEmail(to, from, subject, contents, bcc, asHtml);
        }


        if ( !isEmailCapable()) {
            throw new IllegalStateException(
                "This RAMADDA server has not been configured to send email");
        }


        String smtpServer = getSmtpServer();
        String smtpUser = getPropertyFromTree(PROP_SMTP_USER, (String) null);
        String smtpPassword = getPropertyFromTree(PROP_SMTP_PASSWORD, (String) null);
        boolean startTls = getPropertyFromTree(PROP_SMTP_STARTTLS, "false").equals("true");



        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", smtpServer);
        props.put("mail.from", from.getAddress());
        javax.mail.Session session = Session.getInstance(props,
                                                         null);
	if(startTls) {
	    // Port we will connect to on the Amazon SES SMTPendpoint. We are choosing port 25 because we will use
	    // STARTTLS to encrypt the connection.
	    props.put("mail.smtp.port", 25); 

        
	    // Set properties indicating that we want to use STARTTLS to encrypt the connection.
	    // The SMTP session will begin on an unencrypted connection, and then the client
	    // will issue a STARTTLS command to upgrade to an encrypted connection.
	    props.put("mail.smtp.auth", "true");
	    props.put("mail.smtp.starttls.enable", "true");
	    props.put("mail.smtp.starttls.required", "true");
	}


        if(smtpUser!=null) {
            props.put("mail.smtp.user", smtpUser);
        }


        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(from);
        Address[] array = new Address[to.size()];
        for (int i = 0; i < to.size(); i++) {
            array[i] = to.get(i);
        }
        msg.setRecipients((bcc
                           ? Message.RecipientType.BCC
                           : Message.RecipientType.TO), array);
        msg.setSubject(subject);
        msg.setSentDate(new Date());
        msg.setContent(contents, (asHtml
                                  ? "text/html"
                                  : "text/plain"));


        // Create a transport.        
        Transport transport = session.getTransport();
                    
        // Send the message.
        try    {
            if(smtpUser!=null) {
                transport.connect(smtpServer, smtpUser, smtpPassword);
                transport.sendMessage(msg, msg.getAllRecipients());
            } else {
                Transport.send(msg);
            }
        } finally {
            // Close and terminate the connection.
            transport.close();         
        }

        /*

        if(smtpPassword!=null) {
            System.err.println("password:" + smtpPassword);
            Transport tr = session.getTransport();
            tr.connect(null, smtpPassword);
            tr.send(msg);
        } else {
            Transport.send(msg);
        }
        */
    }



    public static void main(String[]args) throws Exception {
	List<Address>    to = (List<Address>) Misc.newList(new InternetAddress("jeff.mcwhirter@gmail.com"));
	InternetAddress from = new InternetAddress("jeff.mcwhirter@gmail.com");
        sendEmailNew(to, from, "test", "message", false,true);
    }

    public static void sendEmailNew(List<Address> to, InternetAddress from,
				    String subject, String body, boolean bcc,
				    boolean asHtml) throws Exception {

        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");

	


	boolean startTls = true;
	String smtpServer = "email-smtp.us-east-1.amazonaws.com";    
	String smtpUser = "";
	String smtpPassword = "";



	if(startTls) {
	    // Port we will connect to on the Amazon SES SMTPendpoint. We are choosing port 25 because we will use
	    // STARTTLS to encrypt the connection.
	    props.put("mail.smtp.port", 25); 

        
	    // Set properties indicating that we want to use STARTTLS to encrypt the connection.
	    // The SMTP session will begin on an unencrypted connection, and then the client
	    // will issue a STARTTLS command to upgrade to an encrypted connection.
	    props.put("mail.smtp.auth", "true");
	    props.put("mail.smtp.starttls.enable", "true");
	    props.put("mail.smtp.starttls.required", "true");
	}

        // Create a Session object to represent a mail session with the specified properties. 
	//        Session session = Session.getDefaultInstance(props);
	//        props.put("mail.smtp.host", smtpServer);
        props.put("mail.from", from.getAddress());
        javax.mail.Session session = Session.getInstance(props,
                                                         null);

        // Create a message with the specified information. 
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("jeff.mcwhirter@gmail.com"));
        Address[] array = new Address[to.size()];
        for (int i = 0; i < to.size(); i++) {
            array[i] = to.get(i);
        }
        msg.setRecipients((bcc
                           ? Message.RecipientType.BCC
                           : Message.RecipientType.TO), array);

        msg.setSubject(subject);
        msg.setSentDate(new Date());
        msg.setContent(body, (asHtml
                                  ? "text/html"
                                  : "text/plain"));

            
        // Create a transport.        
        Transport transport = session.getTransport();
                    
        // Send the message.
        try    {
            if(smtpUser!=null) {
                transport.connect(smtpServer, smtpUser, smtpPassword);
            }
            // Send the email.
            transport.sendMessage(msg, msg.getAllRecipients());
        } finally {
            // Close and terminate the connection.
            transport.close();         
        }
    }




}
