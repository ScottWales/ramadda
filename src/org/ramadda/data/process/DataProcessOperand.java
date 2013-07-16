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

import java.util.List;


/**
 * Class to hold a set of entries for a DataProcessInput or Output
 */
public class DataProcessOperand {

    /** The list of entries for this operand */
    private List<Entry> entries;

    /** the description */
    private String description;

    /**
     * Create an operand from the entry
     * @param entry the entry
     */
    public DataProcessOperand(Entry entry) {
    	this(Misc.newList(entry));
    }
    
    public DataProcessOperand(String description, Entry entry) {
    	this(description, Misc.newList(entry));
    }
    
    /**
     * Create an operand with a description and list of entries
     *
     * @param description  the description
     * @param entries      the entries
     */
    public DataProcessOperand(List<Entry> entries) {
    	this("", entries);
    }
    
    /**
     * Create an operand with a description and list of entries
     *
     * @param description  the description
     * @param entries      the entries
     */
    public DataProcessOperand(String description, List<Entry> entries) {
        this.description = description;
        this.entries     = entries;
    }

    /**
     * Get the entries
     *
     * @return  the entries
     */
    public List<Entry> getEntries() {
        return entries;
    }

    /**
     * Set the entries
     *
     * @param newEntries  the new entries
     */
    public void setEntries(List<Entry> newEntries) {
        entries = newEntries;
    }

    /**
     * Get the description of this operand
     *
     * @return  the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description of the operand
     *
     * @param description  the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

}
