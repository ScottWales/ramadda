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

package com.infocetera.chart;


import com.infocetera.util.*;

import java.applet.Applet;


import java.awt.*;


import java.util.Hashtable;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;



/**
 * Class ChartCanvas _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class ChartCanvas extends Canvas {

    /** _more_          */
    public ChartApplet myApplet;

    /** _more_          */
    Component legend;

    /** _more_          */
    Component leftAxis;

    /** _more_          */
    static Font pieFont = new Font("Dialog", Font.BOLD, 8);

    /** _more_          */
    static Font labelFont = new Font("Dialog", Font.BOLD, 16);

    /**
     * _more_
     *
     * @param applet _more_
     */
    public ChartCanvas(ChartApplet applet) {
        myApplet = applet;
        Vector  colors     = new Vector();
        Color[] baseColors = {
            new Color(204, 102, 204), new Color(153, 51, 102), Color.blue,
            new Color(0, 153, 0), Color.green, new Color(155, 253, 155),
            Color.yellow, new Color(255, 204, 153), Color.orange,
            new Color(204, 0, 0), Color.red
        };


        for (int i = 0; i < baseColors.length; i++) {
            colors.addElement(baseColors[i].darker());
            colors.addElement(baseColors[i]);
            //      colors.addElement (baseColors[i].brighter());
        }

        rainbow = new Color[colors.size()];
        for (int i = 0; i < colors.size(); i++) {
            rainbow[i] = (Color) colors.elementAt(i);
        }
        rainbow = baseColors;
    }

    /** _more_          */
    public Image chartImage = null;

    /** _more_          */
    public int chartWidth;

    /** _more_          */
    public int chartHeight;

    /** _more_          */
    public Image bufferedImage = null;

    /** _more_          */
    public int bufferWidth;

    /** _more_          */
    public int bufferHeight;


    /** _more_          */
    public Vector searchRectList;

    /** _more_          */
    public Vector displayRectList;

    /** _more_          */
    public Vector dataList;

    /** _more_          */
    public Rectangle hilite;

    /** _more_          */
    public int hiliteIdx = -1;

    /** _more_          */
    public boolean justRepaintHilite = false;



    /** _more_          */
    public boolean forPrint = false;

    /** _more_          */
    public static final int ROW_CENTRIC = 0;

    /** _more_          */
    public static final int COL_CENTRIC = 1;

    /**
     * _more_
     *
     * @param centricValue _more_
     *
     * @return _more_
     */
    public static boolean isRowCentric(int centricValue) {
        return (centricValue == ROW_CENTRIC);
    }

    /**
     * _more_
     *
     * @param centricValue _more_
     *
     * @return _more_
     */
    public static boolean isColCentric(int centricValue) {
        return (centricValue == COL_CENTRIC);
    }

    /**
     * _more_
     *
     * @param centric _more_
     *
     * @return _more_
     */
    public int getNumMajorElements(int centric) {
        if (isColCentric(centric)) {
            return numDataCols;
        }
        return numDataRows;
    }

    /**
     * _more_
     *
     * @param centric _more_
     *
     * @return _more_
     */
    public int getNumMinorElements(int centric) {
        if ( !isColCentric(centric)) {
            return numDataCols;
        }
        return numDataRows;
    }

    /**
     * _more_
     *
     * @param major _more_
     * @param minor _more_
     * @param centric _more_
     *
     * @return _more_
     */
    public double getData(int major, int minor, int centric) {
        if (isRowCentric(centric)) {
            return data[major][minor];
        } else {
            return data[minor][major];
        }
    }



    /**
     * _more_
     *
     * @param major _more_
     * @param centric _more_
     * @param value _more_
     *
     * @return _more_
     */
    public double getMajorPercentage(int major, int centric, double value) {
        double min;
        double max;


        if (true || myApplet.getColOrRowRelative()) {
            if (isRowCentric(centric)) {
                min = minRow[major];
                max = maxRow[major];
            } else {
                min = minCol[major];
                max = maxCol[major];
            }
        } else {
            min = minValue;
            max = maxValue;
        }

        double diff = (max - min);
        if (diff == 0.0) {
            return 1.0;
        }
        double total = getTotal(major, centric);
        if (total != 0.0) {
            return (value) / total;
        }
        return 1.0;


    }

    /**
     * _more_
     *
     * @param major _more_
     * @param centric _more_
     *
     * @return _more_
     */
    public double getTotal(int major, int centric) {
        if (isRowCentric(centric)) {
            return rowTotal[major];
        } else {
            return colTotal[major];
        }
    }

    /**
     * _more_
     *
     * @param minor _more_
     * @param centric _more_
     *
     * @return _more_
     */
    public String getMinorLabel(int minor, int centric) {
        if (isRowCentric(centric)) {
            return colHeaders[minor];
        } else {
            return rowHeaders[minor];
        }
    }

    /**
     * _more_
     *
     * @param major _more_
     * @param centric _more_
     *
     * @return _more_
     */
    public String getMajorLabel(int major, int centric) {
        if (isRowCentric(centric)) {
            return rowHeaders[major];
        } else {
            return colHeaders[major];

        }
    }


    /** _more_          */
    public static final int CHART_BAR_ROW = 0;

    /** _more_          */
    public static final int CHART_BAR_COL = 1;

    /** _more_          */
    public static final int CHART_LINE_ROW = 2;

    /** _more_          */
    public static final int CHART_LINE_COL = 3;

    /** _more_          */
    public static final int CHART_PIE_ROW = 4;

    /** _more_          */
    public static final int CHART_PIE_COL = 5;

    /** _more_          */
    public static final int CHART_KIVIAT_ROW = 6;

    /** _more_          */
    public static final int CHART_KIVIAT_COL = 7;

    /** _more_          */
    public static final int CHART_SCATTER_ROW = 8;

    /** _more_          */
    public static final int CHART_SCATTER_COL = 9;

    /** _more_          */
    public static final int CHART_STACK_ROW = 10;

    /** _more_          */
    public static final int CHART_STACK_COL = 11;

    /** _more_          */
    public static final int CHART_GRADIENT_GRAY = 12;

    /** _more_          */
    public static final int CHART_GRADIENT_COLOR = 13;

    /** _more_          */
    public static final int CHART_DATAGRID = 14;

    /**
     * _more_
     *
     * @param chartType _more_
     *
     * @return _more_
     */
    public int getCentric(int chartType) {
        if ((chartType / 2) * 2 == chartType) {
            return ROW_CENTRIC;
        }
        return COL_CENTRIC;
    }

    /** _more_          */
    public static final String[] chartNames = {
        "Grid", "Bar chart - rows", "Bar chart - columns",
        "Stacked bars - rows", "Stacked bars - columns", "Line chart - rows",
        "Line chart - columns", "Pie chart - rows", "Pie chart - columns",
        "Gray scale  gradient", "Color scale gradient",
        "Kiviat diagram - rows", "Kiviat diagram - cols"
        //,
        //    "Scatter plot - rows",
        //    "Scatter plot - cols",
    };

    /** _more_          */
    public static final int[] chartTypes = {
        CHART_DATAGRID, CHART_BAR_ROW, CHART_BAR_COL, CHART_STACK_ROW,
        CHART_STACK_COL, CHART_LINE_ROW, CHART_LINE_COL, CHART_PIE_ROW,
        CHART_PIE_COL, CHART_GRADIENT_GRAY, CHART_GRADIENT_COLOR,
        CHART_KIVIAT_ROW, CHART_KIVIAT_COL, CHART_SCATTER_ROW,
        CHART_SCATTER_COL
    };


    /** _more_          */
    public static final String[] chartKeys = {
        "DATAGRID", "BAR_ROW", "BAR_COL", "STACK_ROW", "STACK_COL",
        "LINE_ROW", "LINE_COL", "PIE_ROW", "PIE_COL", "GRADIENT_GRAY",
        "GRADIENT_COLOR", "KIVIAT_ROW", "KIVIAT_COL"
    };








    /** _more_          */
    public int chartType = CHART_LINE_ROW;

    /** _more_          */
    public Color grayGradient[];

    /** _more_          */
    public Color indexedColors[];

    /** _more_          */
    public Color[] rainbow = null;



    /** _more_          */
    public Color[] colors = {
        Color.blue, Color.red, Color.green, Color.cyan, Color.magenta,
        Color.orange, Color.pink, Color.yellow
    };

    /** _more_          */
    public Color[] darker = {
        Color.blue.darker(), Color.red.darker(), Color.green.darker(),
        Color.cyan.darker(), Color.magenta.darker(), Color.orange.darker(),
        Color.pink.darker(), Color.yellow.darker()
    };

    /** _more_          */
    public String entries[][];

    /** _more_          */
    public String rowHeaders[];

    /** _more_          */
    public String colHeaders[];

    /** _more_          */
    public double data[][];

    /** _more_          */
    double[] rowTotal;

    /** _more_          */
    double[] colTotal;

    /** _more_          */
    public int numRows;

    /** _more_          */
    public int numCols;

    /** _more_          */
    public int numDataRows;

    /** _more_          */
    public int numDataCols;

    /** _more_          */
    public double maxAxisValue = 0.0;

    /** _more_          */
    public int numAxisTicks = 14;

    /** _more_          */
    public double maxValue = -100000.0;

    /** _more_          */
    public double minValue = 100000.0;

    /** _more_          */
    public double maxColSum = 0.0;

    /** _more_          */
    public double maxRowSum = 0.0;

    /** _more_          */
    public double[] sumRow;

    /** _more_          */
    public double[] sumCol;

    /** _more_          */
    public double[] maxRow;

    /** _more_          */
    public double[] minRow;

    /** _more_          */
    public double[] maxCol;

    /** _more_          */
    public double[] minCol;



    /** _more_          */
    String xLabel = "";

    /** _more_          */
    String yLabel = "";


    /**
     * _more_
     *
     * @param newType _more_
     */
    public void setChartType(int newType) {
        chartType = newType;
        legend.repaint();
        //    leftAxis.repaint ();
        repaint();
    }



    /**
     * _more_
     */
    public void readParams() {

        int    colorStep = 0;
        double stepPerIdx;

        grayGradient = new Color[20];
        stepPerIdx   = 255.0 / 20.0;
        for (int i = 0; i < 20; i++) {
            double num = (double) (20 - i);
            int    idx = (int) (num * stepPerIdx + 0.5);
            grayGradient[i] = new Color(idx, idx, idx);
        }


        String tmp = myApplet.getParameter("xlabel");
        if (tmp != null) {
            xLabel = tmp;
        }
        tmp = myApplet.getParameter("ylabel");
        if (tmp != null) {
            yLabel = tmp;
        }
        tmp = myApplet.getParameter("forprint");
        if (tmp != null) {
            int value = Integer.decode(tmp).intValue();
            forPrint = (value == 1);
        }
        tmp = myApplet.getParameter("charttype");
        if (tmp != null) {
            for (int i = 0; i < chartKeys.length; i++) {
                if (chartKeys[i].equals(tmp)) {
                    chartType = chartTypes[i];
                    break;
                }
            }
        }









        numRows = 0;
        numCols = 0;
        Vector rows = new Vector();
        while (true) {
            String rowParam = myApplet.getParameter("ROW" + (numRows + 1));
            if (rowParam == null) {
                break;
            }
            numRows++;
            Vector cols     = GuiUtils.split(rowParam, new String[] { "," });
            Vector entryRow = new Vector();
            rows.addElement(entryRow);
            if (cols.size() > numCols) {
                numCols = cols.size();
            }
            for (int col = 0; col < cols.size(); col++) {
                String entry = (String) cols.elementAt(col);
                if (entry.equals("BLANK")) {
                    entry = "";
                }
                entry = GU.replace(entry, "_COMMA_", ",");
                entryRow.addElement(entry);
            }
        }



        entries = new String[numRows][numCols];
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                entries[row][col] = "";
            }
        }


        for (int row = 0; row < rows.size(); row++) {
            Vector entryRow = (Vector) rows.elementAt(row);
            for (int col = 0; col < entryRow.size(); col++) {
                entries[row][col] = (String) entryRow.elementAt(col);
            }
        }


        int firstNumericCol = -1;
        int firstNumericRow = -1;


        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                String entry = entries[row][col];
                entry = entry.trim();
                if (entry.equals("")) {
                    continue;
                }
                try {
                    if (firstNumericCol < 0) {
                        Double.valueOf(entry);
                        firstNumericCol = col;
                        firstNumericRow = row;
                    }
                } catch (Exception e) {}
            }
            if (firstNumericCol >= 0) {
                break;
            }
        }



        data        = new double[numRows][numCols];

        numDataRows = numRows - firstNumericRow;
        numDataCols = numCols - firstNumericCol;

        sumRow      = new double[numDataRows];
        maxRow      = new double[numDataRows];
        minRow      = new double[numDataRows];

        sumCol      = new double[numDataCols];
        maxCol      = new double[numDataCols];
        minCol      = new double[numDataCols];



        for (int row = firstNumericRow; row < numRows; row++) {
            for (int col = firstNumericCol; col < numCols; col++) {
                try {
                    double value =
                        Double.valueOf(entries[row][col]).doubleValue();
                    data[row - firstNumericRow][col - firstNumericCol] =
                        value;
                    if ((value > maxValue)
                            || ((row == firstNumericRow)
                                && (col == firstNumericCol))) {
                        maxValue = value;
                    }
                    if ((value < minValue)
                            || ((row == firstNumericRow)
                                && (col == firstNumericCol))) {
                        minValue = value;
                    }

                } catch (Exception e) {
                    data[row - firstNumericRow][col - firstNumericCol] = 0;
                }
            }
        }


        for (int row = 0; row < numDataRows; row++) {
            sumRow[row] = 0.0;
            for (int col = 0; col < numDataCols; col++) {
                if (row == 0) {
                    sumCol[col] = 0.0;
                    maxCol[col] = data[row][col];
                    minCol[col] = data[row][col];
                } else {
                    if (data[row][col] > maxCol[col]) {
                        maxCol[col] = data[row][col];
                    }
                    if (data[row][col] < minCol[col]) {
                        minCol[col] = data[row][col];
                    }
                }

                sumRow[row] += data[row][col];
                sumCol[col] += data[row][col];


                if (col == 0) {
                    maxRow[row] = data[row][col];
                    minRow[row] = data[row][col];
                } else {
                    if (data[row][col] > maxRow[row]) {
                        maxRow[row] = data[row][col];
                    }
                    if (data[row][col] < minRow[row]) {
                        minRow[row] = data[row][col];
                    }
                }
            }
        }


        for (int row = 0; row < numDataRows; row++) {
            if (row == 0) {
                maxRowSum = 0.0;
            } else if (sumRow[row] > maxRowSum) {
                maxRowSum = sumRow[row];
            }
        }

        for (int col = 0; col < numDataCols; col++) {
            if (col == 0) {
                maxColSum = 0.0;
            } else if (sumCol[col] > maxColSum) {
                maxColSum = sumCol[col];
            }
        }


        maxAxisValue = (maxValue + (numAxisTicks - maxValue % numAxisTicks));




        rowHeaders   = new String[numDataRows];
        colHeaders   = new String[numDataCols];


        if (firstNumericCol == 0) {
            for (int i = 0; i < numDataRows; i++) {
                rowHeaders[i] = "Row " + (i + 1);
            }
        } else {
            for (int i = 0; i < numDataRows; i++) {
                rowHeaders[i] =
                    entries[i + firstNumericRow][firstNumericCol - 1];
            }
        }


        if (firstNumericRow == 0) {
            for (int i = 0; i < numDataCols; i++) {
                colHeaders[i] = "Column " + (i + 1);
            }
        } else {
            for (int i = 0; i < numDataCols; i++) {
                colHeaders[i] =
                    entries[firstNumericRow - 1][i + firstNumericCol];
            }
        }





        rowTotal = new double[numDataRows];
        colTotal = new double[numDataCols];
        for (int row = 0; row < numDataRows; row++) {
            rowTotal[row] = 0.0;
            for (int col = 0; col < numDataCols; col++) {
                rowTotal[row] += data[row][col];
            }
        }

        for (int col = 0; col < numDataCols; col++) {
            colTotal[col] = 0.0;
            for (int row = 0; row < numDataRows; row++) {
                colTotal[col] += data[row][col];
            }
        }

    }


    /** _more_          */
    public static final GuiUtils GU = null;


    /**
     * _more_
     *
     * @param e _more_
     * @param x _more_
     * @param y _more_
     *
     * @return _more_
     */
    public boolean mouseMove(Event e, int x, int y) {
        if ((myApplet == null) || (searchRectList == null)
                || (dataList == null)) {
            return true;
        }

        Rectangle oldHilite = hilite;
        hilite    = null;
        hiliteIdx = -1;
        for (int i = searchRectList.size() - 1; i >= 0; i--) {
            Rectangle r = (Rectangle) searchRectList.elementAt(i);
            if (r.contains(x, y)) {
                hiliteIdx = i;
                hilite    = (Rectangle) displayRectList.elementAt(i);
                break;
            }
        }



        if (hilite == oldHilite) {
            return true;
        }


        boolean hiliteChanged = false;
        if ((oldHilite != hilite) && (oldHilite != null)) {
            hiliteChanged = true;
        }



        if (hilite != null) {
            justRepaintHilite = true;
            Point rc = (Point) dataList.elementAt(hiliteIdx);
            if ((rc.y >= 0) && (rc.x >= 0)) {
                String hdr = "";
                hdr = entries[rc.x][0] + "-" + entries[0][rc.y];
                String rowCol = "Row:" + rc.x + " Col:" + rc.y;
                if (myApplet.label != null) {
                    myApplet.label.setText(hdr + " Value:"
                                           + entries[rc.x][rc.y]);
                }
            }
            hiliteChanged = true;
        } else if (hiliteChanged) {
            if (myApplet.label != null) {
                myApplet.label.setText("");
            }
        }





        if (hiliteChanged) {
            repaint();
        }
        return true;
    }

    /**
     * _more_
     *
     * @param e _more_
     * @param x _more_
     * @param y _more_
     *
     * @return _more_
     */
    public boolean mouseUp(Event e, int x, int y) {
        if (hilite != null) {
            Point rc = (Point) dataList.elementAt(hiliteIdx);
            if (myApplet.clickUrl != null) {
                String url = GU.replace(myApplet.clickUrl, "%ROW%",
                                        "" + rc.x);
                url = GU.replace(url, "%COL%", "" + rc.y);
                url = GU.replace(url, "%HTTP%", myApplet.getUrlPrefix());
                myApplet.showUrl(url, "CHARTCLICK");
            }
            return true;
        }
        return false;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public Component doMakeContents() {
        setBackground(Color.white);
        Panel contents = new Panel();
        contents.setLayout(new BorderLayout());
        contents.add("Center", this);
        legend = new Component() {
            public void paint(Graphics g) {
                paintLegend(g);
            }
            public Dimension getMinimumSize() {
                return new Dimension(200, 100);
            }
            public Dimension getSize() {
                return new Dimension(200, 500);
            }
        };

        /*    leftAxis = new Component () {
              public void paint (Graphics g) {
                paintAxis (g);
              }
            };
        */

        Component right = GU.doLayout(new Component[] {
                              GU.inset(legend, 2, 2),
                              new Label("                          ") }, 1,
                                  GU.DS_Y, GU.DS_YN);
        contents.add("East", right);
        if (leftAxis != null) {
            Component left = GU.doLayout(new Component[] {
                                 GU.inset(leftAxis, 2, 2),
                                 new Label(
                                     "                          ") }, 1,
                                         GU.DS_Y, GU.DS_YN);
            contents.add("West", left);
        }


        return contents;
    }




    /** _more_          */
    public static final int BASE3D_DIMENSION = 8;

    /** _more_          */
    int threeD1 = BASE3D_DIMENSION;

    /** _more_          */
    int threeD2 = BASE3D_DIMENSION;


    /** _more_          */
    int leftMargin = 50;

    /** _more_          */
    int rightMargin = 10;

    /** _more_          */
    int topMargin = 10;

    /** _more_          */
    int bottomMargin = 50;

    /** _more_          */
    int displayWidth;

    /** _more_          */
    int displayHeight;

    /** _more_          */
    int xOrigin;

    /** _more_          */
    int yOrigin;

    /** _more_          */
    int xExtent;

    /** _more_          */
    int yExtent;

    /** _more_          */
    double doubleDisplayHeight;

    /** _more_          */
    double scale;

    /**
     * _more_
     *
     * @param g _more_
     */
    public void initBounds(Graphics g) {
        Rectangle b = bounds();
        displayWidth  = b.width - leftMargin - rightMargin;
        displayHeight = b.height - topMargin - bottomMargin;
        xOrigin       = leftMargin;
        yOrigin       = b.height - bottomMargin;
        xExtent       = xOrigin + displayWidth;
        yExtent       = topMargin + displayWidth;
        double dDisplayHeight = (double) displayHeight;
        double range          = maxAxisValue;

        if (minValue < 0.0) {
            range = maxAxisValue - minValue;
        }

        if (range != 0.0) {
            scale = dDisplayHeight / range;
        } else {
            scale = 1.0;
        }
    }



    /**
     * _more_
     *
     * @param g _more_
     * @param centric _more_
     */
    public void printAxisLabels(Graphics g, int centric) {
        String theYLabel = yLabel;
        String theXLabel = xLabel;
        if (isColCentric(centric)) {
            theYLabel = xLabel;
            theXLabel = yLabel;
        }


        Font oldFont = g.getFont();
        g.setFont(labelFont);
        FontMetrics fm              = g.getFontMetrics();
        int         labelHeight     = fm.getMaxDescent() + fm.getMaxAscent();
        int         halfLabelHeight = (int) ((double) fm.getMaxAscent()
                                             / 2.0);
        int         width           = fm.stringWidth(theXLabel);
        g.setColor(Color.black);
        g.drawString(theXLabel, leftMargin + displayWidth / 2 - width / 2,
                     yOrigin + 40);


        int yLabelLength = theYLabel.length();
        for (int idx = 0; idx < yLabelLength; idx++) {
            char[] c = { theYLabel.charAt(idx) };
            g.drawChars(c, 0, 1, 4, topMargin + labelHeight * (idx + 1));
        }


        g.setFont(oldFont);

    }




    /**
     * _more_
     *
     * @param g _more_
     */
    public synchronized void update(Graphics g) {
        Rectangle b = bounds();
        if ((bufferedImage == null) || (bufferWidth != b.width)
                || (bufferHeight != b.height)) {
            if ((b.width <= 0) || (b.height <= 0)) {
                return;
            }
            bufferWidth       = b.width;
            bufferHeight      = b.height;
            bufferedImage     = createImage(b.width, b.height);
            justRepaintHilite = false;
        }

        if ( !justRepaintHilite) {
            Graphics bufferedGraphics = bufferedImage.getGraphics();
            bufferedGraphics.setColor(Color.white);
            bufferedGraphics.fillRect(0, 0, bufferWidth + 4,
                                      bufferHeight + 4);
            paint(bufferedGraphics);
        }

        g.drawImage(bufferedImage, 0, 0, Color.white, null);
        justRepaintHilite = false;

        if (hilite != null) {
            g.setColor(Color.yellow);
            g.drawRect(hilite.x, hilite.y, hilite.width, hilite.height);
        }
    }


    /**
     * _more_
     *
     * @param g _more_
     */
    public void paintAxis(Graphics g) {
        switch (chartType) {

          case CHART_LINE_ROW :
          case CHART_LINE_COL :
          case CHART_BAR_ROW :
          case CHART_BAR_COL :
          case CHART_STACK_ROW :
          case CHART_STACK_COL : {
              printChartAxis(g, bounds());
          }
        }

    }



    /**
     * _more_
     *
     * @param g _more_
     */
    public void paintLegend(Graphics g) {
        g.setFont(pieFont);
        int centric = getCentric(chartType);


        switch (chartType) {

          case CHART_LINE_ROW : {
              printMajorColorKey(g, ROW_CENTRIC);
              return;
          }

          case CHART_LINE_COL : {
              printMajorColorKey(g, centric);
              return;
          }

          case CHART_GRADIENT_GRAY : {
              printGradientKey(g, grayGradient);
              return;
          }

          case CHART_GRADIENT_COLOR : {
              printGradientKey(g, rainbow);
              return;
          }

          case CHART_SCATTER_ROW :
          case CHART_SCATTER_COL :
          case CHART_DATAGRID : {
              return;
          }



          case CHART_BAR_ROW :
          case CHART_BAR_COL :
          case CHART_STACK_ROW :
          case CHART_STACK_COL :
          case CHART_PIE_ROW :
          case CHART_PIE_COL :
          case CHART_KIVIAT_ROW :
          case CHART_KIVIAT_COL : {
              printMinorColorKey(g, centric);
              return;
          }

          default : {
              chartType = CHART_BAR_ROW;
              printMinorColorKey(g, ROW_CENTRIC);
          }
        }

    }


    /**
     * _more_
     *
     * @param g _more_
     */
    public void paint(Graphics g) {
        //    if (true) return;
        searchRectList  = new Vector();
        displayRectList = new Vector();
        dataList        = new Vector();

        Rectangle b = bounds();
        g.setColor(Color.white);
        g.fillRect(0, 0, b.width + 2, b.height + 2);
        initBounds(g);


        if ((chartImage == null) || (chartWidth != b.width)
                || (chartHeight != b.height)) {
            if ((b.width <= 0) || (b.height <= 0)) {
                return;
            }
            chartWidth  = b.width;
            chartHeight = b.height;
            chartImage  = createImage(b.width * 10, b.height);
        }

        Graphics chartGraphics = chartImage.getGraphics();
        chartGraphics.setColor(Color.white);
        chartGraphics.fillRect(0, 0, chartWidth + 4, chartHeight + 4);
        chartGraphics.translate(hTrans, vTrans);
        paintChart(chartGraphics);
        g.drawImage(chartImage, 0, 0, Color.white, null);
    }

    /** _more_          */
    int hTrans = 0;

    /** _more_          */
    int vTrans = 0;



    /**
     * _more_
     *
     * @param g _more_
     */
    public void paintChart(Graphics g) {

        switch (chartType) {

          case CHART_STACK_ROW :
          case CHART_STACK_COL : {
              printBarChart(g, getCentric(chartType), true);
              return;
          }

          case CHART_LINE_ROW :
          case CHART_LINE_COL : {
              printLineChart(g, getCentric(chartType));
              return;
          }

          case CHART_PIE_ROW :
          case CHART_PIE_COL : {
              printPieChart(g, getCentric(chartType));
              return;
          }

          case CHART_GRADIENT_GRAY : {
              printColorGradient(g, grayGradient);
              return;
          }

          case CHART_GRADIENT_COLOR : {
              printColorGradient(g, rainbow);
              return;
          }

          case CHART_KIVIAT_ROW : {
              printKiviat(g, ROW_CENTRIC);
              return;
          }

          case CHART_KIVIAT_COL : {
              printKiviat(g, COL_CENTRIC);
              return;
          }

          case CHART_SCATTER_COL :
          case CHART_SCATTER_ROW : {
              printScatterChart(g, getCentric(chartType));
              return;
          }

          case CHART_DATAGRID : {
              printDataGrid(g);
              return;
          }


          case CHART_BAR_ROW :
          case CHART_BAR_COL : {
              printBarChart(g, getCentric(chartType), false);
              return;
          }

          default : {
              chartType = CHART_BAR_ROW;
              printBarChart(g, getCentric(chartType), false);
          }
        }
    }


    /**
     * _more_
     *
     * @param g _more_
     * @param b _more_
     */
    public void printChartAxis(Graphics g, Rectangle b) {
        //    if (threeD1 == 0) return;

        FontMetrics fm              = g.getFontMetrics();
        int         labelHeight     = fm.getMaxDescent() + fm.getMaxAscent();
        int         halfLabelHeight = (int) ((double) fm.getMaxAscent()
                                             / 2.0);
        int         leftX           = xOrigin;

        g.setColor(Color.black);
        g.drawRect(leftX + threeD2, topMargin - threeD1,
                   displayWidth - threeD2, displayHeight + 1);
        int cnt = 0;
        for (int axisY = yOrigin; axisY >= topMargin; axisY -= 20) {
            double value = ((yOrigin - axisY) / scale + 0.5);
            String label = "" + ((int) value);
            int    width = fm.stringWidth(label) + 4;
            g.drawString(label, leftX - width, axisY + halfLabelHeight);
            g.drawLine(leftX, axisY, leftX - 2, axisY);
            cnt++;
            if (cnt % 4 == 0) {
                g.setColor(Color.lightGray);
                g.drawLine(leftX, axisY - threeD2 + 1, leftX + displayWidth,
                           axisY - threeD2 + 1);
                g.setColor(Color.black);
            }
        }

        int[] axisX = { leftX + threeD2, leftX + displayWidth,
                        leftX + displayWidth - threeD2, leftX };
        int[] axisY = { yOrigin - threeD2, yOrigin - threeD2, yOrigin,
                        yOrigin };

        int[] axisX2 = { leftX, leftX, leftX + threeD2, leftX + threeD2 };
        int[] axisY2 = { yOrigin, topMargin, topMargin - threeD1,
                         yOrigin - threeD1 };

        g.setColor(Color.gray);
        g.fillPolygon(axisX, axisY, axisX.length);
        g.fillPolygon(axisX2, axisY2, axisX2.length);

        g.setColor(Color.black);
        GuiUtils.drawPolyLine(g, axisX, axisY);
        GuiUtils.drawPolyLine(g, axisX2, axisY2);
    }


    /**
     * _more_
     *
     * @param centric _more_
     *
     * @return _more_
     */
    public double getMax(int centric) {
        return ((centric == ROW_CENTRIC)
                ? maxRowSum
                : maxColSum);
    }


    /**
     * _more_
     *
     * @param g _more_
     * @param centric _more_
     * @param doStack _more_
     */
    public void printBarChart(Graphics g, int centric, boolean doStack) {

        int currentX         = leftMargin;
        int numMajorElements = getNumMajorElements(centric);
        int numMinorElements = getNumMinorElements(centric);
        int boxWidth = (displayWidth - threeD1)
                       / (numMajorElements * (numMinorElements + 1));


        if (doStack) {
            double dDisplayHeight = (double) displayHeight;
            boxWidth = (displayWidth - threeD1) / (numMajorElements);
            if ((getMax(centric) <= dDisplayHeight) && (maxRowSum != 0.0)) {
                scale = dDisplayHeight / getMax(centric);
            } else {
                scale = dDisplayHeight / getMax(centric);
            }
        }
        int extraPer   = 0;
        int extraExtra = 0;
        int boxSpan    = 0;

        if ( !doStack) {
            boxSpan = numMajorElements * boxWidth
                      + (numMajorElements * numMinorElements * boxWidth);
            if (numMajorElements > 1) {
                int totalExtra = (int) ((displayWidth - BASE3D_DIMENSION)
                                        - boxSpan);
                extraPer   = totalExtra / (numMajorElements - 1);
                extraExtra = totalExtra - ((numMajorElements - 1) * extraPer);
                //      System.err.println ("total:" + totalExtra + " extraPer:" + extraPer + " " + extraExtra);
            }

        }


        printAxisLabels(g, centric);
        Rectangle b = bounds();
        printChartAxis(g, b);

        FontMetrics fm          = g.getFontMetrics();
        int         labelHeight = fm.getMaxDescent() + fm.getMaxAscent();


        //    printXLabels (g,  centric, boxWidth);


        for (int major = 0; major < numMajorElements; major++) {
            int currentY = yOrigin;
            int startX   = currentX;
            for (int minor = 0; minor < numMinorElements; minor++) {
                double value  = getData(major, minor, centric);
                int    height = (int) (scale * (value));
                int    y      = currentY - height;
                Color  c      = colors[minor % colors.length];
                g.setColor(c);
                g.fillRect(currentX, y, boxWidth, height);
                addRect(new Rectangle(currentX, y, boxWidth + threeD1,
                                      height), new Rectangle(currentX, y,
                                          boxWidth, height), major, minor,
                                              centric);

                int[] xs = { currentX, currentX + threeD1,
                             currentX + boxWidth + threeD1,
                             currentX + boxWidth };
                int[] ys = { y, y - threeD2, y - threeD2, y };
                g.fillPolygon(xs, ys, xs.length);
                int[] xs2 = { currentX + boxWidth,
                              currentX + boxWidth + threeD1,
                              currentX + boxWidth + threeD1,
                              currentX + boxWidth };
                int[] ys2 = { y, y - threeD2, y + height - threeD2,
                              y + height };
                g.setColor(c.darker());
                g.fillPolygon(xs2, ys2, xs2.length);

                g.setColor(Color.black);
                GuiUtils.drawPolyLine(g, xs, ys);
                GuiUtils.drawPolyLine(g, xs2, ys2);

                g.drawRect(currentX, y, boxWidth, height);
                if (doStack) {
                    currentY -= height;
                } else {
                    currentX += boxWidth;
                }

            }

            String label   = getMajorLabel(major, centric);
            int    centerX = startX + (numMinorElements * boxWidth) / 2;
            if (doStack) {
                centerX = currentX + boxWidth / 2;
            }
            g.setColor(Color.black);
            boolean stringOk = true;
            if (numMajorElements > 25) {
                stringOk = ((major % 5) == 0);
            }

            int y = yOrigin + 8;
            if (stringOk) {
                printLabel(g, label, startX, yOrigin + labelHeight,
                           labelHeight);
                if ( !doStack) {
                    g.setColor(Color.gray);
                    g.drawLine(currentX + threeD1, yOrigin - threeD2,
                               currentX + threeD1,
                               2 + (BASE3D_DIMENSION - threeD1));
                }
            }
            currentX += boxWidth;
            currentX += extraPer;
            if (extraExtra-- > 0) {
                currentX += 1;
            }


        }




        g.setColor(Color.black);
        //    g.drawRect(xOrigin,topMargin,displayWidth,displayHeight);
    }

    /**
     * _more_
     *
     * @param g _more_
     * @param label _more_
     * @param x _more_
     * @param y _more_
     * @param labelHeight _more_
     */
    public void printLabel(Graphics g, String label, int x, int y,
                           int labelHeight) {
        int         labelLength = label.length();
        int         yStep       = (int) (0.25 * (double) labelHeight);
        FontMetrics fm          = g.getFontMetrics();
        //    int labelHeight  = fm.getMaxDescent()+fm.getMaxAscent();
        g.setColor(Color.gray);
        for (int idx = 0; idx < labelLength; idx++) {
            char   c  = label.charAt(idx);
            char[] ca = { c };
            g.drawChars(ca, 0, 1, x, y + yStep * (idx + 1));
            x += fm.stringWidth("" + c) + 1;
        }
    }




    /**
     * _more_
     *
     * @param g _more_
     */
    public void printDataGrid(Graphics g) {}


    /**
     * _more_
     *
     * @param g _more_
     * @param centric _more_
     */
    public void printScatterChart(Graphics g, int centric) {
        //    printAxisLabels(g, centric) ;
        Rectangle b = bounds();
        printChartAxis(g, b);

        FontMetrics fm          = g.getFontMetrics();
        int         labelHeight = fm.getMaxDescent() + fm.getMaxAscent();

        int boxWidth = displayWidth
                       / (numDataRows * numDataCols + numDataRows);
        int currentX         = leftMargin;

        int numMajorElements = getNumMajorElements(centric);
        int numMinorElements = getNumMinorElements(centric);

        for (int major = 0; major < numMajorElements; major++) {
            int startX = currentX;
            for (int minor = 0; minor < numMinorElements; minor++) {
                int height = (int) (scale * getData(major, minor, centric));
                int y      = yOrigin - height;
                g.setColor(Color.black);
                g.setColor(colors[minor % colors.length]);
                g.fillRect(currentX, y, boxWidth, height);
                currentX += boxWidth;
            }
            String label     = getMajorLabel(major, centric);
            int    halfWidth = (fm.stringWidth(label)) / 2;
            int    y         = yOrigin + labelHeight + 2;
            int    centerX   = startX + (numMinorElements * boxWidth) / 2;
            g.setColor(Color.black);
            g.drawString(label, centerX - halfWidth, y);
            currentX += boxWidth;
        }

        g.setColor(Color.black);
        g.drawRect(xOrigin, topMargin, displayWidth, displayHeight);
    }


    /**
     * _more_
     *
     * @param g _more_
     * @param centric _more_
     * @param boxWidth _more_
     */
    public void printXLabels(Graphics g, int centric, int boxWidth) {
        FontMetrics fm          = g.getFontMetrics();
        int         labelHeight = fm.getMaxDescent() + fm.getMaxAscent();
        int         currentX    = leftMargin;
        g.setColor(Color.black);
        int numMinorElements = getNumMinorElements(centric);
        for (int minor = 0; minor < numMinorElements; minor++) {
            boolean stringOk = true;
            if (numMinorElements > 25) {
                stringOk = ((minor % 5) == 0);
            }
            if (stringOk) {
                g.drawLine(currentX, yOrigin - 1, currentX + threeD1,
                           yOrigin - 1 - threeD1);
                g.drawLine(currentX, yOrigin - 1, currentX, yOrigin + 2);
                printLabel(g, getMinorLabel(minor, centric), currentX,
                           yOrigin + labelHeight, labelHeight);
                g.drawLine(currentX + threeD1, yOrigin - threeD2,
                           currentX + threeD1,
                           2 + (BASE3D_DIMENSION - threeD1));
            }
            currentX += boxWidth;
        }


    }


    /**
     * _more_
     *
     * @param g _more_
     * @param centric _more_
     */
    public void printLineChart(Graphics g, int centric) {
        Rectangle b = bounds();

        printAxisLabels(g, centric);
        printChartAxis(g, b);



        int numMajorElements = getNumMajorElements(centric);
        int numMinorElements = getNumMinorElements(centric);
        int lastX            = 0;
        int lastY            = 0;
        int boxWidth         = 1;

        if (numMinorElements > 1) {
            boxWidth = (displayWidth - threeD1) / (numMinorElements - 1);
        }

        printXLabels(g, centric, boxWidth);

        int currentX = leftMargin;
        for (int major = 0; major < numMajorElements; major++) {
            Color theColor = colors[major % colors.length];
            for (int minor = 0; minor < numMinorElements; minor++) {
                int height   = (int) (scale * getData(major, minor, centric));
                int currentY = yOrigin - height;
                if (minor != 0) {
                    int[] xs = { lastX, lastX + threeD1, currentX + threeD1,
                                 currentX };
                    int[] ys = { lastY, lastY - threeD1, currentY - threeD1,
                                 currentY };
                    g.setColor(theColor);
                    g.fillPolygon(xs, ys, xs.length);

                    g.setColor(((threeD1 > 0)
                                ? Color.black
                                : theColor));
                    GuiUtils.drawPolyLine(g, xs, ys);
                    addRect(
                        new Rectangle(
                            currentX - Math.min(10, boxWidth) / 2,
                            currentY - threeD1, Math.min(10, boxWidth),
                            threeD1 * 2), major, minor, centric);
                }

                lastX    = currentX;
                lastY    = currentY;
                currentX += boxWidth;
            }
            currentX = leftMargin;
        }



        //    g.setColor(Color.black);
        //    g.drawRect(xOrigin,topMargin,displayWidth,displayHeight);
    }


    /**
     * _more_
     *
     * @param g _more_
     * @param centric _more_
     */
    public void printKiviat(Graphics g, int centric) {
        g.setFont(pieFont);
        FontMetrics fm               = g.getFontMetrics();
        int         labelHeight      = fm.getMaxDescent() + fm.getMaxAscent();
        int         numMajorElements = getNumMajorElements(centric);
        int         numMinorElements = getNumMinorElements(centric);
        int         currentX         = 0;
        int         currentY         = topMargin + 10;
        int         pieWidth         = displayWidth / (numMajorElements + 1);
        if (pieWidth < 40) {
            pieWidth = 40;
        }

        if (numMinorElements == 0) {
            return;
        }

        double anglePerElement = 360.0 / (double) numMinorElements;
        int    lineLength      = pieWidth / 2;
        Point  src             = new Point(lineLength, 0);
        Point  dest            = new Point(0, 0);
        Point  valuePoint      = new Point(0, 0);
        Point  valueSrc        = new Point(0, 0);


        int[]  xs              = new int[numMinorElements];
        int[]  ys              = new int[numMinorElements];


        for (int major = 0; major < numMajorElements; major++) {
            g.setColor(Color.black);
            //      g.fillArc (currentX-1,currentY-1,pieWidth+2,pieWidth+2,0,360);
            g.setColor(Color.black);
            g.drawArc(currentX, currentY, pieWidth, pieWidth, 0, 360);
            addMajorRect(new Rectangle(currentX, currentY, pieWidth,
                                       pieWidth), major, centric);

            double currentAngle = 0.0;
            g.setColor(Color.black);
            String label = getMajorLabel(major, centric);
            GU.drawClippedString(g, label, currentX,
                                 currentY + pieWidth + labelHeight, pieWidth);

            for (int minor = 0; minor < numMinorElements; minor++) {
                int    centerX    = currentX + pieWidth / 2;
                int    centerY    = currentY + pieWidth / 2;
                double value      = getData(major, minor, centric);
                double percentage = getMajorPercentage(major, centric, value);

                valueSrc.x = (int) ((pieWidth / 2) * percentage + 0.5);

                g.setColor(colors[minor % colors.length]);
                GuiUtils.rotatePoint(dest, src,
                                     GuiUtils.toRadian(currentAngle));
                GuiUtils.rotatePoint(valuePoint, valueSrc,
                                     GuiUtils.toRadian(currentAngle));
                dest.x    += centerX;
                dest.y    += centerY;
                xs[minor] = valuePoint.x + centerX;
                ys[minor] = valuePoint.y + centerY;

                g.drawLine(centerX, centerY, dest.x, dest.y);
                currentAngle += anglePerElement;
            }
            g.setColor(Color.gray);
            g.fillPolygon(xs, ys, xs.length);
            currentX += pieWidth + 10;

            if (currentX > xExtent) {
                currentY += pieWidth + labelHeight + 4;
                currentX = 0;
            }

        }
    }



    /**
     * _more_
     *
     * @param g _more_
     * @param centric _more_
     */
    public void printMinorColorKey(Graphics g, int centric) {
        FontMetrics fm               = g.getFontMetrics();
        int         labelHeight      = fm.getMaxDescent() + fm.getMaxAscent();
        int         labelBoxHeight   = labelHeight;
        int         labelBoxWidth    = labelHeight;
        int         labelX           = 0;

        int         numMinorElements = getNumMinorElements(centric);

        for (int minor = 0; minor < numMinorElements; minor++) {
            g.setColor(colors[minor % colors.length]);
            int labelY = minor * labelBoxHeight;
            g.fillRect(labelX, labelY, labelBoxWidth, labelBoxHeight);
            g.setColor(Color.black);
            String label = getMinorLabel(minor, centric);
            g.drawString(label, labelX + labelBoxWidth + 2,
                         labelY + labelBoxHeight);
        }
    }


    /**
     * _more_
     *
     * @param g _more_
     * @param centric _more_
     */
    public void printMajorColorKey(Graphics g, int centric) {
        FontMetrics fm               = g.getFontMetrics();
        int         labelHeight      = fm.getMaxDescent() + fm.getMaxAscent();
        int         labelBoxHeight   = labelHeight;
        int         labelBoxWidth    = labelHeight;
        int         labelX           = 0;
        int         numMajorElements = getNumMajorElements(centric);
        for (int major = 0; major < numMajorElements; major++) {
            g.setColor(colors[major % colors.length]);
            int labelY = 10 + major * labelBoxHeight;
            g.fillRect(labelX, labelY, labelBoxWidth, labelBoxHeight);
            g.setColor(Color.black);
            String label = getMajorLabel(major, centric);
            g.drawString(label, labelX + labelBoxWidth + 2,
                         labelY + labelBoxHeight);
        }
    }

    /**
     * _more_
     *
     * @param g _more_
     * @param centric _more_
     */
    public void printPieChart(Graphics g, int centric) {
        g.setFont(pieFont);

        int numMajorElements = getNumMajorElements(centric);
        int numMinorElements = getNumMinorElements(centric);
        int currentX         = leftMargin;
        int pieWidth         = displayWidth / (numMajorElements + 1);
        if (pieWidth < 40) {
            pieWidth = 40;
        }
        FontMetrics fm          = g.getFontMetrics();
        int         labelHeight = fm.getMaxDescent() + fm.getMaxAscent();
        //    int pieHeight = (int)(0.50*(double)pieWidth);
        int pieHeight = pieWidth;
        int currentY  = topMargin + 10;


        for (int major = 0; major < numMajorElements; major++) {
            int currentStartAngle = 0;
            g.setColor(Color.black);
            String label = getMajorLabel(major, centric);
            GU.drawClippedString(g, label, currentX,
                                 currentY + pieWidth + labelHeight, pieWidth);
            addMajorRect(new Rectangle(currentX, currentY, pieWidth,
                                       pieWidth), major, centric);
            for (int minor = 0; minor < numMinorElements; minor++) {
                double value      = getData(major, minor, centric);
                double percentage = getMajorPercentage(major, centric, value);
                int    arcAngle   = (int) (360.0 * percentage + 0.5);
                g.setColor(colors[minor % colors.length]);
                g.fillArc(currentX, currentY, pieWidth, pieHeight,
                          currentStartAngle, arcAngle);
                currentStartAngle += arcAngle;
            }
            currentX += pieWidth + 10;
            if (currentX > xExtent) {
                currentX = leftMargin;
                currentY += pieWidth + labelHeight * 2;
            }


        }
    }


    /**
     * _more_
     *
     * @param searchRect _more_
     * @param displayRect _more_
     * @param major _more_
     * @param minor _more_
     * @param centric _more_
     */
    public void addRect(Rectangle searchRect, Rectangle displayRect,
                        int major, int minor, int centric) {
        searchRectList.addElement(searchRect);
        displayRectList.addElement(displayRect);

        Point dataIndex = ((centric == ROW_CENTRIC)
                           ? new Point(major, minor)
                           : new Point(minor, major));
        if ((major >= 0) && (minor >= 0)) {
            dataIndex.x += (numRows - numDataRows);
            dataIndex.y += (numCols - numDataCols);
        }

        dataList.addElement(dataIndex);


    }


    /**
     * _more_
     *
     * @param r _more_
     * @param major _more_
     * @param minor _more_
     * @param centric _more_
     */
    public void addRect(Rectangle r, int major, int minor, int centric) {
        addRect(r, r, major, minor, centric);
    }

    /**
     * _more_
     *
     * @param r _more_
     * @param major _more_
     * @param centric _more_
     */
    public void addMajorRect(Rectangle r, int major, int centric) {
        if (centric == ROW_CENTRIC) {
            addRect(r, major, -1, ROW_CENTRIC);
        } else {
            addRect(r, -1, major, ROW_CENTRIC);
        }
    }



    /**
     * _more_
     *
     * @param g _more_
     * @param gradient _more_
     */
    public void printGradientKey(Graphics g, Color[] gradient) {
        int grayHeight = (int) ((double) displayHeight / gradient.length
                                + 0.5);
        int grayY = 10;
        for (int grayIdx = 0; grayIdx < gradient.length; grayIdx++) {
            g.setColor(gradient[grayIdx]);
            g.fillRect(0, grayY, 20, grayHeight);
            if ((grayIdx % 4 == 0) || (grayIdx == (gradient.length - 1))) {
                double value = ((double) (maxValue - minValue))
                               * ((double) grayIdx / gradient.length);
                String label = "" + value;
                g.setColor(Color.black);
                g.drawString(label, 20, grayY + 5);
            }
            grayY += grayHeight;
        }



        g.setColor(Color.black);
        g.drawRect(leftMargin + displayWidth + 10, topMargin, 20,
                   grayHeight * gradient.length);
    }


    /**
     * _more_
     *
     * @param g _more_
     * @param gradient _more_
     */
    public void printColorGradient(Graphics g, Color[] gradient) {
        printAxisLabels(g, ROW_CENTRIC);

        int boxWidth  = (bounds().width - rightMargin) / numDataCols;
        int boxHeight = (displayHeight) / numDataRows;
        int currentY  = topMargin;
        int offset    = (int) (100.0 / (double) (gradient.length) + 1.0);

        for (int row = 0; row < numDataRows; row++) {
            int currentX = 0;
            for (int col = 0; col < numDataCols; col++) {
                int percentage = (int) (100.0 * data[row][col] / maxValue);
                if (percentage < 0) {
                    percentage = 0;
                } else if (percentage >= 100) {
                    percentage = 99;
                }
                int index = percentage / offset;

                //      System.err.println("Value: " + data[row][col] + "Percentage: " + percentage + " index: " + index);
                if (index < 0) {
                    index = 0;
                }
                if (index >= gradient.length) {
                    index = gradient.length - 1;
                }
                Color gray = gradient[index];

                g.setColor(gray);
                g.fillRect(currentX, currentY, boxWidth, boxHeight);
                addRect(new Rectangle(currentX, currentY, boxWidth,
                                      boxHeight), row, col, ROW_CENTRIC);


                if (percentage > 75) {
                    g.setColor(Color.white);
                } else {
                    g.setColor(Color.black);
                }

                boolean stringOk = true;

                if (numDataCols > 50) {
                    stringOk = ((col % 10) == 0);
                }


                if (stringOk) {
                    g.drawString("" + data[row][col], currentX + 2,
                                 currentY + (boxHeight / 2));
                }
                currentX += boxWidth;
            }
            currentY += boxHeight;
        }

        g.setColor(Color.black);
        g.drawRect(0, topMargin, boxWidth * numDataCols,
                   boxHeight * numDataRows);


    }













}

