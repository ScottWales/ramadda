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
package org.ramadda.geodata.fits;


import nom.tam.fits.*;


import org.ramadda.repository.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.AtomUtil;


import org.w3c.dom.*;

import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;

import java.awt.geom.Rectangle2D;

import java.io.File;


import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import java.util.List;


/**
 *
 *
 * @author Jeff McWhirter
 * @version $Revision: 1.3 $
 */
public class FitsTypeHandler extends GenericTypeHandler {

    /** _more_          */
    public static final String PROP_INSTRUMENT = "INSTRUME";

    /** _more_          */
    public static final String PROP_TELESCOPE = "TELESCOP";

    /** _more_          */
    public static final String PROP_LOCATION = "LOCATION";

    /** _more_          */
    public static final String PROP_ORIGIN = "ORIGIN";

    /** _more_          */
    public static final String PROP_OBJECT = "OBJECT";

    /** _more_          */
    public static final String PROP_OBSERVER = "OBSERVER";

    /** _more_          */
    public static final String PROP_AUTHOR = "AUTHOR";

    /** _more_          */
    public static final String PROP_REFERENCE = "REFERENC";

    /** _more_          */
    public static final String PROP_TYPE_OBS = "TYPE-OBS";

    /** _more_          */
    public static final String PROP_DATE_OBS = "DATE-OBS";

    /** _more_          */
    public static final String PROP_TIME_OBS = "TIME-OBS";

    /** _more_          */
    public static final String[] PROPS = { PROP_ORIGIN, PROP_TELESCOPE,
                                           PROP_INSTRUMENT, PROP_TYPE_OBS, };


    /** _more_          */
    public static final String PROP_ = "";



    /**
     * ctor
     *
     * @param repository the repository
     * @param node the xml node from the types.xml file
     * @throws Exception On badness
     */
    public FitsTypeHandler(Repository repository, Element node)
            throws Exception {
        super(repository, node);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param parent _more_
     * @param newEntry _more_
     *
     * @throws Exception _more_
     */
    public void initializeEntryFromForm(Request request, Entry entry,
                                        Entry parent, boolean newEntry)
            throws Exception {
        if (newEntry) {
            initializeNewEntry(entry);
        }
    }


    /**
     * _more_
     *
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    @Override
    public void initializeNewEntry(Entry entry) throws Exception {
        Object[] values = entry.getValues();
        if (values == null) {
            values = new Object[4];
        }
        Fits fits = new Fits(entry.getFile());
        for (int headerIdx = 0; headerIdx < fits.size(); headerIdx++) {
            BasicHDU hdu  = fits.getHDU(headerIdx);
            Date     date = hdu.getObservationDate();
            if (date != null) {
                entry.setStartDate(date.getTime());
                entry.setEndDate(date.getTime());
            }
            nom.tam.fits.Header header = hdu.getHeader();
            for (int i = 0; i < PROPS.length; i++) {
                if (values[i] == null) {
                    String value = header.getStringValue(PROPS[i]);
                    if (value != null) {
                        values[i] = value.trim();
                    }
                }
            }
        }
        entry.setValues(values);
    }


    /**
     * _more_
     *
     * @param file _more_
     *
     * @throws Exception _more_
     */
    public static void processFits(String file) throws Exception {
        Fits fits = new Fits(file);
        System.out.println("file:" + file);
        for (int headerIdx = 0; headerIdx < fits.size(); headerIdx++) {
            BasicHDU            hdu     = fits.getHDU(headerIdx);
            nom.tam.fits.Header header  = hdu.getHeader();
            String              hduType = "N/A";
            if (hdu instanceof AsciiTableHDU) {
                hduType = "Ascii Table";
            } else if (hdu instanceof ImageHDU) {
                hduType = "Image";
            } else if (hdu instanceof BinaryTableHDU) {
                hduType = "Binary Table";
            }

            System.out.println("hdu:" + hduType + " -- "
                               + hdu.getObservationDate());
            for (String prop : PROPS) {
                String value = header.getStringValue(prop);
                if (value != null) {
                    System.out.println("\t" + prop + "=" + value);
                }
            }
            int numCards = header.getNumberOfCards();
            for (int cardIdx = 0; cardIdx < numCards; cardIdx++) {
                String card = header.getCard(cardIdx);
                //                System.out.println("\tcard:" + card);
            }
        }
    }







    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
        for (String file : args) {
            processFits(file);
        }

    }


}
