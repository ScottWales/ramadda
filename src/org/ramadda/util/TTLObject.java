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



import java.util.Date;
import java.util.Hashtable;



/**
 * Keep the given object in memory for only a time threshold
 *
 *
 *
 * @param <VALUE>
 */
public class TTLObject<VALUE> {

    /** holds the object */
    private TTLCache<String, VALUE> cache;


    /**
     * ctor
     *
     * @param timeThresholdInMilliseconds time to live
     */
    public TTLObject(long timeThresholdInMilliseconds) {
        this(null, timeThresholdInMilliseconds);
    }

    /**
     * default ctor. 1 hour in cache. No time reset. No size limit
     *
     * @param object object to store
     */
    public TTLObject(VALUE object) {
        this(object, 1000 * 60 * 60);
    }

    /**
     * ctor.
     *
     * @param object object to store
     * @param timeThresholdInMilliseconds time in cache
     */
    public TTLObject(VALUE object, long timeThresholdInMilliseconds) {
        cache = new TTLCache<String, VALUE>(timeThresholdInMilliseconds);
        if (object != null) {
            put(object);
        }
    }

    /**
     * store a new object
     *
     * @param value new object_
     */
    public void put(VALUE value) {
        cache.put("", value);
    }

    /**
     * _more_
     *
     * @param t _more_
     */
    public void setTimeThreshold(long t) {
        cache.setTimeThreshold(t);
    }

    /**
     * get the object or null if its expired
     *
     * @return object
     */
    public VALUE get() {
        return cache.get("");
    }

}
