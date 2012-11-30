package org.ramadda.data.tools;

import org.ramadda.data.record.*;
import org.ramadda.data.point.*;
import org.ramadda.data.record.filter.*;

import ucar.unidata.util.Misc;

import java.io.*;

import java.util.ArrayList;

import java.util.List;
import java.lang.reflect.*;


/**
 */
public  class RecordTool {

    private RecordFileFactory recordFileFactory;

    private String recordFileClass;

    public RecordTool (String fileFactoryClass) throws Exception {
        if(fileFactoryClass!=null) {
            recordFileFactory  = (RecordFileFactory) Misc.findClass(fileFactoryClass).newInstance();
        }
    }

    public RecordFile doMakeRecordFile(String inFile) throws Exception {
        if(recordFileClass!=null) {
            Class c = Misc.findClass(recordFileClass);
            Constructor ctor = Misc.findConstructor(c,
                                                    new Class[] { String.class});
            return (RecordFile)ctor.newInstance(new Object[] { inFile});
        }
        return getRecordFileFactory().doMakeRecordFile(inFile);
    }

    /**
     * _more_
     *
     * @param message _more_
     */
    public void usage(String message) {
        System.err.println("Error:" + message);
        System.exit(1);
    }

    public RecordFileFactory getRecordFileFactory() throws Exception {
        return recordFileFactory;
    }

/**
Set the RecordFileClass property.

@param value The new value for RecordFileClass
**/
public void setRecordFileClass (String value) {
	recordFileClass = value;
}

/**
Get the RecordFileClass property.

@return The RecordFileClass
**/
public String getRecordFileClass () {
	return recordFileClass;
}



}