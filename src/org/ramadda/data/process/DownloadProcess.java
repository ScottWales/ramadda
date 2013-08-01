/*
 * Copyright 2008-2013 Jeff McWhirter/ramadda.org
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

package org.ramadda.data.process;


import org.ramadda.repository.Request;



/**
 * A class for holding process information
 */
public  class DownloadProcess extends DataProcess {
	
    /**
     * Default ctor
     */
    public DownloadProcess() {
        this("foo","bar");
    }
	
    /**
     * Create a DownloadProcess
     * 
     * @param id       the unique id for this DataProcess
     * @param label    the label for this DataProcess
     */
    public DownloadProcess(String id, String label) {
        super(id, label);
    }

    /**
     * Add to form
     *
     * @param request  the Request
     * @param input    the Entry
     * @param sb       the form
     *
     * @throws Exception  problem adding to the form
     */
    public void addToForm(Request request,
                          DataProcessInput input,
                          StringBuffer sb)
        throws Exception {}


    /**
     * Process the request
     *
     * @param request  The request
     * @param input  the granule
     *
     * @return  the processed data
     *
     * @throws Exception  problem processing
     */
    public  DataProcessOutput processRequest(Request request, DataProcessInput input) {
        return null;
    }

    /**
     * Can we handle this type of DataProcessInput?
     * 
     * @return true if we can handle
     */
    public boolean canHandle(DataProcessInput dpi) {
    	return dpi != null && !dpi.getOperands().isEmpty();
    }

}
