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


import org.ramadda.repository.auth.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;
import org.ramadda.repository.util.*;

import org.ramadda.util.HtmlTemplate;
import org.ramadda.util.PropertyProvider;


import org.ramadda.util.HtmlUtils;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;

import java.io.*;
import java.io.File;
import java.io.InputStream;
import java.net.URL;


import java.util.ArrayList;
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

    public static final String DEFAULT_TEMPLATE  = "aodnStyle";


    /** _more_          */
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

    /** Set this to true to print to a file the missing messages and this also
        adds a "NA:" to the missing labels.*/
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

        if (!request.get(ARG_DECORATE, true)) {
            return;
        }

        Repository repository = getRepository();
        Entry currentEntry =
            (Entry) getSessionManager().getSessionProperty(request,
                "lastentry");
        String   template     = null;
        HtmlTemplate htmlTemplate;
        if (request.isMobile()) {
            htmlTemplate = getMobileTemplate();
        } else {
            htmlTemplate = getTemplate(request);
        }
        template = htmlTemplate.getTemplate();

        String sessionMessage =
            getSessionManager().getSessionMessage(request);


        String jsContent = getTemplateJavascriptContent();

        List   links     = (List) result.getProperty(PROP_NAVLINKS);
        String linksHtml = HtmlUtils.space(1);

        if (links != null) {
            linksHtml = StringUtil.join(htmlTemplate.getTemplateProperty(
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
            htmlTemplate.getTemplateProperty("ramadda.template.favorites",
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
                Entry entry = favorite.getEntry();
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
            String cartTemplate = htmlTemplate.getTemplateProperty(
                                      "ramadda.template.cart",
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
        if (sessionMessage != null) {
            content = repository.showDialogNote(sessionMessage) + content;
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
            template = pageDecorator.decoratePage(repository, request,
                    template, currentEntry);
        }
        String   html   = template;
        String[] macros = new String[] {
            MACRO_LOGO_URL, logoUrl, MACRO_LOGO_IMAGE, logoImage,
            MACRO_HEADER_IMAGE, iconUrl(ICON_HEADER), MACRO_HEADER_TITLE,
            pageTitle, 
            MACRO_USERLINK, getUserManager().getUserLinks(request, htmlTemplate),
            MACRO_LINKS, linksHtml,
            MACRO_REPOSITORY_NAME,
            repository.getProperty(PROP_REPOSITORY_NAME, "Repository"),
            MACRO_FOOTER, repository.getProperty(PROP_HTML_FOOTER, BLANK),
            MACRO_TITLE, result.getTitle(), MACRO_BOTTOM,
            result.getBottomHtml(), MACRO_SEARCH_URL,
            getSearchManager().getSearchUrl(request), 
            MACRO_CONTENT, content + jsContent, MACRO_FAVORITES,
            favorites.toString(), MACRO_ENTRY_HEADER, entryHeader,
            MACRO_HEADER, header, MACRO_ENTRY_FOOTER, entryFooter,
            MACRO_ENTRY_BREADCRUMBS, entryBreadcrumbs, MACRO_HEADFINAL, head,
            MACRO_ROOT, repository.getUrlBase(),
        };


        for (int i = 0; i < macros.length; i += 2) {
            html = html.replace("${" + macros[i] + "}", macros[i + 1]);
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
            "", " id=\"tooltipdiv\" class=\"tooltip-outer\" ") + HtmlUtils.div(
            "", " id=\"popupdiv\" class=\"tooltip-outer\" ") + HtmlUtils.div(
            "", " id=\"output\"") + HtmlUtils.div(
            "", " id=\"selectdiv\" class=\"selectdiv\" ") + HtmlUtils.div(
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
                phraseMap = tmpPhraseMap;
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
        String       type    =
            IOUtil.stripExtension(IOUtil.getFileTail(file));
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
    private List<HtmlTemplate> getTemplates() {
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
            if(i==0)
                getLogManager().logInfoAndPrint("RAMADDA: language packs:" + listing);
            for (String path : listing) {
                if ( !path.endsWith(".pack")) {
                    if(i==0)
                        getLogManager().logInfoAndPrint("RAMADDA: not ends with .pack:" + path);
                    continue;
                }
                if (seenPack.contains(path)) {
                    getLogManager().logInfoAndPrint("RAMADDA: seen:" + path);
                    continue;
                }
                seenPack.add(path);
                String content =
                    getStorageManager().readUncheckedSystemResource(path,
                        (String) null);
                if (content == null) {
                    getLogManager().logInfoAndPrint("RAMADDA: could not read:" + path);
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
                    getLogManager().logInfoAndPrint("RAMADDA: adding language:" + path);
                    languages.add(new TwoFacedObject(name, type));
                    languageMap.put(type, properties);
                } else {
                    getLogManager().logError("No _type_ found in: " + path);
                    getLogManager().logInfoAndPrint("RAMADDA: no _type_ found in:" + path);
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


    /**
     * Find the html template for the given request
     *
     * @param request The request
     *
     * @return _more_
     */
    public HtmlTemplate getTemplate(Request request) {
        if (request.isMobile()) {
            return getMobileTemplate();
        }
        List<HtmlTemplate> theTemplates = getTemplates();
        if ((request == null) && (defaultTemplate != null)) {
            return defaultTemplate;
        }
        String templateId = request.getHtmlTemplateId();

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




}
