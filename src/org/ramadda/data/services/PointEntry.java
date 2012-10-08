/*
 * Copyright 2010 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
 * http://www.unavco.org
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
 * 
 */

package org.ramadda.data.services;


import org.ramadda.repository.*;

import org.ramadda.data.record.*;

import org.ramadda.data.record.*;
import org.ramadda.data.record.filter.*;

import ucar.unidata.util.IOUtil;

import java.io.File;

import java.util.concurrent.*;


/**
 * This is a wrapper around  ramadda Entry and a RecordFile
 *
 *
 */
public class PointEntry extends RecordEntry {



    /**
     * ctor
     *
     *
     * @param recordOutputHandler output handler
     * @param request the request
     * @param entry the entry
     */
    public PointEntry(PointOutputHandler outputHandler, Request request,
                      Entry entry) {
        super(outputHandler, request, entry);
    }


}
