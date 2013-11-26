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

package org.ramadda.repository.util;


import org.ramadda.repository.Request;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.output.*;

import org.ramadda.sql.SqlUtil;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;

import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.WrapperException;
import ucar.unidata.xml.XmlUtil;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import java.lang.reflect.*;



import java.net.*;




import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.regex.*;



import java.util.regex.*;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 *
 *
 * @author RAMADDA Development Team
 */
public class RequestArgument {

    /** _more_          */
    private String argsProperty;


    /** _more_ */
    private List<String> args;


    /**
     * _more_
     *
     * @param argsProperty _more_
     */
    public RequestArgument(String argsProperty) {
        this.argsProperty = argsProperty;
    }


    /**
     * _more_
     *
     * @param args _more_
     */
    public RequestArgument(String[] args) {
        this.args = new ArrayList<String>();
        for (String arg : args) {
            this.args.add(arg);
        }
    }

    /**
     * _more_
     *
     *
     * @param request _more_
     * @return _more_
     */
    public List<String> getArgs(Request request) {
        if (args == null) {
            args = StringUtil.split(
                request.getRepository().getProperty(argsProperty, ""));
        }

        return args;
    }



}
