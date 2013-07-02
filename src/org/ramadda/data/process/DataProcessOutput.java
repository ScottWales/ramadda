/*
* Copyright 2008-2013 Jeff McWhirter/ramadda.org
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
package org.ramadda.data.process;


import org.ramadda.repository.Entry;

import ucar.unidata.util.Misc;

import java.util.ArrayList;
import java.util.List;


/**
 * A class to hold DataProcess output
 */
public class DataProcessOutput {

    /** empty output id */
    public int DATAPROCESS_OUTPUT_EMPTY = 0;

    /** entry output id */
    public int DATAPROCESS_OUTPUT_ENTRY = 1;

    /** List of entries */
    private List<Entry> entries;

    /**
     * Default ctor
     */
    public DataProcessOutput() {}

    /**
     * Create a DataProcessOutput from the file
     *
     * @param file  the associated file
     */
    public DataProcessOutput(Entry entry) {
        this((List<Entry>) Misc.newList(entry));
    }

    /**
     * Create the DataProcessOutput from a list of files
     *
     * @param files  the files
     */
    public DataProcessOutput(List<Entry> entries) {
        this.entries = new ArrayList<Entry>(entries);
    }

    /**
     * Get the entries
     *
     * @return the entries or an empty list
     */
    public List<Entry> getEntries() {
        if (entries == null) {
            return new ArrayList<Entry>();
        }

        return entries;
    }

    /**
     * Get the DataProcessOutput type
     *
     * @return 
     */
    public int getDataProcessOutputType() {
        if ((entries == null) || entries.isEmpty()) {
            return DATAPROCESS_OUTPUT_EMPTY;
        }

        return DATAPROCESS_OUTPUT_ENTRY;
    }

    /**
     * Check if this has output
     *
     * @return  true if output type is not EMPTY
     */
    public boolean hasOutput() {
        return getDataProcessOutputType() != DATAPROCESS_OUTPUT_EMPTY;
    }
}
