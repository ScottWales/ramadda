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

package org.ramadda.repository.type;


import org.ramadda.repository.*;

import ucar.unidata.util.StringUtil;

import java.io.File;




import java.util.ArrayList;
import java.util.List;


/**
 * Class description
 *
 *
 * @version        $version$, Fri, Aug 23, '13
 * @author         Enter your name here...
 */
public class LocalFileInfo {

    /** _more_ */
    public static final int COL_PATH = 0;

    /** _more_ */
    public static final int COL_AGE = 1;

    /** _more_ */
    public static final int COL_INCLUDES = 2;

    /** _more_ */
    public static final int COL_EXCLUDES = 3;

    /** _more_ */
    public static final int COL_NAMES = 4;



    /** _more_ */
    private File rootDir;

    /** _more_ */
    private List<String> includes;

    /** _more_ */
    private List<String> excludes;

    /** _more_ */
    private List<String> names;

    /** _more_ */
    private double ageLimit = -1;

    /**
     * _more_
     *
     * @param repository _more_
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    public LocalFileInfo(Repository repository, Entry entry)
            throws Exception {
        Object[] values = entry.getValues();
        if (values == null) {
            return;
        }
        rootDir  = new File((String) values[0]);
        names    = get(values, COL_NAMES);
        includes = get(values, COL_INCLUDES);
        excludes = get(values, COL_EXCLUDES);
        ageLimit = ((Double) values[COL_AGE]).doubleValue();
        checkMe(repository);
    }

    /**
     * _more_
     *
     * @param repository _more_
     * @param rootDir _more_
     *
     * @throws Exception _more_
     */
    public LocalFileInfo(Repository repository, File rootDir)
            throws Exception {
        this(repository, rootDir, null, null, null, -1);

    }

    /**
     * _more_
     *
     * @param repository _more_
     * @param rootDir _more_
     * @param includes _more_
     * @param excludes _more_
     * @param names _more_
     * @param ageLimit _more_
     *
     * @throws Exception _more_
     */
    public LocalFileInfo(Repository repository, File rootDir,
                         List<String> includes, List<String> excludes,
                         List<String> names, double ageLimit)
            throws Exception {
        this.rootDir  = rootDir;
        this.includes = (includes != null)
                        ? includes
                        : new ArrayList<String>();
        this.excludes = (excludes != null)
                        ? excludes
                        : new ArrayList<String>();
        this.names    = (names != null)
                        ? names
                        : new ArrayList<String>();
        this.ageLimit = ageLimit;
        checkMe(repository);
    }


    /**
     * _more_
     *
     * @param repository _more_
     *
     * @throws Exception _more_
     */
    private void checkMe(Repository repository) throws Exception {
        if ( !rootDir.exists()) {
            throw new RepositoryUtil.MissingEntryException(
                "Could not find entry: " + rootDir);
        }
        repository.getStorageManager().checkLocalFile(rootDir);
    }

    /**
     * _more_
     *
     * @param values _more_
     * @param idx _more_
     *
     * @return _more_
     */
    public static List<String> get(Object[] values, int idx) {
        if (values[idx] == null) {
            return new ArrayList<String>();
        }

        return (List<String>) StringUtil.split(values[idx], "\n", true, true);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isDefined() {
        return rootDir != null;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List<String> getNames() {
        return names;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List<String> getIncludes() {
        return includes;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List<String> getExcludes() {
        return excludes;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public File getRootDir() {
        return rootDir;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public double getAgeLimit() {
        return ageLimit;
    }



}
