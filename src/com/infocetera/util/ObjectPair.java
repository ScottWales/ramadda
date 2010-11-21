/*
 *
 * 
 * 
 * 
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
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

