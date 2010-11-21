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

import java.applet.*;


import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.net.*;

import java.util.*;


/**
 * Class RowList _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class RowList extends ScrollCanvas {

    /** _more_          */
    public static final int ROWHEIGHT = GanttView.ROWHEIGHT;

    /** _more_          */
    GanttView view;

    /** _more_          */
    GanttTask dragTask = null;

    /** _more_          */
    GanttTask dragParent;

    /** _more_          */
    int dragListIndex;

    /** _more_          */
    MouseEvent dragEvent;

    /** _more_          */
    int dragY;

    /** _more_          */
    Point dragPoint = new Point();

    /** _more_          */
    boolean dragOk;


    /** _more_          */
    int dragTaskNameX;


    /**
     * _more_
     *
     * @param view _more_
     */
    public RowList(GanttView view) {
        this.view = view;
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                rowListClicked(me);
            }
            public void mouseReleased(MouseEvent me) {
                rowListReleased(me);
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent me) {
                rowListDragged(me);
            }
        });
    }


    /**
     * _more_
     *
     * @param e _more_
     */
    public void rowListReleased(MouseEvent e) {
        if ((dragTask == dragParent) || (dragTask == null) || (e.getY() < 0)
                || (e.getY() > bounds().height)) {
            dragClear();
            return;
        }

        String parentId = "-1";
        if (dragParent != null) {
            if (dragTask.isAncestor(dragParent)) {
                view.showMessage(
                    "Error: Cannot add an ancestor as a child of a descendant\n");
                dragClear();
                return;
            }
            parentId = dragParent.getId();
        }

        if (dragParent == null) {
            view.topTasks.removeElement(dragTask);
            if ((dragListIndex >= 0)
                    && (dragListIndex <= view.topTasks.size())) {
                view.topTasks.insertElementAt(dragTask, dragListIndex);
            } else {
                view.topTasks.addElement(dragTask);
            }
        }
        GanttTask oldParent = dragTask.getParentTask();
        dragTask.setParentTask(dragParent, dragListIndex);

        Hashtable extra = new Hashtable();
        extra.put("%PARENT%", parentId);
        extra.put("%INDEX%", "" + dragListIndex);
        view.handleCommands(view.changeParentCommands, GU.vector(dragTask),
                            extra, null);

        if (oldParent != null) {
            oldParent.updateTimes();
        }
        dragTask.updateTimes();

        view.setTaskPositions();
        view.repaint();
        dragClear();
    }

    /**
     * _more_
     */
    private void dragClear() {
        dragTask   = null;
        dragParent = null;
        dragEvent  = null;
        view.message("");
        repaint();
    }


    /**
     * _more_
     *
     * @param e _more_
     */
    public void rowListDragged(MouseEvent e) {

        if ((view.changeParentCommands == null)
                || (e.getModifiers() & e.BUTTON1_MASK) == 0) {
            return;
        }

        if (dragEvent == null) {
            dragEvent = e;
            return;
        }
        if ((dragTask == null)
                && (GU.distance(e.getX(), e.getY(), dragEvent.getX(),
                                dragEvent.getY()) < 5.0)) {
            return;
        }

        int y = translateInputY(e.getY());
        dragPoint.x = translateInputX(e.getX());
        dragPoint.y = translateInputY(e.getY());

        if (dragTask == null) {
            dragTask = view.findTask(translateInputX(e.getX()), y,
                                     view.BOX_HEADER);
            if (dragTask == null) {
                return;
            }
        }

        int       position       = dragY / ROWHEIGHT;
        GanttTask taskAtPosition = view.findTaskAtIndex(position);
        dragParent    = null;
        dragListIndex = -1;

        int rem = y % ROWHEIGHT;
        dragY = y - rem;

        String message = "Move task \"" + dragTask.getName() + "\" ";

        if (rem < ((double) ROWHEIGHT / 3.0)) {
            //top of box
            dragY += 2;
            if (taskAtPosition != null) {
                dragParent = taskAtPosition.getParentTask();
                if (dragParent != null) {
                    dragListIndex =
                        dragParent.getSubTasks().indexOf(taskAtPosition);
                    message += "as a child of task \"" + dragParent + "\" ";
                } else {
                    dragListIndex = view.topTasks.indexOf(taskAtPosition);
                    message       += "to the top level ";
                }
                message += "before task \"" + taskAtPosition + "\"";
            }
        } else if (rem < (2.0 * (double) ROWHEIGHT / 3.0)) {
            //center of box
            dragY      += ROWHEIGHT / 2;
            dragParent = taskAtPosition;
            if (dragParent == null) {
                message += "to the top level ";
            } else {
                message += "as the last child of  task \"" + dragParent
                           + "\"";
            }
        } else {
            //bottom of box
            dragY += ROWHEIGHT - 1;
            if (taskAtPosition != null) {
                dragParent = taskAtPosition.getParentTask();
                if (dragParent != null) {
                    dragListIndex =
                        1 + dragParent.getSubTasks().indexOf(taskAtPosition);
                    message += "as a child of task \"" + dragParent + "\" ";
                } else {
                    dragListIndex = 1 + view.topTasks.indexOf(taskAtPosition);
                    message       += "to the top level ";
                }
                message += "after task \"" + taskAtPosition + "\"";
            }
        }

        dragOk = true;
        if (dragParent != null) {
            dragOk = !dragTask.isAncestor(dragParent);
        }


        Rectangle b = bounds();
        if ((e.getY() < 0) || (e.getY() > b.height)) {
            if (vScroll != null) {
                vScroll.setValue(vScroll.getValue()
                                 + (e.getY() - vScroll.getValue()));
                view.repaint();
            }
        }

        if (dragY < 0) {
            message = "";
        }

        if (dragOk) {
            view.message(message);
        } else {
            if (dragTask == dragParent) {
                view.message("Cannot move the task to itself");
            } else {
                view.message(
                    "Cannot add an ancestor as a child of a descendant");
            }
        }



        Vector visibleTasks = view.visibleTasks;
        int    tabs         = ((dragParent == null)
                               ? 0
                               : dragParent.getTabs() + 1);
        dragTaskNameX = GanttTask.getNameX(tabs);
        repaint();

    }



    /**
     * _more_
     *
     * @param e _more_
     */
    public void rowListClicked(MouseEvent e) {
        int x = translateInputX(e.getX());
        int y = translateInputY(e.getY());

        if ((e.getModifiers() & e.BUTTON1_MASK) == 0) {
            view.showTaskMenu(view.findTask(x, y, view.BOX_HEADER), false,
                              this, e.getX(), e.getY());
            return;
        }
        GanttTask task = view.findTask(x, y, view.BOX_CLICK);
        if (task == null) {
            return;
        }
        task.setChildrenVisible( !task.getChildrenVisible());
        view.setTaskPositions();
        view.repaintAll();
    }


    /**
     * _more_
     *
     * @param g _more_
     */
    public void paintInner(Graphics g) {
        Rectangle b = bounds();
        g.setColor(Color.black);
        g.drawRect(translateInputX(0), translateInputY(0), b.width - 1,
                   b.height - 1);

        Vector visibleTasks = view.visibleTasks;
        for (int i = 0; i < visibleTasks.size(); i++) {
            GanttTask task = (GanttTask) visibleTasks.elementAt(i);
            task.paintHeader(g);
        }

        if (dragTask != null) {
            g.setColor(Color.gray);
            int olx = translateInputX(0);
            int orx = translateInputX(bounds().width);
            g.drawLine(olx, dragY, orx, dragY);
            g.drawLine(olx, dragY - 1, orx, dragY - 1);
            //      g.setColor (Color.lightGray);
            g.drawString(dragTask.getName(), dragTaskNameX,
                         dragY + dragTask.mainBox.height);

            if ( !dragOk) {
                g.setColor(Color.black);
                int aw  = 16;
                int aw2 = aw / 2;
                int ax  = dragPoint.x - aw;
                g.fillArc(ax, dragPoint.y, aw, aw, 0, 360);
                g.setColor(Color.white);
                g.fillArc(ax + 2, dragPoint.y + 2, aw - 4, aw - 4, 0, 360);
                g.setColor(Color.black);
                g.drawLine(ax, dragPoint.y + aw2, ax + aw, dragPoint.y + aw2);
            }

        }

    }




}

