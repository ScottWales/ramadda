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

package org.ramadda.geodata.cdmdata;


import ucar.unidata.xml.XmlUtil;


/**
 * A utility class for Ncml for handling NcML generation for
 * aggregations.
 */
public class NcmlUtil {

    /** JoinExisting Aggregation type */
    public static final String AGG_JOINEXISTING = "joinExisting";

    /** JoinNew Aggregation type */
    public static final String AGG_JOINNEW = "joinNew";

    /** Union Aggregation type */
    public static final String AGG_UNION = "union";

    /** Ensemble Aggregation type */
    public static final String AGG_ENSEMBLE = "ensemble";

    /** The NcML XML namespace identifier */
    public static final String XMLNS_XMLNS =
        "http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2";

    /** The netcdf tag */
    public static final String TAG_NETCDF = "netcdf";

    /** The variable tag */
    public static final String TAG_VARIABLE = "variable";

    /** The attribute tag */
    public static final String TAG_ATTRIBUTE = "attribute";

    /** The aggregation tag */
    public static final String TAG_AGGREGATION = "aggregation";

    /** The variableAgg tag */
    public static final String TAG_VARIABLEAGG = "variableAgg";

    /** The name attribute */
    public static final String ATTR_NAME = "name";

    /** The shape attribute */
    public static final String ATTR_SHAPE = "shape";

    /** The type attribute */
    public static final String ATTR_TYPE = "type";

    /** The value attribute */
    public static final String ATTR_VALUE = "value";

    /** The dimName attribute */
    public static final String ATTR_DIMNAME = "dimName";

    /** The coordValue attribute */
    public static final String ATTR_COORDVALUE = "coordValue";

    /** The location attribute */
    public static final String ATTR_LOCATION = "location";

    /** The enhance attribute */
    public static final String ATTR_ENHANCE = "enhance";

    /** The timeUnitsChange attribute */
    public static final String ATTR_TIMEUNITSCHANGE = "timeUnitsChange";

    /** the aggregation type */
    private String aggType;

    /**
     * Create a new NcML utility with the aggregation type
     *
     * @param aggType _more_
     */
    public NcmlUtil(String aggType) {
        this.aggType = aggType;
    }

    /**
     * Create a String identifier for this object
     *
     * @return  the String identifier
     */
    public String toString() {
        return aggType;
    }

    /**
     * Is this a JoinExisting aggregation?
     *
     * @return true if this is a JoinExisting aggregation
     */
    public boolean isJoinExisting() {
        return aggType.equalsIgnoreCase(AGG_JOINEXISTING);
    }

    /**
     * Is this a JoinNew aggregation?
     *
     * @return  true if JoinNew
     */
    public boolean isJoinNew() {
        return aggType.equalsIgnoreCase(AGG_JOINNEW);
    }

    /**
     * Is this an Union aggregation?
     *
     * @return true if Union
     */
    public boolean isUnion() {
        return aggType.equalsIgnoreCase(AGG_UNION);
    }

    /**
     * Is this an Ensemble aggregation?
     *
     * @return true if ensemble aggregation
     */
    public boolean isEnsemble() {
        return aggType.equalsIgnoreCase(AGG_ENSEMBLE);
    }


    /**
     * Create an open Ncml tag
     *
     * @param sb  the StringBuffer to add to
     */
    public static void openNcml(StringBuffer sb) {
        sb.append(XmlUtil.openTag(TAG_NETCDF,
                                  XmlUtil.attrs(new String[] { "xmlns",
                XMLNS_XMLNS })));
    }


    /**
     * Add the ensemble variable and attributes
     *
     * @param sb the StringBuffer to add to
     * @param name  the name of the ensemble variable
     */
    public static void addEnsembleVariables(StringBuffer sb, String name) {
        /*
 <variable name='ens' type='String' shape='ens'>
   <attribute name='long_name' value='ensemble coordinate' />
   <attribute name='_CoordinateAxisType' value='Ensemble' />
 </variable>
        */

        sb.append(XmlUtil.tag(TAG_VARIABLE, XmlUtil.attrs(new String[] {
            ATTR_NAME, name, ATTR_TYPE, "String", ATTR_SHAPE, name
        }), XmlUtil.tag(TAG_ATTRIBUTE, XmlUtil.attrs(new String[] { ATTR_NAME,
                "long_name", ATTR_VALUE,
                "ensemble coordinate" })) + XmlUtil.tag(TAG_ATTRIBUTE,
                    XmlUtil.attrs(new String[] { ATTR_NAME,
                "_CoordinateAxisType", ATTR_VALUE, "Ensemble" }))));
    }


}
