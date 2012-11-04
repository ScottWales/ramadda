package org.ramadda.data.tools;

import org.ramadda.data.record.*;
import org.ramadda.data.point.*;
import org.ramadda.data.record.filter.*;

import ucar.unidata.util.Misc;

import java.io.*;

import java.util.ArrayList;

import java.util.List;


/**
 */
public  class RecordTool {

    private RecordFileFactory recordFileFactory;

    public RecordTool (String fileFactoryClass) throws Exception {
        recordFileFactory  = (RecordFileFactory) Misc.findClass(fileFactoryClass).newInstance();
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
}