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

package org.ramadda.plugins.biblio;


import org.ramadda.repository.*;
import org.ramadda.util.Utils;


import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.DateUtil;

import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;

import java.util.List;

import ucar.unidata.util.TwoFacedObject;

/**
 * Class description
 *
 *
 * @version        $version$, Thu, Nov 25, '10
 * @author         Enter your name here...
 */
public class BiblioImporter extends ImportHandler implements BiblioConstants {




    /**
     * ctor
     */
    public BiblioImporter(Repository repository) {
        super(repository);
    }


    @Override
    public void addImportTypes(List<TwoFacedObject>importTypes) {
        super.addImportTypes(importTypes);
        importTypes.add(new TwoFacedObject("Bibliography Import","biblio"));
    }


    public Result handleRequest(Request request, Repository repository,
                                String uploadedFile, Entry parentEntry)
            throws Exception {
        if(!request.getString(ARG_IMPORT_TYPE,"").equals("biblio")) {
            return null;
        }
        return null;
    }




    public void process(String s) throws Exception {
        boolean inKeyword = false;
        List<Entry> entries= new ArrayList<Entry>();
        List<String> keywords = new ArrayList<String>();
        List<String> authors = new ArrayList<String>();
        Entry entry  = null; 
        Object[] values = new Object[10];
        for(String line: StringUtil.split(s, "\n",true,true)) {
            List<String> toks = StringUtil.splitUpTo(line," ",2);
            if(toks.get(0).startsWith("%") && toks.size() ==2) {
                String tag = toks.get(0);
                String value = toks.get(1);
                if(tag.equals(TAG_BIBLIO_TYPE)) {
                    if(entry !=null) {
                        values[IDX_OTHER_AUTHORS]  = StringUtil.join("\n",authors);
                        for(int idx=0;idx<TAGS.length;idx++) {
                            if(values[idx] == null) {
                                values[idx] = "";
                            }
                        }
                        entry.setValues(values);
                        //Add authors and keywords
                        entries.add(entry);
                    }
                    keywords = new ArrayList<String>();
                    authors =  new ArrayList<String>();
                    entry  = new Entry();
                    values = new Object[10];
                    values[IDX_TYPE]  =value;
                    continue;
                }

                if(tag.equals(TAG_BIBLIO_AUTHOR)) {
                    if(!Utils.stringDefined((String)values[IDX_PRIMARY_AUTHOR])) {
                        values[IDX_PRIMARY_AUTHOR] = value;
                    } else {
                        authors.add(value);
                    }
                    continue;
                }

                if(tag.equals(TAG_BIBLIO_TITLE)) {
                    entry.setName(value);
                    continue;
                }
                if(tag.equals(TAG_BIBLIO_DESCRIPTION)) {
                    entry.setDescription(value);
                    continue;
                }

                if(tag.equals(TAG_BIBLIO_DATE)) {
                    Date date  = DateUtil.parse(value);
                    entry.setStartDate(date.getTime());
                    entry.setEndDate(date.getTime());
                    continue;
                }

                if(tag.equals(TAG_BIBLIO_KEYWORD)) {
                    inKeyword = true;
                    keywords.add(value);
                    continue;
                }
                boolean gotone = false;
                for(int idx=0;idx<TAGS.length && !gotone;idx++) {
                    if(tag.equals(TAGS[idx])) {
                        values[INDICES[idx]] = value;
                        gotone = true;
                    }
                }

                if(gotone) {
                    continue;
                }
                //                System.err.println ("Unknown tag:" + tag + "=" + value);
            } else if(inKeyword) {
                keywords.add(line);
                continue;
            } else {
                System.err.println ("LINE:" +line);
            }
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
        BiblioImporter importer = new BiblioImporter(null);
        for (String file : args) {
            importer.process(IOUtil.readContents(file,(String)null));
        }
    }

}
