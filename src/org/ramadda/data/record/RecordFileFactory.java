/*
 * Copyright 2010 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
 * http://www.unavco.org
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
 * 
 */

package org.ramadda.data.record;

import org.ramadda.data.record.*;
import org.ramadda.data.record.filter.*;

import ucar.unidata.util.Misc;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.StringUtil;


import java.io.File;
import java.util.List;
import java.util.Hashtable;
import java.util.Properties;
import java.util.ArrayList;


/**
 * Class description
 *
 *
 * @version        Enter version here..., Fri, May 21, '10
 * @author         Enter your name here...
 */
public class RecordFileFactory {

    private List<RecordFile> prototypes = new ArrayList<RecordFile>();

    public RecordFileFactory() {
    }

    public RecordFileFactory(String classListFile) throws Exception {
        addPrototypes(classListFile);
    }

    public void addPrototype(RecordFile file) {
        prototypes.add(file);
    }

    public void addPrototypes(String classListFile) throws Exception {
        for(String line: StringUtil.split(IOUtil.readContents(classListFile,getClass()),"\n",true,true)) {
            if(line.startsWith("#")) continue;
            Class c = Misc.findClass(line);
            addPrototype((RecordFile)c.newInstance());
        }
    }


    public RecordFile doMakeRecordFile(String path) throws Exception {
        return doMakeRecordFile(path, null);
    }

    public RecordFile doMakeRecordFile(String path, Hashtable properties) throws Exception {
        for(RecordFile f: prototypes) {
            if (f.canLoad(path)) {
                //                System.err.println("loading " +  f.getClass().getName());
                return  f.cloneMe(path, properties);
            }
        }
        throw new IllegalArgumentException("Unknown file type:" + path);
    }

    public boolean canLoad(String path) throws Exception {
        for(RecordFile f: prototypes) {
            if (f.canLoad(path)) {
                return true;
            }
        }
	return false;
    }


    public static void main(String[] args) throws Exception {


        for(int i=0;i<args.length;i++) {
	    final int[]cnt = {0};
            RecordFile file  = new RecordFileFactory().doMakeRecordFile(args[i]);
	    
	    final RecordVisitor  visitor = new RecordVisitor() {
		    public boolean visitRecord(RecordFile file, VisitInfo visitInfo, Record record) {
			cnt[0]++;
			return true;
		    }};

	    file.visit(visitor, new VisitInfo(), null);
	    System.err.println(args[i] +" #points:" + cnt[0]);
        }
    }



}
