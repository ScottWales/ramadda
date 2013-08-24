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
package org.ramadda.data.point.netcdf;


import org.ramadda.data.point.*;

import org.ramadda.data.record.*;

import ucar.unidata.util.StringUtil;

import java.io.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


/**
 * Class description
 *
 *
 * @version        $version$, Fri, Aug 23, '13
 * @author         Enter your name here...    
 */
public class NetcdfRecord extends DataRecord {


    /**
     * _more_
     *
     * @param file _more_
     * @param fields _more_
     */
    public NetcdfRecord(RecordFile file, List<RecordField> fields) {
        super(file, fields);
        initFields(fields);
    }


    /**
     * _more_
     *
     * @param recordIO _more_
     *
     * @return _more_
     *
     * @throws IOException _more_
     */
    public ReadStatus read(RecordIO recordIO) throws IOException {
        ReadStatus status = ReadStatus.OK;



        //        setLocation(values[idxX], values[idxY], ((idxZ >= 0)
        //                ? values[idxZ]
        //                : 0));
        return status;
    }


}
