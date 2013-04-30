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

package org.ramadda.repository;


import org.ramadda.repository.auth.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;
import org.ramadda.repository.util.*;


import org.ramadda.util.HtmlTemplate;


import org.ramadda.util.HtmlUtils;
import org.ramadda.util.PropertyProvider;

import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;

import java.io.*;
import java.io.File;
import java.io.InputStream;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;



/**
 * The main class.
 *
 */
public class PageHandler extends RepositoryManager {

    /** _more_          */
    public static final String DEFAULT_TEMPLATE = "aodnStyle";



    /** _more_ */
    public static final String DEFAULT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss z";

    /** _more_ */
    public static final String DEFAULT_TIME_SHORTFORMAT =
        "yyyy/MM/dd HH:mm z";

    /** _more_ */
    public static final String DEFAULT_TIME_THISYEARFORMAT =
        "yyyy/MM/dd HH:mm z";


    /** _more_ */
    public static final TimeZone TIMEZONE_UTC = TimeZone.getTimeZone("UTC");

    /** _more_          */
    public static final TimeZone TIMEZONE_GMT = TimeZone.getTimeZone("GMT");



    /** _more_ */
    public static final GregorianCalendar calendar =
        new GregorianCalendar(TIMEZONE_UTC);



    /** _more_ */
    protected SimpleDateFormat sdf;

    /** _more_ */
    protected SimpleDateFormat displaySdf;

    /** _more_ */
    protected SimpleDateFormat thisYearSdf;


    /** _more_ */
    protected SimpleDateFormat dateSdf =
        RepositoryUtil.makeDateFormat("yyyy-MM-dd");

    /** _more_ */
    protected SimpleDateFormat timeSdf =
        RepositoryUtil.makeDateFormat("HH:mm:ss z");


    /** _more_ */
    private static final org.ramadda.util.HttpFormField dummyFieldToForceCompile =
        null;

    /** _more_ */
    public static final String PROP_LANGUAGE_DEFAULT =
        "ramadda.language.default";


    /** _more_ */
    public static final String MSG_PREFIX = "<msg ";

    /** _more_ */
    public static final String MSG_SUFFIX = " msg>";


    /** html template macro */
    public static final String MACRO_LINKS = "links";

    /** html template macro */
    public static final String MACRO_LOGO_URL = "logo.url";

    /** html template macro */
    public static final String MACRO_LOGO_IMAGE = "logo.image";

    /** html template macro */
    public static final String MACRO_SEARCH_URL = "search.url";

    /** html template macro */
    public static final String MACRO_ENTRY_HEADER = "entry.header";

    /** html template macro */
    public static final String MACRO_HEADER = "header";

    /** html template macro */
    public static final String MACRO_ENTRY_FOOTER = "entry.footer";

    /** html template macro */
    public static final String MACRO_ENTRY_BREADCRUMBS = "entry.breadcrumbs";

    /** html template macro */
    public static final String MACRO_HEADER_IMAGE = "header.image";

    /** html template macro */
    public static final String MACRO_HEADER_TITLE = "header.title";

    /** html template macro */
    public static final String MACRO_USERLINK = "userlinks";

    /** _more_          */
    public static final String MACRO_ALLLINKS = "alllinks";

    /** html template macro */
    public static final String MACRO_FAVORITES = "favorites";


    /** html template macro */
    public static final String MACRO_REPOSITORY_NAME = "repository_name";

    /** html template macro */
    public static final String MACRO_FOOTER = "footer";

    /** html template macro */
    public static final String MACRO_TITLE = "title";

    /** html template macro */
    public static final String MACRO_ROOT = "root";

    /** html template macro */
    public static final String MACRO_HEADFINAL = "headfinal";

    /** html template macro */
    public static final String MACRO_BOTTOM = "bottom";

    /** html template macro */
    public static final String MACRO_CONTENT = "content";


    /** _more_ */
    private List<HtmlTemplate> htmlTemplates;

    /** _more_ */
    private HtmlTemplate mobileTemplate;

    /** _more_ */
    private HtmlTemplate defaultTemplate;


    /** _more_ */
    private Properties phraseMap;


    /** _more_ */
    private Hashtable<String, Properties> languageMap = new Hashtable<String,
                                                            Properties>();

    /** _more_ */
    private List<TwoFacedObject> languages = new ArrayList<TwoFacedObject>();





    /** _more_ */
    private HashSet<String> seenMsg = new HashSet<String>();

    /**
     * Set this to true to print to a file the missing messages and this also
     *   adds a "NA:" to the missing labels.
     */
    private boolean debugMsg = false;

    /** _more_ */
    private PrintWriter allMsgOutput;

    /** _more_ */
    private PrintWriter missingMsgOutput;

    /**
     * _more_
     *
     * @param repository _more_
     */
    public PageHandler(Repository repository) {
        super(repository);
    }


    /**
     * _more_
     */
    public void adminSettingsChanged() {
        super.adminSettingsChanged();
        phraseMap = null;
    }

    /**
     * _more_
     *
     *
     * @param request The request
     * @param result _more_
     *
     * @throws Exception _more_
     */
    public void decorateResult(Request request, Result result)
            throws Exception {
        if ( !request.get(ARG_DECORATE, true)) {
            return;
        }
        Repository repository   = getRepository();
        Entry      currentEntry = getSessionManager().getLastEntry(request);
        String       template = null;
        HtmlTemplate htmlTemplate;
        if (request.isMobile()) {
            htmlTemplate = getMobileTemplate();
        } else {
            htmlTemplate = getTemplate(request, currentEntry);
        }
        template = htmlTemplate.getTemplate();

        String systemMessage = getRepository().getSystemMessage(request);


        String jsContent     = getTemplateJavascriptContent();

        List   links         = (List) result.getProperty(PROP_NAVLINKS);
        String linksHtml     = HtmlUtils.space(1);

        if (links != null) {
            linksHtml = StringUtil.join(
                htmlTemplate.getTemplateProperty(
                    "ramadda.template.link.separator", ""), links);
        }
        String entryHeader = (String) result.getProperty(PROP_ENTRY_HEADER);
        if (entryHeader == null) {
            entryHeader = "";
        }
        String entryFooter = (String) result.getProperty(PROP_ENTRY_FOOTER);
        if (entryFooter == null) {
            entryFooter = "";
        }


        String entryBreadcrumbs =
            (String) result.getProperty(PROP_ENTRY_BREADCRUMBS);
        if (entryBreadcrumbs == null) {
            entryBreadcrumbs = "";
        }

        String header = "";
        if (entryHeader.length() > 0) {
            header = entryHeader;
        }

        String favoritesWrapper = htmlTemplate.getTemplateProperty(
                                      "ramadda.template.favorites.wrapper",
                                      "${link}");
        String favoritesTemplate =
            htmlTemplate.getTemplateProperty(
                "ramadda.template.favorites",
                "<span class=\"linkslabel\">Favorites:</span>${entries}");
        String favoritesSeparator =
            htmlTemplate.getTemplateProperty(
                "ramadda.template.favorites.separator", "");

        List<FavoriteEntry> favoritesList =
            getUserManager().getFavorites(request, request.getUser());
        StringBuffer favorites = new StringBuffer();
        if (favoritesList.size() > 0) {
            List favoriteLinks = new ArrayList();
            int  favoriteCnt   = 0;
            for (FavoriteEntry favorite : favoritesList) {
                if (favoriteCnt++ > 100) {
                    break;
                }
                Entry     entry     = favorite.getEntry();
                EntryLink entryLink = getEntryManager().getAjaxLink(request,
                                          entry, entry.getLabel(), null,
                                          false, null, false);
                String link = favoritesWrapper.replace("${link}",
                                  entryLink.toString());
                favoriteLinks.add("<nobr>" + link + "</nobr>");
            }
            favorites.append(favoritesTemplate.replace("${entries}",
                    StringUtil.join(favoritesSeparator, favoriteLinks)));
        }

        List<Entry> cartEntries = getUserManager().getCart(request);
        if (cartEntries.size() > 0) {
            String cartTemplate =
                htmlTemplate.getTemplateProperty("ramadda.template.cart",
                    "<b>Cart:<b><br>${entries}");
            List cartLinks = new ArrayList();
            for (Entry entry : cartEntries) {
                EntryLink entryLink = getEntryManager().getAjaxLink(request,
                                          entry, entry.getLabel(), null,
                                          false);
                String link = favoritesWrapper.replace("${link}",
                                  entryLink.toString());
                cartLinks.add("<nobr>" + link + "<nobr>");
            }
            favorites.append(HtmlUtils.br());
            favorites.append(cartTemplate.replace("${entries}",
                    StringUtil.join(favoritesSeparator, cartLinks)));
        }

        String content = new String(result.getContent());
        if ((systemMessage != null) && (systemMessage.length() > 0)) {
            content = HtmlUtils.div(
                systemMessage,
                HtmlUtils.cssClass("ramadda-system-message")) + content;
        }

        String head =
            "<script type=\"text/javascript\" src=\"${root}/shadowbox/adapter/shadowbox-base.js\"></script>\n<script type=\"text/javascript\" src=\"${root}/shadowbox/shadowbox.js\"></script>\n<script type=\"text/javascript\">\nShadowbox.loadSkin('classic', '${root}/shadowbox/skin'); \nShadowbox.loadLanguage('en', '${root}/shadowbox/lang');\nShadowbox.loadPlayer(['img', 'qt'], '${root}/shadowbox/player'); \nwindow.onload = Shadowbox.init;\n</script>";

        //Skip the shadowbox for now
        head = (String) result.getProperty(PROP_HTML_HEAD);
        if (head == null) {
            head = (String) request.getExtraProperty(PROP_HTML_HEAD);
        }


        if (head == null) {
            head = "";
        }
        String logoImage = repository.getLogoImage(result);
        String logoUrl   = (String) result.getProperty(PROP_LOGO_URL);
        if ((logoUrl == null) || (logoUrl.trim().length() == 0)) {
            logoUrl = getProperty(PROP_LOGO_URL, "");
        }
        String pageTitle = (String) result.getProperty(PROP_REPOSITORY_NAME);
        if (pageTitle == null) {
            pageTitle = repository.getProperty(PROP_REPOSITORY_NAME,
                    "Repository");
        }

        for (PageDecorator pageDecorator :
                repository.getPluginManager().getPageDecorators()) {
            String tmpTemplate = pageDecorator.decoratePage(repository, request,
                    template, currentEntry);
            if(tmpTemplate!=null) {
                template = tmpTemplate;
            }
        }
        String   html   = template;
        String[] macros = new String[] {
            MACRO_LOGO_URL, logoUrl, MACRO_LOGO_IMAGE, logoImage,
            MACRO_HEADER_IMAGE, iconUrl(ICON_HEADER), MACRO_HEADER_TITLE,
            pageTitle, MACRO_USERLINK,
            getUserManager().getUserLinks(request, htmlTemplate), MACRO_LINKS,
            linksHtml, MACRO_REPOSITORY_NAME,
            repository.getProperty(PROP_REPOSITORY_NAME, "Repository"),
            MACRO_FOOTER, repository.getProperty(PROP_HTML_FOOTER, BLANK),
            MACRO_TITLE, result.getTitle(), MACRO_BOTTOM,
            result.getBottomHtml(), MACRO_SEARCH_URL,
            getSearchManager().getSearchUrl(request), MACRO_CONTENT,
            content + jsContent, MACRO_FAVORITES, favorites.toString(),
            MACRO_ENTRY_HEADER, entryHeader, MACRO_HEADER, header,
            MACRO_ENTRY_FOOTER, entryFooter, MACRO_ENTRY_BREADCRUMBS,
            entryBreadcrumbs, MACRO_HEADFINAL, head, MACRO_ROOT,
            repository.getUrlBase(),
        };


        for (int i = 0; i < macros.length; i += 2) {
            html = html.replace("${" + macros[i] + "}", macros[i + 1]);
        }

        for (String property : htmlTemplate.getPropertyIds()) {
            html = html.replace("${" + property + "}",
                                getRepository().getProperty(property, ""));
        }


        //cleanup old macro
        html = StringUtil.replace(html, "${sublinks}", BLANK);

        html = translate(request, html);
        result.setContent(html.getBytes());

    }

    /**
     * _more_
     */
    public void clearTemplates() {
        htmlTemplates   = null;
        defaultTemplate = null;
    }




    /**
     * _more_
     *
     * @return _more_
     */
    public String getTemplateJavascriptContent() {
        return HtmlUtils.div(
            "",
            " id=\"tooltipdiv\" class=\"tooltip-outer\" ") + HtmlUtils.div(
                "",
                " id=\"popupdiv\" class=\"tooltip-outer\" ") + HtmlUtils.div(
                    "", " id=\"output\"") + HtmlUtils.div(
                    "",
                    " id=\"selectdiv\" class=\"selectdiv\" ") + HtmlUtils.div(
                        "", " id=\"floatdiv\" class=\"floatdiv\" ");
    }




    /**
     * _more_
     *
     * @param template _more_
     * @param ignoreErrors _more_
     *
     * @return _more_
     */
    public String processTemplate(String template, boolean ignoreErrors) {
        List<String> toks   = StringUtil.splitMacros(template);
        StringBuffer result = new StringBuffer();
        if (toks.size() > 0) {
            result.append(toks.get(0));
            for (int i = 1; i < toks.size(); i++) {
                if (2 * (i / 2) == i) {
                    result.append(toks.get(i));
                } else {
                    String prop = getRepository().getProperty(toks.get(i),
                                      (String) null);
                    if (prop == null) {
                        if (ignoreErrors) {
                            prop = "${" + toks.get(i) + "}";
                        } else {
                            throw new IllegalArgumentException(
                                "Could not find property:" + toks.get(i)
                                + ":");
                        }
                    }
                    if (prop.startsWith("bsf:")) {
                        prop = new String(
                            RepositoryUtil.decodeBase64(prop.substring(4)));
                    }
                    result.append(prop);
                }
            }
        }

        return result.toString();
    }




    /**
     * _more_
     *
     * @param request The request
     * @param s _more_
     *
     * @return _more_
     */
    public String translate(Request request, String s) {
        String     language = request.getLanguage();
        Properties tmpMap;
        Properties map =
            (Properties) languageMap.get(getProperty(PROP_LANGUAGE_DEFAULT,
                "default"));
        if (map == null) {
            map = new Properties();
        }
        tmpMap = (Properties) languageMap.get(getProperty(PROP_LANGUAGE,
                BLANK));
        if (tmpMap != null) {
            map.putAll(tmpMap);
        }
        tmpMap = (Properties) languageMap.get(language);

        if (tmpMap != null) {
            map.putAll(tmpMap);
        }

        Properties tmpPhraseMap = phraseMap;
        if (tmpPhraseMap == null) {
            String phrases = getProperty(PROP_ADMIN_PHRASES, (String) null);
            if (phrases != null) {
                Object[] result = parsePhrases("", phrases);
                tmpPhraseMap = (Properties) result[2];
                phraseMap    = tmpPhraseMap;
            }
        }

        if (tmpPhraseMap != null) {
            map.putAll(tmpPhraseMap);
        }


        StringBuffer stripped     = new StringBuffer();
        int          prefixLength = MSG_PREFIX.length();
        int          suffixLength = MSG_PREFIX.length();
        //        System.out.println(s);
        while (s.length() > 0) {
            String tmp  = s;
            int    idx1 = s.indexOf(MSG_PREFIX);
            if (idx1 < 0) {
                stripped.append(s);

                break;
            }
            String text = s.substring(0, idx1);
            if (text.length() > 0) {
                stripped.append(text);
            }
            s = s.substring(idx1 + 1);

            int idx2 = s.indexOf(MSG_SUFFIX);
            if (idx2 < 0) {
                //Should never happen
                throw new IllegalArgumentException(
                    "No closing message suffix:" + s);
            }
            String key   = s.substring(prefixLength - 1, idx2);
            String value = null;
            if (map != null) {
                value = (String) map.get(key);
            }
            if (debugMsg) {
                try {
                    if (allMsgOutput == null) {
                        allMsgOutput = new PrintWriter(
                            new FileOutputStream("allmessages.pack"));
                        missingMsgOutput = new PrintWriter(
                            new FileOutputStream("missingmessages.pack"));
                    }
                    if ( !seenMsg.contains(key)) {
                        allMsgOutput.println(key + "=");
                        allMsgOutput.flush();
                        System.err.println(key);
                        if (value == null) {
                            missingMsgOutput.println(key + "=");
                            missingMsgOutput.flush();
                        }
                        seenMsg.add(key);
                    }
                } catch (Exception exc) {
                    throw new RuntimeException(exc);
                }
            }


            if (value == null) {
                value = key;
                if (debugMsg) {
                    value = "NA:" + key;
                }
            }
            stripped.append(value);
            s = s.substring(idx2 + suffixLength);
        }

        return stripped.toString();
    }

    /**
     * _more_
     *
     *
     * @param file _more_
     * @param content _more_
     *
     * @return _more_
     */
    private Object[] parsePhrases(String file, String content) {
        List<String> lines   = StringUtil.split(content, "\n", true, true);
        Properties   phrases = new Properties();
        String       type    = IOUtil.stripExtension(IOUtil.getFileTail(file));
        String       name    = type;
        for (String line : lines) {
            if (line.startsWith("#")) {
                continue;
            }
            List<String> toks = StringUtil.split(line, "=", true, true);
            if (toks.size() == 0) {
                continue;
            }
            String key = toks.get(0).trim();
            String value;
            if (toks.size() == 1) {
                if ( !debugMsg) {
                    continue;
                }
                value = "UNDEF:" + key;
            } else {
                value = toks.get(1).trim();
            }
            if (key.equals("language.id")) {
                type = value;
            } else if (key.equals("language.name")) {
                name = value;
            } else {
                if (value.length() == 0) {
                    if (debugMsg) {
                        value = "UNDEF:" + value;
                    } else {
                        continue;
                    }
                }


                phrases.put(key, value);
            }
        }

        return new Object[] { type, name, phrases };
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public List<TwoFacedObject> getLanguages() {
        return languages;
    }




    /**
     * _more_
     *
     * @param request The request
     *
     * @return _more_
     */
    public HtmlTemplate getHtmlTemplate(Request request) {
        return null;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public HtmlTemplate getMobileTemplate() {
        if (mobileTemplate == null) {
            for (HtmlTemplate htmlTemplate : getTemplates()) {
                if (htmlTemplate.getId().equals("mobile")) {
                    //xxx
                    if (true) {
                        return htmlTemplate;
                    }
                    mobileTemplate = htmlTemplate;

                    break;
                }
            }
        }

        return mobileTemplate;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public List<HtmlTemplate> getTemplates() {
        List<HtmlTemplate> theTemplates = htmlTemplates;
        if (theTemplates == null) {
            //            System.err.println ("Loading templates");
            String imports = "";
            try {
                imports = getStorageManager().readSystemResource(
                    "/org/ramadda/repository/resources/imports.html");
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }
            imports = imports.replace("${root}",
                                      getRepository().getUrlBase());
            theTemplates = new ArrayList<HtmlTemplate>();

            String defaultId = getProperty(PROP_HTML_TEMPLATE_DEFAULT,
                                           DEFAULT_TEMPLATE);

            List<String> templatePaths =
                new ArrayList<String>(
                    getRepository().getPluginManager().getTemplateFiles());
            for (String path :
                    StringUtil.split(getProperty(PROP_HTML_TEMPLATES,
                        "%resourcedir%/template.html"), ";", true, true)) {
                path = getStorageManager().localizePath(path);
                templatePaths.add(path);
            }
            for (String path : templatePaths) {
                try {
                    //Skip resources called template.html that might be for other things
                    if (IOUtil.getFileTail(path).equals("template.html")) {
                        continue;
                    }
                    String resource =
                        getStorageManager().readSystemResource(path);
                    try {
                        resource = processTemplate(resource);
                    } catch (Exception exc) {
                        getLogManager().logError(
                            "failed to process template:" + path, exc);

                        continue;
                    }
                    String[] changes = { "userlink", MACRO_USERLINK,
                                         "html.imports", "imports", };
                    for (int i = 0; i < changes.length; i += 2) {
                        resource = resource.replace("${" + changes[i] + "}",
                                "${" + changes[i + 1] + "}");
                    }

                    resource = resource.replace("${imports}", imports);
                    HtmlTemplate template = new HtmlTemplate(getRepository(),
                                                path, resource);
                    //Check if we got some other ...template.html file from a plugin
                    if (template.getId() == null) {
                        continue;
                    }
                    theTemplates.add(template);

                    if (defaultTemplate == null) {
                        if (defaultId == null) {
                            defaultTemplate = template;
                        } else {
                            if (Misc.equals(defaultId, template.getId())) {
                                defaultTemplate = template;
                            }
                        }
                    }
                } catch (Exception exc) {
                    //noop
                }
            }
            if (getRepository().cacheResources()) {
                htmlTemplates = theTemplates;
            }
        }

        return theTemplates;
    }


    /**
     * _more_
     *
     * @param html _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String processTemplate(String html) throws Exception {
        StringBuffer template = new StringBuffer();
        while (true) {
            int idx1 = html.indexOf("<include");
            if (idx1 < 0) {
                template.append(html);

                break;
            }
            template.append(html.substring(0, idx1));
            html = html.substring(idx1);
            idx1 = html.indexOf(">") + 1;
            String include = html.substring(0, idx1);
            include = include.substring("<include".length());
            include = include.replace(">", "");
            Hashtable props = StringUtil.parseHtmlProperties(include);
            String    url   = (String) props.get("href");
            if (url != null) {
                String includedContent =
                    getStorageManager().readSystemResource(new URL(url));
                //                String includedContent =  IOUtil.readContents(url, Repository.class);
                template.append(includedContent);
            }
            html = html.substring(idx1);
        }
        html = template.toString();
        if (html.indexOf("${imports}") < 0) {
            html = html.replace("<head>", "<head>\n${imports}");
        }
        if (html.indexOf("${headfinal}") < 0) {
            html = html.replace("</head>", "${headfinal}\n</head>");
        }

        return html;
    }



    /** _more_ */
    private HashSet<String> seenPack = new HashSet<String>();

    /**
     * _more_
     *
     * @throws Exception _more_
     */
    protected void loadLanguagePacks() throws Exception {
        //        getLogManager().logInfoAndPrint("RAMADDA: loadLanguagePacks");
        List sourcePaths =
            Misc.newList(
                getStorageManager().getSystemResourcePath() + "/languages",
                getStorageManager().getPluginsDir().toString());
        for (int i = 0; i < sourcePaths.size(); i++) {
            String       dir     = (String) sourcePaths.get(i);
            List<String> listing = getRepository().getListing(dir,
                                       getClass());
            for (String path : listing) {
                if ( !path.endsWith(".pack")) {
                    if (i == 0) {
                        getLogManager().logInfoAndPrint(
                            "RAMADDA: not ends with .pack:" + path);
                    }

                    continue;
                }
                if (seenPack.contains(path)) {
                    continue;
                }
                seenPack.add(path);
                String content =
                    getStorageManager().readUncheckedSystemResource(path,
                        (String) null);
                if (content == null) {
                    getLogManager().logInfoAndPrint(
                        "RAMADDA: could not read:" + path);

                    continue;
                }
                Object[]   result     = parsePhrases(path, content);
                String     type       = (String) result[0];
                String     name       = (String) result[1];
                Properties properties = (Properties) result[2];
                if (type != null) {
                    if (name == null) {
                        name = type;
                    }
                    languages.add(new TwoFacedObject(name, type));
                    languageMap.put(type, properties);
                } else {
                    getLogManager().logError("No _type_ found in: " + path);
                }
            }
        }
    }



    /**
     * _more_
     *
     * @param request The request
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public String getTemplateProperty(Request request, String name,
                                      String dflt) {
        return getTemplate(request).getTemplateProperty(name, dflt);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public List<TwoFacedObject> getTemplateSelectList() {
        List<TwoFacedObject> tfos = new ArrayList<TwoFacedObject>();
        tfos.add(new TwoFacedObject("-default-", ""));
        for (HtmlTemplate template : getTemplates()) {
            tfos.add(new TwoFacedObject(template.getName(),
                                        template.getId()));
        }

        return tfos;

    }


    public HtmlTemplate getTemplate(Request request) {
        Entry      currentEntry = null;
        if(request!=null) {
            try {
                currentEntry = getSessionManager().getLastEntry(request);
            }catch(Exception exc) {
                throw new RuntimeException(exc);
            }
        }
        return getTemplate(request, currentEntry);
    }


    /**
     * Find the html template for the given request
     *
     * @param request The request
     *
     * @return _more_
     */
    public HtmlTemplate getTemplate(Request request, Entry entry) {
        if (request.isMobile()) {
            return getMobileTemplate();
        }


        List<HtmlTemplate> theTemplates = getTemplates();
        if ((request == null) && (defaultTemplate != null)) {
            return defaultTemplate;
        }

        String templateId = request.getHtmlTemplateId();
        if((templateId==null || templateId.length()==0)  && entry!=null) {
            try {
                List<Metadata> metadataList =
                    getMetadataManager().findMetadata(entry,
                                                      ContentMetadataHandler.TYPE_TEMPLATE, true);
                if(metadataList!=null) {
                    for(Metadata metadata: metadataList) {
                        templateId = metadata.getAttr1();
                        request.put(ARG_TEMPLATE, templateId);
                        break;
                    }
                }
            } catch(Exception exc) {
                throw new RuntimeException(exc);
            }
        }


        User   user       = request.getUser();

        if ((templateId == null) && user.getAnonymous()) {
            templateId = user.getTemplate();
        }

        if (templateId != null) {
            for (HtmlTemplate template : theTemplates) {
                if (Misc.equals(template.getId(), templateId)) {
                    return template;
                }
            }
        }

        if (user.getAnonymous()) {
            if (defaultTemplate != null) {
                return defaultTemplate;
            }
            return theTemplates.get(0);
        }

        for (HtmlTemplate template : theTemplates) {
            if (request == null) {
                return template;
            }
            if (template.isTemplateFor(request)) {
                return template;
            }
        }
        if (defaultTemplate != null) {
            return defaultTemplate;
        }

        return theTemplates.get(0);
    }


    /**
     * _more_
     *
     * @param msg _more_
     *
     * @return _more_
     */
    public static String msg(String msg) {
        if (msg == null) {
            return null;
        }
        if (msg.indexOf(MSG_PREFIX) >= 0) {
            //            System.err.println("bad msg:" + msg+"\n" + LogUtil.getStackTrace());
            //            throw new IllegalArgumentException("bad msg:" + msg);
            return msg;

        }

        return MSG_PREFIX + msg + MSG_SUFFIX;
    }

    /**
     * _more_
     *
     * @param msg _more_
     *
     * @return _more_
     */
    public static String msgLabel(String msg) {
        if (msg == null) {
            return null;
        }
        if (msg.length() == 0) {
            return msg;
        }
        msg = msg(msg);

        return msg(msg) + ":" + HtmlUtils.space(1);
    }

    /**
     * _more_
     *
     * @param h _more_
     *
     * @return _more_
     */
    public static String msgHeader(String h) {
        return HtmlUtils.div(msg(h), HtmlUtils.cssClass(CSS_CLASS_HEADING_1));
    }



    /**
     * _more_
     *
     * @param formName _more_
     * @param fieldName _more_
     *
     * @return _more_
     */
    public String getCalendarSelector(String formName, String fieldName) {
        String anchorName = "anchor." + fieldName;
        String divName    = "div." + fieldName;
        String call =
            HtmlUtils.call("selectDate",
                           HtmlUtils.comma(HtmlUtils.squote(divName),
        //                              "document.forms['"  + formName + "']." + fieldName, 
        "findFormElement('" + formName + "','" + fieldName
                            + "')", HtmlUtils.squote(anchorName),
                                    HtmlUtils.squote(
                                        "yyyy-MM-dd"))) + "return false;";

        return HtmlUtils
            .href("#", HtmlUtils
                .img(iconUrl(ICON_CALENDAR), " Choose date", HtmlUtils
                    .attr(HtmlUtils.ATTR_BORDER, "0")), HtmlUtils
                        .onMouseClick(call) + HtmlUtils
                        .attrs(HtmlUtils.ATTR_NAME, anchorName, HtmlUtils
                            .ATTR_ID, anchorName)) + HtmlUtils
                                .div("", HtmlUtils
                                    .attrs(HtmlUtils
                                        .ATTR_ID, divName, HtmlUtils
                                        .ATTR_STYLE, "position:absolute;visibility:hidden;background-color:white;layer-background-color:white;"));
    }



    /**
     * _more_
     *
     * @param request The request
     * @param name _more_
     * @param formName _more_
     * @param date _more_
     *
     * @return _more_
     */
    public String makeDateInput(Request request, String name,
                                String formName, Date date) {
        return makeDateInput(request, name, formName, date, null);
    }

    /**
     * _more_
     *
     * @param request The request
     * @param name _more_
     * @param formName _more_
     * @param date _more_
     * @param timezone _more_
     *
     * @return _more_
     */
    public String makeDateInput(Request request, String name,
                                String formName, Date date, String timezone) {
        return makeDateInput(request, name, formName, date, timezone, true);
    }

    /**
     * Make the HTML for a date input widget
     *
     * @param request The request
     * @param name    the name
     * @param formName  the form name
     * @param date      the default date
     * @param timezone  the timezone
     * @param includeTime  true to include a time box
     *
     * @return  the widget html
     */
    public String makeDateInput(Request request, String name,
                                String formName, Date date, String timezone,
                                boolean includeTime) {
        String dateHelp = "e.g., yyyy-mm-dd,  now, -1 week, +3 days, etc.";
        String           timeHelp   = "hh:mm:ss Z, e.g. 20:15:00 MST";

        String           dateArg    = request.getString(name, "");
        String           timeArg    = request.getString(name + ".time", "");
        String           dateString = ((date == null)
                                       ? dateArg
                                       : dateSdf.format(date));
        SimpleDateFormat timeFormat = ((timezone == null)
                                       ? timeSdf
                                       : getSDF("HH:mm:ss z", timezone));
        String           timeString = ((date == null)
                                       ? timeArg
                                       : timeFormat.format(date));

        String           inputId    = "dateinput" + (HtmlUtils.blockCnt++);


        String js =
            "<script>jQuery(function() {$( "
            + HtmlUtils.squote("#" + inputId)
            + " ).datepicker({ dateFormat: 'yy-mm-dd',changeMonth: true, changeYear: true,constrainInput:false, yearRange: '1900:2100' });});</script>";
        String extra = "";
        if (includeTime) {
            extra = " T:"
                    + HtmlUtils.input(name + ".time", timeString,
                                      HtmlUtils.sizeAttr(6)
                                      + HtmlUtils.attr(HtmlUtils.ATTR_TITLE,
                                          timeHelp));
        }

        return "\n" + js + "\n"
               + HtmlUtils.input(name, dateString,
                                 HtmlUtils.SIZE_10 + HtmlUtils.id(inputId)
                                 + HtmlUtils.title(dateHelp)) + extra;
    }



    /**
     * _more_
     *
     * @param link _more_
     * @param menuContents _more_
     *
     * @return _more_
     */
    public String makePopupLink(String link, String menuContents) {
        return makePopupLink(link, menuContents, false, false);
    }


    /**
     * _more_
     *
     * @param link _more_
     * @param menuContents _more_
     * @param makeClose _more_
     * @param alignLeft _more_
     *
     * @return _more_
     */
    public String makePopupLink(String link, String menuContents,
                                boolean makeClose, boolean alignLeft) {
        return makePopupLink(link, menuContents, "", makeClose, alignLeft);
    }


    /**
     * _more_
     *
     * @param link _more_
     * @param menuContents _more_
     * @param linkAttributes _more_
     *
     * @return _more_
     */
    public String makePopupLink(String link, String menuContents,
                                String linkAttributes) {
        return makePopupLink(link, menuContents, linkAttributes, false,
                             false);
    }

    /**
     * _more_
     *
     * @param link _more_
     * @param menuContents _more_
     * @param linkAttributes _more_
     * @param makeClose _more_
     * @param alignLeft _more_
     *
     * @return _more_
     */
    public String makePopupLink(String link, String menuContents,
                                String linkAttributes, boolean makeClose,
                                boolean alignLeft) {
        String compId   = "menu_" + HtmlUtils.blockCnt++;
        String linkId   = "menulink_" + HtmlUtils.blockCnt++;
        String contents = makePopupDiv(menuContents, compId, makeClose);
        String onClick = HtmlUtils.onMouseClick(HtmlUtils.call("showPopup",
                             HtmlUtils.comma(new String[] { "event",
                HtmlUtils.squote(linkId), HtmlUtils.squote(compId), (alignLeft
                ? "1"
                : "0") })));
        String href = HtmlUtils.href("javascript:noop();", link,
                                     onClick + HtmlUtils.id(linkId)
                                     + linkAttributes);

        return href + contents;
    }



    /**
     * _more_
     *
     * @param link _more_
     * @param innerContents _more_
     * @param initCall _more_
     *
     * @return _more_
     */
    public String makeStickyPopup(String link, String innerContents,
                                  String initCall) {
        boolean alignLeft = true;
        String  compId    = "menu_" + HtmlUtils.blockCnt++;
        String  linkId    = "menulink_" + HtmlUtils.blockCnt++;
        String  contents  = makeStickyPopupDiv(innerContents, compId);
        String onClick =
            HtmlUtils.onMouseClick(HtmlUtils.call("showStickyPopup",
                HtmlUtils.comma(new String[] { "event",
                HtmlUtils.squote(linkId), HtmlUtils.squote(compId), (alignLeft
                ? "1"
                : "0") })) + initCall);
        String href = HtmlUtils.href("javascript:noop();", link,
                                     onClick + HtmlUtils.id(linkId));

        return href + contents;
    }



    /**
     * _more_
     *
     * @param contents _more_
     * @param compId _more_
     *
     * @return _more_
     */
    public String makeStickyPopupDiv(String contents, String compId) {
        StringBuffer menu = new StringBuffer();
        String cLink = HtmlUtils.jsLink(
                           HtmlUtils.onMouseClick(
                               HtmlUtils.call(
                                   "hideElementById",
                                   HtmlUtils.squote(compId))), HtmlUtils.img(
                                       iconUrl(ICON_CLOSE)), "");
        contents = cLink + HtmlUtils.br() + contents;

        menu.append(HtmlUtils.div(contents,
                                  HtmlUtils.id(compId)
                                  + HtmlUtils.cssClass(CSS_CLASS_POPUP)));

        return menu.toString();
    }




    /**
     * _more_
     *
     * @param contents _more_
     * @param compId _more_
     * @param makeClose _more_
     *
     * @return _more_
     */
    public String makePopupDiv(String contents, String compId,
                               boolean makeClose) {
        StringBuffer menu = new StringBuffer();
        if (makeClose) {
            String cLink = HtmlUtils.jsLink(
                               HtmlUtils.onMouseClick("hidePopupObject();"),
                               HtmlUtils.img(iconUrl(ICON_CLOSE)), "");
            contents = cLink + HtmlUtils.br() + contents;
        }

        menu.append(HtmlUtils.div(contents,
                                  HtmlUtils.id(compId)
                                  + HtmlUtils.cssClass(CSS_CLASS_POPUP)));

        return menu.toString();
    }



    /**
     * _more_
     *
     * @param request The request
     * @param url _more_
     * @param okArg _more_
     * @param extra _more_
     *
     * @return _more_
     */
    public static String makeOkCancelForm(Request request, RequestUrl url,
                                          String okArg, String extra) {
        StringBuffer fb = new StringBuffer();
        fb.append(request.form(url));
        fb.append(extra);
        String okButton     = HtmlUtils.submit("OK", okArg);
        String cancelButton = HtmlUtils.submit("Cancel",
                                  Constants.ARG_CANCEL);
        String buttons = RepositoryUtil.buttons(okButton, cancelButton);
        fb.append(buttons);
        fb.append(HtmlUtils.formClose());

        return fb.toString();
    }


    /** _more_ */
    private Hashtable<String, SimpleDateFormat> dateFormats =
        new Hashtable<String, SimpleDateFormat>();

    /** _more_ */
    TimeZone defaultTimeZone;


    /** _more_ */
    protected List<SimpleDateFormat> formats;


    /**
     * _more_
     *
     * @param format _more_
     * @param timezone _more_
     *
     * @return _more_
     */
    protected SimpleDateFormat getSDF(String format, String timezone) {
        String key;
        if (timezone != null) {
            key = format + "-" + timezone;
        } else {
            key = format;
        }
        SimpleDateFormat sdf = dateFormats.get(key);
        if (sdf == null) {
            sdf = new SimpleDateFormat();
            sdf.applyPattern(format);
            if (timezone == null) {
                sdf.setTimeZone(TIMEZONE_UTC);
            } else {
                if ((defaultTimeZone != null)
                        && (timezone.equals("")
                            || timezone.equals("default"))) {
                    sdf.setTimeZone(defaultTimeZone);
                } else {
                    sdf.setTimeZone(TimeZone.getTimeZone(timezone));
                }
            }
            dateFormats.put(key, sdf);
        }

        return sdf;
    }


    /**
     * _more_
     *
     * @param format _more_
     *
     * @return _more_
     */
    public SimpleDateFormat makeSDF(String format) {
        return getSDF(format, null);
    }

    /**
     * _more_
     *
     * @param d _more_
     *
     * @return _more_
     */
    public String formatDate(Date d) {
        return formatDate(d, null);
    }

    /**
     * _more_
     *
     * @param d _more_
     * @param timezone _more_
     *
     * @return _more_
     */
    public String formatDate(Date d, String timezone) {
        if (sdf == null) {
            sdf = makeSDF(getProperty(PROP_DATE_FORMAT, DEFAULT_TIME_FORMAT));
        }
        SimpleDateFormat dateFormat = ((timezone == null)
                                       ? sdf
                                       : getSDF(getProperty(PROP_DATE_FORMAT,
                                           DEFAULT_TIME_FORMAT), timezone));
        if (d == null) {
            return BLANK;
        }
        synchronized (dateFormat) {
            return dateFormat.format(d);
        }
    }


    /**
     * _more_
     *
     * @param request The request
     * @param ms _more_
     *
     * @return _more_
     */
    public String formatDate(Request request, long ms) {
        return formatDate(new Date(ms));
    }


    /**
     * _more_
     *
     * @param request The request
     * @param ms _more_
     * @param timezone _more_
     *
     * @return _more_
     */
    public String formatDate(Request request, long ms, String timezone) {
        return formatDate(new Date(ms), timezone);
    }

    /**
     * _more_
     *
     * @param request The request
     * @param d _more_
     *
     * @return _more_
     */
    public String formatDate(Request request, Date d) {
        return formatDate(d);
    }


    /**
     * _more_
     *
     * @param request The request
     * @param d _more_
     * @param timezone _more_
     *
     * @return _more_
     */
    public String formatDate(Request request, Date d, String timezone) {
        return formatDate(d, timezone);
    }




    /**
     * _more_
     *
     * @param request The request
     * @param d _more_
     * @param timezone _more_
     *
     * @return _more_
     */
    public String formatDateShort(Request request, Date d, String timezone) {
        return formatDateShort(request, d, timezone, "");
    }

    /**
     * _more_
     *
     * @param request The request
     * @param d _more_
     * @param timezone _more_
     * @param extraAlt _more_
     *
     * @return _more_
     */
    public String formatDateShort(Request request, Date d, String timezone,
                                  String extraAlt) {
        SimpleDateFormat sdf = getSDF(getProperty(PROP_DATE_SHORTFORMAT,
                                   DEFAULT_TIME_SHORTFORMAT), timezone);
        if (d == null) {
            return BLANK;
        }

        Date   now      = new Date();
        long   diff     = now.getTime() - d.getTime();
        double minutes  = DateUtil.millisToMinutes(diff);
        String fullDate = formatDate(d, timezone);
        String result;
        if ((minutes > 0) && (minutes < 65) && (minutes > 55)) {
            result = "about an hour ago";
        } else if ((diff > 0) && (diff < DateUtil.minutesToMillis(1))) {
            result = (int) (diff / (1000)) + " seconds ago";
        } else if ((diff > 0) && (diff < DateUtil.hoursToMillis(1))) {
            int value = (int) DateUtil.millisToMinutes(diff);
            result = value + " minute" + ((value > 1)
                                          ? "s"
                                          : "") + " ago";
        } else if ((diff > 0) && (diff < DateUtil.hoursToMillis(24))) {
            int value = (int) (diff / (1000 * 60 * 60));
            result = value + " hour" + ((value > 1)
                                        ? "s"
                                        : "") + " ago";
        } else if ((diff > 0) && (diff < DateUtil.daysToMillis(6))) {
            int value = (int) (diff / (1000 * 60 * 60 * 24));
            result = value + " day" + ((value > 1)
                                       ? "s"
                                       : "") + " ago";
        } else {
            result = sdf.format(d);
        }

        return HtmlUtils.span(result,
                              HtmlUtils.cssClass(CSS_CLASS_DATETIME)
                              + HtmlUtils.attr(HtmlUtils.ATTR_TITLE,
                                  fullDate + extraAlt));
    }





    /**
     * _more_
     *
     * @param dttm _more_
     *
     * @return _more_
     *
     * @throws java.text.ParseException _more_
     */
    public Date parseDate(String dttm) throws java.text.ParseException {
        if (formats == null) {
            formats = new ArrayList<SimpleDateFormat>();
            formats.add(makeSDF("yyyy-MM-dd HH:mm:ss z"));
            formats.add(makeSDF("yyyy-MM-dd HH:mm:ss"));
            formats.add(makeSDF("yyyy-MM-dd HH:mm"));
            formats.add(makeSDF("yyyy-MM-dd"));
        }


        for (SimpleDateFormat fmt : formats) {
            try {
                synchronized (fmt) {
                    return fmt.parse(dttm);
                }
            } catch (Exception noop) {}
        }

        throw new IllegalArgumentException("Unable to parse date:" + dttm);
    }

    /**
     * _more_
     *
     *
     * @param request The request
     * @return _more_
     */
    public List getNavLinks(Request request) {
        List    links   = new ArrayList();
        boolean isAdmin = false;
        if (request != null) {
            User user = request.getUser();
            isAdmin = user.getAdmin();
        }

        String template = getPageHandler().getTemplateProperty(request,
                              "ramadda.template.link.wrapper", "");

        ApiMethod homeApi = getRepository().getApiManager().getHomeApi();
        for (ApiMethod apiMethod : getRepository().getApiManager().getTopLevelMethods()) {
            if (apiMethod.getMustBeAdmin() && !isAdmin) {
                continue;
            }
            if ( !apiMethod.getIsTopLevel()) {
                continue;
            }
            String url;
            if (apiMethod == homeApi) {
                url = fileUrl(apiMethod.getRequest());
            } else {
                url = request.url(apiMethod.getUrl());
            }


            String html = template.replace("${url}", url);
            html = html.replace("${label}", msg(apiMethod.getName()));
            html = html.replace("${topgroup}",
                                getEntryManager().getTopGroup().getName());
            links.add(html);
        }
        return links;
    }


    /**
     * _more_
     *
     * @param request The request
     * @param urls _more_
     * @param arg _more_
     *
     * @return _more_
     */
    public String makeHeader(Request request, List<RequestUrl> urls,
                             String arg) {
        List<String> links = new ArrayList();
        String       type  = request.getRequestPath();
        String onLabel = null;
        for (RequestUrl requestUrl : urls) {
            String label = requestUrl.getLabel();
            label = msg(label);
            if (label == null) {
                label = requestUrl.toString();
            }
            String url = request.url(requestUrl) + arg;
            if (type.endsWith(requestUrl.getPath())) {
                //links.add(HtmlUtils.span(label,
                // HtmlUtils.cssClass("subheader-on")));
                onLabel = label;
                //            } else {
            }
            links.add(
                      HtmlUtils.span(
                                     HtmlUtils.href(url, label),
                                     HtmlUtils.cssClass("subheader-off")));
            //            }
        }
        String header =
            StringUtil.join("<span class=\"subheader-sep\">|</span>", links);

        return HtmlUtils.tag(HtmlUtils.TAG_CENTER,
                             HtmlUtils.cssClass("subheader-container"),
                             HtmlUtils.tag(HtmlUtils.TAG_SPAN,
                                           HtmlUtils.cssClass("subheader"),
                                           header)) +
            (onLabel==null?"":HtmlUtils.p() +msgHeader(onLabel));
    }



    /**
     * _more_
     *
     * @param h _more_
     *
     * @return _more_
     */
    public String showDialogNote(String h) {
        return getMessage(h, Constants.ICON_INFORMATION, true);
    }

    /**
     * _more_
     *
     * @param h _more_
     *
     * @return _more_
     */
    public String progress(String h) {
        return getMessage(h, Constants.ICON_PROGRESS, false);
    }


    /**
     * _more_
     *
     * @param h _more_
     *
     * @return _more_
     */
    public String showDialogWarning(String h) {
        return getMessage(h, Constants.ICON_WARNING, true);
    }


    /**
     * _more_
     *
     * @param h _more_
     * @param buttons _more_
     *
     * @return _more_
     */
    public String showDialogQuestion(String h, String buttons) {
        return getMessage(h + "<p><hr>" + buttons, Constants.ICON_QUESTION,
                          false);
    }

    /**
     * _more_
     *
     * @param h _more_
     *
     * @return _more_
     */
    public String showDialogError(String h) {
        h = getDialogString(h);

        return getMessage(h, Constants.ICON_ERROR, true);
    }


    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    public static String getDialogString(String s) {
        s = HtmlUtils.entityEncode(s);
        s = s.replace("&#60;msg&#32;", "<msg ");
        s = s.replace("&#32;msg&#62;", " msg>");
        s = s.replace("&#32;", " ");
        s = s.replace("&#60;p&#62;", "<p>");
        s = s.replace("&#60;br&#62;", "<br>");
        s = s.replace("&#38;nbsp&#59;", "&nbsp;");

        return s;
    }


    /**
     * _more_
     *
     * @param h _more_
     * @param icon _more_
     * @param showClose _more_
     *
     * @return _more_
     */
    public String getMessage(String h, String icon, boolean showClose) {
        String html =
            HtmlUtils.jsLink(HtmlUtils.onMouseClick("hide('messageblock')"),
                             HtmlUtils.img(iconUrl(Constants.ICON_CLOSE)));
        if ( !showClose) {
            html = "&nbsp;";
        }
        h = "<div class=\"innernote\"><table cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><tr><td valign=\"top\">"
            + HtmlUtils.img(iconUrl(icon)) + HtmlUtils.space(2)
            + "</td><td valign=\"bottom\"><span class=\"notetext\">" + h
            + "</span></td></tr></table></div>";

        return "\n<table border=\"0\" id=\"messageblock\"><tr><td><div class=\"note\"><table><tr valign=top><td>"
               + h + "</td><td>" + html + "</td></tr></table>"
               + "</div></td></tr></table>\n";
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     */
    public PageStyle doMakePageStyle(Request request, Entry entry) {
        try {
            PageStyle pageStyle = new PageStyle();
            if (request.exists(PROP_NOSTYLE)
                    || getProperty(PROP_NOSTYLE, false)) {
                return pageStyle;
            }
            List<Metadata> metadataList =
                getMetadataManager().findMetadata(entry,
                    ContentMetadataHandler.TYPE_PAGESTYLE, true);
            if ((metadataList == null) || (metadataList.size() == 0)) {
                return pageStyle;
            }

            //menus -1, showbreadcrumbs-2, toolbar-3, entry header-4, layout toolbar-5, type-6,  apply to this-7, wiki-8
            Metadata theMetadata = null;
            for (Metadata metadata : metadataList) {
                if (Misc.equals(metadata.getAttr(7), "false")) {
                    if (metadata.getEntryId().equals(entry.getId())) {
                        continue;
                    }
                }
                String types = metadata.getAttr(6);
                if ((types == null) || (types.trim().length() == 0)) {
                    theMetadata = metadata;

                    break;
                }
                for (String type : StringUtil.split(types, ",", true, true)) {
                    if (type.equals("file") && !entry.isGroup()) {
                        theMetadata = metadata;

                        break;
                    }
                    if (type.equals("folder") && entry.isGroup()) {
                        theMetadata = metadata;

                        break;
                    }
                    if (entry.getTypeHandler().isType(type)) {
                        theMetadata = metadata;

                        break;
                    }
                }
            }

            if (theMetadata == null) {
                return pageStyle;
            }

            pageStyle.setShowBreadcrumbs(Misc.equals(theMetadata.getAttr2(),
                    "true"));
            pageStyle.setShowToolbar(Misc.equals(theMetadata.getAttr3(),
                    "true"));
            pageStyle.setShowEntryHeader(Misc.equals(theMetadata.getAttr4(),
                    "true"));
            pageStyle.setShowLayoutToolbar(
                Misc.equals(theMetadata.getAttr(5), "true"));

            boolean canEdit = getAccessManager().canDoAction(request, entry,
                                  Permission.ACTION_EDIT);
            if ( !canEdit) {
                String menus = theMetadata.getAttr1();
                if ((menus != null) && (menus.trim().length() > 0)) {
                    if (menus.equals("none")) {
                        pageStyle.setShowMenubar(false);
                    } else {
                        for (String menu :
                                StringUtil.split(menus, ",", true, true)) {
                            pageStyle.setMenu(menu);
                        }
                    }
                }
            }
            if ((theMetadata.getAttr(8) != null)
                    && (theMetadata.getAttr(8).trim().length() > 0)) {
                pageStyle.setWikiTemplate(theMetadata.getAttr(8));
            }

            return pageStyle;
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }



}
