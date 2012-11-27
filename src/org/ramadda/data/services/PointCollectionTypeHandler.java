
package org.ramadda.data.services;


import org.ramadda.data.record.RecordField;
import org.ramadda.data.services.*;


import org.ramadda.repository.*;
import org.ramadda.repository.map.MapInfo;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.AtomUtil;

import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;

import ucar.unidata.xml.XmlUtil;


import java.util.Date;
import java.util.HashSet;
import java.util.List;


/**
 *
 *
 * @author Jeff McWhirter
 * @version $Revision: 1.3 $
 */
public abstract class PointCollectionTypeHandler extends RecordCollectionTypeHandler {





    /**
     * ctor
     *
     * @param repository the repository
     * @param node the xml node from the types.xml file
     * @throws Exception On badness
     */
    public PointCollectionTypeHandler(Repository repository, Element node)
            throws Exception {
        super(repository, node);
    }


    public PointOutputHandler getPointOutputHandler() {
        return (PointOutputHandler)getRecordOutputHandler();
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param map _more_
     *
     * @return _more_
     */
    @Override
    public boolean addToMap(Request request, Entry entry, MapInfo map) {
        try {
            getPointOutputHandler().addToMap(request, entry, map);
            return true;
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }




    /**
     * add the list of point services
     *
     * @param request the request
     * @param entry the entry
     * @param services _more_
     *
     * @return list of services
     */
    @Override
    public void getServices(Request request, Entry entry,
                            List<Service> services) {
        super.getServices(request, entry, services);

        String dfltBbox = entry.getWest() + "," + entry.getSouth() + ","
                          + entry.getEast() + "," + entry.getNorth();
        String     url;
        String[][] values = {
            { getPointOutputHandler().OUTPUT_LATLONALTCSV.toString(),
              "Lat/Lon/Alt CSV", ".csv", RecordConstants.ICON_POINTS },
            { getPointOutputHandler().OUTPUT_LAS.toString(), "LAS", ".las", null },
            { getPointOutputHandler().OUTPUT_ASC.toString(), "ARC Ascii Grid",
              ".asc", null },
            { getPointOutputHandler().OUTPUT_KMZ.toString(), "Google Earth KMZ",
              ".kmz", getIconUrl(request, ICON_KML) },
            { getPointOutputHandler().OUTPUT_POINTCOUNT.toString(), "Point Count",
              ".xml", null },
        };

        for (String[] tuple : values) {
            String product = tuple[0];
            String name    = tuple[1];
            String suffix  = tuple[2];
            String icon    = tuple[3];
            String tail    = HtmlUtils.urlEncodeExceptSpace(
                              entry.getName()).replaceAll(" ", "_") + suffix;

            url = HtmlUtils.url(getRepository().URL_ENTRY_SHOW + "/" + tail,
                                new String[] {
                ARG_ENTRYID, entry.getId(), ARG_OUTPUT,
                getPointOutputHandler().OUTPUT_PRODUCT.getId(), ARG_PRODUCT, product,
                RecordConstants.ARG_ASYNCH, "true",
                //                RecordConstants.ARG_RECORD_SKIP,
                //                macro(RecordConstants.ARG_RECORD_SKIP), ARG_BBOX,
                //                macro(ARG_BBOX), ARG_DEFAULTBBOX, dfltBbox
            }, false);
            services.add(new Service(product, name,
                                     request.getAbsoluteUrl(url), icon));
        }


        try {
            for (Metadata metadata :
                    getMetadataManager().getMetadata(entry)) {
                if (metadata.getType().equals(METADATA_URL)) {
                    //attr1=url, attr2=label, attr3=rel, attr4=mime type
                    services.add(new Service(metadata.getAttr3(),
                                             metadata.getAttr2(),
                                             metadata.getAttr1(), null,
                                             metadata.getAttr4()));
                }
            }
            //Gack, cut-and-paste, add something to metadatamgr sometime
            for (Metadata metadata :
                     getMetadataManager().getInheritedMetadata(request, entry)) {
                if (metadata.getType().equals(METADATA_URL)) {
                    //attr3= type, attr2=label, attr1=url
                    services.add(new Service(metadata.getAttr3(),
                                             metadata.getAttr2(),
                                             metadata.getAttr1()));
                }
            }
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }


}
