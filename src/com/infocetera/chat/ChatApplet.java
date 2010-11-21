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

/**
 * (C) 1999-2002  WTS Systems, L.L.C.
 *   All rights reserved
 */


package com.infocetera.chat;


import com.infocetera.util.*;


import java.applet.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.image.ImageObserver;

import java.io.*;

import java.lang.reflect.*;

import java.net.*;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.*;
import javax.swing.text.*;


/**
 *  This is the central class for the chat applet.
 *  The base class, SocketApplet, provides client
 *  services (e.g., message passing to the server).
 *  <p>
 *  The file: example.html shows the applet tag
 *  and different ways of configuring the chat client (e.g.,
 *  socket based server, http/polling based server, etc.)
 *  <p>
 */
public class ChatApplet extends SocketApplet implements ImageObserver,
        ActionListener, WindowListener {

    /**
     *  The MSG_ vars are the types of the xml messages
     *  handled by this chat applet. (e.g., <message type="GFX">...</message>)
     *  Note: the incoming messages typically have a from="userid"
     *  attribute that we use to identify the source of the message
     *  and potentially block the message.
     */

    /**
     *  Sent from the server at the initial startup. Contains
     *  information about the session  (e.g., user id, edit capability, etc.)
     */
    public static final String MSG_SESSION = "SESSION";


    /**
     *  A file/url has been added.
     *  <pre>&lt;message type="FILE" name="Readable file name" url="url of the file"/&gt;</pre>
     *  Both the name and the url can be specified or just one of either.
     */

    public static final String MSG_FILE = "FILE";

    /** _more_ */
    public static final String MSG_LEAVEROOM = "LEAVEROOM";

    /**
     *  A graphics canvas command has been specified. The form is:
     *  <pre>&lt;message type="GFX" &gt;
     *  &lt;command command="some canvas command" ... /&gt;
     *  &lt;command command="some other canvas command" ... /&gt;
     *  &lt;/message&gt;</pre>
     *  The message contains a set of command tags, each specifying
     *  a different canvas operation.
     */
    public static final String MSG_GFX = "GFX";

    /**
     *  A private message between two individual users.
     *  <pre>&lt;message type="PRIVATE"&gt; The text of the message&lt;/message&gt;</pre>
     */
    public static final String MSG_PRIVATE = "PRIVATE";

    /**
     *  A text message
     *  <pre>&lt;message type="TEXT"&gt; The text of the message&lt;/message&gt;</pre>
     */
    public static final String MSG_TEXT = "TEXT";

    /**
     *  Have the browser go to the specified url.
     *  <pre>&lt;message type="URL"/&gt;</pre>
     */

    public static final String MSG_URL = "URL";

    /**
     *  Add the user to the list.
     *  <pre>&lt;message type="USERADD"&gt;&lt;user name="some name" icon="user's icon"/&lt;&lt;/message&gt;</pre>
     */
    public static final String MSG_USERADD = "USERADD";

    /**
     *  Remove the user from the list.
     *  Same user tag as the MSG_USERADD
     */
    public static final String MSG_USERREMOVE = "USERREMOVE";

    /**
     *  This is the list of users at the beginning  of the session.
     *  Same form as MSG_USERADD (but with multiple user tags)
     */

    public static final String MSG_USERLIST = "USERLIST";

    /** _more_ */
    public static final String MSG_ROOMS = "ROOMS";


    /**
     *  Tag name for the "user" tags
     */
    public static final String TAG_USER = "user";

    /** _more_ */
    public static final String TAG_ROOM = "room";


    /** _more_ */
    public static final String TAG_REPLACEMENTS = "replacements";

    /** _more_ */
    public static final String TAG_REPLACE = "replace";

    /** _more_ */
    public static final String TAG_PROPERTIES = "properties";

    /** _more_ */
    public static final String TAG_MESSAGES = "messages";

    /** _more_ */
    public static final String TAG_PROPERTY = "property";

    /** _more_ */
    public static final String ATTR_FROM = "from";

    /** _more_ */
    public static final String ATTR_TO = "to";

    /** _more_ */
    public static Vector replaceFrom = new Vector();

    /** _more_ */
    public static Vector replaceTo = new Vector();

    /**
     *  We prepend this to every message.
     */
    private String prefix = "";


    /**
     *  The ATTR_... vars are the names of the xml attributes
     *  used to communicate with the chat server
     */

    /**
     *  Can the user edit the whiteboard. We'll add more capabilities
     *  later.
     */
    public static final String ATTR_EDIT = "edit";

    /**
     *  The icon associated with the user in the AvatarCanvas
     */
    public static final String ATTR_ICON = "icon";

    /**
     *  Name of the user (and also for the name of the file in the FILE message)
     */
    public static final String ATTR_NAME = "name";

    /** _more_ */
    public static final String ATTR_ID = "id";

    /** _more_ */
    public static final String ATTR_VALUE = "value";

    /** _more_ */
    public static final String ATTR_URL = "url";


    /** _more_ */
    public static final String USER_CHAT = "chat";

    /**
     *  Different user interface widget action commands
     */

    public static final String ID_ROOMLABEL = "roomlabel";

    /** _more_ */
    public static final String ID_FILELIST = "filelist";

    /** _more_ */
    public static final String ID_ROOMLIST = "roomlist";

    /** _more_ */
    public static final String ID_USERLIST = "userlist";

    /** _more_ */
    public static final String ID_WHITEBOARD = "whiteboard";

    /** _more_ */
    public static final String ID_CHATTEXTAREA = "chattextarea";

    /** _more_ */
    public static final String ID_CHATTEXT = "chattext";

    /** _more_ */
    public static final String ID_CHATTEXT_WRAPPER = "chattext_wrapper";

    /** _more_ */
    public static final String ID_CHATAVATAR = "chatavatar";

    /** _more_ */
    public static final String ID_CHATAVATAR_WRAPPER = "chatavatar_wrapper";



    /** _more_ */
    public static final String CMD_SHOW = "ui.show";

    /** _more_ */
    public static final String CMD_SETPREFIX = "chat.setprefix";

    /** _more_ */
    public static final String CMD_SHOWPREFIX = "chat.showprefix";

    /** _more_ */
    public static final String CMD_CLEARPREFIX = "chat.clearprefix";

    /** _more_ */
    public static final String CMD_INPUT = "chat.input";

    /** _more_ */
    public static final String CMD_WRITE = "chat.write";

    /** _more_ */
    public static final String CMD_CLEAR = "chat.cleartext";

    /** _more_ */
    public static final String CMD_SAVESESSION = "chat.savesession";

    /** _more_ */
    public static final String CMD_CHANGEROOM = "chat.changeroom";


    /** _more_ */
    public static final String CMD_USERS_IGNORE = "users.ignore";

    /** _more_ */
    public static final String CMD_USERS_PRIVATE = "users.private";

    /** _more_ */
    public static final String CMD_FILES_GO = "files.go";

    /** _more_ */
    public static final String CMD_FILES_TOGGLE = "files.toggle";

    /** _more_ */
    public static final String CMD_FILES_VIEW = "files.view";

    /** _more_ */
    public static final String CMD_FILES_SELECT = "files.select";

    /** _more_ */
    public static final String CMD_FILES_UPLOAD = "files.upload";

    /** _more_ */
    public static final String CMD_FILES_REMOVE = "files.remove";

    /** _more_ */
    public static final String CMD_FILES_SHARE = "files.share";

    /** _more_ */
    public static final String CMD_UI_FLOAT = "ui.float";



    /** _more_ */
    boolean haveInitialized = false;

    /** _more_ */
    ChatUser myUser;


    /**
     * Holds the parameter that defines where we get the skin xml file from.
     */
    private String skinUrl;

    /** _more_ */
    private boolean ignoreWelcome = false;

    /** _more_ */
    Color foregroundColor = new Color(255, 204, 102);


    /**
     *  Are we showing the avatar view
     */
    boolean showingAvatar = false;


    /**
     *  Is the top part of the gui shown
     */
    boolean topShown = true;

    /**
     *  Is the bottom part of the gui shown
     */
    boolean bottomShown = true;


    /**
     *  Does this user have edit capabilities on the server
     */
    boolean canEdit = false;

    /**
     *  Is the applet currently floating in its own window
     */
    boolean floating = false;

    /** _more_ */
    boolean initFloat = false;

    /** _more_ */
    boolean firstFloat = true;


    /**
     *  This is the Frame used when the applet is floating
     */
    Frame floatFrame;

    /**
     *  This is the original Container when the applet is embedded
     */
    Container oldParent;


    /**
     *  Holds the name of the transcript (if given) that this applet was initialized
     *  with
     */
    String transcriptName = "";


    /**
     *  The gui contents of this applet
     */
    Container myContents;

    /**
     *  Is the processor of the "skin" xml ui
     */
    XmlUi xmlUi;

    /** _more_ */
    private XmlNode skinRoot;

    /**
     *  List of current users in this chat session
     */
    Vector users = new Vector();

    /**
     *  The gui component that shows the list of users
     */
    UserList userList;


    /**
     *  The text view of the chat
     */
    private TextCanvas textCanvas;

    /** _more_ */
    private TextArea textArea;


    /**
     *  This is the special avatar view
     */
    private AvatarCanvas avatarCanvas;

    /**
     *  This is the whiteboard canvas
     */
    private WhiteBoardCanvas canvas;

    /**
     *  Holds the whiteboard canvas
     */
    Component canvasPanel;

    /**
     */
    String loadFilesUrl;




    /**
     *  Holds the gui list of files
     */
    boolean onlyShowImages = false;

    /** _more_ */
    JList fileList;

    /** _more_ */
    Vector files = new Vector();

    /** _more_ */
    List roomList;

    /** _more_ */
    Vector roomIds = new Vector();

    /** _more_ */
    Vector roomNames = new Vector();


    /** _more_ */
    Label roomLabel;






    /**
     *  Do nothing constructor
     */
    public ChatApplet() {}

    /**
     *  Initialize the applet.   Read in parameters. Create the gui.
     */




    public void initInner() {
        super.initInner();

        ChatUser.init();


        //Get the parameters
        skinUrl = getProperty("skinurl", (String) null);

        try {
            readSkin();
        } catch (Exception exc) {
            System.err.println("Error reading skin: " + skinUrl);
            exc.printStackTrace();
        }
        if (skinRoot == null) {
            initFailed = true;
            add(new Label("Error reading skin"));

            return;
        }

        transcriptName = getProperty("transcriptname", "");
        canEdit        = getProperty("canedit", true);
        foregroundColor = getProperty("foregroundcolor",
                                      new Color(255, 204, 102));
        loadFilesUrl = getProperty("loadfilesurl");
        setBackground(getProperty("backgroundcolor", Color.lightGray));
        String logUrl = getProperty("logurl");

        SocketApplet.debug = getProperty("chat.debug", SocketApplet.debug);
        DisplayCanvas.debug = getProperty("whiteboard.debug",
                                          DisplayCanvas.debug);
        CanvasCommand.debug = getProperty("command.debug",
                                          CanvasCommand.debug);

        setLayout(new BorderLayout());
        add("Center", myContents = doMakeContents());

        initFloat = getProperty("chat.float", false);
        if (initFloat) {
            setFloat(true);
        }

        XmlNode messagesNode = skinRoot.getChild(TAG_MESSAGES);
        if (messagesNode != null) {
            processMessages(messagesNode);
        }

        String transcript = readUrl(getProperty("transcripturl",
                                (String) null));
        if (debug) {
            System.err.println("Transcript:" + transcript);
        }
        if (transcript != null) {
            handleMessages(transcript);
        }

        String log = readUrl(getProperty("logurl", (String) null));
        if (log != null) {
            handleLog(log);
        }
        ignoreWelcome = ((transcript != null) || (log != null));
    }

    /**
     * _more_
     *
     * @param log _more_
     */
    private void handleLog(String log) {
        XmlNode root;
        try {
            root = XmlNode.parse(log);
        } catch (Exception exc) {
            errorMsg("An error occurred while processing log.\n" + exc);
            return;
        }

        int size = root.size();
        for (int i = 0; i < size; i++) {
            XmlNode message = root.get(i);
            int seconds = Integer.decode(message.getAttribute("seconds",
                              "0")).intValue();
            //Sometime we can do an animated
            handleMessage(message);
        }
    }


    /**
     * _more_
     */
    private void readSkin() {
        String skinXml = readUrl(skinUrl);
        if (skinXml == null) {
            try {
                skinXml = new String(
                    GuiUtils.readResource(
                        "/com/infocetera/chat/defaultskin.xml", getClass()));
            } catch (Exception exc) {
                System.err.println("Error reading default skin: " + exc);
                System.err.println("Skin file: "
                                   + "/com/infocetera/chat/defaultskin.xml");
            }
        }

        if (skinXml != null) {
            try {
                XmlNode root = XmlNode.parse(skinXml);
                if (root.size() > 0) {
                    skinRoot = root.get(0);
                    processReplacements(skinRoot.getChild(TAG_REPLACEMENTS));
                    processProperties(skinRoot.getChild(TAG_PROPERTIES));
                }
            } catch (Exception exc) {
                //      System.err.println ("Error parsing skin xml:" + skinXml + "\n" + exc);
                exc.printStackTrace();
            }
        }
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public Color getForegroundColor() {
        return foregroundColor;
    }




    /**
     *  Create the gui
     *
     * @return _more_
     */
    public TextCanvas makeTextCanvas() {
        TextCanvas ct = new TextCanvas(this, getMyUser());
        ct.setBackground(getProperty("chattext.bgcolor", Color.white));
        ct.line1color = getProperty("chattext.line1color", (Color) null);
        ct.line2color = getProperty("chattext.line2color", (Color) null);
        return ct;
    }

    /**
     * _more_
     *
     * @param img _more_
     * @param flags _more_
     * @param x _more_
     * @param y _more_
     * @param width _more_
     * @param height _more_
     *
     * @return _more_
     */
    public boolean imageUpdate(Image img, int flags, int x, int y, int width,
                               int height) {

        if ((flags & ImageObserver.ERROR) != 0) {
            errorMsg("image error:" + IfcApplet.getImagePath(img));
            return false;
        }
        return true;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public Container doMakeContents() {

        if (myContents != null) {
            return myContents;
        }

        if (skinRoot == null) {
            return GuiUtils.inset(new Label("Failed to read skin.xml"), 2, 2);
        }

        Hashtable idToComponent = new Hashtable();

        canvas = new WhiteBoardCanvas(this);
        String bgImage = getProperty("whiteboard.bgimage", (String) null);
        if ((bgImage != null) && (bgImage.length() > 0)) {
            Image image = getImage(bgImage);
            if(image!=null) {
                image.getWidth(this);
                canvas.setBackgroundImage(image, bgImage);
            }
        }


        canvas.setBackground(getProperty("whiteboard.bgcolor", Color.white));
        idToComponent.put(ID_WHITEBOARD,
                          canvasPanel = canvas.doMakeContents());

        idToComponent.put(ID_CHATTEXT, textCanvas = makeTextCanvas());
        idToComponent.put(ID_CHATTEXT_WRAPPER, textCanvas.doMakeContents());

        textArea = new TextArea();
        textArea.setBackground(getProperty("chattext.bgcolor", Color.white));
        idToComponent.put(ID_CHATTEXTAREA, textArea);


        idToComponent.put(ID_CHATAVATAR,
                          avatarCanvas = new AvatarCanvas(this));
        idToComponent.put(ID_CHATAVATAR_WRAPPER,
                          avatarCanvas.doMakeContents());
        avatarCanvas.setBackground(getProperty("chattext.bgcolor",
                Color.white));

        if (userList == null) {
            userList = new UserList("FRED", this);
        }
        idToComponent.put(ID_USERLIST, userList.doMakeContents());

        idToComponent.put(ID_ROOMLABEL,
                          roomLabel = new Label("Room:                    "));


        roomList = new List(4, false);
        roomList.addActionListener(this);
        idToComponent.put(ID_ROOMLIST, roomList);


        fileList = new JList();
        fileList.setToolTipText("Right click to show menu");
        fileList.setVisibleRowCount(4);
        fileList.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                try {
                    UrlEntry entry = (UrlEntry) fileList.getSelectedValue();
                    if ( !SwingUtilities.isRightMouseButton(e)) {
                        if ((e.getClickCount() > 1) && (entry != null)) {
                            showUrl(entry.url, "CHAT.FILE");
                        }
                        return;
                    }
                    JPopupMenu popup = new JPopupMenu();
                    ArrayList  items = new ArrayList();
                    popup.add(makeMenuItem("Add new url", CMD_FILES_SELECT));
                    if (onlyShowImages) {
                        popup.add(makeMenuItem("Show all files",
                                CMD_FILES_TOGGLE));
                    } else {
                        popup.add(makeMenuItem("Only show images",
                                CMD_FILES_TOGGLE));
                    }
                    if (entry != null) {
                        popup.add(makeMenuItem("View selected",
                                CMD_FILES_GO));
                        if (entry.isImage) {
                            popup.add(makeMenuItem("Use as background",
                                    CMD_FILES_VIEW));
                        }
                        popup.add(makeMenuItem("Remove selected",
                                CMD_FILES_REMOVE));
                        popup.add(makeMenuItem("Share selected",
                                CMD_FILES_SHARE));
                    }
                    popup.show(fileList, e.getX(), e.getY());
                } catch (Exception exc) {
                    errorMsg("Error:" + exc);
                }
            }
        });
        JScrollPane sp = new JScrollPane(fileList);
        sp.setPreferredSize(new Dimension(150, 100));


        idToComponent.put(ID_FILELIST, sp);

        xmlUi = new XmlUi(this, skinRoot, idToComponent, this);
        return xmlUi.getContents();

    }


    /**
     * _more_
     *
     * @param n _more_
     */
    private void processReplacements(XmlNode n) {
        if (n == null) {
            return;
        }
        for (int i = 0; i < n.size(); i++) {
            XmlNode child = n.get(i);
            if ( !child.tagEquals(TAG_REPLACE)) {
                continue;
            }
            replaceFrom.addElement(child.getAttribute(ATTR_FROM, "no value"));
            replaceTo.addElement(child.getAttribute(ATTR_TO, "no value"));
        }
    }

    /**
     * _more_
     *
     * @param n _more_
     */
    private void processProperties(XmlNode n) {
        if (n == null) {
            return;
        }
        for (int i = 0; i < n.size(); i++) {
            XmlNode child = n.get(i);
            if ( !child.tagEquals(TAG_PROPERTY)) {
                continue;
            }
            String name  = child.getAttribute(ATTR_NAME);
            String value = child.getAttribute(ATTR_VALUE);
            if ((name == null) || (value == null)) {
                continue;
            }
            properties.put(name, value);
        }
    }






    /**
     *  This sets the editField
     *
     * @param f _more_
     */
    public void setCanEdit(boolean f) {
        canEdit = f;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getCanEdit() {
        return canEdit;
    }


    /**
     *  Set whether the given  user is being ignored or not
     *
     * @param user _more_
     * @param ignore _more_
     */
    public void setIgnoreUser(ChatUser user, boolean ignore) {
        user.setIgnored(ignore);
        if (avatarCanvas != null) {
            avatarCanvas.setIgnore(user, ignore);
        }
        if (textCanvas != null) {
            textCanvas.repaint();
        }
    }

    /**
     *  Take the text from the input field and send a MSG_TEXT message to the server
     *
     * @param comp _more_
     */
    public void writeInput(JTextComponent comp) {
        if (comp == null) {
            return;
        }
        writeInput(comp.getText());
        comp.setText("");
    }

    /**
     * _more_
     *
     * @param msg _more_
     */
    public void writeInput(String msg) {
        msg = prefix + msg;
        write(MSG_TEXT, XmlNode.encode(msg));
        if ( !checkUrl(msg) && getMyUser().userOk()) {
            avatarCanvas.message(getMyUser(), msg);
            putText(getMyUser(), msg);
        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    ChatUser getMyUser() {
        if (myUser == null) {
            myUser = ChatUser.getUser(getUserName());
        }
        return myUser;
    }

    /**
     *  Write the "GFX" message. Called by the WhiteBoardCanvas
     *
     * @param subXml _more_
     */
    public void writeGfx(String subXml) {
        write(MSG_GFX, subXml);
    }






    /**
     *  Send a private message to the user
     *
     * @param to _more_
     * @param msg _more_
     */
    public void writePrivate(ChatUser to, String msg) {
        write(MSG_PRIVATE, attr("to", to.getId()), XmlNode.encode(msg));
    }


    /**
     * _more_
     *
     * @param otherUser _more_
     * @param msg _more_
     */
    public void showPrivateMessage(ChatUser otherUser, String msg) {
        PrivateMessageDialog theDialog = PrivateMessageDialog.find(otherUser,
                                             this);
        theDialog.message(msg);
    }

    /**
     *  Find and/or create the private message dialog for the given user
     *
     * @param otherUser _more_
     */
    public void createPrivateMessage(ChatUser otherUser) {
        PrivateMessageDialog.find(otherUser, this);
    }


    /**
     *  Called from base class to handle message type="STATE" messages
     *
     * @param message _more_
     */
    public void processState(XmlNode message) {
        super.processState(message);
        String v = message.getAttribute(ATTR_EDIT);
        if (v != null) {
            setCanEdit(new Boolean(v).booleanValue());
        }
        if (roomLabel != null) {
            roomLabel.setText("Room: " + roomName);
        }

        getMyUser().setName(userName);
        getMyUser().setId(userId);
    }

    /**
     *  Handle the FILE message
     *
     * @param message _more_
     */
    public void processFile(XmlNode message) {
        if (fileList == null) {
            return;
        }
        String filename = message.getAttribute(ATTR_NAME);
        String url      = message.getAttribute(ATTR_URL);
        String fileType = message.getAttribute("filetype");
        addFile(url, filename, fileType.equals("image"));
    }

    /**
     * _more_
     *
     * @param url _more_
     * @param filename _more_
     * @param isImage _more_
     */
    public void addFile(String url, String filename, boolean isImage) {
        if (filename == null) {
            filename = url;
        }
        if (filename == null) {
            return;
        }
        UrlEntry entry = new UrlEntry(url, filename, isImage);
        if (files.contains(entry)) {
            return;
        }
        files.add(entry);
        updateFileList();
    }

    /**
     * _more_
     */
    private void updateFileList() {
        if (onlyShowImages) {
            fileList.setListData(getImageUrls().toArray());
        } else {
            fileList.setListData(files.toArray());
        }
    }


    /**
     *  Popup dialog to enter a url
     */
    public void selectFile() {
        TextField urlFld = new TextField(50);
        Label     urlLbl = new Label("URL:", Label.RIGHT);
        Container entry = GuiUtils.doLayout(new Component[] { urlLbl,
                urlFld }, 2, GuiUtils.DS_NY, GuiUtils.DS_N);
        JFrame         myFrame   = GuiUtils.getFrame(this);
        OkCancelDialog theDialog = new OkCancelDialog(myFrame, entry);
        show(theDialog);
        if (theDialog.okPressed) {
            String url = urlFld.getText().trim();
            addFile(url, url, isImage(url));
        }
    }


    /**
     * _more_
     *
     * @param message _more_
     */
    public void processRooms(XmlNode message) {
        roomIds   = new Vector();
        roomNames = new Vector();
        Vector children = message.getChildren();
        for (int i = 0; i < children.size(); i++) {
            XmlNode child = (XmlNode) children.elementAt(i);
            if ( !child.tagEquals(TAG_ROOM)) {
                continue;
            }
            roomNames.addElement(child.getAttribute(ATTR_NAME, ""));
            roomIds.addElement(child.getAttribute(ATTR_ID, ""));
        }
    }



    /**
     *  Add new users
     *
     * @param message _more_
     * @param showText _more_
     */
    public void processUsers(XmlNode message, boolean showText) {
        Vector children = message.getChildren();
        for (int i = 0; i < children.size(); i++) {
            XmlNode child = (XmlNode) children.elementAt(i);
            if ( !child.tagEquals(TAG_USER)) {
                continue;
            }
            String name = child.getAttribute(ATTR_NAME);
            if (name == null) {
                continue;
            }
            if (showText && !(getUserName().equals(name))) {
                putText("New user: " + name);
            }
            addUser(child.getAttribute(ATTR_ID), name,
                    child.getAttribute(ATTR_ICON));
        }
    }

    /**
     *  A user (or users) has gone
     *
     * @param message _more_
     */
    public void processUserRemove(XmlNode message) {
        Vector children = message.getChildren();
        for (int i = 0; i < children.size(); i++) {
            XmlNode child = (XmlNode) children.elementAt(i);
            String  id    = child.getAttribute(ATTR_ID);
            if (id == null) {
                continue;
            }
            ChatUser oldUser = ChatUser.getUser(id);
            int      idx     = users.indexOf(oldUser);
            if (idx < 0) {
                continue;
            }
            userList.removeUserAt(idx);
            users.removeElement(oldUser);
            putText("User gone: " + oldUser);
            avatarCanvas.removeUser(oldUser);
        }
    }

    /**
     *  Pull out the text part of the XmlNode
     *  (e.g., &lt;givennode&gt;Some text&lt/givennode&gt;
     *
     * @param message _more_
     *
     * @return _more_
     */
    public static String getBody(XmlNode message) {
        String body = message.getChildValue();
        if (body == null) {
            body = "";
        } else {
            body = body.trim();
        }
        return XmlNode.decode(body);
    }


    /**
     *  If a user types "url:http://..."
     *  in the text chat we treat that as a url and add it
     *  to the list of files.
     *
     * @param body _more_
     *
     * @return _more_
     */
    public boolean checkUrl(String body) {
        if (body.startsWith("url:")) {
            String url = body.substring(4);
            addFile(url, url, isImage(url));
            return true;
        }
        return false;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    ArrayList<UrlEntry> getImageUrls() {
        ArrayList<UrlEntry> images = new ArrayList<UrlEntry>();
        for (UrlEntry entry : (Vector<UrlEntry>) files) {
            if (entry.isImage) {
                images.add(entry);
            }
        }
        return images;
    }

    /**
     * _more_
     *
     * @param url _more_
     *
     * @return _more_
     */
    public boolean isImage(String url) {
        url = url.toLowerCase();
        return url.endsWith(".gif") || url.endsWith(".png")
               || url.endsWith(".");
    }

    /**
     *  Handle the incoming message (in xml) from the chat server
     *
     * @param type _more_
     * @param message _more_
     */
    public void processMessage(String type, XmlNode message) {
        if ( !haveInitialized || type.equals(MSG_SESSION)) {
            if ( !type.equals(MSG_SESSION)) {
                return;
            }
            getMyUser().setId(message.getAttribute(ATTR_ID));
            haveInitialized = true;
            return;
        }

        ChatUser from = ChatUser.getUser(message.getAttribute(ATTR_FROM),
                                         message.getAttribute(ATTR_NAME),
                                         false);
        if ((from != null) && !from.userOk()) {
            return;
        }
        if (type.equals(MSG_TEXT)) {
            if (ignoreWelcome) {
                ignoreWelcome = false;
                return;
            }

            String body = getBody(message);
            if ( !checkUrl(body)) {
                putText(from, body);
                avatarCanvas.message(from, body);
            }
            repaint();
        } else if (type.equals(MSG_LOGIN)) {
            String body = message.getChildValue();
            if (body == null) {
                body = "Please login";
            }
            boolean   justName  = message.getAttribute("justname", false);
            TextField nameField = new TextField((justName
                    ? ""
                    : getUserName()), 20);
            TextField pwdField  = new TextField("", 20);
            pwdField.setEchoChar('*');
            Container contents = (justName
                                  ? GuiUtils.doLayout(new Component[] {
                                      new Label("Name:"),
                                      nameField }, 2, GuiUtils.DS_NN,
                                          GuiUtils.DS_NN)
                                  : GuiUtils.doLayout(new Component[] {
                                      new Label("Name:"),
                                      nameField, new Label("Password:"),
                                      pwdField }, 2, GuiUtils.DS_NN,
                                          GuiUtils.DS_NN));

            JFrame myFrame = GuiUtils.getFrame(this);
            OkCancelDialog theDialog =
                new OkCancelDialog(myFrame,
                                   GuiUtils.topCenterBottom(new Label(body),
                                       contents, null));
            show(theDialog);
            if (theDialog.okPressed) {
                userName = nameField.getText();
                myUser   = ChatUser.getUser(getUserName());
                write(MSG_LOGIN,
                      attr("channel", room)
                      + attr("userid", nameField.getText().trim())
                      + attr("password", pwdField.getText().trim()), "");
            } else {
                disconnect();
                notifyDisconnect();
            }
        } else if (type.equals(MSG_PRIVATE)) {
            showPrivateMessage(from, getBody(message));
        } else if (type.equals(MSG_GFX)) {
            canvas.processMsg(message);
        } else if (type.equals(MSG_USERADD)) {
            processUsers(message, true);
        } else if (type.equals(MSG_ROOMS)) {
            processRooms(message);
        } else if (type.equals(MSG_USERLIST)) {
            //First time
            if (userList == null) {
                userList = new UserList("FRED", this);
            }
            userList.clear();
            users = new Vector();
            processUsers(message, false);
        } else if (type.equals(MSG_USERREMOVE)) {
            processUserRemove(message);
        } else if (type.equals(MSG_URL)) {
            String url = message.getAttribute(ATTR_URL);
            if (url != null) {
                showUrl(url, "CHAT.URL");
            }
        } else if (type.equals(MSG_FILE)) {
            processFile(message);
        } else {
            super.processMessage(type, message);
        }
        repaint();
    }


    /**
     *  New user, add it to the UserList
     *
     * @param id _more_
     * @param name _more_
     * @param icon _more_
     */
    public void addUser(String id, String name, String icon) {
        ChatUser newUser = ChatUser.getUser(id, name);
        newUser.setIconUrl(icon);
        if (users.contains(newUser)) {
            return;
        }
        users.addElement(newUser);
        System.err.println("Adding user:" + newUser + " have list:"
                           + (userList != null));
        if (userList != null) {
            userList.addUser(newUser);
        }
        avatarCanvas.addUser(newUser);
    }

    /**
     *  Called at start up to tell the server what is the "room"
     *  we are on. Need to add the ability to list rooms, switch rooms,
     *  etc.
     */
    public void initConnection() {
        super.initConnection();
        //Write the <message type=SESSION channel=... sessionid=... userid=...> tag
        write(MSG_SESSION,
              attr("channel", room) + attr("sessionid", sessionId), "");

        write(MSG_USERLIST, "");
        write(MSG_FILE, "");

    }

    /**
     * _more_
     *
     * @param t _more_
     */
    public void putText(String t) {
        putText(ChatUser.getUser(USER_CHAT), t);
    }

    /**
     *  Write the given text to the chat TextArea
     *
     * @param u _more_
     * @param t _more_
     */
    public void putText(ChatUser u, String t) {
        if (textCanvas != null) {
            textCanvas.addMessage(u, t);
        }
        textArea.setText(textArea.getText() + "\n(" + u.getName() + ") " + t);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getSelectedFile() {
        UrlEntry entry = (UrlEntry) fileList.getSelectedValue();
        if (entry != null) {
            return entry.url;
        }
        return null;

    }



    /**
     *  Popup the files menu
     *
     * @param src _more_
     */
    public void showFilesMenu(Component src) {
        JPopupMenu filesMenu = new JPopupMenu();
        this.add(filesMenu);
        filesMenu.add(makeMenuItem("Add new url", CMD_FILES_SELECT));
        if (loadFilesUrl != null) {
            filesMenu.add(makeMenuItem("Select file from server",
                                       CMD_FILES_UPLOAD));
        }
        filesMenu.add(makeMenuItem("Remove selected", CMD_FILES_REMOVE));
        filesMenu.add(makeMenuItem("Share selected", CMD_FILES_SHARE));
        filesMenu.add(makeMenuItem("Use as background", CMD_FILES_VIEW));
        Rectangle bounds = src.getBounds();
        filesMenu.show(src, 0, bounds.height);
    }



    /**
     * _more_
     *
     * @param cmd _more_
     *
     * @return _more_
     */
    private JTextComponent getTextComponent(String cmd) {
        Component comp = xmlUi.argToComponent(cmd);
        if (comp == null) {
            return null;
        }
        if ( !(comp instanceof JTextComponent)) {
            System.err.println("Not a TextComponent " + comp);
            return null;
        }
        return (JTextComponent) comp;
    }

    /**
     *
     * @param event _more_
     */
    public void actionPerformed(ActionEvent event) {

        Object source = event.getSource();
        String cmd    = event.getActionCommand();
        if (cmd.startsWith(CMD_SHOW)) {
            String   text     = textArea.getText();
            TextArea textArea = new TextArea(text, 10, 50);
            JFrame   myFrame  = GuiUtils.getFrame(this);
            OkCancelDialog ok = new OkCancelDialog(myFrame, textArea, true,
                                    false);
            ok.init();
            return;
        }


        if (cmd.equals("chat.debug")) {
            SocketApplet.debug  = !SocketApplet.debug;
            DisplayCanvas.debug = !DisplayCanvas.debug;
            CanvasCommand.debug = !CanvasCommand.debug;
        } else if (cmd.startsWith("wb.")) {
            canvas.actionPerformed(new ActionEvent(source, 0,
                    cmd.substring(3)));
        } else if (cmd.equals(CMD_UI_FLOAT)) {
            toggleFloat();
        } else if (cmd.equals(CMD_USERS_IGNORE)) {
            userList.toggleIgnore();
        } else if (cmd.equals(CMD_USERS_PRIVATE)) {
            ChatUser user = userList.getSelectedItem();
            if (user != null) {
                createPrivateMessage(user);
            }
        } else if (cmd.startsWith(CMD_SETPREFIX)) {
            JTextComponent comp = getTextComponent(cmd);
            if (comp == null) {
                return;
            }
            prefix = comp.getText();
        } else if (cmd.startsWith(CMD_SHOWPREFIX)) {
            JTextComponent comp = getTextComponent(cmd);
            if (comp == null) {
                return;
            }
            comp.setText(comp.getText() + " " + prefix);
        } else if (cmd.startsWith(CMD_CLEARPREFIX)) {
            prefix = "";
        } else if (cmd.startsWith(CMD_WRITE)) {
            String text = XmlUi.extractOneArg(cmd);
            if (text == null) {
                return;
            }
            writeInput(text);
        } else if (cmd.equals(CMD_INPUT)
                   && (source instanceof JTextComponent)) {
            writeInput((JTextComponent) source);
        } else if (cmd.startsWith(CMD_INPUT)) {
            JTextComponent comp = getTextComponent(cmd);
            if (comp == null) {
                return;
            }
            writeInput(comp);
        } else if (cmd.equals(CMD_FILES_TOGGLE)) {
            onlyShowImages = !onlyShowImages;
            updateFileList();
        } else if (cmd.equals(CMD_FILES_REMOVE)) {
            UrlEntry entry = (UrlEntry) fileList.getSelectedValue();
            if (entry != null) {
                files.remove(entry);
                updateFileList();
            }
        } else if ((source == fileList) || cmd.equals(CMD_FILES_GO)) {
            String file = getSelectedFile();
            if (file != null) {
                showUrl(file, "CHAT.FILE");
            }
        } else if (cmd.equals(CMD_FILES_VIEW)) {
            String file = getSelectedFile();
            if (file != null) {
                Image image = getImage(file);
                canvas.setBackgroundImage(image, file);
                canvas.write(canvas.CMD_BACKGROUNDIMAGE,
                             XmlNode.attr("url", file));
            }
        } else if (cmd.equals(CMD_FILES_SHARE)) {
            String file = getSelectedFile();
            if (file != null) {
                write(MSG_FILE, attr("url", file), "");
            }
        } else if (cmd.equals(CMD_FILES_SELECT)) {
            selectFile();
        } else if (cmd.equals(CMD_FILES_UPLOAD)) {
            showUrl(getFullUrl(loadFilesUrl), "CHAT.FILEUPLOAD");
        } else if (cmd.equals(CMD_CLEAR)) {
            clearText();
        } else if (cmd.equals(CMD_CHANGEROOM)) {
            List roomList = new List();
            for (int i = 0; i < roomNames.size(); i++) {
                roomList.add(roomNames.elementAt(i).toString());
                if (roomIds.elementAt(i).equals(room)) {
                    roomList.select(i);
                }
            }

            //      Frame myFrame =(Frame) getParent ();
            JFrame myFrame = GuiUtils.getFrame(this);
            OkCancelDialog theDialog =
                new OkCancelDialog(
                    myFrame,
                    GuiUtils.topCenterBottom(
                        new Label("Select a new room"), roomList, null));
            theDialog.size = new Dimension(150, 200);
            show(theDialog);
            if ( !theDialog.okPressed) {
                return;
            }
            int idx = roomList.getSelectedIndex();
            if (idx < 0) {
                return;
            }
            String newRoom = roomIds.elementAt(idx).toString();
            if (room.equals(newRoom)) {
                return;
            }
            room     = newRoom;
            roomName = roomNames.elementAt(idx).toString();
            clearText();
            canvas.clearAll();
            userList.clear();
            write(MSG_LEAVEROOM, "");
            initConnection();
        } else if (cmd.equals(CMD_SAVESESSION)) {
            beginPublishTranscript();
        }

    }


    /**
     * _more_
     */
    public void clearText() {
        textArea.setText("");
        textCanvas.clear();
        avatarCanvas.clear();
    }

    /**
     * _more_
     */
    public void notifyDisconnect() {
        setFloat(false);
        super.notifyDisconnect();
    }

    /**
     * _more_
     */
    public void stop() {
        setFloat(false);
        super.stop();
    }

    /**
     * _more_
     */
    public void toggleFloat() {
        setFloat( !floating);
    }

    /**
     *  Float or embed the applet.
     *
     * @param doFloat _more_
     */
    public void setFloat(boolean doFloat) {
        if (doFloat == floating) {
            return;
        }

        if (floating) {
            oldParent.add("Center", this);
            floatFrame.dispose();
            invalidate();
            oldParent.validate();
        } else {
            oldParent  = getParent();
            floatFrame = new Frame();
            floatFrame.setLayout(new BorderLayout());
            floatFrame.add("Center", this);
            floatFrame.addWindowListener(this);
            floatFrame.pack();
            floatFrame.show();
            if (firstFloat && initFloat) {
                floatFrame.setSize(new Dimension(500, 600));
            }
        }
        firstFloat = false;
        floating   = !floating;
    }


    /**
     * _more_
     *
     * @param e _more_
     */
    public void windowClosing(WindowEvent e) {
        if (e.getSource() == floatFrame) {
            setFloat(false);
        }
    }

    /**
     * _more_
     *
     * @param e _more_
     */
    public void windowActivated(WindowEvent e) {}

    /**
     * _more_
     *
     * @param e _more_
     */
    public void windowClosed(WindowEvent e) {}

    /**
     * _more_
     *
     * @param e _more_
     */
    public void windowDeactivated(WindowEvent e) {}

    /**
     * _more_
     *
     * @param e _more_
     */
    public void windowDeiconified(WindowEvent e) {}

    /**
     * _more_
     *
     * @param e _more_
     */
    public void windowIconified(WindowEvent e) {}

    /**
     * _more_
     *
     * @param e _more_
     */
    public void windowOpened(WindowEvent e) {}





    /**
     *  Not working with the new protocol
     */
    public void beginPublishTranscript() {
        Label sessionLbl = new Label("Transcript name:", Label.RIGHT);
        final Label label =
            new Label(
                "Note: Any  previous transcripts  with the same name will be overwritten");
        final TextField sessionFld = new TextField(transcriptName, 30);
        final JCheckBox textCbx    = new JCheckBox("Text", null, true);
        final JCheckBox filesCbx   = new JCheckBox("Files", null, true);
        final JCheckBox drawingCbx = new JCheckBox("Whiteboard", null, true);
        Component       whichPanel;

        if (fileList != null) {
            whichPanel = GuiUtils.doLayout(new Component[] {
                new Label("Save:"),
                textCbx, filesCbx, drawingCbx }, 4, GuiUtils.DS_N,
                    GuiUtils.DS_N);
        } else {
            filesCbx.setSelected(false);
            whichPanel = GuiUtils.doLayout(new Component[] {
                new Label("Save:"),
                textCbx, drawingCbx }, 3, GuiUtils.DS_N, GuiUtils.DS_N);
        }


        Container contents = GuiUtils.doLayout(new Component[] { label,
                GuiUtils.doLayout(new Component[] { sessionLbl, sessionFld },
                                  2, GuiUtils.DS_NY, GuiUtils.DS_N),
                whichPanel }, 1, GuiUtils.DS_Y, GuiUtils.DS_N);


        JFrame         myFrame   = GuiUtils.getFrame(this);
        OkCancelDialog theDialog = new OkCancelDialog(myFrame, contents);
        show(theDialog);
        if (theDialog.okPressed) {
            doPublishTranscript(sessionFld.getText(), textCbx.isSelected(),
                                filesCbx.isSelected(),
                                drawingCbx.isSelected());
        }
    }


    /**
     * Class UrlEntry _more_
     *
     *
     * @author IDV Development Team
     * @version $Revision: 1.3 $
     */
    public static class UrlEntry {

        /** _more_ */
        String url;

        /** _more_ */
        String name;

        /** _more_ */
        boolean isImage = false;

        /**
         * _more_
         *
         * @param url _more_
         * @param name _more_
         * @param isImage _more_
         */
        public UrlEntry(String url, String name, boolean isImage) {
            this.url     = url;
            this.name    = name;
            this.isImage = isImage;
        }

        /**
         * _more_
         *
         * @return _more_
         */
        public String getUrl() {
            return url;
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
         * @param o _more_
         *
         * @return _more_
         */
        public boolean equals(Object o) {
            if ( !getClass().equals(o.getClass())) {
                return false;
            }
            UrlEntry that = (UrlEntry) o;
            return this.url.equals(that.url);
        }
    }


    /**
     *  Not working for now.
     *
     * @param title _more_
     * @param includeText _more_
     * @param includeFiles _more_
     * @param includeDrawing _more_
     */
    public void doPublishTranscript(String title, boolean includeText,
                                    boolean includeFiles,
                                    boolean includeDrawing) {
        try {
            String textXml  = "";
            String gfxXml   = "";
            String filesXml = "";

            if (includeDrawing) {
                gfxXml = canvas.getGlyphXml();
            }
            if (includeText) {
                textXml = textCanvas.getTextXml();
            }

            if (includeFiles && (fileList != null)) {
                ListModel model = fileList.getModel();
                for (int i = 0; i < model.getSize(); i++) {
                    UrlEntry entry = (UrlEntry) model.getElementAt(i);
                    filesXml = filesXml + "<message type=\"FILE\" name=\""
                               + entry.name + "\"  url=\"" + entry.url
                               + "\" />\n";
                }
            }

            write("SAVE",
                  "BEGINSUBJECT:" + title + ":ENDSUBJECT" + textXml
                  + filesXml + gfxXml);
        } catch (Exception exc) {}
    }


    /**
     *  Utility to create a button
     *
     * @param label _more_
     * @param cmd _more_
     * @param listener _more_
     *
     * @return _more_
     */
    public static JButton makeButton(String label, String cmd,
                                     ActionListener listener) {
        JButton b = new JButton(label);
        b.addActionListener(listener);
        b.setActionCommand(cmd);
        return b;
    }


    /**
     * _more_
     *
     * @param label _more_
     * @param command _more_
     *
     * @return _more_
     */
    JMenuItem makeMenuItem(String label, String command) {
        JMenuItem mi = new JMenuItem(label);
        mi.setActionCommand(command);
        mi.addActionListener((ActionListener) this);
        return mi;
    }





}

