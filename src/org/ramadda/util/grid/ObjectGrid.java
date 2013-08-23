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

package org.ramadda.util.grid;


import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.IOUtil;

import ucar.unidata.util.StringUtil;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.*;

import java.io.File;

import java.util.List;



/**
 * !!!!!!!!NOTE!!!!!!!
 * This file is copied from the UNAVCO SVN tree (I know I shouldn't do this
 * but I couldn't deal with yet another library from somewhere else).
 * SO: DO NOT EDIT THIS FILE HERE OR IF YOU DO MAKE SURE ITS CHECKED INTO
 * THE UNAVCO TREE
 *
 * @param <T>
 */
public class ObjectGrid<T> extends Grid {

    /** An extra 2d array of counts */
    private T[][] objectGrid;


    /**
     * ctor
     *
     * @param width number of columns
     * @param height number of rows
     * @param north northern bounds
     * @param west west bounds
     * @param south southern bounds
     * @param east east bounds
     */
    public ObjectGrid(int width, int height, double north, double west,
                      double south, double east) {
        super(width, height, north, west, south, east);
    }



    /**
     * create if needed and return the grid
     *
     * @return the grid
     */
    public T[][] getGrid() {
        if (objectGrid == null) {
            objectGrid = (T[][]) new Object[getHeight()][getWidth()];
        }

        return objectGrid;
    }


    /**
     * _more_
     *
     * @param lat _more_
     * @param lon _more_
     *
     * @return _more_
     */
    public T getValue(double lat, double lon) {
        int yIndex = getLatitudeIndex(lat);
        int xIndex = getLongitudeIndex(lon);

        return getGrid()[yIndex][xIndex];
    }

    /**
     * _more_
     *
     * @param yIndex _more_
     * @param xIndex _more_
     *
     * @return _more_
     */
    public T getValueFromIndex(int yIndex, int xIndex) {
        return getGrid()[yIndex][xIndex];
    }


    /**
     * _more_
     *
     * @param lat _more_
     * @param lon _more_
     * @param value _more_
     */
    public void setValue(double lat, double lon, T value) {
        setValueByIndex(getLatitudeIndex(lat), getLongitudeIndex(lon), value);
    }

    /**
     * _more_
     *
     * @param yIndex _more_
     * @param xIndex _more_
     * @param value _more_
     */
    public void setValueByIndex(int yIndex, int xIndex, T value) {
        getGrid()[yIndex][xIndex] = value;
    }



    /**
     * _more_
     *
     * @param args _more_
     */
    public static void main(String[] args) {}

}
