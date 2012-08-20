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

package org.ramadda.repository;


import org.ramadda.repository.auth.*;


import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;

import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;

import java.awt.geom.Rectangle2D;


import java.io.File;

import java.util.ArrayList;


import java.util.Date;
import java.util.Hashtable;
import java.util.List;


/**
 * Class Entry _more_
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class Entry implements Cloneable {

    /** _more_ */
    public static final String IDDELIMITER = ":";

    /** _more_ */
    public static final String PATHDELIMITER = "/";

    /** _more_ */
    public static final double NONGEO = -999999;



    /** _more_ */
    List<Comment> comments;

    /** _more_ */
    List<Permission> permissions = null;

    /** _more_ */
    Hashtable permissionMap = new Hashtable();

    /** _more_ */
    List<Association> associations;


    /** _more_ */
    List<Metadata> metadata;

    /** _more_ */
    private String id;

    /** _more_ */
    private String name = "";

    /** _more_ */
    private String description = "";

    /** _more_ */
    private Entry parentEntry;

    /** _more_ */
    private String parentEntryId;

    /** _more_ */
    private String treeId;


    /** _more_ */
    private User user;

    /** _more_ */
    private long createDate = 0L;

    /** _more_ */
    private long changeDate = 0L;


    /** _more_ */
    boolean isDummy = false;

    /** _more_ */
    Object[] values;

    /** _more_ */
    private Resource resource = new Resource();

    /** _more_ */
    private String category;

    /** _more_ */
    private TypeHandler typeHandler;

    /** _more_ */
    private long startDate;

    /** _more_ */
    private long endDate;

    /** _more_ */
    private double south = NONGEO;

    /** _more_ */
    private double north = NONGEO;

    /** _more_ */
    private double east = NONGEO;

    /** _more_ */
    private double west = NONGEO;

    /** _more_ */
    private double altitudeBottom = NONGEO;

    /** _more_ */
    private double altitudeTop = NONGEO;

    /** _more_ */
    private boolean isLocalFile = false;

    /** _more_ */
    private boolean isRemoteEntry = false;

    /** _more_ */
    private String remoteServer;

    /** _more_ */
    private String icon;

    /** _more_ */
    private Hashtable properties;

    /** _more_ */
    private String propertiesString;

    /** _more_ */
    private Hashtable transientProperties = new Hashtable();


    /** _more_ */
    private boolean isGroup = false;


    /** _more_ */
    List<Entry> subGroups;

    /** _more_ */
    List<Entry> subEntries;


    /** _more_ */
    private List<String> childIds;


    /**
     * _more_
     */
    public Entry() {}

    /**
     * _more_
     *
     * @param that _more_
     */
    public Entry(Entry that) {
        //        super(that);
        initWith(that);
    }


    /**
     * _more_
     *
     * @param id _more_
     */
    public Entry(String id) {
        setId(id);
    }


    /**
     * _more_
     *
     * @param handler _more_
     * @param isDummy _more_
     */
    public Entry(TypeHandler handler, boolean isDummy) {
        this(handler, isDummy, "Search Results");
    }


    /**
     * _more_
     *
     * @param handler _more_
     * @param isDummy _more_
     * @param dummyName _more_
     */
    public Entry(TypeHandler handler, boolean isDummy, String dummyName) {
        this("", handler);
        this.isDummy = isDummy;
        setName(dummyName);
        setDescription("");
    }



    /**
     * _more_
     *
     * @param id _more_
     * @param typeHandler _more_
     */
    public Entry(String id, TypeHandler typeHandler) {
        this(id);
        this.typeHandler = typeHandler;
    }

    /**
     * _more_
     *
     * @param id _more_
     * @param typeHandler _more_
     * @param isGroup _more_
     */
    public Entry(String id, TypeHandler typeHandler, boolean isGroup) {
        this(id);
        this.typeHandler = typeHandler;
        this.isGroup     = isGroup;
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param id _more_
     *
     * @return _more_
     */
    public Entry createGeneratedEntry(Request request, String id) {
        return null;
    }

    /**
     *  Set the ChildIds property.
     *
     *  @param value The new value for ChildIds
     */
    public void setChildIds(List<String> value) {
        childIds = value;
    }

    /**
     *  Get the ChildIds property.
     *
     *  @return The ChildIds
     */
    public List<String> getChildIds() {
        return childIds;
    }





    /**
     * _more_
     *
     * @return _more_
     *
     * @throws CloneNotSupportedException _more_
     */
    public Object clone() throws CloneNotSupportedException {
        Entry that = (Entry) super.clone();
        that.associations = null;

        return that;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getFullName() {
        return getFullName(false);
    }

    /**
     * _more_
     *
     * @param encodeForUrl _more_
     *
     * @return _more_
     */
    public String getFullName(boolean encodeForUrl) {
        String name = getName();
        //        boolean debug = name.indexOf("crap")>=0;
        //Encode any URLish characters
        if (encodeForUrl) {
            name = encodeName(name);
        }
        //        if(debug)
        //            System.err.println ("getFullName:" + name);
        Entry parent = getParentEntry();
        if (parent != null) {
            String parentName = parent.getFullName(encodeForUrl);

            //            if(debug)
            //                System.err.println ("parent name:" + parentName);
            return parentName + PATHDELIMITER + name;
        }

        return name;
    }


    /**
     * _more_
     *
     * @param name _more_
     *
     * @return _more_
     */
    public static String encodeName(String name) {
        name = name.replaceAll("\\/", "%2F");
        name = name.replaceAll("\\?", "%3F");
        name = name.replaceAll("\\&", "%26");
        name = name.replaceAll("\\#", "%23");

        return name;
    }

    /**
     * _more_
     *
     * @param name _more_
     *
     * @return _more_
     */
    public static String decodeName(String name) {
        name = name.replaceAll("%2F", "/");
        name = name.replaceAll("%3F", "?");
        name = name.replaceAll("%26", "&");

        return name;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public File getFile() {
        return getTypeHandler().getFileForEntry(this);
    }



    /**
     *  _more_
     *
     *  @param template _more_
     */
    public void initWith(Entry template) {
        setName(template.getName());
        setDescription(template.getDescription());

        if (template.getMetadata() != null) {
            List<Metadata> thisMetadata = new ArrayList<Metadata>();
            for (Metadata metadata : template.getMetadata()) {
                metadata.setEntryId(getId());
                thisMetadata.add(metadata);
            }
            setMetadata(thisMetadata);
        }
        setCreateDate(template.getCreateDate());
        setChangeDate(template.getChangeDate());
        setStartDate(template.getStartDate());
        setEndDate(template.getEndDate());
        setNorth(template.getNorth());
        setSouth(template.getSouth());
        setEast(template.getEast());
        setWest(template.getWest());
        this.altitudeTop    = template.altitudeTop;
        this.altitudeBottom = template.altitudeBottom;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Rectangle2D.Double getBounds() {
        return new Rectangle2D.Double(west, south, east - west,
                                      north - south);
    }


    /**
     * _more_
     *
     * @param rect _more_
     */
    public void setBounds(Rectangle2D.Double rect) {
        west  = rect.getX();
        south = rect.getY();
        east  = west + rect.getWidth();
        north = south + rect.getHeight();
    }




    /**
     * _more_
     *
     * @param name _more_
     * @param description _more_
     * @param group _more_
     * @param parentEntry _more_
     * @param user _more_
     * @param resource _more_
     * @param category _more_
     * @param createDate _more_
     * @param changeDate _more_
     * @param startDate _more_
     * @param endDate _more_
     * @param values _more_
     */
    public void initEntry(String name, String description, Entry parentEntry,
                          User user, Resource resource, String category,
                          long createDate, long changeDate, long startDate,
                          long endDate, Object[] values) {
        //        super.init(name, description, parentEntry, user, createDate,changeDate);
        this.id          = id;
        this.name        = name;
        this.description = description;
        this.parentEntry = parentEntry;
        this.user        = user;

        setCreateDate(createDate);
        setChangeDate(changeDate);


        this.resource = resource;
        this.category = category;
        if ((category == null) || (category.length() == 0)) {
            this.category = typeHandler.getDefaultCategory();
        }
        if (this.category == null) {
            this.category = "";
        }
        this.startDate = startDate;
        this.endDate   = endDate;
        this.values    = values;
    }








    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isFile() {
        return (resource != null) && resource.isFile();
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getInsertSql() {
        return null;
    }

    /**
     * Set the resource property.
     *
     * @param value The new value for resource
     */
    public void setResource(Resource value) {
        resource = value;
    }

    /**
     * Get the resource property.
     *
     * @return The resource
     */
    public Resource getResource() {
        return resource;
    }




    /**
     * _more_
     *
     * @param value _more_
     */
    public void setDate(long value) {
        setCreateDate(value);
        setChangeDate(value);
        setStartDate(value);
        setEndDate(value);
    }




    /**
     * Set the StartDate property.
     *
     * @param value The new value for StartDate
     */
    public void setStartDate(long value) {
        startDate = value;
    }

    /**
     * Get the StartDate property.
     *
     * @return The StartDate
     */
    public long getStartDate() {
        return startDate;
    }

    /**
     * Set the EndDate property.
     *
     * @param value The new value for EndDate
     */
    public void setEndDate(long value) {
        endDate = value;
    }

    /**
     * Get the EndDate property.
     *
     * @return The EndDate
     */
    public long getEndDate() {
        return endDate;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isTopEntry() {
        return isGroup() && (getParentEntryId() == null);
    }

    /**
     * _more_
     *
     * @return _more_
     * @deprecated use isTopEntry
     */
    public boolean isTopGroup() {
        return isTopEntry();
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isGroup() {
        if (isGroup) {
            return true;
        }
        if (typeHandler != null) {
            return typeHandler.isGroup();
        }

        return false;
    }

    /**
     * _more_
     *
     * @param g _more_
     */
    public void setGroup(boolean g) {
        isGroup = g;
    }

    /**
     * Set the Type property.
     *
     * @param value The new value for Type
     */
    public void setTypeHandler(TypeHandler value) {
        typeHandler = value;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getType() {
        return typeHandler.getType();
    }

    /**
     * _more_
     *
     * @param type _more_
     *
     * @return _more_
     */
    public boolean isType(String type) {
        return getType().equals(type);
    }


    /**
     * Get the Type property.
     *
     * @return The Type
     */
    public TypeHandler getTypeHandler() {
        return typeHandler;
    }



    /**
     * Set the Values property.
     *
     * @param value The new value for Values
     */
    public void setValues(Object[] value) {
        values = value;
    }

    /**
     * Get the Values property.
     *
     * @return The Values
     */
    public Object[] getValues() {
        return values;
    }

    /**
     * _more_
     *
     * @param index _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public String getValue(int index, String dflt) {
        if ((values == null) || (index >= values.length)
                || (values[index] == null)) {
            return dflt;
        }

        return values[index].toString();
    }

    /**
     * _more_
     *
     * @param index _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public double getValue(int index, double dflt) {
        String sValue = getValue(index, "");
        if (sValue.length() == 0) {
            return dflt;
        }

        return Double.parseDouble(sValue);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return name + " id:" + id + "  type:" + getTypeHandler();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean hasLocationDefined() {
        if ((south != NONGEO) && (east != NONGEO) && !hasAreaDefined()) {
            return true;
        }

        return false;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public double[] getLocation() {
        return new double[] { south, east };
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public double getLatitude() {
        return south;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public double getLongitude() {
        return east;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public double[] getCenter() {
        return new double[] { south + (north - south) / 2,
                              east + (west - east) / 2 };
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean hasAreaDefined() {
        if ((south != NONGEO) && (east != NONGEO) && (north != NONGEO)
                && (west != NONGEO)) {
            if ((south == north) && (east == west)) {
                return false;
            }

            return true;
        }

        return false;
    }

    /**
     * _more_
     */
    public void trimAreaResolution() {
        double diff = (south - north);
        if (Math.abs(diff) > 1) {
            south = ((int) (south * 1000)) / 1000.0;
            north = ((int) (north * 1000)) / 1000.0;
        }
        diff = (east - west);
        if (Math.abs(diff) > 1) {
            east = ((int) (east * 1000)) / 1000.0;
            west = ((int) (west * 1000)) / 1000.0;
        }

    }

    /**
     * _more_
     */
    public void clearArea() {
        south = north = east = west = NONGEO;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getLabel() {
        String label = getBaseLabel();
        if (label.length() > 0) {
            return label;
        }

        return getTypeHandler().getLabel() + ": " + new Date(startDate);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getBaseLabel() {
        if ((name != null) && (name.trim().length() > 0)) {
            return name;
        }
        if ((description != null) && (description.trim().length() > 0)) {
            return description;
        }

        return "";

    }



    /**
     * _more_
     *
     * @param that _more_
     */
    public void setLocation(Entry that) {
        this.north          = that.north;
        this.south          = that.south;
        this.east           = that.east;
        this.west           = that.west;
        this.altitudeTop    = that.altitudeTop;
        this.altitudeBottom = that.altitudeBottom;
    }


    /**
     * _more_
     *
     * @param lat _more_
     * @param lon _more_
     * @param alt _more_
     */
    public void setLocation(double lat, double lon, double alt) {
        this.north          = lat;
        this.south          = lat;
        this.east           = lon;
        this.west           = lon;
        this.altitudeTop    = alt;
        this.altitudeBottom = alt;
    }


    /**
     * Set the South property.
     *
     * @param value The new value for South
     */
    public void setSouth(double value) {
        south = value;
    }

    /**
     * Get the South property.
     *
     * @return The South
     */
    public double getSouth() {
        return ((south == south)
                ? south
                : NONGEO);
    }

    /**
     * Set the North property.
     *
     * @param value The new value for North
     */
    public void setNorth(double value) {
        north = value;
    }

    /**
     * Get the North property.
     *
     * @return The North
     */
    public double getNorth() {
        return ((north == north)
                ? north
                : NONGEO);
    }

    /**
     * _more_
     *
     * @param value _more_
     */
    public void setLatitude(double value) {
        north = value;
        south = value;
    }

    /**
     * _more_
     *
     * @param value _more_
     */
    public void setLongitude(double value) {
        east = value;
        west = value;
    }

    /**
     * _more_
     *
     * @param value _more_
     */
    public void setAltitude(double value) {
        altitudeTop    = value;
        altitudeBottom = value;
    }

    /**
     *  Set the AltitudeTop property.
     *
     *  @param value The new value for AltitudeTop
     */
    public void setAltitudeTop(double value) {
        altitudeTop = value;
    }

    /**
     *  Get the AltitudeTop property.
     *
     *  @return The AltitudeTop
     */
    public double getAltitudeTop() {
        return altitudeTop;
    }

    /**
     *  Set the AltitudeBottom property.
     *
     *  @param value The new value for AltitudeBottom
     */
    public void setAltitudeBottom(double value) {
        altitudeBottom = value;
    }

    /**
     *  Get the AltitudeBottom property.
     *
     *  @return The AltitudeBottom
     */
    public double getAltitudeBottom() {
        return altitudeBottom;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public double getAltitude() {
        return altitudeTop;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean hasAltitudeTop() {
        return (altitudeTop == altitudeTop) && (altitudeTop != NONGEO);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean hasAltitudeBottom() {
        return (altitudeBottom == altitudeBottom)
               && (altitudeBottom != NONGEO);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean hasAltitude() {
        return hasAltitudeTop() && hasAltitudeBottom()
               && (altitudeBottom == altitudeTop);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean hasNorth() {
        return (north == north) && (north != NONGEO);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean hasSouth() {
        return (south == south) && (south != NONGEO);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean hasEast() {
        return (east == east) && (east != NONGEO);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean hasWest() {
        return (west == west) && (west != NONGEO);
    }


    /**
     * Set the East property.
     *
     * @param value The new value for East
     */
    public void setEast(double value) {
        east = value;
    }

    /**
     * Get the East property.
     *
     * @return The East
     */
    public double getEast() {
        return ((east == east)
                ? east
                : NONGEO);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public double[][] getLatLonBounds() {
        return new double[][] {
            { north, north, south, south, north },
            { west, east, east, west, west }
        };
    }

    /**
     * Set the West property.
     *
     * @param value The new value for West
     */
    public void setWest(double value) {
        west = value;
    }

    /**
     * Get the West property.
     *
     * @return The West
     */
    public double getWest() {
        return ((west == west)
                ? west
                : NONGEO);
    }



    /**
     * Set the Category property.
     *
     * @param value The new value for Category
     */
    public void setCategory(String value) {
        category = value;
    }

    /**
     * Get the Category property.
     *
     * @return The Category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Set the IsLocalFile property.
     *
     * @param value The new value for IsLocalFile
     */
    public void setIsLocalFile(boolean value) {
        isLocalFile = value;
    }

    /**
     * Get the IsLocalFile property.
     *
     * @return The IsLocalFile
     */
    public boolean getIsLocalFile() {
        return isLocalFile;
    }

    /**
     *  Set the Icon property.
     *
     *  @param value The new value for Icon
     */
    public void setIcon(String value) {
        icon = value;
    }

    /**
     *  Get the Icon property.
     *
     *  @return The Icon
     */
    public String getIcon() {
        return icon;
    }


    /**
     * Set the IsRemoteEntry property.
     *
     * @param value The new value for IsRemoteEntry
     */
    public void setIsRemoteEntry(boolean value) {
        isRemoteEntry = value;
    }

    /**
     * Get the IsRemoteEntry property.
     *
     * @return The IsRemoteEntry
     */
    public boolean getIsRemoteEntry() {
        return isRemoteEntry;
    }

    /**
     * Set the RemoteServer property.
     *
     * @param value The new value for RemoteServer
     */
    public void setRemoteServer(String value) {
        remoteServer = value;
    }

    /**
     * Get the RemoteServer property.
     *
     * @return The RemoteServer
     */
    public String getRemoteServer() {
        return remoteServer;
    }

    /**
     * _more_
     *
     * @param key _more_
     *
     * @return _more_
     */
    public Object getTransientProperty(Object key) {
        return transientProperties.get(key);
    }

    /**
     * _more_
     *
     * @param key _more_
     * @param value _more_
     */
    public void putTransientProperty(Object key, Object value) {
        transientProperties.put(key, value);
    }

    /**
     *  Set the Properties property.
     *
     *
     * @param key _more_
     *  @param value The new value for Properties
     *
     * @throws Exception _more_
     */
    public void putProperty(String key, Object value) throws Exception {
        getProperties(true).put(key, value);
    }

    /**
     * _more_
     *
     * @param key _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Object getProperty(String key) throws Exception {
        Hashtable properties = getProperties();
        if (properties == null) {
            return null;
        }

        return properties.get(key);
    }


    /**
     *  Get the Properties property.
     *
     *  @return The Properties
     *
     * @throws Exception _more_
     */
    public Hashtable getProperties() throws Exception {
        return getProperties(false);
    }


    /**
     * _more_
     *
     * @param force _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Hashtable getProperties(boolean force) throws Exception {
        if (properties == null) {
            if (propertiesString != null) {
                properties =
                    (Hashtable) Repository.decodeObject(propertiesString);
                propertiesString = null;
            }
            if ((properties == null) && force) {
                properties = new Hashtable();
            }
        }

        return this.properties;
    }


    /**
     *  Get the PropertiesString property.
     *
     *  @return The PropertiesString
     *
     * @throws Exception _more_
     */
    public String getPropertiesString() throws Exception {
        if (properties != null) {
            return Repository.encodeObject(properties);
        }

        return null;
    }


    /**
     *  Set the SubGroups property.
     *
     *  @param value The new value for SubGroups
     */
    public void setSubGroups(List<Entry> value) {
        subGroups = value;
    }

    /**
     *  Get the SubGroups property.
     *
     *  @return The SubGroups
     */
    public List<Entry> getSubGroups() {
        return subGroups;
    }

    /**
     * Set the SubEntries property.
     *
     * @param value The new value for SubEntries
     */
    public void setSubEntries(List<Entry> value) {
        subEntries = value;
    }

    /**
     * Get the SubEntries property.
     *
     * @return The SubEntries
     */
    public List<Entry> getSubEntries() {
        return subEntries;
    }

    /**
     * _more_
     *
     * @param name _more_
     * @param description _more_
     * @param parentEntry _more_
     * @param user _more_
     * @param createDate _more_
     * @param changeDate _more_
     */
    public void init(String name, String description, Entry parentEntry,
                     User user, long createDate, long changeDate) {
        this.name        = name;
        this.description = description;
        this.parentEntry = parentEntry;
        this.user        = user;
        setCreateDate(createDate);
        setChangeDate(changeDate);
    }






    /**
     * _more_
     *
     * @param o _more_
     *
     * @return _more_
     */
    public boolean equals(Object o) {
        if ( !o.getClass().equals(getClass())) {
            return false;
        }
        Entry that = (Entry) o;

        return Misc.equals(this.id, that.id);
    }



    /**
     * Set the CreateDate property.
     *
     * @param value The new value for CreateDate
     */
    public void setCreateDate(long value) {
        createDate = value;
    }

    /**
     * Get the CreateDate property.
     *
     * @return The CreateDate
     */
    public long getCreateDate() {
        return createDate;
    }



    /**
     * Set the ChangeDate property.
     *
     * @param value The new value for ChangeDate
     */
    public void setChangeDate(long value) {
        changeDate = value;
    }

    /**
     * Get the ChangeDate property.
     *
     * @return The ChangeDate
     */
    public long getChangeDate() {
        return changeDate;
    }

    /**
     * Set the Group property.
     *
     * @param value The new value for Group
     * @deprecated use setParentEntry
     */
    public void setParentEntry(Entry value) {
        parentEntry = value;
        if (parentEntry != null) {
            parentEntryId = parentEntry.getId();
        } else {
            parentEntryId = null;
        }
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public Entry getParentEntry() {
        return parentEntry;
    }



    /**
     * Set the ParentId property.
     *
     * @param value The new value for ParentId
     */
    public void setParentGroupId(String value) {
        setParentEntryId(value);
    }

    /**
     * _more_
     *
     * @param value _more_
     */
    public void setParentEntryId(String value) {
        parentEntryId = value;
    }

    /**
     * Get the ParentId property.
     *
     * @return The ParentId
     * @deprecated use getParentEntryId
     */
    public String xxxgetParentGroupId() {
        return getParentEntryId();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getParentEntryId() {
        return ((parentEntry != null)
                ? parentEntry.getId()
                : parentEntryId);
    }




    /**
     * Set the Name property.
     *
     * @param value The new value for Name
     */
    public void setName(String value) {
        name = value;
    }

    /**
     * Get the Name property.
     *
     * @return The Name
     */
    public String getName() {
        if (name == null) {
            name = "";
        }

        return name;
    }



    /**
     * Set the Description property.
     *
     * @param value The new value for Description
     */
    public void setDescription(String value) {
        description = value;
    }

    /**
     * Get the Description property.
     *
     * @return The Description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the Id property.
     *
     * @param value The new value for Id
     */
    public void setId(String value) {
        id = value;
    }

    /**
     * Get the Id property.
     *
     * @return The Id
     */
    public String getId() {
        return id;
    }

    /**
     *  Set the User property.
     *
     *  @param value The new value for User
     */
    public void setUser(User value) {
        user = value;
    }

    /**
     *  Get the User property.
     *
     *  @return The User
     */
    public User getUser() {
        return user;
    }



    /**
     * _more_
     */
    public void clearMetadata() {
        metadata = null;
    }


    /**
     * _more_
     *
     * @param value _more_
     *
     * @return _more_
     */
    public boolean addMetadata(Metadata value) {
        return addMetadata(value, false);
    }

    /**
     * _more_
     *
     * @param value _more_
     *
     * @return _more_
     */
    public boolean hasMetadata(Metadata value) {
        if (metadata == null) {
            return false;
        }

        return metadata.contains(value);
    }

    /**
     * _more_
     *
     * @param type _more_
     *
     * @return _more_
     */
    public boolean hasMetadataOfType(String type) {
        if (metadata == null) {
            return false;
        }
        for (Metadata myMetadata : metadata) {
            if (myMetadata.getType().equals(type)) {
                return true;
            }
        }

        return false;
    }



    /**
     * _more_
     *
     * @param value _more_
     * @param checkUnique _more_
     *
     * @return _more_
     */
    public boolean addMetadata(Metadata value, boolean checkUnique) {
        if (metadata == null) {
            metadata = new ArrayList<Metadata>();
        }
        if (checkUnique && metadata.contains(value)) {
            return false;
        }
        metadata.add(value);

        return true;
    }


    /**
     * Set the Metadata property.
     *
     * @param value The new value for Metadata
     */
    public void setMetadata(List<Metadata> value) {
        metadata = value;
    }

    /**
     * Get the Metadata property.
     *
     * @return The Metadata
     */
    public List<Metadata> getMetadata() {
        return metadata;
    }



    /**
     * _more_
     */
    public void clearAssociations() {
        associations = null;
    }


    /**
     * Set the Associations property.
     *
     * @param value The new value for Associations
     */
    public void setAssociations(List<Association> value) {
        associations = value;
    }

    /**
     * Get the Associations property.
     *
     * @return The Associations
     */
    public List<Association> getAssociations() {
        return associations;
    }


    /**
     * _more_
     *
     * @param value _more_
     */
    public void addAssociation(Association value) {
        if (associations == null) {
            associations = new ArrayList<Association>();
        }
        associations.add(value);

    }


    /**
     * Set the Comments property.
     *
     * @param value The new value for Comments
     */
    public void setComments(List<Comment> value) {
        comments = value;
    }

    /**
     * Get the Comments property.
     *
     * @return The Comments
     */
    public List<Comment> getComments() {
        return comments;
    }

    /**
     * _more_
     *
     * @param value _more_
     */
    public void addComment(Comment value) {
        if (comments == null) {
            comments = new ArrayList<Comment>();
        }
        comments.add(value);

    }



    /**
     * Set the Permissions property.
     *
     * @param value The new value for Permissions
     */
    public void setPermissions(List<Permission> value) {
        permissions   = value;
        permissionMap = new Hashtable();
        if (permissions != null) {
            for (Permission permission : permissions) {
                permissionMap.put(permission.getAction(),
                                  permission.getRoles());
            }
        }
    }

    /**
     * _more_
     *
     * @param action _more_
     *
     * @return _more_
     */
    public List getRoles(String action) {
        return (List) permissionMap.get(action);
    }


    /**
     * Get the Permissions property.
     *
     * @return The Permissions
     */
    public List<Permission> getPermissions() {
        return permissions;
    }




    /**
     * _more_
     *
     * @param value _more_
     */
    public void addPermission(Permission value) {
        if (permissions == null) {
            permissions = new ArrayList<Permission>();
        }
        permissions.add(value);

    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isDummy() {
        return isDummy;
    }





}
