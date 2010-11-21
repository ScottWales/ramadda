/*
 * 
 * 
 * 
 * 
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

    /** _more_          */
    public boolean open = false;

    /** _more_          */
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

