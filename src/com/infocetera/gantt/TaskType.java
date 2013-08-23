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

package com.infocetera.gantt;


import com.infocetera.util.GuiUtils;

import java.awt.*;


import java.util.Vector;


/**
 * Class TaskType _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class TaskType {

    /** _more_ */
    public static final int ROWHEIGHT = 40;

    /** _more_ */
    private Color fillColor = null;

    /** _more_ */
    private GanttView view;

    /** _more_ */
    Vector tasks = new Vector();

    /** _more_ */
    String name;

    /** _more_ */
    String id;

    /** _more_ */
    boolean milestone;



    /**
     * _more_
     *
     * @param view _more_
     * @param id _more_
     * @param name _more_
     * @param fillColor _more_
     * @param milestone _more_
     */
    public TaskType(GanttView view, String id, String name, Color fillColor,
                    boolean milestone) {
        this.view      = view;
        this.id        = id;
        this.name      = name;
        this.milestone = milestone;
        if (fillColor != null) {
            this.fillColor = fillColor;
        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public GanttView getView() {
        return view;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isMilestone() {
        return milestone;
    }

    /**
     * _more_
     *
     * @param task _more_
     */
    public void addTask(GanttTask task) {
        tasks.addElement(task);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public Color getColor() {
        return fillColor;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getName() {
        return name;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getId() {
        return id;
    }



}
