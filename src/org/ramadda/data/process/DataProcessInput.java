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


import java.io.File;
import java.util.List;


/**
 * Class description
 *
 *
 */
public class DataProcessInput {


    private Entry processEntry;

    private File processDir;

    /** The entries for this input */
    private List<Entry> entries;

    public DataProcessInput(List<Entry> entries) {
        this.entries = entries;
    }

    /**
     * Create a DataProcessInput from a list of entries
     *
     * @param entries the entries
     */
    public DataProcessInput(Entry processEntry, File dir, 
                            List<Entry> entries) {

        this.processEntry = processEntry;
        this.processDir = dir;
        this.entries = entries;

    }


    /**
     * Get the entries
     *
     * @return the list of entries
     */
    public List<Entry> getEntries() {
        return entries;
    }

    public Entry getProcessEntry() {
        return processEntry;
    }

    public File getProcessDir() {
        return processDir;
    }


}
