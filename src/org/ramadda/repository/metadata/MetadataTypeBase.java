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

package org.ramadda.repository.metadata;


import org.ramadda.repository.*;
import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;


import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;

import java.awt.Image;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import java.net.URL;
import java.net.URLConnection;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;



/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class MetadataTypeBase extends RepositoryManager {

    /** _more_ */
    private SimpleDateFormat sdf;

    /** _more_ */
    public static final String TAG_TYPE = "type";

    /** _more_ */
    public static final String TAG_ELEMENT = "element";

    /** _more_ */
    public static final String TAG_TEMPLATE = "template";

    /** _more_ */
    public static final String ATTR_FILE = "file";

    /** _more_ */
    public static final String ATTR_TAG = "tag";

    /** _more_ */
    public static final String ATTR_TYPE = "type";


    /** _more_ */
    public static final String ATTR_NAME = "name";

    /** _more_ */
    public static final String ATTR_LABEL = "label";

    /** _more_ */
    public static final String ATTR_SUFFIX = "suffix";

    /** _more_ */
    public static final String ATTR_SEARCHABLE = "searchable";



    /** _more_ */
    public static final String ATTR_SHOWINHTML = "showinhtml";


    /** _more_ */
    public static final String TEMPLATETYPE_THREDDS = "thredds";

    /** _more_ */
    public static final String TEMPLATETYPE_DIF = "dif";


    /** _more_ */
    public static final String TEMPLATETYPE_ISO = "iso";


    /** _more_ */
    public static final String TEMPLATETYPE_OAIDC = "oai_dc";

    /** _more_ */
    public static final String TEMPLATETYPE_HTML = "html";

    /** _more_ */
    private String name;

    /** _more_ */
    private String label;

    /** _more_ */
    private String suffixLabel;

    /** _more_ */
    private boolean showInHtml = true;

    /** _more_ */
    private Hashtable<String, String> templates = new Hashtable<String,
                                                      String>();



    /** _more_ */
    List<MetadataElement> children = new ArrayList<MetadataElement>();


    /** _more_ */
    MetadataHandler handler;

    /** _more_ */
    private boolean searchable = false;

    /** _more_ */
    private Hashtable<String, String> tags = new Hashtable<String, String>();


    /**
     * _more_
     *
     *
     * @param handler _more_
     */
    public MetadataTypeBase(MetadataHandler handler) {
        super(handler.getRepository());
        this.handler = handler;
    }


    /**
     *  Set the Handler property.
     *
     *  @param value The new value for Handler
     */
    public void setHandler(MetadataHandler value) {
        this.handler = value;
    }

    /**
     *  Get the Handler property.
     *
     *  @return The Handler
     */
    public MetadataHandler getHandler() {
        return this.handler;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return name;
    }


    /**
     * _more_
     *
     * @param templateType _more_
     * @param entry _more_
     * @param metadata _more_
     * @param parent _more_
     *
     * @throws Exception _more_
     */
    protected void checkFileXml(String templateType, Entry entry,
                                Metadata metadata, Element parent)
            throws Exception {
        for (MetadataElement element : getChildren()) {
            if ( !element.getDataType().equals(element.TYPE_FILE)) {
                continue;
            }
            File f = getFile(entry, metadata, element);
            if (f == null) {
                continue;
            }
            String tail = getStorageManager().getFileTail(f.toString());
            String path =handler.getRepository().getAbsoluteUrl(null,
                                                                handler.getRepository().getMetadataManager().URL_METADATA_VIEW) + "/" + tail;

            String url = HtmlUtils.url(path, ARG_ELEMENT,
                                       element.getIndex() + "", ARG_ENTRYID,
                                       metadata.getEntryId(),
                                       ARG_METADATA_ID, metadata.getId());
            //TODO:
            if (templateType.equals(TEMPLATETYPE_THREDDS)) {
                XmlUtil.create(parent.getOwnerDocument(), "property", parent,
                               new String[] { "name", (element.getThumbnail()
                        ? "thumbnail"
                        : "attachment"), "value", url });
            }

        }
    }


    /**
     * _more_
     *
     * @param templateType _more_
     * @param entry _more_
     * @param metadata _more_
     * @param parent _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String applyTemplate(String templateType, Entry entry,
                                Metadata metadata, Element parent)
            throws Exception {
        checkFileXml(templateType, entry, metadata, parent);

        String template = getTemplate(templateType);
        if ((template == null) || (template.length() == 0)) {
            return null;
        }
        //Remove newlines??
        template = template.replaceAll("\n", "");
        template = template.replace("${root}", getRepository().getUrlBase());


        template = template.replace("${root}", getRepository().getUrlBase());


        template = template.replace("${entry.id}", entry.getId());
        template = template.replace("${entry.name}", entry.getName());
        template = template.replace("${entry.name.cdata}",
                                    wrapCdata(entry.getName()));
        template = template.replace("${entry.description}",
                                    entry.getDescription());
        template = template.replace("${entry.description.cdata}",
                                    wrapCdata(entry.getDescription()));
        template = template.replace("${entry.publishdate}",
                                    formatDate(entry.getCreateDate()));
        template = template.replace("${entry.changedate}",
                                    formatDate(entry.getChangeDate()));

        for (MetadataElement element : getChildren()) {
            String value = element.getValueForXml(templateType, entry,
                               metadata,
                               metadata.getAttr(element.getIndex()), parent);

            template = applyMacros(template, element, value);

        }

        return template;
    }


    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    public String wrapCdata(String s) {
        return "<![CDATA[" + s + "]]>";
    }




    /**
     * _more_
     *
     * @param template _more_
     * @param element _more_
     * @param value _more_
     *
     * @return _more_
     */
    public String applyMacros(String template, MetadataElement element,
                              String value) {
        if (value == null) {
            value = "";
        }
        //        value = XmlUtil.encodeString(value);
        String   label = element.getLabel(value);
        String   name  = element.getName();
        String[] keys  = {
            "attr" + element.getIndex(), name, name.toLowerCase(),
            name.replace(" ", "_"), name.toLowerCase().replace(" ", "_"),
            element.getId(),
        };
        Hashtable macros = new Hashtable();
        macros.put("name", name);
        //      template = template.replaceAll("\\${name}", name);
        for (String key : keys) {
            if (key == null) {
                continue;
            }
            //                System.err.println("key: " + key);
            //            template = template.replaceAll("\\${" + key + "}", value);
            //            template = template.replaceAll("\\${" + key + ".label}", label);
            //            template = template.replaceAll("\\${" + key + ".cdata}",
            //                                        "<![CDATA[" + value + "]]>");
            macros.put(key, value);
            macros.put(key + ".label", label);
            macros.put(key + ".cdata", wrapCdata(value));
            macros.put(key + ".encoded", XmlUtil.encodeString(value));
        }

        template = StringUtil.applyMacros(template, macros, false);
        template = template.replaceAll("\r\n\r\n", "<p>");
        template = template.replace("\n\n", "<p>");

        return template;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean hasElements() {
        return getChildren().size() > 0;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isSimple() {
        if (getChildren().size() > 1) {
            return false;
        }

        return !getChildren().get(0).getDataType().equals(TYPE_GROUP);
    }


    /**
     * _more_
     *
     * @param what _more_
     *
     * @return _more_
     */
    public String getTag(String what) {
        return tags.get(what + ".tag");
    }

    /**
     * _more_
     *
     * @param node _more_
     *
     * @throws Exception _more_
     */
    public void init(Element node) throws Exception {
        setName(XmlUtil.getAttribute(node, ATTR_NAME, ""));
        setLabel(XmlUtil.getAttribute(node, ATTR_LABEL, (String) null));
        setSuffixLabel(XmlUtil.getAttribute(node, ATTR_SUFFIX,
                                            (String) null));

        setShowInHtml(XmlUtil.getAttribute(node, ATTR_SHOWINHTML, true));
        setSearchable(XmlUtil.getAttributeFromTree(node, ATTR_SEARCHABLE,
                false));

        NamedNodeMap nnm = node.getAttributes();
        if (nnm != null) {
            for (int i = 0; i < nnm.getLength(); i++) {
                Attr   attr     = (Attr) nnm.item(i);
                String attrName = attr.getNodeName();
                if (attrName.endsWith(".tag")) {
                    tags.put(attrName, attr.getNodeValue());
                }
            }
        }

        NodeList children = XmlUtil.getElements(node);
        for (int i = 0; i < children.getLength(); i++) {
            Element childNode = (Element) children.item(i);
            if (childNode.getTagName().equals(TAG_TEMPLATE)) {
                processTemplateTag(childNode);
            } else if (childNode.getTagName().equals(TAG_ELEMENT)) {}
            else {
                logError("Unknown metadata xml tag:"
                         + XmlUtil.toString(childNode), null);
            }
        }

        List childrenElements = XmlUtil.findChildren(node, TAG_ELEMENT);
        int  lastIndex        = 0;
        for (int j = 0; j < childrenElements.size(); j++) {
            Element elementNode = (Element) childrenElements.get(j);
            int     index       = lastIndex + 1;
            if (XmlUtil.hasAttribute(elementNode,
                                     MetadataElement.ATTR_INDEX)) {
                index = XmlUtil.getAttribute(elementNode,
                                             MetadataElement.ATTR_INDEX,
                                             index);
            }
            lastIndex = index;
            MetadataElement element = new MetadataElement(getHandler(), this,
                                          lastIndex, elementNode);
            addElement(element);
        }

    }


    /**
     * _more_
     *
     * @param templateType _more_
     * @param template _more_
     */
    public void processTemplate(String templateType, String template) {
        templates.put(templateType, template);
    }


    /**
     * _more_
     *
     * @param childNode _more_
     *
     * @throws Exception _more_
     */
    public void processTemplateTag(Element childNode) throws Exception {
        String templateType = XmlUtil.getAttribute(childNode, ATTR_TYPE);
        if (XmlUtil.hasAttribute(childNode, ATTR_FILE)) {

            processTemplate(templateType,
                            XmlUtil.getAttribute(childNode, ATTR_FILE));
        } else {
            processTemplate(templateType, XmlUtil.getChildText(childNode));
        }
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public List<MetadataElement> getChildren() {
        return children;
    }

    /**
     * _more_
     *
     * @param element _more_
     */
    public void addElement(MetadataElement element) {
        getChildren().add(element);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param metadata _more_
     * @param matchFile _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getImageUrl(Request request, Entry entry,
                              Metadata metadata, String matchFile)
            throws Exception {
        for (MetadataElement element : getChildren()) {
            if ( !element.getDataType().equals(element.TYPE_FILE)) {
                continue;
            }

            File f = getFile(entry, metadata, element);
            if (f == null) {
                String value = metadata.getAttr(element.getIndex());
                if ((value != null) && value.startsWith("http")) {
                    return value;
                }

                return null;
            }

            String tail = getStorageManager().getFileTail(f.toString());
            if (matchFile != null) {
                if ( !matchFile.equals("*")
                        && !Misc.equals(matchFile, tail)) {
                    continue;
                }
            }
            if (ImageUtils.isImage(f.toString())) {
                tail = tail.replaceAll(" ", "_");
                String path =
                    handler.getRepository().getMetadataManager()
                        .URL_METADATA_VIEW + "/" + tail;


                return HtmlUtils.url(path, ARG_ELEMENT,
                                     element.getIndex() + "", ARG_ENTRYID,
                                     metadata.getEntryId(), ARG_METADATA_ID,
                                     metadata.getId());
            }
        }

        return null;
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param metadata _more_
     * @param element _more_
     * @param forLink _more_
     *
     * @return _more_
     */
    public String getFileHtml(Request request, Entry entry,
                              Metadata metadata, MetadataElement element,
                              boolean forLink) {
        File f = getFile(entry, metadata, element);
        if ((f == null) || !f.exists() || f.isDirectory()) {
            return null;
        }

        String extra = (forLink
                        ? " "
                        : "");
        String tail  = getStorageManager().getFileTail(f.toString());
        String path  =
            handler.getRepository().getMetadataManager().URL_METADATA_VIEW
            + "/" + tail;


        if (ImageUtils.isImage(f.toString())) {
            String img = HtmlUtils.img(HtmlUtils.url(path, ARG_ELEMENT,
                             element.getIndex() + "", ARG_ENTRYID,
                             metadata.getEntryId(), ARG_METADATA_ID,
                             metadata.getId(), ARG_THUMBNAIL,
                             "" + forLink), (forLink
                                             ? msg("Click to enlarge")
                                             : ""), extra);

            if (forLink) {
                String bigimg = HtmlUtils.img(HtmlUtils.url(path,
                                    ARG_ELEMENT, element.getIndex() + "",
                                    ARG_ENTRYID, metadata.getEntryId(),
                                    ARG_METADATA_ID,
                                    metadata.getId()), "thumbnail", "");


                String imgUrl = HtmlUtils.url(path, ARG_ELEMENT,
                                    element.getIndex() + "", ARG_ENTRYID,
                                    metadata.getEntryId(), ARG_METADATA_ID,
                                    metadata.getId());

                img = handler.getPageHandler().makePopupLink(img, bigimg,
                        true, false);
            } else {
                img = img + "\n<br>\n<b>" + tail + "</b>\n";
            }

            return img;
        } else if (f.exists()) {
            String name = getStorageManager().getFileTail(f.getName());

            return HtmlUtils.href(HtmlUtils.url(path, ARG_ELEMENT,
                    element.getIndex() + "", ARG_ENTRYID,
                    metadata.getEntryId(), ARG_METADATA_ID,
                    metadata.getId()), name);
        }

        return "";
    }


    /**
     * _more_
     *
     * @param t _more_
     *
     * @return _more_
     */
    private String formatDate(long t) {
        if (sdf == null) {
            sdf = RepositoryUtil.makeDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        }
        synchronized (sdf) {
            return sdf.format(new Date(t)) + "Z";
        }
    }


    /**
     * _more_
     *
     * @param entry _more_
     * @param metadata _more_
     * @param attr _more_
     *
     * @return _more_
     */
    public File getFile(Entry entry, Metadata metadata, int attr) {
        String filename = metadata.getAttr(attr);
        if ((filename == null) || (filename.trim().length() == 0)) {
            return null;
        }

        return new File(
            IOUtil.joinDir(
                getStorageManager().getEntryDir(
                    metadata.getEntryId(), false), filename));
    }


    /**
     * _more_
     *
     * @param entry _more_
     * @param metadata _more_
     * @param element _more_
     *
     * @return _more_
     */
    public File getFile(Entry entry, Metadata metadata,
                        MetadataElement element) {
        File f;
        if ( !entry.getIsLocalFile()) {
            f = getFile(entry, metadata, element.getIndex());
        } else {
            f = new File(metadata.getAttr(element.getIndex()));
        }

        if ((f == null) || !f.exists()) {
            return null;
        }

        return f;
    }



    /**
     * Set the Name property.
     *
     * @param value The new value for Name
     */
    public void setName(String value) {
        name = value;
    }

    /**
     * Get the Name property.
     *
     * @return The Name
     */
    public String getName() {
        return name;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getLabel() {
        if (label != null) {
            return label;
        }

        return getName();
    }


    /**
     * _more_
     *
     * @param value _more_
     */
    public void setLabel(String value) {
        label = value;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getSuffixLabel() {
        return suffixLabel;
    }


    /**
     * _more_
     *
     * @param value _more_
     */
    public void setSuffixLabel(String value) {
        suffixLabel = value;
    }

    /**
     *  Set the ShowInHtml property.
     *
     *  @param value The new value for ShowInHtml
     */
    public void setShowInHtml(boolean value) {
        this.showInHtml = value;
    }

    /**
     *  Get the ShowInHtml property.
     *
     *  @return The ShowInHtml
     */
    public boolean getShowInHtml() {
        return this.showInHtml;
    }

    /**
     * Get the Template property.
     *
     *
     * @param type _more_
     * @return The Template
     */
    public String getTemplate(String type) {
        return templates.get(type);
    }

    /**
     * Set the Searchable property.
     *
     * @param value The new value for Searchable
     */
    public void setSearchable(boolean value) {
        this.searchable = value;
    }

    /**
     * Get the Searchable property.
     *
     * @return The Searchable
     */
    public boolean getSearchable() {
        return this.searchable;
    }


}
