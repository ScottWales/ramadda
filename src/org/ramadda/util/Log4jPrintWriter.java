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


import org.apache.log4j.Logger;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import java.io.*;


/**
 * Orignally from JD Evora
 */


public class Log4jPrintWriter extends PrintWriter {

    /** _more_ */
    private Logger log;

    /** _more_ */
    StringBuffer text = new StringBuffer("");

    /**
     * _more_
     *
     * @param log _more_
     */
    public Log4jPrintWriter(Logger log) {
        super(System.err);  // PrintWriter doesn't have default constructor.
        this.log = log;
    }

    /**
     * _more_
     */
    public void log() {
        if (log != null) {
            log.info(text.toString());
        }
        text.setLength(0);
    }


    // overrides all the print and println methods for 'print' it to the constructor's Category

    /**
     * _more_
     */
    public void close() {
        flush();
    }

    /**
     * _more_
     */
    public void flush() {
        if ( !text.toString().equals("")) {
            log();
        }
    }



    /**
     * _more_
     *
     * @param b _more_
     */
    public void print(boolean b) {
        text.append(b);
    }

    /**
     * _more_
     *
     * @param c _more_
     */
    public void print(char c) {
        text.append(c);
    }

    /**
     * _more_
     *
     * @param s _more_
     */
    public void print(char[] s) {
        text.append(s);
    }

    /**
     * _more_
     *
     * @param d _more_
     */
    public void print(double d) {
        text.append(d);
    }

    /**
     * _more_
     *
     * @param f _more_
     */
    public void print(float f) {
        text.append(f);
    }

    /**
     * _more_
     *
     * @param i _more_
     */
    public void print(int i) {
        text.append(i);
    }

    /**
     * _more_
     *
     * @param l _more_
     */
    public void print(long l) {
        text.append(l);
    }

    /**
     * _more_
     *
     * @param obj _more_
     */
    public void print(Object obj) {
        text.append(obj);
    }

    /**
     * _more_
     *
     * @param s _more_
     */
    public void print(String s) {
        text.append(s);
    }

    /**
     * _more_
     */
    public void println() {
        if ( !text.toString().equals("")) {
            log();
        }
    }

    /**
     * _more_
     *
     * @param x _more_
     */
    public void println(boolean x) {
        text.append(x);
        log();
    }

    /**
     * _more_
     *
     * @param x _more_
     */
    public void println(char x) {
        text.append(x);
        log();
    }

    /**
     * _more_
     *
     * @param x _more_
     */
    public void println(char[] x) {
        text.append(x);
        log();
    }

    /**
     * _more_
     *
     * @param x _more_
     */
    public void println(double x) {
        text.append(x);
        log();
    }

    /**
     * _more_
     *
     * @param x _more_
     */
    public void println(float x) {
        text.append(x);
        log();
    }

    /**
     * _more_
     *
     * @param x _more_
     */
    public void println(int x) {
        text.append(x);
        log();
    }

    /**
     * _more_
     *
     * @param x _more_
     */
    public void println(long x) {
        text.append(x);
        log();
    }

    /**
     * _more_
     *
     * @param x _more_
     */
    public void println(Object x) {
        text.append(x);
        log();
    }

    /**
     * _more_
     *
     * @param x _more_
     */
    public void println(String x) {
        text.append(x);
        log();
    }
}
