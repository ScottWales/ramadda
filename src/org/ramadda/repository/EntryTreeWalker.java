/*
* Copyright 2008-2012 Jeff McWhirter/ramadda.org
*                     Don Murray/CU-CIRES
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


import org.ramadda.repository.auth.*;

import org.ramadda.repository.database.*;
import org.ramadda.repository.harvester.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;

import org.ramadda.repository.type.*;
import org.ramadda.util.HtmlTemplate;
import org.ramadda.util.Utils;

import org.ramadda.util.HtmlUtils;
import org.ramadda.util.TTLCache;
import org.ramadda.util.TTLObject;

import org.ramadda.util.TempDir;

import org.w3c.dom.*;


import ucar.unidata.sql.Clause;
import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.TwoFacedObject;


import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.GuiUtils;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.JobManager;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;

import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlNodeList;
import ucar.unidata.xml.XmlUtil;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;


import java.io.*;

import java.io.File;
import java.io.InputStream;



import java.net.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import java.text.SimpleDateFormat;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import java.util.regex.*;


public abstract class EntryTreeWalker  implements Constants {
    private Repository repository;
    private Request request;
    private boolean recurse;
    private int totalCnt = 0;
    private int processedCnt = 0;
    private Object actionId;
    private StringBuffer sb = new StringBuffer();


    public EntryTreeWalker(Request request, Repository repository, Object actionId) {
        this.repository = repository;
        this.request = request;
        this.actionId  =actionId;
        recurse = request.get(EntryManager.ARG_WALK_RECURSE, false);
    }

    public StringBuffer getMessageBuffer() {
        return sb;
    }

    public Repository getRepository() {
        return repository;
    }

    public Request getRequest() {
        return request;
    }

    public void append(Object object) {
        sb.append(object);
    }

    public void incrementProcessedCnt(int by) {
        processedCnt+=by;
    }
    public boolean isRunning() {
        return getRepository().getActionManager().getActionOk(actionId);
    }

    public boolean entryOk(Entry entry) {
        if(!entry.isGroup()) return false;
        if(entry.getTypeHandler().isSynthType() || 
           getRepository().getEntryManager().isSynthEntry(entry.getId())) return false;
        return true;
    }

    public void updateMessage() {
        getRepository().getActionManager().setActionMessage(actionId,
                                            "# entries:" + totalCnt +"<br># changed entries:" + processedCnt);
    }

    public boolean walk(Entry entry) throws Exception {
        if(!isRunning()) return true;
        if(!entryOk(entry)) return true;
        totalCnt++;
        updateMessage();
        List<Entry> children = getRepository().getEntryManager().getChildren(request, entry);
        if (children == null) {
            return true;
        }
        if(recurse) {
            for(Entry child: children) {
                if(!getRepository().getActionManager().getActionOk(actionId)) return false;
                if(!walk(child)) return false;
            }
        }
        if(!processEntry(entry, children)) return false;
        return true;
    }

    public abstract boolean processEntry(Entry entry, List<Entry> children) throws Exception;

}

