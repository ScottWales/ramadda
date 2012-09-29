/*
* Copyright 2008-2011 Jeff McWhirter/ramadda.org
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

package org.ramadda.data.record;


import java.io.*;


/**
 * This class  is  a holder for various IO capabilities. It needs a core InputStream or  OutputStream.
 * This holds wrappers around those streams - e.g., DataInputStream, DataOutputStream, PrintWriter, etc.
 * This is used in conjunction with the record file reading and writing.
 *
 * @author  Jeff McWhirter
 */
public class RecordIO {

    /** the input stream */
    private InputStream inputStream;

    /** the output stream */
    private OutputStream outputStream;

    /** reader */
    private BufferedReader bufferedReader;

    /** data input stream */
    private DataInputStream dataInputStream;

    /** data output stream */
    private DataOutputStream dataOutputStream;

    /** print writer */
    private PrintWriter printWriter;

    /**
     * ctor
     *
     * @param reader initialize with a buffered reader
     */
    public RecordIO(BufferedReader reader) {
        bufferedReader = reader;
    }

    /**
     * ctor
     *
     * @param inputStream input stream
     */
    public RecordIO(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * ctor
     *
     * @param outputStream output stream
     */
    public RecordIO(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * Copy ctor
     *
     * @param that what to copy
     */
    public void reset(RecordIO that) {
        close();
        this.inputStream      = that.inputStream;
        this.outputStream     = that.outputStream;
        this.bufferedReader   = that.bufferedReader;
        this.dataInputStream  = that.dataInputStream;
        this.dataOutputStream = that.dataOutputStream;
        this.printWriter      = that.printWriter;
    }

    /**
     * Close all of the  streams
     *
     */
    public void close() {
        try {
            if (dataInputStream != null) {
                dataInputStream.close();
            }
        } catch (Exception ignore) {}

        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception ignore) {}


        try {
            if (printWriter != null) {
                printWriter.close();
            }
        } catch (Exception ignore) {}

        try {
            if (dataOutputStream != null) {
                dataOutputStream.close();
            }
        } catch (Exception ignore) {}

        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (Exception ignore) {}
    }

    /**
     * Return the input stream
     *
     * @return the input stream
     *
     * @throws IOException On badness
     */
    public InputStream getInputStream() throws IOException {
        return inputStream;
    }

    /**
     * Return the output stream
     *
     * @return the output stream
     *
     * @throws IOException On badness
     */
    public OutputStream getOutputStream() throws IOException {
        return outputStream;
    }


    /**
     * Create if needed and return the DataInputStream
     *
     * @return The DataInputStream
     *
     * @throws IOException On badness
     */
    public DataInputStream getDataInputStream() throws IOException {
        if (dataInputStream == null) {
            dataInputStream = new DataInputStream(getInputStream());
        }
        return dataInputStream;
    }


    /**
     * Create if needed and return the DataOutputStream
     *
     * @return The DataOutputStream
     *
     * @throws IOException On badness
     */
    public DataOutputStream getDataOutputStream() throws IOException {
        if (dataOutputStream == null) {
            dataOutputStream = new DataOutputStream(outputStream);
        }
        return dataOutputStream;
    }



    /**
     * Create if needed and return the BufferedReader
     *
     * @return The BufferedReader
     *
     * @throws IOException On badness
     */
    public BufferedReader getBufferedReader() throws IOException {
        if (bufferedReader == null) {
            bufferedReader =
                new BufferedReader(new InputStreamReader(getInputStream()));
        }
        return bufferedReader;
    }

    /**
     * Read a line from the buffered reader
     *
     * @return The read line
     *
     * @throws IOException On badness
     */
    public String readLine() throws IOException {
        return getBufferedReader().readLine();
    }


    /**
     * Create if needed and return the PrintWriter
     *
     * @return The PrintWriter
     *
     * @throws IOException On badness
     */
    public PrintWriter getPrintWriter() throws IOException {
        if (printWriter == null) {
            printWriter = new PrintWriter(getOutputStream());
        }
        return printWriter;
    }

}
