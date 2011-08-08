/*
 * Copyright 1997-2010 Unidata Program Center/University Corporation for Atmospheric Research
 * Copyright 2010-  Jeff McWhirter, Don Murray
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.ramadda.repository.metadata;


import com.drew.imaging.jpeg.*;
import com.drew.lang.*;

import com.drew.metadata.*;
import com.drew.metadata.exif.*;
import com.drew.metadata.iptc.IptcDirectory;


import org.ramadda.repository.*;

import ucar.unidata.ui.ImageUtils;

import ucar.unidata.util.IOUtil;

import java.awt.Image;
import java.awt.image.*;

import java.io.File;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;


/**
 * A class for handling JPEG Metadata
 *
 * @author RAMADDA Development Team
 */
public class JpegMetadataHandler extends MetadataHandler {

    /** Camera Direction type */
    public static final String TYPE_CAMERA_DIRECTION = "camera.direction";


    /**
     * Construct a new instance for the repository
     *
     * @param repository  the repository
     *
     * @throws Exception  problems
     */
    public JpegMetadataHandler(Repository repository) throws Exception {
        super(repository);
    }


    /**
     * Get the initial metadata
     *
     * @param request  the request
     * @param entry  the entry
     * @param metadataList the metadata list
     * @param extra  extra stuff
     * @param shortForm  true for shortform
     */
    public void getInitialMetadata(Request request, Entry entry,
                                   List<Metadata> metadataList,
                                   Hashtable extra, boolean shortForm) {

        String path = entry.getResource().getPath();

        if ( !entry.getResource().isImage()) {
            return;
        }

        try {
            Image image = ImageUtils.readImage(entry.getResource().getPath(),
                              false);

            ImageUtils.waitOnImage(image);
            Image newImage = ImageUtils.resize(image, 100, -1);

            ImageUtils.waitOnImage(newImage);
            File f = getStorageManager().getTmpFile(request,
                         IOUtil.stripExtension(entry.getName())
                         + "_thumb.jpg");
            ImageUtils.writeImageToFile(newImage, f);


            String fileName = getStorageManager().copyToEntryDir(entry,
                                  f).getName();
            Metadata thumbnailMetadata =
                new Metadata(getRepository().getGUID(), entry.getId(),
                             ContentMetadataHandler.TYPE_THUMBNAIL, false,
                             fileName, null, null, null, null);

            metadataList.add(thumbnailMetadata);

        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }


        if ( !(path.toLowerCase().endsWith(".jpg")
                || path.toLowerCase().endsWith(".jpeg"))) {
            return;
        }
        try {
            File jpegFile = new File(path);
            com.drew.metadata.Metadata metadata =
                JpegMetadataReader.readMetadata(jpegFile);
            com.drew.metadata.Directory exifDir =
                metadata.getDirectory(ExifDirectory.class);
            com.drew.metadata.Directory dir =
                metadata.getDirectory(GpsDirectory.class);
            com.drew.metadata.Directory iptcDir =
                metadata.getDirectory(IptcDirectory.class);

            if (exifDir != null) {
                //This tells ramadda that something was added
                if (exifDir.containsTag(
                        ExifDirectory.TAG_DATETIME_ORIGINAL)) {
                    Date dttm =
                        exifDir.getDate(ExifDirectory.TAG_DATETIME_ORIGINAL);
                    if (dttm != null) {
                        entry.setStartDate(dttm.getTime());
                        entry.setEndDate(dttm.getTime());
                        extra.put("1", "");
                    }
                }

            }

            if (iptcDir != null) {
                // Get caption and make it the description if the user didn't add one
                if (iptcDir.containsTag(IptcDirectory.TAG_CAPTION)) {
                    String caption =
                        iptcDir.getString(IptcDirectory.TAG_CAPTION);
                    if ((caption != null)
                            && entry.getDescription().isEmpty()) {
                        entry.setDescription(caption);
                    }
                }

            }

            if (dir.containsTag(GpsDirectory.TAG_GPS_IMG_DIRECTION)) {
                Metadata dirMetadata =
                    new Metadata(getRepository().getGUID(), entry.getId(),
                                 TYPE_CAMERA_DIRECTION, DFLT_INHERITED,
                                 "" + getValue(dir,
                                     GpsDirectory
                                         .TAG_GPS_IMG_DIRECTION), Metadata
                                             .DFLT_ATTR, Metadata.DFLT_ATTR,
                                                 Metadata.DFLT_ATTR,
                                                 Metadata.DFLT_EXTRA);

                metadataList.add(dirMetadata);
            }

            if ( !dir.containsTag(GpsDirectory.TAG_GPS_LATITUDE)) {
                return;
            }
            double latitude  = getValue(dir, GpsDirectory.TAG_GPS_LATITUDE);
            double longitude = getValue(dir, GpsDirectory.TAG_GPS_LONGITUDE);
            if (longitude > 0) {
                longitude = -longitude;
            }
            double altitude = (dir.containsTag(GpsDirectory.TAG_GPS_ALTITUDE)
                               ? getValue(dir, GpsDirectory.TAG_GPS_ALTITUDE)
                               : 0);
            entry.setLocation(latitude, longitude, altitude);
            //This tells ramadda that something was added
            extra.put("1", "");
        } catch (Exception exc) {
            getRepository().getLogManager().logError("Processing jpg:"
                    + path, exc);
        }

    }


    /**
     * Get the value of a tag as a double
     *
     * @param dir  the directory
     * @param tag  the tag
     *
     * @return  the double value
     *
     * @throws Exception  couldn't create the double
     */
    private double getValue(Directory dir, int tag) throws Exception {
        try {
            Rational[] comps = dir.getRationalArray(tag);
            if (comps.length == 3) {
                int   deg = comps[0].intValue();
                float min = comps[1].floatValue();
                float sec = comps[2].floatValue();
                sec += (min % 1) * 60;
                return deg + min / 60 + sec / 60 / 60;
            }
        } catch (Exception exc) {
            //Ignore this
        }
        return dir.getDouble(tag);
    }


}
