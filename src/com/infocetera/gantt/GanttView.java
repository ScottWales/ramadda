/*
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


import javax.swing.*;


/**
 * Class GanttView _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class GanttView extends ScrollCanvas implements MouseListener,
        MouseMotionListener, KeyListener, ItemListener {


    /** _more_          */
    public static final long DAY_FACTOR = 24 * 60 * 60 * 1000;

    /** _more_          */
    public static final int ROWHEIGHT = 20;

    /** _more_          */
    public static final Font boldLblFont = new Font("Times", Font.BOLD, 12);

    /** _more_          */
    public static final Font normalLblFont = new Font("Times", 0, 12);

    /** _more_          */
    public static final Font smallFont = new Font("Times", 0, 10);

    /** _more_          */
    public static String[] days = {
        "", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
        "Saturday"
    };

    /** _more_          */
    public static String[] shortDays = {
        "", "S", "M", "T", "W", "T", "F", "S"
    };

    /** _more_          */
    public static String[] medDays = {
        "", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
    };

    /** _more_          */
    public static String[] months = {
        "January", "February", "March", "April", "May", "June", "July",
        "August", "September", "October", "November", "December"
    };

    /** _more_          */
    public static String[] shortMonths = {
        "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct",
        "Nov", "Dec"
    };



    /** _more_          */
    public static final String CMD_COLORBY = "colorby";

    /** _more_          */
    public static final String CMD_TOGGLE_SUBTASKS = "togglesubtasks";



    /** _more_          */
    public static final String CMD_SCROLLTOPOSITION = "scrolltoposition";

    /** _more_          */
    public static final String CMD_SCROLLTODATE = "scrolltodate";

    /** _more_          */
    public static final String CMD_HILITE = "hilite";

    /** _more_          */
    public static final String CMD_SELECT = "select";

    /** _more_          */
    public static final String CMD_MESSAGE = "message";

    /** _more_          */
    public static final String CMD_URL = "url";

    /** _more_          */
    public static final String CMD_XML = "xml";

    /** _more_          */
    public static final String CMD_PAUSE = "pause";


    /** _more_          */
    public static final String CMD_SHOW = "show";



    /** _more_          */
    public static final String CMD_LEGEND_SHOW = "legend.show";

    /** _more_          */
    public static final String CMD_LEGEND_HIDE = "legend.hide";

    /** _more_          */
    public static final String CMD_TREE_SHOW = "tree.show";



    /** _more_          */
    public static final String CMD_REPAINT = "repaint";

    /** _more_          */
    public static final String CMD_FLOAT = "float";

    /** _more_          */
    public static final String CMD_RELOAD = "reload";



    /** _more_          */
    public static final String CMD_DISPLAY = "display";

    /** _more_          */
    public static final String CMD_DISPLAY_POPUP = "display.popup";

    /** _more_          */
    public static final String CMD_DISPLAY_DAY = "display.day";

    /** _more_          */
    public static final String CMD_DISPLAY_WEEK = "display.week";

    /** _more_          */
    public static final String CMD_DISPLAY_MONTH = "display.month";



    /** _more_          */
    public static final int DISPLAY_DAY = 0;

    /** _more_          */
    public static final int DISPLAY_WEEK = 1;

    /** _more_          */
    public static final int DISPLAY_MONTH = 2;

    /** _more_          */
    public static final String[] DISPLAY_NAMES = { " Day  ", " Week ",
            " Month" };

    /** _more_          */
    public static final double[] DISPLAY_X = { 30.0, 20.0, 2.5 };


    /** _more_          */
    public static final String PARAM_DATAURL = "dataurl";

    /** _more_          */
    public static final String PARAM_CHANGEDATECOMMANDS =
        "changedatecommands";

    /** _more_          */
    public static final String PARAM_CHANGEPARENTCOMMANDS =
        "changeparentcommands";

    /** _more_          */
    public static final String TAG_GANTT = "gantt";

    /** _more_          */
    public static final String TAG_COMMAND = "command";

    /** _more_          */
    public static final String TAG_DEPENDENCY = "dependency";

    /** _more_          */
    public static final String TAG_TASK = "task";

    /** _more_          */
    public static final String TAG_RESOURCE = "resource";

    /** _more_          */
    public static final String TAG_TYPE = "type";

    /** _more_          */
    public static final String TAG_STATUS = "status";

    /** _more_          */
    public static final String ATTR_CHILDRENVISIBLE = "childrenvisible";

    /** _more_          */
    public static final String ATTR_HILITECOLOR = "hilitecolor";

    /** _more_          */
    public static final String ATTR_BGCOLOR = "bgcolor";

    /** _more_          */
    public static final String ATTR_HEADERBGCOLOR = "headerbgcolor";

    /** _more_          */
    public static final String ATTR_ID = "id";

    /** _more_          */
    public static final String ATTR_COLOR = "color";

    /** _more_          */
    public static final String ATTR_RESOURCE = "resource";

    /** _more_          */
    public static final String ATTR_TYPE = "type";

    /** _more_          */
    public static final String ATTR_MILESTONE = "milestone";

    /** _more_          */
    public static final String ATTR_PRIORITY = "priority";

    /** _more_          */
    public static final String ATTR_STATUS = "status";

    /** _more_          */
    public static final String ATTR_TASKFILLCOLOR = "fillcolor";

    /** _more_          */
    public static final String ATTR_NAME = "name";

    /** _more_          */
    public static final String ATTR_START = "start";

    /** _more_          */
    public static final String ATTR_END = "end";

    /** _more_          */
    public static final String ATTR_COMPLETE = "complete";

    /** _more_          */
    public static final String ATTR_LENGTH = "length";

    /** _more_          */
    public static final String ATTR_COMMANDS = "commands";

    /** _more_          */
    public static final String ATTR_NUMTASKS = "numtasks";

    /** _more_          */
    public static final String ATTR_FROM = "from";

    /** _more_          */
    public static final String ATTR_TO = "to";

    /** _more_          */
    public static final String ATTR_LABEL = "label";



    /** _more_          */
    public static final GuiUtils GU = null;

    /** _more_          */
    public static Color bgColor = Color.white;

    /** _more_          */
    public static Color weekendColor = new Color(234, 234, 234);

    /** _more_          */
    public static Color headerBgColor = Color.white;

    /** _more_          */
    public static Color hiliteColor = Color.orange;

    /** _more_          */
    public static Color mouseOverColor = new Color(255, 255, 204);



    //  public static Color headerLineColor = new Color (0, 128,192);

    /** _more_          */
    public static Color headerLineColor = Color.gray;

    /** _more_          */
    public static Color clickBoxColor = new Color(255, 255, 204);

    /** _more_          */
    public static int POS_ID, POS2_ID, WIDTH_ID;

    /** _more_          */
    public static int POS_TASKNAME, POS2_TASKNAME, WIDTH_TASKNAME;

    /** _more_          */
    public static int POS_DURATION, POS2_DURATION, WIDTH_DURATION;

    /** _more_          */
    public static int POS_COMPLETE, POS2_COMPLETE, WIDTH_COMPLETE;

    /** _more_          */
    public static int POS_RESOURCE, POS2_RESOURCE, WIDTH_RESOURCE;

    /** _more_          */
    public static int POS_TYPE, POS2_TYPE, WIDTH_TYPE;

    /** _more_          */
    public static int POS_STATUS, POS2_STATUS, WIDTH_STATUS;

    /** _more_          */
    public static int POS_STARTDATE, POS2_STARTDATE, WIDTH_STARTDATE;

    /** _more_          */
    public static int POS_ENDDATE, POS2_ENDDATE, WIDTH_ENDDATE;

    /** _more_          */
    public static int WIDTH_COL1;

    /** _more_          */
    public static int VISIBLE_COL1;

    /** _more_          */
    public static int HEIGHT_HEADER = 30;



    /** _more_          */
    public static String[] TASKHEADER_TITLES;

    /** _more_          */
    public static int[] TASKHEADER_POS;

    /** _more_          */
    public static int[] TASKHEADER_POS2;

    static {
        int x = 0;

        POS_ID          = x;
        POS2_ID         = (x += 20);
        WIDTH_ID        = POS2_ID - POS_ID;

        POS_TASKNAME    = x;
        POS2_TASKNAME   = (x += 150);
        WIDTH_TASKNAME  = POS2_TASKNAME - POS_TASKNAME;

        POS_DURATION    = x;
        POS2_DURATION   = (x += 60);
        WIDTH_DURATION  = POS2_DURATION - POS_DURATION;

        POS_COMPLETE    = x;
        POS2_COMPLETE   = (x += 60);
        WIDTH_COMPLETE  = POS2_COMPLETE - POS_COMPLETE;

        POS_RESOURCE    = x;
        POS2_RESOURCE   = (x += 150);
        WIDTH_RESOURCE  = POS2_RESOURCE - POS_RESOURCE;

        POS_TYPE        = x;
        POS2_TYPE       = (x += 100);
        WIDTH_TYPE      = POS2_TYPE - POS_TYPE;

        POS_STATUS      = x;
        POS2_STATUS     = (x += 100);
        WIDTH_STATUS    = POS2_STATUS - POS_STATUS;

        POS_STARTDATE   = x;
        POS2_STARTDATE  = (x += 200);
        WIDTH_STARTDATE = POS2_STARTDATE - POS_STARTDATE;

        POS_ENDDATE     = x;
        POS2_ENDDATE    = (x += 200);
        WIDTH_ENDDATE   = POS2_ENDDATE - POS_ENDDATE;

        TASKHEADER_POS  = new int[] {
            POS_ID, POS_TASKNAME, POS_DURATION, POS_COMPLETE, POS_RESOURCE,
            POS_TYPE, POS_STATUS, POS_STARTDATE, POS_ENDDATE
        };

        TASKHEADER_POS2 = new int[] {
            POS2_ID, POS2_TASKNAME, POS2_DURATION, POS2_COMPLETE,
            POS2_RESOURCE, POS2_TYPE, POS2_STATUS, POS2_STARTDATE,
            POS2_ENDDATE
        };

        TASKHEADER_TITLES = new String[] {
            "", "Task name", "Length", "Complete", "Resource", "Type",
            "Status", "Start date", "End date"
        };



        WIDTH_COL1   = TASKHEADER_POS2[TASKHEADER_POS2.length - 1];
        VISIBLE_COL1 = POS2_TASKNAME;

    }
    ;


    /** _more_          */
    public static final int COLOR_DFLT = 0;

    /** _more_          */
    public static final int COLOR_RESOURCE = 1;

    /** _more_          */
    public static final int COLOR_TYPE = 2;

    /** _more_          */
    public static final int COLOR_PRIORITY = 3;

    /** _more_          */
    public static final int COLOR_STATUS = 4;

    /** _more_          */
    public static final String[] COLORBYS = { "default", "resource",
            "tasktype", "priority", "status" };

    /** _more_          */
    private int colorByIndex = 0;



    /** _more_          */
    private static final GregorianCalendar cal = new GregorianCalendar();


    /** _more_          */
    String changeDateCommands;

    /** _more_          */
    String changeParentCommands;

    /** _more_          */
    String error = null;


    /** _more_          */
    Frame legendFrame;

    /** _more_          */
    Vector treePanels = new Vector();


    /** _more_          */
    XmlUi xmlUi;



    /** _more_          */
    public static final int SHOW_DEPENDENCIES = 0;

    /** _more_          */
    public static final int SHOW_MOUSEOVER = 1;

    /** _more_          */
    public static final int SHOW_NAME = 2;

    /** _more_          */
    public static final int SHOW_RESOURCE = 3;

    /** _more_          */
    public static final int SHOW_STATUS = 4;

    /** _more_          */
    public static final int SHOW_TYPE = 5;

    /** _more_          */
    public static final int SHOW_PRIORITY = 6;

    /** _more_          */
    public static final int SHOW_LENGTH = 7;

    /** _more_          */
    public static boolean[] showState = {
        true, false, true, false, false, false, false, false
    };

    /** _more_          */
    public static final String[] SHOW_NAMES = {
        "Dependencies", "Mouse over", "Task name", "Resource", "Status",
        "Task type", "Priority", "Length"
    };

    /**
     * _more_
     *
     * @param t _more_
     *
     * @return _more_
     */
    public boolean getShow(int t) {
        return showState[t];
    }


    /** _more_          */
    StringBuffer lblBuff = new StringBuffer();

    /**
     * _more_
     *
     * @param task _more_
     *
     * @return _more_
     */
    public String getLabel(GanttTask task) {
        lblBuff.setLength(0);

        boolean didone = false;
        for (int i = SHOW_NAME; i <= SHOW_LENGTH; i++) {
            if ( !showState[i]) {
                continue;
            }
            if (didone) {
                lblBuff.append("/");
            }
            didone = true;
            switch (i) {

              case SHOW_NAME :
                  lblBuff.append(task.getName());
                  break;

              case SHOW_RESOURCE :
                  lblBuff.append(task.getResource().getName());
                  break;

              case SHOW_STATUS :
                  lblBuff.append(task.getStatus().getName());
                  break;

              case SHOW_TYPE :
                  lblBuff.append(task.getType().getName());
                  break;

              case SHOW_PRIORITY :
                  lblBuff.append(task.getPriority());
                  break;

              case SHOW_LENGTH :
                  lblBuff.append(task.getDuration());
                  break;
            }
        }
        return lblBuff.toString();
    }


    /** _more_          */
    boolean floating = false;

    /** _more_          */
    Frame floatFrame;




    /** _more_          */
    int displayType = DISPLAY_WEEK;

    /** _more_          */
    double xPerDay = DISPLAY_X[DISPLAY_WEEK];


    /** _more_          */
    GanttApplet ganttApplet;

    /** _more_          */
    private Component contents;

    /** _more_          */
    private RowList rowList;

    /** _more_          */
    private ScrollCanvas mainHeaderCanvas;

    /** _more_          */
    private ScrollCanvas taskHeaderCanvas;




    /** _more_          */
    Vector selectionSet = new Vector();


    /** _more_          */
    GanttTask hilite = null;

    /** _more_          */
    int initialMouseDX;

    /** _more_          */
    Hashtable movedTasks;

    /** _more_          */
    boolean mouseDown = false;

    /** _more_          */
    boolean dragPropagate = false;

    /** _more_          */
    boolean dragStart = false;

    /** _more_          */
    boolean dragEnd = false;

    /** _more_          */
    boolean dragComplete = false;


    /** _more_          */
    boolean changedDate = false;


    /** _more_          */
    private long actualMinDate;

    /** _more_          */
    private long actualMaxDate;

    /** _more_          */
    private long minDate = Long.MAX_VALUE;

    /** _more_          */
    private long maxDate = Long.MIN_VALUE;

    /** _more_          */
    private int numDays = 1;

    /** _more_          */
    private Calendar startCal;

    /** _more_          */
    private Calendar endCal;

    /** _more_          */
    private int fromYear;

    /** _more_          */
    private int fromMonth;

    /** _more_          */
    private int toYear;

    /** _more_          */
    private int toMonth;



    /** _more_          */
    private Vector commands = new Vector();

    /** _more_          */
    private Vector commandNumTasks = new Vector();

    /** _more_          */
    private Vector commandLabels = new Vector();

    /** _more_          */
    private Vector commandTypes = new Vector();

    /** _more_          */
    private Vector resources = new Vector();

    /** _more_          */
    private Hashtable resourceMap = new Hashtable();

    /** _more_          */
    private Vector types = new Vector();

    /** _more_          */
    private Hashtable typeMap = new Hashtable();

    /** _more_          */
    private Vector statuses = new Vector();

    /** _more_          */
    private Hashtable statusMap = new Hashtable();

    /** _more_          */
    boolean haveLoaded = false;

    /** _more_          */
    Vector allTasks = new Vector();

    /** _more_          */
    Vector visibleTasks = new Vector();

    /** _more_          */
    Vector topTasks = new Vector();




    /** _more_          */
    private Hashtable taskMap = new Hashtable();

    /** _more_          */
    boolean simpleUI = false;


    /**
     * _more_
     *
     * @param ga _more_
     */
    public GanttView(GanttApplet ga) {
        ganttApplet = ga;
        //    processXml ();
        //    if (topTasks.size() ==0)
        //      error = "No tasks defined";
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        simpleUI = ganttApplet.getParameter("simpleui", false);
        changeDateCommands =
            ganttApplet.getParameter(PARAM_CHANGEDATECOMMANDS);
        changeParentCommands =
            ganttApplet.getParameter(PARAM_CHANGEPARENTCOMMANDS);
    }



    /**
     * _more_
     */
    private void setMinMaxDate() {
        actualMinDate = Long.MAX_VALUE;
        actualMaxDate = Long.MIN_VALUE;
        for (int i = 0; i < allTasks.size(); i++) {
            GanttTask task  = (GanttTask) allTasks.elementAt(i);
            long      start = task.getStartDate();
            if (start < actualMinDate) {
                actualMinDate = start;
            }
            long end = task.getEndDate();
            if (end > actualMaxDate) {
                actualMaxDate = end;
            }
        }
        actualMaxDate += DAY_FACTOR;
    }



    /**
     * _more_
     */
    private void processXml() {
        String xml = "";
        long start = (System.currentTimeMillis()-1000*60*60)/1000;
        long end = System.currentTimeMillis()/1000;
        String tmpxml = "<root><task name=\"foo\" start=\"" + start +"\" end=\"" + end +"\"></task></root>";
        try {
            xml = GuiUtils.readUrl(
                ganttApplet.getFullUrl(
                    ganttApplet.getParameter(PARAM_DATAURL)));
            xml = tmpxml;
            //      System.err.println ("xml:" + xml);
            processXml(xml);
            if (topTasks.size() == 0) {
                return;
            }
            //      setAllTasks ();
            //      setDisplayType (displayType);
        } catch (Exception e) {
            print("error " + e +"\n" + IfcApplet.getStackTrace(e));
        }
    }


    /**
     * _more_
     *
     * @param xml _more_
     */
    private void processXml(String xml) {
        try {
            processXmlInner(xml);
            print("Processed xml");
        } catch(Exception exc) {
            print("Error:" + IfcApplet.getStackTrace(exc));
            print(xml);
        }
    }

    private void processXmlInner(String xml) {
        XmlNode root = XmlNode.parse(xml);
        Vector  top  = root.getChildren();
        if (top.size() != 1) {
            throw new IllegalArgumentException("Ill formed xml:" + xml);
        }
        XmlNode ganttNode = (XmlNode) top.elementAt(0);
        hiliteColor = ganttNode.getAttribute(ATTR_HILITECOLOR, hiliteColor);
        bgColor     = ganttNode.getAttribute(ATTR_BGCOLOR, bgColor);
        setBackground(bgColor);
        headerBgColor = ganttNode.getAttribute(ATTR_HEADERBGCOLOR,
                headerBgColor);

        Vector ganttNodes = ganttNode.getChildren();
        for (int i = 0; i < ganttNodes.size(); i++) {
            XmlNode node = (XmlNode) ganttNodes.elementAt(i);
            if (node.getTag().equals(TAG_TASK)) {
                processTasks(null, node);
            } else if (node.getTag().equals(TAG_RESOURCE)) {
                String id   = node.getAttribute(ATTR_ID, (String) null);
                String name = node.getAttribute(ATTR_NAME, id);
                if (id == null) {
                    id = name;
                }
                if (id != null) {
                    GanttResource resource =
                        findResource(id, name,
                                     node.getAttribute(ATTR_COLOR,
                                         (Color) null));
                }
            } else if (node.getTag().equals(TAG_TYPE)) {
                String id   = node.getAttribute(ATTR_ID, (String) null);
                String name = node.getAttribute(ATTR_NAME, id);
                if (id == null) {
                    id = name;
                }
                if (id != null) {
                    findType(id, name,
                             node.getAttribute(ATTR_COLOR, Color.white),
                             node.getAttribute(ATTR_MILESTONE, false));
                }
            } else if (node.getTag().equals(TAG_STATUS)) {
                String id   = node.getAttribute(ATTR_ID, (String) null);
                String name = node.getAttribute(ATTR_NAME, id);
                if (id == null) {
                    id = name;
                }
                if (id != null) {
                    findStatus(id, name,
                               node.getAttribute(ATTR_COLOR, (Color) null));
                }
            } else if (node.getTag().equals(TAG_COMMAND)) {
                String cmds  = node.getAttribute(ATTR_COMMANDS);
                String label = node.getAttribute(ATTR_LABEL);
                if ((cmds != null) && (label != null)) {
                    commands.addElement(cmds);
                    commandLabels.addElement(label);
                    commandTypes.addElement(node.getAttribute(ATTR_TYPE,
                            "task"));
                    commandNumTasks.addElement(
                        new Integer(node.getAttribute(ATTR_NUMTASKS, 1)));
                }
            } else if (node.getTag().equals(TAG_DEPENDENCY)) {
                String    from     = node.getAttribute(ATTR_FROM);
                String    to       = node.getAttribute(ATTR_TO);
                GanttTask fromTask = getTask(from);
                GanttTask toTask   = getTask(to);
                if ((fromTask != null) && (toTask != null)) {
                    fromTask.addToDependency(toTask);
                    toTask.addFromDependency(fromTask);
                }
            } else {
                System.err.println("Unknown tag:" + node.getTag());
            }
        }
    }

    /**
     * _more_
     *
     * @param res _more_
     */
    public void setDisplayType(int res) {
        displayType = res;
        xPerDay     = DISPLAY_X[displayType];
        if (topTasks.size() == 0) {
            return;
        }
        setMinMaxDate();

        //Force the minDate to start on a week boundary
        minDate = actualMinDate;
        maxDate = actualMaxDate;
        Calendar minCal = new GregorianCalendar();
        minCal.setTime(new Date(minDate));
        Calendar maxCal = new GregorianCalendar();
        maxCal.setTime(new Date(maxDate));
        switch (displayType) {

          case DISPLAY_WEEK :
              long minDow = minCal.get(Calendar.DAY_OF_WEEK);
              minDate -= ((minDow - 1) * DAY_FACTOR);
              long maxDow = maxCal.get(Calendar.DAY_OF_WEEK);
              maxDate += ((7 - maxDow) * DAY_FACTOR);
              break;

          case DISPLAY_MONTH :
              long minDom = minCal.get(Calendar.DAY_OF_MONTH);
              minDate -= ((minDom - 1) * DAY_FACTOR);
              long maxDom = maxCal.get(Calendar.DAY_OF_MONTH);
              long dim = (long) getDaysInMonth(maxCal.get(Calendar.MONTH),
                             maxCal.get(Calendar.YEAR));
              maxDate += ((dim - maxDom - 1) * DAY_FACTOR);
              break;
        }

        Rectangle b = bounds();
        while (dateToX(maxDate) < b.width) {
            maxDate += DAY_FACTOR;
        }

        setDateRange(minDate, maxDate);

        setTaskPositions();
        setScrollbarValues();
        hilite = null;
        repaintAll();
    }



    /**
     * _more_
     *
     * @param min _more_
     * @param max _more_
     */
    private void setDateRange(long min, long max) {
        minDate  = min;
        maxDate  = max;
        numDays  = (int) ((maxDate - minDate) / DAY_FACTOR) + 1;


        startCal = new GregorianCalendar();
        startCal.setTime(new Date(minDate));
        endCal = new GregorianCalendar();
        endCal.setTime(new Date(maxDate));

        fromYear  = startCal.get(Calendar.YEAR);
        fromMonth = startCal.get(Calendar.MONTH);
        toYear    = endCal.get(Calendar.YEAR);
        toMonth   = endCal.get(Calendar.MONTH);

        setScrollbarValues();
        setTaskPositions();

    }


    /**
     * _more_
     *
     * @param parent _more_
     * @param taskNode _more_
     */
    public void processTasks(GanttTask parent, XmlNode taskNode) {
        String resourceName = taskNode.getAttribute(ATTR_RESOURCE,
                                  "Resource");
        String typeId   = taskNode.getAttribute(ATTR_TYPE, "");
        String statusId = taskNode.getAttribute(ATTR_STATUS, "");
        int    priority = taskNode.getAttribute(ATTR_PRIORITY, 1);
        String name     = taskNode.getAttribute(ATTR_NAME, "");
        String id       = taskNode.getAttribute(ATTR_ID, "");
        long start = (long) (1000)
                     * taskNode.getAttribute(ATTR_START, (long) 0);
        long end = (long) (1000) * taskNode.getAttribute(ATTR_END, (long) 0);
        int    length   = taskNode.getAttribute(ATTR_LENGTH, 0);
        double complete = taskNode.getAttribute(ATTR_COMPLETE, 0.0);
        boolean childrenVisible = taskNode.getAttribute(ATTR_CHILDRENVISIBLE,
                                      true);


        GanttResource resource = findResource(resourceName, resourceName,
                                     null);
        TaskType    type   = findType(typeId, typeId, Color.white, false);
        GanttStatus status = findStatus(statusId, statusId, null);
        GanttTask newTask = new GanttTask(this, id, name, start, end, length,
                                          complete, resource, status, type,
                                          priority);


        taskMap.put(id, newTask);
        if (parent != null) {
            newTask.setParentTask(parent);
        } else {
            topTasks.addElement(newTask);
        }


        Vector childTaskNodes = taskNode.getChildren();
        for (int i = 0; i < childTaskNodes.size(); i++) {
            XmlNode childTaskNode = (XmlNode) childTaskNodes.elementAt(i);
            processTasks(newTask, childTaskNode);
        }
        if ( !childrenVisible) {
            newTask.setChildrenVisible(false);
        }
    }


    /**
     * _more_
     */
    public void doLoad() {
        hilite          = null;
        selectionSet    = new Vector();

        commandTypes    = new Vector();
        commands        = new Vector();
        commandNumTasks = new Vector();
        commandLabels   = new Vector();

        resources       = new Vector();
        resourceMap     = new Hashtable();

        types           = new Vector();
        typeMap         = new Hashtable();

        statuses        = new Vector();
        statusMap       = new Hashtable();

        Hashtable childrenVisibleMap = new Hashtable();
        for (int i = 0; i < allTasks.size(); i++) {
            GanttTask task = (GanttTask) allTasks.elementAt(i);
            childrenVisibleMap.put(task.getId(),
                                   new Boolean(task.getChildrenVisible()));
        }



        allTasks     = new Vector();
        visibleTasks = new Vector();
        topTasks     = new Vector();
        taskMap      = new Hashtable();
        processXml();
        setAllTasks();

        for (int i = 0; i < allTasks.size(); i++) {
            GanttTask task = (GanttTask) allTasks.elementAt(i);
            Boolean   b    = (Boolean) childrenVisibleMap.get(task.getId());
            if (b != null) {
                task.setChildrenVisible(b.booleanValue());
            }
        }

        setDisplayType(displayType);
        makeTree();
        repaintAll();
        setScrollbarValues();
    }


    /**
     * _more_
     */
    private void makeTree() {
        for (int i = 0; i < treePanels.size(); i++) {
            ((TreePanel) treePanels.elementAt(i)).makeTree();
        }
    }

    /**
     * _more_
     *
     * @param p _more_
     */
    public void removeTreePanel(TreePanel p) {
        treePanels.removeElement(p);
    }

    /**
     * _more_
     *
     * @param date _more_
     */
    public void scrollToDate(long date) {
        int x = dateToX(date);
        if (hScroll != null) {
            hScroll.setValue(x);
        }
        repaintAll();
    }

    /**
     * _more_
     *
     * @param task _more_
     */
    public void scrollToPosition(GanttTask task) {
        if (vScroll != null) {
            vScroll.setValue(task.getPosition() * ROWHEIGHT);
        }
        repaintAll();
    }


    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     * @param w _more_
     * @param h _more_
     */
    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);
        setScrollbarValues();
    }

    /**
     * _more_
     */
    private void setScrollbarValues() {

        Rectangle b = bounds();
        if (hScroll != null) {
            hScroll.setValues(hScroll.getValue(), b.width, 0,
                              daysToWidth(numDays));
        }

        if (vScroll != null) {
            vScroll.setValues(vScroll.getValue(), b.height, 0,
                              allTasks.size() * ROWHEIGHT);
        }

    }

    /**
     * _more_
     */
    void repaintAll() {
        if (rowList == null) {
            return;
        }
        repaint();
        rowList.repaint();
        mainHeaderCanvas.repaint();
        taskHeaderCanvas.repaint();
    }

    /** _more_          */
    Hashtable seen;

    /**
     * _more_
     */
    protected void setAllTasks() {
        for (int i = 0; i < topTasks.size(); i++) {
            setAllTasks((GanttTask) topTasks.elementAt(i));
        }
    }

    /**
     * _more_
     *
     * @param parent _more_
     */
    protected void setAllTasks(GanttTask parent) {
        allTasks.addElement(parent);
        Vector sub = parent.getSubTasks();
        for (int i = 0; i < sub.size(); i++) {
            setAllTasks((GanttTask) sub.elementAt(i));
        }
    }

    /**
     * _more_
     */
    protected void setTaskPositions() {
        int position = 0;
        visibleTasks = new Vector();
        seen         = new Hashtable();
        for (int i = 0; i < topTasks.size(); i++) {
            GanttTask task = (GanttTask) topTasks.elementAt(i);
            position = recurseTaskPositions(task, position, true);
        }

        seen     = new Hashtable();
        position = 0;
        for (int i = 0; i < topTasks.size(); i++) {
            GanttTask task = (GanttTask) topTasks.elementAt(i);
            position = recurseTaskPositions(task, position, false);
        }
        makeTree();
    }


    /**
     * _more_
     *
     * @param task _more_
     * @param position _more_
     * @param doingVisible _more_
     *
     * @return _more_
     */
    protected int recurseTaskPositions(GanttTask task, int position,
                                       boolean doingVisible) {
        if ((task == null) || (seen.get(task) != null)) {
            return position;
        }
        seen.put(task, task);
        if (doingVisible) {
            visibleTasks.addElement(task);
            task.setPosition(position++);
            if ( !task.getChildrenVisible()) {
                return position;
            }
        } else {
            task.setAbsolutePosition(position++);
        }


        Vector sub = task.getSubTasks();
        for (int i = 0; i < sub.size(); i++) {
            GanttTask child = (GanttTask) sub.elementAt(i);
            if (doingVisible && !child.getVisible()) {
                continue;
            }
            position = recurseTaskPositions(child, position, doingVisible);
        }
        return position;
    }



    /**
     * _more_
     *
     * @param id _more_
     * @param name _more_
     * @param color _more_
     *
     * @return _more_
     */
    public GanttResource findResource(String id, String name, Color color) {
        GanttResource resource = (GanttResource) resourceMap.get(id);
        if (resource == null) {
            resource = new GanttResource(this, id, name, color);
            resourceMap.put(id, resource);
            resources.addElement(resource);
        }
        return resource;
    }

    /**
     * _more_
     *
     * @param id _more_
     * @param name _more_
     * @param color _more_
     * @param milestone _more_
     *
     * @return _more_
     */
    public TaskType findType(String id, String name, Color color,
                             boolean milestone) {
        TaskType type = (TaskType) typeMap.get(id);
        if (type == null) {
            type = new TaskType(this, id, name, color, milestone);
            typeMap.put(id, type);
            types.addElement(type);
        }
        return type;
    }


    /**
     * _more_
     *
     * @param id _more_
     * @param name _more_
     * @param color _more_
     *
     * @return _more_
     */
    public GanttStatus findStatus(String id, String name, Color color) {
        GanttStatus status = (GanttStatus) statusMap.get(id);
        if (status == null) {
            status = new GanttStatus(this, id, name, color);
            statusMap.put(id, status);
            statuses.addElement(status);
        }
        return status;
    }



    /**
     * _more_
     *
     * @param msg _more_
     */
    public void message(String msg) {
        xmlUi.setLabel("message", msg);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Component getContents() {
        if (contents == null) {
            if (error != null) {
                return new Label(error);
            }
            contents = doMakeContents();
            if ( !simpleUI) {
                scrollToDate(actualMinDate - 7 * DAY_FACTOR);
            }
            //Initialize the display type to be week
            handleAction(CMD_DISPLAY_WEEK, null);
        }
        return contents;
    }







    /**
     * _more_
     *
     * @return _more_
     */
    private Component doMakeContents() {
        JScrollBar hScroll = getHScroll(500, 5000);
        JScrollBar vScroll = getVScroll(300, 3000);


        taskHeaderCanvas = new ScrollCanvas() {
            public void paintInner(Graphics g) {
                paintTaskHeader(g);
            }
        };


        rowList = new RowList(this);
        vScroll.addAdjustmentListener(rowList);
        rowList.vScroll = vScroll;


        JScrollBar rowListHScroll = rowList.getHScroll(VISIBLE_COL1,
                                        WIDTH_COL1);
        rowListHScroll.addAdjustmentListener(taskHeaderCanvas);
        taskHeaderCanvas.hScroll = rowListHScroll;



        mainHeaderCanvas         = new ScrollCanvas() {
            public void paintInner(Graphics g) {
                paintMainHeader(g);
            }
        };

        hScroll.addAdjustmentListener(mainHeaderCanvas);
        mainHeaderCanvas.hScroll = hScroll;

        rowList.setBackground(Color.white);
        mainHeaderCanvas.setSize(1000, HEIGHT_HEADER);
        taskHeaderCanvas.setSize(VISIBLE_COL1, HEIGHT_HEADER);
        rowList.setSize(VISIBLE_COL1, 1000);

        Component[] lcomps     = { taskHeaderCanvas, rowList,
                                   rowListHScroll };

        Component[] ccomps     = {
            mainHeaderCanvas, filler(), this, vScroll, hScroll, filler()
        };

        Component   taskHeader = GU.doLayout(lcomps, 1, GU.DS_N, GU.DS_NYN);
        Component   mainCanvas = GU.doLayout(ccomps, 2, GU.DS_YN, GU.DS_NYN);



        String      skinPath   = "/com/infocetera/gantt/defaultskin.xml";
        String uiXml = new String(GU.readResource(skinPath, getClass(),
                           true));
        XmlNode   uiRoot     = XmlNode.parse(uiXml).get(0);
        Hashtable components = new Hashtable();
        components.put("taskheader", taskHeader);
        components.put("maincanvas", mainCanvas);
        xmlUi = new XmlUi(ganttApplet, uiRoot, components, this, this);
        return xmlUi.getContents();
    }

    /**
     * _more_
     *
     * @param label _more_
     * @param command _more_
     *
     * @return _more_
     */
    MenuItem makeMenuItem(String label, String command) {
        MenuItem mi = new MenuItem(label);
        mi.setActionCommand(command);
        mi.addActionListener(this);
        return mi;
    }

    /**
     * _more_
     *
     * @param src _more_
     * @param x _more_
     * @param y _more_
     */
    public void showDisplayMenu(Component src, int x, int y) {
        PopupMenu popup = new PopupMenu();
        doPopup(popup, src, x, y);
    }

    /**
     * _more_
     *
     * @param popup _more_
     * @param ae _more_
     */
    public void doPopup(PopupMenu popup, ActionEvent ae) {
        Component src = (Component) ae.getSource();
        doPopup(popup, src, 0, src.bounds().y + src.bounds().height);
    }

    /**
     * _more_
     *
     * @param popup _more_
     * @param src _more_
     * @param x _more_
     * @param y _more_
     */
    public void doPopup(PopupMenu popup, Component src, int x, int y) {
        this.add(popup);
        popup.show(src, x, y);
    }




    /**
     * _more_
     *
     * @param l _more_
     * @param init _more_
     * @param cbg _more_
     *
     * @return _more_
     */
    Checkbox getCheckbox(String l, boolean init, CheckboxGroup cbg) {
        Checkbox b;
        if (cbg != null) {
            b = new Checkbox(l, cbg, init);
        } else {
            b = new Checkbox(l, init);
        }
        b.addItemListener(this);
        return b;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    Panel filler() {
        Panel p = new Panel();
        //    p.setBackground (bgColor);
        return p;
    }




    /** _more_          */
    public static final int BOX_MAIN = 1;

    /** _more_          */
    public static final int BOX_CLICK = 2;

    /** _more_          */
    public static final int BOX_HEADER = 3;


    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     * @param box _more_
     *
     * @return _more_
     */
    public GanttTask findTask(int x, int y, int box) {
        GanttTask closest     = null;
        double    minDistance = 10.0;
        Point     p           = new Point(x, y);

        for (int i = 0; i < visibleTasks.size(); i++) {
            GanttTask task = (GanttTask) visibleTasks.elementAt(i);
            double    tmp  = GU.distance(p, ((box == BOX_MAIN)
                                             ? task.mainBox
                                             : ((box == BOX_CLICK)
                    ? task.headerClickBox
                    : task.headerBox)));
            if (tmp < minDistance) {
                closest     = task;
                minDistance = tmp;
            }

        }
        return closest;
    }

    /**
     * _more_
     *
     * @param e _more_
     */
    public void keyPressed(KeyEvent e) {
        boolean changed = true;
        if (e.getKeyChar() == 'm') {
            handleAction(CMD_DISPLAY_MONTH);
        } else if (e.getKeyChar() == 'w') {
            handleAction(CMD_DISPLAY_WEEK);
        } else if (e.getKeyChar() == 'd') {
            handleAction(CMD_DISPLAY_DAY);
        } else {
            changed = false;
        }
        if (changed) {
            repaint();
        }
    }


    /**
     * _more_
     *
     * @param selectedTask _more_
     * @param fromMain _more_
     * @param src _more_
     * @param x _more_
     * @param y _more_
     */
    public void showTaskMenu(GanttTask selectedTask, boolean fromMain,
                             Component src, int x, int y) {

        boolean   didOne = true;
        PopupMenu popup  = new PopupMenu();
        MenuItem  mi;

        boolean   addSeparator = false;

        if (selectedTask != null) {
            mi = new MenuItem("Show tree map");
            mi.addActionListener(new TaskActionListener(this,
                    GuiUtils.vector(selectedTask), CMD_TREE_SHOW));
            popup.add(mi);

            if (selectedTask.isParent()) {
                if (selectedTask.getChildrenVisible()) {
                    mi = new MenuItem("Hide sub-tasks");
                } else {
                    mi = new MenuItem("Show sub-tasks");
                }
                mi.addActionListener(new TaskActionListener(this,
                        GuiUtils.vector(selectedTask), CMD_TOGGLE_SUBTASKS));
                popup.add(mi);
            }


            addSeparator = true;
        }


        if ( !fromMain && (selectedTask != null)) {
            didOne = true;
            mi     = new MenuItem("Scroll to");
            String cmd = (fromMain
                          ? CMD_SCROLLTOPOSITION
                          : CMD_SCROLLTODATE);
            mi.addActionListener(new TaskActionListener(this,
                    GU.vector(selectedTask), cmd + ";" + CMD_SELECT));
            popup.add(mi);
            addSeparator = true;
        }
        if (addSeparator) {
            popup.addSeparator();
        }

        for (int i = 0; i < commands.size(); i++) {
            Integer need        = (Integer) commandNumTasks.elementAt(i);
            int     numTasks    = need.intValue();
            Vector  actionTasks = new Vector();
            if (numTasks == 1) {
                if (selectedTask == null) {
                    continue;
                }
                actionTasks.addElement(selectedTask);
            } else if (fromMain && (numTasks <= selectionSet.size())) {
                for (int selIdx = 0; selIdx < numTasks; selIdx++) {
                    actionTasks.addElement(selectionSet.elementAt(selIdx));
                }
            }
            if (actionTasks.size() != numTasks) {
                continue;
            }
            String label = commandLabels.elementAt(i).toString();

            String type  = (String) commandTypes.elementAt(i);
            if (type.equals("dependency") && (numTasks == 1)) {}

            didOne = true;
            for (int actionTaskIdx = 0; actionTaskIdx < actionTasks.size();
                    actionTaskIdx++) {
                GanttTask actionTask =
                    (GanttTask) actionTasks.elementAt(actionTaskIdx);
                label = GuiUtils.replace(label,
                                         "%NAME" + (actionTaskIdx + 1) + "%",
                                         actionTask.getName());
                if (actionTaskIdx == 0) {
                    label = GuiUtils.replace(label, "%NAME%",
                                             actionTask.getName());
                }

            }
            mi = new MenuItem(label);
            mi.addActionListener(new TaskActionListener(this, actionTasks,
                    (String) commands.elementAt(i)));
            popup.add(mi);
        }
        if ( !didOne) {
            return;
        }


        doPopup(popup, src, x, y);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public int getColorBy() {
        return colorByIndex;
    }

    /**
     * _more_
     */
    public void stop() {
        super.stop();
        if (legendFrame != null) {
            legendFrame.dispose();
            legendFrame = null;
        }
        for (int i = 0; i < treePanels.size(); i++) {
            ((TreePanel) treePanels.elementAt(i)).stop();
        }

    }


    /**
     * Class TaskActionListener _more_
     *
     *
     * @author IDV Development Team
     * @version $Revision: 1.3 $
     */
    private static class TaskActionListener implements ActionListener {

        /** _more_          */
        Vector tasks;

        /** _more_          */
        GanttView view;

        /** _more_          */
        String command;

        /**
         * _more_
         *
         * @param view _more_
         * @param task _more_
         * @param command _more_
         */
        public TaskActionListener(GanttView view, GanttTask task,
                                  String command) {
            tasks = new Vector();
            tasks.addElement(task);
            this.view    = view;
            this.command = command;
        }


        /**
         * _more_
         *
         * @param view _more_
         * @param tasks _more_
         * @param command _more_
         */
        public TaskActionListener(GanttView view, Vector tasks,
                                  String command) {
            this.view    = view;
            this.tasks   = tasks;
            this.command = command;
        }


        /**
         * _more_
         *
         * @param e _more_
         */
        public void actionPerformed(ActionEvent e) {
            view.handleCommands(command, tasks, null, e);
        }
    }


    /**
     * _more_
     *
     * @param e _more_
     */
    public void itemStateChanged(ItemEvent e) {
        Object sel = e.getItemSelectable();
        if (true) {
            repaint();
            return;
        }
    }



    /**
     * _more_
     *
     * @param e _more_
     */
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        handleAction(action, e);
    }


    /**
     * _more_
     *
     * @param commands _more_
     * @param commandTasks _more_
     * @param extra _more_
     * @param ae _more_
     */
    protected void handleCommands(String commands, Vector commandTasks,
                                  Hashtable extra, ActionEvent ae) {
        Vector cmds = GuiUtils.parseCommands(commands);
        for (int i = 0; i < cmds.size(); i++) {
            String[] sa     = (String[]) cmds.elementAt(i);
            String   func   = sa[0];
            String   params = sa[1];
            try {
                handleFunction(func, params, commandTasks, extra, ae);
            } catch (Exception exc) {
                print("Handling action:" + func + "\n" + exc);
                exc.printStackTrace();
            }
        }
    }

    /**
     * _more_
     *
     * @param func _more_
     */
    private void handleAction(String func) {
        handleAction(func, null);
    }

    /**
     * _more_
     *
     * @param func _more_
     * @param ae _more_
     */
    private void handleAction(String func, ActionEvent ae) {
        try {
            handleCommands(func, new Vector(), null, ae);
        } catch (Exception exc) {
            print("Handling action:" + func + "\n" + exc);
            exc.printStackTrace();
        }
    }


    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    public GanttTask getTask(String id) {
        if (id == null) {
            return null;
        }
        return (GanttTask) taskMap.get(id);
    }

    /**
     * _more_
     *
     * @param id _more_
     * @param tasks _more_
     *
     * @return _more_
     */
    private GanttTask getTask(String id, Vector tasks) {
        GanttTask task = getTask(id);
        if ((task == null) && (tasks != null) && (tasks.size() > 0)) {
            task = (GanttTask) tasks.elementAt(0);
        }
        return task;
    }





    /**
     * _more_
     *
     * @param func _more_
     * @param params _more_
     * @param commandTasks _more_
     * @param extra _more_
     * @param ae _more_
     *
     * @throws Exception _more_
     */
    protected void handleFunction(String func, String params,
                                  Vector commandTasks, Hashtable extra,
                                  ActionEvent ae)
            throws Exception {

        if (func.equals(CMD_SHOW)) {
            String[] args  = XmlUi.extractTwoArgs(params);
            String   type  = args[0].toLowerCase();
            boolean  value = new Boolean(args[1]).booleanValue();
            for (int i = 0; i < SHOW_NAMES.length; i++) {
                if (SHOW_NAMES[i].toLowerCase().startsWith(type)) {
                    showState[i] = value;
                    break;
                }
            }
            for (int i = 0; i < allTasks.size(); i++) {
                GanttTask task = (GanttTask) allTasks.elementAt(i);
                task.setLabel(null);
            }
            repaint();
            return;
        }


        if (func.equals(CMD_COLORBY)) {
            colorByIndex = GuiUtils.getIndex(COLORBYS, null, params,
                                             colorByIndex);
            repaint();
            return;
        }

        if (func.equals(CMD_SCROLLTODATE)) {
            GanttTask task = getTask(params, commandTasks);
            if (task != null) {
                scrollToDate(task.getStartDate());
            }
            return;
        }
        if (func.equals(CMD_SCROLLTOPOSITION)) {
            GanttTask task = getTask(params, commandTasks);
            if (task != null) {
                scrollToPosition(task);
            }
            return;
        }

        if (func.equals(CMD_SELECT)) {
            GanttTask task = getTask(params, commandTasks);
            selectionSet.removeAllElements();
            if (task != null) {
                selectionSet.addElement(task);
            }
            return;
        }



        if (func.equals(CMD_MESSAGE)) {
            showMessage(params);
            return;
        }

        if (func.equals(CMD_URL) || func.equals(CMD_XML)) {
            StringBuffer ids        = new StringBuffer();
            StringBuffer startDates = new StringBuffer();
            StringBuffer endDates   = new StringBuffer();
            StringBuffer completes  = new StringBuffer();
            for (int i = 0; i < commandTasks.size(); i++) {
                GanttTask task = (GanttTask) commandTasks.elementAt(i);
                if (i > 0) {
                    ids.append(",");
                    startDates.append(",");
                    endDates.append(",");
                    completes.append(",");
                }
                ids.append("" + task.getId());
                completes.append("" + task.getComplete());
                startDates.append("" + (task.getStartDate() / 1000));
                endDates.append("" + (task.getEndDate() / 1000));
                params = GuiUtils.replace(params, "%ID" + (i + 1) + "%",
                                          task.getId());
                params = GuiUtils.replace(params,
                                          "%RESOURCEID" + (i + 1) + "%",
                                          task.getResource().getId());
                params = GuiUtils.replace(params,
                                          "%STARTDATE" + (i + 1) + "%",
                                          "" + task.getStartDate() / 1000);
                params = GuiUtils.replace(params, "%ENDDATE" + (i + 1) + "%",
                                          "" + task.getEndDate() / 1000);
                if (i == 0) {
                    params = GuiUtils.replace(params, "%ID%", task.getId());
                    params = GuiUtils.replace(params, "%RESOURCEID%",
                            task.getResource().getId());
                    params = GuiUtils.replace(params, "%STARTDATE%",
                            "" + task.getStartDate() / 1000);
                    params = GuiUtils.replace(params, "%ENDDATE%",
                            "" + task.getEndDate() / 1000);
                }
            }
            params = GuiUtils.replace(params, "%IDS%", ids.toString());
            params = GuiUtils.replace(params, "%COMPLETES%",
                                      completes.toString());
            params = GuiUtils.replace(params, "%STARTDATES%",
                                      startDates.toString());
            params = GuiUtils.replace(params, "%ENDDATES%",
                                      endDates.toString());
            params = replace(params, extra);



            params = ganttApplet.getFullUrl(params);
            if (func.equals(CMD_URL)) {
                showUrl(params, "gantt");
            } else {
                showResultXml(GuiUtils.readUrl(params));
            }
            return;
        }

        if (func.equals(CMD_RELOAD)) {
            doLoad();
            return;
        }


        if (func.equals(CMD_LEGEND_SHOW)) {
            if (legendFrame == null) {
                legendFrame = new Frame("Legend messages");
                legendFrame.setLayout(new BorderLayout());


                Label  lbl = null;
                Panel  keyPanel;
                Vector panels = new Vector();

                keyPanel = new Panel(new GridLayout(0, 2, 0, 4));
                for (int i = 0; i < resources.size(); i++) {
                    GanttResource resource =
                        (GanttResource) resources.elementAt(i);
                    keyPanel.add(new Label(resource.getName()));
                    lbl = new Label(" ");
                    lbl.setBackground(resource.getColor());
                    keyPanel.add(lbl);
                }
                keyPanel.add(new Label(" "));
                lbl = new Label("Resources", Label.CENTER);
                lbl.setFont(boldLblFont);
                panels.addElement(GU.topCenterBottom(GU.topCenterBottom(lbl,
                        keyPanel, null), null, null));


                keyPanel = new Panel(new GridLayout(0, 2, 0, 4));
                for (int i = 0; i < types.size(); i++) {
                    TaskType type = (TaskType) types.elementAt(i);
                    keyPanel.add(new Label(type.getName()));
                    lbl = new Label(" ");
                    lbl.setBackground(type.getColor());
                    keyPanel.add(lbl);
                }
                keyPanel.add(new Label(" "));
                lbl = new Label("Task types", Label.CENTER);
                lbl.setFont(boldLblFont);
                panels.addElement(GU.topCenterBottom(GU.topCenterBottom(lbl,
                        keyPanel, null), null, null));


                keyPanel = new Panel(new GridLayout(0, 2, 0, 4));
                for (int i = 0; i < statuses.size(); i++) {
                    GanttStatus status = (GanttStatus) statuses.elementAt(i);
                    keyPanel.add(new Label(status.getName()));
                    lbl = new Label(" ");
                    lbl.setBackground(status.getColor());
                    keyPanel.add(lbl);
                }
                keyPanel.add(new Label(" "));
                lbl = new Label("Statuses", Label.CENTER);
                lbl.setFont(boldLblFont);
                panels.addElement(GU.topCenterBottom(GU.topCenterBottom(lbl,
                        keyPanel, null), null, null));

                //      Container legendPanel =GU.flow (GU.getCompArray(panels),4);
                GU.tmpInsets = new Insets(2, 4, 2, 4);
                Container legendPanel = GU.doLayout(GU.getCompArray(panels),
                                            3, GU.DS_Y, GU.DS_N);

                legendFrame.add("Center", legendPanel);


                legendFrame.add("South",
                                GU.wrap(getButton("Close", CMD_LEGEND_HIDE)));
                legendFrame.pack();
                legendFrame.show();
            }
            legendFrame.setBackground(ganttApplet.getBackground());
            legendFrame.setVisible(true);
            return;
        }


        if (func.equals(CMD_LEGEND_HIDE)) {
            if (legendFrame != null) {
                legendFrame.setVisible(false);
            }
            return;
        }


        if (func.equals(CMD_TOGGLE_SUBTASKS)) {
            if ((commandTasks != null) && (commandTasks.size() > 0)) {
                ((GanttTask) commandTasks.elementAt(
                    0)).toggleChildrenVisible();
                setTaskPositions();
                repaintAll();
            }
            return;
        }



        if (func.equals(CMD_TREE_SHOW)) {
            if ((commandTasks != null) && (commandTasks.size() > 0)) {
                treePanels.addElement(new TreePanel(this,
                        (GanttTask) commandTasks.elementAt(0)));
            } else {
                treePanels.addElement(new TreePanel(this));
            }

            return;
        }




        if (func.equals(CMD_DISPLAY)) {
            setDisplayType(new Integer(params).intValue());
            return;

        }

        if (func.equals(CMD_DISPLAY_MONTH)) {
            setDisplayType(DISPLAY_MONTH);
            return;
        }

        if (func.equals(CMD_DISPLAY_WEEK)) {
            setDisplayType(DISPLAY_WEEK);
            return;
        }

        if (func.equals(CMD_DISPLAY_DAY)) {
            setDisplayType(DISPLAY_DAY);
            return;
        }

        if (func.equals(CMD_FLOAT)) {
            toggleFloat();
            return;
        }

        super.handleFunction(func, params, extra, ae);

    }

    /**
     * _more_
     */
    private void toggleFloat() {

        if (floating) {
            xmlUi.setLabel("floatbutton", "Float window");
            ganttApplet.add("Center", getContents());
            floatFrame.dispose();
            invalidate();
            ganttApplet.validate();
            floating = false;
        } else {
            try {
                xmlUi.setLabel("floatbutton", "Close window");
                floatFrame = new Frame();
                floatFrame.setBackground(ganttApplet.getBackground());
                floatFrame.setLayout(new BorderLayout());
                floatFrame.add("Center", contents);
                floatFrame.pack();
                floatFrame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        toggleFloat();
                    }
                });
                Dimension screenSize =
                    Toolkit.getDefaultToolkit().getScreenSize();
                screenSize.width  = (int) (0.8 * screenSize.width);
                screenSize.height = (int) (0.8 * screenSize.height);
                floatFrame.setSize(screenSize);
                floatFrame.setLocation((int) (0.1 * screenSize.width),
                                       (int) (0.1 * screenSize.height));
                floatFrame.show();
                floating = true;
            } catch (Exception exc) {
                print("error:" + exc);
                System.err.println("err:" + exc);
            }
        }
    }

    /**
     * _more_
     *
     * @param s _more_
     * @param h _more_
     *
     * @return _more_
     */
    private String replace(String s, Hashtable h) {
        if (h == null) {
            return s;
        }
        for (Enumeration keys = h.keys(); keys.hasMoreElements(); ) {
            String k = keys.nextElement().toString();
            s = GuiUtils.replace(s, k, h.get(k).toString());
        }
        return s;
    }


    /**
     * _more_
     *
     * @param position _more_
     *
     * @return _more_
     */
    public GanttTask findTaskAtIndex(int position) {
        if ((position >= 0) && (position < visibleTasks.size())) {
            return (GanttTask) visibleTasks.elementAt(position);
        }
        return null;
    }




    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseEntered(MouseEvent e) {}

    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseExited(MouseEvent e) {
        if (hilite != null) {
            hilite = null;
            repaint();
        }
    }

    /**
     * _more_
     *
     * @param date _more_
     *
     * @return _more_
     */
    public long dateToDay(long date) {
        return date / (DAY_FACTOR);
    }

    /**
     * _more_
     *
     * @param e _more_
     */
    public void mousePressed(MouseEvent e) {
        mouseDown = true;
        if ((e.getModifiers() & e.BUTTON1_MASK) == 0) {
            return;
        }
        dragStart     = false;
        dragEnd       = false;
        dragPropagate = e.isAltDown();
        dragComplete  = e.isShiftDown();
        int x = translateInputX(e.getX());
        if (hilite != null) {
            movedTasks = new Hashtable();
            movedTasks.put(hilite, hilite);
            if ( !dragPropagate && e.isControlDown()) {
                int midX = hilite.getBoxCenterX();
                if (translateInputX(e.getX()) < midX) {
                    dragStart = true;
                } else {
                    dragEnd = true;
                }
            }
            if (dragComplete) {
                initialMouseDX = x - (hilite.mainBox.x
                                      + (int) (hilite.getComplete()
                                          * hilite.mainBox.width));
            } else {
                initialMouseDX = x - (dragEnd
                                      ? hilite.mainBox.x
                                        + hilite.mainBox.width
                                      : hilite.mainBox.x);
            }
        }
    }

    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseDragged(MouseEvent e) {
        if ((e.getModifiers() & e.BUTTON1_MASK) == 0) {
            return;
        }
        if ((hilite == null) || (changeDateCommands == null)) {
            message("");
            return;
        }

        int x = translateInputX(e.getX());
        x -= initialMouseDX;
        int       y      = translateInputY(e.getY());
        Rectangle bounds = bounds();
        changedDate = true;


        double dayPerX   = 1.0 / xPerDay;
        int    deltaDays = (int) (dayPerX * x + 0.5);
        long   deltaDate = (long) (deltaDays * DAY_FACTOR);
        long   newDate   = minDate + deltaDate;

        if (newDate < minDate) {
            setDateRange(newDate, maxDate);
        }
        if (newDate > maxDate) {
            setDateRange(minDate, newDate);
        }


        long      theDate  = (dragEnd
                              ? hilite.getEndDate()
                              : hilite.getStartDate());
        long      dateDiff = newDate - theDate;
        String    name     = "";
        Hashtable tmpMoved = new Hashtable();
        if (dragComplete) {
            double    complete = hilite.getComplete();
            Rectangle box      = hilite.mainBox;
            if (x < box.x) {
                complete = 0.0;
            } else if (x > box.x + box.width) {
                complete = 1.0;
            } else {
                complete = ((double) (x - box.x)) / box.width;
            }
            complete = ((double) ((int) (complete * 20))) / 20.0;

            hilite.setComplete(complete);
            message("Set the complete percentage of task  " + hilite + " to "
                    + ((int) (complete * 100)) + "%");
        } else {
            if (dragPropagate) {
                hilite.deltaDate(dateDiff, tmpMoved);
                name = "start";
            } else {
                if ( !dragEnd) {
                    hilite.setStartDate(theDate + dateDiff);
                    if ( !dragStart) {
                        hilite.setEndDate(hilite.getEndDate() + dateDiff);
                    }
                    if (hilite.getEndDate() < hilite.getStartDate()) {
                        hilite.setEndDate(hilite.getStartDate());
                    }
                    name = "start";
                } else {
                    hilite.setEndDate(hilite.getEndDate() + dateDiff);
                    name = "end";
                    if (hilite.getEndDate() < hilite.getStartDate()) {
                        hilite.setStartDate(hilite.getEndDate());
                    }
                }
            }

            hilite.checkDependencies(tmpMoved);
            for (Enumeration keys =
                    tmpMoved.keys(); keys.hasMoreElements(); ) {
                Object t = keys.nextElement();
                movedTasks.put(t, t);
            }
            message("Set " + name + " date of task: " + hilite + " to "
                    + formatDate(new Date(newDate), true));
        }


        if (translateOutputX(dateToX(newDate)) < 0) {
            scrollToDate(newDate);
        } else if (translateOutputX(dateToX(newDate)) > bounds.width) {
            scrollToDate(newDate
                         - (long) (DAY_FACTOR * dayPerX * bounds.width));
        }


        repaintAll();
    }

    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseClicked(MouseEvent e) {
        int x = translateInputX(e.getX());
        int y = translateInputY(e.getY());
        if ((e.getModifiers() & e.BUTTON1_MASK) == 0) {
            GanttTask task = findTask(x, y, BOX_MAIN);
            showTaskMenu(task, true, this, e.getX(), e.getY());
            return;
        }

        GanttTask task = findTask(x, y, BOX_MAIN);
        if ((task == null) && (selectionSet.size() > 0)) {
            selectionSet.removeAllElements();
            repaint();
            return;
        }

        if ((task != null) && !selectionSet.contains(task)) {
            if ( !e.isControlDown() && !e.isShiftDown()) {
                selectionSet.removeAllElements();
            }
            selectionSet.addElement(task);
            repaint();
        }
    }


    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseReleased(MouseEvent e) {
        mouseDown = false;
        message("");
        if (hilite == null) {
            return;
        }
        Vector moved = new Vector();
        if (dragComplete) {
            moved.addElement(hilite);
        } else if (changedDate) {
            for (Enumeration keys =
                    movedTasks.keys(); keys.hasMoreElements(); ) {
                GanttTask movedTask = (GanttTask) keys.nextElement();
                movedTask.updateTimes();
                moved.addElement(movedTask);
            }
            setTaskPositions();
        }
        if (moved.size() > 0) {
            handleCommands(changeDateCommands, moved, null, null);
        }

    }



    /**
     * _more_
     *
     * @param xml _more_
     */
    public void showResultXml(String xml) {
        String text = "";
        try {
            XmlNode root = XmlNode.parse(xml);
            Vector  top  = root.getChildren();
            if (top.size() != 1) {
                throw new IllegalArgumentException("Ill formed xml:" + xml);
            }
            root = (XmlNode) top.elementAt(0);
            Vector messages = root.getChildren();
            String commands = root.getAttribute(ATTR_COMMANDS, (String) null);
            //      System.err.println ("commands:" + commands);
            if (commands != null) {
                handleAction(commands, null);
            }

            for (int i = 0; i < messages.size(); i++) {
                XmlNode message = (XmlNode) messages.elementAt(i);
                text = text + message.getAttribute("message", "") + "\n";
            }
        } catch (Exception exc) {
            print("error:" + exc);
            text = xml;
        }
        showMessage(text);
    }

    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseMoved(MouseEvent e) {
        clearMouseOver(this);

        int       x       = translateInputX(e.getX());
        int       y       = translateInputY(e.getY());

        GanttTask closest = findTask(x, y, BOX_MAIN);
        changedDate = false;
        if (hilite != closest) {
            hilite = closest;
            if (hilite != null) {
                message(
                    "Drag: move;   Control-drag: change length;   Alt-drag: move all;   Shift-drag: Change complete %");
            } else {
                message("");
            }
            repaint();
        }
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
        GanttTask closest = findTask(x, y, BOX_MAIN);
        if (closest != null) {
            if (e.shiftDown()) {
                //      showUrl  (getUrl (closest), "_test");
            } else {}
        }
        return true;
    }

    /**
     *  Popup the given url into the given window (defined by which).
     *  If which is null or "" then popup in this window.
     *
     * @param urlString _more_
     * @param which _more_
     */
    void showUrl(String urlString, String which) {
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (Throwable exc1) {
            try {
                url = new URL(ganttApplet.getFullUrl(urlString));
            } catch (Throwable exc2) {}
        }

        if (url != null) {
            if ((which != null) && !which.equals("")) {
                ganttApplet.getAppletContext().showDocument(url, which);
            } else {
                ganttApplet.getAppletContext().showDocument(url);
            }
        }
    }


    /**
     * _more_
     *
     * @param g _more_
     */
    public void paintTaskHeader(Graphics g) {
        Rectangle b = taskHeaderCanvas.bounds();

        g.setColor(headerBgColor);
        g.fillRect(taskHeaderCanvas.translateInputX(0),
                   taskHeaderCanvas.translateInputY(0), b.width, b.height);
        //    System.err.println ("paintTaskHeader:" + headerBgColor);


        g.setColor(Color.black);
        int olx = taskHeaderCanvas.translateInputX(0);
        int orx = taskHeaderCanvas.translateInputX(b.width);
        int oty = taskHeaderCanvas.translateInputY(0);
        int oby = taskHeaderCanvas.translateInputY(b.height);

        g.drawRect(olx, oty, b.width - 1, b.height - 1);
        g.setColor(Color.white);
        g.drawLine(olx, oty + 1, orx - 2, oty + 1);

        g.setFont(boldLblFont);
        int off = 2;

        for (int i = 0; i < TASKHEADER_POS.length; i++) {
            int x = TASKHEADER_POS[i];
            g.setColor(Color.black);
            g.drawString(TASKHEADER_TITLES[i], x + 2 + 1, HEIGHT_HEADER - 2);
            g.setColor(Color.gray);
            g.drawLine(x, 0, x, HEIGHT_HEADER);
            g.setColor(Color.white);
            g.drawLine(x + 1, 1, x + 1, HEIGHT_HEADER - 2);
        }
    }


    /**
     * _more_
     *
     * @param g _more_
     */
    public void paintMainHeader(Graphics g) {

        if (error != null) {
            return;
        }

        Rectangle b    = mainHeaderCanvas.bounds();
        int       mid  = (b.height / 2);

        int       olx  = mainHeaderCanvas.translateInputX(0);
        int       orx  = mainHeaderCanvas.translateInputX(b.width);
        int       oty  = mainHeaderCanvas.translateInputY(0);
        int       oby  = mainHeaderCanvas.translateInputY(b.height);
        int       omid = mainHeaderCanvas.translateInputY(mid);
        g.setColor(headerBgColor);
        g.fillRect(olx, oty, b.width, b.height);

        g.setColor(Color.white);
        g.drawLine(olx + 1, oty + 1, orx - 2, oty + 1);


        /*    g.drawLine (olx,oty,orx,oty);
            g.drawLine (olx,oby-1,orx,oby-1);
            g.drawLine (orx-1,oty,orx-1,oby);*/

        Calendar theCal      = new GregorianCalendar();
        Date     theDate     = new Date();
        long     currentDate = minDate;
        int      currentX    = 0;

        g.setFont(boldLblFont);
        g.setColor(Color.black);

        switch (displayType) {

          case DISPLAY_DAY : {
              while (currentDate <= maxDate) {
                  theDate.setTime(currentDate);
                  theCal.setTime(theDate);
                  int year  = theCal.get(Calendar.YEAR);
                  int month = theCal.get(Calendar.MONTH);
                  int dim   = getDaysInMonth(month, year);
                  int dom   = theCal.get(Calendar.DAY_OF_MONTH);
                  int days  = (dim - dom) + 1;
                  if (days <= 0) {
                      throw new IllegalArgumentException("oops:" + dim + " "
                              + dom + " " + theDate);
                  }
                  int    width   = daysToWidth(days);
                  String yearStr = "" + year;
                  currentX = dateToX(currentDate);
                  //      g.setColor (Color.white);
                  //      g.drawLine (currentX+1, 1, currentX+1, mid-1);  
                  g.setColor(Color.gray);
                  g.drawLine(currentX, 0, currentX, mid);
                  g.setColor(Color.black);

                  String label = months[month] + " " + yearStr;
                  GuiUtils.drawClippedString(g, label, currentX + 3, mid - 2,
                                             width);
                  for (int day = 1; day <= days; day++) {
                      int x = daysToWidth(day - 1) + currentX;
                      g.setColor(Color.gray);
                      g.drawLine(x, mid, x, b.height - 1);
                      g.setColor(Color.black);
                      g.drawString("" + (day + dom - 1), x + 3, b.height - 2);
                  }
                  currentDate += days * DAY_FACTOR;
              }
              break;
          }

          case DISPLAY_WEEK : {
              while (currentDate <= maxDate) {
                  theDate.setTime(currentDate);
                  theCal.setTime(theDate);
                  int dow  = theCal.get(Calendar.DAY_OF_WEEK);
                  int days = 8 - dow;
                  if (days <= 0) {
                      throw new IllegalArgumentException("oops:" + days + " "
                              + dow + " " + theDate);
                  }
                  currentX    = dateToX(currentDate);
                  currentDate += days * DAY_FACTOR;
                  int    width = daysToWidth(days);
                  int    dom   = theCal.get(Calendar.DAY_OF_MONTH);
                  int    month = theCal.get(Calendar.MONTH);
                  String year  = "" + theCal.get(Calendar.YEAR);
                  g.setColor(Color.gray);
                  g.drawLine(currentX, 0, currentX, mid);
                  g.setColor(Color.black);
                  String label = months[month] + " " + dom + ", '"
                                 + year.substring(2);
                  GuiUtils.drawClippedString(g, label, currentX + 3, mid - 2,
                                             width);
                  for (int day = 1; day <= days; day++) {
                      int x = daysToWidth(day - 1) + currentX;
                      g.setColor(Color.gray);
                      g.drawLine(x, mid, x, b.height - 1);
                      g.setColor(Color.black);
                      g.drawString(shortDays[day + (dow - 1)], x + 3,
                                   b.height - 2);
                  }
              }
              break;

          }

          case DISPLAY_MONTH : {
              while (currentDate <= maxDate) {
                  theDate.setTime(currentDate);
                  theCal.setTime(theDate);
                  int year  = theCal.get(Calendar.YEAR);
                  int month = theCal.get(Calendar.MONTH);
                  int dom   = theCal.get(Calendar.DAY_OF_MONTH);
                  int doy   = theCal.get(Calendar.DAY_OF_YEAR);
                  int diy   = getDaysInYear(year);
                  int days  = (diy - doy) + 1;
                  if (days <= 0) {
                      throw new IllegalArgumentException("oops:" + diy + " "
                              + doy + " " + theDate);
                  }
                  currentX    = dateToX(currentDate);
                  currentDate += days * DAY_FACTOR;
                  int width = daysToWidth(days);

                  g.setColor(Color.gray);
                  g.drawLine(currentX, 0, currentX, mid);
                  g.setColor(Color.black);
                  GuiUtils.drawClippedString(g, "" + year, currentX + 3,
                                             mid - 2, width);
                  int totalDays = 0;
                  for (int monthIdx = month; monthIdx < 12; monthIdx++) {
                      int dim = getDaysInMonth(monthIdx, year);
                      int x   = daysToWidth(totalDays) + currentX;
                      width = daysToWidth(dim - dom + 1);
                      g.setColor(Color.gray);
                      g.drawLine(x, mid, x, b.height - 1);
                      g.setColor(Color.black);
                      GuiUtils.drawClippedString(g, months[monthIdx], x + 3,
                              b.height - 2, width);
                      totalDays += dim - dom + 1;
                      dom       = 1;
                  }
              }
              break;
          }

        }


        g.setColor(Color.gray);
        g.drawLine(olx, omid, orx, omid);
        g.setColor(Color.black);
        g.drawRect(olx, oty, b.width - 1, b.height - 1);


    }

    /**
     * _more_
     *
     * @param dt _more_
     * @param longDay _more_
     *
     * @return _more_
     */
    public String formatDate(Date dt, boolean longDay) {
        cal.setTime(dt);
        return (longDay
                ? days[cal.get(Calendar.DAY_OF_WEEK)]
                : medDays[cal.get(Calendar.DAY_OF_WEEK)]) + " "
                + months[cal.get(Calendar.MONTH)] + " "
                + cal.get(Calendar.DAY_OF_MONTH) + ", "
                + cal.get(Calendar.YEAR);
    }



    /**
     * _more_
     *
     * @param days _more_
     *
     * @return _more_
     */
    public int daysToWidth(int days) {
        return (int) (days * xPerDay + 0.5);
    }


    /**
     * _more_
     *
     * @param days _more_
     *
     * @return _more_
     */
    public int durationToWidth(int days) {
        return (int) (days * xPerDay + 0.5);
    }

    /**
     * _more_
     *
     * @param date _more_
     *
     * @return _more_
     */
    public int dateToX(long date) {
        long diff    = date - minDate;
        long dayDiff = diff / (DAY_FACTOR);
        return (int) ((dayDiff) * xPerDay + 0.5);
    }




    /**
     * _more_
     *
     * @param year _more_
     *
     * @return _more_
     */
    public static int getDaysInYear(int year) {
        if (isLeapYear(year)) {
            return 366;
        }
        return 365;
    }

    /**
     * _more_
     *
     * @param m _more_
     * @param year _more_
     *
     * @return _more_
     */
    public static int getDaysInMonth(int m, int year) {
        m++;
        switch (m) {

          case 1 : {
              return 31;
          }

          case 3 : {
              return 31;
          }

          case 5 : {
              return 31;
          }

          case 7 : {
              return 31;
          }

          case 8 : {
              return 31;
          }

          case 10 : {
              return 31;
          }

          case 12 : {
              return 31;
          }

          case 4 : {
              return 30;
          }

          case 6 : {
              return 30;
          }

          case 9 : {
              return 30;
          }

          case 11 : {
              return 30;
          }
        }
        if (isLeapYear(year)) {
            return 29;
        }
        return 28;
    }

    /**
     * _more_
     *
     * @param year _more_
     *
     * @return _more_
     */
    public static boolean isLeapYear(int year) {
        return ((year % 4) == 0) && ((year % 100) != 0)
               || ((year % 400) == 0);
    }



    /**
     * _more_
     *
     * @param g _more_
     */
    public void paintInner(Graphics g) {

        if ( !haveLoaded) {
            haveLoaded = true;
            g.drawString("Loading chart", 20, 20);
            doLoad();
            return;
        }

        if (error != null) {
            g.drawString(error, 20, 20);
            return;
        }

        Rectangle myBounds = bounds();


        int       olx      = translateInputX(0);
        int       orx      = translateInputX(myBounds.width);
        int       oty      = translateInputY(0);
        int       oby      = translateInputY(myBounds.height);


        boolean doWeekend = ((displayType == DISPLAY_WEEK)
                             || (displayType == DISPLAY_DAY));
        //    boolean doWeeklines = (displayType == DISPLAY_WEEK);
        boolean  doWeeklines = true;


        Calendar theCal      = new GregorianCalendar();
        Date     theDate     = new Date();

        long     currentDate = minDate;
        if (doWeekend) {
            g.setColor(weekendColor);
            while (currentDate <= maxDate) {
                theDate.setTime(currentDate);
                theCal.setTime(theDate);
                int dow  = theCal.get(Calendar.DAY_OF_WEEK);
                int days = 1;
                if ((dow == 1) || (dow == 7)) {
                    int x = dateToX(currentDate);
                    g.setColor(weekendColor);
                    g.fillRect(x, oty, (int) xPerDay, oby);
                    days = ((dow == 1)
                            ? 6
                            : 1);
                    if ((dow == 1) && doWeeklines) {
                        g.setColor(Color.gray);
                        g.drawLine(x, oty, x, oby);
                        g.setColor(Color.black);
                    }
                }
                currentDate += days * DAY_FACTOR;
            }
        }


        g.setColor(Color.gray);
        currentDate = minDate;
        while (currentDate <= maxDate) {
            theDate.setTime(currentDate);
            theCal.setTime(theDate);
            int year  = theCal.get(Calendar.YEAR);
            int month = theCal.get(Calendar.MONTH);
            int dim   = getDaysInMonth(month, year);
            int dom   = theCal.get(Calendar.DAY_OF_MONTH);
            int days  = (dim - dom) + 1;
            if (dom == 1) {
                int x = dateToX(currentDate);
                g.drawLine(x, oty, x, oby);
            }
            currentDate += days * DAY_FACTOR;
        }

        /*
            switch (displayType) {
            case DISPLAY_DAY: {
              while (currentDate<=maxDate) {
                currentX+=(int)xPerDay;
              }
              break;
              }*/



        for (int i = 0; i < visibleTasks.size(); i++) {
            GanttTask task = (GanttTask) visibleTasks.elementAt(i);
            if (task == hilite) {
                continue;
            }
            task.paint(g, false);
        }
        if (hilite != null) {
            hilite.paint(g, true);
        }

        if (hilite != null) {
            g.setColor(hiliteColor);
            Rectangle b = hilite.mainBox;
            g.drawRect(b.x, b.y, b.width, b.height);
            g.drawRect(b.x - 1, b.y - 1, b.width + 2, b.height + 2);
        }


        g.setColor(Color.black);
        for (int i = 0; i < selectionSet.size(); i++) {
            GanttTask task = (GanttTask) selectionSet.elementAt(i);
            task.paintSelection(g);
        }

        if ( !mouseDown) {
            paintMouseOver(g, hilite);
        }

        g.setColor(Color.black);
        g.drawRect(olx, oty, myBounds.width - 1, myBounds.height - 1);


    }

    /**
     * _more_
     *
     * @param task _more_
     */
    void removeSelection(GanttTask task) {
        selectionSet.removeElement(task);
    }


    /**
     * _more_
     *
     * @param g _more_
     * @param hilite _more_
     */
    protected void paintMouseOver(Graphics g, GanttTask hilite) {
        if (hilite == null) {
            return;
        }
        Rectangle bounds = hilite.mainBox;
        //    int x = translateOutputX (bounds.x+bounds.width-2);
        //    int y = translateOutputY (bounds.y+ bounds.height-4);
        int       x        = bounds.x + bounds.width - 2;
        int       y        = bounds.y + bounds.height - 4;
        Rectangle myBounds = bounds();
        int       w        = translateInputX(myBounds.width);
        int       h        = translateInputY(myBounds.height);
        paintMouseOver(this, g, hilite, x, y, w, h);
    }


    /** _more_          */
    Hashtable mouseOverMap = new Hashtable();

    /**
     * _more_
     *
     * @param comp _more_
     */
    public void clearMouseOver(Component comp) {
        mouseOverMap.remove(comp);
    }

    /**
     * _more_
     *
     * @param comp _more_
     * @param g _more_
     * @param hilite _more_
     * @param x _more_
     * @param y _more_
     * @param w _more_
     * @param h _more_
     */
    protected void paintMouseOver(final Component comp, Graphics g,
                                  GanttTask hilite, int x, int y, int w,
                                  int h) {
        if ((hilite == null) || !getShow(SHOW_MOUSEOVER)) {
            return;
        }

        Long last     = (Long) mouseOverMap.get(comp);
        long sleepFor = 0;
        long waitTime = 1000;
        if (last != null) {
            long then      = last.longValue();
            long waitedFor = System.currentTimeMillis() - then;
            if (waitedFor < waitTime) {
                sleepFor = waitTime - waitedFor;
            }
        } else {
            mouseOverMap.put(comp, new Long(System.currentTimeMillis()));
            sleepFor = waitTime;
        }
        if (sleepFor > 0) {
            final long sleepForFinal = sleepFor;
            Thread     t             = new Thread() {
                public void run() {
                    try {
                        Thread.currentThread().sleep(sleepForFinal);
                        comp.repaint();
                    } catch (Exception exc) {}
                }
            };
            t.start();
            return;
        }
        //    mouseOverMap.remove (comp);

        String start = hilite.getStartDateString();
        String end   = hilite.getEndDateString();
        String mouseOver = "Task: " + hilite.getName() + "\n" + "Parent:"
                           + hilite.getParentTask() + "\n" + "Resource: "
                           + hilite.resource.getName() + "\n" + "Start: "
                           + start + "\n" + "End: " + end + "\n" + "Length: "
                           + hilite.getDuration() + " hours" + "\n"
                           + "Complete: "
                           + ((int) (hilite.getComplete() * 100)) + "%"
                           + "\n" + "Status: " + hilite.getStatus().getName()
                           + "\n" + "Type: " + hilite.getType().getName()
                           + "\n" + "";
        g.setFont(normalLblFont);
        String          t          = mouseOver;
        FontMetrics     fm         = g.getFontMetrics();
        int             maxd       = fm.getMaxDescent();
        int             width      = 0;
        int             lineHeight = maxd + fm.getMaxAscent();
        StringTokenizer tok        = new StringTokenizer(t, "\n");
        int             lines      = tok.countTokens();
        int             height     = (lineHeight + 1) * lines;

        while (tok.hasMoreTokens()) {
            String line = tok.nextToken().trim();
            int    tmp  = fm.stringWidth(line) + 4;
            if (tmp > width) {
                width = tmp;
            }
        }

        if (y + height > h) {
            y -= ((y + height) - h + 2);
        }
        if (x + width > w) {
            x -= ((x + width) - w);
        }


        g.setColor(mouseOverColor);
        g.fillRect(x, y, width, height);
        g.setColor(Color.black);
        g.drawRect(x, y, width, height);
        g.setColor(Color.black);

        y   += lineHeight;
        y   -= 2;

        tok = new StringTokenizer(t, "\n");
        while (tok.hasMoreTokens()) {
            String line = tok.nextToken().trim();
            g.drawString(line, x + 2, y);
            y += lineHeight;
        }
    }

    /**
     * _more_
     *
     * @param t _more_
     *
     * @return _more_
     */
    public boolean isHilite(GanttTask t) {
        return hilite == t;
    }

    /**
     * _more_
     *
     * @param gif _more_
     * @param c _more_
     *
     * @return _more_
     */
    public Image getImage(String gif, Class c) {
        try {
            InputStream imageStream = c.getResourceAsStream(gif);
            if (imageStream == null) {
                return null;
            }
            int    length           = imageStream.available();
            byte[] thanksToNetscape = new byte[length];
            imageStream.read(thanksToNetscape);
            return Toolkit.getDefaultToolkit().createImage(thanksToNetscape);
        } catch (Exception exc) {
            print(exc + " getting resource ");
        }
        return null;
    }


    /**
     * _more_
     *
     * @param s _more_
     */
    public void print(String s) {
        ganttApplet.debug(s);
        System.err.println(s);
    }




}

