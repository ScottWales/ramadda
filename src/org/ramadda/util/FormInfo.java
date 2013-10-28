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

    List<Constraint> constraints = new ArrayList<Constraint>();

    public FormInfo() {}



    public List<Constraint> getConstraints() {
        return constraints;
    }

    public void addJavascriptValidation(StringBuffer js) {
        for(Constraint constraint: constraints) {
            constraint.addJavascriptValidation(js);
        }
    }

    public void addSizeValidation(String label, String id, int length) {
        constraints.add(new MaxLength(label, id, length));
    }


    public void addRequiredValidation(String label, String id) {
        constraints.add(new Required(label, id));
    }


    public void addMinValidation(String label, String id, double min) {
        constraints.add(new Value(label, id, min, true));
    }

    public void addMaxValidation(String label, String id, double max) {
        constraints.add(new Value(label, id, max, false));
    }

    public static class Constraint {
        public String label;
        public String id;
        public Constraint(String label, String id) {
            this.label = label;
            this.id = id;
        }

        public void addJavascriptValidation(StringBuffer js) {
        }

        public void error(StringBuffer js, String message) {
            js.append(HtmlUtils.call("alert", HtmlUtils.squote(message)));
            js.append("event.preventDefault();\n");
            js.append("return;\n");
        }

    }



    public static class Value extends Constraint {
        double value;
        boolean min  =true;

        public Value(String label, String id,double value, boolean min) {
            super(label, id);
            this.value = value;
            this.min = min;
        }

        public void addJavascriptValidation(StringBuffer js) {
            js.append("if(!inputValueOk("  +
                      HtmlUtils.squote(id) +"," +value + "," + (min?"true":"false")+")) {\n");
            String message;
            if(min)
                message = "Error: " + label +" is < " + value;
            else 
                message = "Error: " + label +" is > " + value;
            error(js, message);
            js.append("}\n");
        }



    }


    public static class MaxLength extends Constraint {
        public int length;
        public MaxLength(String label, String id, int length) {
            super(label, id);
            this.length = length;
        }

        public void addJavascriptValidation(StringBuffer js) {
            js.append("if(!inputLengthOk("  +
                      HtmlUtils.squote(id) +"," +length +")) {\n");
            String message = "Error: " + label +" is too long. Max length is " +length;
            error(js, message);
            js.append("}\n");
        }


    }



    public static class Required extends Constraint {
        public Required(String label, String id) {
            super(label, id);
        }

        public void addJavascriptValidation(StringBuffer js) {
            js.append("if(!inputIsRequired("  +
                      HtmlUtils.squote(id)+")) {\n");
            String message = "Error: " + label +" is required";
            error(js, message);
            js.append("}\n");
        }


    }

}
