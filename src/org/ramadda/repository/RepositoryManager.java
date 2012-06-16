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

package org.ramadda.repository;


import org.ramadda.repository.admin.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.database.*;
import org.ramadda.repository.harvester.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.WikiManager;
import org.ramadda.repository.search.*;



import org.w3c.dom.*;


import ucar.unidata.sql.SqlUtil;

import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.GuiUtils;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;

import ucar.unidata.xml.XmlUtil;




import java.io.*;

import java.io.File;
import java.io.InputStream;

import java.lang.reflect.*;



import java.net.*;


import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;



/**
 *
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class RepositoryManager implements RepositorySource, Constants,
                                          RequestHandler {


    /** _more_ */
    public static final String HELP_ROOT =
        "http://facdev.unavco.org/repository";


    /** _more_ */
    protected Repository repository;



    /**
     * _more_
     *
     * @param repository _more_
     */
    public RepositoryManager(Repository repository) {
        this.repository = repository;
        this.repository.addRepositoryManager(this);
    }


    public void shutdown() throws Exception  {
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public Repository getRepository() {
        return repository;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public RepositoryBase getRepositoryBase() {
        return repository;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param label _more_
     * @param contents _more_
     *
     * @return _more_
     */
    public String formEntry(Request request, String label, String contents) {
        if (request.isMobile()) {
            return "<tr><td><div class=\"formlabel\">" + label + "</div>"
                   + contents + "</td></tr>";
        } else {
            return "<tr><td><div class=\"formlabel\">" + label
                   + "</div></td><td>" + contents + "</td></tr>";
            //            return HtmlUtil.formEntry(label, contents);
        }
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param label _more_
     * @param contents _more_
     *
     * @return _more_
     */
    public static String formEntryTop(Request request, String label,
                               String contents) {
        if (request.isMobile()) {
            return "<tr><td><div class=\"formlabel\">" + label + "</div>"
                   + contents + "</td></tr>";
        } else {
            return "<tr valign=top><td><div class=\"formlabel\">" + label
                   + "</div></td><td>" + contents + "</td></tr>";
            //            return HtmlUtil.formEntryTop(label, contents);
        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getActive() {
        if ((repository == null) || !repository.getActive()) {
            return false;
        }
        return true;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param result _more_
     *
     * @return _more_
     */
    public Result addHeaderToAncillaryPage(Request request, Result result) {
        return result;
        //        return getEntryManager().addEntryHeader(request, null, result);

    }

    /**
     * _more_
     *
     * @param request _more_
     * @param message _more_
     */
    public void fatalError(Request request, String message) {
        throw new IllegalArgumentException(message);
    }



    /**
     * _more_
     *
     * @param bytes _more_
     *
     * @return _more_
     */
    public static String formatFileLength(double bytes) {
        if (bytes < 5000) {
            return ((int) bytes) + " bytes";
        }
        if (bytes < 1000000) {
            bytes = ((int) ((bytes * 100) / 1000.0)) / 100.0;
            return ((int) bytes) + " KB";
        }
        bytes = ((int) ((bytes * 100) / 1000000.0)) / 100.0;
        return bytes + " MB";
    }

    /**
     * _more_
     *
     * @param url _more_
     *
     * @return _more_
     */
    public String fileUrl(String url) {
        return getRepository().fileUrl(url);
    }

    /**
     * _more_
     *
     * @param url _more_
     *
     * @return _more_
     */
    public String iconUrl(String url) {
        return getRepository().iconUrl(url);
    }




    /**
     * _more_
     *
     *
     * @param request _more_
     * @param sb _more_
     * @param label _more_
     * @param value _more_
     */
    public void addCriteria(Request request, StringBuffer sb, String label,
                            Object value) {
        String sv;
        if (value instanceof Date) {
            Date dttm = (Date) value;
            sv = formatDate(request, dttm);
        } else {
            sv = value.toString();
        }
        sv = sv.replace("<", "&lt;");
        sv = sv.replace(">", "&gt;");
        sb.append("<tr valign=\"top\"><td align=right>");
        sb.append(HtmlUtil.b(label));
        sb.append("</td><td>");
        sb.append(sv);
        sb.append("</td></tr>");
    }

    /**
     * _more_
     *
     * @param msg _more_
     *
     * @return _more_
     */
    public static String msg(String msg) {
        return Repository.msg(msg);
    }

    /**
     * _more_
     *
     * @param msg _more_
     *
     * @return _more_
     */
    public static String boldMsg(String msg) {
        return HtmlUtil.b(msg(msg));
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param s _more_
     *
     * @return _more_
     */
    public String translateMsg(Request request, String s) {
        return getRepository().translate(request, msg(s));
    }


    /**
     * _more_
     *
     * @param msg _more_
     *
     * @return _more_
     */
    public static String msgLabel(String msg) {
        return Repository.msgLabel(msg);
    }

    /**
     * _more_
     *
     * @param h _more_
     *
     * @return _more_
     */
    public static String msgHeader(String h) {
        return Repository.msgHeader(h);
    }


    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    public static String tableSubHeader(String s) {
        return HtmlUtil.row(HtmlUtil.colspan(subHeader(s), 2));
    }

    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    public static String subHeader(String s) {
        return HtmlUtil.div(s, HtmlUtil.cssClass(CSS_CLASS_HEADING_2));
    }


    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    public static String formHeader(String s) {
        return HtmlUtil.div(s, HtmlUtil.cssClass("formgroupheader"));
    }


    /**
     * _more_
     *
     * @param url _more_
     * @param label _more_
     *
     * @return _more_
     */
    public String subHeaderLink(String url, String label) {
        return HtmlUtil.href(url, label,
                             HtmlUtil.cssClass(CSS_CLASS_HEADING_2_LINK));
    }


    /**
     * _more_
     *
     * @param url _more_
     * @param label _more_
     * @param toggle _more_
     *
     * @return _more_
     */
    public String subHeaderLink(String url, String label, boolean toggle) {
        //        if(true) return "x";
        String img = HtmlUtil.img(iconUrl(toggle
                                          ? ICON_MINUS
                                          : ICON_PLUS));
        label = img + HtmlUtil.space(1) + label;
        String html =
            HtmlUtil.href(url, label,
                          HtmlUtil.cssClass(CSS_CLASS_HEADING_2_LINK));
        return html;
        //return "<table border=1><tr valign=bottom><td>" + html +"</table>";
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param d _more_
     *
     * @return _more_
     */
    public String formatDate(Request request, Date d) {
        return getRepository().formatDate(request, d);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param d _more_
     * @param entry _more_
     *
     * @return _more_
     */
    public String formatDate(Request request, Date d, Entry entry) {
        return getRepository().formatDate(
            request, d, getEntryManager().getTimezone(entry));
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param ms _more_
     *
     * @return _more_
     */
    public String formatDate(Request request, long ms) {
        return getRepository().formatDate(request, ms);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param ms _more_
     * @param entry _more_
     *
     * @return _more_
     */
    public String formatDate(Request request, long ms, Entry entry) {
        return getRepository().formatDate(
            request, ms, getEntryManager().getTimezone(entry));
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public DatabaseManager getDatabaseManager() {
        return repository.getDatabaseManager();
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public RegistryManager getRegistryManager() {
        return repository.getRegistryManager();
    }


    /**
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public String getProperty(String name, String dflt) {
        return repository.getProperty(name, dflt);
    }

    /**
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public boolean getProperty(String name, boolean dflt) {
        return repository.getProperty(name, dflt);
    }


    /**
     * _more_
     *
     * @param h _more_
     *
     * @return _more_
     */
    public String header(String h) {
        return RepositoryUtil.header(h);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Admin getAdmin() {
        return repository.getAdmin();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public UserManager getUserManager() {
        return repository.getUserManager();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public SessionManager getSessionManager() {
        return repository.getSessionManager();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public LogManager getLogManager() {
        return repository.getLogManager();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public ActionManager getActionManager() {
        return repository.getActionManager();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public AccessManager getAccessManager() {
        return repository.getAccessManager();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public EntryManager getEntryManager() {
        return repository.getEntryManager();
    }

    public PageHandler getPageHandler() {
        return repository.getPageHandler();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public SearchManager getSearchManager() {
        return repository.getSearchManager();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public AssociationManager getAssociationManager() {
        return repository.getAssociationManager();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public MetadataManager getMetadataManager() {
        return repository.getMetadataManager();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public WikiManager getWikiManager() {
        return repository.getWikiManager();
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public HarvesterManager getHarvesterManager() {
        return repository.getHarvesterManager();
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public StorageManager getStorageManager() {
        return repository.getStorageManager();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public MapManager getMapManager() {
        return repository.getMapManager();
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param message _more_
     */
    protected void log(Request request, String message) {
        getRepository().getLogManager().log(request, message);

    }



    /**
     * _more_
     *
     * @param message _more_
     * @param exc _more_
     */
    public void logException(String message, Exception exc) {
        getRepository().getLogManager().logError(message, exc);
    }


    /**
     * _more_
     *
     * @param message _more_
     * @param exc _more_
     */
    public void logError(String message, Throwable exc) {
        getRepository().getLogManager().logError(message, exc);
    }


    /**
     * _more_
     *
     * @param message _more_
     */
    public void logInfo(String message) {
        getRepository().getLogManager().logInfo(message);
    }

    public void adminSettingsChanged() {
    }


    private static int dialogCnt=0;
    public String makeFormSubmitDialog(StringBuffer sb, String message) {
        String id = "dialog-message" + (dialogCnt++);
        String onSubmit = " onsubmit=\"return submitEntryForm('#" + id +"');\" ";
        String loadingImage = HtmlUtil.img(getRepository().iconUrl(ICON_PROGRESS));
        sb.append("<div style=\"display:none;\" id=\"" + id +"\">" + loadingImage +" " + message +"</div>");
        return onSubmit;

    }


    public String makeButtonSubmitDialog(StringBuffer sb, String message) {
        String id = "dialog-message" + (dialogCnt++);
        String onSubmit = " onclick=\"return submitEntryForm('#" + id +"');\" ";
        String loadingImage = HtmlUtil.img(getRepository().iconUrl(ICON_PROGRESS));
        sb.append("<div style=\"display:none;\" id=\"" + id +"\">" + loadingImage +" " + message +"</div>");
        return onSubmit;

    }


}
