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


import org.ramadda.util.HtmlUtils;


/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class EntryLink {

    /** _more_ */
    private String link;

    /** _more_ */
    private String folderBlock;

    /** _more_ */
    private String uid;

    /**
     * _more_
     *
     * @param link _more_
     * @param folderBlock _more_
     * @param uid _more_
     */
    public EntryLink(String link, String folderBlock, String uid) {
        this.link        = link;
        this.folderBlock = folderBlock;
        this.uid         = uid;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return link + HtmlUtils.br() + folderBlock;
    }

    /**
     *  Set the Link property.
     *
     *  @param value The new value for Link
     */
    public void setLink(String value) {
        link = value;
    }

    /**
     *  Get the Link property.
     *
     *  @return The Link
     */
    public String getLink() {
        return link;
    }

    /**
     *  Set the FolderBlock property.
     *
     *  @param value The new value for FolderBlock
     */
    public void setFolderBlock(String value) {
        folderBlock = value;
    }

    /**
     *  Get the FolderBlock property.
     *
     *  @return The FolderBlock
     */
    public String getFolderBlock() {
        return folderBlock;
    }

    /**
     *  Set the Uid property.
     *
     *  @param value The new value for Uid
     */
    public void setUid(String value) {
        uid = value;
    }

    /**
     *  Get the Uid property.
     *
     *  @return The Uid
     */
    public String getUid() {
        return uid;
    }



}
