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

package org.ramadda.util;


/**
 * A utility for running a process
 */
public class ProcessRunner implements Runnable {

    /** process killed return code */
    public static final int PROCESS_KILLED = -143;

    /** the process */
    Process process;

    /** a flag for whether the process is finished */
    private boolean finished =false;

    /** the process return code */
    private int returnCode= 0;

    private long timeoutMillis = 0;

    /**
     * Create a new ProcessRunner
     *
     * @param process  the process
     */
    public ProcessRunner(Process process, long timeoutMillis) {
        this.process = process;
        this.timeoutMillis = timeoutMillis;
    }

    /**
     * Run this thread
     */
    public void run() {
        try {
            returnCode = process.waitFor();
        } catch (InterruptedException e) {
            // Ignore
        } finally {
            finished = true;
        }
        synchronized (this) {
            notifyAll();
            finished = true;
        }
    }

    /**
     * Wait for or kill the process
     *
     * @param proc  the process
     * @param numberOfMillis  the time to wait (-1 to wait forever)
     *
     * @return  the process return code
     */
    public int runProcess() {
        Thread        thread   = new Thread(this);
        thread.start();
        waitForOrKill();
        return this.returnCode;
    }


    /**
     * Wait for or kill the process
     *
     * @param millis  amount of time to wait before killing process
     *
     * @return the process exit code or -143 if process killed
     */
    private void waitForOrKill() {

        //If we don't have a time out then we just want to wait until we're done
        if(timeoutMillis<=0)  {
            while(!finished) {
                try {
                    wait(100);
                } catch (InterruptedException e) {}
            }
            return;
        }
        if (!finished) {
            try {
                wait(timeoutMillis);
            } catch (InterruptedException e) {
                // Ignore
            }
            if (!finished) {
                process.destroy();
                returnCode = PROCESS_KILLED;
            }
        }
    }
}


