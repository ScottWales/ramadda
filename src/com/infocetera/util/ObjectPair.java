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

/**
 * (C) 1999-2002  WTS Systems, L.L.C.
 *   All rights reserved
 */



package com.infocetera.util;


/**
 * Class ObjectPair _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class ObjectPair {

    /** _more_ */
    public Object o1;

    /** _more_ */
    public Object o2;


    /**
     * _more_
     *
     * @param o1 _more_
     * @param o2 _more_
     */
    public ObjectPair(Object o1, Object o2) {
        this.o1 = o1;
        this.o2 = o2;
    }

    /**
     *  For now just show the first object
     *
     * @return _more_
     */
    public String toString() {
        return o1.toString();
    }

    /**
     * _more_
     *
     * @param o _more_
     *
     * @return _more_
     */
    public boolean equals(Object o) {
        if ( !(o instanceof ObjectPair)) {
            return false;
        }
        ObjectPair other = (ObjectPair) o;

        return o1.equals(other.o1) && o2.equals(other.o2);
    }


}
