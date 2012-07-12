/*
 * Copyright 2008-2011 Jeff McWhirter/ramadda.org
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

package org.ramadda.plugins.mail;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;

import javax.mail.*;
import javax.mail.internet.*;
import org.w3c.dom.*;


import ucar.unidata.xml.XmlUtil;
import ucar.unidata.util.IOUtil;
import org.ramadda.util.HtmlUtils;
import ucar.unidata.util.StringUtil;

import java.io.*;
import java.util.Date;
import java.util.List;


/**
 *
 *
 */
public class MailTypeHandler extends GenericTypeHandler {


    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public MailTypeHandler(Repository repository, Element entryNode)
        throws Exception {
        super(repository, entryNode);
    }


    @Override
    public void initializeEntryFromForm(Request request, Entry entry,
                                        Entry parent, boolean newEntry)
        throws Exception {
        if(!newEntry) return;
        if(!entry.isFile()) return;
        MimeMessage message  = new MimeMessage(null, getStorageManager().getFileInputStream(entry.getFile().toString()));
        if(entryHasDefaultName(entry)) {
            entry.setName(message.getSubject());
        }
        StringBuffer from = new StringBuffer();
        for(Address address: message.getFrom()) {
            from.append(address.toString());
            from.append("\n");
        }
        StringBuffer to = new StringBuffer();
        for(Address address: message.getAllRecipients()) {
            to.append(address.toString());
            to.append("\n");
        }
        StringBuffer desc = new StringBuffer();
        Object content = message.getContent();
        Date fromDttm = message.getSentDate();
        Date toDttm = message.getReceivedDate();
        if(toDttm==null) toDttm = fromDttm;
        if(fromDttm!=null) {
            entry.setStartDate(fromDttm.getTime());
        }
        if(toDttm!=null) {
            entry.setEndDate(toDttm.getTime());
        }

        if(content instanceof MimeMultipart){
            MimeMultipart multipart= (MimeMultipart) content;
            for(int i=0;i<multipart.getCount();i++) {
                MimeBodyPart part = (MimeBodyPart)multipart.getBodyPart(i);
                String disposition = part.getDisposition();
                if (disposition == null) {
                    Object partContent = part.getContent();
                    desc.append(partContent);
                    desc.append("\n");
                    continue;
                }
                if (disposition.equals(Part.ATTACHMENT) || 
                        disposition.equals(Part.INLINE)) {
                    if(part.getFileName()!=null) {
                        InputStream inputStream = part.getInputStream();
                        File f = getStorageManager().getTmpFile(request, part.getFileName());
                        OutputStream outputStream = getStorageManager().getFileOutputStream(f);
                        IOUtil.writeTo(inputStream, outputStream);
                        IOUtil.close(inputStream);
                        IOUtil.close(outputStream);
                        String fileName = getStorageManager().copyToEntryDir(entry,
                                                                             f).getName();
                        Metadata metadata =
                            new Metadata(getRepository().getGUID(), entry.getId(),
                             ContentMetadataHandler.TYPE_ATTACHMENT, false,
                             fileName, null, null, null, null);
                        entry.addMetadata(metadata);
                    }
                }
            }
        } else if(content instanceof Part) {
            //TODO
            Part part= (Part) content;
        } else {
            String contents = content.toString();
            desc.append(contents);
            desc.append("\n");
        }
        Object[] values = getEntryValues(entry);
        values[0] = from.toString();
        values[1] = to.toString();

        if(entry.getDescription().length()==0) {
            entry.setDescription(desc.toString());
        }
    }




@Override
    public Result getHtmlDisplay(Request request, Entry entry) throws Exception {
        StringBuffer sb = new StringBuffer();
        String from = entry.getValue(0,"");
        String to = entry.getValue(1,"");
        sb.append(HtmlUtils.formTable());
        sb.append(HtmlUtils.formEntry(msgLabel("From"),
                                      StringUtil.join(" ,",
                                                      StringUtil.split(from,"\n", true, true))));
        sb.append(HtmlUtils.formEntry(msgLabel("To"),
                                      StringUtil.join(" ,",
                                                      StringUtil.split(to,"\n", true, true))));


        sb.append(HtmlUtils.formEntry(msgLabel("Date"),
                                      getRepository().formatDate(request, new Date(entry.getStartDate()),null)));
        StringBuffer attachmentsSB = new StringBuffer();
        getMetadataManager().decorateEntry(request, entry, attachmentsSB, false);

        sb.append(HtmlUtils.formTableClose());
        sb.append(HtmlUtils.hr());
        String desc = entry.getDescription();
        desc = desc.replaceAll("\r\n\r\n", "\n<p>\n");
        sb.append(HtmlUtils.div(desc,HtmlUtils.cssClass("mail-body")));
        if(attachmentsSB.length()>0) {
            sb.append(HtmlUtils.hr());
            sb.append(HtmlUtils.makeShowHideBlock(msg("Attachments"),
                                                  "<div class=\"description\">" + attachmentsSB
                                                  + "</div>", false));
        }


        return new Result(msg("Email Message"), sb);
    }


}
