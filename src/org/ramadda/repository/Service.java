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


/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class Service {

    /** _more_ */
    public static final String TYPE_KML = "kml";

    /** _more_ */
    public static final String TYPE_WMS = "wms";

    /** _more_ */
    public static final String TYPE_GRID = "grid";

    /** _more_ */
    private String type;

    /** _more_ */
    private String name;

    /** _more_ */
    private String url;

    /** _more_ */
    private String icon;

    /** _more_          */
    private String mimeType;


    /**
     * _more_
     *
     * @param type _more_
     * @param name _more_
     * @param url _more_
     */
    public Service(String type, String name, String url) {
        this(type, name, url, null);
    }

    /**
     * _more_
     *
     * @param type _more_
     * @param name _more_
     * @param url _more_
     * @param icon _more_
     */
    public Service(String type, String name, String url, String icon) {
        this(type, name, url, icon, null);
    }

    /**
     * _more_
     *
     * @param type _more_
     * @param name _more_
     * @param url _more_
     * @param icon _more_
     * @param mimeType _more_
     */
    public Service(String type, String name, String url, String icon,
                   String mimeType) {
        this.type     = type;
        this.name     = name;
        this.url      = url;
        this.icon     = icon;
        this.mimeType = mimeType;
    }


    /**
     * _more_
     *
     * @param type _more_
     *
     * @return _more_
     */
    public boolean isType(String type) {
        return this.type.equals(type);
    }

    /**
     *  Set the Type property.
     *
     *  @param value The new value for Type
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     *  Get the Type property.
     *
     *  @return The Type
     */
    public String getType() {
        return this.type;
    }

    /**
     *  Set the Name property.
     *
     *  @param value The new value for Name
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     *  Get the Name property.
     *
     *  @return The Name
     */
    public String getName() {
        return this.name;
    }

    /**
     *  Set the Url property.
     *
     *  @param value The new value for Url
     */
    public void setUrl(String value) {
        this.url = value;
    }

    /**
     *  Get the Url property.
     *
     *  @return The Url
     */
    public String getUrl() {
        return this.url;
    }


    /**
     *  Set the Icon property.
     *
     *  @param value The new value for Icon
     */
    public void setIcon(String value) {
        this.icon = value;
    }

    /**
     *  Get the Icon property.
     *
     *  @return The Icon
     */
    public String getIcon() {
        return this.icon;
    }

    /**
     *  Set the MimeType property.
     *
     *  @param value The new value for MimeType
     */
    public void setMimeType(String value) {
        mimeType = value;
    }

    /**
     *  Get the MimeType property.
     *
     *  @return The MimeType
     */
    public String getMimeType() {
        return mimeType;
    }


}
