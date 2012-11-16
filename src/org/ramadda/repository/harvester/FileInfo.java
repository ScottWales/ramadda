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

package org.ramadda.repository.harvester;


import org.ramadda.util.HtmlUtils;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.io.File;
import java.io.FileInputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * Class FileInfo holds information about a file or directory
 *
 *
 */
public class FileInfo {

    /** tracks whether we have initialized ourselves */
    private boolean hasInitialized = false;

    /** The file */
    private File file;

    /** _more_          */
    private File rootDir;

    /** _more_ */
    private long time;

    /** _more_ */
    private long size = 0;

    /** _more_ */
    private int fileCount = 0;

    /** _more_ */
    private boolean isDir;

    /** _more_ */
    private List addedFiles;

    /**
     * ctor
     *
     * @param f the file
     */
    public FileInfo(File f) {
        this(f, f, f.isDirectory());

    }

    /**
     * ctor
     *
     * @param f the file
     * @param rootDir _more_
     * @param isDir is file a directory
     */
    public FileInfo(File f, File rootDir, boolean isDir) {
        this.isDir   = isDir;
        this.rootDir = rootDir;
        file         = f;
    }

    /**
     * _more_
     */
    private void doInit() {
        time = file.lastModified();
        if ( !isDir) {
            size = file.length();
        } else {
            File[] files = file.listFiles();
            if (files != null) {
                fileCount = files.length;
                for (File child : files) {
                    size += child.length();
                }
            }
        }
        hasInitialized = true;
    }


    /**
     * override hashcode
     *
     * @return hashcode
     */
    public int hashCode() {
        return file.hashCode();

    }

    /**
     * _more_
     *
     * @param obj _more_
     *
     * @return _more_
     */
    public boolean equals(Object obj) {
        if ( !(obj instanceof FileInfo)) {
            return false;
        }
        FileInfo that = (FileInfo) obj;

        return this.file.equals(that.file);
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public boolean hasChanged() {
        if ( !hasInitialized) {
            doInit();

            return true;
        }
        long newTime      = file.lastModified();
        long newSize      = 0;
        int  newFileCount = 0;

        if (isDir) {
            File[] files = this.file.listFiles();
            if (files != null) {
                newFileCount = files.length;
                for (File child : files) {
                    newSize += child.length();
                }
            }
        } else {
            newSize = file.length();
        }

        boolean changed = (newTime != time) || (newSize != size)
                          || (newFileCount != fileCount);
        time      = newTime;
        size      = newSize;
        fileCount = newFileCount;

        return changed;
    }

    /**
     * _more_
     */
    public void reset() {
        time      = -1;
        size      = -1;
        fileCount = 0;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public File getFile() {
        return file;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean exists() {
        return file.exists();
    }

    /**
     * _more_
     */
    public void clearAddedFiles() {
        addedFiles = null;
    }

    /**
     * _more_
     *
     * @param f _more_
     */
    public void addFile(Object f) {
        if (addedFiles == null) {
            addedFiles = new ArrayList();
        }
        addedFiles.add(f);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        String s   = file.toString();
        List   tmp = addedFiles;
        if ((tmp != null) && (tmp.size() > 0)) {
            String fileBlock = HtmlUtils.insetDiv("Added files:<br>"
                                   + StringUtil.join("<br>", tmp), 0, 10, 0,
                                       0);

            return HtmlUtils.makeShowHideBlock(s, fileBlock, false);
        }

        return s;
    }

    /**
     * _more_
     *
     * @param rootDir _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public static List<FileInfo> collectDirs(final File rootDir, final Harvester harvester)
            throws Exception {
        final List<FileInfo> dirs       = new ArrayList();
        IOUtil.FileViewer    fileViewer = new IOUtil.FileViewer() {
            public int viewFile(File f) throws Exception {
                if (f.isDirectory()) {
                    if (f.getName().startsWith(".")) {
                        return DO_DONTRECURSE;
                    }
                    if(!okToRecurse(f, harvester)) {
                        return DO_DONTRECURSE;
                    }
                    dirs.add(new FileInfo(f, rootDir, true));
                }

                return DO_CONTINUE;
            }
        };
        IOUtil.walkDirectory(rootDir, fileViewer);

        return dirs;
    }

    public static boolean okToRecurse(File dir, Harvester harvester) throws Exception {
        //check for a ramadda.properties file. 
        File propFile = new File(IOUtil.joinDir(dir,"ramadda.properties"));
        if(propFile.exists()) {
            harvester.logHarvesterInfo("Checking properties file:" + propFile);
            Properties properties = new Properties();
            FileInputStream fis = new FileInputStream(propFile);
            properties.load(fis);
            IOUtil.close(fis);
            String ok = (String) properties.get("harvester.ok");
            if(ok!=null && ok.trim().equals("false")) {
                harvester.logHarvesterInfo("Skipping directory:" + dir);
                return false;
            }
            harvester.logHarvesterInfo("Not Skipping directory:" + ok);
        }
        return true;
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
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
        File rootDir = new File(".");
        if ( !rootDir.exists()) {
            rootDir = new File("");
        }
        if (args.length > 0) {
            rootDir = new File(args[0]);
        }
        final List<FileInfo> dirs       = new ArrayList();
        final int[]          cnt        = { 0 };
        IOUtil.FileViewer    fileViewer = new IOUtil.FileViewer() {
            public int viewFile(File f) throws Exception {
                cnt[0]++;
                if (cnt[0] % 1000 == 0) {
                    System.err.print(".");
                }
                if (f.isDirectory()) {
                    dirs.add(new FileInfo(f, f, true));
                    //    if(dirs.size()%1000==0) System.err.print(".");
                }

                return DO_CONTINUE;
            }
        };

        long tt1 = System.currentTimeMillis();
        IOUtil.walkDirectory(rootDir, fileViewer);
        long tt2 = System.currentTimeMillis();
        //        System.err.println("found:" + dirs.size() + " in:" + (tt2 - tt1)
        //                           + " looked at:" + cnt[0]);

        while (true) {
            long t1 = System.currentTimeMillis();
            for (FileInfo fileInfo : dirs) {
                long oldTime = fileInfo.time;
                if (fileInfo.hasChanged()) {
                    //                    System.err.println("Changed:" + fileInfo);
                    File[] files = fileInfo.file.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        if (files[i].lastModified() > oldTime) {
                            //                            System.err.println("    " + files[i].getName());
                        }

                    }


                }
            }
            long t2 = System.currentTimeMillis();
            //            System.err.println ("Time:" + (t2-t1));
            Misc.sleep(5000);
        }
    }

}
