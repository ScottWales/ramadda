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

package com.infocetera.tableview;


import java.awt.*;


/**
 * Class TableRow _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class TableRow extends Rectangle {

    /** _more_ */
    public boolean open = false;

    /** _more_ */
    public Object[] cols;

    /**
     * _more_
     *
     * @param col _more_
     *
     * @return _more_
     */
    public Object getColumnValue(int col) {
        return cols[col];
    }

    /**
     * _more_
     *
     * @param g _more_
     * @param hilite _more_
     * @param hiliteColumn _more_
     * @param columnWidth _more_
     */
    public void paint(Graphics g, TableRow hilite, int hiliteColumn,
                      int columnWidth) {
        boolean isHighlight = (this == hilite);
        int     myWidth     = columnWidth - 1;
        int     myHeight    = height - 1;
        int     crntX       = x;
        g.setColor(Color.black);
        g.drawLine(x, y + myHeight, x + width, y + myHeight);
        for (int c = 0; c < cols.length; c++) {
            g.setColor(Color.black);
            g.drawLine(crntX, y, crntX, y + height);
            if (isHighlight && (hiliteColumn == c)) {
                g.setColor(Color.yellow);
                g.drawRect(crntX + 1, y, myWidth - 1, myHeight - 1);
            }
            if (open) {
                g.setColor(Color.black);
                g.drawString(cols[c].toString(), crntX + 2, y + myHeight - 2);
            }
            crntX += columnWidth;


        }
    }



}
