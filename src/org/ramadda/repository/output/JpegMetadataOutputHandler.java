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

package org.ramadda.repository.output;


import com.drew.imaging.jpeg.*;
import com.drew.lang.*;



import com.drew.metadata.*;
import com.drew.metadata.exif.*;

import org.ramadda.repository.*;


import org.w3c.dom.*;


import org.ramadda.util.HtmlUtils;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;

import java.awt.Image;
import java.awt.image.*;


import java.io.*;

import java.io.File;


import java.net.*;

import java.util.Iterator;
import java.util.List;



/**
 *
 *
 * @author RAMADDA Development Team
 */
public class JpegMetadataOutputHandler extends OutputHandler {


    /** _more_ */
    public static final OutputType OUTPUT_JPEG_METADATA =
        new OutputType("JPEG Metadata", "jpeg.metadata",
                       OutputType.TYPE_VIEW | OutputType.TYPE_FORSEARCH, "",
                       ICON_IMAGES);

    /**
     * _more_
     *
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public JpegMetadataOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_JPEG_METADATA);
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
            String path = state.entry.getResource().getPath().toLowerCase();
            if ( !(path.endsWith(".jpg") || 
                   path.endsWith(".jpeg")||
                   path.endsWith(".tiff"))) {
                    return;
                }
            links.add(makeLink(request, state.getEntry(),
                               OUTPUT_JPEG_METADATA));
        }
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
        File jpegFile = new File(entry.getResource().getPath());
        outputTags(sb, jpegFile,true);
        return new Result("JPEG Metadata", sb);
    }

    private static void outputTags(StringBuffer sb, File jpegFile, boolean forHtml) throws Exception {

        com.drew.metadata.Metadata metadata  = com.drew.imaging.ImageMetadataReader.readMetadata(jpegFile);
        //            JpegMetadataReader.readMetadata(jpegFile);

        if(forHtml) 
            sb.append("<ul>");
        //        java.lang.Iterable<Directory> directories = metadata.getDirectories();
        Iterator directories = metadata.getDirectories().iterator();
        while (directories.hasNext()) {
            Directory directory = (Directory) directories.next();
            if(forHtml) 
                sb.append("<li> ");
            sb.append(directory.getName());
            sb.append("\n");
            if(forHtml) 
                sb.append("<ul>");
            //            Iterator tags = directory.getTagIterator();
            //while (tags.hasNext()) {
            //                Tag tag = (Tag) tags.next();
            for (Tag tag : directory.getTags()) {
                if (tag.getTagName().indexOf("Unknown") >= 0) {
                    continue;
                }
                if(forHtml) 
                    sb.append("<li> ");
                sb.append(tag.getTagName());
                sb.append(":");
                sb.append(tag.getDescription());
                sb.append("\n");
            }
            if(forHtml) 
                sb.append("</ul>");
        }
        if(forHtml) 
            sb.append("</ul>");
    }

    public static void main(String[]args) throws Exception {
        for(String file: args) {
            StringBuffer sb = new StringBuffer();
            outputTags(sb, new File(file),false);
            System.out.println(sb);
        }
    }

}

