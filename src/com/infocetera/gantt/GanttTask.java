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

package com.infocetera.gantt;


import com.infocetera.util.*;

import java.awt.*;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Vector;



/**
 * Class GanttTask _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class GanttTask {

    /** _more_          */
    private GregorianCalendar cal = new GregorianCalendar();

    /** _more_          */
    public static int MILESTONE_WIDTH = GanttView.ROWHEIGHT / 2;

    /** _more_          */
    public static final int SEL_WIDTH = 6;

    /** _more_          */
    public static final int H_SEL_WIDTH = 3;

    /** _more_          */
    private static Color DFLT_COLOR = new Color(0, 128, 192);

    /** _more_          */
    private static Color[] PRIORITY_COLOR = {
        GuiUtils.getColor("#FFFFFF"), GuiUtils.getColor("#FFEEEE"),
        GuiUtils.getColor("#FFDDDD"), GuiUtils.getColor("#FFAAAA"),
        GuiUtils.getColor("#FF8888"), GuiUtils.getColor("#FF6666"),
        GuiUtils.getColor("#FF4444"), GuiUtils.getColor("#FF2222"),
        GuiUtils.getColor("#FF1111"), GuiUtils.getColor("#FF0000")
    };

    /** _more_          */
    private Color completeColor = Color.orange;

    /** _more_          */
    public static final int CLICKBOX_WIDTH = 8;

    /** _more_          */
    public static final int XPERTAB = 10;

    /** _more_          */
    Rectangle mainBox = new Rectangle();

    /** _more_          */
    Rectangle headerBox = new Rectangle();

    /** _more_          */
    Rectangle headerClickBox = new Rectangle(0, 0, CLICKBOX_WIDTH,
                                             CLICKBOX_WIDTH);

    /** _more_          */
    public static final Font boldFont = new Font("Times", Font.BOLD, 12);

    /** _more_          */
    public static final Font normalFont = new Font("Times", 0, 12);




    /** _more_          */
    public static int BOXHEIGHT = 10;


    /** _more_          */
    GanttView view;

    /** _more_          */
    GanttResource resource;

    /** _more_          */
    GanttStatus status;

    /** _more_          */
    TaskType type;

    /** _more_          */
    int priority = 1;

    /** _more_          */
    String label;

    /** _more_          */
    private Vector subTasks = new Vector();

    /** _more_          */
    private Vector toDependencies = new Vector();

    /** _more_          */
    private Vector fromDependencies = new Vector();


    /** _more_          */
    private boolean visible = true;

    /** _more_          */
    private boolean childrenVisible = true;

    /** _more_          */
    private long startDate;

    /** _more_          */
    private long endDate;

    /** _more_          */
    private Date startDateObj;

    /** _more_          */
    private Date endDateObj;

    /** _more_          */
    private int duration;

    /** _more_          */
    private double complete;

    /** _more_          */
    private String name;

    /** _more_          */
    private GanttTask parent;

    /** _more_          */
    private int absolutePosition = 0;

    /** _more_          */
    private int position = 0;

    /** _more_          */
    private int tabPosition = 0;

    /** _more_          */
    private String id;


    /**
     * _more_
     *
     * @param view _more_
     * @param id _more_
     * @param taskName _more_
     * @param startDate _more_
     * @param endDate _more_
     * @param duration _more_
     * @param complete _more_
     * @param resource _more_
     * @param status _more_
     * @param type _more_
     * @param priority _more_
     */
    public GanttTask(GanttView view, String id, String taskName,
                     long startDate, long endDate, int duration,
                     double complete, GanttResource resource,
                     GanttStatus status, TaskType type, int priority) {
        this.id        = id;
        this.view      = view;
        this.resource  = resource;
        this.type      = type;
        this.status    = status;
        this.name      = taskName;
        this.startDate = startDate;
        this.endDate   = endDate;
        startDateObj   = new Date(startDate);
        endDateObj     = new Date(endDate);
        this.duration  = duration;
        this.complete  = complete;
        this.priority  = priority;
    }

    /**
     * _more_
     *
     * @param movedTasks _more_
     */
    public void checkDependencies(Hashtable movedTasks) {
        if (movedTasks.get(this) != null) {
            return;
        }
        movedTasks.put(this, this);
        long end = this.endDate + view.DAY_FACTOR;
        for (int i = 0; i < toDependencies.size(); i++) {
            GanttTask task = (GanttTask) toDependencies.elementAt(i);
            if (movedTasks.get(task) != null) {
                continue;
            }
            if (end > task.startDate) {
                long diff = (end - task.startDate);
                task.startDate += diff;
                task.endDate   += diff;
                task.setPosition(task.position);
                task.checkDependencies(movedTasks);
            }
        }

        for (int i = 0; i < fromDependencies.size(); i++) {
            GanttTask task = (GanttTask) fromDependencies.elementAt(i);
            if (movedTasks.get(task) != null) {
                continue;
            }
            end = task.endDate + view.DAY_FACTOR;
            if (end > this.startDate) {
                long diff = (this.startDate - end);
                task.startDate += diff;
                task.endDate   += diff;
                task.setPosition(task.position);
                task.checkDependencies(movedTasks);
            }
        }

    }


    /**
     * _more_
     *
     * @param label _more_
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getPriority() {
        return priority;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public GanttResource getResource() {
        return resource;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public GanttStatus getStatus() {
        return status;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public TaskType getType() {
        return type;
    }

    /**
     * _more_
     *
     * @param to _more_
     */
    public void addToDependency(GanttTask to) {
        toDependencies.addElement(to);
    }

    /**
     * _more_
     *
     * @param from _more_
     */
    public void addFromDependency(GanttTask from) {
        fromDependencies.addElement(from);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getVisible() {
        return visible;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getDuration() {
        return duration;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public double getComplete() {
        return complete;
    }

    /**
     * _more_
     *
     * @param c _more_
     */
    public void setComplete(double c) {
        complete = c;
    }



    /**
     * _more_
     *
     * @param diff _more_
     * @param seen _more_
     */
    public void deltaDate(long diff, Hashtable seen) {
        if (seen.get(this) != null) {
            return;
        }
        seen.put(this, this);
        setStartDate(startDate + diff);
        setEndDate(endDate + diff);
        for (int i = 0; i < toDependencies.size(); i++) {
            GanttTask other = (GanttTask) toDependencies.elementAt(i);
            other.deltaDate(diff, seen);
        }
        for (int i = 0; i < subTasks.size(); i++) {
            GanttTask other = (GanttTask) subTasks.elementAt(i);
            other.deltaDate(diff, seen);
        }
    }



    /**
     * _more_
     *
     * @param s _more_
     */
    public void setStartDate(long s) {
        startDate    = s;
        startDateObj = new Date(startDate);
        setPosition(position);
    }

    /**
     * _more_
     *
     * @param s _more_
     */
    public void setEndDate(long s) {
        endDate    = s;
        endDateObj = new Date(endDate);
        setPosition(position);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public long getStartDate() {
        return startDate;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Date getStartDateObj() {
        return startDateObj;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Date getEndDateObj() {
        return endDateObj;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public long getEndDate() {
        return endDate;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getStartDateString() {
        return getDateString(startDateObj);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getEndDateString() {
        return getDateString(endDateObj);
    }

    /**
     * _more_
     *
     * @param date _more_
     *
     * @return _more_
     */
    public String getDateString(Date date) {
        cal.setTime(date);
        return GanttView.days[cal.get(Calendar.DAY_OF_WEEK)] + " "
               + GanttView.months[cal.get(Calendar.MONTH)] + " "
               + cal.get(Calendar.DAY_OF_MONTH) + ", "
               + cal.get(Calendar.YEAR);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getId() {
        return id;
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
    public int getPosition() {
        return position;
    }

    /**
     * _more_
     *
     * @param p _more_
     */
    public void setAbsolutePosition(int p) {
        absolutePosition = p;
    }

    /**
     * _more_
     *
     * @param descendant _more_
     *
     * @return _more_
     */
    public boolean isAncestor(GanttTask descendant) {
        if (null == descendant) {
            return false;
        }
        if (this == descendant) {
            return true;
        }
        return isAncestor(descendant.getParentTask());
    }

    /**
     * _more_
     *
     * @param p _more_
     */
    public void setPosition(int p) {
        if (parent != null) {
            tabPosition = parent.tabPosition + 1;
        } else {
            tabPosition = 0;
        }

        position = p;

        int y = position * GanttView.ROWHEIGHT;
        mainBox.x = view.dateToX(startDate);
        mainBox.y = position * GanttView.ROWHEIGHT + BOXHEIGHT / 2;
        mainBox.width = view.dateToX(endDate + GanttView.DAY_FACTOR)
                        - mainBox.x;

        mainBox.height = BOXHEIGHT;

        headerClickBox.y = (y + GanttView.ROWHEIGHT) - headerClickBox.height
                           - 2;
        headerClickBox.x = GanttView.POS_TASKNAME + 2 + tabPosition * XPERTAB;
    }

    /**
     * _more_
     *
     * @param tabs _more_
     *
     * @return _more_
     */
    public static int getNameX(int tabs) {
        return GanttView.POS_TASKNAME + 2 + tabs * XPERTAB + CLICKBOX_WIDTH;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getBoxCenterX() {
        return mainBox.x + mainBox.width / 2;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getBoxCenterY() {
        return mainBox.y + mainBox.height / 2;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getBoxLeft() {
        return mainBox.x;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getBoxRight() {
        return mainBox.x + mainBox.width;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getBoxTop() {
        return mainBox.y;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getBoxBottom() {
        return mainBox.y + mainBox.height;
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public int getTabs() {
        return tabPosition;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public Vector getSubTasks() {
        return subTasks;
    }

    /**
     * _more_
     *
     * @param subTask _more_
     */
    public void removeChildTask(GanttTask subTask) {
        subTasks.removeElement(subTask);
    }

    /**
     * _more_
     *
     * @param subTask _more_
     */
    private void addChildTask(GanttTask subTask) {
        addChildTask(subTask, -1);
    }

    /**
     * _more_
     *
     * @param subTask _more_
     * @param index _more_
     */
    private void addChildTask(GanttTask subTask, int index) {
        if ( !subTasks.contains(subTask)) {
            if ((index < 0) || (index >= subTasks.size())) {
                subTasks.addElement(subTask);
            } else {
                subTasks.insertElementAt(subTask, index);
            }
        }
    }

    /**
     * _more_
     *
     * @param newParent _more_
     */
    public void setParentTask(GanttTask newParent) {
        setParentTask(newParent, -1);
    }

    /**
     * _more_
     *
     * @param newParent _more_
     * @param index _more_
     */
    public void setParentTask(GanttTask newParent, int index) {
        if (parent != null) {
            parent.removeChildTask(this);
        }
        parent = newParent;
        if (parent != null) {
            parent.addChildTask(this, index);
        }
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public GanttTask getParentTask() {
        return parent;
    }

    /**
     * _more_
     *
     * @param weightBy _more_
     *
     * @return _more_
     */
    public double getMyWeight(int weightBy) {
        switch (weightBy) {

          case TreePanel.TREE_DURATION :
              return duration;

          case TreePanel.TREE_LEFT :
              return (1.0 - complete) * duration;

          case TreePanel.TREE_COMPLETE :
              return complete * duration;

          case TreePanel.TREE_RANGE :
              return (double) (GanttView.DAY_FACTOR + startDate - endDate);
        }
        return 0.0;
    }

    /**
     * _more_
     *
     * @param weightBy _more_
     *
     * @return _more_
     */
    public double getTotalWeight(int weightBy) {
        if (subTasks.size() == 0) {
            return getMyWeight(weightBy);
        }
        int total = 0;
        for (int i = 0; i < subTasks.size(); i++) {
            GanttTask child = (GanttTask) subTasks.elementAt(i);
            total += child.getTotalWeight(weightBy);
        }
        return total;
    }

    /**
     * _more_
     *
     * @param weightBy _more_
     *
     * @return _more_
     */
    public double getWeight(int weightBy) {
        return ( !childrenVisible
                 ? getTotalWeight(weightBy)
                 : getMyWeight(weightBy));
    }

    /**
     * _more_
     *
     * @param weightBy _more_
     * @param colorBy _more_
     *
     * @return _more_
     */
    public NestedTreeNode getTreeNode(int weightBy, int colorBy) {
        if ( !visible) {
            return null;
        }

        NestedTreeNode node = new NestedTreeNode(name, getWeight(weightBy),
                                  this, Color.black,
                                  getFillColor(false, colorBy));
        node.hiliteColor = view.hiliteColor;

        if (childrenVisible) {
            for (int i = 0; i < subTasks.size(); i++) {
                GanttTask child = (GanttTask) subTasks.elementAt(i);
                if ( !child.visible) {
                    continue;
                }
                node.add(child.getTreeNode(weightBy, colorBy));
            }
        } else {}
        return node;
    }

    /**
     * _more_
     *
     * @param b _more_
     */
    public void setVisible(boolean b) {
        visible = b;
        if ( !visible) {
            view.removeSelection(this);
        }
        if ( !childrenVisible) {
            return;
        }
        for (int i = 0; i < subTasks.size(); i++) {
            GanttTask task = (GanttTask) subTasks.elementAt(i);
            task.setVisible(b);
        }
    }


    /**
     * _more_
     */
    public void updateTimes() {
        if (isParent()) {
            double totalComplete = 0.0;
            for (int i = 0; i < subTasks.size(); i++) {
                GanttTask task = (GanttTask) subTasks.elementAt(i);
                if (i == 0) {
                    duration = task.duration;
                } else {
                    duration += task.duration;
                }
                totalComplete += task.duration * task.complete;
                if ((i == 0) || (task.startDate < startDate)) {
                    startDate = task.startDate;
                }
                if ((i == 0) || (task.endDate > endDate)) {
                    endDate = task.endDate;
                }
            }
            if (duration > 0) {
                complete = totalComplete / duration;
            } else {
                complete = 0.0;
            }
            setStartDate(startDate);
            setEndDate(endDate);
        }
        if (parent != null) {
            parent.updateTimes();
        }
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getChildrenVisible() {
        return childrenVisible;
    }

    /**
     * _more_
     */
    public void toggleChildrenVisible() {
        setChildrenVisible( !childrenVisible);
    }

    /**
     * _more_
     *
     * @param b _more_
     */
    public void setChildrenVisible(boolean b) {
        childrenVisible = b;
        for (int i = 0; i < subTasks.size(); i++) {
            GanttTask task = (GanttTask) subTasks.elementAt(i);
            task.setVisible(b);
        }
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isParent() {
        return (subTasks.size() > 0);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getBottom() {
        return getTop() + GanttView.ROWHEIGHT;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getTop() {
        return position * GanttView.ROWHEIGHT;
    }



    /**
     * _more_
     *
     * @param g _more_
     */
    public void paintHeader(Graphics g) {

        int top    = getTop();
        int bottom = getBottom();
        headerBox.x      = 0;
        headerBox.y      = top;
        headerBox.width  = view.WIDTH_COL1;
        headerBox.height = bottom - top;

        Font    initFont = g.getFont();
        boolean isParent = isParent();


        g.setColor(view.headerLineColor);
        g.drawLine(0, bottom, view.WIDTH_COL1, bottom);
        for (int i = 1; i < view.TASKHEADER_POS.length; i++) {
            int x = view.TASKHEADER_POS[i];
            g.drawLine(x, top, x, bottom);
        }


        g.setColor(Color.black);
        g.setFont(boldFont);
        GuiUtils.drawStringAt(g, "" + (absolutePosition + 1),
                              view.POS2_ID - 2, bottom - 1, GuiUtils.PT_SE);

        if (isParent) {
            g.setFont(boldFont);
        } else {
            g.setFont(normalFont);
        }
        GuiUtils.drawStringAt(g, "" + duration + "d", view.POS2_DURATION - 2,
                              bottom - 1, GuiUtils.PT_SE);



        if (isParent) {
            Rectangle hcb = headerClickBox;
            g.setColor(view.clickBoxColor);
            g.fillRect(headerClickBox.x, headerClickBox.y,
                       headerClickBox.width, headerClickBox.height);
            g.setColor(Color.black);
            g.drawRect(headerClickBox.x, headerClickBox.y,
                       headerClickBox.width, headerClickBox.height);
            g.drawLine(hcb.x + 2, hcb.y + hcb.height / 2,
                       hcb.x + hcb.width - 2, hcb.y + hcb.height / 2);
            if ( !childrenVisible) {
                g.drawLine(hcb.x + hcb.width / 2, hcb.y + 2,
                           hcb.x + hcb.width / 2, hcb.y + hcb.height - 2);
            }
        }
        int circleW;


        int offsetX = 2 + headerClickBox.x + headerClickBox.width;
        GuiUtils.drawClippedString(g, name, offsetX, bottom - 1,
                                   view.WIDTH_TASKNAME - headerClickBox.width
                                   - 2);

        String completeStr = (int) (complete * 100) + "%";
        GuiUtils.drawStringAt(g, completeStr, view.POS2_COMPLETE - 2,
                              bottom - 1, GuiUtils.PT_SE);

        GuiUtils.drawClippedString(g, resource.getName(),
                                   view.POS_RESOURCE + 1, bottom - 1,
                                   view.WIDTH_RESOURCE - 1);

        circleW = 0;
        if (type.getColor() != null) {
            circleW = GanttView.ROWHEIGHT - 6;
            g.setColor(type.getColor());
            int ax = view.POS2_TYPE - circleW;
            g.fillArc(ax, top + 3, circleW, circleW, 0, 360);
            g.setColor(Color.black);
            g.drawArc(ax, top + 3, circleW, circleW, 0, 360);
        }


        GuiUtils.drawClippedString(g, type.getName(), view.POS_TYPE + 1,
                                   bottom - 1, view.WIDTH_TYPE - 1 - circleW);




        circleW = 0;
        if (status.getColor() != null) {
            circleW = GanttView.ROWHEIGHT - 6;
            g.setColor(status.getColor());
            int ax = view.POS2_STATUS - circleW;
            g.fillArc(ax, top + 3, circleW, circleW, 0, 360);
            g.setColor(Color.black);
            g.drawArc(ax, top + 3, circleW, circleW, 0, 360);
        }

        g.setColor(Color.black);

        GuiUtils.drawClippedString(g, status.getName(), view.POS_STATUS + 1,
                                   bottom - 1,
                                   view.WIDTH_STATUS - 1 - circleW);

        GuiUtils.drawStringAt(g, GuiUtils.clipString(g,
                view.formatDate(startDateObj, false),
                view.WIDTH_STARTDATE - 1), view.POS2_STARTDATE - 2,
                                           bottom - 1, GuiUtils.PT_SE);

        GuiUtils.drawStringAt(g, GuiUtils.clipString(g,
                view.formatDate(endDateObj, false),
                view.WIDTH_ENDDATE - 1), view.POS2_ENDDATE - 2, bottom - 1,
                                         GuiUtils.PT_SE);


        g.setFont(initFont);

    }



    /**
     * _more_
     *
     * @param normal _more_
     *
     * @return _more_
     */
    private Color getFillColor(boolean normal) {
        return getFillColor(normal, view.getColorBy());
    }


    /**
     * _more_
     *
     * @param normal _more_
     * @param by _more_
     *
     * @return _more_
     */
    private Color getFillColor(boolean normal, int by) {
        switch (by) {

          case GanttView.COLOR_RESOURCE :
              return resource.getColor();

          case GanttView.COLOR_STATUS :
              return status.getColor();

          case GanttView.COLOR_TYPE :
              return type.getColor();

          case GanttView.COLOR_PRIORITY :
              return PRIORITY_COLOR[priority];
        }
        if (normal) {
            if (isParent()) {
                return Color.black;
            }
            if (type.isMilestone()) {
                return Color.black;
            }
            return DFLT_COLOR;
        }
        return null;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isMilestone() {
        return type.isMilestone();
    }

    /**
     * _more_
     *
     * @param g _more_
     * @param isHilite _more_
     */
    public void paint(Graphics g, boolean isHilite) {
        g.setColor(Color.black);

        int   top       = mainBox.y;
        int   bottom    = mainBox.y + mainBox.height;
        int   mid       = mainBox.y + mainBox.height / 2;
        int   right     = mainBox.x + mainBox.width;
        int   left      = mainBox.x;

        Color fillColor = getFillColor(true);
        g.setColor(fillColor);

        if (isMilestone()) {
            int w = MILESTONE_WIDTH;
            int[] xs2 = { right - w, right - w / 2, right, right - w / 2,
                          right - w };
            int[] ys = { mid, top, mid, bottom, mid };
            g.fillPolygon(xs2, ys, 5);
            g.setColor(Color.black);
            g.drawPolygon(xs2, ys, 5);
        } else if (isParent()) {
            g.fillRect(mainBox.x, mainBox.y, mainBox.width, mainBox.height);
            g.setColor(Color.black);
            g.drawRect(mainBox.x, mainBox.y, mainBox.width, mainBox.height);
            g.setColor(fillColor);
            int o = 2;
            int w = 12;
            int h = GanttView.ROWHEIGHT / 4;
            if (mainBox.width > (2 * (w + o))) {
                left  += o;
                right -= o;
                int[] xs1 = { left, left + w / 2, left + w, left };
                int[] ys  = { bottom, bottom + h, bottom, bottom };
                g.fillPolygon(xs1, ys, 4);
                int[] xs2 = { right, right - w / 2, right - w, right };
                g.fillPolygon(xs2, ys, 4);
                g.setColor(Color.black);
                g.drawPolygon(xs1, ys, 3);
                g.drawPolygon(xs2, ys, 3);
                g.setColor(fillColor);
                g.drawLine(left, bottom, left + w, bottom);
                g.drawLine(right, bottom, right - w, bottom);

            }
        } else {
            g.fillRect(mainBox.x, mainBox.y, mainBox.width, mainBox.height);

            if (complete > 0.0) {
                g.setColor(completeColor);
                g.fillRect(mainBox.x, mainBox.y + 3,
                           (int) (complete * mainBox.width),
                           mainBox.height - 5);
            }

            g.setColor(Color.blue);
            g.drawRect(mainBox.x, mainBox.y, mainBox.width, mainBox.height);


        }

        g.setColor(Color.black);

        if (label == null) {
            label = view.getLabel(this);
        }


        g.drawString(label, mainBox.x + mainBox.width + 2,
                     mainBox.y + mainBox.height - 1);
        paintDependencies(g, isHilite, toDependencies, true);
        if (isHilite) {
            paintDependencies(g, isHilite, fromDependencies, false);
        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getMessage() {
        return "Task:" + name + " Resource: " + resource.getName() + " "
               + getStartDateString() + "-" + getEndDateString()
               + " Length: " + getDuration() + " hours" + " Complete: "
               + ((int) (getComplete() * 100)) + "%";
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return name;
    }

    /**
     * _more_
     *
     * @param g _more_
     * @param isHilite _more_
     * @param from _more_
     * @param to _more_
     */
    private void paintDependency(Graphics g, boolean isHilite,
                                 GanttTask from, GanttTask to) {
        int fromX = from.mainBox.x + from.mainBox.width + 1;
        int fromY = from.mainBox.y + from.mainBox.height / 2;
        int anchX = fromX + 8;
        int anchY = fromY;
        if ( !from.visible || !to.visible) {
            return;
        }
        g.setColor((view.isHilite(from) || view.isHilite(to))
                   ? view.hiliteColor
                   : Color.black);
        g.drawLine(fromX, fromY, anchX, anchY);
        int   off = 4;
        int[] xs  = { anchX - off, anchX, anchX + off, anchX, anchX - off };
        int[] ys  = { anchY, anchY - off, anchY, anchY + off, anchY };
        //      g.fillPolygon (xs, ys, xs.length);


        int toX = (to.isMilestone()
                   ? to.mainBox.x + to.mainBox.width - MILESTONE_WIDTH
                   : to.mainBox.x);
        int toY = to.mainBox.y + to.mainBox.height;
        if (fromY < toY) {
            toY = to.mainBox.y;
        }
        if (to.isMilestone()) {
            toY = to.mainBox.y + to.mainBox.height / 2;
        }

        if (toX >= anchX) {
            g.drawLine(anchX, anchY, anchX, toY);
            g.drawLine(anchX, toY, toX, toY);
        } else {
            int tmpY;
            if (toY > anchY) {
                tmpY = toY - 10;
            } else {
                tmpY = toY + 10;
            }
            g.drawLine(anchX, anchY, anchX, tmpY);
            g.drawLine(anchX, tmpY, toX - 10, tmpY);
            g.drawLine(toX - 10, tmpY, toX - 10, toY);
            g.drawLine(toX - 10, toY, toX, toY);
        }
        int aw = 4;
        int ah = 2;
        g.drawLine(toX, toY, toX - aw, toY - ah);
        g.drawLine(toX, toY, toX - aw, toY + ah);
    }


    /**
     * _more_
     *
     * @param g _more_
     * @param isHilite _more_
     * @param l _more_
     * @param isTo _more_
     */
    private void paintDependencies(Graphics g, boolean isHilite, Vector l,
                                   boolean isTo) {
        if ( !view.getShow(view.SHOW_DEPENDENCIES)) {
            return;
        }
        for (int i = 0; i < l.size(); i++) {
            GanttTask other = (GanttTask) l.elementAt(i);
            if (isTo) {
                paintDependency(g, isHilite, this, other);
            } else {
                paintDependency(g, isHilite, other, this);
            }
        }
    }

    /**
     * _more_
     *
     * @param g _more_
     */
    public void paintSelection(Graphics g) {
        Rectangle r  = mainBox;
        int       L  = r.x - H_SEL_WIDTH;
        int       T  = r.y - H_SEL_WIDTH;
        int       R  = r.x + r.width - H_SEL_WIDTH + 1;
        int       B  = r.y + r.height - H_SEL_WIDTH + 1;
        int       MY = T + r.height / 2;
        int       MX = L + r.width / 2;
        g.setColor(Color.black);
        g.fillRect(L, T, SEL_WIDTH, SEL_WIDTH);
        g.fillRect(L, B, SEL_WIDTH, SEL_WIDTH);
        g.fillRect(R, T, SEL_WIDTH, SEL_WIDTH);
        g.fillRect(R, B, SEL_WIDTH, SEL_WIDTH);
    }




}

