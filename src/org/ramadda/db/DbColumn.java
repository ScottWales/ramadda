/**
 * Copyright 2009 ramadda.org
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

package org.ramadda.db;


import org.w3c.dom.*;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.xml.XmlUtil;

import ucar.unidata.repository.*;
import ucar.unidata.repository.auth.*;
import ucar.unidata.repository.metadata.*;
import ucar.unidata.repository.output.OutputHandler;
import ucar.unidata.repository.output.OutputType;
import ucar.unidata.repository.type.*;



import java.util.ArrayList;
import java.util.List;


/**
 *
 */

public class DbColumn {

    private String name;
    private String label;
    

    public DbColumn(String name, String label) {
        this.name  = name;
        this.label = label;
    }



}

