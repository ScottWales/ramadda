

import org.w3c.dom.Element;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.xml.XmlUtil;

/**
 */
public class GeoUtils {


    /**
     * Look up the location of the given address
     *
     * @param address The address
     *
     * @return The location or null if not found
     */
    public static double[] getLocationFromAddress(String address) {
        if (address == null) {
            return null;
        }
        address = address.trim();
        if (address.length() == 0) {
            return null;
        }
        
        String latString      = null;
        String lonString      = null;
        String encodedAddress = StringUtil.replace(address, " ", "%20");

        try {
            
            String url =
                "http://where.yahooapis.com/geocode?appid=ramadda&q="+ encodedAddress;
            String result = IOUtil.readContents(url, GeoUtils.class);
            Element root    = XmlUtil.getRoot(result);
            Element latNode = XmlUtil.findDescendant(root,
                                                     "latitude");
            Element lonNode = XmlUtil.findDescendant(root,
                                                     "longitude");
            if ((latNode != null) && (lonNode != null)) {
                latString = XmlUtil.getChildText(latNode);
                lonString = XmlUtil.getChildText(lonNode);
            }
        } catch (Exception exc) {}
        if(latString!=null && lonString!=null) {
            return new double[]{Double.parseDouble(latString),
                                Double.parseDouble(lonString)};

        }
        return null;
    }

}
