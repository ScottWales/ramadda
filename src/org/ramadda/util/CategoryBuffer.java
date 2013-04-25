/*
 * Copyright 2008-2012 Jeff McWhirter/ramadda.org
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

package org.ramadda.util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;



/**
 * A collection of utilities for rss feeds xml.
 *
 * @author Jeff McWhirter
 */

public class CategoryBuffer {

    List<String> categories = new ArrayList<String>();
    Hashtable<String, StringBuffer> buffers = new Hashtable<String,StringBuffer>();

    public CategoryBuffer() {
    }

    public StringBuffer get(String category) {
        if(category == null) category = "";
        StringBuffer sb = buffers.get(category);
        if(sb == null) {
            sb = new StringBuffer();
            buffers.put(category, sb);
            categories.add(category);
        }
        return sb;
    }

    public void append(String category, Object object) {
        get(category).append(object);
    }

    public List<String> getCategories() {
        return categories;
    }


}


