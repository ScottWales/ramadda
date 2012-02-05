package org.ramadda.geodata.fits;


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
import nom.tam.fits.*;


/**
 *
 *
 * @author Jeff McWhirter
 * @version $Revision: 1.3 $
 */
public class FitsTypeHandler extends GenericTypeHandler {

    public static final String PROP_INSTRUMENT = "INSTRUME";
    public static final String PROP_TELESCOPE = "TELESCOP";
    public static final String PROP_LOCATION = "LOCATION";
    public static final String PROP_ORIGIN = "ORIGIN";
    public static final String PROP_OBJECT = "OBJECT";
    public static final String PROP_OBSERVER = "OBSERVER";
    public static final String PROP_AUTHOR = "AUTHOR";
    public static final String PROP_REFERENCE = "REFERENC";
    public static final String PROP_TYPE_OBS = "TYPE-OBS";
    public static final String PROP_DATE_OBS = "DATE-OBS";
    public static final String PROP_TIME_OBS = "TIME-OBS";

    public static final String[] PROPS = {
        PROP_ORIGIN,
        PROP_TELESCOPE,
        PROP_INSTRUMENT,
        PROP_TYPE_OBS,
    };


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

    public void initializeEntryFromForm(Request request, Entry entry,
                                        Entry parent, boolean newEntry)
            throws Exception {
        if(newEntry) {
            initializeNewEntry(entry);
        }
    }


    @Override
    public void initializeNewEntry(Entry entry) throws Exception {
        Object[] values = entry.getValues();
        if (values == null) {
            values = new Object[4];
        }
        Fits fits = new Fits(entry.getFile());
        for(int headerIdx=0;headerIdx<fits.size();headerIdx++) {
            BasicHDU hdu = fits.getHDU(headerIdx);
            Date date = hdu.getObservationDate();
            if(date!=null) {
                entry.setStartDate(date.getTime());
                entry.setEndDate(date.getTime());
            }
            nom.tam.fits.Header header = hdu.getHeader();
            for(int i=0;i<PROPS.length;i++) {
                if(values[i]==null) {
                    String value = header.getStringValue(PROPS[i]);
                    if(value!=null)
                        values[i] =   value.trim();
                }
            }
        }
        entry.setValues(values);
    }


    public static void processFits(String file) throws Exception {
        Fits fits = new Fits(file);
        System.out.println("file:" + file);
        for(int headerIdx=0;headerIdx<fits.size();headerIdx++) {
            BasicHDU hdu = fits.getHDU(headerIdx);
            nom.tam.fits.Header header = hdu.getHeader();
            String hduType = "N/A";
            if(hdu instanceof AsciiTableHDU) {
                hduType = "Ascii Table";
            } else  if(hdu instanceof ImageHDU) {
                hduType = "Image";
            } else  if(hdu instanceof BinaryTableHDU) {
                hduType = "Binary Table";
            }

            System.out.println("hdu:" + hduType +" -- " + hdu.getObservationDate());
            for(String prop:PROPS) {
                String value = header.getStringValue(prop);
                if(value!=null) {
                    System.out.println("\t" + prop +"=" + value);
                }
            }
            int numCards = header.getNumberOfCards();
            for(int cardIdx=0;cardIdx<numCards;cardIdx++) {
                String card = header.getCard(cardIdx);
                //                System.out.println("\tcard:" + card);
            }
        }
    }







    public static void main(String[]args) throws Exception  {
        for(String file: args) {
            processFits(file);
        }

    }


}
