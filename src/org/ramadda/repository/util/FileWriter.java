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

package org.ramadda.repository.util;

import ucar.unidata.util.IOUtil;
import java.util.zip.*;
import java.io.*;



/**
 * Holds information for generating entries in entries.xml
 */
public class FileWriter {

    private ZipOutputStream zos;

    private File directory;

    public FileWriter(File directory) {
        this.directory = directory;
    }

    public FileWriter(ZipOutputStream zos) {
        this.zos = zos;
    }

    public void close() throws Exception {
        if(zos!=null) {
            IOUtil.close(zos);
        }
    }

    public void setCompressionOn() {
        if(zos!=null) {
            zos.setLevel(0);
        }
    }


    public void writeFile(String name, InputStream fis) throws Exception {
        if(zos!=null) {
            ZipEntry zipEntry = new ZipEntry(name);
            zos.putNextEntry(zipEntry);
            try {
                IOUtil.writeTo(fis, zos);
                zos.closeEntry();
            } finally {
                IOUtil.close(fis);
                zos.closeEntry();
            }
        } else {
            FileOutputStream fos = new FileOutputStream(IOUtil.joinDir(directory, name));
            IOUtil.writeTo(fis, fos);
            IOUtil.close(fos);
        }
    }


    public void writeFile(String name, byte[] bytes) throws Exception {
        if(zos!=null) {
            zos.putNextEntry(new ZipEntry(name));
            zos.write(bytes, 0, bytes.length);
            zos.closeEntry();
        } else {
            writeFile(name, new ByteArrayInputStream(bytes));
            //TODO
        }
    }




}
