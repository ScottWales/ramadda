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
import org.ramadda.repository.Repository;

import java.util.ArrayList;
import java.util.List;



/**
 */

public class CollectionOperand extends DataProcessInput {

    /** the collection */
    private Entry collection;

    /**
     * Create a CollectionOperand
     *
     * @param collection  the collection
     * @param granules    the entries
     */
    public CollectionOperand(Entry collection, List<Entry> granules) {
        super(granules);
        this.collection = collection;
    }

    /**
     * Get the Collection Entry
     *
     * @return  the collection
     */
    public Entry getCollectionEntry() {
        return collection;
    }

    /**
     * Get the collection granules (entries)
     *
     * @return  the entries
     */
    public List<Entry> getGranules() {
        return getEntries();
    }
}