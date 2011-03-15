/*
 * Copyright 2010- ramadda.org
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
 * 
 */

package org.ramadda.util;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


/**
 */
public class MapList<T,U> {

    private Hashtable<T,U> map = new Hashtable<T,U>();
    private List<T> keys = new ArrayList<T>();


    public List<T> getKeys() {
        return keys;
    }

    public U get(T key) {
        return map.get(key);
    }

    public void put(T key, U value) {
        if(!map.contains(key)) {
            keys.add(key);
        }
        map.put(key,value);
    }

}
