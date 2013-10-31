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

// $Id: StringUtil.java,v 1.53 2007/06/01 17:02:44 jeffmc Exp $


package org.ramadda.util;


import java.util.ArrayList;
import java.util.List;


/**
 */

public class FormInfo {

    /** _more_          */
    List<Constraint> constraints = new ArrayList<Constraint>();

    /**
     * _more_
     */
    public FormInfo() {}



    /**
     * _more_
     *
     * @return _more_
     */
    public List<Constraint> getConstraints() {
        return constraints;
    }

    /**
     * _more_
     *
     * @param js _more_
     */
    public void addJavascriptValidation(StringBuffer js) {
        for (Constraint constraint : constraints) {
            constraint.addJavascriptValidation(js);
        }
    }

    /**
     * _more_
     *
     * @param label _more_
     * @param id _more_
     * @param length _more_
     */
    public void addSizeValidation(String label, String id, int length) {
        constraints.add(new MaxLength(label, id, length));
    }


    /**
     * _more_
     *
     * @param label _more_
     * @param id _more_
     */
    public void addRequiredValidation(String label, String id) {
        constraints.add(new Required(label, id));
    }


    /**
     * _more_
     *
     * @param label _more_
     * @param id _more_
     * @param min _more_
     */
    public void addMinValidation(String label, String id, double min) {
        constraints.add(new Value(label, id, min, true));
    }

    /**
     * _more_
     *
     * @param label _more_
     * @param id _more_
     * @param max _more_
     */
    public void addMaxValidation(String label, String id, double max) {
        constraints.add(new Value(label, id, max, false));
    }

    /**
     * Class description
     *
     *
     * @version        $version$, Thu, Oct 31, '13
     * @author         Enter your name here...    
     */
    public static class Constraint {

        /** _more_          */
        public String label;

        /** _more_          */
        public String id;

        /**
         * _more_
         *
         * @param label _more_
         * @param id _more_
         */
        public Constraint(String label, String id) {
            this.label = label;
            this.id    = id;
        }

        /**
         * _more_
         *
         * @param js _more_
         */
        public void addJavascriptValidation(StringBuffer js) {}

        /**
         * _more_
         *
         * @param js _more_
         * @param message _more_
         */
        public void error(StringBuffer js, String message) {
            js.append(HtmlUtils.call("alert", HtmlUtils.squote(message)));
            js.append("event.preventDefault();\n");
            js.append("return;\n");
        }

    }



    /**
     * Class description
     *
     *
     * @version        $version$, Thu, Oct 31, '13
     * @author         Enter your name here...    
     */
    public static class Value extends Constraint {

        /** _more_          */
        double value;

        /** _more_          */
        boolean min = true;

        /**
         * _more_
         *
         * @param label _more_
         * @param id _more_
         * @param value _more_
         * @param min _more_
         */
        public Value(String label, String id, double value, boolean min) {
            super(label, id);
            this.value = value;
            this.min   = min;
        }

        /**
         * _more_
         *
         * @param js _more_
         */
        public void addJavascriptValidation(StringBuffer js) {
            js.append("if(!inputValueOk(" + HtmlUtils.squote(id) + ","
                      + value + "," + (min
                                       ? "true"
                                       : "false") + ")) {\n");
            String message;
            if (min) {
                message = "Error: " + label + " is < " + value;
            } else {
                message = "Error: " + label + " is > " + value;
            }
            error(js, message);
            js.append("}\n");
        }



    }


    /**
     * Class description
     *
     *
     * @version        $version$, Thu, Oct 31, '13
     * @author         Enter your name here...    
     */
    public static class MaxLength extends Constraint {

        /** _more_          */
        public int length;

        /**
         * _more_
         *
         * @param label _more_
         * @param id _more_
         * @param length _more_
         */
        public MaxLength(String label, String id, int length) {
            super(label, id);
            this.length = length;
        }

        /**
         * _more_
         *
         * @param js _more_
         */
        public void addJavascriptValidation(StringBuffer js) {
            js.append("if(!inputLengthOk(" + HtmlUtils.squote(id) + ","
                      + length + ")) {\n");
            String message = "Error: " + label
                             + " is too long. Max length is " + length;
            error(js, message);
            js.append("}\n");
        }


    }



    /**
     * Class description
     *
     *
     * @version        $version$, Thu, Oct 31, '13
     * @author         Enter your name here...    
     */
    public static class Required extends Constraint {

        /**
         * _more_
         *
         * @param label _more_
         * @param id _more_
         */
        public Required(String label, String id) {
            super(label, id);
        }

        /**
         * _more_
         *
         * @param js _more_
         */
        public void addJavascriptValidation(StringBuffer js) {
            js.append("if(!inputIsRequired(" + HtmlUtils.squote(id)
                      + ")) {\n");
            String message = "Error: " + label + " is required";
            error(js, message);
            js.append("}\n");
        }


    }

}
