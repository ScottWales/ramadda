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

package org.ramadda.repository.type;


import org.ramadda.repository.*;

import org.ramadda.repository.metadata.*;

import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;

import ucar.unidata.sql.Clause;


import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.DateUtil;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;

import java.io.File;

import java.sql.PreparedStatement;

import java.sql.ResultSet;
import java.sql.Statement;




import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;


/**
 * Class TypeHandler _more_
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class LocalFileTypeHandler extends GenericTypeHandler {


    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public LocalFileTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }

    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public boolean canBeCreatedBy(Request request) {
        return request.getUser().getAdmin();
    }

    /**
     * _more_
     *
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getIconUrl(Request request, Entry entry) throws Exception {
        if (entry.isGroup()) {
            return iconUrl(ICON_SYNTH_FILE);
        }

        return super.getIconUrl(request, entry);
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isSynthType() {
        return true;
    }

    /**
     * _more_
     *
     * @param id _more_
     * @param baseFile _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public File getFileFromId(String id, File baseFile) throws Exception {
        if ((id == null) || (id.length() == 0)) {
            return baseFile;
        }
        String subPath = new String(RepositoryUtil.decodeBase64(id));
        File   file    = new File(IOUtil.joinDir(baseFile, subPath));
        if ( !IOUtil.isADescendent(baseFile, file)) {
            throw new IllegalArgumentException("Bad file path:" + subPath);
        }

        getStorageManager().checkLocalFile(file);

        return file;
    }

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

    /**
     * _more_
     *
     * @param values _more_
     * @param idx _more_
     *
     * @return _more_
     */
    public List<String> get(Object[] values, int idx) {
        if (values[idx] == null) {
            return new ArrayList<String>();
        }

        return (List<String>) StringUtil.split(values[idx], "\n", true, true);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param mainEntry _more_
     * @param parentEntry _more_
     * @param synthId _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<String> getSynthIds(Request request, Entry mainEntry,
                                    Entry parentEntry, String synthId)
            throws Exception {

        //        System.err.println("getSynthIds " + mainEntry);
        List<String> ids    = new ArrayList<String>();
        Object[]     values = mainEntry.getValues();
        if (values == null) {
            return ids;
        }
        int  max     = request.get(ARG_MAX, VIEW_MAX_ROWS);
        int  skip    = request.get(ARG_SKIP, 0);

        long t1      = System.currentTimeMillis();
        File rootDir = new File((String) values[0]);
        if ( !rootDir.exists()) {
            throw new RepositoryUtil.MissingEntryException(
                "Could not find entry: " + rootDir);
        }
        String rootDirPath = rootDir.toString();
        getStorageManager().checkLocalFile(rootDir);

        File   childPath = getFileFromId(synthId, rootDir);
        File[] files     = childPath.listFiles();
        //        files = IOUtil.sortFilesOnName(files);

        Metadata sortMetadata = null;
        if (mainEntry != null) {
            try {
                List<Metadata> metadataList =
                    getMetadataManager().findMetadata(mainEntry,
                        ContentMetadataHandler.TYPE_SORT, true);
                if ((metadataList != null) && (metadataList.size() > 0)) {
                    sortMetadata = metadataList.get(0);
                }
            } catch (Exception ignore) {}
        }



        boolean descending = !request.get(ARG_ASCENDING, false);
        String  by         = request.getString(ARG_ORDERBY, "fromdate");
        if (sortMetadata != null) {
            if ( !request.exists(ARG_ASCENDING)) {
                if (Misc.equals(sortMetadata.getAttr2(), "true")) {
                    descending = false;
                } else {
                    descending = true;
                }
            }
            if ( !request.exists(ARG_ORDERBY)) {
                by = sortMetadata.getAttr1();
            }
        }



        if (by.equals("name")) {
            files = IOUtil.sortFilesOnName(files, descending);
        } else if (by.equals("mixed")) {
            List<File> filesByDate = new ArrayList<File>();
            List<File> filesByName = new ArrayList<File>();
            for (File f : files) {
                String name = f.getName();
                if (name.matches(
                        ".*(\\d\\d\\d\\d\\d\\d|\\d\\d\\d\\d_\\d\\d).*")) {
                    filesByDate.add(f);
                } else {
                    filesByName.add(f);
                }
            }
            //            System.err.println ("by date:" + filesByDate);
            //            System.err.println ("by name:" + filesByName);
            File[] byDate = IOUtil.sortFilesOnAge(toArray(filesByDate),
                                descending);
            File[] byName = IOUtil.sortFilesOnAge(toArray(filesByName),
                                descending);
            int cnt = 0;
            for (int i = 0; i < byName.length; i++) {
                files[cnt++] = byName[i];
            }
            for (int i = 0; i < byDate.length; i++) {
                files[cnt++] = byDate[i];
            }
        } else {
            files = IOUtil.sortFilesOnAge(files, descending);
        }


        List<String> includes = get(values, COL_INCLUDES);
        List<String> excludes = get(values, COL_EXCLUDES);
        long         age      = (long) (1000
                           * (((Double) values[COL_AGE]).doubleValue() * 60));
        long       now      = System.currentTimeMillis();
        int        start    = skip;
        List<File> fileList = new ArrayList<File>();
        List<File> dirList  = new ArrayList<File>();
        for (int i = start; i < files.length; i++) {
            File childFile = files[i];
            if (childFile.isDirectory()) {
                dirList.add(childFile);
            } else {
                fileList.add(childFile);
            }
        }
        dirList.addAll(fileList);

        int cnt = 0;
        for (File childFile : dirList) {
            if (childFile.isHidden()) {
                continue;
            }
            if ((age != 0) && (now - childFile.lastModified()) < age) {
                continue;
            }
            if ( !match(childFile, includes, true)) {
                continue;
            }
            if (match(childFile, excludes, false)) {
                continue;
            }
            ids.add(getSynthId(mainEntry, rootDirPath, childFile));
            cnt++;
            if (cnt >= max) {
                break;
            }
        }
        long t2 = System.currentTimeMillis();

        //        System.err.println ("Time:" + (t2-t1) + " ids:" + ids.size());
        return ids;


    }

    /**
     * _more_
     *
     * @param files _more_
     *
     * @return _more_
     */
    private File[] toArray(List<File> files) {
        File[] a = new File[files.size()];
        for (int i = 0; i < a.length; i++) {
            a[i] = files.get(i);
        }

        return a;
    }


    /**
     * _more_
     *
     * @param pattern _more_
     *
     * @return _more_
     */
    private String getRegexp(String pattern) {
        if ( !pattern.startsWith("regexp:")) {
            pattern = StringUtil.wildcardToRegexp(pattern);
        } else {
            pattern = pattern.substring("regexp:".length());
        }

        return pattern;
    }

    /**
     * _more_
     *
     * @param file _more_
     * @param patterns _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    private boolean match(File file, List<String> patterns, boolean dflt) {
        String  value      = file.toString();
        boolean hadPattern = false;
        for (String pattern : patterns) {
            pattern = pattern.trim();
            if (pattern.startsWith("dir:")) {
                if (file.isDirectory()) {
                    hadPattern = true;
                    pattern    =
                        getRegexp(pattern.substring("dir:".length()).trim());
                    if (StringUtil.stringMatch(value, pattern, true, false)) {
                        return true;
                    }
                }
            } else {
                if ( !file.isDirectory()) {
                    hadPattern = true;
                    if (StringUtil.stringMatch(value, getRegexp(pattern),
                            true, false)) {
                        return true;
                    }
                }
            }
        }
        if (hadPattern) {
            return false;
        }

        return dflt;
    }

    /**
     * _more_
     *
     * @param parentEntry _more_
     * @param rootDirPath _more_
     * @param childFile _more_
     *
     * @return _more_
     */
    public String getSynthId(Entry parentEntry, String rootDirPath,
                             File childFile) {
        String subId = childFile.toString().substring(rootDirPath.length());
        subId = RepositoryUtil.encodeBase64(subId.getBytes()).replace("\n",
                                            "");

        return Repository.ID_PREFIX_SYNTH + parentEntry.getId() + ":" + subId;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param parentEntry _more_
     * @param id _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry makeSynthEntry(Request request, Entry parentEntry, String id)
            throws Exception {
        List<Metadata> metadataList =
            getMetadataManager().getMetadata(parentEntry);
        Object[] values = parentEntry.getValues();
        if (values == null) {
            return null;
        }
        File rootDir    = new File((String) values[0]);
        File targetFile = getFileFromId(id, rootDir);
        if ( !rootDir.exists() || !targetFile.exists()) {
            return null;
        }
        long t1 = System.currentTimeMillis();
        //TODO: Check the time since last change here
        List<String> includes = get(values, COL_INCLUDES);
        List<String> excludes = get(values, COL_EXCLUDES);
        if ( !match(targetFile, includes, true)) {
            throw new IllegalArgumentException("File cannot be accessed");
        }
        if (match(targetFile, excludes, false)) {
            throw new IllegalArgumentException("File cannot be accessed");
        }

        String synthId = Repository.ID_PREFIX_SYNTH + parentEntry.getId()
                         + ":" + id;
        TypeHandler handler = (targetFile.isDirectory()
                               ? getRepository().getTypeHandler(
                                   TypeHandler.TYPE_GROUP)
                               : getRepository().getTypeHandler(
                                   TypeHandler.TYPE_FILE));
        Entry entry = (targetFile.isDirectory()
                       ? (Entry) new Entry(synthId, handler, true)
                       : new Entry(synthId, handler));

        if (targetFile.isDirectory()) {
            entry.setIcon(ICON_SYNTH_FILE);
        }
        Entry templateEntry = getEntryManager().getTemplateEntry(targetFile);
        String       name   = null;
        List<String> names  = get(values, COL_NAMES);
        for (String pair : names) {
            boolean doPath = false;
            if (pair.startsWith("path:")) {
                pair   = pair.substring("path:".length());
                doPath = true;
            } else if (pair.startsWith("name:")) {
                pair   = pair.substring("name:".length());
                doPath = false;
            }
            if (name == null) {
                if (doPath) {
                    name = targetFile.toString();
                } else {
                    name = IOUtil.getFileTail(targetFile.toString());
                }
            }
            String[] tuple = StringUtil.split(pair, ":", 2);
            if ((tuple == null) || (tuple.length != 2)) {
                continue;
            }
            name = name.replaceAll(".*" + tuple[0] + ".*", tuple[1]);
        }
        if (name == null) {
            name = IOUtil.getFileTail(targetFile.toString());
        }
        entry.setIsLocalFile(true);
        Entry parent;
        if (targetFile.getParentFile().equals(rootDir)) {
            parent = (Entry) parentEntry;
        } else {
            String parentId = getSynthId(parentEntry, rootDir.toString(),
                                         targetFile.getParentFile());
            parent = (Entry) getEntryManager().getEntry(request, parentId,
                    false, false);
        }

        entry.initEntry(name, "", parent,
                        getUserManager().getLocalFileUser(),
                        new Resource(targetFile, (targetFile.isDirectory()
                ? Resource.TYPE_LOCAL_DIRECTORY
                : Resource.TYPE_LOCAL_FILE)), "", targetFile.lastModified(),
                targetFile.lastModified(), targetFile.lastModified(),
                targetFile.lastModified(), null);

        if (templateEntry != null) {
            entry.initWith(templateEntry);
        } else {
            //Tack on the metadata
            entry.setMetadata(metadataList);
        }
        long t2 = System.currentTimeMillis();

        //        System.err.println ("makeSynthEntry: " + entry + " " +(t2-t1));
        return entry;
    }


    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    public Entry createEntry(String id) {
        //Make the top level entyr act like a group
        return new Entry(id, this, true);
    }

}
