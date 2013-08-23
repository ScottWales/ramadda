/*
* Copyright 2008-2013 Geode Systems LLC
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

package org.ramadda.repository.client;


/**
 *
 * @author xuqing
 */
public class EntryFile {

    /** _more_ */
    public String entryName;

    /** _more_ */
    public String entryDescription;

    /** _more_ */
    public String parent;

    /** _more_ */
    public String filePath;

    /** _more_ */
    public String north = "";

    /** _more_ */
    public String south = "";

    /** _more_ */
    public String west = "";

    /** _more_ */
    public String east = "";

    /**
     * _more_
     *
     * @param entryName _more_
     * @param entryDescription _more_
     * @param parent _more_
     * @param filePath _more_
     */
    public EntryFile(String entryName, String entryDescription,
                     String parent, String filePath) {
        this.entryName        = entryName;
        this.entryDescription = entryDescription;
        this.parent           = parent;
        this.filePath         = filePath;
    }

    /**
     * _more_
     *
     * @param entryName _more_
     * @param entryDescription _more_
     * @param parent _more_
     * @param filePath _more_
     * @param north _more_
     * @param south _more_
     * @param west _more_
     * @param east _more_
     */
    public EntryFile(String entryName, String entryDescription,
                     String parent, String filePath, String north,
                     String south, String west, String east) {
        this.entryName        = entryName;
        this.entryDescription = entryDescription;
        this.parent           = parent;
        this.filePath         = filePath;
        this.east             = east;
        this.west             = west;
        this.north            = north;
        this.south            = south;
    }

    /**
     * _more_
     *
     * @param north _more_
     * @param south _more_
     * @param west _more_
     * @param east _more_
     */
    public void setRange(String north, String south, String west,
                         String east) {
        this.east  = east;
        this.west  = west;
        this.north = north;
        this.south = south;
    }
}
