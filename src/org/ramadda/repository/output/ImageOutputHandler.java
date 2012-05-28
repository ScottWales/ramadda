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

package org.ramadda.repository.output;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.type.*;


import org.w3c.dom.*;

import ucar.unidata.sql.SqlUtil;
import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;

import java.awt.Image;
import java.awt.image.*;


import java.io.*;

import java.io.File;


import java.net.*;


import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;



import java.util.regex.*;

import java.util.zip.*;


/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class ImageOutputHandler extends OutputHandler {

    /** _more_ */
    public static final String ARG_IMAGE_STYLE = "image.style";

    /** _more_ */
    public static final String ARG_IMAGE_EDIT = "image.edit";

    /** _more_ */
    public static final String ARG_IMAGE_APPLY_TO_GROUP =
        "image.applytogroup";

    /** _more_ */
    public static final String ARG_IMAGE_UNDO = "image.undo";

    /** _more_ */
    public static final String ARG_IMAGE_EDIT_RESIZE = "image.edit.resize";

    /** _more_ */
    public static final String ARG_IMAGE_EDIT_WIDTH = "image.edit.width";

    /** _more_ */
    public static final String ARG_IMAGE_EDIT_CROP = "image.edit.crop";

    /** _more_ */
    public static final String ARG_IMAGE_EDIT_REDEYE = "image.edit.redeye";

    /** _more_ */
    public static final String ARG_IMAGE_CROPX1 = "image.edit.cropx1";

    /** _more_ */
    public static final String ARG_IMAGE_CROPY1 = "image.edit.cropy1";

    /** _more_ */
    public static final String ARG_IMAGE_CROPX2 = "image.edit.cropx2";

    /** _more_ */
    public static final String ARG_IMAGE_CROPY2 = "image.edit.cropy2";

    /** _more_ */
    public static final String ARG_IMAGE_EDIT_ROTATE_LEFT =
        "image.edit.rotate.left";

    /** _more_ */
    public static final String ARG_IMAGE_EDIT_ROTATE_LEFT_X =
        "image.edit.rotate.left.x";

    /** _more_ */
    public static final String ARG_IMAGE_EDIT_ROTATE_LEFT_Y =
        "image.edit.rotate.left.y";

    /** _more_ */
    public static final String ARG_IMAGE_EDIT_ROTATE_RIGHT =
        "image.edit.rotate.right";

    /** _more_ */
    public static final String ARG_IMAGE_EDIT_ROTATE_RIGHT_X =
        "image.edit.rotate.right.x";


    /** _more_ */
    public static final String ARG_IMAGE_EDIT_ROTATE_RIGHT_Y =
        "image.edit.rotate.right.y";

    /** _more_ */
    public static final OutputType OUTPUT_GALLERY =
        new OutputType("Gallery", "image.gallery",
                       OutputType.TYPE_VIEW | OutputType.TYPE_FORSEARCH, "",
                       ICON_IMAGES);

    /** _more_ */
    public static final OutputType OUTPUT_VIDEO =
        new OutputType("Play Video", "image.video",
                       OutputType.TYPE_VIEW | OutputType.TYPE_FORSEARCH, "",
                       ICON_IMAGES);

    /** _more_ */
    public static final OutputType OUTPUT_PLAYER =
        new OutputType("Image Player", "image.player",
                       OutputType.TYPE_VIEW | OutputType.TYPE_FORSEARCH, "",
                       ICON_IMAGES);

    /** _more_ */
    public static final OutputType OUTPUT_SLIDESHOW =
        new OutputType("Slideshow", "image.slideshow",
                       OutputType.TYPE_VIEW | OutputType.TYPE_FORSEARCH, "",
                       ICON_IMAGES);


    /** _more_ */
    public static final OutputType OUTPUT_EDIT = new OutputType("Edit Image",
                                                     "image.edit",
                                                     OutputType.TYPE_VIEW,
                                                     "", ICON_IMAGES);



    /**
     * _more_
     *
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public ImageOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_GALLERY);
        addType(OUTPUT_PLAYER);
        //        addType(OUTPUT_SLIDESHOW);
        addType(OUTPUT_EDIT);
        addType(OUTPUT_VIDEO);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param state _more_
     * @param links _more_
     *
     *
     * @throws Exception _more_
     */

    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {


        if (state.entry != null) {
            if (state.entry.isFile()) {
                String extension =
                    IOUtil.getFileExtension(
                        state.entry.getResource().getPath()).toLowerCase();
                if (extension.equals(".mp3") || extension.equals(".mp4")
                        || extension.equals(".mpg")) {
                    links.add(makeLink(request, state.getEntry(),
                                       OUTPUT_VIDEO));
                }
            }
            if (getAccessManager().canDoAction(request, state.entry,
                    Permission.ACTION_EDIT)) {
                if (state.entry.getResource().isEditableImage()) {
                    File f = state.entry.getFile();
                    if ((f != null) && f.canWrite()) {
                        Link link = makeLink(request, state.getEntry(),
                                             OUTPUT_EDIT);
                        link.setLinkType(OutputType.TYPE_EDIT);
                        links.add(link);
                    }
                }
            }
            return;
        }


        List<Entry> entries = state.getAllEntries();
        if (entries.size() == 0) {
            return;
        }

        if (entries.size() > 0) {
            boolean ok = false;
            for (Entry entry : entries) {
                if (entry.getResource().isImage()) {
                    ok = true;
                    break;
                }
            }
            if ( !ok) {
                return;
            }
        }

        if (state.getEntry() != null) {
            //            links.add(makeLink(request, state.getEntry(), OUTPUT_SLIDESHOW));
            links.add(makeLink(request, state.getEntry(), OUTPUT_GALLERY));
            links.add(makeLink(request, state.getEntry(), OUTPUT_PLAYER));
        }
    }




    /** _more_ */
    private Hashtable<String, Image> imageCache = new Hashtable<String,
                                                      Image>();

    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    private Image getImage(Entry entry) {
        Image image = imageCache.get(entry.getId());
        if (image == null) {
            image = ImageUtils.readImage(entry.getResource().getPath(),
                                         false);
            //Keep the cache size low
            if (imageCache.size() > 5) {
                imageCache = new Hashtable<String, Image>();
            }
            imageCache.put(entry.getId(), image);
        }
        return image;
    }

    /**
     * _more_
     *
     * @param entry _more_
     * @param image _more_
     */
    private void putImage(Entry entry, Image image) {
        imageCache.put(entry.getId(), image);
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputEntry(Request request, OutputType outputType,
                              Entry entry)
            throws Exception {

        StringBuffer sb = new StringBuffer();

        if (outputType.equals(OUTPUT_VIDEO)) {
            Link link = entry.getTypeHandler().getEntryDownloadLink(request,
                            entry);
            if (link == null) {
                sb.append("Not available");
            } else {
                sb.append(HtmlUtil.p());
                String html =
                    "<OBJECT CLASSID=\"clsid:02BF25D5-8C17-4B23-BC80-D3488ABDDC6B\" CODEBASE=\"http://www.apple.com/qtactivex/qtplugin.cab\"  > <PARAM NAME=\"src\" VALUE=\""
                    + link.getUrl()
                    + "\" > <PARAM NAME=\"autoplay\" VALUE=\"true\" > <EMBED SRC=\""
                    + link.getUrl()
                    + "\" TYPE=\"image/x-macpaint\" PLUGINSPAGE=\"http://www.apple.com/quicktime/download\"  AUTOPLAY=\"true\"></EMBED> </OBJECT>";

                System.err.println(html);
                sb.append(html);
            }


            return new Result("Video", sb);
        }

        String  url            = getImageUrl(request, entry, true);
        Image   image          = null;
        boolean shouldRedirect = false;

        boolean applyToGroup   = request.get(ARG_IMAGE_APPLY_TO_GROUP, false);

        if ( !applyToGroup) {
            image          = getImage(entry);
            shouldRedirect = processImage(request, entry, image);
        } else {
            List<Entry> entries = getEntryManager().getChildren(request,
                                      entry.getParentEntry());
            for (Entry childEntry : entries) {
                if ( !childEntry.getResource().isEditableImage()) {
                    continue;
                }
                image          = getImage(childEntry);
                shouldRedirect = processImage(request, childEntry, image);
            }
        }


        if (shouldRedirect) {
            request.remove(ARG_IMAGE_EDIT_RESIZE);
            request.remove(ARG_IMAGE_EDIT_REDEYE);
            request.remove(ARG_IMAGE_EDIT_CROP);
            request.remove(ARG_IMAGE_EDIT_ROTATE_LEFT);
            request.remove(ARG_IMAGE_EDIT_ROTATE_RIGHT);
            request.remove(ARG_IMAGE_EDIT_ROTATE_LEFT_X);
            request.remove(ARG_IMAGE_EDIT_ROTATE_RIGHT_X);
            request.remove(ARG_IMAGE_EDIT_ROTATE_LEFT_Y);
            request.remove(ARG_IMAGE_EDIT_ROTATE_RIGHT_Y);
            request.remove(ARG_IMAGE_UNDO);
            return new Result(request.getUrl());
        }



        if (image == null) {
            image = getImage(entry);
        }
        int imageWidth  = image.getWidth(null);
        int imageHeight = image.getHeight(null);

        sb.append(request.formPost(getRepository().URL_ENTRY_SHOW));


        sb.append(HtmlUtil.hidden(ARG_ENTRYID, entry.getId()));
        sb.append(HtmlUtil.hidden(ARG_OUTPUT, OUTPUT_EDIT));
        sb.append(HtmlUtil.submit(msgLabel("Change width"),
                                  ARG_IMAGE_EDIT_RESIZE));
        sb.append(HtmlUtil.input(ARG_IMAGE_EDIT_WIDTH, "" + imageWidth,
                                 HtmlUtil.SIZE_5));
        sb.append(HtmlUtil.space(2));

        sb.append(HtmlUtil.submit(msg("Crop"), ARG_IMAGE_EDIT_CROP));
        sb.append(HtmlUtil.submit(msg("Remove Redeye"),
                                  ARG_IMAGE_EDIT_REDEYE));
        sb.append(HtmlUtil.hidden(ARG_IMAGE_CROPX1, "",
                                  HtmlUtil.SIZE_3
                                  + HtmlUtil.id(ARG_IMAGE_CROPX1)));
        sb.append(HtmlUtil.hidden(ARG_IMAGE_CROPY1, "",
                                  HtmlUtil.SIZE_3
                                  + HtmlUtil.id(ARG_IMAGE_CROPY1)));
        sb.append(HtmlUtil.hidden(ARG_IMAGE_CROPX2, "",
                                  HtmlUtil.SIZE_3
                                  + HtmlUtil.id(ARG_IMAGE_CROPX2)));
        sb.append(HtmlUtil.hidden(ARG_IMAGE_CROPY2, "",
                                  HtmlUtil.SIZE_3
                                  + HtmlUtil.id(ARG_IMAGE_CROPY2)));
        sb.append(HtmlUtil.div("",
                               HtmlUtil.cssClass("image_edit_box")
                               + HtmlUtil.id("image_edit_box")));



        sb.append(HtmlUtil.space(2));
        sb.append(HtmlUtil.submitImage(iconUrl(ICON_ANTIROTATE),
                                       ARG_IMAGE_EDIT_ROTATE_LEFT,
                                       msg("Rotate Left")));
        sb.append(HtmlUtil.space(2));
        sb.append(HtmlUtil.submitImage(iconUrl(ICON_ROTATE),
                                       ARG_IMAGE_EDIT_ROTATE_RIGHT,
                                       msg("Rotate Right")));
        File entryDir = getStorageManager().getEntryDir(entry.getId(), false);
        File original = new File(entryDir + "/" + "originalimage");
        if (original.exists()) {
            sb.append(HtmlUtil.space(2));
            sb.append(HtmlUtil.submit(msg("Undo all edits"), ARG_IMAGE_UNDO));
        }

        sb.append(HtmlUtil.space(20));
        sb.append(HtmlUtil.checkbox(ARG_IMAGE_APPLY_TO_GROUP, "true",
                                    applyToGroup));
        sb.append(HtmlUtil.space(1));
        sb.append(msg("Apply to siblings"));


        sb.append(HtmlUtil.formClose());


        String clickParams =
            "event,'imgid',"
            + HtmlUtil.comma(HtmlUtil.squote(ARG_IMAGE_CROPX1),
                             HtmlUtil.squote(ARG_IMAGE_CROPY1),
                             HtmlUtil.squote(ARG_IMAGE_CROPX2),
                             HtmlUtil.squote(ARG_IMAGE_CROPY2));

        sb.append(
            HtmlUtil.importJS(getRepository().fileUrl("/editimage.js")));

        String call = HtmlUtil.onMouseClick(HtmlUtil.call("editImageClick",
                          clickParams));
        sb.append(HtmlUtil.img(url, "", HtmlUtil.id("imgid") + call));
        return new Result("Image Edit", sb);

    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param image _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private boolean processImage(Request request, Entry entry, Image image)
            throws Exception {
        if ( !getAccessManager().canDoAction(request, entry,
                                             Permission.ACTION_EDIT)) {
            throw new AccessException("Cannot edit image", null);
        }

        int   imageWidth  = image.getWidth(null);
        int   imageHeight = image.getHeight(null);
        Image newImage    = null;
        if (request.exists(ARG_IMAGE_UNDO)) {
            File f = entry.getFile();
            if ((f != null) && f.canWrite()) {
                File entryDir =
                    getStorageManager().getEntryDir(entry.getId(), true);
                File original = new File(entryDir + "/" + "originalimage");
                if (original.exists()) {
                    imageCache.remove(entry.getId());
                    IOUtil.copyFile(original, f);
                    return true;
                }
            }
        } else if (request.exists(ARG_IMAGE_EDIT_RESIZE)) {
            newImage = ImageUtils.resize(image,
                                         request.get(ARG_IMAGE_EDIT_WIDTH,
                                             imageWidth), -1);

        } else if (request.exists(ARG_IMAGE_EDIT_REDEYE)) {
            int x1 = request.get(ARG_IMAGE_CROPX1, 0);
            int y1 = request.get(ARG_IMAGE_CROPY1, 0);
            int x2 = request.get(ARG_IMAGE_CROPX2, 0);
            int y2 = request.get(ARG_IMAGE_CROPY2, 0);
            if ((x1 < x2) && (y1 < y2)) {
                newImage = ImageUtils.removeRedeye(image, x1, y1, x2, y2);
            }

        } else if (request.exists(ARG_IMAGE_EDIT_CROP)) {
            int x1 = request.get(ARG_IMAGE_CROPX1, 0);
            int y1 = request.get(ARG_IMAGE_CROPY1, 0);
            int x2 = request.get(ARG_IMAGE_CROPX2, 0);
            int y2 = request.get(ARG_IMAGE_CROPY2, 0);
            if ((x1 < x2) && (y1 < y2)) {
                newImage = ImageUtils.clip(ImageUtils.toBufferedImage(image),
                                           new int[] { x1,
                        y1 }, new int[] { x2, y2 });
            }
        } else if (request.exists(ARG_IMAGE_EDIT_ROTATE_LEFT)
                   || request.exists(ARG_IMAGE_EDIT_ROTATE_LEFT_X)) {
            newImage = ImageUtils.rotate90(ImageUtils.toBufferedImage(image),
                                           true);

        } else if (request.exists(ARG_IMAGE_EDIT_ROTATE_RIGHT)
                   || request.exists(ARG_IMAGE_EDIT_ROTATE_RIGHT_X)) {
            newImage = ImageUtils.rotate90(ImageUtils.toBufferedImage(image),
                                           false);

        }
        if (newImage != null) {
            ImageUtils.waitOnImage(newImage);
            putImage(entry, newImage);
            File f = entry.getFile();
            getStorageManager().checkReadFile(f);
            if ((f != null) && f.canWrite()) {
                File entryDir =
                    getStorageManager().getEntryDir(entry.getId(), true);
                File original = new File(entryDir + "/" + "originalimage");
                if ( !original.exists()) {
                    IOUtil.copyFile(f, original);
                }
                ImageUtils.writeImageToFile(newImage, f);
            }
            return true;
        }

        return false;
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param group _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputGroup(Request request, OutputType outputType,
                              Entry group, List<Entry> subGroups,
                              List<Entry> entries)
            throws Exception {
        Result result = makeResult(request, group, entries);
        addLinks(request, result, new State(group, subGroups, entries));
        return result;
    }

    /**
     * _more_
     *
     * @param output _more_
     *
     * @return _more_
     */
    public String getMimeType(OutputType output) {
        if (output.equals(OUTPUT_GALLERY) || output.equals(OUTPUT_PLAYER)
                || output.equals(OUTPUT_SLIDESHOW)) {
            return repository.getMimeTypeFromSuffix(".html");
        }
        return super.getMimeType(output);
    }





    /**
     * _more_
     *
     * @param request _more_
     * @param group _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Result makeResult(Request request, Entry group,
                              List<Entry> entries)
            throws Exception {

        StringBuffer sb     = new StringBuffer();
        OutputType   output = request.getOutput();
        if (entries.size() == 0) {
            sb.append("<b>Nothing Found</b><p>");
            return new Result("Query Results", sb, getMimeType(output));
        }

        if (output.equals(OUTPUT_GALLERY)) {
            sb.append("<table>");
        } else if (output.equals(OUTPUT_PLAYER)) {
            if ( !request.exists(ARG_ASCENDING)) {
                entries = getEntryManager().sortEntriesOnDate(entries, true);
            }
        }

        int    col        = 0;
        String firstImage = "";
        if (output.equals(OUTPUT_PLAYER)) {
            int cnt = 0;
            for (int i = entries.size() - 1; i >= 0; i--) {
                Entry  entry = entries.get(i);
                String url   = getImageUrl(request, entry);
                if (url == null) {
                    continue;
                }
                if (cnt == 0) {
                    firstImage = url;
                }
                String entryUrl = getEntryLink(request, entry);
                String title    =
                    "<table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">";
                title += "<tr><td><b>Image:</b> " + entryUrl
                         + "</td><td align=right>"
                         + new Date(entry.getStartDate());
                title += "</table>";
                title = title.replace("\"", "\\\"");
                sb.append("addImage(" + HtmlUtil.quote(url) + ","
                          + HtmlUtil.quote(title) + ");\n");
                cnt++;
            }
        } else if (output.equals(OUTPUT_SLIDESHOW)) {
            for (int i = entries.size() - 1; i >= 0; i--) {
                Entry entry = entries.get(i);
                if ( !entry.getResource().isImage()) {
                    continue;
                }
                String url = HtmlUtil.url(
                                 request.url(repository.URL_ENTRY_GET) + "/"
                                 + getStorageManager().getFileTail(
                                     entry), ARG_ENTRYID, entry.getId());
                String thumburl = HtmlUtil.url(
                                      request.url(repository.URL_ENTRY_GET)
                                      + "/"
                                      + getStorageManager().getFileTail(
                                          entry), ARG_ENTRYID, entry.getId(),
                                              ARG_IMAGEWIDTH, "" + 100);
                String entryUrl = getEntryLink(request, entry);
                request.put(ARG_OUTPUT, OutputHandler.OUTPUT_HTML);
                String title = entry.getTypeHandler().getEntryContent(entry,
                                   request, true, false).toString();
                request.put(ARG_OUTPUT, output);
                title = title.replace("\"", "\\\"");
                title = title.replace("\n", " ");
                sb.append("addImage(" + HtmlUtil.quote(url) + ","
                          + HtmlUtil.quote(thumburl) + ","
                          + HtmlUtil.quote(title) + ");\n");

            }
        } else {
            int cnt = 0;
            for (Entry entry : entries) {
                String url = getImageUrl(request, entry);
                if (url == null) {
                    continue;
                }
                /*
                if(cnt==0) {
                    sb.append(HtmlUtil.href(url,"View Gallery","  rel=\"shadowbox[gallery]\" "));
                } else {
                    sb.append(HtmlUtil.href(url,entry.getName(),"  rel=\"shadowbox[gallery]\" class=\"hidden\" "));
                }
                cnt++;
                sb.append(HtmlUtil.br());
                if(true)
                    continue;
                */
                if (col >= 2) {
                    sb.append("</tr>");
                    col = 0;
                }
                if (col == 0) {
                    sb.append("<tr valign=\"bottom\">");
                }
                col++;

                sb.append("<td>");
                String imgExtra = "";
                //                String imgExtra = XmlUtil.attr(ARG_WIDTH, "400");
                String imageUrl = url + "&imagewidth="
                                  + request.get(ARG_IMAGEWIDTH, 400);
                sb.append(HtmlUtil.href(url,
                                        (HtmlUtil.img(imageUrl, "",
                                            imgExtra))));
                sb.append("<br>\n");
                sb.append(getEntryLink(request, entry));
                sb.append(" " + new Date(entry.getStartDate()));
                sb.append("<p></td>");
            }
        }


        if (output.equals(OUTPUT_GALLERY)) {
            sb.append("</table>\n");
        } else if (output.equals(OUTPUT_PLAYER)) {
            String playerTemplate =
                repository.getResource(PROP_HTML_IMAGEPLAYER);
            String widthAttr = "";
            int    width     = request.get(ARG_WIDTH, 600);
            if (width > 0) {
                widthAttr = HtmlUtil.attr(HtmlUtil.ATTR_WIDTH, "" + width);
            }
            String imageHtml = "<img name=\"animation\" border=\"0\" "
                               + widthAttr + HtmlUtil.attr("SRC", firstImage)
                               + " alt=\"image\">";

            String tmp = playerTemplate.replace("${imagelist}",
                             sb.toString());
            tmp = tmp.replace("${imagehtml}", imageHtml);
            tmp = StringUtil.replace(tmp, "${root}", repository.getUrlBase());
            String imgstyle = request.getString(ARG_IMAGE_STYLE,
                                  "height: 750px; max-height: 750px;");
            tmp = StringUtil.replace(tmp, "${style}", imgstyle);
            String fullUrl = "";
            if (width > 0) {
                request.put(ARG_WIDTH, "0");
                fullUrl = HtmlUtil.href(request.getUrl(),
                                        msg("Use image width"));
            } else {
                request.put(ARG_WIDTH, "600");
                fullUrl = HtmlUtil.href(request.getUrl(),
                                        msg("Use fixed width"));
            }

            sb  = new StringBuffer();
            sb.append(tmp);
            sb.append(HtmlUtil.leftRight(getSortLinks(request),
                    fullUrl));
        } else if (output.equals(OUTPUT_SLIDESHOW)) {
            String template = repository.getResource(PROP_HTML_SLIDESHOW);
            template = template.replace("${imagelist}", sb.toString());
            template = StringUtil.replace(template, "${root}",
                                          repository.getUrlBase());
            sb = new StringBuffer(template);
        }
        StringBuffer finalSB = new StringBuffer();
        showNext(request, new ArrayList<Entry>(), entries, finalSB);

        finalSB.append(HtmlUtil.p());
        finalSB.append(sb);
        return new Result(group.getName(), finalSB, getMimeType(output));

    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entries _more_
     * @param finalSB _more_
     * @param addHeader _more_
     *
     * @throws Exception _more_
     */
    public void makePlayer(Request request, List<Entry> entries,
                           StringBuffer finalSB, boolean addHeader)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        if (entries.size() == 0) {
            finalSB.append("<b>Nothing Found</b><p>");
            return;
        }

        if ( !request.exists(ARG_ASCENDING)) {
            entries = getEntryManager().sortEntriesOnDate(entries, true);
        }

        int    col        = 0;
        String firstImage = "";

        int    cnt        = 0;
        for (int i = entries.size() - 1; i >= 0; i--) {
            Entry  entry = entries.get(i);
            String url   = getImageUrl(request, entry);
            if (url == null) {
                continue;
            }
            if (cnt == 0) {
                firstImage = url;
            }
            String entryUrl = getEntryLink(request, entry);
            String title    =
                "<table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">";
            title += "<tr><td><b>Image:</b> " + entryUrl
                     + "</td><td align=right>"
                     + new Date(entry.getStartDate());
            title += "</table>";
            title = title.replace("\"", "\\\"");
            sb.append("addImage(" + HtmlUtil.quote(url) + ","
                      + HtmlUtil.quote(title) + ");\n");
            cnt++;
        }

        String playerTemplate = repository.getResource(PROP_HTML_IMAGEPLAYER);
        String widthAttr      = "";
        int    width          = request.get(ARG_WIDTH, 600);
        if (width > 0) {
            widthAttr = HtmlUtil.attr(HtmlUtil.ATTR_WIDTH, "" + width);
        }
        String imageHtml = "<IMG NAME=\"animation\" BORDER=\"0\" "
                           + widthAttr + HtmlUtil.attr("SRC", firstImage)
                           + " ALT=\"image\">";

        String tmp = playerTemplate.replace("${imagelist}", sb.toString());
        tmp = tmp.replace("${imagehtml}", imageHtml);
        tmp = StringUtil.replace(tmp, "${root}", repository.getUrlBase());
        if (addHeader) {
            String fullUrl = "";
            if (width > 0) {
                request.put(ARG_WIDTH, "0");
                fullUrl = HtmlUtil.href(request.getUrl(),
                                        msg("Use image width"));
            } else {
                request.put(ARG_WIDTH, "600");
                fullUrl = HtmlUtil.href(request.getUrl(),
                                        msg("Use fixed width"));
            }
            sb = new StringBuffer(HtmlUtil.leftRight(getSortLinks(request),
                    fullUrl));
        } else {
            sb = new StringBuffer();
        }
        sb.append(tmp);
        finalSB.append(sb);
    }




    /**
     *
     * public void makeSlideshow(Request request, List<Entry> entries,
     *                      StringBuffer finalSB, boolean addHeader)
     *       throws Exception {
     *   StringBuffer sb = new StringBuffer();
     *   if (entries.size() == 0) {
     *       finalSB.append("<b>Nothing Found</b><p>");
     *       return;
     *   }
     *
     *   if ( !request.exists(ARG_ASCENDING)) {
     *       entries = getEntryManager().sortEntriesOnDate(entries, true);
     *   }
     *   finalSB.append(
     *       HtmlUtil.importJS(getRepository().fileUrl("/slides/js/slides.min.jquery.js")));
     *   String slidesTemplate = repository.getResource("ramadda.html.slides");
     *   System.out.println(slidesTemplate);
     *   finalSB.append(slidesTemplate);
     *   for (int i = entries.size() - 1; i >= 0; i--) {
     *       Entry  entry = entries.get(i);
     *       String url   = getImageUrl(request, entry);
     *       if (url == null) {
     *           continue;
     *       }
     *       String entryUrl = getEntryLink(request, entry);
     *       String title = entry.getName();
     *       //            title += "<tr><td><b>Image:</b> " + entryUrl
     *       //                     + "</td><td align=right>"
     *       //                     + new Date(entry.getStartDate());
     *       sb.append("addImage(" + HtmlUtil.quote(url) + ","
     *                 + HtmlUtil.quote(title) + ");\n");
     *       cnt++;
     *   }
     *
     *   String playerTemplate = repository.getResource(PROP_HTML_IMAGEPLAYER);
     *   String widthAttr      = "";
     *   int    width          = request.get(ARG_WIDTH, 600);
     *   if (width > 0) {
     *       widthAttr = HtmlUtil.attr(HtmlUtil.ATTR_WIDTH, "" + width);
     *   }
     *   String imageHtml = "<IMG NAME=\"animation\" BORDER=\"0\" "
     *                      + widthAttr + HtmlUtil.attr("SRC", firstImage)
     *                      + " ALT=\"image\">";
     *
     *   String tmp = playerTemplate.replace("${imagelist}", sb.toString());
     *   tmp = tmp.replace("${imagehtml}", imageHtml);
     *   tmp = StringUtil.replace(tmp, "${root}", repository.getUrlBase());
     *   if (addHeader) {
     *       String fullUrl = "";
     *       if (width > 0) {
     *           request.put(ARG_WIDTH, "0");
     *           fullUrl = HtmlUtil.href(request.getUrl(),
     *                                   msg("Use image width"));
     *       } else {
     *           request.put(ARG_WIDTH, "600");
     *           fullUrl = HtmlUtil.href(request.getUrl(),
     *                                   msg("Use fixed width"));
     *       }
     *       sb = new StringBuffer(HtmlUtil.leftRight(getSortLinks(request),
     *               fullUrl));
     *   } else {
     *       sb = new StringBuffer();
     *   }
     *   sb.append(tmp);
     *   finalSB.append(sb);
     * }
     *
     */


}
