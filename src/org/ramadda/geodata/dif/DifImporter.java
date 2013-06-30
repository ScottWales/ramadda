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

package org.ramadda.geodata.dif;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.Metadata;
import org.ramadda.repository.metadata.*;
import org.ramadda.util.DifUtil;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;


import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.DateUtil;


import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.util.ArrayList;
import java.util.Date;

import java.util.List;

import ucar.unidata.util.TwoFacedObject;

/**
 * Class description
 *
 *
 * @version        $version$, Thu, Nov 25, '10
 * @author         Enter your name here...
 */
public class DifImporter extends ImportHandler {

    public static final String TYPE_DIF = "DIF";
    public static final String ARG_DIF_TYPE = "dif.type";

    /**
     * ctor
     */
    public DifImporter(Repository repository) {
        super(repository);
    }


    @Override
     public void addImportTypes(List<TwoFacedObject>importTypes, StringBuffer formBuffer) {
        super.addImportTypes(importTypes, formBuffer);
        importTypes.add(new TwoFacedObject("Dif Import",TYPE_DIF));
        //        formBuffer.append(HtmlUtils.formEntry(msgLabel("DIF Type"), HtmlUtils.input(ARG_DIF_TYPE,"")));
    }


    public Result handleRequest(Request request, Repository repository,
                                String uploadedFile, Entry parentEntry)
            throws Exception {
        if(!request.getString(ARG_IMPORT_TYPE,"").equals(TYPE_DIF)) {
            return null;
        }
        List<Entry> entries = new ArrayList<Entry>();
        if(uploadedFile.endsWith(".zip")) {
            List<File> unzippedFiles = getStorageManager().unpackZipfile(request, uploadedFile);
            for(File f:unzippedFiles) {
                //                if(f.getName().endsWith(".txt")) {
                String xml =  new String(IOUtil.readBytes(getStorageManager().getFileInputStream(f)));
                processXml(request, parentEntry, xml, entries);
            }
        } else {
            String xml =  new String(IOUtil.readBytes(getStorageManager().getFileInputStream(uploadedFile)));
            processXml(request, parentEntry, xml, entries);
        } 

        StringBuffer sb = new StringBuffer();
        for(Entry entry: entries) {
            entry.setUser(request.getUser());
        }
        getEntryManager().addNewEntries(request, entries);
        sb.append(msgHeader("Imported entries"));
        sb.append("<ul>");
        for(Entry entry: entries) {
            sb.append("<li> ");
            sb.append(getEntryManager().getBreadCrumbs(request, entry, true, parentEntry)[1]);
        }

        return getEntryManager().addEntryHeader(request, parentEntry,
                                                new Result("", sb));
    }


    private void processXml(Request request, Entry parentEntry, String xml, List<Entry> entries) throws Exception {
        String type = request.getString(ARG_DIF_TYPE,"project_instrument");
        Entry entry  =  getRepository().getTypeHandler(type).createEntry(getRepository().getGUID());
        Object[] values = entry.getTypeHandler().getValues(entry);

        Element root = XmlUtil.getRoot(xml);
        String title = XmlUtil.getGrandChildText(root, DifUtil.TAG_Entry_Title,"no name");
        String id = XmlUtil.getGrandChildText(root, DifUtil.TAG_Entry_ID,"");
        values[0] =  id;
        

        addMetadata(entry, root, DifUtil.TAG_Keyword, DifMetadataHandler.TYPE_KEYWORD);
        addMetadata(entry, root, DifUtil.TAG_ISO_Topic_Category, DifMetadataHandler.TYPE_ISO_TOPIC_CATEGORY);
        addMetadata(entry, root, DifUtil.TAG_Originating_Center, DifMetadataHandler.TYPE_ORIGINATING_CENTER);
        addMetadata(entry, root, DifUtil.TAG_Reference, DifMetadataHandler.TYPE_REFERENCE);
        addMetadata(entry, root, DifUtil.TAG_Distribution, DifMetadataHandler.TYPE_DISTRIBUTION, DifUtil.TAGS_Distribution);
        addMetadata(entry, root, DifUtil.TAG_Project, DifMetadataHandler.TYPE_PROJECT, DifUtil.TAGS_Project);
        addMetadata(entry, root, DifUtil.TAG_Parameters, DifMetadataHandler.TYPE_PARAMETERS, DifUtil.TAGS_Parameters);
        addMetadata(entry, root, DifUtil.TAG_Data_Set_Citation, DifMetadataHandler.TYPE_DATA_SET_CITATION, DifUtil.TAGS_Data_Set_Citation);
        entry.setDescription(XmlUtil.getGrandChildText(root, DifUtil.TAG_Summary,""));
        entry.setName(title);
        entry.setParentEntryId(parentEntry.getId());
        entries.add(entry);
    }


    private void addMetadata(Entry entry, Element root, String tag, String metadataId) throws Exception {
        for(Element node:(List<Element>) XmlUtil.findChildren(root, tag)) {
            String value = XmlUtil.getChildText(node);
            Metadata metadata = new Metadata(getRepository().getGUID(),
                                            entry.getId(), metadataId,
                                            DFLT_INHERITED, value,
                                            Metadata.DFLT_ATTR,
                                            Metadata.DFLT_ATTR,
                                            Metadata.DFLT_ATTR,
                                            Metadata.DFLT_EXTRA);
            entry.addMetadata(metadata);
        }
    }


    private void addMetadata(Entry entry, Element root, String tag, String metadataId, String[]subTags) throws Exception {
        for(Element node:(List<Element>) XmlUtil.findChildren(root, tag)) {
            String[] values = new String[subTags.length];
            for(int i=0;i<values.length;i++) {
                values[i]= XmlUtil.getGrandChildText(node, subTags[i],"");
            }
            Metadata metadata = new Metadata(getRepository().getGUID(),
                                             entry.getId(), metadataId,
                                             values);
            entry.addMetadata(metadata);
        }
    }



    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
        DifImporter importer = new DifImporter(null);
        for (String file : args) {
            List<File> files = new ArrayList<File>();
            StringBuffer sb = new StringBuffer();
            //            importer.processXml(null, "parent", IOUtil.readContents(file,(String)null),files, sb);
        }
    }

}
