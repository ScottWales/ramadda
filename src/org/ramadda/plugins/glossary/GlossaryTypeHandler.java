/*
 * Copyright 2008-2011 Jeff McWhirter/ramadda.org
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package org.ramadda.plugins.glossary;


import org.w3c.dom.*;

import org.ramadda.repository.*;
import org.ramadda.repository.database.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;


import ucar.unidata.sql.Clause;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;



import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;



/**
 *
 *
 */
public class GlossaryTypeHandler extends ExtensibleGroupTypeHandler {


    public static final String ARG_LETTER = "letter";

    /** _more_ */
    public static String TYPE_GLOSSARY = "glossary";

    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public GlossaryTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }



    public int getDefaultQueryLimit(Request request, Entry entry) {
        if(request.defined(ARG_OUTPUT)) return super.getDefaultQueryLimit(request, entry);
        return 1000;
    }


    public List<Clause> assembleWhereClause(Request request,
                                            StringBuffer searchCriteria)
            throws Exception {
        List<Clause> where       = super.assembleWhereClause(request, searchCriteria);
        if(request.defined(ARG_OUTPUT)) return where;
        if(!request.defined(ARG_LETTER)) return where;
        String letter = request.getString(ARG_LETTER,"A");
        if(letter.equals("all")) return where;
        where.add(Clause.like(Tables.ENTRIES.COL_NAME,letter+"%"));
        return where;
    }


    public void getChildrenEntries(Request request, Entry group, List<Entry> entries, List<Entry> subGroups, List<Clause> where) throws Exception {
        if(!request.defined(ARG_OUTPUT) && !request.defined(ARG_LETTER)) return;
        super.getChildrenEntries(request, group, entries, subGroups, where);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param group _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result getHtmlDisplay(Request request, Entry group,
                                 List<Entry> subGroups, List<Entry> entries)
            throws Exception {
        //If the user specifically selected the html output then don't show as a gossary
        if(request.defined(ARG_OUTPUT)) return null;

        StringBuffer sb = new StringBuffer();
        sb.append(group.getDescription());
        sb.append(HtmlUtil.p());

        boolean canAdd = getAccessManager().canDoAction(request, group,
                             Permission.ACTION_NEW);

        if (canAdd) {
            String label = 
                HtmlUtil.img(getRepository().iconUrl(ICON_NEW),  msg("New GLOSSARY Question"))+" "  + msg("Create new glossary entry");
            sb.append(HtmlUtil
                      .href(HtmlUtil
                            .url(request
                                 .entryUrl(getRepository().URL_ENTRY_FORM, group,
                                           ARG_GROUP), ARG_TYPE,
                                 GlossaryEntryTypeHandler.TYPE_GLOSSARYENTRY), label));
        }

        List<String> letters = new ArrayList<String>();
        Hashtable<String,StringBuffer> letterToBuffer = new Hashtable<String,StringBuffer>();

        subGroups.addAll(entries);


        sb.append(HtmlUtil.p());
        sb.append("<center>");
        List<String> header = new ArrayList<String>();
        String theLetter = request.getString(ARG_LETTER,"");
        String[] ltrs = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","0","1","2","3","4","5","6","7","8","9","all"};
        String url = request.getUrl(ARG_LETTER);
        for(String letter: ltrs) {
            if(letter.equals(theLetter)) {
                header.add(HtmlUtil.b(letter));
            } else {
                header.add(HtmlUtil.href(url+"&" + ARG_LETTER +"=" + letter, letter));
            }
        }
        sb.append(StringUtil.join("&nbsp;|&nbsp;", header));
        sb.append("</center>");

        if(subGroups.size()==0 && request.defined(ARG_LETTER)) {
            sb.append(getRepository().showDialogNote(msg("No glossary entries found")));
        }
        sb.append("<style type=\"text/css\">.glossary_entry {margin:0px;margin-bottom:5px;}\n");
        sb.append(".glossary_entries {margin:0px;margin-bottom:5px;}\n</style>");
        for(Entry entry: subGroups) {
            String name  = entry.getName();
            String letter = "-";
            if(name.length()>0) {
                letter = name.substring(0,1).toUpperCase();
            }
            StringBuffer letterBuffer =letterToBuffer.get(letter);
            if(letterBuffer==null) {
                letterToBuffer.put(letter, letterBuffer = new StringBuffer());
                letters.add(letter);
                letterBuffer.append("<ul class=\"glossary_entries\">");
            }
            String href= getEntryManager().getAjaxLink(request, entry, name).toString();
            letterBuffer.append(HtmlUtil.li(href, HtmlUtil.cssClass("glossary_entry")));
        }

        letters = (List<String>) Misc.sort(letters);

        for(String letter: letters) {
            StringBuffer letterBuffer =letterToBuffer.get(letter);
            letterBuffer.append("</ul>");
            sb.append("<a name=\"letter_" + letter +"\"></a>");
            sb.append(HtmlUtil.h2(letter));
            sb.append(letterBuffer);
        }
        return new Result(msg("GLOSSARY"), sb);
    }




}
