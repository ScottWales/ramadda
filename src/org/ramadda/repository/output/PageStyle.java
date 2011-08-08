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

package org.ramadda.repository.output;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;


/**
 *
 *
 */
public class PageStyle {

    /** _more_ */
    public static final String MENU_FILE = "file";

    /** _more_ */
    public static final String MENU_EDIT = "edit";

    /** _more_ */
    public static final String MENU_VIEW = "view";

    /** _more_ */
    public static final String MENU_CONNECT = "connect";

    /** _more_ */
    public static final String MENU_OTHER = "other";


    /** _more_ */
    private String folderWikiTemplate;

    private String fileWikiTemplate;

    /** _more_ */
    private boolean showMenubar = true;

    /** _more_          */
    private boolean showToolbar = true;

    /** _more_ */
    private boolean showBreadcrumbs = true;



    /**
     * _more_
     */
    public PageStyle() {}

    /**
     * _more_
     *
     *
     * @param entry _more_
     * @param menu _more_
     *
     * @return _more_
     */
    public boolean okToShowMenu(Entry entry, String menu) {
        return true;
    }

    /**
     *  Set the WikiTemplate property.
     *
     *  @param value The new value for WikiTemplate
     */
    public void setFolderWikiTemplate(String value) {
        folderWikiTemplate = value;
    }

    public void setFileWikiTemplate(String value) {
        fileWikiTemplate = value;
    }

    /**
     *  Get the WikiTemplate property.
     *
     *
     * @param entry _more_
     *  @return The WikiTemplate
     */
    public String getWikiTemplate(Entry entry) {
        //If its a fake entry (e.g, from search results) then
        //don't use the wiki template
        if(entry.isDummy()) {
            return null;
        }

        if(entry.isGroup()) {
            return folderWikiTemplate;
        }
        return fileWikiTemplate;
    }

    /**
     *  Set the ShowMenubar property.
     *
     *  @param value The new value for ShowMenubar
     */
    public void setShowMenubar(boolean value) {
        showMenubar = value;
    }

    /**
     *  Get the ShowMenubar property.
     *
     *
     * @param entry _more_
     *  @return The ShowMenubar
     */
    public boolean getShowMenubar(Entry entry) {
        //        if(true) return false;
        return showMenubar;
    }


    /**
     *  Set the ShowToolbar property.
     *
     *  @param value The new value for ShowToolbar
     */
    public void setShowToolbar(boolean value) {
        showToolbar = value;
    }

    /**
     *  Get the ShowToolbar property.
     *
     *
     * @param entry _more_
     *  @return The ShowToolbar
     */
    public boolean getShowToolbar(Entry entry) {
        //        if(true) return false;
        return showToolbar;
    }

    /**
     *  Set the ShowBreadcrumbs property.
     *
     *  @param value The new value for ShowBreadcrumbs
     */
    public void setShowBreadcrumbs(boolean value) {
        showBreadcrumbs = value;
    }

    /**
     *  Get the ShowBreadcrumbs property.
     *
     *
     * @param entry _more_
     *  @return The ShowBreadcrumbs
     */
    public boolean getShowBreadcrumbs(Entry entry) {
        //        if(true) return false;
        return showBreadcrumbs;
    }



}
