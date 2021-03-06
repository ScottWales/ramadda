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





/**
 * !!!!!!!!NOTE!!!!!!!
 * This file is copied from the UNAVCO SVN tree (I know I shouldn't do this
 * but I couldn't deal with yet another library from somewhere else).
 * SO: DO NOT EDIT THIS FILE HERE OR IF YOU DO MAKE SURE ITS CHECKED INTO
 * THE UNAVCO TREE
 */
public class GridUtils {



    /**
     * fill array with value
     *
     * @param a array
     * @param value value
     */
    public static void fill(float[][] a, float value) {
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                a[i][j] = value;
            }
        }
    }

    /**
     * fill array with value
     *
     * @param a array
     * @param value value
     */
    public static void fill(double[][] a, double value) {
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                a[i][j] = value;
            }
        }
    }

    /**
     * fill array with value
     *
     * @param a array
     * @param value value
     */
    public static void fill(int[][] a, int value) {
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                a[i][j] = value;
            }
        }
    }



    /**
     * _more_
     *
     * @param a _more_
     *
     * @return _more_
     */
    public static double[][] cloneArray(double[][] a) {
        double[][] b = new double[a.length][a[0].length];
        for (int i = 0; i < a.length; i++) {
            System.arraycopy(a[i], 0, b[i], 0, a[0].length);
        }

        return b;
    }



}
