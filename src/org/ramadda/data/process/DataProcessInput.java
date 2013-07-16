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



import ucar.unidata.util.Misc;


import java.io.File;

import java.util.List;


/**
 * Class description
 *
 *
 */
public class DataProcessInput {

    /** the process directory for this input */
    private File processDir;

    /** The operands for this input */
    private List<DataProcessOperand> operands;

    /**
     * Create a data process input
     *
     * @param operands  the operands for this process
     */
    public DataProcessInput(DataProcessOperand operand) {
        this(null, Misc.newList(operand));
    }

    /**
     * Create a data process input
     *
     * @param operands  the operands for this process
     */
    public DataProcessInput(File processDir, DataProcessOperand operand) {
        this(processDir, Misc.newList(operand));
    }

    /**
     * Create a data process input
     *
     * @param operands  the operands for this process
     */
    public DataProcessInput(List<DataProcessOperand> operands) {
    	// TODO: should we call this with a default directory?
        this(null, operands);
    }

    /**
     * Create a DataProcessInput from a list of operands
     *
     *
     * @param dir process directory
     * @param operands the operands
     */
    public DataProcessInput(File dir, List<DataProcessOperand> operands) {

        this.processDir = dir;
        this.operands   = operands;

    }


    /**
     * Get the operands
     *
     * @return the list of operands
     */
    public List<DataProcessOperand> getOperands() {
        return operands;
    }

    /**
     * Get the process directory
     *
     * @return  the process directory
     */
    public File getProcessDir() {
        return processDir;
    }


}
