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

package org.ramadda.data.record;


import org.ramadda.data.record.*;
import org.ramadda.data.record.filter.*;

import ucar.unidata.util.IOUtil;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;


import java.io.File;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;


/**
 * Class description
 *
 *
 * @version        Enter version here..., Fri, May 21, '10
 * @author         Enter your name here...
 */
public class RecordFileFactory {

    /** _more_ */
    private List<RecordFile> prototypes = new ArrayList<RecordFile>();

    /**
     * _more_
     */
    public RecordFileFactory() {}

    /**
     * _more_
     *
     * @param classListFile _more_
     *
     * @throws Exception _more_
     */
    public RecordFileFactory(String classListFile) throws Exception {
        addPrototypes(classListFile);
    }

    /**
     * _more_
     *
     * @param file _more_
     */
    public void addPrototype(RecordFile file) {
        prototypes.add(file);
    }

    /**
     * _more_
     *
     * @param classListFile _more_
     *
     * @throws Exception _more_
     */
    public void addPrototypes(String classListFile) throws Exception {
        for (String line :
                StringUtil.split(IOUtil.readContents(classListFile,
                    getClass()), "\n", true, true)) {
            if (line.startsWith("#")) {
                continue;
            }
            Class c = Misc.findClass(line);
            addPrototype((RecordFile) c.newInstance());
        }
    }


    /**
     * _more_
     *
     * @param path _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public RecordFile doMakeRecordFile(String path) throws Exception {
        return doMakeRecordFile(path, null);
    }

    /**
     * _more_
     *
     * @param path _more_
     * @param properties _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public RecordFile doMakeRecordFile(String path, Hashtable properties)
            throws Exception {
        for (RecordFile f : prototypes) {
            if (f.canLoad(path)) {
                //                System.err.println("loading " +  f.getClass().getName());
                return f.cloneMe(path, properties);
            }
        }

        throw new IllegalArgumentException("Unknown file type:" + path);
    }

    /**
     * _more_
     *
     * @param path _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public boolean canLoad(String path) throws Exception {
        for (RecordFile f : prototypes) {
            if (f.canLoad(path)) {
                return true;
            }
        }

        return false;
    }


    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public void test(String[] args) throws Exception {
        for (int i = 0; i < args.length; i++) {
            final int[]         cnt     = { 0 };
            RecordFile          file    = doMakeRecordFile(args[i]);

            final RecordVisitor visitor = new RecordVisitor() {
                public boolean visitRecord(RecordFile file,
                                           VisitInfo visitInfo,
                                           Record record) {
                    cnt[0]++;

                    return true;
                }
            };

            file.visit(visitor, new VisitInfo(), null);
            System.err.println(args[i] + " #points:" + cnt[0]);
        }

    }

    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
        new RecordFileFactory().test(args);
    }



}
