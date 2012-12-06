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

package org.ramadda.util;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.User;
import org.ramadda.repository.type.*;

import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.io.*;
import java.io.File;

import java.util.List;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.*;


/**
 *
 *
 * @author Jeff McWhirter
 */
public class MailUtil {
    /**
     * _more_
     *
     * @param content _more_
     * @param desc _more_
     *
     * @throws Exception _more_
     */
    public static void extractText(Object content, StringBuffer desc)
            throws Exception {
        extractText(content, desc,"");
    }

    public static boolean extractText(Object content, StringBuffer desc, String tab)
        throws Exception {
        //System.err.println (tab + "Extract text");
        tab = tab+"\t";
        if (content instanceof MimeMultipart) {
            //System.err.println (tab + "Is Multipart");
            MimeMultipart multipart = (MimeMultipart) content;
            for (int i = 0; i < multipart.getCount(); i++) {
                MimeBodyPart part = (MimeBodyPart) multipart.getBodyPart(i);
                String       disposition = part.getDisposition();
                String       contentType = part.getContentType().toLowerCase();
                //System.err.println(tab+"part:" + part + " Type:" + contentType);
                Object       partContent = part.getContent();
                if (disposition == null) {
                    //System.err.println(tab+"disposition is null");
                    if (partContent instanceof MimeMultipart) {
                        //System.err.println(tab+"part is mulitpart");
                        if(extractText(partContent, desc, tab+"\t"))  {
                            //System.err.println(tab+"got text");
                            return true;
                        }
                    } else {
                        //Only ingest the text
                        if (contentType.indexOf("text/plain") >= 0) {
                            desc.append(partContent);
                            desc.append("\n");
                            return true;
                        }
                        //System.err.println(tab+"content type:" + contentType);
                    }
                    continue;
                }
                if (disposition.equals(Part.INLINE)
                        && (contentType.indexOf("text/plain") >= 0)) {
                    //System.err.println(tab+"inline text");
                    desc.append(partContent);
                    return true;
                }

                //System.err.println(tab+"disposition:" + disposition + " Type:" + contentType +" part:" + partContent.getClass().getName());
                /*
                if (disposition.equals(Part.ATTACHMENT)
                        || disposition.equals(Part.INLINE)) {
                    if (part.getFileName() != null) {
                        InputStream inputStream = part.getInputStream();
                    }
                    }*/
            }
        } else if (content instanceof Part) {
            //TODO
            Part part = (Part) content;
            //System.err.println(tab+"Part:" + part);
        } else {
            //System.err.println (tab + "content:" +  content.getClass().getName());
            //            //System.err.println ("xxx content:" +
            //            //System.err.println("Content");
            String contents = content.toString();
            desc.append(contents);
            desc.append("\n");
            return true;
        }
        return false;
    }



}
